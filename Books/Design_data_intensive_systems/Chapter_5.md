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
1. A replication topology describes the communication paths along which writes are propagated from one node to another.
2. With more than 2 leaders various different topologies are possible. 
    - All-in-all topology:
        - Every leader sends its writes to every other leader.
    - Circular topology:
        - Each node receives writes from one node and forwards those writes to one other node. 
    - Star topology:
        - One designated root node forwards writes to all other nodes.
3. In circular/star topologies to prevent infinite replication each node is given a unique identifier and the replication log, each write is tagged with the identifiers of all nodes it has passed through. A node receiving a data change tagged with it's own identifier, that data change is ignored. 
4. A problem with circular/star topologies is that if one node fails, it can interrupt the flow of replication messages. The reconfiguration of the topology to work around the failed node is manual. 
5. All in all is pretty densely connected, messages can travel different paths. All-in-all has issues too. Some network links are faster than the others and so some replication messages might overtake others which may lead to causality issues eg. update of a row that never existed because update arrived first than insert on that replica. Simply attaching a timestamp to every write is not sufficient, because clocks cannot be trusted to be in sync.
6. We use version vectors to take care of the casual ordering.
7. Conflict detection techniques are poorly implemented in many multi-leader systems. 
8. If using multi-leader replication, it's worth being aware of these issues, carefully reading the documentation and thoroughly testing the database to ensure it provides the guarantees you require for your application.

## Leaderless replication
1. Single Leader / multi-leader replication systems are based on the idea that a client sends a write request to one node (the leader) and the database takes care of copying that write to other replicas. Leader determines the order in which writes are to be processed and followers apply the writes in the same order. 
2. Certain data storage systems allow any replica to accept writes from client. Dynamo (in-house by Amazon), Riak, Cassandra, Voldemort are open source data stores with leaderless replication models. 
3. Either a client can redirect it's writes to several replicas or a coordinator does this on behalf of the client. The coordinator does'nt enforce a particular ordering of writes.

### Writing to the database when a node is down
1. If you have a database system with 3 replicas and one of them is currently down (perhaps rebooted after installing a system update), for a leader based replication if you need to keep processing writes, you may need to perform a failover. 
2. Fail-over does not exist for a leaderless replication. The client sends the writes to 3 replicas in parallel. One misses it. If it's sufficient for 2 out of 3 replicas to ack the write, after the client gets two successful responses, we consider the write successful.
3. If the unavailaible node, comes back online, it misses the writes. So if any client tries to read from it, it will get stale values as responses. 
4. To solve this, the read requests are also sent in parallel to several nodes. The client gets several different responses. Version numbers identify which values are newer. 

### Read repair and anti-entropy
1. Replication must ensure all data is eventually copied to every replica. 
2. Two mechanims are used in Dynamo 
    - Read repair:
        - A client sends several read requests to all replicas in parallel
        - It can detect any stale responses.
        - It writes the newer values to those replicas returning stale values
        - This works well for values that are frequently read.

    - Anti-entropy:
        - Some datastores have background processes that constantly looks for differentces between replicas and copies missing data from one replica to another. 
        - This does not uses replication logs and data is not copied in any particular order. 
        - There may be significant delay before the data is copied. 

### Quorums for reads & writes
1. If every successful write is guaranteed to be present on at-least 2 out of 3 replicas, 1 replica can be stale.
2. If we read from at-le
ast 2 replicas, we can be sure that at-least one of them is up-to-date. If the third replica is down or slow, reads can still continue to return an up-to-date value.
3. Generalizing it. If there are n replicas, every write must be confirmed by w nodes to be considered successful and we require r nodes at-least for every read. 
4. As long as w + r > n, we expect an up-to-date value when reading because at-least one of r nodes we're reading from must be up to date.
5. Reads and writes obeying these r and w values are called quorum reads and writes. 
6. Dynamo has n, r and w configurable. A common choice is to make n and odd number and w = r  = (n  + 1) / 2 rounded up. 
7. There may be more than n nodes in a cluster but any given value can be stored on n nodes. This allows dataset to be partitioned supporting datasets that are larger to fit on node node. 
8. The quorum condition w + r > n allows the system to tolerate unavailaible nodes as follows
    - if w < n, we can still process writes if certain node(s) are unavailaible.
    - if r < n, we can still process reads if certain node(s) are unavailaible
    - N = 3, R = W = 2, we can tolerate 1 unavailaible node. 
    - N = 5, R = W = 3, we can tolerate 2 unavailable nodes. 
    - Reads and writes are sent to all replicas n in parallel. W and R determine for how many nodes we wait for i.e. how many among the n nodes need to report success before we consider the read/write to be successful. 
    - If fewer than w or r nodes are availaible, writes/reads return an error. 

### Limitations of quorum consistency
1. For every read to return a latest written value, we must have w + r > n. The set of nodes to which we've written and the set of nodes from which you'll read must overlap.
2. Quorums may not be necessarily majorities - it only matters if the set of nodes used by read/write operations overlap by atleast one node. Other quorum assignment algorithms are possible. 
3. W and R can be set to smaller numbers (w + r <= n). In this case, it's more likely that your read did'nt return the latest value. On the positive side, this configuration allows lower latency and high availaibility.  
4. Even with w + r > n, there are  few likely edge cases. Possible scenarios
    - If a sloppy quorum is used, the w writes may end up on different nodes than the r reads so there is no longer an overlap.
    - If writes occur concurrently, not clear which happenend first. The solution here is to merge the concurrent writes. If a winner is picked on the basis of the timestamp (last writes win), the writes can be lost due to clock skew. 
    - Writes happening concurrently with a read may be reflected on only some of the replicas. Undetermined whether the read should return the new value or old value
    - If a write succeeds on less than w replicas, it's not rolled back on the ones where it succeeded. If a write is reported as failed, subsequent reads may or may not return the value from that write.
    - If a node carrying a new value fails, and it's data is restored from a replica having an old value, the quorum condition may fail.
    - Even if everything is working correctly, there are edge cases where we can get unlucky with the timing. 
5. Although quorums appear to return the latest value, at times it's not so simple. The parameters w and r allow to adjust the probablity of stale values being read but they should not be taken as absolute guarantees. 
6. We don't get guarantees (Read your own writes, monotonic reads, consistent prefix reads) in this. Stronger guarantees require transactions or consensus.

### Monitoring staleness
1. It's very important to monitor replication health and whether the database can return uptodate values.
2. If replication falls behind significantly, it should alert so you can investigate the cause. 
3. For leader based replication, the database exposes some metrics for the replication lag, which could be feeded into a monitoring system. 
4. For leader based replication since writes are applied in same order on leader and folowers, each follower has a position in the replication log. We can substract the leader's position and the follower's position to get the replication lag.
5. For leaderless replication, no fixed order of writes making monitoring difficult. Databases can only use read repair (no anti-entropy), there is no limit on which how stale the value can be. 
6. Some research has gone through in measuring staleness on the basis of w, r and n but not in common practise.
7. Eventual consistency is a deliberately vague guarantee. For operability, it is important to quantify "eventual".

### Sloppy quorums and hinted handoff
1. Quorums as described so far may not be as fault-tolerant as they could be. Network interruptions could cut a client off from large number of database nodes. 
2. In such situations it's likely that fewer than w or r nodes remain so that client can no longer reach a quorum. 
3. In a large cluster, it's likely the client can connect to some database nodes during network interruption but not to the nodes it needs to assemble a quorum for a particular value. In that case, we face a trade-off. 
    - Is it better to return errors to all requests to whom we cannot reach w or r nodes?
    - Should we accept all writes anyway and write them to some nodes that are reachable but are'nt among the n nodes on which the value usually lives? 
4. The second option is called sloppy quorum. Writes/reads require w and r successfull responses but those may include nodes not among the designated n home nodes. 
5. Once the network interruption is fixed, any writes that one node temporarily accepts on behalf of another node are sent to the appropriate home node. This is called hinted handoff. 
6. Sloppy quorums ensure write availaibility, but they may not guarantee the latest value of a key to be returned because it may have been temporarily written to noddes outside of n. 
7. Sloppy quorums are common in all Dynamo implementations. In Riak, they are enabled by default. 

### Multi-data center operation: 
1. Leaderless replication is also suitable for multi-datacenter operation since it's designed to tolerate conflicting concurrent writes, network interrupts and latency spikes
2. Cassandra/Voldemort implemtent their multi-datacenter support within normal leaderless model. The n replicas include nodes in all datacenters and in the configuration, you can specify how many of the n replicas you want to have within each data center. 
3. A write is sent to all replicas regardless of datacenter but the client usually waits for acknowledgement from a quorum number of nodes within the local datacenter. 
4. Higher latency writes to other data centers are performed asynchronously although it's configurable.

### Detecting concurrent writes
1. Dynamo style databases often have to handle concurrent updates to several keys and so conflicts may arise despite the use of strict quorums. 
2. The problem is that events may arrive in different order at different nodes due to variable network delays and partial failures. To become eventually consistent, replicas should converge to the same value.
3. Most replication implementations are quite poor in handing this and if you want to avoid loosing data you need to know a lot about the internals of your database's conflict handling.
4. Discussing few techniques for conflict resolution.

#### Last writes win (Discard concurrent writes)
1. Declare each replica needs to store most "recent" (provided we have some mechanism of determining which value is recent) value and allow older values to be overwritten or discarded. 
2. "Recent" is quite misleading. We say that the writes are concurrent and so their order should be undefined. 
3. Even though, there is no natural ordering on writes, we can force an arbitrary ordering say timestamp. Attach a timestamp to each write, pick the biggest timestamp and discard any writes with an earlier timestamp. This is called Last writes win and is the only supported conflict resolution method in Cassandra. 
4. Even though this achieves eventual consistency, it's at the cost of durability. Even several concurrent writes to the same key have been reported successfully, only one of the writes will survive. 
5. In case of caching, LWW may be acceptable. If losing data is not acceptable, LWW is a poor choice. 
6. The only safe way of using a database with LWW is to ensure that a key is written once and is immutable afterwards totally avoiding concurrent updates. In case of Cassandra, give each write a unique UUID. 
7. Deciding whether two operations are concurrent - The "Happens-Before" 
    - If client A's write starts after client B's write and it builds on client B's write, we say A is casually dependent on B. 
    - If each of client A and client B start the write operation on the same key X not knowing about each other, it's a concurrent operation. We need an algorithm for determining this. 
8. Because of problems with clocks in distributed systems, it's quite difficult to tell whether two things happened exactly at the same time. The time does not matter, we call two operations as concurrent if they are unware of each other regardless of the physical time in which they occur.
9. Per the theory of relativity, if two events occuring some distance apart cannot affect each other if the time between the events is shorter than the time it takes for light to travel the distance between them. 
10. In computer systems, two operations can stilll be concurrent if they occur some time apart from each other, may be due to network prolems preventing one operation from knowing about the other.

#### Algorithm to determine whether two operations are concurrent - Capturing the happens-before relationship (NEED to revisit)
1. Let's start with a database with one replica. Later on, we could generalize the approach to a leaderless database with many replicas. 
2. Consider two clients C1 and C2 adding items to the same shopping cart.
    - Initially shoppoing cart is empty. 
    - C1 adds milk to the cart. So Cart contents-  {"cart": [{"milk", 1}]} are returned to client C1. The 1 is the version number. 
    - C2 adds eggs not knowing C1 added milk concurrently. The server adds eggs and milk and eggs are treated as different values in the cart with the version number as 2 So Cart contents-  {"cart": [{"milk", 2}, {"eggs": 2}]} are returned to client C2
    - C1 now wants to add flour so it sends the request as  {"cart": [{"milk: 1}, {"flour": 1}]} from the previous output it received. The server sees that this value supersedes the previous C1's write of milk but is concurrent with eggs. It assigns both milk and flour the version number 3 but keeps eggs at 2. So Cart contents-  {"cart": [{"milk", 3}, {"eggs": 2}, {"flour": 3}]} are returned to client C1
    - C2 now wants to add ham unaware that C1 added flour. C2 tries to send [{"milk": 2}, {"eggs": 2}, {"ham": 2}]. Server detects that version 2 overwrites eggs but is concurrent to milk and flour. So it keeps milk and flour at 3 and eggs and ham at 4. So Cart contents-  {"cart": [{"milk", 3}, {"eggs": 4}, {"flour": 3}, {"ham": 4}]} are returned to client C2
    - Finally C1 wants to add bacon. It received {"cart": [{"milk", 3}, {"eggs": 2}, {"flour": 3}]} and it merges bacon as {"cart": [{"milk", 3}, {"eggs": 2}, {"flour": 3}, {"bacon": 3}]} and sends it to the server. This overwrites milk, flour but is concurrent with eggs, milk, ham and so the server keeps those two concurrent values.
3. Considering the process in a short way.
    - Empty Cart: {"cart": {}}
    - C1 adds milk. Server says ok and returns response {"cart" {"version": 1, "value": [milk]}}
    - C2 adds eggs not knowing C1 added milk. Server says ok and returns response {"cart" {"version": 2, "value": [milk, eggs]}}
    - C1 wants to add flour not knowing C2 added eggs. It tries to send {"cart" {"version": 1, "value": [milk, flour]}}. Server says ok and returns response {"cart" {"version": 3, "value": [[milk, flour], [eggs]}}
    - C2 wants to add ham not knowing C1 added flour. It tries to sends {"version": 2, "value": [milk, eggs, ham]}}
4. Thus old versions of the value do get overwritten automatically and eventually no writes are lost. 
5. A textual description of the algorithm is as as follows
    - Server maintains a version number for every key, increments the version number every time that key is written and stores it along with the value. 
    - When a client reads a key, the server returns all values that have not been overwritten as well as latest version number. A client must read a key before writing. 
    - When a client writes a key, it must merge together all values received in prior read and also include the previous version number. 
    - When the server receives a write, it can overwrite all values with that version number  or below. It must keep the values at a higher version number since they are concurrent with the incoming write.

#### Merging concurrently written values
1. For the above algorithm, clients need to do extra work. If several operations happen concurrently, clients have to clean up afterward by merging the concurrently written values also called concurrent value siblings. 
2. We can pick a single value depending on version or timestamp. 
3. Another approach is to take union. But if you merge two sibling carts and few items have been removed from them, this may not give the right result. To prevent this, we can have the system use a version number to indicate a deleted item. 
4. It's error prone to handling concurrent sibling merging in application code. There are data structures (Riak's CRDTs) which help to merge siblings in sensible ways handling deletions.

#### Version vectors:
1. When there are multiple replicas handing writes concurrently, we use version vectors. 
2. Each replica keeps track of it's own version number and also of the version numers it has seen from other replicas while processing a write. This indicates which values to override and which to keep as siblings.
3. Version vector is a collection of version numbers from all replicas. 