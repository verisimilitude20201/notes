# Batch processing

## Introduction
1. Request-Response style data processing in which you ask for something or send an instruction and hope the system gives an answer later on. Most databases, caches, search indexes, index servers work in this way.
2. It's assumed that the user triggers the request and we measure the response time of these systems. 
3. Request-response is not the only way of building systems. There are 3 general categories of systems
    - Online Request-Response systems:
        - Give a request get a response as discussed above.
        - Response time is usually the primary measure of the performance of the service and availaibility is very important.
    - Batch processing systems:
        - Take a large amount of data as input , runs a job to process it and produce an output. 
        - Jobs take from minutes to several days so a user does'nt wait for the job to finish.
        - They are scheduled to run periodically. 
        - The primary performance measure is throughput.
    - Stream processing
        - Mid-way between online and offline batch processing. 
        - A stream processor consumes inputs and produces outputs. It operates on events shortly after they happen whereas a batch job processes a fixed set of data. 
        - It builds on batch processing. 
4. Batch processing is an important building block in building reliable, scalable and maintainble applications. 
5. MapReduce was an algorithm published by Google that made it massively scalable. It was a major step forward in terms of scale of processing that could be achieved on commodity hardware.
6. Unix philosophy carries over to large-scale, heterogenous distributed systems.

## Batch processing with simple Unix tools
1. Let's assume you have a web server appending a line to its access log every time it receives a request.
```
216.58.210.78 27-Feb-2015:17:55:11 +0000 "GET /index.html" 200 4455 "Mozilla/5.0 Macintosh Intel Mac OS X AppleWebKit
```
2. That's a lot of information and mostly consists of remote address, timestemp, URL, status_code, bytes sent and http user agent.
3. Simple Log analysis using UNIX tools
    - Let's say we execute the below commands on this access log
        ```
        cat access.log | awk '{print $7} | sort | uniq -c | sort -r -n | head -n 5
        ```
    - The above command prints the top 5 most requested URLs.
    - The command is very powerful, it processes gigabytes of log files in a matter of seconds. 
    - Many data analyses can be done in a few minutes using some combination of awk, sed, grep, sort, uniq, xargs and they perform very well.
    - If you use a Ruby script to do the above, there is a big difference in the execution flow (apart from the obvious syntactic difference) and it becomes apparent if you run this script on a very large file
    - If the job's working set is larger than the availaible memory, the UNIX sorting approach has the advantage that it can mae efficient use of disks. Chunks of data can be sorted in memory and written to disk as segment files and multiple sorted segments can be merged into one sorted file. Merge sort has sequential access patterns performing well on disks.
    - GNU sort utility automatically handles larger than memory datasets by spilling to disk and automatically parallizing sorting across multiple CPU cores. The simple chain of user commands that we saw earlier scales to large data-sets without running out of memory.
    - The only bottle neck is the rate at which the input file can be read.

### The Unix philosophy
1. Doug Mcllroy said in 1968 about the Unix philosophy "We should have some ways of connecting programs like a garden hose. Screw in another segment when it becomes necessary to massae data in another way". The idea of connecting programs with pipes became a part of the Unix philosophy.
2. The Unix philosophy was described in the following 4 points
    - Make each program do one thing well. To build a new job, build afresh rather than complicate old programs by adding new features.
    - Expect the output of every program to become an input to another program. Don't clutter output with extraneous information. Don't insist on interactive input. Avoid stringent columnar or binary input formats. 
    - Design and build software, even operating systems  to be tried early. Don't hesitate to throw away the clumsy parts.
    - Use tools in preference to unskilled help to lighten a programming task.
3. This approach viz rapid prototyping, incremental iteration, being friendly to experimentation, breaking down large projects into manageable chunks forms a part of the Agile and DevOps movement of today.
4. A Unix shell like Bash lets us compose small programs into powerful data processing tools. They can be joined together in flexible ways. How does Unix enable this composability
    - Uniform interface:
        - If the output of a program is to become the input to another program, those programs must use the same data format or a compatible interface.
        - That interface is a file in Unix, an ordered sequence of bytes. Several different things can be represented using a file: an actual file, a communication channel to another process viz. stdio, stdout, socket, a device driver. All these things share a uniform interface so they can be plugged together.
        - Many Unix programs treat these sequence of bytes as ASCII records using the same record separator allowing them to interoperate. 
        - The parsing of each record uses a whitespace, tab or CSV or even a pipe to split a line into fields. 
        - Not many pieces of software compose as easily as Unix does. Today, it's an exception to have programs that work together as Unix does.
        - Even databases don't make it easy to get data out of one and into the other. This lack of integration leads to Balkanisation of data.
    - Separation of logic & wiring: 
        - Another characteristic of Unix tools is their use of stdio and stdout. If you run a program and don't specify stdio and stdout, the default stdin comes from keyboard and stdout goes to the screen. You can even take in input from a file or redirect output to a file. Pipes connect stdout of one process to the stdin of another process (with a small in-memory buffer without writing intermediate data to disk)
        - The shell can wire up the input and output in whatever way they want, the program does'nt know or care where the input is coming from and output is going to. Separating the input/output from the program logic makes it easy to compose small tools into bigger systems (Inversion of Control, loose coupling or late binding)
        - There are still limits to what stdio and stdout can do. Programs with multiple inputs or outputs are possible but tricky.
    - Transparency & Experimentation: Unix tools make it quite easy to see what's going on
        - The input to Unix commands are normally treated as immutable. You can run the commands as long as you want without damaging the input. 
        - You can end the piepline at any point, pipe the output to less and look at it if it has the expected form. Great for debugging.
        - Write the output of one pipeline stage to a file and use that as input for next stage. 
    - The biggest limitation of a Unix tools is that they run on a single machine. 

## MapReduce and Distributed Filesystems
1. MapReduce is like Unix tools but its distributed potentially across thousands of machines. It takes in one or more inputs and produces one or more outputs.
2. Running a MapReduce job does'nt modify the input and does'nt normally have any side effects other than producing the output. Output files are written once in a sequential form without modifying any output.
3. MapReduce reads and writes files on a distributed file system. In Hadoop's implementation of MapReduce, that file system is called HDFS (Hadoop Distributed File System - an open source implementation of Google File System)
4. HDFS is based on Shared Nothing approach in contrast to the shared disk approach of SAN and NAS architectures. 
    - Shared disk is implemented by a centralized storage applicance using custom hardware and special network infra such as Fibre channel. 
    - Shared nothing requires no special hardware only computers connected by a conventional data center network.
5. HDFS consists of a daemon process running on each machine exposing a network service allowing other nodes to access files stored on that machine (every machine has some disks attached to it). 
6. A centralized server NameNode tracks which file blocks are stored on which machine.
7. For tolerating disk and machine failures several copies of the data are stored on several machines. An erasure coding scheme allows lost data to be recovered with lower storage overhead. 
8. The techniques are similar to RAID, which provides redundancy across several disks attached to the same machine. In a distributed file system, file access and replication are done over a conventional datacenter network without any special hardware.
9. The cost of data storage and access on HDFS using commodity hardware and open source software is much less than the equivalent capacity on a dedicated storage appliance.

### MapReduce Job Execution
1. MapReduce is a programming framework to write code to process large datasets in a distributed file system like HDFS.
2. Let's understand it by the web server access log example to understand the pattern of data processing
    - Read the set of input files and break it up into records. For the access log, each record is one line in the log.
    - Call mapper function to extract a key value from each input record. The key for the access log is the URL and value is empty.
    - Sort key-value pairs by key.
    - Call reducer function to iterate over sorted key-value pairs. Sorting has made adjacent multiple occurences of the same key. For the access log example, the reducer is the command uniq -c which counts the number of adjacent records with the same key.
3. To create a MapReduce job, you need to implement two callback functions
    - Mapper: Called once for every input to extract key and value from record. Does'nt keep state from one record to the next so each record is handled independently.
    - Reducer: Takes the key-value pairs produced by mappers and collects all values belonging to the same key and calls the reducer with an iterator over that collection of values. The reducer can produce output records. 
4. MapReduce can parallelize computation across many machines without you having to write code to explicitely handle the parallelism. 
5. Mapper and reducer can operate only one record at a time so they don't need to know where their input is coming from or output is going to.
6. More commonly, mappers and reducers are written in a conventional programming language. For example: In Hadoop MapReduce, mapper and reducer are a Java class implementing a particular interface.
7. MapReduce parallelization is based on partitioning. The input to a partition is typically a directory in HDFS and each file or block within the directory is a separate partition that can be processed by a separate map task.
8. The MapReduce scheduler tries to run each mapper on one of the machines that stores a replica of the input file provided that machine has enough CPU/RAM to run the map task. This is called "putting the computation near data". It saves copying the input file over the network reducing network load.
9. MapReduce code first copies the code eg JAR files to the appropriate node and then starts the map task which begins reading the input file and passes one record at a time to the mapper callback. Mapper output consists of key-value pairs. 
10. Reduce side of the computation is partitioned. The number of reducers are configured by job author
11. To ensure all key-value pairs with the same key go to the same reducer, the framework computes a hash of the key to determine which reducer should receive a particular key-value pair.
12. The key-value pairs should be sorted. Since the dataset is too large to be sorted with a conventional sorting algorithm on a single machine, we have a staged sort.
    - Each map task partitions its output by hash of the key and each partition is written to a sorted file on the mapper's local disk using a technique similar to SSTable and LSM trees.
    - After a mapper finishes reading it's input file and writing the sorted output, the MapReduce scheduler notifies the reducer to start fetching the output files for that mapper. 
    - The process of partitioning by reducer, sorting and copying data partitions from mapper to reducer is called shuffle.
13. Reducer task takes files from the mappers and merges them together to preserve the sort order. If different mappers produced the records with the same key, they will be adjacent to each other.
14. The reducer is called with a key and an iterator that sequentially scans all records with the same key. The reducer can produce any number of output records with the same key and uses any arbitrary logic to combine them. 
15. MapReduce workflows:
    - A single MapReduce job is fairly limited in the range of problems that it can solve. 
    - It's very common for MapReduce jobs to be chained together into workflows such that the output of one job becomes the input for the next one. 
    - Such chaining is done implicitely by directory name: the first job must be configured to write it's output to a designated directory in HDFS and second job must be configured to use that same directory as it's input.
    - Chained MapReduce jobs are thus like pipelines of Unix commands. 
    - A batch job's output is considered valid when the job has completed successfully. MapReduce discards the partial output of a failed job.
    - To handle dependencies between job executions, various workflow schedulers for Hadoop such as Oozie, Azkaban, Luigi have been developed.   
    - Workflows consisting of 50 to 100 MapReduce jobs are common when building recommendation systems and in a large organization, many teams might be running different jobs that read each other's output. Workflow schedulers help manage and wire together complex MapReduce executions.  

### Reduce side joins and Grouping (Join implementation)
1. It is common for many datasets to have one record to be an association for another record: a foreign key in a relational model, document reference in a document model, edge in a graph model. 
2. Joins are necessary whenever you attempt to access the records on both sides of the association. For a query involving joins, it will involve multiple index lookups.
3. When a MapReduce job is given a set of files as joins, it reads all content of those files. A database would call it a full table scan. It does'nt have a concept of indexes. If we can parallelize the computation across several machines, scanning the entire set of files might be a reasonable thing to do.
4. In context of bathc processing, joins mean resolving all occurences of some association within a dataset.
5. Example: Analysis of user activity events
    - A Typical example is when we have a stream of user activity data on a website (clickstream or activity stream) and a database of users. (Star schema: Log of events is the fact table and user database is one of the dimensions)
    - Need to correlate user activity with user profile information. Activity data contains only user ID. Embedding profile information in each record would be expensive.
    - Simplest implementation of the analytics job would be to go through each event and join it with the user database to get user data for every user id. The processing however would be limited by the round trip to the database server, the effectiveness of a local cache would depend on the distribution of data and running large number of queries in parallel would overwhelm the database.
    - To achieve good throughput in a batch process, the computation should be local to one machine. Network request for every record would render it too slow. It would also introduce non-determinism because the data in remote database could change while the job is running. 
    - A better approach would be to take a copy of the user database from a DB backup using an ETL process and put it in the same distributed file system as the log of user activity events
    - You would have the user activity data in one set of files and user data in another file and you can use MapReduce to bring together all relevant records in the same place and process them efficiently.
6. Sort-merge joins:
    - We would have one set of mappers going through the activity data which extract the user id as key and activity as value while another set of mappers would go over the user database (extracting the user id as key and user's profile information as value.
    - MapReduce framework partitions the mapper output by key and sorts the key-value pairs. 
    - All events activity key-value pairs and user profile info key-value pairs get adjancent to each other in the reducer input.
    - A secondary sort may also be performed wherein the user profile key-value pair comes before the user activity key-value pair.
    - The reducer can now store the user profile information in local variable and iterate over activity events for that user id. Thereby it needs to keep just one user profile record in memory and never needs to make any requests over the network.
    - This algorithm is known as a sort-merge join since the mapper output is sorted key and the reducers then merge together the sorted lists of records from both sides of join.
7. Bringing all data together at the same place. 
    - Sort-merge join brings all the necessary data to perform the join operation at the same place. 
    - Having lined up all data, the reducer can be a single-threaded piece of code that can churn through records with a high throughput and low memory overhead.
    - When a mapper emits a key-value pair, the key acts like the destination address to which the value should be delivered. All key-value pairs with the same key will be delivered to the same reducer (destination)
    - MapReduce has separated the physical network communication aspects of the computation (get the data to the right machine) from application logic(processing the data once you have it). This contrasts with the typical use of database, where a request to fetch data from database occurs deep inside a piece of application.
    - MapReduce transparently retries failed tasks without affecting application logic.
8. GROUP BY:
    - Another pattern of "bringing same data together" is grouping records by some key (GROUP BY in SQL).
    - All kinds of records with the same type are grouped together and then aggregation is applied within each group. 
         - Pick top K ranking in each function.
         - SUM(field)
         - COUNT(*)
    - To implement this using MapReduce, set the mappers in such a way that they produce the desired grouping key. Partitioning and sorting process brings all records with the same key to the same reducer. Quite similar to the JOIN implementation.
    - One common grouping use is sessionization, collate all activity events for a particular user session to find the sequence of actions a user did. This can be used to figure out whether some marketing activity is worthwhile.
9. Handling skew:
    - The pattern of bringing all data together breaks down if there is a very large amount of data for a single key (skew)
    - Such disproportionately active records are called linchpin objects or hot keys
    - Completing all user activity for such a hot key gives rise to significant skew at the reducer's end and slows it down. 
    - Few algorithms can be used to compensate for hot keys. Skewed join method runs a sampling job to determine hot key. The mappers send records related to any hot key to one of several reducers chosen at random. For the other input to the join, records related to the hot key are sent to those reducers handling them. 
    - Sharded join method requires hot keys to be specified explicitely rather than use a sampling job. It uses randomization to alleviate hot spots in a partitioned database.
    - Skewed join optimization in Hive  requires hot keys to be specified explicitely in the table metadata. It stores records related to those keys in separate files from the rest. On joining, it perfoms a map side join on that table for the hot keys.
    - When grouping by a hot key and aggregating them, the grouping is performed in 2 stages.
        - First MapReduce sends the records to a random reducer so each reducer performs the grouping on a subset of records for the hot key and outputs a compact aggregated value per key.
        - Second MapReduce combines the values from all of the first stage reducers into a single value per key.

### Map-side joins
1. Mappers take the role of extracting key value from each record, assigning the key to a reducer partition and sorting by key. 
2. Reduce side join approach has the advantage that you don't need to make any assumptions of the input data. Mappers have already prepared the data ready to be joined. However, all the sorting, copying to reducers and merging of reducer inputs can be expensive. Data may also be written to disk several times as it passes through the stages of MapReduce. 
3. It's possible to make joins faster using a so-called map-side join approach if you can make certain assumptions about your input data. This is a simple cut-down MapReduce job in which there are no reducers and no sorting. 
4. Broadcast hash joins:
    - A typical case of Map-side joins is when a large dataset is joined with a small dataset. The small dataset should be small enough to be loaded in memory.
    - Imagine that the user database is small enough to fit in memory. Each mapper on start up can read the database into an in-memory hash table, and simply iterate through each activity event to substitute the user information when the user id is encountered. 
    - This is called as "broadcast hash join" reflecting the fact that each mapper for a partition of the large input reads the entirety of the small input in memory. The small input is effectively broadcast along all the mappers and the word "hash" reflects its use of the in-memory hash table.
    - As an alternative, the small input can also stored in a read-only index on the local disk. The frequently used parts of the index will remain in the OS page cache. This can provide random access lookups without requiring all the dataset to be fit into memory.
5. Partitioned hash joins:
    - If the inputs to the map-side join are partitioned in the same way, hash join approach can be applied to each partition independently. 
    - If the activity events and the user database are partitioned on the basis of the last decimal digit of the user, you can be sure that all the records you might want to join are located in the same number partition. Each mapper can now load only a smaller amount of data into its hash table.
    - This only works if both of the join's inputs have same number of partitions with records based on the same key and same hash function. 
    - These are also called bucketed map joins in Hive.
6. Map-side merge joins: 
    - If the inputs are partitioned in the same way and also sorted based on the same key. 
    - Does'nt matter if the inputs are small enough to fit in memory. A mapper would perform the same merging operation that would be performed by a reducer: read both files sequentially in ascending order of keys, match records with the same key. 
    - This could also be performed in the reduce state of the prior job. It may still be appropriate to perform the merge join in a separate map-only job if the partitioned and sorted sets are need for some other purpose apart from this join.
7. MapReduce flows with map-side joins:
    - When the output of a MapReduce job is consumed by downstream job, the structure of the output depends on whether it's a map-side join or reduce-side join.
    - The output of a map-side join is partitioned and sorted by the join key, whereas the output of the map-side join is partitioned and sorted in the same way as the large input (one map task is started for each of the file block of the join's large input, regardless of whether a partitioned or broadcast join is used.)
    - Map-side joins make more assumptions about the size, sorting and partitioning of their input databases. 
    - Knowing physical layout of data is important while optimizing join strategies. You must know the encoding format and the name of the dictionary, you must also know the number of partitions and the keys by which data is partitioned and stored.

### The Output of batch workflows
1. Analytic queries often scan over a large number of records performing grouping and aggregations and the output has the form of a report: a graph showing the change in metric over time, top 10 items according to a ranking. The consumer of such data is a business analyst who uses it to make business decisions. 
2. Batch processing is closer to analytics where it scans over large portions of input datasets. But the output of batch processing is somewhat different, it may not be a report
3. Building search indexes:
    - Google originally used MapReduce for building search indexes. 
    - Full-text search index such as Lucene is a file (term dictionary) in which you can efficiently lookup keywords and find a list of all document IDs containing that word. In reality, the search index requires additional data to rank search results by relevance, correct misspellings, resolve synonyms. 
    - A batch process is a very effective process of building the search indexes.
        - Mappers partition the set of documents needed, 
        - Each reducer builds index for it's partition 
        - Index files are written to the distributed file-system.
    - Building document-partitioned indexes parallelizes very well. 
    - Index files are immutable once they are created.
    - We can re-run the indexing workflow for entire set of documents and replace previous index files if the indexed set of document changes. This is computationally expensive if only a small number of documents changed. 
    - Alternatively, Lucene writes new segment files indicating adds, updates and deltes and asynchronously merges and compacts segment files in the background.
4. Key-value stores as batch process output:
    - One more common use of batch processing is to build machine learning classifiers/models (spam filters, anomaly detection, image recogintion, recommendation engines).
    - The output of these batch jobs can be queried by user ID to obtain friend suggestions for a user or product id to get a list of related products. How does the output of these batch jobs get to a database so that the application can query it? 
    - For each output record, we can save it in database. But this is a bad idea for several reasons
        - Making a network request for every record is orders of magnitude slower.
        - Concurrently executing mappers writing to the same database may overwhelm it and reads may suffer.
        - MapReduce provides a clean all-or-nothing guarantee. If a job succeeds, the result is the output of every task running once, if some tasks failed along the way. If the entire job fails, no output is produced. Writing to an external system from a job produces external visible effects that cannot be hidden. Partially completed jobs' output may go into the database.
    - A better solution is to build a brand new database inside the batch job and write it as files to the job's output directory in the distributed filesystem. Those data files are immutable and can be loaded in bulk into servers that handle read-only queries.
    - When loading data into Voldemort, the server continues to server requests to the old data files while new data files are being copied from the filesystem to the server's local disk.
    - If anything goes wrong during this process, we can switch back to the old files since they are still there and immutable.
5. Unix philosophy of batch process outputs:
    - The Unix philosophy encourages experimentation by being explicit about the dataflow. A program reads its input and writes its output. In the process, any previous output is completely replaced with the new output and the input is unchanged with no other side-effects. The MapReduce also follows this same philosophy. We can re-run the batch job without fearing any side-effects.
    - This has several advantages:
        - We can simply roll back to a previous version of the code and re-run the job and the output will be correct again. Databases with read/write transactions don't have this property. If you deploy buggy code that writes bad data, then rolling back the code will do nothing to fix the database.
        - Feature developments can proceed more quickly because of the ease of rolling back. This principle minimizing irreversibility is beneficial for Agile
        - Map or Reduce tasks on failure can be scheduled to run again on the same input. The automatic retry is safe because inputs are immutable and outputs from failed tasks are discarded by the MapReduce framework.
        - The same set of files can be used as input for various different jobs.
        - MapReduce jobs separate logic from wiring (configuring the input/output directories) which provides separation of concerns: one team can focus on implementing a job that does one thing well while other team can decide where and when to run that job.

### Comparing Hadoop To Distributed Databases
1. Hadoop is a distributed version of Unix where HDFS is the filesystem and MapReduce is a Unix Process (which happens to run the sort utility)
2. MapReduce style distributed processing is not new, Massively Parallel processing (MPP) databases that are about a decade old already do this. For example: Gamma, Teradata, Tandem.
3. The difference is MPP databases focus on parallel execution of analytical queries on a cluster of machines while the combination of MapReduce and distributed filesystem provides something that's more general purpose operating system that can run programs.
4. Diversity of Storage:
    - Databases require you to structure data according to a data model. Files in a distributed file systems are just byte sequences which can be in any data model and encoding.
    - Hadoop opens up the possibility of dumping data into HDFS and figuring out how to process it further later on.
    - MPP databases require careful modelling of data and query patterns before importing the data into the database's proprietary storage format.
    - At times, raw data in a raw format is much more useful than trying to decide the data model upfront. The idea is similar to data warehousing: bring data from all parts of the organization together and dump it into one place because it enables joins across disparate datasets. Collecting data in a raw form and worrying about schema design later allows the data collection to be speeded up. This is conceptually what a data lake or enterprise data hub is.
    - The interpretation of data now becomes a consumer's problem (schema on read approach)
    - Simply dumping raw data allows for several transformations. There may not be one ideal data model but several different views onto the data suitable for different purposes. This approach is called "sushi principle: raw data is better".
    - Thus, Hadoop is used for implementing ETL processes, data from transaction processing systems is dumped in a raw form and MapReduce jobs are written to cleanup that data, transform it into an relational form and import it into an MPP database for analytics. Data modelling is decoupled from data collection,.
5. Diversity of Processing models:
    - Although SQL provides an efficient and expressive query language to write expressive queries, thus making it accessible for graphical tools used by business analysts, not all kinds of processing can be expressed as SQL queries. For example: if we are building recommendation engines, machine learning, text processing, you need a more general kind of data processing that are often specific to an application. 
    - MapReduce gives the engineers ability to run their own code over large datasets. With Hadoop and MapReduce, you can build an SQL query engine on top of it, you can also write many other forms of batch processes that do not lend themsevles to being expressed as an SQL query.
    - Subsequently, people found the MapReduce model limiting, so various other processing models were built atop Hadoop 
    - Hadoop-based processing systems are flexible enough to support diverse set of workloads within the same cluster. 
    - Hadoop ecosystem includes both random-access OLTP databases such as HBase and MPP style analytic databases such as Impala. Both use HDFS for storage. 
6. Designing for frequent faults:
    - Two more differences standout between Hadoop and MPP database: the handling of fault and use of memory and disk. Batch processing is less sensitive to faults since they don't immediately affect users and can be run again.
    - MPP databases resubmit the query and retry it again if a node crashes while executing the query. This way of error handling is acceptable since cost of retrying is not great. MPP databases try to keep as much data in memory as possible (hash joins) to avoid the cost of reading from disk.
    - MapReduce can tolerate the failure of a map or reduce task without affecting the individual job as a whole. It is retried at the granularity of an individual task. It's eager to write data on disk assuming that dataset would be too big to fit in memory anyways.
    - To understand the reasons of MapReduce's sparing use of memory and task level recovery, it's helpful to look at it's environment for which it was originally designed. 
    - Google has mixed datacenters in which online production and offline batch jobs run on same machines. Every task also has a resource allocation enforced using containters. Every task has a priority and low priority tasks on same machines can be terminated to free up resources. 
    - Non-production (low priority) tasks computing resources can be overcommitted because system knows it can reclaim resources if necessary. 
    - Since MapReduce tasks run at a lower priority, they run the risk of being pre-empted any time. Batch jobs effectively pick up the scraps under the table and use any computing resources that remain after the high-priority processes have taken.
    - At Google a MapReduce task that runs for an hour has approximately 5% risk of being terminated to make space for a higher priority process.
    - This is why MapReduce at Google is designed to tolerate frequent task termination. Not because of hardware being unreliable it's because of freedom to arbitrarily terminate processes enables better resource utilization in a cluster.

## Beyond MapReduce
1. The kind of distributed computation depends on the volume of data, its structure and type of processing being done with it so tools other than MapReduce may be useful.
2. MapReduce though simple to understand what its doing, is actually hard and laborious to implement. Any join algorithm would require to be implemented from scratch.
3. Therefore, many higher level programming models such as Pig, Hive, Cascading, Crunch were created as abstractions on the top of MapReduce which were easier to implement.
4. Just adding an abstraction on top of MapReduce does'nt solve some problems which manifest themselves as poor performance for some kinds of processing. Let's look at some other tools apart from MapReduce.

### Materialization of Intermediate State
1. The main contact points of a batch job with the outside world is its input and output directory on the distributed file system. If you want the output of one job to become the input to another job, you need to configure the second job's input directory to be same as first job's output directory. External job scheduler must start second job after first completes.
2. In many cases, this output of one job only serves as an input to the second job maintained by the same team. In this case, files on the distributed file system are simply intermediate state - simply passing the state from one job to another. The process of writing these intermediate files is called materialization. 
3. In contrast, Unix pipes stream one command's output to the next command's input incrementally using only a small in-memory buffer.
4. MapReduce approach of fully materializing intermediate state has drawbacks
    - A MapReduce job can start when all tasks in preceding jobs have completed. Processes connected with a Unix pipe can all begin at the same time with output being consumed as soon as it is produced. Having to wait until all previous tasks have completed slows down the execution of the workflow as a whole.
    - Storing intermediate state in a distributed filesystem means those files are replicated across several nodes which is often overkill for temporary data.
5. Dataflow engines:
    - To fix these problems with MapReduce, several dataflow execution engines for distributed computation have been developed. Most popular are Flink and Spark.
    - They handle an entire workflow as one job rather than breaking it up into independent sub-jobs. Since they explicitely model the flow of data through several processing engines, they are called as dataflow engines. 
    - They work by calling a user-defined function to process one record at a time on a single thread. They parallelize work by partitioning inputs and they copy the output of one function over the network to become the input to another function. These functions can be assembled in more flexible ways. We call these functions as operators and dataflow engines provide many options to connect operators.
        - Repartition and sort records by key like in shuffle stage of MapReduce. Enables sort-merge joins and grouping similar to MapReduce.
        - Take several inputs, partition them but skip the sorting.
        - For broadcast hash joins, output from one operator can be sent to all partitions of the join operator.
    - Several advantages over MapReduce:
        - Sorting needs to be performed only at places where it's actually necessary.
        - No unecessary map tasks since work done by a mapper can be incorporated into the preceding reduce operator
        - All joins and data dependencies in a workflow explicitely declared, the schedular has an overview of what data is required where so it can make locality optimizations.
        - Usually sufficient for intermediate state between operators in-memory or written to local disk which requires less I/O than writing to a distributed filesystem (where its replicated and written to disk for each replica)
        - Operators execute as soon as their input is ready, there is no need to wait for the entire preceding stage to finish before next one starts.
        - Existing JVM processes can be reused to run new operators reducing startup overheads compared to MapReduce.
    - Operators are a generalization of Map and Reduce, the same processing code can work in a dataflow engine: Pig, Hive or Cascading can be switched from MapReduce to Tez or Spark with a simple configuration change. 
    - Tex is a thin library relying on YARN shuffle service for actual copy of data between nodes
    - Spark and Flink are big frameworks that include their own network communications layer, scheduler, user-facing APIs.
    - Fault-tolerance: 
        - Spark, Flink and Tez don't write intermediate state to HDFS so they take a different approach: if a machine fails and intermediate state is lost, it is recomputed from other data that's availaible (prior intermediary stage or original input data normally on HDFS)
        - For this, the framework must keep track of how a given piece of data was computed, which input partitions it used and which operators were applied to it. Flink checkpoints operator state while allowing to resume running an operator that ran into fault during its execution.
        - It's important to know if a computation is deterministic: that is given the same input, it produces the same output especially in cases where lost data gets sent to downstream operators.
        - To avoid cascading faults, its better to make operators deterministic. Avoid iterating over a hash table, use random numbers generated from a fixed seed. These are a few ways to avoid non-determinism from creeping in.
    - Discussion of materialization:
        - Dataflow engines like Flink are like Unix pipes built on the idea of pipelined execution. That is to incrementally pass the output of an operator to other operators not waiting for the input to be complete before starting to process it.
        - Operators requiring sorting need to accumulate state temporarily. The last output record can be the one with the lowest key. But many flows can be execute in a pipelined manner.
        - The output needs to go somewhere durable so users find, so its written to distributed filesystem. Like with MapReduce, the inputs are immutable and output is completely replaced. The improvement over MapReduce is saving yourself writing all the intermediate state to the filesystem as well.

    ### Graphs and Iterative processing
    1. The need to do batch processing on offline graphs often comes in a context where the goal is to perform some kind of offline processing or analysis on an entire graph often in machine learning applications such as recommendation engines. 
    2. PageRank is a most famous graph analysis algorithm which tries to estimate the popularity of a web page based on what other pages link to it.
    3. Dataflow engines like Spark, Flink and Tez arrange operators in a job in a directed acyclic graph (DAG). The flow of data from one operator to another is structured as a graph while the data typically consists of relational-style tuples. This is different from graph processing where data itself has the form of a graph.
    4. Many graph algorithms are expressed by traversing one edge at a time, joining one vertex with an adjacent vertex in order to propagate some information and repeating until some condition is met (until there are no more edges to follow)
    5. A graph can be stored in a distributed filesystem (in files containing lists of vertices and edges) this idea of repeating until done can't be done in plain MapReduce which does a single pass through data. This is implemented in an iterative style
        - External scheduler runs a batch process to calculate one step of the algorithm. 
        - When the batch completes, the scheduler checks whether the iterative algorithm has finished based on the completion condition.
        - If the algorithm has not yet finished, the scheduler goes back to step 1.
    6. MapReduce does'nt account for the iterative nature of the algorithm, it will read the entire input dataset and produces a completely new output dataset.
    7. Pregel processing model
        - Bulk Synchronous parallel model of computation has become popular for batch processing graphs. It's also known as Pregel model.
        - Just as in MapReduce, mappers conceptually sends a message to a particular call of the reducer because the mapper collects together all mapper outputs with the same key, same is the idea with Pregel, one vertex can send a message to another vertex and typically those are sent along the edges of the graph.
        - The difference from MapReduce is that in Pregel, a vertex remembers its state from one iteration ot the next and so the function only needs to process new incoming messages. 
        - More or less similar to actor model in distributed actor framework except vertex state and messages between vertexes are fault-tolerant and durable and communication has a timing guarantee.
        - Message passing model makes it possible to batch them and improve the performance of Pregel. The only waiting between iterations is the prior iteration must completely finish and all of its messages should get sent before the next iteration can start.
        - Messages are guaranteed to be processed exactly once at their destination in the following iteraton. The framework transparently recovers from faults.
        - Fault-tolerance is achieved by periodically checkpointing the state of all vertices at the end of an iteration to durable storage. The simplest solution on node failure is to roll back the entire graph computation to restart at the last checkpoint.
        - Parallel execution:
            - Vertex does'nt need to know on which physical machine its executing, it sends messages to a vertex ID. 
            - Framework decides the partitioning of the graph and which vertex runs on which machine, which vertexes to colocate. Often, the graph is simply partitioned by an arbitrarily assigned vertex ID, making no attempt to group related vertexes.
            - Graph algorithms often have a lot of cross-machine overhead, intermediate state is bigger than the original graph. 
            - Therefore, if the graph can fit on a single computer, even a single threaded batch processing for a graph may be faster (GraphChi framework) than a distributed batch process.
            - Effectively parallelizing graph algorithms is an area of ongoing research.

### High-level APIs and languages: 
1. MapReduce engines have now become matured and robust enough to store and process petabytes of data on clusters of over 10,000 machines. Now, the attention has been turned to other areas: improving the programming model, improving the efficiency of processing, broadening the set of problems that these technologies can solve.
2. Higher-level languages and APIs like Hive, Crunch, Pig became popular because programming MapReduce jobs by hand is laborious.
3. These dataflow APIs use relational style way of building blocks to express a computation: joining datasets on some value, grouping tuples by key, filtering on some condition, aggregating tuples by counting, summing. These operations are implemented using the join and grouping algorithms.
4. These high-level interfaces require less code, also allow interactive use in which you write code incrementally in a shell and run it frequently to observe what its doing. This approach encourages experimentation.
5. High-level interfaces makes the system more productive and they also improve job execution efficiency.
6. The move towards declarative query languages:
    - The choice of join algorithms can make a big difference to the performance of a batch job and it is nice to not have to understand and remember the various join algorithms. 
    - Joins are specified in a declarative way, the application simply states which joins are required and query optimizer decides how they can be bst executed.
    - There is still a difference between MapReduce and SQL. MapReduce was built on the idea of function callbacks. A user defined function is called and that function is free to call arbitrary code to decide what to output. You can use a large ecosystem of existing libraries to do parsing, natural language analysis, image analysis, statistical/numerical algorithms.
    - There are also advantages to incorporating more declarative features besides joins in dataflow systems. For ex: If a callback function contains a simple filtering condition, there is a single CPU overhead to call the function on every record. If this filtering and mapping operations are expressed in a declarative way, query optimizer can take advantage of column-oriented layouts and read only required columns from disk.
7. Specialization in different domains:
    - Traditionally, MPP databases have served the need of business intelligence and business reporting but its just one among many domains in which batch reporting is used.
    - Statistical and numerical algorithms which are required for machine learning such as classification and recommendation systems are also important.
    - Reusable implementations are also emerging: Mahout implementing machine learning on top of MapReduce, Flink and Spark.
    - Algorithms like k-nearest neighbors which searches items that are close to given item in some multi-dimensional space (similarity search)
    - Genome analysis algorithms that try to find strings that are similar but not identical.
    - As batch processing systems gain built-in functionality and high level declarative operators and MPP databases became more programmable and flexible, the two are beginning to look moe alike. 