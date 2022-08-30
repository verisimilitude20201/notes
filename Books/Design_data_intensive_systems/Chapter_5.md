# Replication

## Introduction

1. Replication implies keeping a copy of same data on multiple machines connected via network. 
2. Several reasons to replicate 
    - Keep data geographically close to the users.
    - Allow system to continue functioning even though some of it's parts have failed.
    - Scaling out the number of machines that can serve read queries. 
3. Let's assume that our dataset is small enough so that each machine can hold a copy of the dataset. 
4. The difficulty lies in replication having to handle changes to replicated data. 3 popular algorithms exist for this single-leader, multi-leader and leaderless and each has it's own pros/cons.
5. Many trade-offs too with replication - how to handle failed replicas,  whether sync or async
6. Fundamental concepts of replication have'nt changed much since the 1970s. Many developers continued to assume single-node databases and the usage of mainstream distributed databases has been very recent (still 10+ years now !)

## Leaders and followers (Single leader/Master slave replication)
1. With multiple replicas, how do we ensure that eventually all data ends up on all replicas? 
2. A write needs to be processed by each replica otherwise the database would be inconsistent.
3. The most common solution is master-slave replication.
    - One of the replicas is denoted as the leader. All write requests are sent by the client to this master which is written to it's local storage.
    - The other replicas are known as followers (secondaries or hot standbys). Whenever the leader writes new data to its local storage, it also sends data changes in a replication log/change stream to all it's followers. Each follower takes the log from the reader and updates it's local copy of the database accordingly.
    - A client can query/read either from the leader or any of the followers. Writes are only meant for leaders
    - Follower are read-only replicas.
    - This replication is default of many relational databases. 
    - Distributed message brokers like Kafka, RabbitMQ highly availaible queues also use it.

### Synchronous or Asynchronous?
1. It may be a configurable option for some DB systems and for some others they may tend towards sync or async.
2. Consider that a user is changing his profile pic. There is one leader and two followers. At some point, the client sends the read request to the leader which writes it to it's local storage and creates a replication log of this write and sends it to it's followers.
    - Follower 1: Receives the log, updates it's local copy and acks the leader. After this the leader returns success to the client. This is synchronous replication. For sync replication, the follower is guaranteed to have the latest copy of data. The disadvantage is if the follower does'nt respond, the leader can't signal a success to the client. It's blocked. 
    - Follower 2: The leader sends it the log but does'nt wait for it's response. This is asynchronous replication. There might be a substantial delay before follower 2 processes the message. Normally it takes about a few seconds. But there might be circumstances where the follower might fall behind the leader by several minutes or more due to being down, network problems. This problem is called replication lag and the follower is said to lag behind the leader

3. Semi-synchronous: Obviously, we cant have all followers to be sync. Even if one node has an outage, it would bring the system to a grinding halt. So, on a database we would have one synchronous follower and others are async. We can have up-to-date data on at-least 2 nodes - one leader and one follower.
4. Often leader-based replication is configured to be async. But if the leader fails and any writes have not been replicated to the followers are lost. An advantage to full-async replication is leader can continue processing writes even if all followers have fallen behind. Weakend durability is a bad trade-off still this is used especially in case of many geographically distributed followers
5. Researchers have continued investigating replication that do not lose data but still provide good replication and availaibility. Chain replication is a variant of synchronous replication implemented in a few systems like Microsoft Azure. Strong connection between consistency of replication and consensus (getting several nodes to agree on a value) 

### Setting up more followers
1. New followers are set up to increase the number of replicas or to replace failed nodes. A challenge is to ensure that the new follower has an accurate copy of the follower's data
2. Since the database is always in flux, a simple data copy might not work. 
3. We could make the files consistent on disk by locking the database, but it would go against high availaibility.
4. Setting up a follower can be done without downtime
    - Take a consistent snapshot of the database at some point in time (without locking). Most databases have this feature. 
    - Copy the snapshot to the new follower node
    - The follower connects to the leader and requests all changes since the last snapshot taken. The snapshot should be associated with an exact position sequence in the leader's replication log. It's called Log Sequence number in PostgreSQL and MySQL calls it binlog coordinates.
    - When the new follower has processed the backlog of data, it is said to have followed up with the leader.
5. The above flow different for different databases. May be semi-automated at times. 

### Handling node outages
Nodes in the system can go down due to planned maintainance (security patches). The goal is to keep the whole system running despite individual node failures. 

#### Leader failure: Failover
1. Handling a leader failure is tricky - one of the follower needs to be promoted to the leader. Clients need to reconfigure to send their writes to the new leader and the other followers need to start consuming from the new leader.
2. Failover can be automatic or manual by an administrator. 
3. Automatic failover process steps
    - Determine if the leader has failed: Most systems use a timeout. Nodes periodically send messages to each other and if a node does'nt respond for a period of 30 seconds, it's assumed to be dead. 
    - Choose a new leader: Can be done through an election process or a new leader can be appointed by a previously elected controller node. The best candidate for a leader is the replica with the most up-to-date data changes from the old leader. Getting all nodes to agree on a leader is a consensus problem
    - Reconfigure the system to use the new leader: Clients now send their write requests to the new leader. If the new leader comes back, it might believe itself to be the new leader. The system needs to ensure that it becomes a follower and recognize the old leader.
4. Automatic Failover fraught with so many dangers
    - In case of async replication, the new leader may not have received all updates from the old one before it's failure. If the old one rejoins the cluster after a new one has been chosen, the new leader would receive conflicting writes. The most common solution is to discard the old leader's writes in this case but it affects durability
    - Discarding the writes is dangerous if other storage systems outside of the database need to coordinate with the database contents. At Github, an out-of-date follower was made the leader. It used auto-incrementing primary keys for new rows. Certain primary keys were already used by the previous leader which were used in a Redis store. This caused inconsistency and some private data was disclosed to the wrong users.
    - Split brain scenario can happen where two nodes believe they are the leader. This is dangerous. Some systems have mechanisms to shut down one node if two leaders are detected. If this mechanism is not used carefully, you can end up both nodes being shut down.
    - Keeping the correct timeout to detect node failures is also complex. Longer timeout means longer time to recovery in case the node fails. If timeout is too short, there could be unecessary fail overs.

5. Node failures, unreliable networks, trade-offs around replica consistency, availaibility, durability, latency are fundamental problems in distributed systems. 

### Implemenation of Replication logs in leader-based replication systems

#### Statement-based replication
1. In the simplest case, the leader logs every write statement that it executes to the statement log and sends it to it's followers. 
2. Every INSERT, UPDATE, DELETE is forwarded to the followers via the log and each follower executes it as though it has been received from a client.
3. This can break down in the following scenarios
    - A statement calling RAND() or NOW() will likely give a different value on each replica
    - Statements using an auto-incrementing column or if they depend on existing data must be executed in exactly the same order or they may have a different effect. 
    - Statements having side-effects may result in diffferent side-effects occuring on each replica unless the effects are absolutely deterministic.
4. Since there are so many edge cases, other replication methods are preferred. VoltDB uses statement based replication and it requires transactions to be deterministic. MySQL has not switched to row-based replication after version 5.1

#### Write-ahead log shipping (WAL)
1. Storage engines append every write to a log file
    - In case of log-structured-merge storage engine, this log is the main place for storage. Log segments are merged, compacted and garbage collected in the background.
    - In case of B-tree which overwrites individual disk blocks, every modification is first written to a WAL so that the index can be restored to a consistent state after crash.
2. Log is an append-only sequence of bytes containing all writes to the database. It can be used to build the same replica on another node. 
3. The leader sends this log over the network to it's followers, and the follower processes this log and builds a copy of the same exact data structures found on the leader.
4. The main disadvantage of this is the log contains details of which bytes changed in which disk blocks. This is very low level and makes the replication closely coupled with the storage engine. 
5. If the database changes it's storage format from one version to another, it's not possible to run different versions of the database software on the leader and followers
6. If the database allows the follower to use a newer version software than that of the leader, you can perform a zero downtime upgrade by first upgrading the followers and then performing a failover to make one of the upgraded nodes as the new leader. 

#### Logical (Row) based replication
1. An alternative to use different log formats for replication and storage engine which allows the replication log to be decoupled from storage engine internals. This is called as logical log since it's decoupled from storage engine internals.
2. Logical log for relational databases is usually a sequence of records describing writes to database tables at the granularity of a row. 
    - For an inserted row, the log contains the new values of all columns.
    - For a deleted row, the log contains enough info to uniquely identify the row that was deleted. 
    - For a uodated row, the log contains enough info to uniquely identify the row that was updated including the new values of all columns that changed. 
4. If a transaction modifies several rows, it generates several such log records followed by an indicator that the transaction was commited.
5. This replication strategy allows the leaders, followers to run different versions of database software or different storage engines. 
6. Logical log format is also easier to parse for external applications. This is useful if you want to send the contents of the log to an external system or for building custom indexes or caches. This is called change data capture.

#### Trigger based replication
1. So far, the approaches covered are basically handled by the database. But if you want to replicate just a subset of data or want to replicate from one kind of database to another, then you move the replication logic up to the application layer.
2. One approach would be to use triggers and stored procedures.
3. A trigger lets you write custom application code that gets triggered when a data change occurs (write transaction). It can log this change to a separate table and/or replicate the change to some other system.
4. This has greater overheads than other replication methods and more prone to bugs than the database's built-in replication. 

### Problems with replication lag
1. We use replication to tolerate node failures, handle scalability (processing more nodes than a single machine can handle), latency (placing replicas geographically closer to the users)
2. All write queries in leader-based replication go to a single leader node but read queries can go to any replica. 
3. A good option is whenever we have a small percentage of writes but multiple reads, we can create many followers and distribute the read requests among followers. This remove the load from the leader. 
4. This is a read-scaling architecture and you can keep on adding more followers to scale reads. But, this works good in an asynchronous replication approach. Synchronous replication might block till the updates propagate and are commited successfully to all followers. 
5. Unfortunately, if an application reads from an asyc follower, it may see outdated information if the follower has fallen behind. This may lead to inconsistencies if you read data from the leader and a follower, you may get different results. 
6. This is just a temporary state. If you would pause the writes, the followers would eventually catch up with the leader. This is called eventual consistency.
7. There's no limit to how a follower can fall behind. The delay between a write happening on the leader and being reflected on the follower - the replication lag may be a fraction of second or couple of hours. 
8. Let's highlight 3 examples of problems that might occur with replication lag and how we could solve them.
    - Reading your own writes
        - Application give users to submit some data and view what they've submitted. For example: Upload a new profile pic, username, change address. 
        - This data gets first sent to the leader  but when the user views the changes, it can be read from a follower. This is appropriate if data is frequently read rather than written. 
        - If we use async replication, if the user sees the data after making write, the data may not have reached the replica. To the user, it looks like the data they submitted was lost. 
        - This situation implies that we need read-after-write consistency also known as read-your-writes. For instance, if a user uploads his profile pic, and reloads the page, they are guaranteed to see the updated pic they just uploaded. 
        - Does not guarantee about other people's updates. 
        - In a leader based system, few techniques to implement this.
            - When reading something a user has modified, read it from the leader. Otherwise, read it from the follower. For example: User profile information on a social network.
            - If most things in the application are modifiable by the user, above technique is not effective. We could track the time of last update and for some short time, after the last update make all reads from the leader.
            - Client remembers the timestamp of it's last write and system can ensure the replica serving updates until at least that timestamp. Provided the replica is not sufficiently up-to-date, another replica might be chosen or the write may wait.  The timestamp could be a logical timestamp or a actual system clock (needs clock sync)
            - For multi-datacenter cluster set-up, any request that needs to be served by the leader must be routed to the data center containing the leader. 
    
    - Cross-Device Read after writes:
        - This means if the user enters some information, on one device and views it from other device they should be able to see it. 
        - Few additional issues
            - Approaches that require remembering the last update timestamp are more difficult because code running on one device does'nt know what updates happend on other device. Need to centralize this information
            - Connections from different devices of the same user need to be routed to the same data-center. 
    
    - Monotonic Reads
        - Use of async followers may see things moving back in time. This happens if the user makes several reads from different replicas. 
        - If we read first from a follower with a little lag and then from a follower with more lag, it may appear as few updates just disappeared.
        - Monotonic reads is a guarantee that this does not happen. When you see data, an old value might be seen. After several reads done in sequence, they will not see time going backward i.e they will not read older data having previously read newer data. 
        - One to ensure monotonic reads from not happening is re-routing all read requests for a user to the same replica. 
    
    - Consistent Prefix reads: 
        - Violations of causality. 
        - For example: imagine below conversation
            Mr Poon: How far you can see into the future, Mrs Cake?
            Mrs Cake: About 10 seconds usually.
            If a 3rd person is listening to this through followers. The things said by Mr Poon go through follower having huge lag but the follower relaying Mrs Cake's words has little lag. This observer would first hear Mrs Cake and then Mr Poon.
        - Preventing this type of anomaly required Consistent Prefix reads guarantee. If sequence of writes happens in certain order, anyone reading those writes will see them in the same order.
        - Particularly a problem with sharded databases. Shards in many distributed databases operate independently. No global ordering of writes as such. When user reads from a database, they may see few parts in older state and few having newer state.
        - We can redirect all casual writes to same shard. There are algorithms keeping track of causal dependencies. 

### Solutions for replication lag 
1. If for an application using an eventually consistent database, if there is no issue with the replication lag increasing to a few minutes then it's fine. Otherwise it's a problem and should design the system to have a stronger guarantee.
2. In some cases, there are ways to giving this stronger guarantee by doing certain reads from the leader. Dealing with these issues in application code is complex and easy to get wrong.
3. Transactions existed stronger guarantees so that the application side can be simpler.
4. Single node transactions have existed since a very long time. In the move to distributed databases, many systems have abandoned them claiming that they are expensive for performace and availaibility and asserting that eventual consistency is inevitable. 

## Multi-leader replication

1. A natural extension of the leader based replication model is allow more than one node to accept writes. 
2. Replication happens in the same way, each node processing a write must forward the change to all nodes. This is called multi-leader configuration or active-active replication.

### Use cases for multi-leader replication

#### Multi-data center operation
1. If you have a database with replicas in several different data centers, in a multi-leader configuration, you can have a leader in each data center. Within each data center normal leader-follower replication can be used. Each data center's leader replicates changes to leaders in other data centers. 
2. Let's see how single-leader and multi-leader fare in a multi-data center deployment
    - Performance:
        - In a single leader, every write goes to the data center with the leader over the internet. This adds latency to writes. 
        - In multi-data center every write can be processed locally with it getting replicated asyncly to other data centers. Inter-data center delay is hidden from users. 
    - Tolerance of outages
        - In a single leader, if the datacenter with the leader fails, failover can promote a follower to be the leader. 
        - In a mult-data center, each datacenter can operate independently of the other and replication can make the failed data center catch up when it comes online.
    - Tolerance of network problems
        - Single datacenter is very sensitive to network problems because writes are made synchronously over internet (inter-data center)
        - Multi-leader configuration with async replication can tolerate network problems better.
3. There are big downsides to multi-leader replication
    - Same data may be concurrently modified in two different data centers and those write conflicts must be resolved. 
    - Retrofitted feature in most databases. Subtle configuration pitfalls and surprising interactions with other databases features such as auto-increment keys, triggers, integrity constraints.

#### Clients with offline operation
1. Another use case for multi leader - If you have an application that should continue to work while it's disconnected from the internet. 
2. Calendar apps on phone. You need to be able to see meetings, read requests, at any time regardless of whether device has an internet connection. Every device has a local database which acts as leader and an async multi-leader replication process syncs between the replicas of your calendar on all your devices once you go online. 
3. Multi-leader replication is a tricky thing to get right. 
4. Couch DB is designed for this mode of operation. 

#### Collaborative editing
1. Collaborative editing applications allows several people to edit a document simultaeneously. For example: Etherpad and Google docs
2. This is not a database replication problem. But it has lot in common to previously mentioned offline editing use-case. When a user edits a document, changes are instantly reflected in their local replicas (state of the document in their browsers) and asynchronously replicated to the server or other users editing the same document. 
3. To guarantee no editing conflicts, application must acquire a lock before a user can edit it. If another user wants to edit the same document, they first have to wait till the first user has commited the changes and released the lock.
4. For faster colloboration, the unit of change should be very small and avoid locking. This requires conflict resolution. 

### Handling write conflicts
1. Conflict resolution is required for multi-leader replication. 
2. If a Wiki page is being edited by two users and if user 1 changes the title of the page from A to B and user 2 changes it from A to C. Both changes are applied at the same time to their local databases and give rise to conflicts during replication. 

#### Synchronous Vs Asynchronous conflict detection
1. In a single leader database, the other writer will block and wait for the 1st write to complete. It may also abort the second write transaction. 
2. For multi-leader replication both writes are successful and conflict is detected asyncly at some later point in time. It may be too late to ask the user to resolve the conflict. 
3. We could use the single leader conflict resolution mechanism for multi-leader as well. Wait for the write to be replicated to all replicas.

#### Conflict avoidance. 
1. The simplest strategy is to avoid conflicts. If the application can ensure that all writes for a particular record go through same leader, conflicts cannot occur. 
2. For example: in an application where users can edit their own data, you can ensure that requests from a particular user always go to the same datacenter and use leaders in that datacenter for reading and writing. 
3. If a datacenter has failed or if the user has either moved to a different location, you need to reroute traffic to another datacenter. In that case, conflict avoidance breaks down and you need to deal with the possiblity of concurrent writes on different leaders.

#### Converging to a consistent state
1. A single leader database applies writes in a sequential order. If there are several updates to the same field, the last write determines the final value of the field. 
2. In a multi-leader configuration, there is no defined ordering of writes. If each database applied the writes in the order it which it saw them, the database would end up in an inconsistent state.
3. Every replication system must ensure all data is eventually the same in all replicas. Thus, the database must resolve conflict in a convergent way which means all replicas must arrive at the same final value once all changes have been replicated. 
4. Several approaches of converging conflict resolution
    - Give each write a unique ID (timestamp, random number a UUID or hash of the key) and pick the write with the highest ID as the winner. This is called Last Writes Win (LWW). It's prone to data loss even though it's popular. 
    - Give each replica a unique ID and let writes originating at higher replicas take precedence over writes at lower numbered replicas. 
    - Merge the values together and concatenate them. 
    - Record conflicts in an explicit data structure and write application code that resolves the conflict at some later time.

##### Custom conflict resolution logic
1. Most multi-leader replication tools let you write conflict resolution logic using application code. That code may be executed on-read or on write
    -  On write:
        - A databases detects a conflict in the log of replicated changes and executes a conflict handler. 
        - This handler runs in a background process and it must execute quickly. 
    - On read: 
        - On conflict, all conflicting writes are stored. 
        - On read, this multiple versions of the data are returned to the application. 
        - The application may ask the user to resolve the conflict or automatically resolve it writing the reconciled data to DB.
2. Conflict resolution usually happens at the level of a individual row or document not for an entire transaction. 
3. A transaction doing several different writes, each write is still considered separately for the purpose of conflict resolution.
4. Automatic conflict resolution rules can become quickly complicated and custom rules written for the same can be error-prone. Amazon's shopping cart is a good example here. Some interesting researches have been done into automatically resolving conflicts due to concurrent modifications
    - Conflict free replicated datatypes (CRDTs) are a family of data structures for sets, maps, ordered lists that can be concurrently modified and resolve conflicts in sensible ways.
    - Mergeable Persistent Data Structures: Track version history explicitely, similar to the Git version control system.
    - Operational Transformation is the conflict resolution algorithm behind collaborative editing applications such as Google docs. 
5. What is a conflict? 
    - Some kinds of conflicts are subtle to detect. For example: A meeting room booking application should'nt allow overlapping booking for a same room. Even if application checks for availaibility, if the two bookings are done on two different leaders, there can be a conflict.

### Multi-leader replication topologies

