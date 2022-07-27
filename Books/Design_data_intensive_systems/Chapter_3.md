# Storage and retrieval


## Introduction
1. On a most fundamental level, a database does two things. When you give it some data, it should store it. When you ask for it to be given back, it should retrieve it.
2. Why you should care of how the database stores the data is you need to select an appropriate storage engine for your application from among the many choices.
3. To tune a storage engine, you need to be aware of what a storage engine capable of doing under the hood.


## Data structures that power your database
1. The most simplest database can be implemented as two simple bash shell functions "get(key)" and "set(key, value)" doing just what their names indicate. It's basically a key-value store.
    - get(key) returns the most recent value saved against that key
    - set(key, value) sets a value against a key where the value can be anything
2. Underlying storage is a simple file with each line containing a key-value pair. Every call to set(key, value) just appends to this file. If the same key is used, you need to look for the latest occurence of that key to find the latest value.
3. Appending to a file is very efficient. The file is also called as a log an append-only file. The word "log" also refers to application logs but here it's meant to refer to an append-only sequence of records
4. The get(key) function has a terrible performance. It needs to read the entire file looking for the occurrences of the said key. In algorithmic terms it's O(n) where n is the number of key-value pairs
5. We need an index here which is an additional data structure to aid making the searching more efficient. It keeps additional meta-data that acts as a signpost that helps to locate the data. We may need several different indexes on different types of data and on different parts of the data. 
6. Maintaining additional index structures incurs additional overhead on the writes. Any kind of index slows down writes, because the index also needs to be kept updated. Well chosen indexes speed up reads but they slow down writes. For this reason, databases don't index by default but leave it to the application developer's knowledge of the access patterns to choose the indexes manually.

### Hash indexes
1. Index for key-value data.
2. Key-value stores are common to the dictionary type in most programming languages. Also called a HashMap.
3. Continuing on the previous example, our data storage is an append-only log file. We could keep an in-memory hash map where every key is mapped to a byte offset within that file. Whenever you append a key-value pair, you also update the hash map to reflect the byte offset of the key you just wrote (append a new key or update an existing key)
4. To find a key, read the byte offset from the in-memory map and seek to that offset.
5. This is what the Bitcask storage engine used in Riak does. It offers high performance reads subject to the requirement that all the keys fit in the availaible RAM. Values can reside on disk and can use more space because they can be read with just one disk seek. If that part of the data file is in file-system cache, no disk seek is necessary.
6. Bitcask is useful in the situations where the value of a key is updated too frequently. You have large number of writes but not too many distinct keys availaible.
7. To prevent from an out-of-disk space issue, we break the log into segments of a certain size. Stop making the appends to a segment file when it reaches a certain size and make subsequent writes to a new segment file.
8. We can then perform compaction on these segments. Compaction means throwing away duplicate keys and retaining only the most recent update for that key.

    a: 1                            a: 5
    b: 2                            b: 2
    c: 3                            c: 3 
    a: 4            ======>         d: 5 
    d: 5                            e: 7 
    e: 7
    a: 5
    f: 11* (Delete Tombstone)                           
9. We can merge several segments together while performing the compaction. So the segment file becomes smaller and number of segment files too reduces. The merging and compaction of segments are done in a background thread so while it happens we can still serve read requests from old segment files and write sements to the new file. After merging, we serve read requests from the new file and old files can be deleted.
10. Each segment now has it's own in-memory hash map of key to file offsets. To find the value of a key, we search the latest segment's map, then the second most recent segment and so on. Merging process keeps the number of segment files small as mentioned earlier.
11. Details that go in the implementation 
    - File format: Faster and simpler to use a binary format that encodes the length of string in bytes followed by raw string in bytes without any escaping.
    - Deletion: Deletion is handled by a appending a special deletion record called tombstone to the data file. During merging, tombstone tells the merging process to discard any previous values for the deleted key.
    - Crash recovery: If database restarts, the in-memory hashmaps are lost. Bitcask speeds up recovery by storing a snapshot of each segment's in-memory hashmap on disk which can be loaded into memory more quickly than regenerating the hashmap by scanning through all segment files
    - Partially written records: The database may crash at any time including half-way through appending a record to the log. Checksums allow such corrupted parts to be detected and ignored.
    - Concurrency: A common implementation choice is a single writer. File segments are append-only and immutable. Therefore, they can be read by read by several concurrent threads. 
    - Why append-only log is good
        - Appending and segment merging are sequential write operations much faster than random writes on magnetic spinnning disks and SSDs.
        - Concurrency and crash recovery becomes simple. 
        - Merging old data segments avoids fragmentation.
    -  Hash-table index disadvantages
        - The Hash-table must fit in memory. It's possible to maintain the hashmap on disk but it's inefficient to make it perform well, needs a lot of random I/O and handle hash collisions. 
        - Range queries are inefficient

### SSTables and LSM-trees
1. We can make a simple change to the format of our sequence files. We require that sequence of key-value pairs is sorted by key. 
2. Now, to this we cannot append key-value pairs to the segment immediately since writes can occur in any order. 
3. SSTables have advantages over hash indexes
    - Merging segments is simple and efficient even if the files are bigger than the availaible memory.
    - Uses the merge sort algorithm.
    - Look at the input files side-by-side, copy the lowest key according to sort order and repeat to an output file. This produces a new merged segment file sorted by key.
    - When multiple segments contain the same key, we can keep the values from most recent segment and discard the values in older segments. 
    - No need to maintain a mapping of all keys and file offsets in-memory
        - You need a sparse in-memory hash map. One key for every few KBs of the segment file because few KBs can be scanned quickly!
        - For example: If you are searching for the key handiwork and you know the offsets of handbag and handsome, you can start from handbag and scan sequentially from there.
    - It's possible to group records that need to be scanned in a range in a single block and compress it before it's written to disk. Each entry of the in-memory index points at the start of the compressed block.

### Constructing and maintaining SSTables.
1. So now our incoming writes occur in any order. 
2. Maintaining a sorted structure on disk is possible using B-Trees but maintaining it in memory is much more easier. Plenty of data structures you can use such as AVL trees, Red-black trees. With them, you can insert keys in any order and read them back in sorted order.
3. Our storage engine now works as follows
   - An incoming write gets written to an in-memory balanced tree (red-black tree). This is also called as a memtable.
   - When it gets bigger than a certain threshold, we write it out to disk in an SSTable file. This is efficient. The tree already maintains the key-value pairs sorted by key. 
   - The new SSTable file becomes the most recent segment of the database. While SSTable is being written to disk, writes can continue to a new memtable instance.
   - To serve a read request, first try to find the key in the most recent memtable, then in the next older, then in the next older and so on.
   - A merging and compaction process runs in the background to combine segment files and to discard overwritten or deleted values.
   - To avoid crashes, we can keep a separate log on disk to which every write is appended. That log is'nt sorted, it's only purpose is to restore the memtable after a crash. Every time the memtable is written out to an SSTable, this log is discarded. 
4. Making an LSM tree out of SSTables
    - The above storage engine is how LevelDB and RocksDB works. These are key-value storage engine libraries that are designed to be embedded into another applications. 
    - LevelDB can also be used in Riak as an alternative to Bitcask. Similar engines are used in Cassandra and HBase inspired by Google's BigTable.
    - Log-Structured-Merge-Tree building on earlier work on log-structured filesystems. Storage engines that are based on this merging and compacting of sorted files are called LSM storage engines. 
    - LSM Tree was described by Patrick O'Neil et. al in his paper.
    - Lucene (indexing engine for full-text search used in Elasticsearch) uses a similar method for storing term dictionary.
    - A full-text index is based on an idea that given a word in a search query, find all documents that mention the word. We can have a key-value structure. Key can be the word, and ID can be the list of all documents containing the word. Lucene maintains this mapping from term to documents in an SSTable like sorted files.
5. Performance optimizations
    - Reading per the described LSM tree algorithm can be slower. You need to look into the memtable, then the most recent SSTable, then the next-most recent one. Each SSTable needs to be read from disk. After reading everything i.e. memtable + all SSTables on disk, then only we can conclude that the given key is not availaible. 
    - Bloom filter is a memory efficient data structure for approximating the contents of a set. It can definitely tell you whether a key does'nt appear in the database thus saving many unnecessary disk reads for non-existent keys.
    - Size-tiered and level tiered are the compaction strategies adopted for compaction/merging. HBase/Cassandra uses them both. LevelDB & RocksDB support only levelled.
    - Size-tiered compaction merges newer and smaller SSTables into older and lerge ones.
    - Leveled compaction splits the key-range into multiple smaller SSTables and older data is moved into separate levels allowing compaction to proceed incrementally and use less disk space. 
    - Even when dataset is much larger than availaible memory, you can efficiently perform range queries. LSM writes are sequential so they can support remarkably high write throughput.

### B-Trees
1. At a glance
    - Most widely used indexing structure
    - Introduced in 1970s.
    - Standard index implementation in all RDBMS and some non-relational databases too. 
    - Keeps key-value pairs sorted by key and allows efficient key-range lookups. 
2. B-trees break the database into fixed-sized nodes/blocks/pages, traditionally 4 KB in size and read/write one page at a time. This design closely corresponds to the underlying hardware. 
3. Pages can be identified using an address which allows one page to reference another on disk. Something similar to a pointer we can use these page references to construct a tree of pages. 
4. One page is the designated root of the tree and all scans to look up a key starts here. 
5. Each child is responsible for a continous range of keys and the keys between the references indicates where the boundaries of the key lie.

        100    |     200   |      300    |
               |           |
              _       
             |
   10   20   30  

6. So we start from the root and depending on the key's range keep following the page references for eg 251 key lies between 200 - 299. Eventually, we reach a page containing individual keys (leaf node) which either contains a the value for each keey inline or contains references to the pages where those values can be found.
7. Branching factor is the number of references to child pages in one page of the tree. In practise this factor is in several hundreds. It depends on the amount of space required to store page references and range boundaries.
8. For updating the value of a key, find that key and it's page, update the value and write it back to disk. 
9. For adding a new key, first we need to find the page whose range encompasses the key and add it there. If there is no space, we split the page into two half-full pages and parent page is updated to account for the new division of key ranges. This ensures that the tree always remains balanced. A B-tree with n keys has depth of O(log n)
10. Most databases fit into a B-tree that's 3-4 levels deep. A 4-level tree with a branching factor of 500 can store upto 250 TB.
11. Making B-trees reliable:
    - Basic underlying write operation of a B-tree is to overwrite a disk page with new data. Overwrite does'nt change the location, all references to that page remain intact. 
    - LSM trees never modify files in-places, its always append only and afterwards compaction/merging takes care of retaining just latest values. 
    - Overwriting a page can be thought of as an actual hardware operation. A magnetic hard disk moves the disk head to the right sector and track to come under the spinning platter. SSDs must erase and rewrite fairly large blocks of a storage chip.
    - Some operations can have several pages updated in one go which is dangerous. If during a page-split operation, while updating the parent page references, the DB crashes, we end up with a corrupted index (orphan pages)
    - WAL is an append-only data structure to which every B-tree modification must be written before it can be applied to the pages of the tree itself. When the database comes up this log is used to restore the B-tree to a consistent state.
12. One complication of updates in place is careful concurrency is required if multiple threads are going to access the B-tree at the same time to avoid inconsistency. This is done by latches (lightweight locks). 
13. LSM is simpler, all mergin is done in the background without interfering with incoming queries and atomically swapping old segments for new segments. 

#### Few B-tree optimizations
1. Copy-on-write: LMDB use a copy-on-write scheme. A modified page is written to a different location and a new version of the parent pages in the tree is created pointing at the new location.
2. Save space in pages by abbreviating the key. The key needs to only provide enough information to act as the boundary between key-ranges. 
3. If a query needs to scan a large part of the key-range, page-by-page layout can be inefficient since a disk seek is required for every page. B-tree implementations therefore try to layout the pages in a sequential manner which becomes difficult as more keys are added. Sequential layout is simpler for LSM trees.
4. Additional pointers included in leaf pages to have references to it's sibling pages to the left and right to ease the scan of keys.
5. B-tree variants called fractal trees borrow some LSM ideas to reduce disk seeks.

### Comparing B-tree with LSM trees
1. B-tree implementations are more mature than their LSM counterparts
2. LSM-trees are faster for writes whereas B-trees are faster for reads. For LSM trees, they have to check several different structures at different stages of compaction and SSTables so reads are slower. 
3. Benchmarks are inconclusive and sensitive to the details of the workload. 
4. Advantages of LSM Trees
    - B-tree index writes every piece of data at-least twice - one to the WAL, second to the tree page, third if there is a page-split. Can also overwrite an entire page at times even if first few bytes have changed. 
    - Due to merging and compaction, log structured indexes too rewrite data multiple times. One write to the database results in multiple writes to the underlying disk is called write amplification. Particularly of concern on SSDs which can overwrite blocks only a limited number of times before wearing out.
    - For write heavy applications, the performance bottleneck might be the rate at which the database can write to disk. Write amplification has a direct performance cost. The more a storage engine writes to disk, the fewer writes per second it can handle within the availaible disk bandwidth
    - LSM trees can sustain a higher write throughput that B-trees because they sometimes have lower write amplification (depending on storage engine configuration & workload) and partly because they sequentially write compact SSTable files rather than overwriting several pages in the tree. Quite imporant on magnetic hard drives where sequential writes are quite faster. 
    - LSM trees can be compressed better to produce smaller files on disk. B-trees do leave some space unused due to fragmentation. LSM trees are not page-oriented and periodically rewrite SSTables to remove fragmentation.
    - Lower write amplification and reduced fragmentation are important on SSDs as well. They allow representing data more compactly to allow more read/write requests within the availaible I/O bandwidth
3. Downsides of LSM trees
    - Compaction can sometimes interfere with the performance of reads/writes that are ongoing
    - Though compaction is performed incrementally by storage engines and without affecting concurrent access, disk have limited resources so at higher writes rate, a read request may need to wait while the disk finishes an expensive compaction operation. 
    - B-tress can be more predictable as compared to LSM trees when it comes to read response time for queries.
    - At high write throughput, the disk's finite write bandwidth needs to be shared between initial write and compaction threads running in background. Bigger the database, more disk bandwidth is required for compaction. 
    - If write throughput is high and compaction is not configured properly, compaction cannot keep up with the rate of incoming writes. The number of unmerged segments keeps on increasing until you run out of disk space and reads also become slower.
    - You need explicit monitoring to detect the situation of compaction being unable to keep up. There is no SSTable based throttling.
    - For B-trees, a key exists in exactly one place. LSM storage engine stores multiple copies of a key at different segments. B-trees are great for transactional semantics with isolation being implemented as a lock on a range of keys. Those locks can be directly attached to the tree. 
    - No quick and easy rule for determining which storage engine is better. You should test for your own use-cases.

### Other indexing structures
1. Secondary index: 
    - Secondary indexes are very common as primary keys. 
    - In a secondary index, the index values are not necessarily unique, many rows or documents with the same entry. 
    - We can make each value in the index as a list of matching row identifiers or making each index unique by appending a row identifier to it.
    - Both B-Tree and LSM indexes can be used as secondary indexes
2. Storing values within the index
    - Queries search for key. A value can be the actual row(document, vertex, tuple in relational DB) or it can be a reference to a row stored elsewhere. Elsewhere means a heap file which stores data in no particular order (may be appedn-only order or may keep track of deleted rows to cleanse them later.)
    - Heap file approach avoids duplicating data, each location in index just references a location in the heap file.
    - While updating the value without changing the key, the heap file approach is efficient, rewrite the record in-place provided the new value is not larger than old value. If it's larger, it probably needs to be moved into a new location in the heap where there is enough space. In that case, we either may add a forwarding pointer to this new location or all indexes need to be updated to this new location.
    - To avoid extra loop, from index to heap file, we may store the indexed row directly within the index. This is known as clustered index. For MySQL's InnoDB engine, the primary key is always a clustered index and secondary key refer to the primary key.
    - A covering index stores only some of a table's columns within an index allowing us to answe some queries using index alone. In which case, the index is said to cover the query.
    - Clustered and covering indexes speed up reads but they require additional storage and add overhead on writes
3. Multi-column indexes: 
    - Multi-column indexes are used incase you want to query documents/rows using one or more fields simultaenously. 
    - Concatenated index simply combines several fields into one key with the index definition defining in which order the fields are concatenated. This index is useful only when all columns in the index are used in the query.
    - Multi-dimensional indexes are a more general way of querying several columns at once. When a user is searching for a restarant on Zomato, the app uses his latitude and longitude to locate restaurants nearby his location. This requires a multi-column range query
    ```
    SELECT * FROM restaurants 
    WHERE latitude BETWEEN 51.4946 AND 51.5079 
    AND longitude BETWEEN -0.1162 and -0.1004
    ```
    - A standard LSM/B-tree tree may not answer that question, it cannot use both indexes simulatenously. We can use a function to translate the 2-D location into a space-filling curve and then use a regular B-tree index. Spatial indexes like R-tree also can be used. 
    - You can even use a multi-dimensional index in case of an e-commerce website that allows searching products based on color value RGB. 
    - You can even have a multi-dimensional index on date and temperature to efficiently search all observations during the year 2013 where the temperature was between 25 to 30 degrees. This technique is used by Hyperdex.

4. Full-text and fuzzy indexes: 
    - Indexes mentioned so far assume you have the exact value to have an exact match or a range of values to do a range query. 
    - Full-text/fuzzy indexes allow to search for similar keys, misspelt words. 
    - Full text search engines commonly allow  synonyms, ignore grammatical variations of a word and to search for occurences of words near each other in the same document. 
    - Lucene is able to search text for words within same edit distance. An edit distance of 1 means one letter has been added, removed, replaced.
    - Lucene uses a SSTable like structure for it's term dictionary. This requires a small in-memory index that tells queries at which offset in the sorted file to look for the query. This in-memory index is a finite state automaton over the characters of the keys. This can be tranformed into the Levenshtein automaton which supports efficient search for words. 
    - Other fuzzy search techniques go in the direction of document classification and machine learning.

5. Keeping everything in memory
    - Above data structures to store data are answers to the limitations of disks
    - Data on disk needs to be laid out carefully if you want good performance on reads and writes. But disks have two significant advantages - they are durable and they have a lower cost per GB than RAM.
    - Many datasets are not that big so it's feasible to keep them entirely in memory potentially distributed across several machines. This led to the development of in-memory databases.
    - Caches are a good use-case for in-memory databases where it's acceptable for data to be lost even if a machine is restarted.
    - Durability can be achieved with special hardware such as battery powered RAM, log of changes to disk, periodic snapshots, replicate in-memory state to other machines. 
    - On restart, it needs to reload it's state either from disk or over the network from a replica. Disk is just used as an append-only log for durability, all reads happen from data in memory
    - VoltDB, MemSQL and Oracle TimesTen are in-memory DBs with a relational model. RAMCloud is an open-source in-memory key-value store
    - In-memory DBs are faster because they can avoid the overhead of encoding in-memory data-structures in a form that can be written to disk. 
    - In-memory DBs provide data models such as Sets, priority queues, sorted sets in Redis that are difficult to implement with disk-based structures. 
    - In-memory DBs can also be extended to store data larger than than availaible memory. Anti-caching approach works by evicting the least recently used data from memory and loading it back when accessed similar to what an OS does with virtual memory and swap files.Database does it at the granularity of pages. 
    - Further changes to storage engine design will be if non-volatile memory NVM technologies become more widely adopted.

## Transaction processing