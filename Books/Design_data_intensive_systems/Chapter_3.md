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
    