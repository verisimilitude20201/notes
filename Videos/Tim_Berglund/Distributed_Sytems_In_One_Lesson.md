Video: 
- https://www.youtube.com/watch?v=Y6Ev8GIlbxc
- https://learning.oreilly.com/videos/distributed-systems-in/9781491924914/9781491924914-video215265/
- Currently watching https://learning.oreilly.com/videos/distributed-systems-in/9781491924914/9781491924914-video215270/ (4:48)

# Distributed Systems in One Lesson


## Distributed System definition and characteristics
1. Distributed System is a collection of multiple computers appearing as a single computer. For example: Cassandra Cluster, Amazon.com
2. Characteristics of a Distributed System
    - The computers have to operate concurrently.
    - The machines fail independently.
    - The computers don't share a global clock. All actions are asynchronous
          - Amazon.com Yes
          - Cassandra Database Yes
          - My Computer: No, because there is a single global clock. All work is done with synchronization with the clock whose operation is driven by rising and falling cycles of the quartz oscillator 

3. Single-threaded versions of storage, computation and messaging things work on a grander scale. But once we start to distribute these things, things appear to get complicated.
4. As we scale, bad things happen to us. Distributed systems are hard and you can get by not having them. But business does'nt expand. There is some reason for us to be distributed

## Distributed Storage

### Single Master Storage
1. Easy days, pleasant life. 
2. Database existing on a single server. 
3. Consider a coffee shop where in they maintain the patron's favorite coffees on a single pad of paper. So they just add the name and the favorite coffee a very simple 2-column schema on paper.

### Read replication
1. Coffee shop grows we hire more baristas.
2. They've retained their speciality where in they note down the favorites coffee for a patron and every time ask for it.
3. With multiple baristas, we would need multiple pads of paper having the same data copied. If a person now needs to change his favorite coffee, he informs the head barista. Head barista writes it on the main paper pad and each barista modifies his own copy on the basis of that.
4. This is read replication - the simplest way to scale single server database.
5. Certain problems
   - Complexity: We need to maintain extra pads
   - Consistency: There is somewhat a delay encountered to get all barista's paper pads updated with the change of favorite drink. For a certain period of time, the paper pad copies of all baristas are inconsistent. We have to live with a eventual consistency and need to accept it. So first time when I order my coffee, it may not have changed with my recommended change. Second time, still no. Third time yes.
   - Only solves the problem of multiple reads rather than writes.
6. If we have a slider which we can use to scale a system up and down. Certain things happen as we scale up our system and it gets busier and busier.
7. Web systems have a typical read traffic that's more than write traffic. RDBMS are good for reads. As we turn up the scale slider up, the single master database runs out and we need to scale reads since it cannot handle the load
8. We do read replication. Here master will accept writes and reads can be served by master as well as followers.
9. We broke consistency. For a single master database, we always read the thing we write. In case of a replicated database, if we write at the master, the write may not have propagate to all followers and if a client just hits that fallen-behind follower, stale data may be read. So this is eventual consistency.
10. As we try to crank up the slider, the read followers will increase and the read performance will be good but all writes will be redirected on a single server which may become a single point of failure.
11. Read replication gives us scaling as well as redundancy.

### Sharding
1. Sharding is used when we run out of gas for read replication. The coffee shop has grown more and more and it's just not possible to provide updates to one single Barista.
2. We are going to split the key i.e. the person name. So persons begining with A-F - Head Barista 1, F-N Head Barista 2, N-Z Head Barista 3. Each one of these head barista play the same game of read replication but these are independent of each other.
3. Some complexity to map a key to a shard. In a coffee shop, we can may be have separate lines
4. We've been doing sharding in RDBMS databases since decades. But it's hard to do that right and applications need to maintain mappings of keys to shards.
5. NoSQL databases like MongoDB have sharding capability baked in and don't need any application side changes. 
6. Find a key and split up the database into multiple parts on the basis of it. Each part of it is a replicated independent database in itself. It's expensive to join across shards. 
7. As we scale up, our business grows. Technology challenges become harder and life gets harder
8. Problems:
  - More complexity: New lines in coffee shop. Separate translation layer in ORM to handle this.
  - Limited data model: To shard efficiently, there should be a single common key that we can shard on.
  - Limited data access patterns: For example: For a coffee shop, only efficient query that can be served is what is Tim's favorite coffee? At times, we would need to go to all shards (scatter), we would need to gather data. Have to pay attention to your data access patterns.
9. As we still crank that slider up, we continue to add more and more servers. The Database latency is going up, we have a higher transactional volume and reads are not happening properly. Addition of an index might improve things.
10. To make reads perform, you remove joins, you denormalize which is immoral (as we learnt in school). You end up in something that's not a relational database now but was initially.
11. Let's do things correctly right from the start and build a distributed database from start. Let's use Cassandra. Cassandra is pretty much upfront about features it does not support.

### Consistent Hashing
1. Build a Web scale coffee shop from grounds up, not to take a fundamentally non-scalable database and try to scale it by sharding.
2. Consistent Hashing is a family of approaches for scaling storages. It comes in different names - Amazon's Dynamo's paper, Distributed Hash tables and so on. 
3. Consistent Hashing: 
  - 8 Baristas (1000, 200, 3000, 4000, 5000, 6000, 7000, 8000) which are peers and are arranged in a circle. They are identified with a numeric token. 
  - There is a Web-scale coffee shop and tries to remember everyone's favorite coffee. Let's say we want to store "Americano" key into the cluster of 8 peers. We run it through a hash function and whatever numeric value we get, we try to store it on the node with the first numeric token that is greater than the key. 
  - While reading we apply the hash function to the key and fetch the key's data from that node.
  - Cassandra, Dynamo, Riak behave this way.
  - We can even store the same key's data across different nodes in the consistent hashing chain.
  
### Quorum - Tuneable Consistency model
1. We created a problem of consistency here. If I enter a coffee shop, input my name and get redirected to a Barista on the basis of the hash. What happens if the Barista is on a smoke break? If he does'nt have my updated favorite coffee? How do I ensure that the value that we read/write gets written or read properly amongst the nodes?
2. We get to decide what our distributed storage consistency levels are.
3. The formula is R + W > N
   - N is the number of replicas (i.e. in this case 3 baristas)
   - W is the number of replicas whom can acknowledge when I change my favorite coffee (Quorum)
   - R is the number of replicas that agree on a value when I read my favorite coffee. (Quorum)
4. In case if one is in a hurry, he would rush in, just go to one of his designated baristas, request his favorite coffee and be happy with whatever the barista gives, even if he has recently updated his favorite coffee. We want low latency reads, we accept eventual consistency, stale reads.
5. On the other hand, someone may accept reads with a high latency but would need accurate values. He would wait for the barista to sync up with the other barista to agree on a favorite coffee and come back.
6. Another problem is what if the two Baristas don't agree on a single coffee. The solution to that problem is very implementation-specific.
7. When would I use this?
- Scale. Not going to scale at all, don't use this. Use a single, RDBMS server.
- Transactional Data: Business transactions buying and selling of things, events happening in the system, data that changes a lot.
- Always On (High availaibility and redundancy)



## CAP Theorem
1. CAP theorem: Distributed database having 3 properties - Availability(Nodes are always available to serve requests), Consistency(When you read a value, that's the most recent version of the value written), Partition Tolerance(Nodes keep coming and going out form a network partition)
2. Consider that two peple are working in a coffee shop writing a play script on two separate pieces of paper with each syncing with each other what to write. The coffee shop closes and they keep doing it over phone at home. Ultimately the battery dies and these two decide to separately write the script instead of syncing with each other. This is a network partition. The spouse comes and asks one of them how's the script going? This is what the CAP theorem is. That guy could say No to answering saying that would be unfair to my partner (Give up availability for consistency in the case of a network partition). Or that guy would say "Well, I have this script but I would need to confirm once with my partner (Give up consistency for availability in the case of a network partition)


## Distributed Computation
1. Single processor and single thread are pretty easy. Multi-threading within the same processor is terrible. 
2. Multi-computer processing is even terrible. We have built several great tools to accomplish this. 

### Map Reduce
1. Consider we have a poem of 1800 words and we would need to do the word count. The answer is simple a wc command on the peom's text file.
2. MapReduce funnels all computation through 2 commands - one called Map and one called Reduce.
3. Map function would tokenize this into words and split it into key value pairs of word and the number 1. I've seen each word a single time.
4. Shuffling makes sure that similar key-value pairs are near by each other.
5. Similar key-value pairs are dumped on the same reducer where they are added up
6. MapReduce is good if you have a lot of data to process that's already stored on a distributed file-system. MapReduce is just a pattern for distributed computing and not a product. Hadoop is a product that implements this pattern. 

### Hadoop
1. Fundamentals of Hadoop
 - MapReduce APIs
 - MapReduce job management
 - Distributed filesystem
 - Enormous eco-system
2. Of these, the one long live piece of infrastructure that's still in active use is HDFS
3. Hadoop spawned an enormous ecosystem that became complicated. The underlying programming model was terrible so we needed this pleasant eco-system on top of it. Things like Hive which made it relatively pleasant to use Hadoop. Spark has taken up much of MapReduce's market share

### Spark
1. Scatter/Gather paradigm (similar to MapReduce). Instead of map, we have transform and instead of reduce, we have action.
2. Bunch of data sitting out on a bunch of computers and programs actually going to visit that data and locally run the computation. 
3. Spark creates an abstraction on top of our data (RDD/DataSets) that's a part of the API. Spark gave you an object that you can program and you can call methods on that object
4. More general programming model - transform and action.
5. No storage opinions it's storage agnostic - HDFS, S3, Parque and the list goes.

### Kafka
1. Approach to distributed computation in which everything is a stream. Data is inflight, you have not put data somewhere and send computational functions to it.
2. Stream processing framework. Streams and stream only and also tables sometimes
3. No cluster required

## Distributed Messaging
1. Means of loosely coupling subsystems. 
2. Release independent functionalities and versioned also indepedently with the means to communicate between them by messaging (Microservicese)
3. Messages are consumed by subscribers. Created by one or more producers organized into topics. Processed by brokers. Persistent over short term. Kafka defines it's own retention period for data. Consumers read the messages at each own rate
4. Microservices are super-good approach and people struggle to get these to talk. 
5. If the data is big that one computer, read/write is too for for one computer, topic becomes big due to a data retention period, guaranteed delivery even when a computer is down?

### Apache Kafka
1. Single server messaging systems can do wonderful things including ensuring messages are delivered exactly once and order. It cannot guarantee resiliency and scalability if the data load grows. As the scale grows, you might need to have trade-off amoungst one of these things.
2. Definitions
   - Message: An immutable array of bytes
   - Broker: One computer that the Kafka cluster is made up of.
   - Topic: Feed of messages
   - Consumer: Single threaded process subscribing to a topic.
   - Producer: A process that publishes messages to a topic.

3. Producer-Broker(Topic)-Consumer - A pipe based architecture
4. When a topic gets big, we split that across into partitions 0, 1 and 2 replicated on each computer.
5. As a consumer, we will just look at some part of that message and hash it, mod the number of partitions and write that to that partition. Within each partition things are ordered. Randomly and uniformly messages are assigned to a partition
6. System-wide / topic-wide partitioning is lost. At the producer level, you can be smart to decide what part of the message should be hashed. Messages from the same host, same user will go ordered into the same partition. 
7. Each partition act as independent computers acting indendently scaling the system
8. We now have a message bus. Whatever events you write to a database. Tomorrow, we can do compute events.

### Lambda architecture
1. Events are there, and two parallel systems are there to process.
2. One system processes the batch processing, long term storage and slow and complete.
3. Second system does stream processsing to compute faster summaries.
4. Forced the people to write the same code twice - batch processing and stream processing.

### Streaming with Events
1. Why not events be events and process them on the fly through a streaming processing framework?
2. There is a Kafka cluster
    - A set of Kafka message nodes forming the Kafka cluster
    - Various services are attached to it. Certain services are doing analytics. Couple of databases also are there. Might have ES cluster, Hadoop cluster all doing different things based on the event bus.
    - An event is propagated across all consumers. No coupling of any sort across consumers.
3. It's possible to go from a single server relational paradigm to a distributed cassandra paradigm. Have to change mind about certain things - consistency, data modelling, 