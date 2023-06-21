# The Future of Data Systems

## Data integration
1. The most appropriate choice of a software tool depends on the circumstances. 
2. In complex applications, data is also used in several ways. You have to inevitably cobble up different pieces of software to provide your application functionality.
3. Combining specialized tools by deriving data:
    - It's common to integrate an OLTP database with a full text search index to handle queries for arbitrary keywords. 
    - Sophisticated search facilities require specialist information retrieval tools. 
    - Conversely search indexes are not very suitable as a general system of record.
    - As different representations of the data increase, the integration problem becomes harder. You may need to keep copies of data in caches, denormalized versions of objects, pass the data through machine learning, classification systems, recommendation systems, send notifications based on changes to data.
    - The need for data integration become apparent if you zoom out and consider dataflows across an entire organization.
4. Reasoning about data-flows:
    - When copies of data need to be maintained in several storage systems, you need to be clear on inputs/outputs. Where data is written first, which representations are derived from which sources, how do you get all data in the right formats.
    - For example: Data might be written to a system of record first, capturing the changes to the database and applying them on the search index in the same order.
    - If it's possible to funnel all writes through a single system that determines an ordering of writes, it becomes easier to derive other representations of the data by processing the same writes in the same order. This is one application of the State machine replication.
    - Updating a derived data system based on an event log can often be made deterministic and idempotent making it easier to recover from faults.
5. Derived Data Vs Distributed Transactions:
    - Classic approach of keeping data systems consistent with each other involves distributed transactions.
    - At an abstract level, they achieve same goal by different means. Distributed transactions decide on an ordering of writes by using locks for mutual exclusion, while CDC and event sourcing use a log for ordering. Distributed transactions use atomic commit to ensure that changes take effect once, log-based systems are based on deterministic retry and idempotence.
    - Transactions provide linearizability & derived data systems are usually updated asynchronously.
    - Distributed transactions based on XA protocol have poor fault tolerance and performance characteristics. 
    - In absence of a widespread support of a good distributed transaction protocol, log-based derived data is the most promising approach for integrating different data systems. But at times, even eventual consistency may not be that useful, and it's difficult to guarantee reading your own writes in the face of it.
6. Limits of total ordering:
    - As systems are scaled, limits to a totally ordered event log become visible
        - Constructing a totally ordered event log requires all events to pass through a single node deciding the ordering. If the throughput of events is more than a single machine can handle, the log is partitioned across machines. The ordering across partitions cannot be guaranteed.
        - For multi-geographic data center, there will be a leader in each datacenter. This implies an undefined ordering of events across data centres.
        - A common design choice in microservives is to deploy each service and it's state as an independent unit, with no durable state shared between services. When two events originate in different services, there is no defined order for those events.
    - Most consensus algorithms (total order broadcast decides total order of events which is equivalent to consensus) are designed for situations when the throughput of a single node is sufficient to process the entire stream of events, and these don't provide mechanism for multiple nodes to share the work of ordering the events.
7. Ordering events to capture casuality: 
    - Causal dependencies arrive in subtle ways. For a social network which stores friendship status in one place and messages in another place, the ordering dependencing between unfriend event and message-send event will be lost. If the message-send event is processed first before the unfriend event and we end up sending the message to the unfriended person unknowingly.
    - We still have some starting options
        - Logical timestamps to capture causality. They still require recipients to handle out of order events and require additional meta-data to be passed around. 
        - Can log an event to record the state of the system that the user saw before making the decision and give that event a unique identifier. Any later events can reference that event identifier in order to record the causal dependency.
        - Conflict resolution algorithms help with events delivered in an unexpected order. 
    - Overtime, patterns for application development may emerge that allow causal dependencies to be captured correctly, derived state to be maintained correctly without forcing all events to go through total order broadcast.

### Batch & Stream processing
1. The goal of data integration is to make sure data ends up at right places in the right form. It requires consuming inputs, transforming, joining, filtering, aggregating, training models and eventually writing to the appropriate outputs.
2. Batch and streams processing are the tools for this and the main difference is batch processing operate on known, finite size datasets. Stream processing operate on unbounded datasets. These distinctions are beginning to blur
3. Spark performs stream processing on top of microbatches i.e a batch processing engine. Flink performs batch processing atop a stream processing engine.
4. Maintaining Derived State:
    - Batch processing has a strong functional flavor encouraing deterministic, pure functions whose output depends only on the input and the processing has no side-effects, treating inputs as immutable and outputs as append-only.
    - Stream processing extends operators to allow manaed, fault-tolerant derived state.
    - Deterministic function are good for fault tolerance and also simplifies reasoning about the dataflows in an organization.
    - It's helpful to think about data pipelines as deriving one thing from another pushing state changes through functional application code and applying the effects to derived systems.
    - Derived data systems could be made synchronous similar to how an RDBMS updates secondary indexes in the same transaction as DB writes. Asynchrony is what makes a systems based on event logs robust, it makes a fault in one part of the system to be contained locally.
    - Secondary indexes often needs to cross partition boundaries. A partitioned system with secondary indexes needs to send writes to multiple partitions (term partitioned index) or send reads to all partitions (document partitioned index). This also is reliable and scalable if index is maintained asynchronously.
5. Reprocessing data for application evolution:
    - Stream processing allows changes to the input to be reflected in derived views with low delay, batch processing allows large amounts of historical data to be reprocessed in order to derive new views from it. 
    - Reprocessing existing data provides a good mechanism for maintaining a system evolving it to support new requirements. Without reprocessing, schema evolution is limited to simple changes like adding an optional field to a record or adding a new type of record
    - Derived views allow gradual evolution. If you want to restructure a data-set, you do not need to perform the migration as a sudden switch. You can maintain new schema / old schema as two independently derived views onto the underlying data.
    - The beauty of such gradual migration is every stage of the process is easily reversible if something goes wrong, you always have a working system to go back to.
6. Lambda architecture:
    - Lambda architecture is a combination of batch and stream processing.
    - The core of the lambda architecture is that incoming data should be recorded by appending immutable events to an always growing data-set, similar to event sourcing. From these events, read-optimized views are derived.
    - The stream processor quickly consumes the events and quickly produces an approximate update to the view, the batch processor consumes the same set of events and produces a corrected version of the derived view.
    - Stream processing is thought to be less reliable and harder to make fault tolerant, batch processing is simpler and less prone to bugs. Stream processing can use fast, approximate algorithms and batch processes use more exact algorithms.
    - Lambda architecture popularized the view of deriving views onto the stream of immutable events and reprocessing events whenever needed.
    - It has some practical problems: 
        - Having to run same logic in a batch and stream mode is significant additional effort. The operational complexity of tuning, debugging and maintaining two separate systems remains even though we have libraries like Summingbird managing abstractions.
        - Stream and batch output needs to be merged to respond to user input. With complex operations such as joins/sessionization or if the output is not a timeseries, this becomes harder.
        - Reprocessing entire batch of data is frequently is expensive for large data-sets. If we process it incrementally, it raises issues like handling cross-boundary windows and handling stragglers. Incrementalizing a batch computation makes it more akin to the streaming layer running counter to the goal of keeping the batch layer as simple as possible.
7. Unifying batch and stream processing: Unifying batch and stream processing requires the following features 
    - Replay historical events through the same processing engine through use of a log-based message broker.
    - Exactly-once semantics for stream processors i.e ensuring that output is the same as if no faults occured by discarding failed or partial output.
    - Tools for windowing by event time not by processing time since processing time is meaningless.

## Unbundling databases
1. Unix and databases have approached the information management problem with entirely different philophies. 
    - Unix viewed its purpose as presenting programmers with a logical but fairly low-level hardware abstraction
    - Relational databases wanted to give application programmers a high level abstraction that would hide the complexitites of data structures on disk, concurrency, crash recovery and so on.
2. While Unix developed files & pipes that are sequence of bytes, databases developed transactions and SQL.
3. While Unix is simpler in the sense that it's a fairly thin wrapper around hardware resources, relational databases are simpler in the sense a short declarative query can draw on a lot of powerful infrastructure (query optimization, indexes, concurrency control, replication) without the author to understand the implementation details.
4. Creating an index:
    - When a new index is created in a relational database, the database scans over a consistent snapshot of the table, picks out the field values being indexed, sorts them out and writes to the index.
    - It then processes the backlog of writes when the snapshot was taken (assuming table was not locked while creating the index) so writes could continue. 
    - The database must then continue to keep the index up-to-date whenever a write is done to the table.
    - This is similar to setting up a new follower replica and bootstrapping change data capture in a streaming system.
    - The database essentially processes the existing dataset and derives the index as a new view onto the existing data.
5. The meta-database of everything:
    - Dataflow across the entire organization starts looking like one huge database. 
    - Whenever a batch stream or ETL process transports data from one place and form to another place an form, it is acting like databases sub-system that keeps indexes/materialized views up-to-date.
    - Batch & Stream processors are like elaborate implementations of triggers, stored procedures and materialized view maintenance rotines. The derived data systems they maintain are like multiple index types.
    - In the emerging architecture of derived data systems, instead of implementing spatial, full-text, B-Tree and other types of indexes and also other data models as a feature of a single integrated product, they are provided by different pieces of software running on different machines, administered by multiple reads.
    - If we start from the premise that no single data model or storage format is suitable for all access patterns, there are two avenues by which different storage and processing tools can be composed into a cohesive system
        - Federated Databases: Unifying reads
            - A federated query interface follows the relational tradition with a high level query language. 
            - It's basically a unified query interface for a wide variety of storage engines and processing methods - an approach known as a federated database or poly-store. Ex: PostgreSQL's foreign data wrapper feature.
            - Users who want to combine data from disparate storage systems can do so through the unified interface; users can also access the various storage engines to fetch data of their choice.
        - Unbundling databases: Unifying writes
            - Federation does not have good answer in synchronizing weites to several different systems.
            - Making it easy to reliably plug together storage systems (change data capture and event logs) is like unbundling a databases's index maintenance features that can sync writes across disparate technologies.
            - This follows the Unix tradition of small tools that do one thing well, communicating through a uniform low-level API (pipes) and can be composed using a higher level language (the shell)
6. Making unbundling work:
    - Federated read-only querying requires mapping of one data model into another which though takes thought is a manageable problem. 
    - Keeping the writes to several storage systems in sync is the hardest problem.
    - Traditionally, we had distributed transactions that synchronize writes across heterogenous systems. However, an asynchronous event log with idempotent writes is more robust and practical approach to keep data on several storage systems in sync.
    - Distributed transactions are used in some stream processors to achieve exactly-once semantics. When a transaction would need to involve systems written by different groups of people, the lack of a standardization protocol makes integration much harder.
    - Ordered log of events with idempotent consumers is a simpler abstraction.
    - The biggest advantage of log-based integration is loose coupling between various components:
        - Makes systems more robust with respect to outages or performance degradation of various individual components. The event log can buffer messages allowing producers or consumers to run unaffected. 
        - Unbundling data systems alows different software components and services to be maintained indepedently of each other. Specialization allows each team to focus on doing one thing well, with well-defined interfaces to other team's systems.
7. Unbundled versus integration systems:
    - Unbundling will not replace the database's in their current form.
    - Database will be required for storing state in stream processors and serve queries for the output of batch and stream processors. Specialized query engines will continue to work for particular workloads.
    - Each pieces of software has a learning curve, operational quirks and configuration issues. A single integrated software product may be able to achieve more better/predictable performance on the kinds of workloads for which it is designed for compared to a system consisting of several tools composed with application code. Building for scale that you don't need is a wasted effort and is a form of premature optimization.
    - The goal of unbundling is to allow to combine different databases to achieve good performance for a wider range of workloads than is possible with a single piece of software.
8. What's missing?
    - We still don't have the unbundled database equivalent of the Unix shell (high-level language for composing storage and processing systems in a simple and a declarative way). For example, a command mysql | elasticsearch by analogy of Unix pipes would created the indexes basis of MySQL documents.
    - It would be great to precompute and update caches more easily. 
    - A materialized view is a precomputed cache so we can create a cache by declaratively specifying materialized views for complex queries including recursive queries on graphs.
9. Designing applications Around dataflow:
   - The approach of unbundling databases by composing specialized storage and processing systems with application code is also called as "database inside-out approach". 
   - The ideas are simply an amalgamation of people's ideas like Dataflow languages such as Oz, Juttle, functional reactive languages such as Elm. The term unbundling was proposed by Jay Kreps.
   - Like how Spreadsheets change a cell's value on the basis of a set formula, whenever a record in the data system changes, we want any index/cached views of that record to be automatically updated.
   - We also need to be able to integrate disparate technologies written by different groups of people over time, reusing existing libraries and services. It's unrealistic to expect software to be developed using one particular language or framework or tool.
   - Application code as a derivation function:
    - When one dataset is derived from another, it goes through some kind of transformation function. For example:  
        - A full-text index is created by applying various natural language processing functions followed by building up a data structure for efficient lookups (inverted index)
        - A secondary index has a straightforward transformation function - for each row or column in the database it picks values in those columns and indexes them. 
        - A cache often contains an aggregation of data in the form in which it is going to be displayed in a user interface UI. Populating the cache requires knowledge of what fields are referenced in the UI.
    - When the function that creates a derived dataset is not a standard function custom code is required to handle the application specific effects. This custom code is where most databases struggle.
    - RDBMS support triggers, stored procedures, user-defined functions to execute application code within the database, they're implemented as an after-thought.
    - Separation of application code and state:
        - Databases do not fit well with the requirements of modern application development such as dependency & package management, version control, rolling upgrades evolvability, monitoring metrics and integration with external systems, calls to network services.
        - Deployment & cluster management tools such as Mesos, Yarn, Docker, Kubernetes and others have specificatlly been designed for running application code.
        - We can have some parts of the system that specialize in durable data storage and other parts that specialize in running application code. The two can interact while still remaining independent.
        - The trend has been to separate stateless application logic separate from persistent state since the style of deployment is convinient, servers can be added and removed at will.
        - In this type of typical programming model, the database acts as a shared mutable variable that can be accessed synchronously over the network. The application can read and update the variable, the database takes care of making it immutable, providing some concurrency control and fault tolerance.
        - You can't subscribe to change to that mutable variable (like an Observer pattern in code). Subscribing to changes is only just beginning to emerge as a new feature.
    - Dataflow: Interplay between application state changes and code:
        - Instead of thinking of a database as a passive variable, we think more about the interplay between state, state changes and code that processes them. Application responds to state changes in one place by triggering state change in another place.
        - Message passing systems have this concept of corresponding to events. 
        - Unbundling databases refers to applying the idea of reacting to state changes in the creation of derived datasets: caches, full-text search indexes, machine learning or analytics systems.
        - Maintaining derived state is not the same as asynchronous job execution for which messaging systems are designed for
            - The order of state changes is important in maintaining derived data. Many message brokers don't have this property when redelivering unacked messages
            - Fault tolerance is essential for derived data: losing a single message causes the derived dataset to go permanently out of sync. Both message delivery & derived state updates must be reliable.
            - Stable message ordering and fault-tolerant message processing demands are quite stringent but are much less expensive and operationally more robust. Modern stream processors can provide these guarantees at scale, they allow application code to run as stream processors.
            - Like Unix tools, stream operators can be composed to build large systems around dataflow. Each operator takes streams of input as a change and produces other stream of state changes as output.
        - Stream processors & services:
            - The current trend of application development involves breaking down functionality into a set of services that communicate via synchronous network requests such as REST APIs.
            - Stream operators are equivalent but the underlying communication mechanism is different: one directional, asynchronous message streams rather than sync request/response interactions.
            - Dataflow systems can offer better performace. In a currency converter modelled as a dataflow system, the code that processes purchases would subscribe to a stream of exchange rate updates ahead of time and record the current rate in the local database. While processing the purchase order to get the currency, it will only query the local database.
            - Not only is the dataflow approach faster and more robust to the failure of other services, but it's also most reliable. Instead of RPC, we now have a stream join between purchase events and exchange rate update events.
            - The join is time-dependent. If events are reprocessed at a later point in time, we need the original exchange rate at the original time of purchase.
            - Subscribing to a stream of changes brings us closer to the spreadsheet model of computation. When some piece of data changes, any derived data depending on it can be swiftly changed.
10. Observing derived state:
    - The write path is whenever a piece of information is writtten to the system, it may go through multiple stages of batch & stream processing and eventually every derived dataset is updated to incorporate the data that was written.
    - The read path is when serving a user request you read from the derived data set perhaps performing some more processing on the result and constructing the user response.
    - The write path and read path encompass the whole journey of data from the point it is collected to the point where it's consumed. The write path is precomputed (eager evaluation) and the read path is done when it's asked for (lazy evaluation)
    - The derived dataset is where the read & write paths meet.
    - Materialized views and caching:
        - A full-text search index is a good example: the write path updates the index and read path searches the index for keywords.
        - Writes need to update the index for all terms that appear in the document. Read needs to search for each of the words in the query and use Boolean logic to find documents that contain all "AND"
        of the words in the query, "OR" of each of the words
        - If you did'nt have an index, a search query would scan all documents like grep which would be very expensive. No index means less work on the write path but more on the read path.
        - Another option is you can precompute the search results for all possible queries. More work on the write path but easy on the read path. The set of all possible queries could be infinite and thus precomputing all search results would require infinite storage space.
        - One more alternative would be to have a fixed set of common queries and precompute the search results for them so that they can be served quickly. This is called a cache of common queries or materialized view.
        - Caches, indexes and materialized views shift the boundary between the read and write path. They allow to do more work on write path to save on effort on read path.
    - Stateful/offline capable clients:
        - Traditionally, web browsers have been stateless clients that can only do useful things when you have an Internet connection.
        - Recently singple page JavaScript web-apps have gained lot of stateful capabilities, including client-side UI interactions and local storage in the web browser. Mobile apps similarly store a lot of data on user device and don't require a round-trip to server. 
        - These have renewed interest in offline-first applications that do as much as possible on the same device without requiring a remote connection.
        - When we move away from the assumption of stateless clients talking to a central database and talk about maintaining state on end-user devices, a world of new opportunities opens up.
        - We can think of the on-device state as cache of state on the server.
    - Pushing State changes to clients:
        - The state in a typical browser web app is not updated unless you explicitely poll for changes. (HTTP-based feed subscription protocols are a basic form of polling)
        - Server-sent events and Websockets provide communication channels by which a Web browser can keep an open TCP connection and server can actively push messages to the browser as long as it stays connected.
        - In terms of read path and write path, this means extending the write path all the way upto the end user. The end user's client still needs a read path initially to get the initial state but thereafter it could rely on a stream of state changes sent by the server.
        - If the device is offline for some time and unable to push any state changes, we can use a log-based messae broker which can reconnect after failing or becoming disconnected and ensure that it does'nt miss any messages.
    - End-to-end event streams:
        - State changes could also flow through and end-to-end write path: from the interaction on one device that triggers a state changes via event logs and through several derived data systems and stream processors all the way to the user interface of another person. The state changes could be propagated with fairly low delay.
        - We have been assuming all this while of statless clients and request/response interactions. These are firmly ingrained in our databases, libraries & protocols.
        - To extend the write path all the way to the end-user, we would need to fundamentally rethink the way we build many of these systems: moving away from request-response towards publish-subscribe flow.
        - With the advantages of more responsive user interfaces and better offline support would make it worth the effort.
    - Reads are events too:
        - A database allows random access read queries to the data that would otherwise require scanning of the whole event log. 
        - Mostly, data storage is separate from the streaming system. Stream processors do need to maintain state to perform aggregations and joins. This state is within the stream processor. Some stream processors make this state queriable by outside client.
        - Writes go to an event log and reads are transient network requests that go directly to the node s that store the data being queried. 
        - It's also possible to represent reads as streams of events and send both read/write events to the stream processor. The processor responds to read events by emitting the result of read to an output stream.
        - In a way, when both writes and reads are represented as events, and routed to the same stream operator, we are performing a stream-table join.