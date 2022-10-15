# Big Table - A Distributed storage system for structured data 

# Current Page 12.

## Introduction
1. Big Table is a distributed storage system designed to store structured data capable of scaling upto petabytes stored on commodity servers. 
2. Google products (Google maps, finance, Google earth) place varied demands of data size (URLs, web pages to satellite imagery), latency (real time or backend data processing)
3. Still Big table has proved to be a high performance solution for all these different needs and has worked successfully.
4. Bigtable is designed to reliably scale to petabytes of data and thousands of machines
5. Bigtable has achieved several goals: wide applicability, scalability, high performance, and high availability
6. Bigtable is used by more than 60 different products at Google for a variety of demanding workloads, which range from throughput-oriented batch-processing
jobs to latency-sensitive serving of data to end users.
7. The Bigtable clusters used by these products span a wide range of configurations - from several hundred thousand nodes storing petabytes of data. 
8. Bigtable supports a simpler data model than relational model (no support for relational model). It supports dynamic control over data layout and format. Allows
clients to reason about the locality properties of the data represented in the underlying storage. 
9. Data is indexed using row and column names that can be arbitrary strings. It treats data as uninterpreted strings although clients often serialize various forms of structured and semi-structured data into these strings.
10. Bigtable schema parameters let clients dynamically control whether to serve
data out of memory or from disk.

## Data model
1. It's like a sparse, distributed, multi-dimensional sorted map. The map is indexed by a row key, column key and a timestamp. Each value in map is an uninterpreted sequence of bytes.
    (row_key: string, col_key: string, timestamp:int64) -> bytes

2. This data model was derived after the analyis of several different use cases and their data at Google 
3. Suppose we want to keep a copy of a large collection of web pages and related information that could be used by many different projects; let us call this particular table the Webtable.
 Row_key              Content                   anchor:cnn.com  anchor: cnn.eu    
         
com.cnn.www --->     <<html>> t0               CNN t4             CNN t5
                     <<html>> t1
                     <<html>> t2   

The row name is a reversed URL. The contents column family contains the page contents, and the anchor column family contains the text of any anchors that reference the page. Each anchor cell has one version; the contents column has three versions shown as timestamps.

### Rows
1. The row keys in a table are arbitrary strings (curren tly up to 64KB in size, although 10-100 bytes is a typical size.
2. Every read or write of data under a single row key is atomic. A design decision that makes it easier for clients to reason about the system's behavior in the presence of concurrent updates to the same row.
3. Data is maintained in lexicographic order by row key. Each row range is called a tablet, which is the unit of distribution and load balancing. 
4. Reads of short row ranges are efficient and involve communication with only a small number of machines. Clients can exploit this property by selecting their row keys so
that they get good locality for their data accesses
5. For the Web table example, pages in the same domain are grouped together into contiguous rows.

### Column families
1. Column families are sets of column keys grouped together. They form the basis of access control. 
2. Usually, the data in each column family is of same type so it can be stored compressed.
3. Column family must be created before data can be stored. 
4. Even though a table may have unbounded number of columns it's recommended to keep the number of column families in a table small.
5. A column key is named using the following syntax: family:qualifier. A family name must be printable but qualifier can be any arbitary string. For Web table example, anchor is a column family and each unique anchor key is a column key.
6. Access control and both disk and memory accounting are performed at the column-family level. These controls allow us to manage several different types of applications: some that add new base data, some that read the base data and create derived column families and so on.

### Timestamp
1. Each cell in Bigtable contains multiple versions of same data.
2. Timestamps are 64-Bit and can be assigned by BigTable or by the client while inserting. Most recent versions of a cell are read first.
3. The client can specify either that only the last n versions of a cell be kept, or that only new-enough versions.
4. For our Webtable example, we instruct Bigtable to store last 3 versions for any cell.

### API
1. API provides support for creating and deleting tables and column families.
2. It also provides functions for changing cluster, table and column family metadata such as access control rights.
3. Clients can read/write values in Bigtable, lookup values from individual rows, iterate over a subset of data in a table. 
4. Clients can iterate over several column families and there are several ways to limit the rows, colums and timestamps produced. For example: For Webtable, we could restrict the scan to produce only achors whose columns match the regex achor:*.cnn.com.
5. Single row transactions are supported which can be used to perform atomic read-modify-write sequences stored under a single row key.
6. Writes across row keys can be batched. 
7. Bigtable allows cells to be used as integer counters.
8. Client supplied scripts (written in a Google internal language called Sawzall) are allpwed to execute in the address space of the servers. The scripts allow various forms transformations, filtering and summarizations. 
9. Bigtable can be used with MapReduce a framework for large scale computations developed at Google.


## Building blocks
1. Bigtable uses Google File system GFS for storing log and data files. 
2. A Bigtable cluster operates in the same shared pool of machines on which other distributed applications are hosted. It relies on a cluster management system for scheduling jobs, monitoring machine status, managing failures. 
3. SSTable
    - Google SSTable format is used to store Bigtable data internally.
    - SSTable provides an immutable, ordered key-value map where both key and value can be arbitrary byte strings.
    - We can lookup by a key, iterate over a key-value range 
    - Each SSTable consists of a sequence of 64 KB blocks. A block index at the end of the SSTable is used to locate the blocks. It's loaded in memory along with the SSTable.
    - Lookup can be performed with a single disk seek: Binary search on the in-memory index and load appropriate blocks from disk. 
    - Optionally, an SSTable can be completely mapped into memory.
4. Chubby: BigTable uses Chubby for variety of tasks
    - Ensure that that there's one active master
    - Store bootstap location of Bigtable data. 
    - Store Bigtable schema information 
    - Store access control lists
5. If Chubby becomes unavailaible for extended time periods, it also affects Bigtable.

## Implementation
1. Bigtable has 3 major components
    - Client library
    - Master server: Assigns tablets to tablet servers, detects additions and expirations, garbage collection of files in GFS
    - Many tablet servers: Can be dynamically added or removed from a cluster to accomodate changes to workloads. Each tablet server maintains a set of tablets. It handles the read/write requests to the tablet and also splits tablets that are too large.
2. Client directly communicate with the tablet servers for reads/writes. Bigtable clients don't relie on the master server for tablet location information. So masters are lightly loaded. 
3. A Bigtable cluster stores many tablets, each table consists of a set of tablets and each tablet contains all data associated with a row. 

### Tablet Location
1. A 3-level hierarchy analogous to B+-tree is used to store tablet location info
2. Chubby stores the first level containing the location to the root tablet.
3. Root tablet contains the location of all tablets in a METADATA tablet. Each METADATA tablet contains location of a set of user tablets. Root tablet is never split to ensure the 3-level tablet location hierarchy has no more than 3 levels
4. METADATA stores the location of a tablet under a row key that's an encoding of the  tablelet's table identifier followed by it's end row. Each metadata row can store 1 KB of data in memory.
5. Tablet locations are cached in the client library. If client does not know the location of the tablet or if it discovers that the cached information is incorrect, it recursively moves up the tablet location hierarchy. This could take 6 round-trips in case of stale client cache because stale data is discovered only on misses. Could reduce the cost by having client library prefetch the tablet locations.
6. There is also a logging of events pertaining to each tablet in the  METADATA table that's useful for debugging and performance analysis.


### Tablet assignment
1. A tablet is assigned to one tablet server at a time
2. Master keeps track of live tablet servers and including which tablets are unassigned. 
3. For an unassigned table and when a tablet server with sufficient room is availaible, the master assigns the tablet sending a tablet load request to the tablet server
4. When a tablet server starts it acquires a exclusive lock on a uniquely named file in a specific Chubby directory. The master monitors this specific directory to discover servers. 
5. If the tablet server looses this exclusive lock, it stops serving tablets. It tries to acquire the exclusive lock on its file as long as the file exists. When it terminates, the lock is reelased so that the master will reassign the tablets more quickly.
6. The master periodically checks with each tablet server for the status of its lock. If the tablet server has lots its lock, the master tries to acquire the lock. 
7. If it is successful in acquiring the lock, Chubby is live and tablet server is either dead or having trouble reaching chubby. The master then deletes the tablet server's file to ensure that it no longer can server again. 
8. The tablet server's tablets are then moved into the set of unassigned tablets. 
9. The following steps are executed by master at start-up
    - The master grabs a unique master lock to prevent concurrent master setups
    - Scans the servers directory to find live servers
    - Communicates with every live server to discover what tablets are assigned to each server. 
    - Scans the METADATA table to learn the set of tablets. On encountering a tablet not already assigned, its assigned to the set of unassigned tablets. 
10. Before starting the METADATA scan, it must ensure that METADATA tablets have been assigned. It adds the root tablet to the set of unassigned tablets. The root tablet contains the names of all metadata tablets.
11. The set of existing tablets only changes when a table is created or deleted, two existing tablets are merged to form one larger tablet, or an existing tablet is split into two smaller tablets. The master inititates all but the last one.
12. The tablet split is recorded in the METADATA table for the new tablet by the tablet server


### Tablet Serving
1. Persistent state is stored in GFS. 
2. Writes are commited to a commit log storing redo records. 
3. Recent commited ones are stored in a sorted buffer in memory called memtable. Older updates are stored in a sequence of SSTables
4. Commit log is used for recovery. 
5. When a write arrives at a tablet server, the server checks if its well-formed and that the sender is authorized to perform the mutation. Chubby stores a list of permitted writers for authorization. A write is commited into the commit log and then to a memtable.
6. A read operation is similarly checked for well-formedness and proper authorization. It's executed on a merged view of SSTables and memtable. We can use binary search on the merged view since its lexicographically sorted. 
7. Incoming read/write operations can continue while tablets are split and merged. 


## Refinements
A number of refinements were done to accomplish the high availaibility, performance and reliability. 

### Locality Groups
1. Clients can group multiple column families together into a locality group. Separate SSTable for each locality group in each tablet. 
2. We can segregate column families not read together into different locality groups to have more efficient reads. For Webtable, page metadata in Webtable can be in one locality group and the contents of the page can be in a different group
3. We can also have some tuning parameters per locality group. For example: Locality groups can be declared in-memory. SSTables for in-memory locality groups are loaded lazily into the
memory of the tablet server. This is useful for small pieces of data accessed frequently.

### Compression
1. Clients can control whether SSTables for a locality group can be compressed and the compression format. 
2. User-specified compression can be applied to each SSTable block. The benefit we have is small portions of the SSTable can be read without decompressing the whole file.
3. For actual compression, a two pass compression scheme is used. Speed is emphasized instead of space reduction. 
    - First pass uses Bentley and Mcllroy's schema compressing long common strings across a large window.
    - Second pass uses a fast compression algorithm looking for repetitions in a small 16KB window of the data.
4. The scheme achieved a 10-to-1 reduction in space in case of the Webtable example. This is because of the way Webtable rows are laid out, all pages from same host are stored close to each other.
5. Compression ratios get even better when we store multiple versions of the same value in Bigtable.

### Caching for read performance

1. Tablet servers use two types of caches
    - Scan cache is a higher level cache that catches the key-value pairs returned by the SSTable interface to the tablet server code. It is useful for application that want to read the same data repeatedly
    - Block cache is useful for applications that tend to read the data close to the data they recently read. It caches SSTable blocks read from GFS.

### Bloom filters
1. Bloom filters exist to minimize the number of disk accesses to read a value from multiple SSTables.
2. A Bloom filter allows us to ask whether an SSTable might contain any data for a specified row/column value pair.
3. A small amount of tablet memory used for storing Bloom filters drastically reduces the number of disk seeks required for read operations.
4. For non-existent rows or columns, due to use of Bloom filters again there are no disk accesses.

### Commit log implementation 
1. Mutations are appended to a single log file per tablet server, co-mingling mutations for different tablets in the same physical log. This is to avoid a larger number of concurrent writes in GFS and large number of disk seeks to multiple log files.
2. Single log file simplifies implementation but complicates recovery. When a tablet server dies, the tablets it served will be moved to other tablet servers. 
3. One approach would be to read the commit log for the original server and reapply the entries needed for the tablets that it needs to recover. If 100 machines were assigned a single tablet from the failed tablet server, the whole log file would be read 100 times. 
4. Commit log is sorted by (table, row name, log sequence number). In the sorted output all mutations for a tablet are contiguous and can be read efficiently by one sequential read. 
5. Writing commit logs to GFS causes peformance hiccups. To protect mutations from GFS latency spikes, each tablet servers has two log writing threads. Only one is active. If writes to the active log file are performed poorly, the log file writing is switched to another thread and the mutations that are in commit log queue are written by the newly active writing thread. 

### Speeding up tablet recovery
1. When the master moves tablets from a source to target tablet server
2. The source tablet does compactions on the tablets to reduce the recovery time. After this, it stops serving the tablet.
3. Before it actually unloads the tablet, it does another minor compaction to removed any uncompated state that arrived while the first compaction was being performed. 
4. After this, the tablet can be loaded on another tablet server without requiring any recovery of log entries.

### Exploiting immutability
1. All generated SSTables are immutable. No synchronization of accesses to the file system when reading from SSTables and so concurreny control becomes very simple.
2. Memtable is the only mutable structure between the readers and the writers. 
3. Memtable is made copy-on-write to reduce read contention and both readers and writers can access it in parallel.
4. Obsolete SSTables are GCed. Each tablet's SSTables are registered in the METADATA table. The master removes obsolete SSTables as a mark-and-sweep garbage collection over the set of SSTables.


## Performance Evaluation
1. The sequential write benchmark used row keys with names 0 to R 􀀀 1. This space of row keys was partitioned into 10N equal-sized ranges. These ranges were
assigned to the N clients by a central scheduler that assigned the next available range to a client as soon as the client finished processing the previous range assigned to it.
2. A single random string was written under each row key. Each string was randomly generated and so incomprehensible.
3. The random write benchmark was similar except that the row key was hashed modulo R immediately before writing so that the write load was spread roughly uniformly across the entire row space.
4. The sequential read benchmark generated row keys in exactly the same way as the sequential write benchmark, but instead of writing under the row key, it read the string stored under the row key.
5. Random read was similar to the random write benchmark.
6. Scan benchmark supports the scanning of a range of rows using BigTable API.
7. Single Tablet Server Performance:
    - Random reads are slower than all other operations by an order of magnitude or more. Each random read involves the transfer of a 64 KB SSTable block over the network from GFS to a tablet server, out of which only a single 1000-byte value is used.
    - Random reads from memory are much faster since they are served from local memory.
    - Random and sequential writes perform better than random reads since each tablet server appends all incoming writes to a single commit log and uses group commit to stream these writes to GFS.
    - Sequential reads perform better than random reads since every 64 KB SSTable block that is fetched from GFS is stored into our block cache, where it is used to serve the next 64 read requests.
    - Scans are even faster since the tablet server can return a large number of values in response to a single client RPC.
8. Aggregate throughput
    - Aggregate throughput increases dramatically, by over a factor of a hundred, as we increase the number of tablet servers in the system from 1 to 500.
    - Performance does'nt increase linearly as we increase the number of tablet servers
    - For most benchmarks, there is a significant drop in per-server throughput when going from 1 to 50 tablet servers. This drop is caused by imbalance in load in multiple server configurations, often due to other processes contending for CPU and network
    - Load balancing algorithm attempts to deal with this imbalance but cannot do so for two reasons 
        - Rebalancing is throttled to reduce the number of tablet movements.
        - Load generated by our benchmarks shifts around as the benchmark progresses.


## Real Applications
1. Google Analytics
    - It's a service that helps webmasters analyze traffic patterns at their web sites. It provides aggregate statistics, such as the number of unique  visitors per day and the page views per URL per day, as well as site-tracking reports, such as the percentage of users that made a purchase
    - Webmasters embed a small JavaScript program in their web pages. This program is invoked whenever a page is visited. 
    - It records various information about the request in Google Analytics, such as a user identifier and information about the page being fetched.
    - The raw click table (200 TB) maintains a row for each end-user session. The row name is a tuple containing the website's name and the time at which the session was created. This schema ensures that sessions that visit the same web site are contiguous, and that they are sorted chronologically.
    - The summary table (20 TB) contains various predefined summaries for each website. This table is generated from the raw click table by periodically scheduled MapReduce jobs. Each MapReduce job extracts recent session data from the raw click table
2. Google Earth:
    - Google operates a collection of services that provide users with access to high-resolution satellite imagery of the world's surface
    - These products allow users to navigate across the world's surface: they can pan, view, and annotate satellite imagery at many different levels of resolution.
    - The preprocessing pipeline uses one table to store raw imagery. During  preprocessing, the imagery is cleaned and consolidated into final serving data. This table contains approximately 70 terabytes of data and therefore is served from disk
    - Each row in the imagery table corresponds to a single geographic segment. Rows are named to ensure that adjacent geographic segments are stored near each other. The table contains a column family to keep track of the sources of data for each segment. This column family has a large number of columns: essentially one for each raw data image.

3. Personalized Search:
    - Is an Opt-in service that records user queries and clicks across a variety of Google properties such as web search, images, and news. 
    - Users can browse their search histories to revisit their old queries and  clicks, and they can ask for personalized search results
    - Stores each user's data in Bigtable. Each user has a unique userid and is assigned a row named by that userid.
    - All user actions are stored in a table. A separate column family is  reserved for each type of action (for example, there is a column family that stores all web queries).
    - Each data element uses as its Bigtable timestamp the time at which the corresponding user action occurred
    - Personalized Search generates user profiles using a MapReduce over Bigtable which is used for personalizing search results.
    - The table is now used by many other Google products that store per user configuration options and settings. A quote has been set for each Google Product using the store 

## Lessons
1. We learned is that large distributed systems are vulnerable to many types of failures, not just
the standard network partitions and fail-stop failures assumed in many distributed protocols. For example: memory and network corruption, large clock skew, hung machines, extended and asymmetric network partitions,
bugs in other systems that we are using (Chubby for example), overflow of GFS quotas, and planned and unplanned hardware maintenance.
2. Some problems were handled by changing protocols (like RPC checksumming) while some others were handled by changing the assumptions made by one part of the system of another part (we stopped assuming a given Chubby operation could return only one of a fixed set of errors) 