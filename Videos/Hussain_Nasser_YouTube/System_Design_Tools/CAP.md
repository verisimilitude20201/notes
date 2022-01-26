# My thoughts on CAP theorem

1. CAP stands for Consistency, Availaibility and Partition Tolerance
    - Consistency: If I write something, and read it back, the change should be reflected
    - Availaibility: If you want to issue a write/read, it succeeds always.
    - Partition tolerance: Network glitches splitting the nodes in a network into distinct network partitions.

2. Say one master node and two secondary read nodes. Master node takes in all the writes. 
    - AP: We can immediately update the replicas or update it in the background. In the first case, the system return successfully after master node commits it. You cannot gurantee consistency in this scenario but only availaibility. 
    - CP: Second option is wait till the commited write is synced to the two replicas. After the operation completes successfully on all replicas, the operation is not successful. In case of a network glitch, we can fail the write or we can choose to retry until we eventually succeed but you're still not availaible.
    - CA: If you can guarantee if you can't tolerate network partitions at all. Especially in case of a single machine.

3. Where a CAP theorem applies depends on which component of the system you zoom on.
4. Consistency in ACID describes the state of a database system, that each DB operation should move the state of the system from one consistent state to the other.

3. Thus we can either get a consistent write or get a highly available system to issue the read/write operations.