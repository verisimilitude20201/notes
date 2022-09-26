# Partioning

1. For large data sets or very high query throughput, replication is not sufficient. we need to break data into partitions also called shards (MongoDB, Elastic search) or region (HBase), tablet (Big Table), vnode(Cassandra), vBucket (Couchbase)
2. Partitions are defined in a way that each record, row or document belongs to one partition. Each partition is a small database of its own. We may have operations spanning partitions though and the database supports it.
3. Scalability is the main reason to partition. We may place different paritions on different nodes in a shared nothing architecture to distribute the data set and query load across many different nodes. 
4. Queries operating in a single partition, each node can execute the queries on its own. Query throughput can be scaled by adding more nodes. 
5. Complex queries can be parallelized across multiple nodes. 
6. Partitioned databases were pioneered since the early 1980s and more recently discovered by NoSQL. 
7. Some databases are designed for either analytic or transactional workloads although the basics of partioning remain the same. 

## Partioning and replication
1. Partioning and replication are usually combined. Copies of each partition are stored on multiple nodes for fault tolerance.
2. If a leader/follower replication model is assumed, each partition's leader is assigned to one node and it's followers are assigned to other nodes. Each node is a leader for some partitions and follower for some other partitions. 
3. The choice of partioning scheme is independent of the choice of replication scheme.

## Partioning a lot of key-value data
1. The goal of partioning should be to spread the data and query load evenly across nodes. 
2. If every node takes a fair share, 10 nodes would be able to take 10 times read/write throughput of a single node. A partition with disproportionately high load is called a hot spot. It happens when some partitions have data or queries than the others. 
3. We could take records and assign them to nodes randomly to prevent skews. But when trying to read an item, we'd have to query all nodes in parallel. 
4. There are better ways to do for example: for a paper encyclopedia you look an entry by it's title since they are alphabhetically sorted.

### Partioning by key ranges
1. Assign a continous range of keys to each partitions.
2. Knowing the keys between the ranges, it can be easily determined in which partition a given key is contained. 
3. If you also know which partition is assigned to which node, you can make the request to the node directly (going by the paper encyclopedia example: pick the book from the appropriate bookshelf.)
4. The range of keys may not be evenly spaced because the data distribution may not be even. To distribute the data evenly, the partition boundaries need to adapt to the data. 
5. The boundaries can be chosen manually or the database chooses them automatically. 
6. Within each partitions, the keys are sorted. This makes range scans easy. Keys can also be treated as concatenated index to fetch several related records in one query. For a sensor application, a key year-month-day-hour-minute-second would make queries by month, day, year range queries easy.
7. The downside is certain access patterns can lead to hot spots. In the sensors example, all the data gets written to the same partition for all sensors for the day and so that partition becomes overloaded. If we prefix the timestamp with the sensor name, the data for different sensors would go into different partitions.

### Partioning by hash of key.
1. Partioning by key ranges has a risk of skews and hot spots. Most distributed data stores use a hash function to determine a partition for the given key. 
2. Good hash function takes skewed data and makes it uniformly distributed. The hash function may not be cryptographically strong for hash purposes. For instance, Cassandra uses Murmur3, MongoDB uses MD5
3. Programming languages have simple hash functions (Java's Object.hashCode()) but they can't be used for partitioning. Same key may have different hashcodes in different processes. 
4. Once you have a suitable hash function, you assign each partition a range of hashes. Every key whose hash falls within that range will be stored in the partition


| po |  p1  |  p2  |  p3  |  p4 |  p5  |  p6 |  p7 |
0    16383          32767       49151              65535


"2010-01-01 17:04" -> 7372 (P0)

5. This technique is good at distributing keys fairly evenly among partitions. Partition boundaries can be evenly spread or they can be chosen randomly (consistent hashing)
6. Consistent hashing is a way of evenly distributing load across a internet-wide system of caches such as a CDN. It uses randomly chosen partition boundaries to avoid the need for central control. This approach does'nt work very well with databases. Let's call the database approach hash partitioning.
7. We loose the ability to do efficient range queries. Keys that were once adjacent are now scattered across all partitions so their sort order is lost. For MongoDB is hash sharding is enabled, any range query is sent to all the shards. Some databases don't support range queries on primary keys (which does hash partitioning)
8. Cassandra achieves a compromise between the two strategies. It uses a compound primary key. First part of the key is hashed to determine the partition. The other columns are used as a concatenated index for sorting the data in Cassandra's SSTables. 
9. This enables an elegant data model for one-to-many relationships. On social media, one user can post many updates. If we choose the primary key for updates as (user_id, update_timestamp), you can efficiently retrieve all updates made by a user within a particular time interval sorted by timestamp. 

### Skewed workloads and relieving hotspots
1. Hashing a key can help reduce hotspots but cannot avoid them entirely.
2. For extreme cases, when all reads and writes are for the same key, they may get redirected to the same partition. For example: On social media websites, when a celebrity user with millions of followers post something, it causes a storm of activity. 
3. It triggers huge volume of writes and reads to the same key. 
4. Most data systems are not able to automatically adjust for such highly skewed workload and so it's the application's responsibility to reduce the skew. 
5. A simple technique is if one key is known to be hot, we could add a random number to the beginning or end of the key. It would spread writes to 100 different keys allowing the distribution to multiple partitions.
6. Reads need to do some extra work, visit all partitions to take data from 100 keys and combine it. 
7. It only makes sense to append the random number for small number of hot keys. Need to track which keys are being split. 
8. Perhaps future databases will be able to automatically detect and compensate for skewed workloads, but for now, the application needs to take care of the trade-offs.

## Partioning and secondary indexes
1. If records are only accessed by their primary key, we could determine the partition from the primary key and use it to route write/read requests to the appropriate partition responsible for that key.
2. The situation complicates if secondary indexes are involved. A secondary index does'nt identify record uniquely but is a way of searching particular occurences of a value. 
3. Secondary indexes are the bread and butter of relational databases and they are common in document databases too. 
4. Many key-value stores (Hbase) avoid them because of their implementational complexities. Riak added them because they are so useful for data modelling. For some like Solr and Elastic search, they form the key components of the search. 
5. Secondary indexes don't map properly to partitions. 
6. Two main approaches to partitioning a database with secondary indexes: Document based partitioning and term based partioning.

### Document based partioning
1. Imagine you operate a website for sold cards. 
2. Each listing has a unique document ID. You partition the database by document ID (example: 0-400 Partition 0, 401-500 Partition 1 and so on)
3. To search by color, you want to allow the secondary index on color and by make. If the index is declared, the database can perform the indexing automatically during insert. For example: Whenever a red car is added, the database partition automatically adds it to the list of document Ids for the index entry color: red. 
4. Each partition is completely separate in maintaining it's own secondary indexes covering only documents within that partition. For write purpose, you only need to deal with the partition containing the document ID that you are writing. For this reason, a document partitioned index is also called as a local index (as in local to it's partition)
5. Read requests for filter by color or make need to be sent to all replicas in parallel and combine the results you get back. This approach is called scatter-gather and it makes read queries on secondary indexes quite expensive. 
6. It's widely used in: MongoDB, Elastic search, SolrCloud to name a few.
7. Most database vendors instruct that you use single partitions so that secondary index queries can be served from a single partition, but it is not always possible.

### Partioning secondary indexes by Term
1. Rather than each partition having it's own index, we can create a global index that covers data in all partitions. 
2. A global index must also be partitioned but differently from the primary key index.
3. Continuing the above example, Cars with red color appear under color: red in the index. But it's partitioned, so that cars with colors starting with a to r appear in Partition 0, s to z appear in partition 1. 
4. This is called term partioned index because the term we're looking for determines the partition. The "term" comes from full text indexes which is a kind of secondary index where the terms are all the words occuring in a document.
5. We can partition by term itself which is useful for range scans. We can also partition by hash of the term which gives more even distribution of load.
6. The advantage of a term-partitioned global index is it can make reads more efficient: rather than doing scatter/gather, a client makes request to the partition only containing the term it wants. 
7. The downside is a write to a term partitioned single document may affect many partitions if every term in the document is on a different partition.
8. In practise, updates to term partitioned index are async i.e. if you read the index after write, the change you made may not be reflected in index. AmazonDB indicates that it's term partitioned indexes may update within fraction of a second but at times may experience longer propogational delays due to network/infra faults. 
9. Riak's search feature, Oracle's data warehouse also use term partitioned indexes. 

## Rebalancing partitions
1. Over time for a database
    - Query throughput increases and so you need to add more CPUs.
    - The dataset increases so you want to add more disks and RAM to store it.
    - A machine fails and other machine(s) can take over the failed machine's responsibility.
2. These changes cause data and requests for data to be moved from one node to another. The process of moving load from one cluster to another is called rebalancing. 
3. Rebalancing is expected to fulfil some minimum requirements
    - After rebalancing, the load (data stored, read, write requests) should be shared faily between the nodes of the cluster. 
    - While rebalancing, the database should continue to accept read/write requests.
    - No more data than necessary should be moved between nodes to make rebalancing fast and minimize network and disk I/O load.

### Strategies for rebalancing

#### How not to do it: hash mod N
1. We discussed that it's best to divide the possible hashes into ranges and assign each range to a partition. 
2. We don't use the mod operator. hash(key) % N returns an integer between 0 and N - 1. The problem with mod N approach though it's an easy way is if the number of nodes N changes, most of the keys need to be moved from one node to another.
3. Frequent moves make rebalancing excessively expensive. We need to look for an approach that does'nt move data that frequently.

#### Fixed number of partitions
1. One more simpler solution is to create many more partitions than availaible and assign several partitions to each node. 
2. A database of 10 nodes can each have 100 partitions assigned to it. On adding a new node to this cluster, a new node can steal few partitions from every existing node until partitions are fairly distributed again. If a node is removed from a cluster, the same principe is applied in reverse.
3. Only partitions move between nodes. The number of partitions does not change, neither does the assignment of keys to partitions. The thing that changes is assignment of partitions to nodes. 
4. The change in assignment takes place after a large amount of data in the partition is transferred. The old assignment of partitions continue to take in read/write requests while the transfer is in progress. 
5. Even mismatched hardware can be accounted for: by assigning more number of partitions to nodes that are more powerful. You can force those nodes to take greater share of the load.
6. This is used in Riak, Voldemort, Elasticsearch, Couchbase.
7. The number of partitions is usually fixed and not changed afterward. Fixed number of partitions is operationally simpler. So many fixed partition databases do not implement partition splitting. Each partition also has a management overhead and so it's counter-productive to choose too high a number. 
8. Choosing right number of partitions is difficult if the data size is variable. If partitions are very large, rebalancing and recovery from failures becomes expensive. 
9. The best performance is achieved when the size of partitions is neither too big or too small. This is hard to achieve if the dataset size is variable and number of partitions are fixed.

#### Dynamic Partioning
1. With key-range partitioning a fixed partitions with fixed boundaries if configured incorrectly would be wrong. Reconfiguring the partition boundaries manually would be a tedious job.
2. Key-range partitioned databases (Rethink DB and HBase) therefore create partitions dynamically. 
3. In HBase if a partition grows to exceed a configured size, it is split into 2 partitions so that data splits 50:50. Conversely, if lots of data is deleted and if a partition shrinks below some threshold, it can be merged with an adjacent partition. Similar to what happens in a B-tree.
4. Each partition is assigned to one node and each node can handle multiple partitions. After a large partition has been split, one of its halves can be transferred to another node in order to balance the load. For HBase, the transfer of partition files happens through HDFS the underlying file system.
5. Here, the advantage is the number of partitions adapts to the data volume.
6. A caveat is that an empty database starts with a single partition since we don't have prior information on where to draw the partition boundaries. While the dataset is small, all writes go to a single partition and other nodes sit idle.
7. HBase and MongoDB allow an initial set of partitions to be configured on an empty database (pre-splitting).
8. Dynamic partitioning can be used for key-range as well as hash partioning. 

#### Partioning proportional to nodes
1. Dynamic partitioning has number of partitions proportional to the size of the data. Splitting and merging keep the size of each partition between some fixed maximum and minimum. 
2. In fixed partioning, the size of each partition is proportional to the size of the dataset. For both cases, the number of partitions is independent of the number of nodes.
3. Cassandra uses a third option. Keep the number of partitions proportional to the number of nodes. Have fixed number of partitions per node.
4. Size of each partition grows as the data size grows. The number of nodes remains unchanged. On increasing the number of nodes, the partitions become smaller. Larger data volume requires a larger number of nodes to store, this keeps size of each partition fairly stable. 
5. Randomization may be used to allocate a new node a given percentage of partitions. When averaged over a large number of partitions (Cassandra by default takes 256 per node), the new node takes a fair share of the load. A rebalancing algorithm is introduced in Cassandra 3.0 to avoid unfair splits. 
6. Picking partition boundaries randomly requires hash-based partitioning to be used. So boundaries can be picked from the range of hashes. 

### Operations: Automatic or Manual rebalance?
1. There are two approaches to rebalancing: 
    - Fully automatic when the system decides to move the partitions from one node to another
    - Fully manual assignment of partitions to nodes is configured by an admin and changes only on manual reconfiguration
2. Fully automated rebalancing though is convinient is unpredictable. Rebalancing involvves rerouting requests and moving a large amount of data fron one node to another. This can overload the network and hamper other request's performance if done incorrectly.
3. This in combination with automatic failure detection is dangerous. If a certain node is overloaded and slow in responding to requests, a rebalancing might happen if the automatic failure mechanism detects this. This put additional load on the overloaded node for the data move making the situation worse and causing a cascading failure.
4. Therefore it's much simpler to have a human intervention to rebalancing. It can prevent operational surprises.

## Request routing
1. The dataset is now partitioned across multiple machines. 
2. When a client makes a request, how does it know to which node to connect? With rebalancing the assigment of partitions to nodes changes. Someone needs to stay on top of those changes.
4. This is a general instance of a problem called Service Discovery. Any service reachable via the network has this problem especially if its aiming for high availaibility. 
5. Few approaches:
    - Allow the client to contact any node via load balancer. If that node coincidentally owns that partition for the key, it will return return the data. Otherwise, it forwards the request to the appropriate node, receives reply and pass it to the client.
    - Send requests to the routing tier which determines which node should handle that request and forward it accordingly. This routing tier is a partition-aware load balancer. 
    - Require clients be aware of the partitions and the assignment of partitions to nodes. 
6. All these approaches have the same challenge: How do we learn about the changes in assignment of partitions to nodes? It's important that all participants agree otherwise requests be sent to the wrong nodes. Consensus problem in distributed systems.
7. Most distributed systems rely on a separate coordination service such as zookeeper to keep track of cluster metadata. Each node registers to Zookeeper and it maintains the authoritative mapping of partitions to nodes. Routing tier or partitioning aware tier can subscribe to this information in Zookeeper. 
8. When a partition is changed, Zookeeper can notify the routing tier. Below diagram shows Linkedin's Expresso using Helix for cluster management. HBase, Solrcloud and Kafka use Zookeeper, MongoDB has similar architecture but it relies on its own config server implementation and mongos daemon as routing tier. Much of these are similar to what is shown below
            
                         Client                  

                     routing tier

            node_0     node_1      node_2 

    Key-range       Partition       Node          IP Address
    A - Abhijit        P0            N0           10.20.200.1
    B - Babhijit       P1            N1           10.20.200.2
    C - Cabhijit       P2            N2           10.20.200.3
    C - Cat            P2            N2           10.20.200.3
9. Cassandra and Riak use a gossip protocol among nodes to disseminate any changes in cluster state. Requests can be sent to any node and that node takes care of forwarding the request to other nodes. This adds complexity to node design but removes the need of an external coordination service.
10. For finding the IP addresses of nodes, since they are not as fast changing as the partitions, it's sufficient to use DNS.

## Massively parallel databases
1. Massively parallel databases used for analytics are much more sophisticated in the types of queries they support. 
2. A typical data warehouse contains several join, filtering, grouping and aggregation operations. 
3. MPP query optimizer breaks this into a number of stages and partitions which can be executed in parallel on different nodes of the database cluster. 
4. Queries scanning ovr a large parts of the dataset particularly benefit from parallel execution.