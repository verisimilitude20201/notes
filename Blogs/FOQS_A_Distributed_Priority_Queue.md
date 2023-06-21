# FOQS - Scaling A Distributed Priority Queue

Link: https://engineering.fb.com/2021/02/22/production-engineering/foqs-scaling-a-distributed-priority-queue/

Application ---> 1  ---->        3|3|2|1|1       ----->  Consumer 1
                                Priority Queue
                                                 ----->  Consumer 2



## Introduction
1. The entire Facebook ecosystem is powwered  by hundreds of microservices many of which benefit from asynchronous processing.
2. Several benefits to async processing
    - Better resource utilization
    - Improved system reliability
    - Allow systems to schedule compute on a future date
    - Helping microservices communicate dependably.

3. A Queue is needed to store work that needs to happen asyncly or passed from one service to another. 
4. Facebook Ordering Queuing Service (FOQS) is a horizontally scalable multitenant, persistent distributed priority queue built on top of shared MySQL that enables developers to decouple and scale microservices and distributed systems.       

## FOQS components. 
1. A Producer enqueues items to be processed. These can have a priority and/or a delay if item processing has to be deferred.
2. Consumer uses a get topics API to discover topics have items. Each topic is a logical priority queue. Consumer dequeues items from the topic either in-line by doing it itself or sending it to a processor pool. If processing succeeds, it acks the item back to the queue. if not, the items are nack'ed and redelivered to consumers

## Use cases
1. Async: Facebook's asynchronous compute platform handling notifications, to integrity chekcs, to scheduling posts for the future. Leverages FOQS ability to hold large back-log of work items to defer running use cases. 
2. Video encoding service which powers asynchronous video encoding.
3. Language translation technologies which are computationally expensive and benefit from parallelization by being broken up into multiple jobs.

## Building on a distributed Priority queue
1. FOQS stores items that live in a topic in a namespace. 
2. Consists of a Thrift API exposing the operations: enqueue, dequeue, ack, nack, GetActiveTopics. 
3. Uses a shard manager which is an internal service to assign shards to host. 

### What is an item
An item is a  message in the priority queue. It consists of below fields.
1. Namespace: The unit of multitenancy in FOQS
2. Topic: A priority queue. One namespace can have many topics.
3. Priority: A lower number signifies a higher priority. 
4. Payload: An immutable binary blob <= 10 KB
5. Metadata: Again a mutable binary blog <= 100 bytes
6. Dequeue delay: Timestamp after which an item should be dequed.
7. Lease duration: The time during which an item dequeued by the consumer needs to be nacked or acked. IF the consumer does neither, FOQS can redeliver the item on the basis of retry policy.
8. FOQS assigned unique ID
9. TTL how long items live in the queue.

Each item in FOQS ==> One row in a MySQL table. ID is assigned on enqueue.

### what is a topic
1. Logical priority queue specified by a user-defined string. Contains items and sorts them by priority and dequeue delay value. 
2. Topics are dynamic and get created by enqueuing an item and specifying a topic
3. Since topics are dynamic, FOQS provides an API for developers to discover the topics by querying the active topics (with at-least one item)

### What is a namespace:
1. Corresponds to a queuing use case. It's the unit for multi-tenancy for FOQS. 
2. It gets a guaranteed capacity say number of enqueues per minute.
3. Namespaces share the same tier i.e. an ensemble of hosts and MySQL shareds) and not affect one another.

### Enqueue         
                                                    | 
                                                    |----> Enqueue Worker --> Shard 1
                                                    |
Enqueue API Call --------> Enqueue FIFO Buffer -----|----> Enqueue Worker --> Shard 2
                                                    |
                                                    |----> Enqueue Worker ---> Shard 3 
                                                    | 
Figure shows single host view of the enqueue design (MySQL shards are external from the host)

1. Enqueue request arriving at an FOQS host gets buffered in a FIFO Buffer queue and returns a Promise
2. Each MySQL sharrd has a corresponding Worker that's reading items form buffer and inserting into MySQL shard.
3. Once that insert is completed, the promise is fulfilled and a response is returned. 
4. FOQS uses a circuit breaker pattern to mark unhealthy shards. Health is defined by slow queries ( > x ms average over a rolling window). If a shard is unhealthy, a worker stops accepting work until it's healthy.
5. On success, enqueue returns a unique ID for each item. This ID is the shard ID and a 64-bit primary key in the shard. This combination identifies every item in FOQS.

### Dequeue
1. Dequeue API accepts a collection of (topic, count) pairs. 
2. For each topic requested, FOQS will return, at most, count items for that topic. The items are ordered by priority and deliver_after, so items with a lower priority will be delivered first.
3. When an item is dequeued, its lease begins. If the item is not acked or nacked within the lease duration, it is made available for redelivery.
4.  FOQS supports both at least once and at most once delivery semantics. If an item is to be delivered at most once, it will be deleted when the lease expires; if at least once, redelivery will be attempted.
5. Shards store items ordered by priorities.  
6. Prefetch Buffer works in the background and fetches the highest priority item across all shards and stores them to be dequeued by the client. 
7. A shared maintains an in-memory index of items ordered by priority that are ready to deliver. 
8. This allows the Prefetch Buffer to efficiently find the highest priority primary keys by doing a k-way merge and running select queries on those rows.
9. To avoid double delivery, they are marked as "delivered" in the shard.
10. Prefetech buffer will keep track of client dequeue rate and a background replenishment thread will replenish the buffer according to this rate.
11. Topics that are being dequeued faster will get more items put in the prefetch buffer
12. The Dequeue simply dequeues the items from this buffer

### Ack/Nack
1. Ack signifies that an item was successfully delivered, does'nt need to reprocess.
2. Nack signifies that an item should be redelivered because the client needs to process it again. It could be deferred allowing the client to use exponential backoff when processing failing items.
3. Because each MySQL shard is owned by at most one FOQS host, an ack/nack request needs to land on the host that owns the shard. Shard Id is contained in every item and so FOQS client uses the shard to locate the host via the Shard manager service.
4. Once the ack/nack is routed to the correct host, it gets sent to a shard-specific in-memory buffer
5. A worker pulls items from this ack buffer and deletes those rows from the MySQL shard
6. A worker pulls items from the nack buffer it updates the row with a new deliver_after time and metadata (if the client updated it)
7. If the ack or nack operations get lost for any reason, such as MySQL unavailability or crashes of FOQS nodes

### Push Vs Pull
1. FOQS uses a pull mechanism where consumers use the dequeue API to pull items from the prefetch buffer. To understand the choice of pull mechanism, let's look at the diverse workloads for FOQS
    - End-to-end processing delay needs: End-to-end processing delay is the delay between an item becoming ready and when it gets dequeued by a consumer. There is a mix of short delays (few milliseconds) and long delays (few days) here
    - Rate of consuming is heterogenous depending on the availaibility  of downstream resources
    -  Topics vary in their priority of processing at the topic level or at individual item level within a topic.
    - Certain topics and items need to be processed in specific regions to ensure affinity to data they are processing
2. Pull model enables serving diverse needs by keeping the consumers more flexible. However, Consumers must discover where data is located and pull it at an appropriate rate based on end-to-end processing latency needs.
3. This challenge is addressed by adding a routing component providing more flexibility to the consumers with respect to where and how they process items.


## Running FOQS at Facebook scale
1. FOQS experienced tremendous growth and processes close to 1 trillion items per day.
2. To handle this load, FOQS has some optimizations

### Checkpointing
1. FOQS has background threads for marking deferred items ready to deliver, expiring lease and so on. For example: If we want to mark the states of all items ready to be delivered, we would need a query selecting all rows with timestamp_col <= UNIX_TIMESTAMP()
2. The problem with such a query is that MySQL needs to lock updates to all rows with timestamp <= now.  It does this by keeping old versions of the row around in a linked list. The longer the list, the slower are the read queries.
3. With checkpointing, FOQS maintains a lower bound on the query (the last known timestamp processed), which bounds the where clause. The where clause becomes
WHERE <<<checkpoint>>> <= timestamp_column AND timestamp_column <= UNIX_TIMESTAMP()

### Disaster Readiness
1. Facebook's infrastructure needs to be able to withstand the loss of entire data centers.  
2. Each FOQS MySQL shard is replicated to two additional regions. The cross-region replication is asynchronous, but the MySQL binlog gets persisted to another building in the same region synchronously.
3. If MySQL databases in a region with the primary need to be downed for maintenance,  MySQL primary is temporarily put into read-only mode until the replicas can catch up. 
4. Once a replica catches up, it's promoted to primary. Now the shard needs to get reassigned to a new FOQS host in that region. This minimizes the amount of cross-region network traffic, which is comparatively expensive
5. Promotion of replicas to primary causes large capacity imbalances across regions and it has improved it's routing so that enqueues go to hosts with enough capacity and dequeues go to hosts with highest prioriry items
6. Enqueue forwarding: Forward an enqueue request to nodes having capacity.
7. Global rate limiting: Since namespaces are the unit of multitenancy for FOQS, each namespace has a rate limit (computed as enqueues per minute). It's enforced globally across regions.

## Challenges ahead
1. Handling failures of multiple domains i.e. regions, racks, data centers.
2. More effective load balancing and discoverability of items during dequeues.
3. Expand the feature set to include more developer needs around workflows, timeers and strict ordering