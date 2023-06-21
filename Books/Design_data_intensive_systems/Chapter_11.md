# Stream Procesing

## Introduction
1. In batch processing, the input is bounded (known, finite size) so the batch process knows when it has to finish reading the input. In reality a lot of data is unbounded because it arrives gradually over time: users produce data today and yesterday, they will even produce tomorrow. So batch processes must chunkize data to process data by the hour/day so last day/hour's data is processed right now.
2. This is too slow. Instead of this, we simply process each event as it happens. And this is the idea behind stream processing.
3. A stream refers to data incrementally availaible over time. For example: stdin/stdout in Unix, lazy lists in programming languages, filesystem APIs in Java, TCP connections delivering audio/video

## Transmitting event streams
1. Batch processing has its input and output as files. 
2. When the input is file, the first step is to parse it into a sequence of records. Stream processing refers the record as an event: A small self-contained, immutable object containing the details of something that happened at some point in time according to a time-of-day clock. Each line of a web server access log is an event.
4. In streaming, an event is produced once by a single producer and then consumed/processed by multiple consumers. You can encode an event as a string/JSON that allows you to append the event to a file, store an event, insert it into an relational table. Furthermore, related events are grouped together in a topic or a stream.
5. As we move towards continous processing with low delays, polling a database or file (where continously the producer dumps events) for new events to be processed becomes expensive if the database is not designed for this usage. It's better for consumers to be notified when new events occur. Specialized tools have been developed for these called Messsaging systems

### Messaging systems
1. A producer sends a message containing the event which is then pushed to the consumers. 
2. A simple way to implement a messaging system is to connect the producer and consumer directly like Unix pipe or TCP connection. Whereas a messaging system allows multiple producer nodes to send messages to the same topic and allows multiple consumer nodes to receive messages in the same topic.
3. With this publish/subscribe model, different systems take a wide range of approaches. To differenciate between them, we have the below questions
    - What happens if the producers produce faster than the consumers that process them?
        - We have three options: the system can drop the messages, buffer messages in a queue or apply back-pressure or flow control to block the producer from sending more. TCP/Unix pipes use a back-pressure. They have a fixed sized buffer and if it becomes full, the sender is blocked until the recipient takes data out of the buffer.
        - If messages are queued, what happens if the queue no longer fits in memory. Does the messaging system write the queue to disk? How does disk access affect the messaging system performace?
    - What happens if the nodes crash or temporarily go offline?
        - Durability requires a combination of writing to disk and/or replication which has a cost. If you can afford to lose messages, you can probably get higher throughput and lower latency on same hardware.
    - Whether messages can be lost depends on the application. For analytics with periodically delivered sensor readings and metrics its fine if we loose a few messages. For event-based systems, its more important that they are delivered reliably. 
    - Batch processing have a strong reliability guarantee. All failed tasks are automatically retried, partial output from failed tasks is automatically discarded. The output is same as if no failures occured.
4. Direct messaging from producers to consumers:
    - A number of systems use direct communication between producers and consumers instead of a broker/intermediary server
        - UDP multicast used in financial industry for streams such as stock feeds. Since UDP is unreliable, producer itself should remember packets it has already sent, so it can retransmit them on demand.
        - Brokerless messaging libraries like ZeroMQ use a similar approach implementing publish/subscribe over TCP or IP multicast
        - StatsD and Brubeck use unreliable TCP messaging for collecting metrics from all over machines on the network and monitoring them. Using UDP makes the metrics at best approximate.
        - Webhooks pattern allows a callback URL of one service which is registered in another service to be triggered (request be made to the URL) when an event occurs. 
    - These systems require application to be aware of the possiblity of message loss. They assume that producers and consumers are constantly online. If the consumers go offline for some reason, the messages may be lost (although producers may later on retransmit them)
5. Message broker:
    - A message broker is a kind of database optimised for handling message streams. Producers write messages to broker and consumers receive them by reading from the broker. The question of durability now moves to the broker.
    - Some message brokers write messages to disk whereas some keep them in memory.
    - Depending on configuration, they allow unbounded queuing as opposed to dropping messages or back-pressure.
    - Consumers are usually asychronous: when a producer sends a message, it waits for the broker to confirm it has buffered the message and does'nt wait for the message to be processed by the consumers.
    - Delivery to consumer happens sometime later depending on the queue backlog
6. Message brokers Vs database: Message brokers can also participate in 2-phase commits. There are still important differences between message broker and databases
    - Databases keep data until its expicitely deleted. Message brokers mostly delete data once its successfully delivered to consumers. Message brokers are not for long term data storage.
    - Most brokers therefore have a small working set and if the consumers are slow and each message takes a longer time to process, overall throughput may degrade.
    - Databases support secondary indexes and offer various ways of searching. Message brokers offer a way of subscribing to multiple topics at a time.
    - Message brokers don't support arbitrary querying but notify clients when new messages are availaible. Database query results are based on point-in-time snapshot of the data.
    - This is the traditional view of message brokers standardized in AQMP/JMS and implemented in RabbitMQ, ActiveMQ and so on.
7. Multiple consumers: When multiple consumers need to consume data from same topic, 2 main pattern of messaging are used
    - Each message is delivered to one of the consumers so consumers can share the work of processing to parallelize it.
    - Each message is delivered to all the consmers. Fan-out allows several independent consumers to tune-in to the same broadcast of messages without affecting each other.
    - These patterns can be combined by grouping consumers such that within each consumer group receives a different types of message but within each group each consumer receives a different message.
8. Acknowledgments and redelivery:
    - In order to ensure that message is not lost when the consumer fails or crashes, message brokers use acknowledgements. A client must explicitely tell the broker that it has finished processing the message so broker can remove it from the queue.
    - If it does'nt receive an ack from the consumer, it resends the message to another consumer. It may be the case that the message was processed but the acknowledgement got lost. This case requires an atomic commit protocol.
    - When combined with load balancing, redelivery can result in out-of-order processing since consumers generally process messages in the order produced by producers. To avoid this problem, we can have a separate queue per consumer. It's usually not a problem if messages are completely independent of each other.
9. Partitioned logs:
    - Message brokers that durably write messages to disk quickly delete them again after they've been delivered to consumers, because they were built with a transient mindset. Databases/file systems store data unless explicitely deleted. 
    - If you add a new consumer to the messaging system, it typically starts consuming messages after it was registered. Any prior messages are gone. With a database, a client can read any data back in time as long as its no longer deleted.
    - Log-based message brokers are a hybrid approach with low latency notifications of messaging and durable storage of databases.
    - Using logs for message storage:
        - A log is simply an append-only sequence of records on disk. 
        - It can be used to implement a message broker. A producer produces messages by appending to end of log and consumer receives them by reading the log sequentially.
        - If a consumer reaches the end of the log, it waits for a notification that new message has bene appended.
        - To scale to higher throughput, a log can be partitioned. Different partitions can be hosted on different machines, making each partition a separate log that can be read and written independently. A topic can then be defined as a group of partitions carrying messages of the same type.
        - Within each partition, a broker assigns a monotonically increasing sequence or offset to every message. Since a partition is append-only, the messages in a partition are all ordered. No ordering guarantees across partitions. 
        - Apache Kafka, Amazon Kinesis, Twitter's DistributedLog are log-based message brokers working like this.
        - These message brokers are able to achieve throughput of millions of messages per second by partitioning accross multiple machines and fault tolerance by replicating messages.
    - Logs compared with traditional messaging:
        - Log-based approach trivially supports fan-out messaging. Multiple consumers can independently read the log without affecting each other.
        - The broker can assign nodes to partitions to achieve load balancing instead of across a group of consumers.
        - Typically, a consumer reads the messages in a log partition sequentially in a single-threaded manner. This load-balancing approach has some downsides:
            - The number of nodes consuming a topic can be utmost equal to the number of log partitions, because the messages within same partition are delivered to the same node.
            - If a single message is slow to process, it holds up the processing of several messages in that partition (head-of-line blocking).
        - In situations where messages are expensive to process and you want to achieve parallelism on a message-by-message basis and where ordering is not important, AQMP/JMS style messaging is preferable. 
        - In situations where each message is fast to process, message ordering is important, the log based approach works well.
    - Consumer offsets:
        - Consuming a partition sequentially means all messages with an offset less than a consumer's current offset have already been processed, and all messages with a greater offset haven't been seen.
        - The broker only needs to record the consumer offset. This reduced book-keeping, pipelined approach is what has increased the throughput of log-based systems.
        - The consumer offset is similar to the log sequence number of single-leader database replication. The follower can resume replication at the log sequence number after it came back up after a crash.
        - If a consumer node fails, another node in the consumer group is assigned the failed consumer's partitions and it starts consuming messages at the last recorded offset.
        - If a consumer had processed subsequent messages but has not recorded their offset, those messages will be processed a second-time after restart.
    - Disk space usage:
        - To reclaim disk space, the log is actually divided into segments and from time to time, old segments are deleted or moved to archive storage.
        - If a slow consumer cannot keep up with the rate of messages, it falls far behind so that its offset points to a deleted segment. Some of the messages can get missed. 
        - A typical large hard-disk has capacity of 6 TB. Sequential write has a throughput of 150 MB / s. It takes about 11 hours to fill the drive. The disk can store 11 hours worth of messages post which it starts overwriting old messages. 
        - Throughput of a log remains more or less constant since every message is written to disk. This contrasts it with purely messaging systems keeping messages in memory by default and write them to disk if the queue grows larger. Throughput depends on the amount of history retained. 
    - When consumers cannot keep up with producers:
        - If a consumer falls so behind that the messages it requires are older that what is retained on disk, it will not be able to read those messages. The broker drops old messages that go further back than the size of the buffer can accomodate.
        - Can monitor how far a consumer is behind the head of the log and raise an alert if it falls behind significantly. 
        - Even if a consumer does fall behind and starts missing messages, it affects just that consumer and not the other consumers. 
        - This is a big operational advantage, you can experimentally consume production log for debugging/testing without worry to disrupt production services.
    - Replaying old messages:
        - With AQMP/JMS style message brokers acknowledging messages causes them to be deleted from the broker. 
        - Consuming from a log-based message broker is more like reading from a file, just a read-only operation.
        - The only side-effect of processing is that the consumer offset moves forward. The consumer offset is under consumer's control so it can be easily manipulated if necessary. So we can process that last day's worth of messages. 
        - This makes log-based messaging more-or-less similar to batch processes where derived data is separated from input data through a repeatable transformation process. It allows for easy experimentation and easier recovery.
        
## Databases and Streams
1. Log-based message brokers have been successful in applying ideas from databases to messaging. We could also go in reverse i.e. apply ideas from message brokers/streams to databases.
2. Event is a record of something happening. The fact that something was written to a database means the connection between databases and streams runs deeper than just physical storage of logs on disk.
3. Replication log consists of a stream of write events produced by the leader as it processes transactions. Followers apply those events in the same order to their copy of database and end up with an accurate copy of same data.
4. State machine replication states that if every event represents a write to the database and every replica processes the same events in same order then the replicas will end up in the same final state.

### Keeping Systems in Sync 
1. No single system satisfies all application needs for data storage, querying and processing. Applications usually need to combine several different technologies to satisfy their querying requirements.
    - OLTP database to server user requests
    - Caching to speed up common requests
    - Full-text index to handle search
    - OLAP Data warehouse for analytics.
2. If data is updated in cache, it also needs to be updated in database, search index and data warehouse. Analytical systems handle it using ETL batch processes viz. periodically dumping data and transforming it and bulk loading in a data warehouse. Data in multiple storage systems needs to be in sync.
3. We can do periodic full database dumps to sync data to multiple systems. One more alternative is dual writes in which application code explicitely writes to each of the systems when data changes. 
4. Problems with dual writes 
    - Dual writes are prone to race conditions which may lead to permanent inconsistencies between data systems. Unless there is a concurrency detection mechanism (version vectors), you may not even notice that concurrent writes occurred.
    - One more problem with dual writes is one of writes may fail while other succeeds. Ensuring that they either both succeed or fail is a case of atomic commit problem.
5. For single leader replication, the leader determines the order of writes so that state machine replicaton approach works among the replicas of the database.
6. Can we make the database as the single leader and all derived data systems such as cache/search indexes a follower of it?

### Change data capture
1. Replication logs of most databases have been considered to be an internal implementation detail of the database. Clients query data through its data model, queries and don't parse the replication log. It was therefore difficult to take the replication log and replicate it to a different storage technology such as search index.
2. Change data capture is the process of observing all mutations to a database and extracting them in a form such that they can be replicated to other systems. If the log of changes is applied in the same order, the data in the search index matches the data in database. The search index and any derived data systems can just be made consumers of the change stream.
3. The data in the search index and data in the data warehouse are just another view onto the data in the system of record.
4. A log-based message broker transports th change events from source database to derived systems since it preserves the ordering of messages.
5. Parsing the replication log is more robust than having triggers which are fragile. 
6. Examples: Databus(Linkedin), Wormhole(Facebook), KafkaConnect
7. Change data captures is usually asynchronous. The system of record does not wait for the change to be applied to the consumers before commiting it. Thus a slow consumer does not affect system of record too much. But all issues of replication lag apply.
8. Initial Snapshot:
    - If you've made the log of all changes ever made to a database, you can reconstruct the entire state of the database by replaying the log. However, it will take a lot of disk space and replaying it will take too long.
    - If you don't have entire log history, you need to start with a consistent snapshot. 
    - The snapshot must correspond to a known position or offset in the changelog, so that you know at which point to start applying changes after the snapshot has been processed. 
    - Log compaction: 
        - In log compaction, the storage engine periodically looks for log records with same key and throws away any duplicates and keeps most recent update for each key. This compaction/merging is a background process.
        - An update with a special null value (tombstone) indicates that a key was deleted. It's removed during log compaction. 
        - As long as a key is not overwritten or deleted it stays in the database. The disk space required for such a compacted log depends on current contents of the database, not the number of writes that have occurred to it. 
        - Same idea works in the case of log-based message brokers and change data capture.
        - If the CDC system is set up such that every change has a primary key and every update for a key replaces the previous value, it's sufficient to keep most recent write for a key.
        - So if you are using the CDC system to build a derived data system such as a search index, you can start a new consumer from offset 0 of the compacted topic, sequentially scanning all messages in the log. The log is guaranteed to contain the latest value for every key in the database.
        - Log compaction is a feature of Kafka and it allows the use of the message broker for durable storage and not just for transient messaging.
    - API Suppport for change streams:
        - Databases are beginning to support change streams as a first class interface rather than typical retrofitted way. For example: RethinkDB allows queries to subscribe to notifications when query results change.
        - Database represents an output stream in the relational data model as a table into which transactions can insert tuples but can't be queried.
        - The stream consists of the log of tuples that commited transactions have written to this table in the order in which they were commited.
        - Kafka Connect is an effort to integrate change data capture tools for a wide range of database systems. Once the stream of change events is in Kafka, it can be used to update derived systems such as cache/search indexes.

## Event Sourcing 
1. Event sourcing originated in the Domain-driven design community. 
2. Event sourcing involves storing all changes to the application state as a log of change events. 
3. However, it applies the idea at a different level of abstraction than CDC
    - For database, the application writing to the database need not be aware that CDC is happening. The log of changes is extracted at a low level (parse the replication log), which ensures that the order of writes matches the order in which they were actually written.
    - In the event sourcing, the application logic is explicitly build from immutable events that are written to the event log. Event store is append-only and updates and deletes are discouraged. Events reflect things that happened at the application level, rather than low-level state changes.
4. Event sourcing from an application point of view is meaningful to record user actions as immutable events rather than recording the effect of those on a mutable database. 
5. Event sourcing makes it easy to evolve applications overtime, helps in debugging making it clear why something happenend.
6. Specialized databases like Event Store have been developed for Event sourcing. But the approach is independent of any database. 
7. Deriving current state from event log:
    - Event log by itself is not useful, users tend to see the current state of modifications
    - Applications using event sourcing need to take log of all events (representing data written to the system) and transform it into application state that is suitable for showing to user.
    - Like CDC, replaying event log gives you to reconstruct the current state of the system. Log compaction is a bit different from CDC
        - In CDC, Log compaction can discard previous events for a same key
        - Event sourcing models events at a higher level: An event typically expresses the intent of user action and not mechanism of status update. Later events don't override prior events and we need the full history of change log to reconstruct the final state. 
    - Applications using event sourcing have some mechanism for storing snapshots of the current state that is derived from the log of events, so they don't need to repeated process full log from 0. 
    - This is only a performance optimization to speed reads and recover from crashes. The basic intention is that the system is able to store all raw events and reprocess the log whenever required.
8. Command and Events:
    - Event sourcing distinguishes between events and commands. When a request comes from a user, it is initially a command. A command may fail for example due to some data integrity issue. The application must first validate the command if it can execute
    - If the validation is successfull and the command is acceptable, it gets converted into an event which is durable and immutable.
    - At the point when the event is generated, it becomes a fact. Even if the user decides later to change/cancel, the fact remains true. And the cancellation or change is a separate event that is added later.
    - A consumer of the event stream is not allowed to reject an event. Any validation of the command must happen synchronously before it becomes an event.
    - Alternatively, a user request to reserve a seat could be split into two events: a tentative reservation and then a separate confirmation event once the reservation is validated.

## State, Streams & Immutability
1. The principle of immutability is what makes event sourcing & change data capture powerful.
2. Databases store the current state of the application and this representation is optimzed for read. The database supports updating, deleting and insertion of data since the application state may change.
3. No matter how the state changes, there is always a sequence of events that caused those changes. The key idea is that mutable state and append-only log do not contradict each other, they are two sides of the same coin. The log of changes, the changelog represents the evolution of state over time.
4. The application state is what you get when you integrate an event stream over time and a change stream is what you get when you differencitate the state by time. 
5. If you consider the log of events to be your system of record, and any mutable state as being derived from it, it becomes easier to reason about the flow of data through a system.
6. In the words of Pat Helland ""
    - Transaction log records all changes made to database. High-speed appends are the only way to change the log. 
    - Contents of the database hold a caching of the latest record values in the logs
    - The database is a cache of the subset of the log. This cached subset includes the latest value of each record and index value from the log. Log compaction is what helps retain the latest version of each record. 
7. Advantages of immutable events
    - Accountants have been using immutability since a very long time. A transaction is recorded in an append-only ledger and essentially a log of events describing money, goods that changed hands. Profit/Loss are derived from the transactions in the ledger. Should a mistake be made, the accountant adds a transaction correcting the mistake.
    - Such auditability in addition to financial systems is important for other systems as well. With an append-only log of events, it is easier to diagnose what happened and recover from the problem.
    - Immutable events may also capture more information that just current state. If a customer adds a product to a cart and later on removes it, these get recorded as two different events. It may be important for analytics that the customer was considering initially to buy this product and then decided against it. This info is recorded in an event log but may get lost in a database that just deletes items from the state.
8. Deriving several views from the same event log:
    - It would make sense for several storage and indexing systems to take their input from a distributed log and build several differnt views. For example:
        - Analytic database Druid ingest directly from Kafka
        - Kafka connect exports data to several different databases and indexes
        - Pistachio is a distributed key-value store using Kafka as commit log.
    - If you want to introduce a new feature presenting existing data in a new way, you can use the event log to produce a new read-optimized view and run it alongside existing systems.
    - Storing data is quite straight-forward if you don't have to worry about how it would be queried and accessed, many complexities of schema design arise from wanting to support certain query access patterns.
    - You will get a lot of benefit from differenciating the form in which data is written from the form it is read by allowing several different read views. This is called as Command Query Responsibility Segregation (CQRS).
    - Traditional approach to database design is based on the fact that data must be written in the same form that it is queried. Normalization/Denormalization largely becomes irrelevant if we can translate data from a write-optimized event log to read optimized application state. 
    - Twitter's home timeline which is a cache of recently written tweets by people a particular user follows is again an example of read-optimized state. Home timelines are highly denormalized since the tweets are duplicated in the timelines of all people following you. Fan-out service keeps the complicated state in sync with new tweets and new following relationships which keeps the duplication manageable.
9. Concurrent writes:
    - Consumers of the event log are asynchronous and there is a possibility that a user may write to the log, then read from a log-derived view and find their write not reflected in the read view (Read-your-own-writes guarantee)
    - We can update the read view first and synchronously append an event to the same log. You need to keep the event log and read view in same storage system for this or need a distributed transaction across different systems.
    - Much of the need for multi-object transactions stems from a single user requiring data to be changed at several different places. Event sourcing can design an event such that it is a self-contained description of a user action. So we append the event atomically to the event log.
    - Provided the event log and application state are partitioned in the same way, then a straight-forward single-threaded log consumer needs no concurrency control for writes. It processes single event at a time. 
    - The log defines a serial order of events in a partition and removes determinism of concurrency.
10. Limitations of immutability:
    - Various databases internally use immutable data structures or multi-version data to support point-in-time snapshots. Version control systems such as Git rely on immutable data to preserve version histories of files.
    - Workloads with a high rate of updates and deletes on a small dataset may have a large immutable history and performance of compaction and garbage collection is crucial. Workloads adding data and rarely updating or deleting are easy to make immutable.
    - Privacy regulations such as GDPR may need data to be deleted. GDPR requires a users personal information be deleted and errorneous information be removed on demand. In such cases, adding an immutable event to a log is not suffice, you actually want to rewrite history and pretend that data was never written in the first place. Datomic calls this as excision.
    - Deletion is more a matter of making it harder to retrieve data than actually making it impossible to retrieve the data.

## Processing Streams:
1. We talked about where streams come from (user activity, sensors, DB writes) and how streams are transported (event logs, direct messaging, log based brokers)
2. How can we process streams? There are 3 options
    - Data in the events can be written to a database, cache, search index, storage system from where it can be queried by other clients. This is a good way of keeping a database in sync with changes happening in other parts of the application. 
    - Push the events to users in some way by sending email alerts or notifications or stream them to a real time dashboard where they are visualized.
    - You can process one or more input streams to produce one or more output streams. A piece of code processing streams in this way is called an operator or a job. Let's investigate this option further
3. The pattern of dataflow in stream processor is similar to Unix processes and MapReduce jobs: A stream processor takes in an input stream and writes its output to a different location in any append-only fashion.
4. Partioning & Parallelization of Streams is similar to MapReduce and basic mapping such as transforming and filtering records are similar and also work same.
5. A stream never ends as compared to batch jobs. Sort-Merge joins cannot be used cause they don't make sense with an unbounded data-set.
6. Fault-tolerance mechanisms change - it does'nt make sense to restart a stream job running for years and restart it from the beginning

## Uses of Stream processing:
1. It has long been used for monitoring and alerting purposes: Below kinds of applications require sophisticated pattern matching and correlations.
    - Fraud detection systems need to determine if the usage patterns of a credit card have unexpectedly changed and block card if it's stolen
    - Trading systems examine changes in a financial market and execute trades according to specified rules
    - Manufacturing systems need to monitor the status of machines in a factory and identify problem in case of a malfunction.
2. Complex Event Processing (CEP): 
    - CEP is an approach developed in the 90s, for analyzing event streams that allow searching for patterns in event streams.
    - CEP allows to specify rules to search for certain patterns of events in a stream.
    - CEP uses a high-level declarative query language to describe the patterns of events that should be detected. These queries get submitted to a processing engine that consumes the input streams internally maintaining a state machine that performs the matching. When a match is found, the system emits a complex event with the details of the event pattern detected
    - Usually a database stores data and treats queries as transient. The CEP engine reverses these roles: queries are stored long-term and events from input stream flow past them in search of a query matching an event pattern.
3. Stream analytics: 
    - The boundary between stream analytics and CEP is blurry.
    - Stream analytics is more oriented toward aggregation and statistical metrics over a large number of events (eg rolling average, rate of some event per time interval)
    - The time interval over which you aggregate is known as a Window.
    - Streaming analytics systems sometimes use probablistic algorithms such as Bloomfilters for set membership, HyperLogLog for cardinality estimation.
    - Probablistic algorithms produce approximate results but require significantly less memory in the stream processor than exact algorithms
    - Sometimes people believe that stream processing systems are lossy and inexact due to their use of stream processing algorithms but that is wrong. There is inherently nothing approximate about stream processing and probablistic algorithms are merely an optimization.
4. Maintaining materialized views:
    - A stream of changes to a database can be used to keep derived data systems such as caches, search indexes, data warehouses up-to-date with a source database. These can be regarded as specific cases of maintaining materialized views (deriving an alternate view on some dataset so it can be queried efficiently, updating the underlying data changes that view)
    - In analytics scnenario, it is not sufficient to consider events within a time window, building materialized view potentially requires all events over an arbitrary time period, apart from any obsolete events that may be discarded by log compaction.
    - Any stream processor can be used for materialized view maintenance, although the need to maintain events forever runs counter to the assumptions of some analytics-oriented frameworks.
5. Search on streams: 
    - There is sometimes a need to search for patterns consisting of multiple complex criteria such as full-text search queries. For example: Media monitoring services subscribe to news feeds from media outlets and search for any news mentioning companies, products or topics of interest. The percolator feature of Elasticsearch is an option for implementing this kind of stream search. 
    - Conventional search engines index documents and run queries over the index. Searching a stream stores queries and documents are run past by queries like in CEP case. To optimize the process, we can index queries and documents and narrow down the set of queries that may match.
6. Message passing and RPC
    - Message passing systems are an alternative to RPC. Mostly based on Actor mode. 
    - They are usually not categorized as Stream processors
    - Actor frameworks manage concurrency and distributed execution of communication modules. 
    - Actors communicate is often ephemeral and one-to-one, event logs are multi-subscriber and durable.
    - Actors communicate in arbitrary ways (cyclic request/response patterns) but stream processing is set in acyclic pipelines. 
    - Some crossover exists between RPC-like and stream processor systems. 
    - Apache Storm has distributed RPC feature where user queries can be farmed out to a set of nodes that process event streams. The queries can be interleaved with event streams and results can be aggregated and sent back.
    - Actor frameworks can also process streams but they do not guarantee message delivery in case of crashes so processing is not fault tolerant.

## Reasoning with Time
1. Stream processors needing to deal with time frequently use "Average over Last 5 minutes". These time windows should be clear but the notion is tricky.
2. If some kind of breakdown related by time needs to happen, a batch processor looks at a timestamp in the event. No point looking at the system clock of the maching running the batch process. The time at which the process is running has nothing to do with the time at which events actually occurred. 
3. Using the timestamps in the events allows the processing to be deterministic, running the same process again on the same input yields the same result.
4. Many stream processing frameworks use the same local system clock on the processing machine. This has the advantage of being simple and reasonable if the delay between event creation and event processing is short. It breaks down in case of significant processing lag. 
5. Event time vs processing time:
    - Many reasons why processing may get delayed viz. network fault, queuing, message broker contention, restart of the system consumer. 
    - Message delays can lead to unpredictable ordering of messages. Stream processing algorithms must be rewritten to accomodate such timing and ordering issues.
    - Confusing event time and processing time leads to bad data. For example: If a stream processor measures the rate of requests per second. If you redeploy it, it may shut down for a minute and process the backlog of events when it comes back up. Measuring the rate in terms of processing time makes it appear as it there is a sudden spike in events, even though it was a steady event stream.
6. Knowing when you're ready - Defining Windows:
    - We can never be sure if we have received all of the events for a particular window or some events are yet to come.
    - It could happen that some events were buffered on another machine somewhere. Such straggler events that arrive after the window has been declared complete should be handled. Two options
        - Drop the straggler event and track the number of dropped events as a metric.
        - Publish a correction, an updated value for the window with stragglers included.
    - Can also use a special message "From now on, there will be no more messages with a timestamp earlier than t".
    - If several different producers are generating events each with their own minimum timestamp, the consumer needs to keep track of each producer individually.
7. Whose clock you're using?
    - Assigning timestamps to events is difficult when events are buffered at several points in the system. A mobile app that sends usage metrics to a server may buffer them in case the internet connecton goes down and start sending at a later point in time when it appears. For the consumers of this stream, all events will appear as stragglers. 
    - The timestamp on the events should be the time at which the user interaction occurred, according to the mobile device's local clock. The time on a user-controlled device may not be trusted. 
    - The time at which the server received the event is more likely to be accurate since server is under your control.
    - We can log 3 timestamps:
        - ODT (Occurrence Device Time) = The time at which event occurred according to the device clock
        - DST (Device Sent Time) = the time at which the event was sent to the server according to the device clock
        - SRT (Server Receive Time) = The Time at which the server received the event according to the server clock.
    - abs(DST - SRT) gives the offset between the server clock and device clock. This offset can be applied to the event timestamp to estimate the true time when the event occurred. 
8. Types of Windows
    - Let's decide how to define the windows over time periods. Several types of windows in common use
    - Tumbling Window:
        - Fixed length and each event belongs to one window. 
        - Can implement 1-minute tumbling window by taking each event timestamp and rounding it down to the nearest minute to determine the window it belongs to.
        
    - Hopping Window:
        - Also has a fixed length but provides some overlap to provide smoothing
        - A 5 minute window with a hop-size of 1 minute would contain events between 10:03:00 and 10:07:59 and next window could cover events between 10:04:00 and 10:08:59
        - Can be implemented by first calculating 1 minute tumbling windows and then aggregating over several adjacent ones.

    - Sliding Window:
        - Stores events occuring within a fixed time interval of each other. 
        - Sliding window is implemented by keeping a buffer of events sorted by time and removing old events when they expire from the window.
    
    - Session Window:
        - Session window groups together all events for the same user that occur closely together in time. 
        - This window ends when the user is inactive for some time.

9. Stream Join
    - Batch jobs can join dataset by keys. Exactly similar requirements exist for Stream joins as well. 
    - New events can appear any time on a stream making stream joins challenging. We have 3 types of stream joins viz. stream-stream joins, stream-table joins and table-table joins
        - Stream-Stream join: Search feature on website and every time someone searches a query, there is a search event and every time someone clicks through there is a click event. To calculate click-through rate for each URL, we need to bring together the events for the search action and click action connected by same session ID. We need to choose a suitable time window for the join because the delay between click and search can be highly variable. For implementing this type of join, the stream processor needs to maintain state. All events occuring in the last one hour indexed by session ID added to the appropriate index. If there is a matching click event, we check the other search index to find the corresponding search event. 
        - Stream-Table join: If we have a user activity stream containing a user ID, the output is a stream of activity events in which the user ID has been enriched by the profile information about the user. This can be implemented by querying a remote database by the user ID for each event which might be slower. We can even import a snapshot of user profile data into the Stream processor. If the contents of the database change over time, we can have a change data capture stream which is subscribed to by the event processor. A Stream-table join is similar to a Stream-Stream join except that the table changelog stream uses a window that stretches back to the beginning of time with newer version records overwritting older ones.
        - Table-table join: In Twitter timeline example, for efficiency purposes we maintain a timeline cache of per user inbox. All timeline reads are single event lookups. We require to do the following for this
            - When a user u sends a new tweet, it is added to the timelines of every user following u.
            - When a user u deletes a tweet, it's removed from all it's followers
            - When u follows v, recent tweets of v are added to u's timeline
            - When u unfollows v, recent tweets of v are removed from u's timeline.
        Implementing this requires events for tweets and follow/unfollow relationships. The Stream processor effectively maintains a materialized view that joins two tables tweets and follows. 
    - Time-dependence of Joins:
        - All joins require the stream processor to maintain some state based on one join input and query that state on messages from another join input.
        - Order is important (it matters whether you first follow or unfollow).
        - If state changes over time, and you join with some state and what point in time do you use for the join? Whe selling things, you need to apply the right tax rate at the time of the sale which may be different from the current tax rate if you are procssing historical data.
        - If ordering of events across streams is undetermined, the join becomes non-deterministic, you cannot re-run the same job to get the same results on the same input, events on the input streams may be interleaved in a different way when you run the job again.
        - This is called as Slowly Changing Dimension (SCD) and is addressed by using a particular identifier for a particular version of the joining record. Every time the tax rate changes, it's given a new identifier and the invoice includes the identifier at the time of the sale. This makes the join deterministic but log compaction is not possible since all versions of record in table should be retained.

## Fault tolerance in Stream processing
1. For a batch processor fails, if a task in MapReduce fails, it can simply be started on another machine and the output of the failed task is discarded. The batch approach to fault tolerance ensures that the output of the batch job is same as if nothing had gone wrong. Each task writes it's output to a different file in HDFS and the output becomes visibile when a task completes successfully.
2. The visible effect in the output is as if records have been processed exactly once even though tasks are restarted multiple times. This is called as Exactly-Once Semantics.
3. For a stream, fault tolerance is subtle, because a Stream is endless and waiting until a stream finishes to make its output visibile is not an option.
4. Micro-batching and checkpointing: 
    - Break each stream into small blocks and treat each block as a miniature batch process. 
    - Batch size is around 1 second which is a result of a performance compromise. Smaller batches incur greater scheduling and coordination overhead while larger batches mean longer delay before the stream processor results become visible.
    - Apache Flink periodically generate rolling checkpoints of state and writes them to durable storage. On crash, a stream operation restarts from it's most recent checkpoint and discards any output from the last checkpoint till now.
    - Micro-batching/checkpointing provide exactly-once semantics within the confinement of the stream processor. As soon as the output leaves a stream processor say gets applied to a database or a search index or cache, the framework is unable to discard the output of a failed job. 
5. Atomic commit revisited.
    - For exactly once processing, we need to ensure that in the presence of faults, all processing of an event and it's side-effects like sending message to downstream operators, database writes, changes to operator state, acks of messages, moving the consumer offset forward, need to take place if and if the processing is successful.
    - Distributed transactions based on XA have problems. In more restricted environments, it's possible to to implement atomic commit. This has been done in Apache Kafka, Google Cloud Dataflow
    - These implementations don't provide transactions across hetergenous technologies but keep transactions internal by managing both state changes and messaging within the stream processing framework. The overhead of transaction protocol can be amortized by by processing several input messages within a single transaction.
6. Idempotence: 
    - Distributed transactions are a way of achieving the discarding of partial output of failed tasks. Another way is idempotence. 
    - Even if a transaction is not naturally idempotent, it can be made idempotent with an extra bit of meta-data. For example: Each Kafka message has a persistent, monotonically increasing offset. When writing a value to an external database, you can include the offset of the message that triggered the last write to the value. Thus we can check whether an update is applied and avoid performing the same update again.
    - Relying on idempotence implies several assumptions, restarting a failed task must replay the same messages in the same order, the processing must be deterministic and no other node may concurrently update the same value.
    - When failing from one processing node to another, fencing may be required to prevent interference from a node that is thought to be dead but still alive. 
7. Rebuilding state after a failure:
    - Any stream process that requires a state must ensure that the state can be recovered after a failure. 
    - We can keep the state in a remote datastore and replicate it although having a remote database for each individual query can be slow. We can also keep state local to to the stream processor and ensure that it's replicated periodically. 
    - Flink periodically captures the snapshots of operator state and writes them to durable storage such as HDFS. 
    - In some cases, you may not build the state since it can be built from the input stream. Example: State consisting of aggregations over a fairly short window, it may be fast enough to simply replay the input events corresponding to that window. 
    - If state is a local replica of a database maintained by change data capture, the database can be rebuilt from log compacted storage. 
    - All these trade-offs depend on performance characteristics of the underlying infrastructure: network latency might be low than disk access latency at times, network bandwidth may be comparable to disk bandwidth. 