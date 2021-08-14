Video: 
- https://www.youtube.com/watch?v=Y6Ev8GIlbxc
- https://learning.oreilly.com/videos/distributed-systems-in/9781491924914/9781491924914-video215265/
- Currently watching https://learning.oreilly.com/videos/distributed-systems-in/9781491924914/9781491924914-video215277/ (5:00)

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
1. Keynote given by Eric Brewster in which he conjectured this for distributed databases. He was then working for Akaimai
2. Was proved by certain researchers at MIT later.
3. Defintions
  - Consistency: When I read from a distributed database, I am always going to get the last update I wrote.
  - Availaibility: When I ask I get a response. When I request my favorite coffee, I get it.
  - Partition Tolerance: Some nodes can occassionally be cut off from the rest of the nodes. May be GC pauses, network configuration issues. When that happens, the database can deal with it and still service requests. And when the issue resolves the database entangles all mess that had happened.
4. For a distributed system, P is not negotiable. Some part of the distributed system may independently. So for any failure we can just trade-off availaibility and consistency. For a single server database, life's good and u can have consistency and availaibility both.
5. Example Scenario from a coffee shop: Consider that two peple are working in a coffee shop writing a play script on two separate pieces of paper with each syncing with each other what to write. The coffee shop closes and they keep doing it over phone at home. Ultimately the battery dies and these two decide to separately write the script instead of syncing with each other. This is a network partition. The spouse comes and asks one of them how's the script going? This is what the CAP theorem is. That guy could say No to answering saying that would be unfair to my partner (Give up availability for consistency in the case of a network partition). Or that guy would say "Well, I have this script but I would need to confirm once with my partner (Give up consistency for availability in the case of a network partition)
6. This is just a set of constraints applied to a distributed system. A decade back one would'nt care about this - a single server database would be just fine.

## Distributed Transactions
1. ACID
   - Atomic: Completes completely or does not at all. Bundle of SQL statements that executes together. Say my entire family wants to update their favorite drinks
   - Consistent: When transaction completes, the database is in a valid state.
   - Isolated: If a transaction is currently trying to update the family's favorite coffee drink flavors, another transaction comes in to read ABC's favorite drink, that transaction would still read the old value / block till the first transaction completes. 
   - Durability: Database always remembers the things that we persist to it.
2. Steps to order coffee in an ACID way
   - Receive an order placed: I'd like a Cappuchino
   - Process the payment based on my credit card.
   - Order gets enqueued with the correlation identifier / may be order details too.
   - Barista takes a cup from that queue, sees the order, makes the coffee
   - Delivers the drink
3. Why split the work?
   - Parallelization: We can specialize people here. This will also avoid unnecessary burden on a single person to do all jobs. Having many people helps to service more customers.
   - Uneven workloads: Certain drinks are getting longer to make than making the payment. We could assign workers to different chunks of work according to how much work needs to be done.
4. What could go wrong: For ACID transactions, none of the below matters, the transaction is entirely undone
   - Payment could fail
   - Insufficient resources to make coffeee
   - Equipment failure
   - Worker failure even if I have paid for coffee
   - Consumer failure: The barista accepting my order had to go somewhere else.

5. Responses to failure
   - Write-off: Work is done, work can no longer go on to next phase so throw it away
   - Retry: The payment operation for example.
   - Compensating action: Customer has paid but you are out of coffee, the machines is broke, you give the customer her money back.
6. Questions
   - How can we design a coffee ship with atomic transactions? 
      o Have one barista per customer and he first prepares the coffee and then the customer can pay only pay if he has his coffee.
      o The reason we don't do this is because it'll affect throughput, it'll slow you down, you'll loose customers.
   - Why they give up on atomicity?
      o Higher throughput, more customers

7. It's possible using Zookeeper to create distributed transactions. Cassandra has light-weight transactions.

## Distributed Computation
1. Lots of data scattered across lots and lots of computers and we use distributed computation to proces them
2. Scatter-Gather
  - A General Paradigm for distributed computation
  - Scatter a computation on many nodes where processing happens on local data and gather the results of those computation. 
  - Data being local is the key.
  - Move programs where the data is.
  - Distributed system, computers are connected over a real, fast network. Network may not be as fast as the connection between the processor and the connection between the disk. Inside a node, common clock, high-speed bus protocols are always higher bandwidth connections.
3. MapReduce: Computational strategy that's it's implemented in Hadoop.
4. Spark: Hadoop's competitor. Both data model and programming interface are entirely different.
5. Storm: Oriented around event processing rather than processing data at rest. Real-time, small latency computation on incoming events rather than processing in batches. Use trade-offs to decide whether to use batch processing and event processing. Some people combine those forming lambda architectures to get best of both

### Map Reduce
1. Is a computational pattern. Mathematical abstraction that lends itself well to distributed computation by breaking the input data to pieces
2. All computations done in two functions: Map and Reduce. Take an input and give an output. Everytime it's given the same input, it gives the same output.
3. Keep data mostly local to where it is wherever it's possible.
4. The goal is to bring the computation near data is.
5. (Key, Value) -> Mapper -> [(K, V), (K, V)] (list of key-value pairs) -> Shuffle (Done by the framework we just provide the mapper and reducer) -> (K, [V1, V2, V2, V3]) (Find all common keys and all values from all the nodes) -> Reducer (Aggregation on all values) -> [(K, V), (K, V), (K, V)]
6. Word-count:
  - Consider we have a poem of 1800 words and we would need to do the word count.
  - Mapper tokenizes the values i.e a bunch of words into key value pairs. Key will be word and value is 1. In a very good way, we split up this problem of seeing a word and counting how many times it is seen amongst a bunch of servers.
  - Poem is only a few bytes, we can just execute a simple wc command against it to get the counts.
  - Shuffle is going to move around the values such that all same values are at one place. Copying will move data across nodes so we take a hit on computation to ensure data locality
  - Reducers can take those lists and can do the adding and give the final output. Even the reducers can be parallelized across nodes.
7. MapReduce is good if you have a lot of data to process that's already stored on a distributed file-system. MapReduce is just a pattern for distributed computing and not a product. Hadoop is a product that implements this pattern. 

### Hadoop
1. Fundamentals of Hadoop
 - MapReduce APIs: Interfaces for mappers and reducers, allows submiting functions to machines in a cluster and query for status
 - MapReduce job management: Hands of mappers to nodes, retries failed mappers on different replicas.
 - Distributed filesystem: Hadoop distributed file system (HDFS)
 - Enormous eco-system: Many open source projects mushroomed around it. Writing raw mappers and reducers is complex we need friendlier interface to build them. Some tools are HBase (Column family database on top of HDFS), Hive(SQL like database), Oozie (job tracker/task tracker), Zookeeper, Mahout (machine learning system atop Hadoop), Scoop (interfaces SQL database to Hadoop), Cascading (Simplifies writing MapReduce APIs and functions). The reasons of this rich eco-systems is the complexity of writing a low-level MapReduce function and the popularity of this framework.
2. HDFS: 
  - Stores files and directories.
  - Looks like a file-systems. Has a root. 
  - Single replicated master that does single meta-data management. The file paths, location of individual blocks of files all in-memory. That is called NameNode.
  - Limited. Hadoop is written in Java. Need to give a maximum Heap Size - 8 GB, 16 GB beyond that operations guys get a little bit nervous. There is a limit of 60 million objects for a 16 GB heap.
  - Files are stored in large, immutable, replicated blocks (4K or 16K current default is 128 MB) on many boxes. 
  - Immutability makes life so easier. We can only create a new block or delete it. We cannot change or modify it in any way.
3. Architecture of HDFS:
  - One NameNode to store metadata
  - One or more DataNodes that hold actual data on HDFS
  - Client app asks NameNode for a DataNode. NameNode says go to DataNode 3 and store there.
  - NameNode changes are low band-width and less frequent. Farm of client apps can each have their own data node. Aggregate I/O performance of the system is a function of the network architecture and we can get away with disk latency.
  - Assumes a large chunk of data gets stored at a time, it's not a transactional flow of purchase records. Gets there and stays there.
  - NameNode coordinates the replication of blocks. DataNodes on going down can contact the NameNode to get the status of what it missed to repair the status of the cluster.
4. How computation gets distributed?
  - Job Tracker is the main compute node. It may or may not be the same as the NameNode.
  - Data Nodes are the same.
  - Client app submits a job to the Job tracker. Job is a Map + Reduce. Most commonly a JAR file (YARN job, many other ways of submitting a job)
  - Job tracker sends the mapper code to a Task Tracker process running on each Data Node. Move compute to where the data is.
  - Mapper stores it's output key value pairs on the same data node where it runs.
  - Shuffler runs on the mapper output and the key value pairs will be moved about on the data nodes.
  - Reducer code then runs on the shuffled output of the map phase and ultimately writes the final output on the file system.
5. Rich Job management APIs.
6. Real World Hadoop
   - Top level Apache project
   - Cloudera, HortonWorks makes packaged versions of Hadoop.
   - Nobody writes Map/Reduce functions. Operate at a higher level of abstraction.
   - Hive (SQL like interface). Constraints the data structure, introduce schema
   - Integrate with BI front-ends and reporting tools, things can be used by business analysts
7. When to use Hadoop
   - Has to be a distributed systems problem, data volume is large
   - Data velocity (Writes are low) is low. Transactional workloads are not a hadoop usecase. HBase can make sense here. 
   - Latency SLAs are not aggresive. Running sorting in trillion size arrray is fast. But this is always Batch mode processing. Stream processing is not a Hadoop use-case

### Spark
1. Scatter/Gather paradigm (similar to MapReduce). Fairly different programming model and a flexible data model. Instead of map, we have transform and instead of reduce, we have action.
2. Bunch of data sitting out on a bunch of computers and programs actually going to visit that data and locally run the computation. 
3. Spark creates an abstraction on top of our data (Resilient Distributed DataSets - RDDs: Clump data into pieces that the framework is able to derive from) that's a part of the API. Hadoop has several data models due to it's complexity. Spark gave you an object that you can program and you can call methods on that object
4. More general programming model - transform and action. More freedom and richer APIs. Common utility functins
5. No storage opinions it's storage agnostic - HDFS, S3, Parque, Cassandra(Models data according to queries) and the list goes. Hadoop comes with HDFS.

### Spark Architecture
1. Spark client runs out of the cluster to create a job to submit to the cluster. It creates a Spark Context while submitting a job.
2. Spark Context involves a connection to a Spark node and the ability to perform the basic operations of the APIs, to create RDDs, run basic operations.
3. Spark context creates a job and hands that job off to a cluster manager in a Spark cluster (can be a Cassandra cluster running Spark on top of Cassandra, a powerful combination for analytical workload).
4. Job consists of a set of operations on clumps of data. These are transformation and actions peformed on RDDs.
5. Cluster manager has an idea of which nodes a given data is and it takes care of executing the jobs on that specific node. This is similar to Hadoop and we can draw many parallels.
6. Inside a worker node, we have an executor that's running in it's own JVM that listens to the cluster manager to receive tasks. These are serialized as JAR and deserialized as a task on the worker and executed.
7. Spark loves to cache and figuring out what (hot) data to cache is a key part of optimizing the computation and performance. 
8. Inside the executor each task processes data local to the node and in-memory.
9. RDD: 
  - Dataset that does'nt fit on one computer. 
  - It's split up among multiple computers. It can be created from an input source (Say Log source, text file, Cassandra table). 
  - Can also be the output from a function either by filtering, mapping, transforming another dataset. RDDs are immutable. 
  - Intermediary RDDs are garbage collected. All transformations are functional.
  - They have data-types
  - Ordered and sortable
  - Lazily evaluated. 
  - Partitioned: Need to have a way where this chunk of RDD goes on server 1, this goes on server 2. By default the partitions are random hash, but they are often pluggable.
  - Collection of things
10. The way it gets stored Spark does not have not have any opinion. 
11. We have a Cassandra database that stores people's favorite drink. We want the favorite drinks of people who ordered coffee on Friday after 3:00 PM. Cassandra does not natively answer this query. We can write a Spark job around that stores such data back into Cassandra so that we can read it quickly.
12. Example: RDD of credit card purchases, the keys are the credit card numbers and the values are a list of transaction IDs. We can hash these keys mod the number of partitions. We can determine the actual partition from  the bank code as well so as to group all transactions from the same bank in the same partition
13. Spark API
   - Transformation function (filter, flatMap, distinct, group by) transforms one RDD into another. 
   - Actions (count, collect, reduce) functions mostly aggregate functions

### Storm
1. Both Hadoop and Spark are similar. They are batch mode processing frameworks. There's a data at rest, computing goes to it and processes it. Spark also has a Streaming part to it.
2. Storm is designed for data that's in motion. Complete stream processing from first to last.

#### Storm Goals
1. Storm is a stream processing engine that in near-realtime processes events. 
2. Friendlier programming model than mere message passing. Messaging model may not be suited to complex event processing.
5. Storm guarantees at-least once processing semantics. This is a distributed system so things can fail, nodes can fail. Storm is willing to get the work done twice rather than not getting done at all due to distributed system failures.
6. Horizontally scalable and fault tolerant
7. Low-latency and fast answers on massive scale data. Storm came up from a project at Twitter firehose that used to do trending analysis and URL analysis.

#### Storm Programming Model
1. Basic definitions
   - Stream: Sequence of tuples (collections of key-value pairs) coming real fast
   - Spout is a source of streams. Points of integration. 
   - Bolt: Applies  functions to an incoming stream and produces one or more output streams
   - Topology: Graph of spouts and bolts. An actual job that runs indefinitely. Can be handed off to a cluster.
2. Assume the coffee shop has scaled nation-wide. We have two Spouts producing different event streams
   - Point-of-sale Spout: Generates the sale stream of coffee from different states and cities for the shop
   - Click Stream Spout: Generates a stream of activity of what people actually do on the coffee shop's web and mobile app.
   - These two streams are given as input to a bolt that transforms them and produces an output stream. 
   - This forms a topology and this is how Spouts, streams, bolts and graphs fit together.
   - This topology is handed off to the cluster.
3. Architectural components
   - Nimbus: Central node to which we give a topology to run. Just like NameNode of Hadoop.
   - That node coordinates with a Zookeeper cluster which helps with coordination, centralized naming. 
   - Supervisor nodes take information about what to do from the Zookeeper cluster and Nimbus master supervises the entire thing.
4. Terminologies
   - Nimbus: Central Coordinator of jobs
   - Supervisor: Node that performs processing
   - Task: A Bolt or spout of execution
   - Worker: JVM process where a topology executes. A worker executes multiple tasks where each task is a partitioned bolt or spout on the stream of data.
5. Stream Grouping: 
   - Assigns tuples to tasks through a consistent hashing mechanism
   - Shuffle Grouping: Random assignments. No logical reasoning or affinity
   - Fields Grouping: mod hash of subset of tuple fields.
   - All Grouping: Broadcast to every task
   - Getting partitioning right is the key to performance.

   
### Lambda architecture
0. Best of both worlds - Stream processing and batch processing
1. Events are there, and two parallel systems are there to process.
2. One system processes the batch processing, long term storage and slow and complete.
3. Second system does stream processsing to compute faster summaries.
4. Forced the people to write the same code twice - batch processing and stream processing.
5. Features
   - System input is an event stream.
   - Events are immutable. Once event happens, it cannot un-happen.
   - Batch and stream processing are functional. Pure functions. Transforms events into another form.
   - Data is dumped into a database or data warehouse.

#### Problems
1. Very high rate of events
2. Must remember and must store and interpret. Lots of Point Of Sale data from the large retail chain.
3. Wrong answer fast and right answer slow: We are willing to tolerance imprecise data at a ridiculously lower latency. May be the precise answer takes 15 minutes to compute but we also need a good enough answer.

#### Lamba components
1. Big Data Store: Stream of events that goes to a resting place (Cassandra DB, any other NoSQL DB). Long term storage, batch processing and slow and accurate.
2. Append only distributed queue: Same stream comes to an event processing framework. May give us temporary queuing, fundamentally stream processing. We never put the event at rest, we process it and moves ahead. It's fast and wrong. Willing to make compromises.
3. Same input stream -> two different pipelines.

#### Why call it lambda? 
1. Named after lambda calculus (Alonzo Church) because it does functional transformations on immutable data data.


### Synchronization - What problems arise when we attempt to synchronize async nodes.
1. "Now" in a distributed systems problematic. They don't have a shared clock.
2. Why synchronization may matter?
  - If we have 10 replicas, there can be 2 replicas that agree on a value and 2 replicas don't
  - We can estimate the time and let last write win. Store a timestamp with every record.It requires us to have an idea of what time it is. We can put a GPS in every server. We can install NTP in every node
  - Derive time logically - vector clocks. They don't tell us when, they just tell us in what order the writes happened.

### Synchronization - Network Time Protocol(NTP)
1. We put a very accurate clock (GPS receiver, atomic clock) on the Internet.
2. The network latency is variable depending on the conditions of the network. We can have a 10ns on the precision clock and 100ms latency on the network
3. NTP measures and accounts  for latency from the time it has measured.
4. It uses layers of time servers also called strata.
    - Stratum 0: Atomic or GPS reference clock having +-10ns accuracy
    - Stratum 1: server attached to the stratum 0 clock having +-5ms accuracy.The server is Internet accessible and it has some sort of device interface that makes it read from the stratum 0 clock. We get small numbers of micro-seconds of jitter on that interface.
    - Stratum 2: Syncs to stratum 1 having +- 10 ms. Could be in the same rack and has variable latency.Can also be in a different data center.
    - Stratum 3: syncs to stratum 2 (+-10ms)
    - Stratum 15: syncs to stratum 14 (+-10ms)
5. 15 Strata are fairly uncommon. Upto 3 is fine. We can fan-out servers at each strata
6. Protocol listens to port 123.
7. 64 Bit timestamp - 32 Bit seconds since the epoch and 32 bits fractional seconds. Only till Year 2036.
8. How NTP calculates time
     Delta = (t3 - t0) - (t2 - t1) where
     
     t3 = client receive timestamp
     t0 = client transmit timestamp thinking what it thinks the time was
     (t3 - t0) round trip time

     t2 = server transmit timestamp
     t1 = server receive timestamp

     Delta is the actual processing time to process the request.
     We have to do this repeatedly to get a pretty good idea of what time it is.
9. Good article - There is no Now. The very idea of now is illusive in distributed systems.

### Synchronization - Vector Clocks
1. NTP does a great job of telling us the time in +-10ms. Operationally trivial, ubiquitous and getting our nodes on the same page.
2. We need time because we need to know sequence. Vector clocks give us an idea about the sequence. Sequence of Modifications of a mutable value in a database. Vector clocks don't tell time
3. Assume that we are concurrently modifying one value. Multiple actors are contending to modify the value. Every actor has an ID. Every actor has a sequence numbers. Not a distributed counter, a single master counter.
4. Riak uses vector clocks to resolve conflicting writes
5. Understanding vector clocks by example of coffee shop 
- Four friends - Alice, Bob, Cathy and Dave and trying to figure out a day to meet for coffee. 
- Alice says Wednesday and attaches her ID and sequence number 1. Wednesday[(A, 1)]
- Alice and Cathy are off somewhere and Bob and Dave are together. May be a network partition
- Bob adds B: 1 and retains Alice's sequence number in the sequence Tuesday[(A, 1), (B, 1)]. Bob does'nt know Alice's sequence counter. He does'nt know if Alice used any other counter and those writes got lost. Bob says Tuesday. This is a descendant of Alice's write, a subsequent.
- Dave says "yes Tuesday" and adds his own ID and sequence number to the vector clock. Tuesday[(A, 1), (B, 1), (D, 1)]
- Network partition resolves and Cathy comes back. She says Thursday and appends her sequence number to the vector clock Thursday [(A, 1), (C, 1)]
- Dave receives this and detects a conflict. He thought it was Tuesday [(A, 1), (B, 1), (D, 1)] but now it's Thursday[(A, 1), (C, 1)]. There's no way the Tuesday write could descend from the Thursday write. We have to find all components of [(A, 1), (C, 1)] in [(A, 1), (B, 1), (D, 1)] to consider the write a descendant.
- Dave adds his own name to [(A, 1), (C, 1), (D, 2)]. We increment the sequence number by 1 for Dave since this is his second write. Furthermore, B's write will also be a part of this [(A, 1), (B, 1), (C, 1), (D, 2)]
- Application needs to resolve the conflict here
- Now Dave steps out and Alice asks Bob (Tuesday[(A, 1), (B, 1), (D, 1)]) and Cathy[Thursday(A, 1), (B, 1), (C, 1), (D, 1)] for their opinions. Bob did'nt get Dave's last message. Now Alice has a conflict
- Tuesday[(A, 1), (B, 1), (D, 1)] & Thursday[(A, 1), (B, 1), (C, 1), (D, 2)]. Everything in Tuesday's vector clock is in Thursday's vector clock Thursday[(A, 1), (B, 1), (C, 1), (D, 2)]. So Thursday it is.
6. Tradeoffs and Cons
  - Unless the sequence is broken, they cannot get the sequence wrong. Last Writes Win can, they have a little window to get the sequence wrong. This LWW con is not a big deal in case there is'nt precise timing requirements.
  - Push the complexity to the client i.e the application. Again this is of no significance if we don't need a precise time. For eg Trading systems. But for ordering systems and all that's okay.
7. Great article - Why vector clocks are easy.
8. Great article - Why cassandra does'nt needs vector clocks.

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



### Streaming with Events
1. Why not events be events and process them on the fly through a streaming processing framework?
2. There is a Kafka cluster
    - A set of Kafka message nodes forming the Kafka cluster
    - Various services are attached to it. Certain services are doing analytics. Couple of databases also are there. Might have ES cluster, Hadoop cluster all doing different things based on the event bus.
    - An event is propagated across all consumers. No coupling of any sort across consumers.
3. It's possible to go from a single server relational paradigm to a distributed cassandra paradigm. Have to change mind about certain things - consistency, data modelling, 