Video: https://www.youtube.com/watch?v=t9DTf4oMN7A&list=PLBtMh4xfa9FGWU4E4oP9uqHuSl9ht7wXD&index=2(1:18:00)

# Isolation Levels - A Practical Demo in SQL server


1. Isolation levels define when and how parts of a transaction become visible to other transactions/ Defines what kinds of anomalies occur. If a transaction T writes data item X, for transaction T' trying to read, what is the expected behavior that can occur? Do we block the read or allow it to go through

2. We don't follow a single isolation level because of the performance costs of supporting a strict isolation level.

3. Types of anomalies
   a. Dirty Read/Writes
   b. Repeatable read: Even in a single transaction, reading same data-item multiple times may not yield the same value since readers don't block write.
   c. Phantom reads: If we try to read values initially in one transaction and if we try to insert a new row in another transaction that commits. Then, in the first transaction, there will be a new row that will be seen in the result if we re-read data. Even if it's non-repeatable read isolation level, it will still allow to go through, since we're inserting a new record.
   d. Write skew 
   e. Lost updates

4. Pessimistic Isolation Levels in brief

  a. Read Uncommitted: Able to read the uncommited value of a transaction. Problem is if we rollback, the application may have read an incorrect value. Moreover, if we do more operations on the revised value then we land in more problems. Performance wise this is very simple. May be used for debugging. Readers are not blocked by writers

  b. Read Commited: We get a exclusive lock on write and a shared lock on read. If a transaction is executing write X and Read X by a different transaction is blocked. Read commited prevents dirty reads and dirty writes but not non-repeatable reads. In this case, 

  c. Non-repeatable read: If a transaction tries to select rows with ID 1 to 20 it's not yet commited. If another transaction comes in and tries to modify row id 15, it stays blocked till the first transaction completes. Within a single transaction, you don't see two different snapshots of same data items. In this case, the transactions holds the read lock till the transaction ends. This can lead to a lost update problem if two transaction try to update the same data item one after another. The last write wins here since it's unaware of the prior update.

     T1      T2
    Alex (U)       

               Alexander(U) (wait)
    Alexa(U)
    Commit

d. Serializable: Serializable gives us an index-range lock on records to avoid phantom-reads. For example: if transaction T1 reads records with ID 1 to 100, there will be shared lock in this range. IF T2 tries to insert a record 90 in this range (presuming 90 is not there) it's blocked till T1 completes.

5. Optimistic Isolation Levels: Locking leads to blocking and it affects concurrency and can lead to deadlocks. Here we create versions of our data and it's the responsibility of the database to work on the versions and decide which version to return. 

   a. Read Commited Snapshot: Does not block on an uncommited value but returns the old value. It returns the new value after the transaction is committed. It's basically a statement-level isolation. For example:

   X = 5
   Begin transaction

   Update X <- 10
   Select X 

   Under read commited snapshot isolation level, this will give 10. Under plain snapshot isolation level this will give 5 since snapshot is taken at start of transaction.