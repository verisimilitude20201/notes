# Google File System (GFS)

## Introduction.
1. GFS is a scalable, distributed file system providing fault tolerance. 
2. It runs on inexpensive commodity hardware and delivers high aggregate performance for several clients.
3. The design has been heavily driven by application workloads and technological environment - both current and anticipated which is a marked departure from earlier file system assumptions
4. GFS shares many of the same goals as previous distributed file systems such as performance, scalability, reliability, and availability.
5. Certain radically different design choices 
    - Failures: Component failures are the norm and not exception. The file system consists of 100s of storage machines built from inexpensive commoditiy parts. The quantity and quality of components virtually guarantee that some are not functional at any given time and some may not recover. Constant monitoring, error detection, fault tolerance, automatic recovery are integral to the system.
    - Huge files: Huge, multi-GB files containing millions of web documents. It's unwieldy to deal with billions of files each in KBs size. Due to having huge files, design assumptions, I/O parameters such as block size have to be revisited.
    - Append-only: Most files are mutated by appending more data. Random writes are non-existent. Once written, files are only read sequentially. Appending becomes the focus of performance optimization and atomicity guarantees, while caching data blocks in the client loses its appeal.
    - Flexibility in codesigning application and it's file system: For example: Relaxed consistency model to vastly simplify the file system. Atomic append has also been introduced to ensure multiple clients can append concurrently to a file without extra synchronization.
6. One of the largest GFS cluster has about 1000 storage nodes over 300 TB of disk storage accessed by 100s of clients on distinct machines.

## Design Overview

### Design assumptioms
We revisit some of our earlier assumptions in detail
1. Built from commodity components that are guaranteed to fail. Constantly monitor
itself and detect, tolerate, and recover promptly from component failures on a routine basis is required.
2. Stores modest number of large files. Multi-GB files supported. No optization for very large number of small size files though they are supported. Few million files at-least 100 MB in size.
3. Primarily two kinds of reads, small random reads and streaming reads. Large streaming reads typically read contigous sections of the file. Random reads randomly read from certain offsets. Performance-conscious applications often batch and sort their small reads to advance steadily through the file.
4. Writes include large, sequential writes appending data to files. Once written, files are seldom modified again. Small writes at random positions are supported but need not be optimized.
5. Efficient semantics must be implemented for multiple clients to concurrently append to the same file. Atomicity with minimal synchronization overhead is essential. The file may be read later, or a consumer may be reading through the file simultaneously.
6. High sustained bandwidth is more important than low latency. Google applications place a premium on processing data in bulk at a higher rate. Certain other applications have stringent response time requirements.

### Standard Interface
1. Familiar file system interface although does not support POSIX APIs.
2. Files are organized into directories and organized into path names
3. Supports same create, delete, read, modify operations.
4. GFS supports record append and snapshot operations
    - Snapshot creates copy of a file or directory at low cost.
    - Record append allows multiple clients to append data to the same file concurrently while guaranteeing the atomicity of each individual client’s append.

### Architecture