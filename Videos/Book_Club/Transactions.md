
Video: https://www.youtube.com/watch?v=1VATRNlTTxk&list=PLBtMh4xfa9FGWU4E4oP9uqHuSl9ht7wXD&index=25(30f:00)

# Transactions 

1. Set of operations that get executed as a single unit
2. What problems do transactions can be avoided
    a. Databases crash
    b. Network interruptions
    c. Applications can crash
    d. Bad request from client
    e. Concurrency problems
    f. Race conditions.

## Transaction Pros
1. Simpler error handling
2. Application need not worry about issues

## Cons
1. Performance hit due to locks/serializability

## ACID
1. Atomicity: Operations are grouped as unit and in case of failure, the whole unit is aborted and rolled back. There are no partial results that are retained.
2. Consistency: More property of application than of the database. Application is responsible for maintaining those invariants like account balance cannot be negative. Integrity is a related concept. Business rules that dictate the standards for acceptable data. These rules
are applied to a database by using integrity constraints and triggers to
prevent invalid data entry. Consistency states that only valid data will be written to the database.
3. Isolation: Concurrent requests should be performed in a way such as they are performed serially.
4. Durability: Data commited is never lost. WALs are often used to reconstruct data. Replicated databases maintain multiple copies of data. Solid durability can be guaranteed by replication, writing to disk and taking backups.


## Single Object writes
If there is 20 KB JSON to be written, only 14 KB gets written and a failure happens. If a client is reading while this is being written, will he read a partial JSON? Single object writes can't be called transactions

## Multi-object transactions
1. Reference updation: Whenever the course reference needs to be updated for a student. 
2. Updating denormalized data: 
3. Secondary indexes updation

