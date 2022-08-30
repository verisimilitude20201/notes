# Dynamo Paper by Amazon

Current Location: Implementation
----------------------------------------------------------


## Abstract
1. Dynamo is a highly availaible key-value storage system that sacrifices consistency under certain failure scenarios to provide this level of availaiblity
2. Uses extensive object versioning and application-assisted conflict resolution in a novel way.


## Introduction
1. Strict operational requirements on Amazon in terms of performance, reliability and efficiency and scalability. 
2. Slightest outage can have worse financial outcomes.
3. Reliability & Scalability of a system running at Amazon scale depends on how well the application state is managed.
4. Amazon uses a decentralized, loosely coupled, service oriented architecture. 
5. Customers should be able to view/add items to their shopping cart even if there are tornadoes, network routes unavailaible. 
6. Failure handling should be treated as a normal case without impacting availaibility or performance.
7. Many services in Amazon only need a primary key access to a data-store. Such as session management, shopping cart, customer preferences, product catalog. Using the common pattern of a relational database would lead to inefficiences and limit scale and availaibility.
8. Dynamo provides a primary key interace to meet the requirement of this application
9. Technical overview
    - Data is partitioned by consistent hashing
    - consistency facilitated by object versioning
    - Consistency among replicas is maintained by a quorum-like technique and a decentralized replica synchronization protocol.
    - Employs a Gossip based distributed failure detection and membership protocol.
    - Completely decentralized.

## Background
1. Amazon's ecommerce platform consists of hundreds of services in an infrastructure consisting of thousands of servers hosted the world over.
2. Each services exposes a well-defined interface and is accessible over the network. 
3. Some of these services are stateless and others are stateful (service that generates response by executing business logic on it's state stored in persistent store.)
4. RDBMS is an overkill for Amazon's services that store and retrieve data only by a primary key. RDBMS requires expensive hardware and managment and highly skilled personnel. Availaible replication technologies are limited and choose consistency over availaibility
5. Dynamo has a simple key-value interface, highly availaible with a clearly defined consistency window, efficient in it's resource usage and simple scale out scheme to address groth in data-set or request rates.

## System assumption and requirements:

### Query Model
1. Simple read/write operations to a single data item uniquely identified by a key.
2. Value is a binary object.
3. Targets relatively small objects less than 1 MB
4. No operation can span multiple data items.

### ACID
1. Dynamo targets applications that operate with weaker consistency levels since this results in high availaibility
2. No isolation gurantees and permits only single key updates. 
3. Enough literature and experience proves that ACID transactions (logical grouping of updates to a database) hampers availaibility and prefers consistency.

### Efficiency:
1. Should perform on a commodity hardware infra.
2. Stringent latency requirements of 99.9 percentile.
3. No security requirements such as authentication and authorization. 
4. Each service should use it's own distinct version of Dynamo and it can scale upto 100 hosts.
5. Services need to be able to configure Dynamo to meet their performance and latency requirements.
6. Tradeoffs are in performance, cost-effectiveness, availaiblity and durability guarantees.


## SLAs
1. To guarantee that an application can deliver it's functionality in a bounded time, each and every dependency in the platform needs to deliver it's functionality even within more tightly bounded time.
2. SLA is a negotiable contract between client & services where they agree on several system related characteristics prominently including client's expected request rate distribution for a particular API and expected service latency under those considerations
3. An example of an SLA is a service guaranteeing a provide a response within 300ms for 99.9% of its requests for a peak client load of 500 requests per second. 
4. Amazon uses percentiles 99.9% for several of it's SLAs and not mean, average or median. Mean, average or median measures are not suitable if the goal of a system is to ensure all customers have a good experience.
5. The choice of 99.9 percentile is based on a cost-benefit analysis.
6. Storage systems play a very important role in deciding a service's SLA especially if the business logic is lightweight. State management becomes a crucial component of a service's SLA.
7. For Dynamo, the main design considerations is to give services control over their system properties such as durability and consistency and let services take their own tradeoffs between consistency, functionality, performance and cost effectiveness. 

## Design considerations
1. Data replication algorithms in commercial systems perfor synchronous replica coordination to provide a strongly consistent data access interface. These tradeoff availaibility of the data under certain failure scenarios.
2. Rather than dealing with the uncertainty of the correctness of an answer, data is made unavailaible until it's made absolutely certain.
3. For systems more prone to network failures, availaibility can be increased by allowing changes to propagate in the background and concurent, disconnected work is tolerated. This leads to conflicts which must be detected and resolved. Who resolves the conflicts? And when to resolve them?
4. Dynamo is designed to be an eventually consistent data store that is eventually all updates reach to all replicas.
5. An important design consideration - when to resolve conflicts ? during read time or write time. Traditional data systems keep the read complexity simple. Writes are rejected if they can't reach all replicas.
6. Dynamo is highly available, always writeable store. Rejecting customer updates would be a poor customer experience. This forces us to push the complexity of conflict resolution to the reads in order to ensure writes are never rejected.
7. Next design choice is who performs the conflict resolution. It can be data store or application. For the data store, the choices are quite limited. It can use simple policies like last writes win. Since application is aware of the schema, it can decide on the conflict. For the applictation that maintains shopping carts, they can merge the various conflicting versions to provide a unified view. 
8. Incremental scalability: Should be able to scale out one node at a time with minimal impact on the rest of the ssyten
9. Symmetry: Every Dynamo node shhould have the same set of responsibility as it's peers. Symmetric design simplifies system provision and maintenance. 
10. Decentralization: Design should favor decentralized peer-to-peer techniques over centralized control. 
11. Heterogenity: The system needs to be able to exploit heterogenity on the infra it's deployed on. The work distribution must be proportional to the capabilities of the individual servers.

## Related Work

### Peer-to-peer systems
1. Several peer-to-peer systems have been built looking at the problem of data distribution and data storage.
2. The 1st generation peer-to-peer networks were networks in which the overlay links were established arbitrarily. A search query usually flooded through the network to find as many peers as possible to share the data.
3. The structured peer-to-peer routing systems employed a globally consistent protocol to ensure that any node can efficiently route a search query to another node that has the required data. Routing mechanisms are used to ensure that the queries can be answered in desired number of hops. Also, each peer can maintain enough routing information so it can route requests to peers in a constant number of hops (O(1) routing)

### Distributed file systems and databases
1. Distributing data for performance, availaibility and durability has been widely researched and studied among the database community.
2. Distributed file systems support hierarchical namespaces and systems like Ficus and Coda replicate files at the expense of consistency. Update conflicts are managed using specialized conflict resolution procedures
3. Google File system is a master-slave architecture for storing the state of Google's applications. The master stores the meta-data and slave stores the actual data. Data is split into several chunks and replicated on these slaves also called as chunkservers. 
4. Some distributed file systems like Coda, Ficus are resilient to network outages and also allow disconnected operations. They differ in their conflict resolution mechanism - it can be system level or application level. All of them allow eventual consistency.
5. In a similar manner, Dynamo allows read/write operations to continue even in face of network outages
6. A key-value store is more suitable because
    - Used to store data of relatively smaller size < 1 MB
    - Easier to configure on per application basis
    - Dynamo does not support data integrity (like Antiquity distributed store which employs a log for data integrity and Byzantine failure resolution protocols). It's built for a trusted environment.
    - Dynamo targets applications that require only key/value access with primary focus on high availability where updates are not rejected even in the wake of network partitions or server failures.
7. Traditional replicated relational data systems provide strong consistency to replicated data and thereby are limited in scalability and availaibility. These are not capable of handling network partitions because they typically provide strong consistency guarantees.
8. Summary of techniques used in Dynamo and their advantages
    - `Problem:` Partitioning
      
      `Technique:` Consistent Hashing

      `Advantage:` Incremental scalability
    
    - `Problem:` High availaibility for writes
      
      `Technique:` Vector clocks with reconciliation for reads

      `Advantage:` Version size is decoupled from update rates
    
    - `Problem:` Handle temporary failures
      
      `Technique:` Sloppy quorum and hinted hand-offs

      `Advantage:` Provide high durability and availaiblity guarantees when few replicas are not availaible
    
    - `Problem:` Recovering from permanent failures
      
      `Technique:` Anti Entropy using Merkle trees

      `Advantage:` Synchronizes divergent replicas in the background
    
    - `Problem:` Membership and fraud detection
      
      `Technique:` Gossip based membership protocol and failure detection

      `Advantage:` Avoids a centralized store of node liveness and preserves symmetry.

## System architecture
1. For a storage system designed to operate in production, the following components are important
  - Data persistence
  - Load balancing
  - Scaling
  - Overload handling
  - Configuration management
  - Request marshalling
  - Request routing
  - Failure recovery
  - Replica synchronnization
  - State transfer
  - Concurrency handling
  - Membership and failure detection

2. The Dynamo paper focuses on core distributed system techniques used in Dynamo: Replication, Partitioning, Scaling, Failure handling, versioning and membership.

### System interface
1. Two very simple operations
  - get(key): Locates the object replicas associated with the key and returns a single object or list of objects with conflicting versions along with a context
  - set(key, value): Determines the object replicas and writes the value to those replicas.

2. A context encodes system metadata about the object that is opaque to the caller and includes the info such as the version. It's stored along with the object
3. Dynamo treats the key and value as an opaque array of bytes. Applies an MD5 hash on the key to generate a 128 bit identifier used to determine the storage nodes responsible for serving the key.

### Partioning Algorithm
1. Dynamo must scale incrementally through dynamic partitioning of data over a set of nodes.
2. It uses consistent hashing to distribute the load across storage hosts.
3. Consistent hashing treats the output hash space as a circular ring of hashes where the largest hash wraps around to the smallest hash. 
4. Each node is assigned a random value around this hash space.
5. Each data item's key is hashed and it's position in this hash space is determined. Walking in a clockwise manner from that position, the first node with a hash value larges than the data item that comes across stores this data item along with it's key.
6. The advantage of consistent hashing is that the arrival or departure of nodes only affects their immediate neighbors. 
7. The randomness in assigning nodes around the hash ring may lead to non-uniform data distribution. To prevent this, and bring about heterogenity, Dynamo has Virtual nodes in which each physical node gets mapped to several positions around the hash ring. How many virtual nodes per physical node to have depends on the node capacity and that brings about heterogenity in the infra.

### Replication
1. Each dataitem is replicated on N nodes where N is configured per instance.
2. A coordinator node is responsible for the replication of data items that fall within it's range. It ensures that it not only stores the key that falls in it's range but also replicates it across N - 1 storages hosts on the ring.
3. Each node becomes responsible for the portion of the ring between it and it's (N - 1)th predecessor.
4. The list of nodes that is responsible for storing a particular key is called it's preference list. The system is designed in a way that each node can know the preference list for a particular key. The preference list for a key skips virtual nodes and contains only distinct physical nodes.  

### Data versioning
1. Provides eventual consistency. Updates usually propagated to all replicas asynchronously.
2. A put() operation may return to it's caller without waiting for the update to propagate to all replicas and so a get() may return a stale value. 
3. Amazon's shopping cart can tolerate such inconsistencies. If the most recent state of cart is'nt available, the last known state is taken and product is added/removed in/from that state. When the most recent state is available, it's reconciled with the current state.
4. Dynamo treats each update (put operation) as immutable and allows multiple versions of the object to be present in the system. 
5. New versions subsume older versions. Oftentimes due to failures and concurrent updates, version branching occurs and Dynamo merges all conflicting operations leaving it upto the client to perform the reconciliation. 
6. Need to design applications that acknowledge the presence of such multiple version histories.
7. Dynamo uses vector clocks to determine causality between different versions of the same object. Vector clocks are a combination of (nodeId, counter) pairs with a clock getting associated with every version of an object. If all counters on a first object's clock are less than or equal to that of the second object's clock, clearly first object was an ancestor of the second and can be forgotten. If not, they have to be merged as conflicting updates.
8. If a client wishes to update an object, it must specify which version it is updating. It passes the context it received from a previous read operation which contains the vector clock information. If it cannot reconcile the version (using less-than logic) it returns all objects with their versions. Diagrammatically this can be shown as

                          |
                          |  (write by Sx)
                      D1([Sx, 1])
                          |
                          |  (write by Sx)
                      D2([Sx, 2])

                        (writes by Sy and Sz) 
            D3([Sx, 2], [Sy, 1])               D4([Sx, 2], [Sz, 1])
                           |
                           |

                D5([Sx, 2], [Sy, 1], [Sz, 1])
9. If several servers coordinate the writes to an objects, size of vector clocks may grow. This may not occur if we consider that writes are usually handled by top N nodes of a preference list. Still at times a clock truncation scheme may be applied to limit the size of vector clocks. Dynamo stores a timestamp when a node updates a data item along with the vector clock. Once the number of node, counter values exceeds a threshold, the oldest item in the list is removed. This problem however, has'nt been encountered much in Production though.

### Execution of get() and put() operations
1. Any Dynamo node is eligible to take in get() and put() operations on any key.
2. These operations are invoked using Amazon's infra-specific request processing framework over HTTP. 
3. Two strategies to select a node for a client request
  - Route the request through a generic load balancer that forwards the request to a Dynamo node on the basis of its load. In this case, the client does not have to link any Dynamo specific code in it's code. 
  - Use a partition aware client library that redirects the request to the appropriate coordinator node depending on the partioning strategy. This achieves a lower latency because it skips a significant forwarding step.

4. A node handling a read/write operation is called the coordinator node. Typically, it's the first node among the top N nodes that lie on the preference list. If a load balancer is used, the coordinator is chosen at random and if it's not among the preference list, the request is forwarded to the appropriate node.
5. Read/write operations typically access the top N healthy nodes in the preference list. If some of the nodes among this list are down, the nodes ranked lower in the preference list are accessed.
6. To maintain consistency, Dynamo uses a quorum like protocol. This has two configurable values - R (minimum number of nodes needing to participate in a read operation), W (minimum number of nodes that need to participate in a write operation). The system's consistency is good if W + R > N. However, the latency of a get() / put() is dictated by the slowest of W or R replicas and so W + R is chosen less than N to provide better latency.
7. For a put() request, the coordinator generates a vector clock and writes the value locally. It then sends the value to the N highest ranked nodes in the preference list. If at least W - 1 nodes respond, then the put() is successful.
8. For a get() request, the coordinator requests data for N highest ranked versions of the key and then waits for R responses before returning the result to the client. If the coordinator ends up gathering multiple versions, it returns all versions it deems that are casually unrelated according to the algorithm defined in the merging strategy. 


### Handling Failures - Hinted handoff
1. Traditional quorum might have reduced durability under failure conditions
2. Dynamo uses sloppy quorum: All reads/writes performed on the first N healthy nodes from the preference list which may not be from among the first N nodes while walking on the consistent hash ring. 
3. If a node A is down on the hash ring, the data meant for it would be sent to node B along with a hint it's meta-data including the intended recipient. 
4. Nodes receiving hinted replicas keep them in a separate local database that is scanned periodically. Upon detected that A has recovered, B will attempt to deliver it to A. Once the transfer is complete, B can delete it's local copy. 
5. Hinted hand-off ensures that read/write operations do not fail due to replica unavailaibility / node failures.
6. Applications needing highest levels of availaibility can set W = 1 ensuring a write passes as long as at-least a single node is availaible to durably write the node to it's local store.
7. The preference list of a key is constructed in a way such that the storage nodes are spread across several data centers. Each object is replicated across several data centers.

### Handling permanent failures: Replica synchronization
1. Dynamo implements an anti-entropy to keep all replicas synchronized. 
2. It uses Merkle trees to detect inconsistencies between replicas faster and minimize the amount of data transferred. 
3. A Merkle tree is a hash tree where leaves are hashes of the values of individual keys. Parent nodes higher in the tree are hashes of their respective children. 
4. If the hash values of the roots of two trees are equal, then the remaining tree must also be equal and replicas must be in sync. 
5. Each node maintains a separate Merkle tree for each key range (the set of
keys covered by a virtual node) it hosts.
6. Merkle tree has a disadvantage, many key ranges change when a node joins/leaves the system thereby the tree needs to be recalculated. 

### Ring membership and failure detection

#### Ring Membership
1. Dynamo uses an explicit mechanism to issue addition or removal of nodes from a Dynamo ring. 
2. An administrator uses a command line tool or a browser to connect to a Dynamo node and issue a
membership change to join a node to a ring or remove a node from a ring
3. The node that serves the request writes the membership change and its time of issue to persistent store.
4. A gossip-based protocol propagates membership changes and maintains an eventually
consistent view of membership.
5. A node contacts a peer chosen at random every second and the two nodes efficiently reconcile
their persisted membership change histories.
6. A node starts for the first time, it chooses its set of tokens (virtual nodes in the consistent hash space) and maps nodes to their respective token set. The mappings
stored at different Dynamo nodes are reconciled during the same communication exchange that reconciles the membership change histories.
7. Partitioning and placement information also propagates via the gossip-based protocol and each storage node is aware of the token ranges handled by its peers.
8. This allows each node to forward a key’s read/write operations to the right set of
nodes directly. 

#### External Discovery
1. It can happen that an administrator adds a node A and node B to the ring. These nodes each become members of the ring but are'nt aware of each other's existence. This gives rise to logical partitions. 
2. To prevent logical partitions, we can statically configure a set of nodes as seed nodes. 
3. Seeds are nodes that are discovered via an external mechanism and are known to all nodes. Because all nodes eventually reconcile their membership with a seed, logical partitions are highly unlikely.

#### Failure Detection
1. For the purpose of avoiding failed attempts at communication, a purely local notion of failure detection is entirely sufficient: node A may consider node B failed if node B does not respond to node A’s messages (even if B is responsive to node C's messages).
2. A node A quickly discovers that a node B is unresponsive when B fails to respond to
a message; Node A then uses alternate nodes to service requests that map to B's partitions. 
3. A periodically retries B to check for the latter's recovery.
4. Decentralized failure detection protocols use a simple gossip-style protocol that enable each node in the system to learn about the arrival (or departure) of other nodes. 
5. Early designs of Dynamo used a decentralized failure detector to maintain a
globally consistent view of failure state.
6. This is because nodes are notified of permanent node additions and removals by the explicit node join and leave methods and temporary node failures are detected by the individual nodes.

#### Adding or Removing storage nodes
1. On addition of a storage node X between nodes A and B, some A and B no longer have
to some of their keys and these nodes transfer those keys to X
2. When a node is removed from the system, the reallocation of keys happens in a
reverse process. 
3. This approach distributes the load of key distribution uniformly across the storage nodes,
which is important to meet the latency requirements.

## Implementation
1. Each storage node has 3 components viz. request coordination component, membership and failure detection and local persistence engine implemented in Java
  - Local Persistence component:
    - Allows for different storage engines to be plugged in.
    - Engines in use are Berkley DB Transactional Engine, MySQL store and an in-memory buffer with peristent backing store.
    - Applications choose the storage engine on the basis of their access patterns and their object size distribution.
  - Request coordination component: 
    - Built on top of an eventdriven messaging substrate where the message processing pipeline is split into multiple stages.
    - Each client request results in the creation of a state machine on the node that received he client request.
    - The state machine contains all the logic for identifying the nodes responsible for a key, sending the requests, waiting for responses, potentially doing retries, processing the replies and packaging the response to the client
    - For example for a read request:
        - send read requests to the nodes
        - wait for quorum number of responses
        - if too few replies were received within a given time bound, fail the request/
        - Gather all versions and determine which ones to be returned
        - if versioning is enabled, perform syntactic reconciliation and generate an opaque write context that contains the vector clock that subsumes all the remaining versions
        - After this, it waits for a short period of time to receive any outstanding responses.
        - If stale versions are received in any of the responses, it initiates a read repair on those nodes and repairs the nodes with the recent values. 
    - Any of the top N nodes in the preference list serve as coordinator nodes to coordinate the writes. 
    - Since each write usually follows a read operation, the coordinator for a write is chosen to be the node that replied fastest to the previous read operation which is stored in the context information of the request.
    - This optimization helps us to satisfy the read-your-own writes consistency. 

## Experiences and lessons learnt
1. Different services' use of Dynamo in Amazon differs by their divergent version reconciliation characteristics and read/write quorum
    - Business logic specific reconciliation: For example the shopping cart service merges all the versions of a customer's shopping cart. 
    - Timestamp based reconciliation: The session information service uses "last writes win" strategy so that the object with the largest timestamp is chosen. 
    - High performance read engine: 
        - For certain services having a high read request rate, we set R = 1, W = N. 
        - Some of these instances function as the authoritative persistence cache for data stored in more heavy weight backing stores. 
        - Services that maintain product catalog and promotional items fit in this category.
2. Client applications can tune the values of N, R and W to achieve their desired levels of
performance, availability and durability. N determines durability. W and R impact object availaibility and consistency. 

3. Low values of W and R can increase the risk of inconsistency as write requests are deemed successful and returned to the clients even if they are not processed by a majority of the replicas. 

4. A common understanding is that durability and availaibility go hand-in-hand. This is not necessarily true. The vulnerability window for durability can be decreased by increasing W.This may cause rejected requests because more storage nodes need to be availaible for servicing the write request thereby decreasing availaiblity.

5. A Common (N, R, W) configuration that gives optimal durability, consistency and availaibility is (3, 2, 2). 

### Balancing performance and durability

1. To provide a consistent user experience, Amazon sets their performance criteria at higher percentiles i.e. 99th percentile or 99.99th percentile. Typical SLA of the services that use Dynamo is 99th percentile of read requests return within 300 ms.
2. The involvement of multiple storage nodes whose performance is limited by the slowest R or W servers and use of commodity servers that have much reduce I/O throughput than high-end servers makes performance a daunting task for Dynamo.
3. At peak time (noticed in December 2006), write latencies are higher than read latencies. The 99.9th percentile latencies are around 200 ms and are an order of magnitude higher than the averages. For a number of services this performance levels are accepted.
4. Certain few customer level services require a higher levels of performance. For these services, Dynamo provides the ability to trade-off durability guarantees for performance. In this case each storage node maintains a buffer in main memory. 
5. Write operation gets periodically written to storage for object stored in the buffer. Reads check if the key is in buffer and serves it from the buffer itself if present. 
6. This write buffering smoothes out higher percentile latencies and reduces the 99.9th percentile latency by a factor of 5 during peak traffic. 
7. The write operation is refined for the coordinator to choose one out of N nodes to make a durable write.
8. Since the coordinator waits only for W responses, the performance of the write
operation is not affected by the performance of the durable write operation performed by a single replica.

### Ensuring uniform load distribution