Video: https://www.youtube.com/watch?v=Y6Ev8GIlbxc(7:59)

# Distributed Systems in One Lesson


## Distributed System definition and characteristics
1. Distributed System is a collection of multiple computers appearing as a single computer. For example: Cassandra Cluster, Amazon.com
2. Characteristics of a Distributed System
    - The computers have to operate concurrently.
    - The machines fail independently.
    - The computers don't share a global clock

3. Single-threaded versions of storage, computation and messaging things work on a grander scale. But once we start to distribute these things, things appear to get complicated.

## Distributed Storage
1. Single-Master Storage: Easy days, pleasant life. Database existing on a single server. 
2. If we have a slider which we can use to scale a system up and down. Certain things happen as we scale up our system and it gets busier and busier.
3. Web systems have a typical read traffic that's more than write traffic. RDBMS are good for reads. As we turn up the scale slider up, the single master database runs out and we need to scale reads since it cannot handle the load. 
4. We do read replication. Here master will accept writes and reads can be served by master as well as followers.
5. We broke consistency. For a single master database, we always read the thing we write. In case of a replicated database, if we write at the master, the write may not have propagate to all followers and if a client just hits that fallen-behind follower, stale data may be read. So this is eventual consistency.