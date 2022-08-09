# Dynamo Paper by Amazon

Current Location: 4 System architecture
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