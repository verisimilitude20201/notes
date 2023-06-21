# The Trouble with Distributed Systems

## Introduction
1. Working with distributed systems is fundamentally different from writing software that works on a single computer. 
2. There are lots and lots of new, exciting ways for things to go wrong.
3. Our task as engineers is to build the systems that do their job inspite of everything going wrong. 
4. Let's see what challenges are we up against.

## Faults and partial failures 
1. A program on a single computer either it works or it does'nt. 
2. There is no fundamental reason of why a software running on a single computer should be flaky. When the hardware is working correctly, the same operation always produces the same result. i.e deterministic. 
3. An individual computer with good software is either fully functional or entirely broken but nothing in between.
4. This is a deliberate choice in the design of computers. If an internal fault occurs, we expect the computer to crash completely rather than returning a wrong result.
5. Computers hide the fuzzy phyical reality on which they're implemented on and present an idealized system model operating with mathematical precision.
6. In distributed systems, we are no longer operating in an idealized system model, we have no choice but to confront the messy realities. 
7. In a distributed system, there may be some parts of the system that are broken in some unpredictable way even though the other parts of the system are working fine. These are partial failures. They are non-deterministic.
8. This non-determinism and possibility of partial failures makes distributed systems hard to work with.

### Cloud computing and Supercomputing
1. Large spectrum of philosophies on how to build large-scale computer systems
    - High-Performance computing: Super computers with thousands of CPUs are typically used for computationally intensive scientific computing tasks such as weather forcasting
    - Cloud computing is associated with multi-tenant data centers, commodity networks connected with an IP network, elastic on-demand resource allocation and metered billing.
    - Traditional enterprise datacenters lie somewhere between these extremes.
2. Differing philosophies have different approaches to handle faults. 
3. For a supercomputer, a job typically checkpoints the state of its computation to durable storage. If one node fails, the common solution is to stop the entire cluster workload. After the faulty node is repaired, the computation is restarted from the last checkpoint. Thus a supercomputer escalates partial failure to total failure more like a single node system.
4. We focus primarily on internet services which differ from supercomputers
    - Internet applications are online and they serve users with low latency. Making service unavailaible is not an option. Offline batch jobs like weather simulation can be stopped. 
    - Supercomputers have specialized hardware where each node is quite reliable and commuicate through shared memory and remote direct memory access. Cloud services are built from commodity machines, have higher failure rates but provide equivalent performance at lower cost.
    - Large datacenter networks are based on IP and Ethernet arranged in Clos topologies. Supercomputers are based on multi-dimensional meshes and toruses which yield better performance for HPC workloads
    - The bigger the system gets, it's likely that one of its components get broken. When the error handling strategy consists simply of giving up, a large system can spend a lot of time recovering from faults rather than do useful work.
    - If system can tolerate failed nodes and keep working, it's a useful feature from operations and maintainence e.g Rolling upgrade.
    - In a geographically distributed deployment (keeping data geographically close to the users), communication most likely goes over the Internet which is slow and unreliable compared to local networks.
5. We must accept the possibility of partial failure and build fault tolerance into distributed systems to make them work i.e build a reliable system from unreliable components. 
6. Fault handling must be a part of the software design and we need to know the behavior of the software in the case of a fault. 
7. It is important to consider a wide range of possibilities of faults to artificially create situations in your testing environment to see what happens. In distributed systems, suspicious, paranoia, pessimism pay off.
8. It's basically an old idea in computing to construct a more reliable system from less reliable parts. 
    - Error correcting codes allow digital data to be transmitted accurately across a communication channel that may occassionally get some bits wrong.
    - IP Protocol is unreliable in that it drops, delay, duplicate or reorder packets. TCP provides a more reliable transport layer atop IP ensuring deduplication, re-transmisison of missing packets, reassembling of packets. 
9. A limit exists as to how reliable a system may be from its underlying unreliable parts.
10. Although the more reliable system may not be pefect, it still takes care of some of the tricky low level faults so the remaining faults are easy to reason about.

## Unreliable Networks
1. The distributed systems that we're focusing on is shared-nothing systems meaning a bunch of machines connected by a network. 
2. The network is the only way those can communicate. Each machine has it's own resources and cannot access the other machine's resources directly except by making requests to a service over the network. 
3. Shared nothing is compatively cheaper approach than others because it requires no special hardware, can make use of commoditized cloud computing services and can achieve high reliability  through redundancy across multiple geographically distributed datacenters. 
4. Most networks are asynchronous packet networks, meaning one node can send a packet/message to another node but the network gives no guarantees of when it will arive or if it will arrive. 
5. For a request-response model, many things can go wrong
    - Lost request
    - Request may have been queued and will be delivered later due to overloaded network or recipient.
    - Remote node may have failed.
    - Remote node may have stopped responding
    - The remote node may have processed your request but the response is lost on the network.
    - The remote node may have processed your request but the response got delayed.
6. Thus, if you send a message to another node and don't receive a response, it's impossible to tell why.
7. The usual way to handle this is a timeout. After sometime, you give up waiting and assume that the response is not going to arrive. However, we still don't know whether the remote node has got the request.

### Network faults in practise
1. One might hope that by now computer networks become reliable. However, we have'nt succeeded yet.
2. Network problems can be fairly common even in controlled environments like a datacenter controlled by one company. For example: One study found about 12 network faults per month for a medium sized datacenter. Adding redundant network gear does'nt reduce faults, it does'nt guard against human error. 
3. Any kind of errors can occur from sharks biting underwater sea cables, network interface dropping packets, misconfigured swithces delaying packets by several seconds to faulty routers. 
4. When one part of the network is cut off from the rest due to some reason, it's sometimes called as a network partition or a netsplit.
5. The fact that netsplits can occur means your network should be able to handle them. Handling does'nt mean tolerating them, you can simply show an error message to the users. 
6. You need to know how your software reacts with network problems and ensure that system can recover from them.

### Detect faults 
1. Many systems need to automatically detect faults
    - Load balancer needs to stop routing requests to a dead node.
    - For a distributed database with master-slave replication, if the leader fails, one of the followers needs to be promoted to the new leader. 
2. Network uncertainty makes it very difficult to know whether a node is working or not. 
    - If the machine is reachable but no process is listening on the destination port because the process crashed. 
    - If the node process crashed but the node's operating system is still running, a script can notify other nodes about the crash, so that other nodes can quickly take over. Hbase does this. 
    - If you have access to the management interface of the network switches, they can be queries to detect link failures. 
    - If the router is sure that the IP address you're trying to connect is unreachable, it may reply with an ICMP Destination Unreachable packet. 
3. Rapid feedback about a remote node being down is useful. It can't be counted on though. If you need to be sure that a request was successful, you need a positive response from the application itself. 
4. If something has gone wrong, in general you have to assume that you will get no response at all. TCP retries transparently but you can also retry at the application level, wait for the timeout to elapse and declare the node to be dead if you don't hear back within the timeout.

### Timeouts and Unbounded delays 
1. How long should a timeout be? 
2. A long timeout means a long wait until a node is declared dead. Short timeot detects faults faster but carries a risk of incorrectly declaring a node to be dead when in fact it might be a temporary slow down.
3. Premature declaration of a node to be dead may cause some unwarranted actions like sending the same email twice. For example: if the node is actually alive but has suffered some temporary slow-down. If another node takes over, it may redo the same action done by the other node.
4. The transfer of responsonbilities of a seemingly dead node to another node places additional load on other nodes and the network. Imagine doing this when the system is running at peak and the node is not actually dead. 
5. Consider a fictitious system having a network guaranteeing that a packet is delivered within a time d or it is lost. Delivery does'nt take longer than d. If r is the time in which the node handles a requests, you can say that every successful request receives a response in time 2d + r. 
6. Most systems that we work with don't have these guarantees. Asynchronous networks have unbounded delays. They try to deliver packets as soon as possible. There is no upper limit for the time a packet may take to arrive. Most server implementations cannot guarantee they can handle requests in a maximum time.
7. For failure detection, it's not sufficient for the system to be fast most of the time. If your timeout is low, it only takes a transient spike in round-trip times to throw system off-balance.

#### Network congestion and queuing
1. Variability of packet delivery times in computer networks is most often due to queuing. 
    - If several different nodes send packets to the same destination, the network switch must queue them up and feed into the destination network one-by-one. On a busy network link, a packet may need to wait a lot (network congestion). If the switch queue fills up, the packet is dropped.
    - If the packet reaches the destination machine, if all cores are currently busy, the incoming request from the network is queued by the OS until the application is ready to handle it.
    - In virtualized environments, a running operating system often pauses while another virtual machine uses a CPU core. During this time, the paused VM cannot consume any data from the network.
    - TCP performs flow control (backpressure or congestion avoidance) in which a node limtis its own rate of sending in order to avoid overloading a network link. The additional data is queued at the sender. TCP considers a packet to be lost if its not acknowledged within some timeout (calculated from observed round-trip times) and lost packets are re-transmitted. Application does'nt get to see the packet loss and re-transsmission, it does see the resulting delay.
2. TCP Vs UDP
    - Latency-sensitive applications such as video confering use UDP instead of TCP. It's actually a trade-off betweem reliability and variability of delays. UDP does not perform flow control or retransmit lost packets.
    - UDP is a good choice in situations where delayed data is worthless. For video-conferencing/video calls, the retry happens at the human layer. Its meaningless re-transmit lost packets for video call before it's data is played over the speakers. 
3. All these factors contribute to variability of network delays. A system with plenty of spare capacity can easily drain queues, whereas in a highly utilized system, long queues build up very quickly. 
4. Public clouds and multi-tenant data centers have shared resouces. Batch workloads can easily saturate network links. Network delays can be highly variable if someone near you uses a lot of resources.
5. For such environments, timeouts can only be chosen experimentally - measure the distribution of the round-trip time over an extended period to determine the expected variability of delays, take application characteristics into account to determine an appropriate trade-off between failure detection delay and risk of premature timeouts.
6. Rather than using constant timeouts, systems can continually measure response times and automatically adjust timeouts according to the response time distribution. This can be done with a Phi Accrual failure detector used in Akka and Cassandra. 

### Synchronous Vs Asynchronous networks
1. Why can't computer networks deliver packets with fixed maximum delay at the hardware level rather than have software worry about it? 
2. To understand this, lets compare datacenter networks to a fixed line telephone network (non-cellular, non-VOIP) which is extremely reliable: delayed audio frames, dropped calls are very rare. Phone calls require a constantly low end-to-end latency to deliver the audio samples of your voice. 
3. The telephone network reserves a guaranteed amount of bandwidth for the entire call along the entire route between the two callers. The circuit that gets created remains in place till the call ends.
4. This kind of network is synchronous: even as data is passed through several routers it does not suffer from queuing. Because when a call is established, 16 bits of space within each frame (ISDN calls run at a fixed rate of 4,000 frames per second) are reserved for the entire duration of the call for all hops in the network. So there is no queuing, maximum end-to-end latency of the network is fixed. We call this a bounded delay.
5. Can we make network delays predictable? 
    - A telephone network circuit is a fixed amount of bandwidth  which nobody else uses when it gets established. TCP opportunistically uses whatever network bandwidth is availaible.
    - Give TCP a variable-sized block of data and it will try to transfer it in the shortest time possible. When a TCP connection is idle, it does'nt use any bandwidth.
    - If datacenter networks were circuit-switched, it would possible to establish a guaranteed maximum round-trip time when a circuit is set-up. But they are not. Ethernet and IP are packet switched protocols which suffer from queuing and thus suffer from unbounded delays. Ethernet/IP protocols don't have a concept of a circuit.
    - Ethernet/IP networks are optimized for  bursty traffic. A circuit is good for audio/video calls. For requesting a web page, sending up  emails, transferring files does not have any bandwidth requirement, we want to complete is as soon as possible. Using circuits for bursty data transfers wastes network capacity and makes transfers unecessarily slow.
    - TCP dynamically adapts the rate of data transfer to the availaibility network capacity.
    - ATM & Infiniband are some hybrid networks combining packet-switching and circuit switching.
    - With careful use of quality of services (QoS, prioritization and scheduling), admission control (rate-limiting on senders,  it's possible to emulate circuit switching on packet networks) or provide statistically bounded delay.
6. Latency & Resource utilization
    - Variable delays are a consequence of dynamic resource partitioning
    - Consider a wire between two telephone switches capable of handling 10,000 simultaneous calls. Each circuit switched over this wire, uses one of 10,000 slots. This resource wire is statically used, if you're the only call on the wire right now and other 9,99,9 slots are unused, your circuit is allocated the same fixed amount of bandwidth as when the wire if fully utilized
    - The internet shares network bandwidth dynamically. Senders compete with each other to get their packets over the wire as quickly as possible and switches determine which packets to be sent next. This has a downside of queuing but it's maximum utilization of the wire. 
    - Same case with CPUs. If you share each core dynamically, one thread has to wait in the OS run queue while another is running. This utilizes the hardware better rather than if you've allocated static CPU cycles. Better hardware utilization is also a motivation for using virtual machines.
    - Latency gurantees are achievable if resources are statically partitioned, but it comes at the cost of reduced utilization - its more expensive 
    - Multi-tenancy with dynamic resource partitioning provides better resource utilization so it's cheaper but has downsides of variable delays. 
7. Currently deployed technology does not allow us to make any guarantees of delays or reliability of the network, we have to assume that network congestion, queueing and unbounded delays will happen. 
8. No correct values for timeouts, they have to be decided experimentally.

## Unreliable clocks
1. Applications depend on clocks in various ways to answer questions like
    - Has this request timed out?
    - What's the 99th percentile response time of this service?
    - How much queries per second this service handled on an average?
    - How long user spends on this site?
    - When does this cache entry expire?
    - What datetime should the reminder email be sent?
2. Using time, we generally measure "Durations" (the time interval between a request and a response) and "points in time" (events that occur on a particular date at a particular time)
3. In a distributed system, there are variable delays in the network so a message received is always later than the time it's sent. Due to variable delays, we don't know how much later. 
4. With multiple machines are involved, it's difficult to determine the order in which things happend.
5. Each machine has it's own clock i.e a quartz crystal oscillator which is not perfectly accurate. NTP protocol is often used to sync the time across servers to the time reported by a group of NTP servers. The servers get their time from a more accurate time source such as a GPS receiver.

### Monotonic Vs Time-of-Day Clocks
1. Modern computers has two types of clocks - monotonic and time-of-day. They serve different purposes.
2. Time-of-day clocks:
    - Returns the current date/time according to some calendar (wall-clock time). For example: System.currentTimeMillis() that returns the milliseconds since the epoch.
    - They are usually synchronized with NTP meaning a timestamp on machine is similar to the timestamp on another machine.
    - Time-of-day clocks have some oddities like if a local clock is way to ahead of NTP server, it appears to jump back at a previous point in time. These jumps make time-of-day clocks unsuitable for measuring elapsed time. 
3. Monotonic clocks: 
    - A monotonic clock is suitable for measuring a duration such as a timeout or a service response time.
    - A monotonic clock is a clock that's always guaranteed to move forward. You can check the difference between values of monotonic clock on a single computer at two different points in time that tell you how much time elapsed between the two points.
    - Absolute value of a monotonic clock is meaningless. It might be the number of nanoseconds since the computer was started or anything else. It makes no sense to compare the monotonic time from two different computers. 
    - Even for the timer associated with more than one CPU cores, it may be separate which is not necessarily synchronized with others. Operating systems can compensate for any discrepancy and try to present a monotonic view of the clock even as they are scheduled to perform on different CPUs.
    - A monotonic clock does'nt assume any synchronization between different nodes' clocks and is not sensitive to slight inaccuracies of measurement. 

### Clock synchronization and Accuracy
1. Time of day clocks need to be synced to an external NTP server. 
2. Hardware clocks and NTP can be fickle beasts. The methods for getting a clock to tell the correct time are'nt nearly as reliable or accurate as one might hope. For example
    - A computer's quartz clock is not very accurate. It drifts (runs faster or slower) than it should. The drift varies per the temperature of the machine. This drift limits the best possible accuracy that you can achieve even if everything else works correctly.
    - NTP may forcibly reset the local clock if it has drifted too far ahead or back. Applications observing the time may see time jumping backward and forward.
    - NTP synchronization is also susceptible to network delays and so there is a limit to its accuracy when you're on a congested network with variable packet delays.
    - Certain NTP servers are wrong or misconfigured and the time they tell may be off by hours. 
    - Leap seconds result in a minute thats 59 seconds or 61 seconds which messes up timing assumptions in systems that are not designed with leap seconds in mind. 
    - Virtual machines in which the hardware clock is virtualized raises additional challenges for applications needing accurate timekeeping. When a CPU core is shared, each VM is paused for milliseconds while another VM is running. From an application's point of view, this pause manifests itself as the clock suddenly jumping forward.
    - Software run on mobile devices or embedded devices cannot trust the device's hardware clock at all. 
3. It's possible to achieve very good clock accuracy if you're willing to invest sufficient resources. It can be accomplished using GPS receivers, the Precision Time Protocol (PTP) and careful deployment and monitoring. 

### Relying on  Synchronized clocks
1. Clocks have a surprising number of pitfalls: a day may not show exactly 86,400 seconds, time-of-day clocks may move backward in time, time on one node may be quite different from the time on another node.
2. Robust software needs to be prepared well to deal with incorrect clocks and faulty networks. Misconfigured clocks are difficult to detect as the clock drifts gradually away from reality.
3. If you use software that requires synchronized clocks, you should also keep monitoring the clock offsets between all the machines. Any node whose clocks drifts too far from the others should be declared dead and should be removed from the cluster.
4. Timestamp for ordering events: This is one case where it's dangerous to rely on the clocks i.e. ordering of events across multiple nodes. If two clients write to a distributed database who got there first? When a write is replicated to other nodes, it is tagged with the timestamp according to the time-of-day clock of its originating node. We use a conflict resolution strategy called Last-Writes-Win in which the last received writes are considered and applied. LWW has a few fundamental problems
    - Databases writes can mysteriously dissapper: A node with a lagging clock is unable to overwrite values previously written by a node with a fast clock until the clock skew between the nodes has elaped.
    - LWW cannot distinguish between writes that occurred in quick succession and writes that are truly concurrent. Additional tracking mechanisms such as version vectors are needd to prevent violations of causality.
It's important to be aware of the definition of recent depends on a local time-of-day clock which may well be incorrect. NTP's synchronization accuracy itself depdnds on the network round-trip time in addition to other sources of error such as quartz drift. Logical clocks which are based on incrementing counters rather than an oscillating quartz crystal are safer alternative for ordering events. Logical clocks measure the relative ordering of events and in contrast time-of-day and monotonic clocks measure actual elapsed time are also known as physical clocks. 
5. Clock readings have a confidence interval:
    - Even if a clock has a microsecond or a nanosecond precision, it does'nt mean the value is actually accurate to such precision. The drift in an imprecise quartz clock can be several milliseconds even if you synchronize with a local NTP server.
    - It does'nt make sense to think of a clock reading as a point in time, it is more like a range of values within a confidence internal: for example a system may be 95% confident that time now is between 10.3 to 10.5 seconds past the minute. The uncertainty bound can be computed based on your time source.
    - If you're getting the time from a server, the uncertainty is based on the expected quartz drift since your last sync with the server + NTP server uncertainty + network round trip time to the server. 
    - Google Spanner's TrueTime API explicitely reports the confidence interval on the local clock. When asked it gives a [earliest_time, lastest_time] combination which is based on how long it has been synchronized with a more accurate time source.
6. Synchronized clocks for global snapshots: 
    - Snapshot isolation allows read-only transactions to see the database in a consistent state at a particular point in time without locking or interfering with read-write transactions. 
    - Most common implementation of snapshot isolation requires a monotonically increasing transaction ID. If a write has greater transaction ID, it happened later than the snapshot and it's invisible to the transaction. 
    - When a database is distributed across many machines & data-centers, a global monotonically increasing transaction ID is difficult to generate; it requires coordination. It must also support causality viz. if transaction A reads a value written by transaction B, it must generate a higher transaction ID than A, otherwise the snapshot isolation may not be consistent.
    - We could use timestamps from synchronized time-of-day clocks as transaction IDs. If we could get the synchronization good enough, they would right properties: i.e later transactions would have a higher timestamp. 
    - Spanner uses confidence intervals returned by their time API. If we have two confidence intervals Aearliest, Alatest and Bearliest, Blatest and if those two intervals do not overlap, i.e Aearliest < Alatest < Bearliest < Blatest, we're sure that B happened after A. 
    - Spanner deliberately waits for the length of the confidence interval to ensure transaction timestamps reflect causality. It ensures any transaction reads data at a sufficiently later time so that their confidence intervals do not overlap.
    - Google deploys an atomic clock in each datacenter allowing clocks to be synchronized to within  7ms to keep the clock uncertainty as small as possible. 

### Process Pause
1. For a distributed database with a leader per partition, how does the leader know it's still the leader and not declared dead by others. 
2. The leader obtains a lease from other nodes. Only one node can hold the lease ay any one time and in order to remain leader, it must periodically renew that lease. If node fails, it stops renewing the lease so another node can take over. 
3. Consider below code
    ```
    while(true) {
        request = getIncomingRequest()
        if(lease.expiryTimeInMillis - System.currentTimeInMillis() < 10000) {
            lease = lease.renew();
        }
        if(lease.isValid()) {
            processRequest();
        }
    }
    ```
    Several things wrong with this code
    - Expiry time ma be set on a separate machine
    - It's relying on sychronized clocks. If clocks are out of sync by a few seconds, this code may start to do strange things.
    - The code assumes that very little time passes between the point it checks the time and the time the request is processed. In the case of an unexpected pause (say 15 second pause after checking the time due to Java GC pause, virualization pause, OS context switch and so on), the lease may have expired by the time the request is processed. 
4. All these occurences can pre-empt the running thread and resume it some point later without the thread noticing. The problem is similar to making multi-threaded code on a single machine thread safe, you can't assume anything about timing because arbitrary context switches and parallelism may occur.
5. A node in a distributed system must assume that it's execution can be paused for a significant length of time. During the pause, the rest of the world keeps moving and even declare the paused node to be dead because it's not responding.

### Response time guarantees
1. As discussed earlier, in many OSes, programming languages, threads/processes may pause for an unbounded amount of time as discussed. The reasons for pausing can be eliminated if we try hard enough
2. For mission critical software, like rockets, ICU computer systems, they must respond predictably to their sensor inputs. There is a specified deadline by which the software must respond if it does'nt meet the deadline.
3. Real-time in these systems it means a system is carefully designed to meet specific timing guarantees in all circumstances. Real-time on the web is more vague where it describes servers pushing data to clients and stream processing without hard response time requirements.
4. Providing hard real-time guarantees in a system requires support from all levels of software stack
    - A real-time operating system (RTOS) that allows processes to be scheduled with a guaranteed allocation of CPU time
    - Library functions document their worst case execution times
    - Dynamic memory allocation is disallowed
    - Enormous testing and measurement to ensure guarantees are met
  Therefore, developing real-time systems is very expensive and they are most commonly used in safety-critical embedded devices. Real time is not the same as high performance. Real-time systems may have lower throughput  since they prioritize timely responses above all else.

### Limiting the impact of garbage collection
1. Language runtimes have some flexibility around when they schedule garbage collection because they can track the rate of object allocation and remaining free memory over time. 
2. GC pauses can also be treated as a brief planned outages of a node and to let other nodes handle requests from clients when one node is collecting it's garbage. 
3. If the runtime can inform the application that a node requires GC pause, the application can stop sending new messages to the node waiting for it to process outstanding requests and then perform the GC when no requests are in progress.
4. One more variant of this is to use garbage collection for short-lived objects and to restart periodically before they accumulate enough long-lived objects to require a full GC. Rolling upgrades can be used to have planned restarts.  
5. These measures can reduce the impact of GC pauses on the application
   
## Knowledge, Truth and Lies
1. In Distributed systems there is only message passing between the various nodes via an unreliable network with variable delays and systems may suffer from partial failures, unreliable clocks and processing pauses.
2. A node in the network cannot know anything for sure it can only make guesses based on the messages it receives or does'nt receive via the network. If a remote node is unresponsive, there is no way of knowing what state it is in.
3. This gives rise to some philosophical questions
    - What do we know to be true or false in our system?
    - How sure can we be of that knowledge if the mechanisms for perception and measurement are unreliable? 
    - Should software systems obey the laws of physical work such as cause and effect?
4. For a distributed system, we usually state the assumptions we are making about the behavior (system model) and design the actual system that meets those assumptions. Reliable behavior is achievable even if the underlying model provides a few guarantees. 

### The truth is defined by the majority
1. Imagine a network with an asymmetric fault. It has a node that's able to receive all messages but any outgoing messages are dropped or delayed. The other nodes can't hear it's responses. After some time, it's marked dead by the other nodes because still they can't hear its responses.
2. As a second scenario, the semi-disconnected node realizes that it's responses are not getting acknowledged by any other nodes. It's wrongly declared dead and it cannot do anything about it.
3. A third scenario. Imagine a node experiencing a long GC pause. All the node's threads are pre-empted by the GC and paused for one minute. No requests are processed and no responses send. Other nodes grow impatient and declare the node as dead.
4. Finally the node's GC finishes or whatever it was doing finishes and it comes back up. It does'nt realize that an entire minute has  passed and it was declared dead. The moral of these stories is a node cannot necessarily trust it's own judgement. 
5. A distributed system cannot relie on a single node because a node may fail at any time potentially  leaving the system stuck and unable to recover. Decisions require some minimum number of votes from several nodes in order to reduce the dependence on one particular node. This is called as quorum. 
6. A quorum is an absolute majority of more than half the nodes (although some other kinds of quorums are possible). A majority quorum allows a system to keep functioning if individual nodes have failed (with 3 nodes, 1 node can fail). It's still safe because there can be only one majority in the system. 
7. The leader and the lock: 
    - A system requires there to be only one of something
        - Only one node to be the leader for a database partition
        - One transaction or client is allowed to hold the lock for a particular resource or object
        - Only one user is allowed to register a particular username.
    - Care should be exercised in implementing this in a distributed system. Even if a node believes itself to be the leader, it does'nt mean that a quorum of nodes agrees. A node may have been the leader but it may have been demoted and another leader may have been elected.
    - A node continuing to act as leader may pose problems in a system that has not been carefully designed. If other nodes believe it, the system as a whole may do something incorrect. 
    - HBase had a problem. If a node having a write lock paused for GC, it's lease would be expired. This lease meant it's the leader and can determine the write to a file. By the time, it's GC finishes and it's back up, it still believes it's the leader and has the lease which is expired and tries to write to the file. But ends up corrupting the data. 
    - Fencing Token
        - A technique used to prevent the scenario of a node thinking of itself as the chosen one. 
        - Each time the lock server grants a lock, it returns a fencing token which is a number that increases every time when a lock is granted. 
        - We require that every time a client writes to the storage service, it must include its current fencing token.
        - Say 
            - Initially a client 1 has a fencing token has a token number 33. It goes into a long pause and it's lease expires
            - Meanwhile client 2 acquires a fencing token with value 34. It sends it's write request with fencing token 34 to the storage service.
            - Now client 1 has come back to life after it's GC pause and it too sends the write request to storage service with token value 33. But, since it's lease is expired and token number 33 is less than the highest token value that it has processed so far, it rejects the request with token 33.
            - If Zookeeper is used as the lock service, the transaction ID txid or the node version cversion is used as the fencing token. They are guaranteed to be monotonically increasing.
        - Checking tokens on the server side may seem a downside but it's a good thing. It's unwise for a service to assume that it's clients will always be well-behaved. The clients are run by people whose priorities are different from those of the people running the service.
    - Byzantine Faults: 
        - If a node wanted to subvert the system's guarantees, it could do so by sending a fake fencing token. We assume here that nodes are honest and they tell the truth.
        - Distributed systems problems become much hard if there is a risk that nodes may lie (send arbitrary or faulty responses). Such faults are called Byzantine faults and problem of reaching consensus is called Byzantine Generals problem.
        - The Byzantine Generals Problem:
            - Is a generalization of the Two-Generals problem imagining a situation in which two army generals need to agree on a battle plan. 
            - They camp on two different sites and can only communicate by messages and the messengers can sometimes get lost or captured. 
            - In the Byzantine version of the problem, there are n generals who need to agree and their endeavor is hampered by the fact that there are some traitors in their midst. The traitors may try to deceive or confuse by sending fake messages.
            - A system is Byzantine fault-tolerant if it continues to operate correctly even if some of the nodes are malfunctioning and not obeying the protocol or if malicious attacks are interfering the network. 
                - In an aerospace environment, data in computer's memory or CPU could become corrupted by radiation. A system failures could result in killing everyone overboard and so flight control systems must tolerate Byzantine faults
                - Peer-to-peer networks like Bitcoin and blockchain are considered to be a way of getting mutually untrusting parties agree on a transaction happening or not. These too are Byzantine fault tolerant.
        - Protocols for making systems Byzantine fault tolerant are compicated and fault tolerant embedded systems rely on the hardware level. In most server side data systems, the cost of deploying Byzantine fault-tolerant systems makes them impractical.
        - Server side systems do expect malicious inputs from clients that end-user systems may control so preventive measures like input validation, output escaping, sanitization are important. However, Byzantine fault-tolerant protocols are'nt required here. Simply the server is made the authority of what a client can and cannot do.
        - In most systems an attacker if he is able to compromise one node he is able to compromise other nodes as well. Thus, authentication, access control systems, encryption, firewalls continue to be the main protection against attackers.
    - Weak forms of lying:
        - Mechanisms to guard software against weak forms of lying such as hardware issues, software bugs and misconfigurations are also worth to be added to prevent sending invalid messages
            - Checksums to catch corrupted network packets due to hardware, OS issues. TCP/UDP based checksums may evade detections at times, we need checksums at the application level too.
            - Publicly accessible application must sanitize any inputs from users. 
            - NTP clients should be configured with multiple server addresses.The use of multiple servers makes NTP more robust

## System model and Reality
1. Many algorithms have been designed to solve distributed systems problems. In order to be useful, these algorithms need to tolerate various faults of distributed systems.
2. Algorithms need to be written in a way that does not depend too heavily on the details of the hardware and software configurations on which they run.
3. System model is an abstraction that describes what things an algorithm may assume. This in turn requires to formalize kinds of faults that we expect to happen in a system.
4. Three system models in common use based on timing assumptions
    - Synchronous model
        - Assumes bounded network delay, bounded process pauses and bounded clock error. 
        - Network, clock drift process pauses don't exceed a fixed upper bound
        - Impractical and it's not realistic because unbounded delays and pauses do occur.
    - Partially synchronous:
        - A system behaves like a synchronous system most of the time but it sometimes exceeds bounds for network delays and process pauses.
        - Realistic model of most systems. Most of the time, network, processes are quite well-behaved but we have to reckon with the fact that any assumptions may be shattered occasionally. 
    - Asynchronous model: 
        - An algorithm is not allowed to make any timing assumptions. It may not even have a clock.
        - Some algorithms can be designed for asynchronous model but it's very restrictive. 
5. Models based on node failures: 3 most common are
    - Crash stop failures: An algorithm may assume that a node can fail only in one way by crashing. The node may suddenly become unresponsive at any moment and thereafter the node is gone.
    - Crash-recovery: Nodes crash at any moment and become responsive after an unknown time. Nodes are assumed to have stable storage that is preserved across crashes (disks) and in-memory state is lost.
    - Byzantine faults: Nodes do absolutely anything including trying to trick and deceive other nodes. 
6. To model real world systems the partially synchronous model with crash recovery is the most useful one.

### Correctness of an algorithm - How Distributed systems cope up with partial synchrony with crash recovery
1. We can define the properties of a distributed algorithm and what it means to be correct. For example: If we are generating fencing tokens, we require following properties
    - Uniqueness: No two requests for a fencing token return the same value
    - Monotonic sequence: If request X returns token tx, and request y return token ty and x completed before y then tx < ty
    - Availaibility: A node requeting a fencing token and does not crash eventually receives a response.
2. An algorithm is said to be correct in a system model if it always satisfies it's properties in all situations that happen in that system model. If all nodes crash or all network delays become infinitely long, then no algorithm will be able to get anything done.

### Safety and Liveness
1. Safety is defined as nothing bad happens and liveness is defined as eventually something good happens. The formal definitions lay too much emphasis on good and bad which is subjective.
2. Actual mathematical definitions:
    - If a safety property is violated, we can point at a particular time when it was broken. After a safety property has been violated, the violation cannot be undone. 
    - A liveness property may not hold at some point in time but there is always hope that it may be satisfied in the future.
3. These properties allow us to deal with different system models. In distributed properties, it's required that safety properties always hold even if the nodes crash or entire network fails. 
4. With livness property we can make caveats like a request needs to receive a response if a majority of nodes have not crashed. 
5. The definition of partially synchronous model requires that eventually the system returns to a synchronous state. That is any period of network interruption lasts only a finite duration.

### Mapping system models to the real world 
1. When implementing an algorithm in practise, the messy facts of reality come back to bite you and it becomes clear that the system model is a simplified abstraction of reality.
2. Algorithms in the crash-recovery model assumes that data in stable storage survives crashes. What happens if data on disk is corrupted or data is wiped out due to hardware misconfiguration.
3. Quorum algorithms rely on a node remembering the data it claims to be stored. If a node breaks the quorum by not remembering the data it has stored, a new system model is needed in which it's assumed that stable storage may survive crashes but sometimes data be lost.
4. The theoratical description of a algorithm may assume certain things not to happen. However, in real implementation, we may still include code to handle the case where something happens that was assumed impossible to happen. This is arguably one of the differences between computer science and engineering.
5. System models are helpful for distilling down the complexity of real systems to a manageable set of faults that we can reason about. We can prove algorithms correct by showing that their properties hold in some system model. 
6. Theoratical analysis may uncover problems in an algorithm that might remain hidden for a long time in a real system that may come to bite you when your assumptions are defeated due to unusual circumstances. Proving an algorithm correct does not mean its implementation on a real system may definitely be correct.