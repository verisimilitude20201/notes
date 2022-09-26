# Big Table - A Distributed storage system for structured data

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