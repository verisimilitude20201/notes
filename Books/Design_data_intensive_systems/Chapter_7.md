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