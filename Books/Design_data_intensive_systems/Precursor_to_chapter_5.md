# Distributed Data

1. We move up a level from data when stored in a single machine. We now ask what happens if multiple machines are involved in storage and retrieval of data? 
2. Various reasons to distribute a database across several machines
    - Scalability: If the data volume grows beyond what a single machine can handle, you spread the load across machines.
    - High availaibility / Fault tolerance: If the applications needs to continue working even if several machines go down, you can use multiple machines to give you redundundancy. When one fails, other can take over.
    - Latency: Having various servers located world-wide so that users local to a region to be geo-redirected to the nearest DC. It avoids network packets to travel all the way along.

3. Scaling to high load 
    - Shared Memory: 
        - Simplest approach - buy the most powerful hardware. Club CPUs, RAM chips and disks under one OS with fast interconnect. 
        - In this kind of shared memory architecture, all components can be treated as a single machine. This is called vertical scaling.
        - The cost of such a machine increases linearly - a machine with twice RAM, disk, CPUs costs twice as much. They have limited fault tolerance. They have hot-swappable components (can replace CPU, RAM without shutting down the machine) but it's limited to a single geographical location.

    - Shared Disk architecture
        - Several machines with independent CPUs and RAMs stored on an array of disks that is shared between the machines which are connected with the storage via a fast network
        - Limited for some data warehousing workloads but contention and overhead of locking limits scalability.
    
    - Shared Nothing architecture (Horizontal Scaling):
        - Each machine running the database software is called a node. Each machine uses it's CPUs, RAM, disks independently. 
        - Any coordination between nodes is done at the software level using a conventional network. 
        - No special hardware required, you can use whatever machines have best price-to-performance ratio. 
        - Distribute data across multiple regions and thus reduce latency and may potentially survive the loss of a continent. 
        - With cloud deployments of virtual machines, one need not operate at Google scale, even for smaller companies a multi-region cluster is now feasible.
        - If your data is distributed across multiple nodes, you need to be aware of the constraints/trade-offs. 
        - It does have additional complexities for applications and limits expressiveness of the data models that you can use. A single threaded program can perform better that a cluster with over 100 CPU cores. 
        - On the other hand, Shared nothing applications can be very powerful.

4. Partitioning Vs Replication
    - Replication: Keeping the same copy of data on several different nodes in different locations. Replication provides redundancy, if certain nodes are unavailaible, data can still be served from other nodes. 
    - Partitions: Splitting a big database into smaller subsets so that different partitions can be assigned to different nodes. Also called sharding.