Video: https://www.youtube.com/watch?v=_E43l5EbNI4(25:20)

# Why Uber shifted to MySQL from Postgres


## Introduction
1. Initially, Uber had a monolithic architecture with Python and PostgreSQL database.
2. They shifted to microservices with MySQL and schemaless which is a novel database sharding layer on top of MySQL.


Uber encountered many Postgres limitations

## PostgreSQL limitations

1. Inefficient architecture for writes
2. Inefficient data replication
3. Issues with table corruption
4. Poor replica MVCC
5. Difficulty to upgrade to new releases


## On-disk format
1. PostgreSQL maintains a physical tuple reference ID for each row in a table. 
2. Each key, secondary key maintains a reference to this tuple ref ID.
3. If a row is updated, it's duplicated first, a new tuple reference ID is assigned to it and then the updates are applied. All references of the old row are modified to reference this new tuple reference ID. This is a bad thing. It takes a ripple effect. 
4. Write ahead log gets large when you want to apply the changes. You push this write-ahead log to each standby database to replicate.


## Replication
1. All changes first written to a Write Ahead Log that forms the basis for replication
2. You push the WAL to the standby databases for replication so that they can get up-to-date.
3. WAL is large; single update statement translates to multiple writes. Postgres supports write based replication rather than statement based replication


## Consequences of PostgreSQL design

### Write amplification

1. Write amplification especially related with SSDs leads to one logical write becomes a much larger, costlier update to the physical layer. 
2. SSDs don't do well with updates. They behave well with inserts and create new pages. Goal of a SSD is to have a page and flush it. To update, you invalidate an existing page, take it, copy it, change it and then write it. So there is little more work involved while updating. That's why LevelDB, RocksDB came to be built to take advantage of SSDs. They built something called as Log Structured Merge Tree. 

### Replication 
1. Write amplification problem also translates to a replication amplification problem.
2. Within a single data center, replication amplification may not be a problem. Between data centers, issues 
3. All problems happens due to a lot of indexes. 

### Data corruption
When queried by a primary key on a table it used to fetch two records instead of one. One of these was the old record for the primary key and the second was the new updated one.


### B-Tree Balancing
1. B-Tree structure needs rebalancing due to various inserts

### Replica MVCC
1. Replicas apply the on-disk representation of WALs on a database. This copy is identical to the master at any point in time. Statement based replication allows replaying the SQL statements