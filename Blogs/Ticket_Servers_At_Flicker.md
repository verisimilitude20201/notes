# Ticket Servers: Distributed Unique Primary keys at Flickr


## Why
1. Ticket servers give globally unique integers to serve as primary keys in a distributed set-up at Flickr
2. Flicker uses sharding to evenly distribute data among multiple servers and spread the query load among them.
3. Sometimes, the data is migrated between databases. 
4. Need to guarantee the uniqueness of a primary key within a shard.
5. MySQL auto-increment primary keys can't guarantee uniqueness over multiple logical/physical databases.


## Alternatives seen?

### GUIDs
1. GUIDs are big and they index very badly in MySQL. 
2. Index size is a key consideration. If you can't keep your indexes in memory, you can't keep your databases fast. 
3. Don't give sequentiality.

### Consistent Hashing
1. Better suited for write cheap environments, MySQL is optimized for random reads.

### Centralized auto-increments

1. Just have a single table containing one ID column that's auto-incremented upon insert in a database and whenever one uploads a photo or comments, insert an ID.
2. It gets big too quickly, flicker has comments, photos, group postings, tags. 

### REPLACE INTO.

1. REPLACE INTO helps to atomically update a single row and get a new auto-incrmeented primary ID. 
2. If an old row has the same value for a Primary key or a Unique index, the old row is deleted before the new row is inserted. 

Let's say we have Tickets64 Table like

     CREATE TABLE Tickets64 (
            ID bigint(20) unsigned NOT NULL autoincrement PRIMARY key
            stub char(1) default ''
     )

To generate a new primary ID

REPLACE INTO Tickets64(stub) VALUES('a');
SELECT LAST_INSERT_ID();

At a time, this can have only one entry.

## SPOFs
1. To handle single point of failures, we can have two ticket servers. 
2. Replication would be problematic and locking would kill the performance
3. We can split the ID space into evens and odds such that one server generates only odd IDs and the other even IDs. 
4. Round-robin between both servers. 
5. Have more sequences for different types of content such as comments, photos, accounts, 

     )
