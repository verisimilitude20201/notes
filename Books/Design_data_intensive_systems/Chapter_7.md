# Transactions

## Introduction
1. Many things can go wrong in data systems
    - DB software may fail at any given time. Even in the middle of a write operation
    - Application may crash halfway in a series of operations
    - Network interruptions
    - Several clients writing to the database at the same time overwriting each other's changes
    - Race conditions
    - Partial data reads

2. Implemnting a fault tolerant mechanism requires a lot of work. Requires careful thinking and then testing the solution to make sure it actually works.
3. Transactions have been the mechanism of choice for simplifying this. It groups all reads/writes in a singl logical unit. All reads/writes execute as a unit; Either the unit succeeds or it fails. We don't need to worry about the partial state.
4. By using transactions, the application is free to ignore certain potential error scenarios and concurrency issues cause the database takes care of them (safety guarantees)
5. To some applications, there are advantages to not using transactions. Weakened transaction guarantees to some extent help achieve high performance or higher availaibility.

## The Slippery concept of a transaction

1. Most transaction styles supported by databases today follow the style introduced in 1975 IBM System R. 
2. NoSQL databases introduced in late 2000s started gaining popularity. They improved on the relational status quo by offering choices of new data models, and having replication and partitioning by default.
3. These databases abandoned transactions entirely or redefined the word to describe much weaker set of guarantees.
4. There emerged a popular belief that transactions are the anti-thesis of scalability and that any large scale database system  would have to abandon transactions to maintain good performance and high availaibility. Transactional guarantees are sometime presented by DB vendors as essential requirement for valuable data. 
5. The truth is there are trade-offs and different pros and cons of transactions.

### The meaning of ACID
1. The safety guarantees provided by transactions are often described by well-known ACID standing for atomicity, consistency, isolation and durability. This serves to establish fault-tolerant mechanism in databases. 
2. ACID has unfortunately become a market-term. When a system claims to be ACID complaint, it's unclear what guarantees it can actually expect. 
3. Systems not meeting the ACID guarantees are sometimes called BASE (Basically Availaible, Soft State & Eventually Consistency) which is even more vague. 

#### Atomicity
1. Atomic refers to something that cannot be broken down into smaller parts. 
2. It's an overloaded term meaning different things in different areas of computer science. 
    - In multithreading, it means there is no way another thread could see the half-finished result of the operation. 
3. In ACID, atomicity, does not mean concurrency.  It describes what happens if a client wants to make several writes but a fault occurs if certain writes have been processed. If all writes are grouped together and the transaction cannot be completed due to a fault, then all changes are discarded.
4. Without atomicity it's difficult to understand which changes have taken effect and which cannot. Atomicity simplifies this problem. The ability to abort a transaction on error and have all writes discarded is a defining feature of ACID.

#### Consistency
1. Again consistency is also overloaded
    - Replica consistency & Eventual consistency
    - Consistent hashing applied to partitioning that some systems use for rebalancing. 
    - CAP theorem uses the word consistency to mean linearizability
    - In ACID, consistency referes to a database to an application being in a good state.
2. The idea of ACID consistency is that you have certain invariants about your data that must always be true. If a transaction starts with a database that is valid according to these invariants and any writes during the transactions preserve the validity, then you can be sure that the invariants are always satisfied. 
3. It's the application's responsiblity to define the transactions correctly and preserve consistency. Application defines what data are valid/invalid. 
4. Atomicity, isolation and durability are properties of the database whereas consistency is a property of the application. Application may rely on databases isolation and atomicity to achieve consistency but it's not to the database alone. 

#### Isolation
1. Databases are being accessed by several clients at the same time. If they are accessing different parts of the database it's no issues. But if they are accessing/writing the same records, we can run into race conditions.
2. Isolation in sense of ACID means concurrently executing transactions are isolated from each other; they cannot step on each other's tows. 
3. The classic database textbooks call this as serializability meaning each transaction can pretend it's the only transaction running on the database. The database ensures that when the transactions have been commited, the result is the same as if they run serially even though in reality, they may be concurrent.
4. Serializability carries a performance penalty. Oracle has a serializable isolation level but it actually implements something as snapshot isolation which is a weaker guarantee than serializability.

#### Durability
1. Durability is a promise that once a transaction has commited successfully, any data it has written won't be forgotten even is there is a hardware fault or database crash. 
2. Data has been written to a non-volatile storage such as hard drive / SSD. It usually involves a WAL log or similar which allows recovery in the case data on disk becomes corrupted. 
3. For replicated databases, durability usually means that the data has been successfully copied to number of nodes. 
4. To provide a durability guarantee, a database must wait until replication complete before reporting a transaction as successfully commited.
5. Perfect reliability does not exist, if all hard disks and backups are destroyed at the same time, nothing we could do to recover the data. There is no one technique that can provide absolute guarantees. There are only various risk-reduction techniques such as writing to disk, replicating to remote machines, backups and they should be used together.

### Single-object and multi-object transactions
1. In ACIDm atomicity and isolation describe what a database should do if a client makes several writes within the same transaction
    - Atomicity gives you an all or nothing guarantee. In case of failure abort everything upto that point.
    - Isolation means concurrently running transactions should'nt interfere with each other. If one transaction makes several writes, then another transaction should see either all or none of those writes.
2. These assume that we want to modify several objects at once. Such multi-object transactions are needed if several pieces of data need to be kept in sync
3. Consider an email application showing the count of unread emails. If it's implemented as a counter field that gets incremented when a new email comes. An anomaly state may be the user getting shown the counter to be 0 but still there's one new unread message. This is a violation of isolation. In an atomic transaction if update to the counter fails, the transaction is aborted and inserted email is rolled back.
4. Relational databases determine the read/write operations belonging to a transaction by checking the client's TCP connection. Everything between a COMMIT and BEGIN TRANSACTION block is a part of the same transaction. 
5. NoSQL databases don't have mechanism of grouping operations together. Even if there's a multi-object API (key-value store having a multi-put operation) that does'nt necessarily mean it has transaction semantics. The command may succeed on some keys and fail for other.

### Single object writes

1. Atomicity and isolation apply when a single object is being changed. Storage engines universally aim to provide atomicity and isolation at the level of a single object. 
2. Certain databases also provide a complex atomic increment operation removing the need for a read-modify-write cycle. A compare and set operation, allows a write to happen only if the value has not been set by someone else. 
3. Compare-and-set and atomic increment are not transactions in the real sense, they are lightweight transactions referred to in marketing terminology.

#### Need for multi-object transactions
1. Distributed databases abandoned multi-object transactions totally because they are difficult to implement across partitions and they get in the way of high availaibility and performance requirments
2. In many cases, the writes to several different objects need to be coordinated
    - For RDBMS, we require coordination to maintain the FK-PK relationship
    - In case of document databases, if data is stored in a denormalized fashion, we would need to update several documents in one go. 
    - For databases with secondary indexes, the indexes also need to be updated every time we make a change to a value.
3. Above applications can be implemented without transactions. However error handling without atomicity, isolation becomes complicated. 

#### Handling errors and aborts
1. Not all systems follow the ACID philosophy - totally discard the changes in case of error. 
2. Datastores with leaderless replication work on a "best effort" basis which can be summarized as "The database can do as much as it can and if it runs into an error, its the application's responsibility to recover from errors"
3. Many developers prefer to think only about the happy path. ORM frameworks don't retry aborted transactions.
4. Retrying aborted transactions is simple but has caveats
    - The transaction may have succeeded but the network failed while acking the success to the client. Retry would cause it to happen again
    - If transaction failed due to overload, retrying it again can complicate the problem. Using exponential backoff may help.
    - It's worth retrying only after a transient error (deadlock, isolation failure, temporary network interruption). For constraint failure, retrying would be pointless
    - If a transaction has side-effects outside of the database, those may occur even if the transaction is aborted. If we want several different systems to abort together, use 2-Phase commit.
    - If client process fails while retrying, any data it was trying to write will be lost.


## Weak isolation levels
1. Concurrency issues only happen when one transaction reads the data when one transaction reads data that's concurrently modified by some other transaction or when two transactions try to concurrently try to modify the same data.
2. Concurrency bugs are hard to find because they are triggered when you get unlucky with timing.
3. It's also very difficult to reason about especially in a large application, where you don't know which other pieces of data are accessing the database. 
4. Databases have simplified the handling of concurrency by providing transaction isolation. Serializable isolation means that database guarantees that transactions have some effect as if run serially. 
5. Serializable isolation has a performance cost and so it's common for systems to use weaker levels of isolation  which protect against some concurrency issues but not all. These can lead to subtle bugs but are nevertheless used in practise. 
6. Rather than replying on tools, we need to develop a good understanding of kinds of concurrency problems that exist and how to prevent them. 

### Read commited
1. This makes two guarantees
    - When reading from the database, you read only commited data.
    - When writing to the database, you will only overwrite data that has been commited.
2. Dirty Read:
    - If a transaction is able to read uncommited written data for another transaction, it's called a Dirty read.
    - At this isolation level, dirty reads must be prevented. Any writes by a transactions becomes visible when that transaction commits. 
3. There are a few problems with dirty reads
    - If a transaction needs to update several objects, a dirty read means other transaction may be able to see some updates but not others. This is confusing to users and may cause transactions/users to take incorrect decisions based on partially updated data.
    - A transaction may be able to see data that may be rolled back later on. This has mind bending consequences
4. Dirty writes:
    - In case of concurrent writes to a same object/row in database, the later value overwrites the previous value
    - If the earlier write is a part of a transaction that has not yet been commited, if the later write overwrites it, it's called dirty write.
    - Transactions running at the read commited isolation level delay the second write till the first transaction's write is commited or aborted. 
    - By preventing dirty writes, we prevent the below problems
        - If transactions updated multiple objects, dirty writes can lead to a very bad outcome. For example: on a car sales website, if Alice and bob are trying to buy the same car, the website needs to be updated to reflect the buyer and send the invoice. With Dirty Writes, Alice may buy the car but the invoice may be sent to Bob.
    - Read commited does not prevent race conditions between counter increments. Second write happens after the first write has commited so it's not a dirty write.
5. Implementing read commited: 
    - Read commited is the default isolation level in many databases such as Oracle.
    - A transaction needing to write to an object must first acquire a row-level lock on it. 
    - It holds on to the lock till it commits or aborts. 
    - Other transactions that need to write to the same object must wait until the first transaction finishes. 
    - To prevent dirty reads, we can have read-level locks as well and to require a transaction to acquire the lock on read and release it on commit/abort. 
    - The practise of read locks does'nt work well, because one long running transaction could force many other transactions to wait until the long running transaction completes. 
    - This hampers response time and is bad for operability.
    - For this reason, database remembers both the old value and the new value set by the transaction. When the transaction is ongoing, any reads get simply the old value. After the transaction commits, we get it's new value.

### Snapshot isolation and repeatable read
1. There are plenty of ways in which we can have concurrency bugs when we use the read commited isolation level. 
2. Repeatable read is a timing anomaly. Say Alice has two accounts A and B contianing $500 and she's transferring $100 from A to B. If she is looking at both accounts while this transfer is in progress, she may see $400 in account A and $500 in account B. 
3. Repeatable read or read skew is accepted under read commited isolation since the account balances were indeed commited at the time when she read them. 
4. Certain situations can't tolerate any temporary inconsistency.
    - Backups of large databases may take time and as backups are being taken, writes continue to be made on the main database. We end up some parts of the database containing older version of data and some parts contain newer version of the data. If we restore from this backup, the database inconsistencies become permanent
    - Analytic queries that scan large parts of the database may return nonsensical data
5. Snapshot isolation: Each transaction reads from a consistent snapshot of the database that was commited at the start of the transaction. This is a common solution to this problem. Snapshot isolation is a common feature of PostgreSQL, MySQL and InnoDB storage engine.
6. Implementing snapshot isolation
    - Write locks are used to prevent dirty writes. This means a transaction that makes a write can block the progress of another transaction that writes to the same object. 
    - Reads don't require locks. 
    - A key principle of snapshot isolation: Writers don't block readers or readers don't block writers. So a database can handle long-running read queries on a consistent snapshot at the same time as processing writes normally without any lock contention
    - The database maintains several versions of the object side-by-side. This is known as multi-version concurrency control
    - Storage engines supporting snapshot isolation typically use MVCC for their read commited isolation level as well. Typical approach is read commited uses a separate snapshot for each query while snapshot isolation uses the same snapshot for an entire transaction.
7. How MVCC based snapshot isolation is implemented in PostgreSQL
    - A transaction is given a unique txid thats always monotonically increasing. 
    - Each write to the database is tagged with the txid of the transaction it happens in.
    - Each row has a created_by and deleted_by field indicating the transaction that inserted this row into the table or deleted this row from the table. 
    - A delete row is marked for deletion by updating the deleted by field. After some time, a garbage collection process marks the rows for deletion and frees their space. 
    - An update is internally translated into a delete and create. 
    - Visibility rules for observing a consistent snapshot 
        - At the start of each transaction, the DB makes a list of all other transactions in progress and ignore the writes made by them even if they are commited later. 
        - Aborted transactions' writes are ignored
        - Any writes made by a transaction with a later transaction ID started after the current transaction are ignored.
        - All other writes are visible.
    - To summarize, an object is visibile if both below conditions are true
        - At the time of reader transaction start, the transaction that created the object must already be commited. 
        - The object is not marked for deletion or if it is, the transaction requesting the deletion had not commited at the time the reader transaction started. 
8. Indexes and snapshot isolation:
    - One way to implement indexes is to point to all versions of an object and require an index query to filter out object versions not visible to the current transaction. On garbage collection, these deleted index entries are purged out along with the data.
    - CouchDB, Datomic, LMDB use an append-only/copy-on-write variant that does not overwrite the pages of the tree but creates a new copy of each modified page. 
    - Parent pages upto the root of the tree point to new, modified copy. 
    - With append-only B-trees, every transaction creates a new B-tree root and a particular root is a consistent snapshot of the database at the point at which it was created. 
    - The CouchDB approach requires background process for compaction and garbage collection.
9. Repeatable read and naming confusion
    - Many databases call snapshot isolation by different names. Oracle calls it serializable, PostgreSQL and MySQL call it repeatable read. 
    - SQL standard does'nt have concept of snapshot isolation because it's based on System R's 1975 definition of isolation levels and snapshot isolation was'nt invented by then. 
    - Instead it defines repeatable read superficially similar to snapshot isolation.
    - Different databases provide different guarantees in their implementations of repeatable read.
    - As a result nobody really knows what repeatable read means.

### Preventing lost updates
1. Uptil now, we have discussed what a transaction can read in face of concurrent writes. 
2. We have'nt talked about the issue of two transactions writing concurrently, we have just disussed dirty writes.
3. Lost updates is another problem that can occur during concurrent writes.
4. This problem typically occurs in a read-modify-write cycle where a value is first read, then modified and then written back to the database. If this is done by more than one transaction concurrently, the later write clobbers the earlier one. 
5. Below are common scenarios that employ read-modify-write and where lost updates can occur.
    - Incrementing an counter, updating account balance
    - Make a local change to a complex JSON
    - Two users editing a wiki page at the same time where each user reads, updates the page and overwrites the server's page with their contents. 
6. Common solutions to address this problem
    - Atomic write operations: 
        - Most databases provide atomic writes which avoid the need to do read-modify-write. 
        - Best solution if the code can be expressed in that way. For example: UPDATE counters SET counter = counter + 1 WHERE key = 'foo' is a concurrency-safe operation in most relational databases.
        - MongoDB provides atomic operations to modify parts of a JSON document and Redis for data structures such as priority queues.
        - All atomic operations are either forced to execute on a single thread or acquire an exclusive lock on the object when it is read.
        - ORM frameworks unfortunately make it easy to accidentally write code that performs unsafe read-modify-write operations. 
    - Explicit locking:
        - If database does not provide atomic writes, the application can explicitely lock objects being updated.
        - Then the application can perform a read-modify-write cycle on the object. If any other transaction concurrently tries to access the same object, it's forced to wait until the 1st transaction completes. 
        - For example: the use of a FOR UPDATE clause in a SELECT query that locks the rows returned by that query for the time-being of the transaction.
    - Automatically detecting lost updates:
        - An alternative to locking is allowing read-modify-write cycles to happen in parallel and if the transaction manager detects a lost update, abort the transaction and force it to retry. 
        - Databases can perform this check efficiently with snapshot isolation. PostgreSQL's repeatable read, Oracle's serializable, SQL Server's snapshot isolation automatically detect when a lost update has occurred and abort the offending transaction. MySQL/InnoDB's repeatable read does not detect lost update. 
    - Atomic compare-and-set: 
        - Databases not supporting transactions, sometimes support an atomic compare-and-set.
        - The purpose of this operation is avoid lost updates by allowing an update to happen only if the value has not changed since it was last read. 
        - For example: for two users concurrently modifying a Qwiki, UPDATE wiki_pages SET content = 'new content' WHERE id = 1234 and content = 'old content'. If the content has changed, this update will have no effect. So we first need to check if the update took place
        - If the database allows to read from old snapshot for the WHERE clause, this statement may not prevent lost updates because the condition may be true even though another concurrent write is occurring.
    - Conflict resolution and replication
        - On replicated database, conflict resolution takes on another dimension since these are replicated to multiple nodes with concurrent updates. 
        - They allow multiple writes to occur concurrently and they cannot guarantee a single up-to-date data copy. So techniques based on locks and compare-and-set don't apply. 
        - A common approach is to allow several current writes to create several concurrent versions (siblings) of a value and use application code or special data structures to merge these values. 
        - Atomic operations work well if they are commutative (if applied in different orders on different replicas, they give the same value eg. incrementing a counter or adding an element to a set). 
        - Riak merges all conflicting updates for a concurrently updated value.
        - Last writes wins is prone to lost updates. It's the default in many replicated databases

## Write Skews and Phantoms
1. Dirty writes and lost updates are race conditions occuring when different transactions try to concurrently write to the value objects. To safeguard, we either use atomic locks or atomic write operations.
2. There are some subtler conflicts that may also occur. For example Write Skew: 
    - Consider an application managing on-call shifts for doctors at a hospital. The requirement is that there should be at-least 1 doctors for the on-call shift. 
    - If we have two doctors, both press the go off call button at the same time. Each transaction checks if two or more doctors are currently on-call and if yes, it's okay for both to call off. 
    - DB is using snapshot isolation and both checks return 2. Both transactions proceed to the next stage. 
    - Both transactions commit and your requirement of having at-least one doctor on call is violated. 
    - This anomaly is called Write skew
3. Characterizing a write skew:
    - It's neither a dirty write or lost update because two transactions are updating two different objects.
    - A definite racce condition. The anomalous behavior occured because both transactions occurred concurrently.
    - It's a special case of the lost update problem. Write skew occurs when different transactions read same objects and then update some of them.
    - For preventing write skew we have more restricted options
        - Atomic transactions won't help as we have multiple transactions involved. 
        - Write skew is not automatically detected in PostgreSQL's repeatable read, Oracle's serializable or SQL server's snapshot isolation level. It requires true serializability.
        - Certain databases have constraints like foreign key constraint and so on. For this, you would need a constraint that involves multiple objects which may be implemented with triggers or materialized views.
        - A best option is to lock the rows the transaction depends on like
        ```
        BEGIN TRANSACTION

        SELECT * FROM doctors WHERE on_call = true AND shift_id = 1234 FOR UPDATE;

        UPDATE doctors SET on_call = false WHERE name='Alice' AND shift_id=1234;

        END TRANSACTION;

        ```
    - Few more examples of Write skew
        - Meeting room booking: You want to enforce that there can't be no two bookings for the same meeting room at the same time. Before making a booking, you first check for any conflicting bookings and if none are found you create the meeting. Once again, truely serializable isolation required. 
        - Multiplayer game: Lock can prevent lost updates i.e. preventing two player from moving the same piece. But the lock does'nt prevent players from moving two different figures to the same position
        - Claiming username: On a website where each user has a unique username, two users may try to create accounts with the same username at the same time. It's not safe under snapshot isolation but you can create a unique key constraint to quickly fix this.
        - Preventing double spending: Two concurrent transactions may cause the balance to together go negative.
    - Query Patterns causing write skew:
        - SELECT query checks whether a requirement is satisfied by searching for some rows that match some search condition
        - Depending on the result of first query, application code decides whether to continue with operation or abort
        - If application decides to go-ahead, it makes a write and commits the transaction. The effect of this write changes the precondition of the decision of the above step.
        - You may decide to make the write first and then commit or abort the transaction on the basis of the result of the query.
        - The 3 examples of write skew viz. meeting room booking, multi-player game, claiming user name check for the absense of rows matching some search condition and then make an insert of the same condition. SELECT FOR UPDATE could not be used here. 
        - This effect where a write in one transaction changes the result of a search query in another transaction is called phantom.
        - Snapshot isolation avoid phantoms in read-only queries. But in read-write transactions, phantoms can lead to tricky cases of write skew.
    Materializing conflicts:
        - For the meeting room example, you can imagine creating a table of timeslots and bookings and rooms. 
        - Each row in the booking table corresponds to a particular room for a time period. 
        - Now a transaction can lock (SELECT FOR UPDATE) the rows corresponding to the desired room and time period. After acquiring the locks it can check for overlapping bookings and insert a new booking.
        - The additional table is a collection of locks used to prevent concurrent booking the same room and time-range. This approach is called materialing conflicts. 
        - It's hard to figure out materializing conflicts and so should just be used as a last resort. A serializable isolation level is much more preferable.

## Serializability
1. Several transactions are prone to race conditions. Some are prevented by read commited and snapshot isolation levels and some subtler ones like write skews and phantoms are not. 
    - Isolation levels are hard and inconsistently implemented in databases
    - Hard to tell  by just looking at application code whether its safe to run at a particular isolation level. 
    - Testing for concurrency issues is hard and no good tools to help detect race conditions.
2. The solution - Serializable isolation. It's regarded as the strongest isolation level. 
3. Guarantees that even thoguh transactions execute in parallel, the end result is as if they executed one at a time. No race conditions can occur. 
4. The main question now is why it's not used? Most databases that provide serializable isolation use one of the 3 below techniques
    - Execute transactions in a literal serial order
    - 2-Phase locking
    - Optimistic concurrency control techniques such as serializable snapshot isolation.
5. Let's discuss these techniques in the context of single node transactions.

### Actual Serial execution
1. Execute only a single transaction at a time, no concurrency at all. We completely sidestep the problem of detecting and preventing conflicts between transactions. 
2. Database designers fairly recently realized that single-threaded loop for executing transactions was feasible. 
3. Two main developments caused this rethink
    - RAM became cheap enough. For many usecases its possible to store to the entire dataset in memory. When all data that a transaction needs is in memory, transactions can execute much faster rather than wait for data to be loaded from disk.
    - Database designers realized that OLTP transactions are short-lived and only make a short number of reads/writes. Long-running queries are read-only so they can be run on a consistent snapshot outside of the serial execution loop.
4. A system designed for single-threaded transactions can perform better than a system supporting concurrency because it does not need locking. It's CPU is only limited to a single CPU core. 
5. To make best use of that single thread, transactions need to be structured differently from their traditional form.
6. Encapsulate transactions in stored procedures
    - In the early days of databases, the intention was that an entire transaction could encompass a user activity such as airline booking. DB designers thought it would be neat if it could be a single transaction such that it could be commited atomically. 
    - This would lead to a lot of wait time because humans are slow to respond. 
    - All OLTP applications avoid the wait by adopting an interactive style. An application makes a query, waits for the result, then issues another query.
    - The throughput would be dreadful because the database would spend a lot of its time waiting for the application to issue the next query. It's necessary to process multiple transactions concurrently in order to get reasonable performance. 
    - Systems with single-threaded serial transactions don't allow multi-statement transactions. The application must submit the entire code to the database ahead of time as a stored procedure. 
    - Provided that all required data is in-memory, the stored procedure can execute very fast without any network or disk I/O.
    - Stores Procedures: Pros and Cons
        - They have been around for quite a long time and they have been included in the SQL standard. They have a bad reputation due to several reasons
            - Each DB vendor has it's own language for stored procedures, have'nt kept up with developments in general purpose programming languages
            - Code running in a database is difficult to manage, version control, test and deploy.
            - A Single database instance is shared among many applications. A badly written stored procedure can cause much more trouble than equally badly written code in an application server.
        - Modern implementations of stored procedures have abandoned PL/SQL and use existing general-purpose programming languages. For eg: Redis uses Lua. 
        - Stored procedures and in-memory data makes executing all transactions on a single thread feasible avoiding the need of other concurrency control mechanisms. 
        - VoltDB uses stored procedures for replication. It requires that stored procedures be deterministic (when run on different nodes, they produce the same result.)
7. Partitioning
    - For applications with a high write throughput, single threaded transaction processor can become a bottleneck. Because the transaction throughput is limited to the speed of a single CPU core.
    - To scale to multiple CPUs, VoltDB supports partioning of data. Each transaction just needa to read/write data within a single partition. 
    - Each CPU core is given its own partition which allows the linear scaling w.r.t the number of CPU cores. 
    - Multi-partition accesses need to be coordinated by the database. The stored procedure needs to be performed in lock-step to ensure serializability across the entire system. 
    - Cross partition transactions are very slower as compared to single partition transactions and their performance cannot be increased by adding more machines.
    - Often data with multiple secondary indexes is likely to require a lot of cross partition coordination.
8. Summary of serial execution:
    - To use serial execution there are certain constraints.
        - Every transaction should be small and fast.
        - Limited to use cases where the active dataset can fit in memory.
        - Write throughput must be low enough to be handled on a single CPU.
        - Cross partition transactions possible but there is a hard limit to the extent to which they can be used.

### Two-Phase Locking (2PL)
1. For several years, 2PL was the only way to implement serializability. 
2. 2PL makes the locking requirements stronger. 
3. Several transactions are allowed to concurrently read the same object as long as nobody is writing to it. As soon as anyone wants to modify that same object it requires an exclusive access to it.
    - If transaction A wants to read the object that B has an exclusive write lock on, B needs to wait until A commits/aborts and the lock is released.
    - Reading an old version of the object is not accetable under 2-PL.
4. Writers dont just block other readers, they also block other writes as well in 2PL
5. Snapshot isolation has the mantra readers never block writers and wrters never block readers.
6. Because 2PL provides serializability, it protects against all the race conditions.
7. Implementation of 2PL
    - 2PL is used by the serializable isolation level in MySQL and repeatable read isolation level in DB2
    - Blocking on readers and writers is implemented by having a lock on each object in the database. It can be in shared mode or exclusive mode.The lock is used as follows
        - If a transaction wants to read an object, it first acquire the lock in shared mode. Several transactions can hold the shared lock on the object, so if there is any existing lock, the transaction must wait.
        - If a transaction first reads and then writes an object, it upgrades its shared lokc to an exclusive lock. The upgrade works directly as getting an exclusive lock.
        - After acquiring a lock, it must continue to hold the lock until the end of the transaction (commit or abort). This is from where the name 2PL comes from; locks are acquired while executing the transaction and released when the transaction completes.
    - The database automatically detects deadlocks between transaction and aborts one of them so others can make progress
8. Performance of 2PL:
    - A big drawback of 2PL is performance throughput and response times of queries are worse under two phase locking and weak isolation.
    - This is more importantly due to reduced concurrency due to locks. If two concurrent transactions try to do anything that may result in a race condition, one has to wait for other to complete.
    - Traditional RDBMS don't limit the duration of a transaction because they are designed for interactive applications that await human input. There is no limit on how long a transaction may wait on another and this may form a queue of transactions even if thery are short. 
    - For this reason, databases running 2PL can have unstable latencies. If there is contention in the workload, it may take one slow transaction or one transaction that acquires many locks to cause the rest of the system to halt.
    - Deadlocks can happen with the lock-based read commited isolation level, they occur more frequently under 2PL serializable isolation level depending on access patterns of the application.
9. Predicate Locks:
    - Phatoms are when one transaction changes the results of another transaction's search query. A database with serializable isolation must prevent phantoms. 
    - For a booking room example, a second transaction is not allowed to concurrently insert or update another booking for another booking for the same room and time range.
    - This is implemented using a Predicate locks. Rather than belonging to an object, it belongs to a series of objects (rows) matching some condition. Such as
    ```
    SELECT * FROM Bookings WHERE room_id = 123 AND end_time > '2018-01-01 12:00' AND start_time < '2018-01-01 13:00'
    ```
    - Predicate lock restricts access as follows
        - A transaction A wanting to read the objects like in the above SELECT query needs to acquire a shared mode predicate lock on the conditions of the query. If another transaction B has an exclusive lock on any of the objects matching those conditions, A must wait until B releases it's lock.
        - If transaction A wants to write any object, it must first check if either the new or old value matches any existing predicate lock. If there is a matching predicate lock held by transaction B, A must wait until B has commited or aborted before it can continue.
    - Predicate locks apply even to those objects that may be inserted in the future.
    - If two-phase locking includes predicate locks, the database prevents all forms of write skew and so its isolation becomes serializable.
10. Index-range locks:
    - If there are many locks by active transactions, checking for matching locks becomes time-consuming. 
    - Most databases with 2PL implement index-range locking which is a simplified approximation of predicate locking
    - We can simplify a predicate by allowing it to match a greater set of objects. 
    - If you have a predicate lock for bookings of room 123 between 12 PM and 1 PM, you can approximate it 
        - By locking bookings for room 123 any time  OR
        - By locking all rooms between 12 PM and 1 PM.
    - Any write that matches the original predicate will also match the approximations.
    - We would have indexes on room_id and start_end & end_time. 
    - We can attach a shared lock on room_id to indicate that a transaction has searched for bookings of room id 123
    - For a time-based index, it can attach a shared lock to a range of values indicating that a transaction searched for bookings that overlap in the time period of 12 PM to 1 PM. 
    - Now if another transaction wants to insert, update or delete a booking for the same room or time period, it updates the same part of the index. It will encounter the shared lock and it will be forced to wait until the lock is released.
    - Index-range locks is not precise as predicate locks but since they have much lower overheads they are a good compromise.
    - If there is no suitable index where a range lock is assigned, the database can fall back on the entire table. This is not good performance wise since it stops all transactions from writing the table but its a safe fallback.

### Serializable Snapshot isolation (SSI)
1. On one had we have implementations of serializability that don't perform well (two-phase locking) or dont'scale well (serial execution). On the other hand, we have weak isolation levels that have good performance but are prone to race conditions (lost updates, write skews and phantoms)
2. This brings us a question - Are serializable isolation and performance at odds? 
3. An algorithm called SSI is very promising and also has a small performance penalty. It's fairly new and is by Micheal Cahill.
4. Both PostgreSQL since 9.1 single node database and FoundationDB use a similar algorithm.

#### Pessimistic Vs Optimistic concurrency control
1. Pessimistic concurrency control is based on the principle that if anything goes wrong, it's better to wait till the situation becomes safe again before doing anything. Similar to mutual exclusion to protect data structures in multi-threaded programming. 
2. Serial execution is pessimism to the extreme, it's equivalent to each transaction having an exclusive lock on the database. We make each transaction very fast to hold the lock for a short time.
3. Optimistic concurrency control means instead of blocking, if something potentially dangerous happens, transactions might continue anyway in the hope that everything may turn all right. 
4. On commit the database checks whether everything is all right and if so the transaction is aborted and re-tried. 
5. Optimistic concurrency control performs badly if there is high contention i.e. many transactions trying to access the same objects. This may lead to higher percentage of aborts and the additional transaction load of retrials can make performance worse.
6. If there is enough spare capacity and load is not that much, Optimistic concurrency control can perform very well. 
7. Contention can be reduced with commutative atomic operations. For example: If several transactions concurrently increment a counter, it does'nt matter in which order the increments are applied as long as counter is not read in the same transaction.
8. SSI is based on snapshot isolation ie. all reads are based on a consistent snapshot of the database. It also adds an algorithm to detect serialization conflicts among writes and determine which transactions to abort.

#### Decisions based on an outdated premise
1. For write skew under snapshot isolation, there is recurring pattern. A transaction reads some data from database, examines the result of the query and decides to take some action  based on the result it saw. 
2. However, under snapshot isolation, the result from the original query may no longer be up-to-date by the time the transaction commits.
3. The transaction is taking action based on a premise that was true at its beginning. Later when it wants to commit, the original data may have changed and the premise becomes false.
4. The transaction needs to assume that any change in the query result means that the writes in that transaction may be invalid. There may be a causal dependency between the read queries and the writes. 
5. To provide serializable isolation, the database must detect situations in which a transaction acted on an outdated premise and abort the transaction. How the database knows query results have changed? Two cases
    - Detecting reads of a stale MVCC object (uncommited write occured before the read)
    - Detecting writes that affect prior reads (the write occured after the read)
6. Detecting reads of a stale MVCC object:
    - Snapshot isolation is implemented by multi-version concurrency control. 
    - Transaction reading from a consistent snapshot ignores writes made by other transactions that have'nt been commited yet. 
    - In Alice's case, her transaction checked whether on_call was true which is not since her transaction is uncommited. However, before her transaction commits, Bob's transaction takes effect and Alice's transaction's premise is no longer true.
    - A database needs to track when a transaction ignores another transaction's writes due to MVCC visibility rules. Before commit, a database checks whether any of the ignored writes have been commited. If so, the transaction is aborted.
    - We must wait until the transaction commits, because it may be only a read-only transactions. We should avoid unecessary aborts. 
7. Detecting writes that affect prior reads: 
    - We discussed index-range locks which allow the database to lock access to all rows matching some search query.
    - Similar technique can be used here, but SSI does'nt block other transactions.
    - Bob and Alice transactions both search for on call doctors in shift_id 1234. The shift_id index can be used to record this fact and it can be kept until the transaction has commited or aborted. 
    - When a transaction writes to an object, it must look in the indexes for any transactions that have recently read the affected data but rather than blocking as in case of locks, it must notifies the Alice and Bob transactions that the data they read is stale.
8. Performance of SSI
    - The granularity of the tracking of each transactions read/write affects performance. If we go into great detail, it can be precise about which transactions need to abort. But the bookkeeping overhead becomes evident. Less detailed tracking is faster but it may lead to more aborts.
    - For some cases, it's okay if a transaction reads information written by another transaction, it's sometimes possible to prove that the result of the transaction is serializable. 
    - The biggest advantage of SSI compared to 2PL is one transaction does'nt need to block waiting for locks held by another transaction. Read-only queries can run on a consistent snapshot which is appealing for read-heavy workloads.
    - SSI is not limited to a single CPU core. FoundationDB distributes the detection of serialization conflicts across multiple machines allowing it to scale to very high throughput.
    - SSI rquires that read/write transactions be fairly short because a transaction that reads and writes data over a long period of time is likely to run into more conflicts and abort, so SSI requires that read/write transactions be fairly short. SSI is less sensitive to slow transactions than two phase locking or serial execution.