Video: https://www.youtube.com/watch?v=Y6Ev8GIlbxc(44:00)

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
6. As we try to crank up the slider, the read followers will increase and the read performance will be good but all writes will be redirected on a single server which may become a single point of failure.
7. Sharding: Find a key and split up the database into multiple parts on the basis of it. Each part of it is a replicated independent database in itself. It's expensive to join across shards. 
8. As we still crank that slider up, we continue to add more and more servers. The Database latency is going up, we have a higher transactional volume and reads are not happening properly. Addition of an index might improve things.
9. To make reads perform, you remove joins, you denormalize which is immoral (as we learnt in school). You end up in something that's not a relational database now but was initially.
10. Let's do things correctly right from the start and build a distributed database from start. Let's use Cassandra. Cassandra is pretty much upfront about features it does not support.
11. Consistent Hashing: 8 Computers which are peers and are arranged in a circle. They are identified with a numeric token. There is a Web-scale coffee shop and tries to remember everyone's favorite coffee. Let's say we want to store "Americano" key into the cluster of 8 peers. We run it through a hash function and whatever numeric value we get, we try to store it on the node with the first numeric token that is greater than the key. While reading we apply the hash function to the key and fetch the key's data from that node.
12. We never know when a node dies so we replicate the key on three nodes. 
13. We fix one thing and break other thing - Consistency. We have 3 copies of something that can change. We can have old copies of the key on certain nodes and new copies on certain nodes. How do we know who is speaking the truth? 
14. We can choose certain number of nodes. If the write succeeds on  those number of nodes, we can say the write operation is successful. If the 2 nodes send the same read value for a key, we can treat the read as successful. If R + W > N then it's a strongly consistent database. If not it's not, then it's an eventually consistent database. You can change consistency semantics per need basis


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
8. We now have a message bus.