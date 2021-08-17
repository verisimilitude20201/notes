Video: https://www.youtube.com/watch?v=sVCZo5B8ghE&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=3(30:00)

# Redis
Redis is an in-memory key-value NoSQL database

## In-Memory, Key-Value & NoSQL
1. In-memory key-value NoSQL store.
2. Relational DBs were schema-rigid and ACID
3. NoSQL has flexible schema, no need for an outage to add a field.
4. Key-value store the key is string the value is anything - blob, JSON, string, numeric entities.
5. Single-Threaded(except when the durability is enabled). It can scale because we can spin up as much instances and replication.

## Optional Durability
1. Back-ground thread perists in-memory data to disk
2. Journalling - append only log: Append the value of a key to a commit log. All inserts, updates, deletes are appended to this log.
3. Acknowledge - when? Is it when it appended in-memory or appended to log? Ack is given when it's updated in memory
4. Snapshotting: Periodically take a snapshot of in-memory structures and flush it to disk. Might loose some data
4. Both happen asynchronously in the background

## Transport Protocol
1. Uses TCP. All databases uses TCP.
2. Request/Response. Get that key. Update that key. 
3. Message format - Redis Serialization Protocol (RESP)

## PUB/SUB
1. Producers can subscribe to a channel and consumers can consume from it.
2. They use a Push model and not a long polling model. The number of clients using Redis will be limit.

## Replication/Clustering
1. Replication: One leader many followers model.
2. Clustering: Can shard data across multiple nodes on the basis of a shard key.
3. Combine clustering with replication. Each leader partition will have part of the data and each will replicate it's part to its followers.