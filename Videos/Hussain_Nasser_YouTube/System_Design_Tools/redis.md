Video: https://www.youtube.com/watch?v=sVCZo5B8ghE&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=3(15:00)

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

## Transport Protocol

## PUB/SUB

## Replication/Clustering