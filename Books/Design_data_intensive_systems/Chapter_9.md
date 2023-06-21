# Consistency and Consensus

## Introduction
1. The simplest way of handling faults in Distributed systems is to simply let the entire service fail and show an error message to the user. 
2. If this way is unacceptable, find ways of tolerating faults that is to keep the service functioning correctly. 
3. The best way of building fault-tolerant systems is to find some general purpose abstractions with some useful guarantees, implement them once and then let applications rely on those guarantees. 
4. Same was the way with transactions. The application can pretend there can be no crashes (atomicity), that nobody is concurrently accessing the database and storage devices are perfectly reliable. The transaction abstraction hides any crashes, disk failures and race conditions that may occur so that application does'nt worry about them.
5. Consensus is one of most important abstractions with Distributed systems. Getting all nodes to agree on something. Consensus is a tricky problem with network faults and process failures in Distributed systems.
6. We can have a database with single leader replication. When the leader dies, the remaining database nodes can use consensus to elect a new leader and all nodes agree who the leader is. Split brain problem can occur due to two nodes believing each one of them to be the leader. 
7. We need to understand the scope of what can and cannot be done. In some situations, it's possible for system to tolerate faults and continue working. In certain other situations it's not possible.

## Consistency guarantees
1. If you look at two database nodes at the same moment in time, you'd likely see different data on the two nodes at different times because write requests arrive at the nodes in different times. These inconsistencies occur no matter what replication method the database uses (single-leader, multi-leader or leaderless)
2. Replicated databases mostly provide eventual consistency. Stop writing to the database and wait for an unspecified time. Then eventually all reads will return the same value. Convergence is a better name for eventual consistency since all replicas eventually converge to the same value.
3. This is a very weak guarantee. It does'nt say when the replicas would converge. If you write a value and read it immediately, there is no guanrantee that you will see the value you wrote because the read may be routed to a different replica
4. The edge cases of eventual consistency become apparent only when there is a fault in the system or at a high concurrency.
5. Systems with stronger guarantees have worse performance and less fault tolerant than those with weaker guarantees
6. While there is some overlap between consistency models and transaction isolation levels, they are mostly independent concerns.
    - Distributed consistency is about coordinating the state of replicas in the face of delays and faults
    - Transaction isolation is about avoiding race conditions due to concurrently executing transactions.

## Linerizability
1. If an eventually consistent, replicated database could give an illusion that there is only one copy of data it would be a lot simpler. Every client would have the same copy of data and no need to wory about replication lag. This is the idea behind linearizability, immediate consistency, strong consistency. 
2. In a linearizable system as soon as one client completes a write, all clients reading from the database must be able to see the value just written. 
3. Maintaining the illusion of a single copy of data means guaranteeing that the value read is the most recent value. Linearizability is a recency guarantee. 

### What makes a system linearizable? 
1. The basic idea behind linearizability is simple - to make a system appear as if there is only a single copy of data. 
2. An example:
    - Consider 3 clients concurrently reading and writing to an object x
    - Due to variable network delays a client does'nt know when exactly a database processed it's request, it knows that it happened sometimes between the client sending the request and receiving the response. 
    - Operations:
        - read(x) -> Read the value of x
        - write(x, v) -> Writ the value v to x.
    - If a read operation by a client completes before the write begins, it must definitely return the old value.
    - If a read operation begins after a write operation has completed, it must definitely return the last written value.
    - Read operations concurrent with a write operation might return either 0 or 1 since we don't know if the write has taken effect at the time of the read operation. This is not what we expect of a linearizable system.
    - To make this system linearizable we add another constraint - if any one read returns a new value 1, all subsequent reads on that client and any other client will also return the same value, even if the write operation has not completed.
    - We now add a third type of operation - cas(x, vold, vnew). If the current value of the register equals vold, it should atomically set to vnew. If x is not vold, it should leave it unchanged. This is atomic compare and set.
    - It is possible to test whether a system's behavior is linearizable by recording the timings of all requests and responses and checking whether they can be arranged in a valid sequential order.
3. Serializability Vs Linearizability
    - Both terms are confusing and mean to arrange in sequential order. But they are two different guarantees and it's important to distinguish between them. 
    - Serializability:
        - Isolation property. Each transaction may read/write multiple objects (row, documents, records). 
        - Guarantees that the transactions behave the same had they executed in serial order.
        - It's okay for that serial order to be different from the order in which they are actually run. 
    - Linearizability:
        - Is a recency guarantee on reads and writes of a single object. 
        - Does'nt group operations into transactions so does not prevent write skew unless additional measures (such as materializing conflicts) are taken.
    - Strict serializability is when a database provides both serializability and linearizability. 2PL and actual serial execution are typically serializable. 
    - Serializable snapshot isolation that does not include writes that are more recent that the snapshot to avoid lock contention is not linearizable. 

### Relying on Linearizability - When is it useful?:
1. Locking and leader election:
    - A system using single-leader replication needs to ensure that there are indeed one leader and not several (split brain)
    - One way to elect a leader is use a lock. Every node that starts up tries to acquire the lock and one that suceeds becomes the leader.
    - This must be linearizable, all node must agree on who owns the lock otherwise it becomes useless.
2. Coordination services like etcd, Zookeeper are used to implement distributed locks and leader election. They use consensus algorithms to implement linearizable operations in a fault-tolerant way. 
3. Distributed locking is also used at a granular scale in some distributed databases like Oracle Real Application clusters

### Constraints and uniqueness guarantees
1. If you want to enforce a uniqueness constraint like username or email address must uniquely identify only one user, you need linearizability. 
2. This is similar to a lock, the user acquires a lock on his chosen username. 
3. Similar cases: 
    - A bank balance should never be negative
    - Two people don't book a same seat on a flight or a movie theater concurrently. 
4. These cases require there to be a single up-to-date value on which all nodes agree on.
5. At times, these constraints could be treated loosely. For example: If a flight is overbooked, you can move customers to a different flights or offer them compensation. These don't require linearizability

### Cross channel dependencies
1. Linearizability violations get noticed because of an additional communication channel in the system. 
2. Consider a simple image upload & resizer system consisting of a 
    - Web server having an API that uploads an image
    - File storage service to store images
    - Queue that is consumed by the image resizer service
    - Resizer service that resizes the image and stores it.
3. Resizer image should begin it's work only when it receives a message through the queue. That is after the Web server successfully stores the image in the file storage service.
4. If the storage service is not linearizable, there is a risk of a race condition. The message queue might be faster than the internal replication inside the storage service. So when the resizer fetches the image, it might see an old version of image or nothing at all.
5. Since there are different communication channels between the Web server and resizer service: the storage service and the message queue, this problem arises. Without linearizability, race conditions between these two channels are possible.

### Implementating linearizability
1. The simplest way to implement linearizable systems is to have only one copy of data. Having a single copy does'nt make it fault tolerant at all. When that copy is lost or when the storage service is down, the data is inaccessible. 
2. Replication is one common approach of making a system fault tolerant.
3. Which types of replication can be made linearizable?
    - Single-leader replication:
        - Leader has the primary copy of data and followers maintain backup copies of the same data. 
        - If reads are made from leader or synchronously updated followers, they have the potential to become linearizable. 
        - Not every single-leader database is actually linearizable due to concurrency bug or by design (it uses snapshot isolation)
        - Asynchronous replication failover may even loose commited writes which affects both linearizability and durability. 
        - Serving reads from a leader means the client knows who the leader is. It's quite possible to have a delusional reader server the reads which it is not. This affects linearizability.
    - Consensus algorithms:
        - Consensus protocols contain measures to prevent split brain and stale replicas.
        - Therefore, they can implement linearizable storage safely. This is how Zookeeper and etcd work.
    - Multi-leader replication: 
        - Not linearizable because they concurrently process writes on multiple nodes and asynchronously replicate them to other nodes. 
        - They can produce write conflicts that require resolution.
    - Leaderless replication
        - People sometimes claim that you can obtain strong conssistency by requiring quorum reads and writes (w + r > n). Depending on quorum config and how strong consistency is defined, this may not be true.
        - "Last writes wins" conflict resolution method is based on time-of-day clocks are non-linearizable because clock timestamps can't be guaranteed.
        - Sloppy quorums and hinted handoff also ruin any chances of linearizability. 
        - With strict quorums, it may seem that quorum reads and writes should be linearizable. However, in a network with variable network delays, it's possible to have race conditions.
        - For dynamo style databases with strict quorum, it's possible to achieve linearizability with a loss in performance. We can perform read repair continously before returning read results to the client. A Writer may perform a latest read of a quorum of nodes before sending it's writes. 
        - Cassandra does wait for read repair to complete on quorum reads but it loses linearizability if there are several concurrent writes to the same key, due to it's use of last-write-wins conflict resolution.
        - A linearizable compare-and-set operation requires a consensus algorithm.
        - It's safest to assume that a leaderless system with Dynamo-style replication does not provide linearizability. 

### The cost of linearizability
1. It's interesting to explore some more pros and cons of linearizability.
2. Consider what happens if there is a network interruption between two data centers. Clients can reach the datacenters but the clients cannot connect with each other. 
3. Each datacenter can still continue to operate normally. Since writes from one datacenter are asynchronously replicated to the other, they queue up and exchanged when network connectivity is restored. 
4. If we use single leader replication, the leader may in one of the data-centers and the follower datacenters synchronously send read and write requests to the leader datacenter. If the network between the datacenters is interrupted, cleints connected to the follower datacenter cannot contact the leader so they cannot make any writes to the leader. They can make reads but reads will be stale (non-linearizable)
5. Clients that can only reach a follower data center will experience an outage till the network link is established. 
6. The CAP theorem: 
    -  The trade-off is as follows
        - If the application requires linearizability, then some replicas cannot process requests while they are disconnected. They must either wait until the network problems are fixed or return an error
        - If the application does not require linearizability, it can be written in a way each replica can process requests independently even if it's disconnected from other replicas. The application remains availaible in face of a network problem but it's behavior is not serializable. 
    - Applications not requiring linearizability can be more tolerant of network problems. This insight is known as  CAP theorem named by Eric Brewer. 
    - Many distributed databases focused on providing linearizable semantics on a cluster of machines with shared storage.
    - CAP encouraged database engineers to explore a design space of distributed shared nothing systems more suitable for implementing large scale web services
    - The Unhelpful CAP:
        - CAP is presented often as Consistency, Availaibility, Partition Tolerance - pick 2 of these 3. 
        - Network partitions are a fault, they will happen anyways, we don't have a choice.
        - When the network is working correctly, a system can provide both consistency (linearizability) and total availaiblity.
        - In case of a network fault, you have to choose between either linearizability or total availaibility. Thus a more better way to refer to CAP is Consistent or Availaible when Partitioned.
        - CAP has several contradictory definitions of availaibity. Many highly availaible systems do not meet CAP's idiosyncratic definition of availaibility. 
        - Lot of misunderstanding and confusion around CAP so CAP is best avoided.
    - CAP explores just one consistency model (linearizability) and one kind of fault (network partitions). It does'nt say anything about network delays or dead nodes or other trade-offs. 
    - CAP has little practical value for designing systems although it was of good historical significance.
7. Linearizability & network delays
    - Surprisingly, very few systems are linearizable in practise. A modern RAM is not linearizable. If a thread running on one CPI core writes to an address, and a thread on another CPU core reads the same address, its not guaranteed to read the same value returned by the first thread unless a fence is used. 
    - Each CPU has it's own memory cache. Memory access goes to the cache first and any changes get asynchronously written to main memory. Access to cache is faster than main memory and so this feature is essential for good performance. 
    - Now there are several copies of data and these copies are asynchronously updated and so linearizability is lost. It just does'nt make sense to apply the CAP theorem to a multi-core memory consistency model. Within a single computer, we assume reliable communication.
    - The reason for dropping linearizability is performance and not fault tolerance. Linearizability is slow and this is true all the time.
    - If we want linearizability, the response time of read and write requests is proportional to the uncertainty of delays within the network. With highly variable delays, the response time of linearizable writes/reads would be very high
    - Faster algorithms for linearizability don't exist but weaker consistency models can be much faster. And so this is an important trade-off

## Ordering guarantees
1. Linearizable operations are executed in a certain well-defined order. 
2. Some more contexts in which ordering matters
    - The leader in single-leader replication determines the order of writes in the replication log. This is the order in which follower should apply those writes. If there is no single leader, conflicts can occur due to concurrent operations
    - Serializability is ensuring transactions behave as if they've been executed in a sequential order. They are literally executed sequentially or concurrent execution is allowed while preventing serializing conflicts. 
    - Use of timestamps and clocks is another attempt to introduce order to determine which one of two writes happenend later.
3. Deep connections between between ordering, linearizability and consensus. 

### Ordering and Causality
1. Ordering is important since it helps preserve causality
2. Causality helps prevent
    - Violation of intuitions of cause and effect, for example: if a question is answered, clearly question has to there first and then the answer (not other way round)
    - One more way to think about causality is a row must first be created before it can be updated.
    - For two operations A and B, either A happened before B or B before A or they are concurrent. This is another expression of causality. If A and B are concurrent, there's no causal link between them.
    - For snapshot isolation, when we say a transaction reads from a consistent snapshot, it means consistent with causality. If the snapshot contains an answer it must also contain the question being answered.
    - In the example, of the on-call doctors, Alice was allowed to go off the call because the transaction thought that Bob was still on-call and vice-versa. The action of going off call is causally dependent on the observation of who is currently on call. Serializable snapshot isolation tracks causal dependencies between transactions. 

3. Causality imposes an order in the system before effect comes cause. The chains of causally dependent operations define the causal order in the system i.e what happens before what
4. If a system obeys the ordering imposed by causality, it's called a causally consistent. For example: Snapshot isolation is causally consistent. When you read from a database, and you see some pieces of data, then you must also see any data that causally precedes it.
5. Causal order is not a total order:
    - Total order allows any two elements to be compared. So if you have two elements, you can always see which is greater. Mathematical sets are not ordered. You can't compare them. 
    - The difference in total order and partial order is reflected in different database consistency models
        - Linearizability:
            - Total order of operations. 
            - System behaves as if there is a single copy of data and every operation is atomic
            - This means for any two operations, we can always say which one happened first. 
        - Causality: 
            - Causality defines a partial order, because some operations are ordered with respect to one another but some that happen concurrently are incomparable 
            - Per this definition, there are no concurrent operations in a linearizable data store. Single timeline in which all operations are totally ordered. 
            - Datastore ensures every request is handled atomically along a single timeline without any concurrency. 
            -  Concurrency means the timeline branches and merges again, operations on different branches are incomparable. 
            - Often for distributed version control systems such as Git, their version histories are very similar like the graph of casual dependencies. 
    - Linearizability is stronger than casual consistency:
        - Linearizability is implies causality. If there are different communication channels, linearizability ensures causality is preserved without automatically having to pass timestamps around. 
        - Linearizability makes systems simple to understand and appealing. But it can harm its performance and availaibility, especially if the system has network delays. 
        - Linearizability is not the only way of preserving causality. A system can be causally consistent without incurring the performance hit of making it linearizable. 
        - In many cases, systems that appear to require linearizability, just require causal consistency which can be implemented more efficienty. 
        - This is a research topic as researchers look into more kinds of databases that preserve causality with performance and availaibility characteristics that are similar to those of eventually consistent systems.
    - Capturing causal dependencies: 
        - To maintain causality one should know which operation happened before which other operation. 
        - If one operation occurred before the other, they must be processed in that order on every replica. 
        - To determine causal dependencies, we need some way of describing the knowledge of a node in a system. If a node has seen the value X, which it issued write Y, X and Y may be causally related. 
        - Causal consistency needs to track causal dependencies across an entire database not just for a single key as was done in leaderless datastore where concurrent writes were detected to a single key to prevent lost updates. 
        - The database needs to know which version of the data was read by the application. The version number of the prior operation is passed back to the database on a write. 

### Sequence number ordering
- Actually keeping track of all causal dependencies can become impractical. In many applications, clients read lots of data before writing something. It's not clear whether  the write is causally dependent on which prior read. 
- We can use sequence numbers or timestamps to order events. A timestamp comes from a ogical clock to generate a sequence of numbers to identify an operation using counters that are incremented. 
- Such counters are compact i.e. few bytes and they provide a total order i.e. every operation has a unique sequence number. 
- Sequence numbers can be created in an order consistent with causality. If operation A happened before operation B, the sequence number of A should be less than B.
- Concurrent operations may be ordered arbitrarily. 
- For single leader replication, the log defines a total order of write operations that is consistent with causality. The leader can assign a monotonically increasing counter or each operation in the replication log. If the follower applies the operations in the same sequence, the state of the follower is guaranteed to be consistent with the leader. 
- Noncausal sequence number generators:
    - It's less clear on how to generate a sequence number if there's not a single leader. 
    - Few methods to do so:
        - Each node can generate independent set of sequence numbers. One node can generate odd and the other even or they can even append their unique node id to the sequence number.
        - Can attach a time-of-day high resolution timestamp to the sequence. This is used in the last writes win conflict resolution method
        - Pre-allocate blocks of sequence numbers. For example: node A might claim the block from 1 to 1000, node B from 1001 to 2000. Each node can independently assign sequence numbers from its block and allocate a new block when it's supply is finished.
    - These options perform better than push sequence numbers through a single leader. But the problem is the sequence numbers they generate are not causally consistent. They don't capture the ordering of operations across several nodes.
        - If one node generates odd sequence numbers and the other node generates even, the former node may lag behind because it processes fewer operations than the latter. We can't accurately tell which one causally happened first.
        - Timestamps from phyical clocks have clock skews which can make them inconsistent with causality.
        - Block allocator may give an operation A that happened later a sequence number of 1000 than an operation B which preceded it which was given the number 2000.
    - Lamport timestamps:
        - Simple method for generating sequence numbers that are consistent with causality.
        - Each node has a unique identifier and keeps a counter of the operations it has processed. The Lamport timestamp simply is (node_id, counter).
        - Two nodes may have the same counter value but by including the node id, each timestamp is made unique.
        - If you have two Lamport timestamps, the one with a greater counter is the greater one and if the two counters are equal, the one with a greater node id is the greater timestamp. They bear no relationship to wall clock time. 
        - Every node and every client keeps track of the maximum counter value it has seen so far. When a node receives a request or response with a maximum counter value greater than its own, it immediately increases its own counter to that maximum. 
        - As long as the maximum counter value is carried along with each operation, this scheme ensures that the ordering from Lamport timestamps is consistent with causality. Each causal dependency results in an increased timestamp.
        - These appear to version vectors but are not. 
            - Version vectors detect whether two operations are concurrent or whether one operation is causally dependent on the other.
            - Lamport timestamps enforce a total ordering. They are more compact. From the total ordering of Lamport timestamps, you can't tell whether two operations are concurrent or causally dependent.
    - Timestamp ordering is not sufficient
        - Lamport timestamps are not sufficient to solve many common distributed system problems.
        - For a system that needs to ensure that a username uniquely identifies a user account. If two accounts are created with the same username, pick the one with the lower timestamp and let the greater one fail. 
        - This approach works for determining the winner you've collected all data for username creation operations. If a node has just received an input to create a username and has to decide right now whether this username has already been used. At that moment, some other node may be concurrently receiving the request with the same username. The former node could check every other node but then this kind of system would grind to a halt.
        - In order to implement a uniqueness constraint, it's not sufficient to have a total ordering of operations, you need to know when that order is finalized.
        - If you have an operation to create a username and you're sure that no other node can insert a claim for the same username ahead of your claim in total order, then you can safely declare the operation to be successful. This idea of knowing when your total order is finalized is called total order broadcast.
    
### Total Order Broadcast
1. For a single CPU core, it's easy to define the total order, its simply the order in which those operations are executed by the CPU. 
2. For a distributed system, getting all nodes to agree on the same order of operations is tricky. 
3. Single leader replication determines total order by choosing one node as the leader and sequencing all operations on a single CPU core on the leader. If the throughput is greater than a single leader can handle, scaling the system becomes a challenge. This problem is known as total order broadcast or atomic broadcast.
4. Scope of ordering guarantee:
    - Partitioned databases within a single leader per partition maintain ordering only per partition. 
    - They cannot offer consistency guarantees across partitions. 
    - Total ordering across all partitions is possible but requires additional coordination.
5. Total order broadcast is a protocol for exchanging messages between nodes. It requires two safety properties to be satisfied
    - Reliable delivery: No messages are lost. If a message is delivered to one node, it is delivered to all nodes.
    - Totally ordered delivery: Messages are delivered to every node in the same order.
6. Reliability and totally ordered delivery should always be satisfied even if a node or the network is faulty.
7. Using total order broadcast:
    - Zookeeper implements total order broadcast. Zookeeper is a consensus service and there is a strong connection between total order broadcast and consensus.
    - If for database replication, every replica processes the same writes in the same order, then the replicas always remain consistent with each other (a temporary replication lag may be there). This principle is called State machine replication.
    - The order is fixed at the time of message delivery. A node canot insert a message into an earlier position in the order if subsequent messages have already been delivered. This makes total order broadcast stronger than timestamp ordering.
    - It's can be said to be a way to create a log (transaction log, WAL log, replication log). All nodes can read the same append-only log and read the same sequence of messages.
    - It can also be used to implement a lock service that provides fencing tokens. Each request to acquire the lock as appended as a message to this log. All messages are sequentially numbered in the order they appear in the log. The sequence number (zxid in Zookeeper) serves as the fencing token because it's monotonically increasing. 
8. Implementing linearizable storage using total order broadcast
    - Linearizability is not the same as total order broadcast. 
    - Total order broadcast is async. Messages are guaranteed to be delivered reliably in a fixed order. No guarantee about when a message will be delivered so one recipient may lag behind the others. Linearizability is a recency guarantee, a read is guaranteed to see the latest value written. 
    - You can build a linearizable storage on top of total order broadcast to ensure for example user names uniquely identify user accounts.
    - Here's how we can implement a linearizable compare-and-set operation using total order broadcast as an append only log for ensuring unique usernames
        - Append a message to the log tentatively claiming the username you want to claim.
        - Read the log and wait for the message you appended to be delivered to you. 
        - Check for any messages claiming the username you want. If the first message for your desired username is your own message, then you are successful, you can commit the username claim (can append another message to the log) and acknowledge it to the client. If first message for your desired username is from another user, you abort the operation.
        - Log entries are delivered to all nodes in the same order, if there are several concurrent writes, all nodes will agree on which one came first. Choose the first of conflicting writes as the winner and abort the later ones, all nodes will agree on who came first. 
        - While this procedure ensures linearizable writes, this does'nt guarantee linearizable reads. Reading from a store that's asyncly updated from the log, it may be stale. This provides sequential or timeline consistency which is a slightly weaker guarantee than linearizability.
    - To make reads linearizable, we have few more options
        - Sequence reads through a log, read the log and perform the actual read when the message from log is delivered back to you. The message's position in the log define the point in time at which read happens. Quorum reads in etcd work like this.
        - If log allows you to fetch the position of the latest log message in a linearizable way, you can query upto that position, wait for all entries to be delivered to you and then perform the read. This is the idea behind zookeeper's sync. 
        - Make your read from a replica that is synchronously updated on writes and sure to be up to date with data. This technique is used in chain replication. 
9. Implementing total order broadcast with linearizable storage
    - We can also assume that we have linearizable storage and show how to build total order broadcast from it. 
    - The easiest way is to assume that you have a linearizable register having an atomic increment-and-get operation. An atomic compare-and-set would also work. 
    - For every message you want to send through total order broadcast, you increment and get the linearizable integer, attach the value you got from the register as a sequence number to the message.
    - Send this message to all nodes (resend any lost messages) and the recipients will deliver the messages consecutively by timestamp
    - The numbers we get from incrementing linearizable registers form a sequence with no gaps. If a node delivers message 4, and receives a message with sequence number of 6, it knows it must wait for message 5. This is the key difference between total order broadcast and timestamp ordering.
    - How hard it is to implement a linearizable integer with an atomic increment-and-get operation? We can just keep it in a variable on one node. The problem lies in handling the situation when the network to that node fails and restoring the value when that node fails. 
    - If you think hard enough about linearizable sequence number generators, you end up with a consensus algorithm. Thus it can be proved that a linearizable compare-and-set or increment-and-get and total order broadcast are equivalent to consensus. 

## Distributed Transactions and Consensus
1. On the surface, consensus means getting several nodes to agree on something. Although, it's believed it should'nt be too hard, many broken systems have been built in the mistaken belief that this problem is easy to solve. 
2. A number of situations in which its important for nodes to agree. 
    - Leader election:
        - In single-leader based replication systems, all nodes need to agree on which node is the leader. 
        - Consensus is used to avoid a split brain situation in which two nodes both believe themselves to be the leader.
        - If there were two leaders, they would both accept writes and their data would diverge, leading to inconsistency and data loss.
    - Atomic commit:
        - For a database supporting distributed transactions spanning several nodes or partitions, a transaction may fail on some nodes and may succeed on others. To maintain ACID atomicity, we have to get all nodes to agree on the outcome of the transaction: either they commit or they abort and roll-back if something goes wrong. This instance of consensus is called the atomic commit problem.
        - The FLP Result - The impossibility of consensus
            - Fischer Lynch Paterson (FLP) result proves that there is no algorithm that is always able to reach consensus if there is a risk that a node may crash. In a distributed system, any of the nodes may crash. But we are discussing algorithms for achieving consensus. That's a contradiction!
            - FLP result assumes an asynchronous model using a deterministic algorithm without any clocks or timeouts. 
            - If we just extend the algorithm to use timeouts, or some other way of identifying crashed nodes then the consensus problem becomes solvable. Even if the algorithm uses random numers to get around the impossibility result then also it is fine. 
            - Thus, distributed systems do achieve consensus in practise.
        - The 2-PC and Atomic commit:
            - Atomicity prevents failed transactions from littering the database with half finished results, partial updates. 
            - This is important for multi-object transactions and databases maintaining secondary indexes. 
            - Atomicity ensures that the secondary index is stays consistent with primary data. 
            - From Single node to Distributed Atomic commit:
                - For single-node transactions, atomicity is implemented by the storage engine.
                - When the clients asks the database to commit the transaction, the database makes the transaction's writes durable in a WAL log, appends a commit record to the log on disk. 
                - If the database node crashes, the transaction is recovered from the log when the node restarts. If the commit record was written successfully before the node crash, the transaction is considered commited else any writes from that transaction are rolled back.
                - The key deciding moment for whether transaction commits/aborts is when the disk finishes writing the commit record, before that moment it's still possible to abort. But after that moment, the transaction is commited even if the database is crashing.
                - The single node  (controller of a particular disk drive) is what makes the transaction atomic. If multiple nodes are involved in a transaction, that is a multi-object transaction in a partitioned database, most NoSQL datastores don't support such transactions but various clustered relational systems do.
                - The commit may succeed on some nodes and fail on other nodes since they independently commit and it would violate the atomicity guarantee and become inconsistent with each other. 
                - If a transaction has commited on one node, it can't be retracted again because it failed on the other nodes. A node must commit once it's certain that all other nodes in the transaction are about to commit. 
                - Once a transaction is commited, it becomes visible to other transactions and other clients relying on that data, this forms the basis of the read commited isolation. Therefore transaction commits should be and are irrevocable. 
                - It's still posssible for the effects of a commited transaction to be later undone by another compensating transaction. From the database point of view, this is a separate transaction and any cross-transaction correction are the application's problem.
            - 2 Phase commit:
                - Achieves atomic transaction commit across multiple nodes to ensure all nodes commit or all commits abort
                - Used internally in some databases and also made availaible in the form of XA transactions. 
                - The commit/abort process is split into two separate phases. 
                - 2 Phase commit which ensures atomic commit in distributed database is very different than 2 Phase Locking which provides serializable isolation
                - 2 PC uses a new component called as a coordinator node (transaction manager). This is often implemented as a library within the same application process that is requesting the transaction but it can be a separate process or service. 
                - 2 PC begins with applications reading/writing data in multiple nodes. These are called participants. 
                - When the application is ready to commit, the coordinator sends a prepare to each node asking whether they are able to commit. Responses from all coordinators are tracked. If all reply with a "yes", the coordinator sends out a commit request in phase 2 and commit eventually takes place. If any participant replies with a "no", the coordinator sends an abort request and the transaction is aborted.
                - A system of promises - why 2PC works? Let's break it down to understand the process
                    - An application requests a transaction ID from the coordinator. This transaction ID is globally unique
                    - The application begins a single node transaction on each of the participants and attaches the globally unique transaction ID to the single node transaction. All reads/writes done on these single node transactions. If anything goes wrong, the coordinator or any of the participants can abort. 
                    - When the application is ready to commit, the coordinator sends prepare request to all the participants, tagged with the global transaction ID. If any of these requests fails, the coordinator issues an abort for all participants for that transaction ID
                    - When a participant receives the prepare request, it checks if it can commit the transaction under all circumstances. By replying a "yes", the node promises to commit the transaction without proper error. 
                    - When the coordinator receives the prepare responses from all participants, it must decide whether to commit or abort. It commits only when all participants responded with a "yes". The coordinator must write that decision to it's log so that it knows which way it decided if it subsequently crashes. This is called the commit point.
                    - Once the coordinator's decision has been written to disk, the commit or abort request is sent to all participants. The coordinator retries forever until the request suceeds. 
                    - Thus the protocol contains two points of no return - when a participant votes yes, it promises it will definitely be able to commit later (although the coordinator can abort) and once the coordinator decides, that decision is irrevocable. Those promises ensure the atomicity of 2PC. Single phase commit lumps these two events into one: Writing the commit record to the transaction log.
                - Coordinator failure:
                    - If any of the prepare requests fails or times out, the coordinator aborts the transaction, if any of the commits or abort requests fail, the coordinator retries them indefinitely. 
                    - If coordinator fails before sending the prepare request, a participant may abort the transaction. Once the participant has voted a yes, it can no longer unilaterally abort the transaction, it must wait to hear back from the coordinator whether the transaction was commited or aborted. If the coordinator fails at this point, the participant can do nothing but wait.
                    - Without hearing from the coordinator, the participant has no way of knowing whether to commit or abort. The only way 2PC can recover is by waiting for the coordinator to recover.
                    - When the coordinator recovers, it determines the status of all in-doubt transactions by reading its transaction log. Any transactons not having a commit record in the transaction log are aborted. 
        - Three Phase commit:
            - 2PC is a blocking protocol because it can get stuck waiting for the coordinator to recover. Theoratically, an atomic commit protocol can be made non-blocking so it does'nt get stuck but making this work is not so straightforward. 
            - 3PC is an alternative to 2PC. It assumes a network with bounded delay and nodes with bounded response times. In most practical systems with unbounded network delay and process pauses, it does'nt guarantee atomicity. 
            - A non-blocking atomic commit protocol requires a perfect failure detector for telling if the node crashed or not. 
            - In a network with unbounded delay, timeout might not be a reliable failure detector because a request may time out due to a network problem, even if no node crashed. So, 2PC continues to be used in practise, despite known problems with coordinator failure.
    
    - Distributed transactions:
        - Distributed transactions especially those that are 2PC based are seen as providing an important safety guarantee that would be hard to achieve otherwise. They are also critisized for causing operational problems, killing performance. 
        - Many cloud services choose not to implement distributed transactions due to the operational problems. 
        - In MySQL, distributed transactions are 10 times slower than their non-distributed counterparts.
        - Much of the performance cost in 2PC is due to additional disk enforcing (fsync) required for crash recovery and network round-trips.
        - We should examine distributed transactions in a bit more detail rather than an outright rejection. 
        - Let's begin by examining the different types of distributed transactions that are oft confused. 
            - Database internal distributed transactions: Certain distributed databases (those which use replication & partitioning in their standard configuration) support internal transactions among the nodes. All nodes participating in the transaction have the same version of the database software. Eg: NDB MySQL cluster. 
            - Hetergenous distributed transactions: The participants are two or more different storage technologies eg two databases from different vendors or even non database systems such as message brokers. Atomic commit must be ensured across all these systems, even though they might be different.
        - Database internal transactions can use protocols internal to the database. Transactions spanning heterogenous technologies are quite challenging. 
        - Exactly once message processing:
            - An example heterogenous transaction: A message from a message queue can be acked as processed if and only if the database transaction for processing the message was successfully commited. This is implemented by atomically commiting the message acknowledgement and database writes in the same transaction. 
            - By atomically commiting the message and the side-effects of it's processing, we can ensure that the message is effectively processed exactly once even if it required a few retries. The abort discards any side effects of partially completed transaction.
            - All the systems affected by the transactions should use the same atomic commit protocol, then only this can work. 
        - Atomic commit protocol allowing heterogenous distributed transactions (XA Transactions):
            - X/Open XA (eXtended Architecture) is a standard for implementing 2PC across heterogenous technologies. 
            - Supported by many traditional RDBMS (PostgreSQL, MySQL, DB2) and message brokers (ActiveMQ, MSMQ)
            - It's a C API for interfacing with a transaction coordinator. Bindings exist for this API in other languages. For Java EE applications, XA transactions are implemented using JTA (Java Transaction API).
            - XA assumes that the application uses a network driver or client library to communicate with the participating dataase or messaging service.
            - An XA-supporting driver calls XA API to find whether an operation should be a part of a distributed transaction and if so, it sends information to the database server. It exposes callbacks through which the coordinator can ask the participant to prepare, commit or abort. 
            - Transaction coordinator implements the XA API. It's simply a library that is loaded into the same process as the application issuing the transaction. It keeps track of the participants, collects their responses, and uses a log on local disk to keep track of the commit/abort decision for each transaction.
            - If the application server/process crashes, the coordinator goes with it. All participants with prepared "yes" but uncommited transactions are stuck in doubt. The application process needs to be restarted for the coordinator's local log to be read and the transaction coordinator to be restarted. 
        - Holding locks while in doubt:
            - Locking is a major problem with 2PC. 
            - In Read Commited, database transactions take a row-level exclusive lock on any rows they modify to prevent dirty writes. Serialization isolation takes a read-level shard lock on any rows read by the transaction. 
            - These locks can't be released until and unless the transaction holding them either commits or aborts. 
            - In 2PC, if the coordinator crashes or it's log lost, the locks held by the in-doubt transactions remains in-place till the coordinator is brought up through manual resolution. This can be an unbounded amount of time. 
            - Thus, if other transactions want to access/write the same rows, they cannot. This causes large parts of your application to become unavailaible until the coordinator is brought up.
        - Recovering from coordinator failures:
            - Theoratically, if the coordinator crashes, it should cleanly recover it's state from the log and resolve any in-doubt transactions. 
            - Orphaned transactions do occur practically speaking and they cannot be resolved automatically, so they sit there in the database holding locks and thus blocking other transactions. Restarts also don't help since 2PC preserves the locks of an in-doubt transaction across restarts
            - The only way out is a manual resolution in which an administrator must examine each transaction's participants to determine if all of them have commited or aborted and then apply the results to others. 
            - XA implementations have an emergency escape hatch called heuristic decisions allowing a participant to unilaterally decide whether to commit or abort a transaction without a definitive decision from the coordinator. However, this violates the system of promises of 2PC. It's only for catastrophic and not regular use.
        - Limitations of distributed transactions:
            - XA based systems though they perform the task of keeping participant data systems in atomically consistent, they have major operational problems.
            - Transaction coordinator itself is a kind of database so it needs to be approached with the same care as any other database. It stores important outcomes of transactions in the transaction log
                - Many transaction coordinator implementations are not highly availaible by default, run on a single machine and have rudimentary replication support.
                - When coordinator is a part of the application server, it changes the nature of deployment. Coordinator logs forms a crucial part of system state. Such application servers are no longer stateless.
                - XA cannot detect deadlocks across different data systems since it would require a standardized protocol to exchange info on the locks each transaction is waiting for. It also does'nt work with SSI.
                - Distributed transactions have a tendency of amplifying failures which runs counter to our goal of running fault tolerant systems.
    - Fault tolerant consensus
        - Consensus means getting all nodes to agree on something. For example: If several people try to book the last seat on an airplane or a movie hall, or take the same username, consensus algorithm determines which one of these mutually incompatible operations is the winner. 
        - One or more nodes propose a value and the consensus algorithm decides on one of those values. In this formalism, the algorithm must satisfy below properties:
            - Uniform agreement: No two nodes decide differently
            - Integrity: No node decides twice
            - Validity: If a node decides value v, then v was proposed by some node. 
            - Termination: Every node that does'nt crash eventually decides some value
        - Uniform agreement and Integrity ensures everyone decides on the same outcome and once you decide an outcome, you cannot change it. 
        - Validity exists to rule out trivial solutions such as an algorithm that always decides null. 
        - The termination property formally proposes the idea of fault tolerance. Even if some nodes fail, the other nodes should reach to a decision.
        - Termination is a liveness property and others are safety properties
        - The system model of consensus assumes that when a node crashes, it disappears and never comes back. So any algorithm that has to wait for a node to recover is not going to satisfy the termination property. 2PC does not meet the requirements for termination. 
        - Any consensus algorithm requires at-least a couple majority of nodes to be functioning correctly to assure termination. Thus, the termination property assumes that fewer that half the nodes are crashed or unreachable. 
        - Most implementations of consensus guarantee that agreement, integrity and validity are always met. 
        - Most consensus algorithms assume that there are no Byzantine faults (sending contradictory messages to other nodes). Consensus can be made robust against these faults too. 
        - A large scale outage can stop a system from processing requests but it does'nt necessarily stop the consensus algorithm from taking valid decisions.
    - Consensus algorithms and total order broadcast:
        - Viewstamped replication, Paxos, Zab are some well known fault tolerant consensus algorithms.
        - These algorithms decide on a sequence of values which makes them total order broadcast. Total order broadcast by definition requires messages to be sent exactly once in the same order to all nodes. 
        - This is like doing several rounds of consensus - in each round nodes propose the message they want to send next, and then decide the next message to be delivered in total order. 
        - Total order broadcast is equivalent to repeated rounds of consensus (each consensus decision corresponding to one message delivery)
            - Agreement: All nodes decide to deliver the same messages in the same order.
            - Integrity: The messages are not duplicated
            - Validity: Messages are not corrupted
            - Termination: Messages are not lost.
        -  Viewstamped replication, Paxos, Zab implement Total order broadast directly rather than doing repeated rounds of one-at-a-time consensus. This optimization is called Multi-Paxos
    - Single-leader replication & Consensus:
        - Is Single leader replication total order broadcast? Because a single leader takes in all the writes and ensures these are propogated to all replicas in the same order? 
        - If the leader is manually chosen and configured, it's a dictorial way of consensus. Only one node accepts writes and when that node goes down, the system becomes unavailaible for writes unless the operators manually configure a different node to be a leader. This does not satisfy termination property of consensus since it requires human intervention to make progress.
        - Automated fail-over is also performed by some databases. This brings us closer to fault-tolerant total order broadcast and thus to solving consensus
        - There is an associated problem of split-brain with automated fail-over. Thus we need consensus to elect a leader. The consensus algorithms described are actually total order broadcast and total order broadcast is like single leader replication, single leader replication requires a leader which in turn requires consensus!
    - Epoch numbering and quorums:
        - All consensus protocols discussed till now use a leader. But they don't guarantee that a leader is unique. 
        - They make a weaker guarantee. They define an epoch number (ballot number: Paxos, view number: Viewstamped Replication, term number: Raft) and guarantee that within each epoch the leader is unique.
        - Every time, the current leader is thought to be dead, a vote is started among the nodes to elect a new leader. 
        - This election is given an incremental, monotonically increasing epoch number. Provided there is a conflict between two different leaders in two different epochs (previous leader was not dead), the leader with the highest epoch prevails.
        - A leader must first check if there is'nt some other leader with a higher epoch number which might take a conflicting decision. A leader must'nt trust its own judgement. It must collect votes from a quorum number of nodes. A node votes in favor of a proposal only if it's not aware of any other leader with a higher epoch. 
        - Thus, we have two rounds of voting: to choose a leader and to vote on a leader's proposal.
        - The quorums for these two votes must overlap. If a vote on a proposal succeeds, at least one of the nodes that voted for it must have participated in the most recent leader election. 
        - If the vote on a proposal, does not reveal any higher numbered epoch, current leader can conclude no leader election with a higher epoch number has happened and it still holds the leadership. 
        - This looks similar to 2PC. But with 2PC, the coordinator is not elected. Fault tolerant consensus algorithms require votes from a majority of nodes. 2PC requires a "yes" from every participant. 
        - Consensus algorithms define a recovery process by which nodes get into a consistent state after a new leader is elected, ensuring that safety properties are always met.
    - Limitations of consensus:
        - Consensus algorithms are a hugh breakthrough for distributed systems 
            - Bring concrete safety properties (agreement, integrity, validity) where everything else is uncertain.
            - Remain fault-tolerant (able to make progress as long as majority of nodes are reachable/working)
            - Provide total order broadcast and therefore can also implement linearizable atomic operations in a fault tolerant way. 
            - The benefits come at a cost. 
                - The process by which nodes vote on proposals is a kind of sync replication. Many databases are configured to perform async replication. In this configuration, some commited data can be potentially lost on failover. 
                - Consensus requires a strict quorum to operate. If a network failure cuts off some nodes from the rest, only the majority portion of the network can make progress and the rest are blocked. 
                - Consensus algorithms assume a fixed set of nodes that participate in voting which means you can't add or remove nodes in the cluster. Dynamic membership extensions to consensus algorithms allow set of nodes in the cluster to change over time but they are much less understood that static membership algorithms.
                - In environments with highly variable network delays, a node often is falsely believed to be dead. Consensus systems rely on timeouts to detect failed nodes. Frequent leader elections in such environments result in terrible performanc because the system can end up spending more time in choosing a leader than doing any useful work.
                - Designing consensus algorithms that are wholly robust to network problems are an open research problem.


## Membership and coordination services
- Zookeeper/etcd are often described as "distributed key-value stores" or coordination & configuration services. So you can read/write keys, iterate over keys. 
- Zookeeper is not directly useful as a database. We end up relying on it indirectly via some other database like Hadoop, Kafka, YARN indirectly rely on it.
- Zookeeper/etcd are designed to hold small amounts of data that can entirely fit in memory (for durability it's stored on disk as well).
- This data is replicated across multiple nodes using a fault tolerant total order broadcast algorithm. 
- Zookeeper is modelled after Google's Chubby, implementing not only total order broadcast but also other features that turn out to be pretty useful when building distributed systems. 
    - Linearizable atomic atomic operations: Using an atomic compare-and-set, you can implement a lock. Several nodes trying to perform the same operation, only one of them will succeed. A distributed lock is usually implemented as a lease, which has an expiry time so that it's eventually released if the client fails. 
    - Totally ordered operations: When some resource is protected by a lock, you need a fencing token to prevent clients from conflicting with each other. The fencing token is a monotonically increasing number that increases when the lock is acquired. Zookeeper totally orders all operations and gives each operation a monotonically increasing transaction ID (zxid) and version number (cversion)
    - Failure detection: Clients maintain a long session on the Zookeeper servers and client and server periodically exchange heartbeats to check if the other node is alive. If the heartbeat cease for a duration that is longer than the session timeout, Zookeeper declares the session dead. Any locks configured by a session can be configured to be released when the session expires (ephemeral nodes) 
    - Change notifications: Not only can the client read locks and values created by other client, it can also watch for changes. Thus a client can find out when another client joins the Zookeeper cluster or if another client fails (because it's session times out and its ephemeral nodes disappear). By subscribing to notifications, a client can avoid frequent polling to find out about the changes
- A combination of the above features is what makes Zookeeper unique for distributed coordination.
- Allocating work to nodes:
    - One example where Chubby/Zookeeper works well if we have several instances of a process or a service, one of them gets chosen as a leader or primary. This is useful for single leader replication 
    - One more example is when you have a partitioned resource (database, message systems, file storage, distributed actor) and we need to decide which partition to assign to which resource. As nodes get removed or failed, other nodes need to take over the failed node's work.
    - A combination of atomic operations, ephemeral nodes and notifications in Zookeeper
    - This approach allows application to automatically recover from faults without any manual intervention. 
    - If an application grows from a few nodes to thousands of nodes, consensus algorithms over so many nodes may get slow. Zookeeper just focuses on a fixed number of nodes and performs its majority votes among those nodes while supporting a potentially large number of clients. 
    - The kind of data managed by Zookeeper is quite slow changing. It represents information like "node running on IP address 10.1.1.23 is the leader for partition 0". Zookeeper is not intended for storing run-time state of the application which may changes millions of times per second. For this, other tools like Apache BookKeeper can be used.
- Service Discovery:
    - Zookeeper, etcd, Consul are used for service discovery. That is to find out which IP address you need to connect to in order to reach a particular service. 
    - You can configure your services such that when they start up, they can register their network endpoints in a service registry where they can be found by other services.
    - Less clear if service discovery requires consensus
    - DNS was the traditional way of resolving IP address for a service name. It uses caching to achieve good performance and availaibility. Reads from DNS are not linearizable, it's not considered problematic if DNS entries are a bit stale. It's more important for DNS to be robust and reliably availaible.
    - If the consensus system knows the leader, it can help other services to discover who the leader is. Therefore its important in Service discovery. 
    - Certain consensus systems support read-only caching replicas. These replicas asynchronously receive the log of all decisions of the consensus algorithm but they do not participate in voting. 
- Membership services:
    - Zookeeper and friends can be seen as a part of a long history of research into membership services and are important for building highly reliable systems. 
    - Membership service determines whihc nodes are currently active and live members of a cluster. 
    - If we couple failure detection with consensus, nodes can come to an agreement on which nodes constitute the current membership. 
    - It's very useful for a system to have an agreement on which nodes currently constitute the current membership. 