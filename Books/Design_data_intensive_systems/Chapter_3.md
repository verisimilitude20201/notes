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
    - Crash recovery
