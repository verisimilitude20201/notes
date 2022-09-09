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