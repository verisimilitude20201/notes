# Chapter 1 - Reliability, Scalability, Maintainaibility

1. For data-intensive applications, raw CPU power is rarely a limiting factor, bigger problems are the amount of data, complexity of data and speed at which it is changing.
2. Built from standard building blocks that provide commonly needed functionality such as
    1. Store data so that other applications can find it
    2. Remember the results of an expensive operation to speed up reads (caches)
    3. Allow users to search data by keywords or filter it in various ways (search indexes)
    4. Send a message to another process to be handled asyncly (stream processing)
    5. Periodically crunch a large amount of accumulated data (batch processing)

## Thinking about data systems
1. Databases, queues, caches are all different category of tools. 
2. Databases and message queue both store data for some time, they have different access patterns, performance characteristics and very different implementations.
3. Many different tools for data storage and processing have emerged which are optimized for a variety of cases and no longer fit into traditional categories. For example: There are data stores that are used as message queues (Redis) and message queues with database like durability guarantee (Kafka). Boundaries between data system categories are blurring.
4. Many applications have wide ranging requirements such that a single tool can no longer meet all it s data storage and processing needs. Instead, that work is broken down into tasks that can be performed efficiently on a single tool and those different tools are stitched together using application code.
5. The API hides the implementation details of the several tools that get combined to provide a service.
6. It's a composite data-system that may provide certain guarantees. For example: cache will be correctly invalidated. 
7. While designing such a system several question arise
    1. How do you ensure that the data remains correct and complete even when things go wrong internally? 
    2. How do you provide consistently good performance to clients even when parts of your system are degraded?
    3. How do you scale to handle increase in load?
    4. What does a good API look like?
5. 3 key characteristics
    1. Reliability: The system should continue to work correctly even in the face of adversity
    2. Scalability: As the system grows in data volume, traffic volume, there should be reasonable ways of dealing with that growth.
    3. Maintainaibilty: Overtime, many different people will work on the system and they should be able to do it productively.

## Reliability
1. Typical expectations from applications
    1. Performs the behavior user expects
    2. Tolerate user making some mistakes or using the application in unexpected ways
    3. Performance is good for the expected use-case under expected volume and data load.
    4. Prevents any unauthorized access.
2. All above means the app is working correctly. We can define reliability as continuing to work correctly even when things go wrong.
3. Things going wrong are called faults and systems that anticipate faults and cope up with them are called fault-tolerant or resilient.
4. Fault is defined as one component of the system deviating from it's spec. Failure is when the system stops providing the required service to the user.
5. It's possible to design fault-tolerant mechanisms that prevent faults from causing failures. We generally prefer tolerating faults rather than preventing them except security faults.
### Hardware faults
1. Hard disk crashes, RAM becomes faulty, power grid black out are some of the hardware faults. 
2. Happen all the time in large data centers where you have lot of machines.
3. To reduce the failure rate of hardware components, add redundancy. Although redundancy cannot prevent hard-ware problems, it can keep a machine running uninterrupted for years.
    1. RAID configuration of disk
    2. Hot swappable CPUs
    3. Data centers having diesel generators for backup
4. Until recently, hardware redundancy was sufficient for most applications.
5. Data volumes and application's computing demands increased many-fold. More applications use large number of machines and with cloud platforms like AWS it's normal for virtual machines to become unavailaible without warning. 
6. Thus, we need to use software redundancy along with hardware. 
7. We also get operational advantages with such set-ups. Systems that tolerant machine failures can be patched one node at a time without taking down entire system (Rolling upgrade)

### Software Fault
1. Unlikely that a large number of coorelated faults in a number of hardware components.
2. Systemic failures may give rise to a series of coordinated system faults
    - Software bug causing every instance of an application to crash when given a bad input.
    - Runaway process consuming memory, time, disk space or network bandwidth
    - A service slows down, begins given corrupted responses
    - Cascading failures. 
3. These lie dormant unless triggered by an unusual set of circumstances. Software makes some assumptions about it's environment and those assumptions ceases to be true. 
4. Lots of corrective/preventive actions can be taken to minimize these
    - Careful thinking about assumptions/interactions in the system.
    - Thorough testing
    - Process isolation
    - Measuring, monitoring and alerts. 

### Human errors
1. Even the best human operators are known to be unreliable. 
2. Configuration errors by human operators are one of the leading causes of outages than hardware faults
3. Few approaches to make our systems reliable inspite of unreliable humans
    - Design systems to minimize the opportunity of error. Well-designed abstractions, APIs, admin interfaces are some ways to do this
    - Decouple the places where people make most mistakes. Fully featured sandbox environments where people can experiment/explore safely using real data can be provided
    - Test thoroughly at all levels.
    - Quick and easy recovery from human errors. For example: Fast to roll-back configuration changes, rolling out new features/code changes gradually.
    - Detailed monitoring metrics and errors i.e. Telemetry.
    - Implementation of good management practices and training.

### Importance of reliability
1. Reliability is very important especially since it involves huge reputation loss and damaged revenue for business applications if they are unreliable. For example: If you are storing all your photos on Instagram, how would you feel if a database crash wipes off all your data? 
2. We might sacrifice reliability to reduce development effort for building a prototype (say for an unproven market). But we should be conscicous and document if we're cutting corners

## Scalability
1. Scalability is a term we used to describe a system's ability to cope up with increased load.
2. Discussing scalability means considering questions like if the system grows in a particular way, how can we cope up with the growth? How can we add more computing resources for that?

### Describing load
1. Current load on the system must be succintly defined first to answer questions like what happens when our load doubles?
2. Current load can be best described in terms of load parameters. The best choice of parameters depends on the architecture of the system
    - Requests per second to a Web server
    - Ratio of reads to writes in a database
    - Number of simultaenously active users in a chatroom
    - Hit rate in a cache
3. Average case may matter for you or your bottleneck may be dominated by a small number of extreme cases.

#### Twitter example
1. Twitter has two main operations
    1. Post Tweet: A user publishing a new message to his followers. Averaging 4.6 requests/sec and 12 K requests/sec at peak
    2. Home Timeline: A user can view tweets by his followers. About 300K requests/sec
2. Twitter's scaling challenge is due to fan-out: Each user following many people and each user is being followed by many people. There are two ways to implement posting a tweet
    1. Inserting the tweet into a global collection of tweets. When a user requests their home-timeline, find all their followers and find the tweets for each of them ordered by time. Can be done via a simple JOIN between users table and tweets table and followers table.
    2. Maintain a simple timeline cache and whenever each user tweets, insert the tweet for each of his/her follower's timeline cache. This result has been computed ahead of time. 
3. Twitter used to use approach 1 but then shifted to approach 2 when the system struggled to keep up with the load of timeline views. This works because the avg rate of tweets is two-order magnitude lower than viewing home timeline so it's more preferable to do more work at write time.
4. Now, certain users can have upto 20 million followers. Eg. Narendra Modi. As per approach 2, it would lead to 20 million writes whenever he tweets!
5. In Twitter's case therefore, the distribution of followers to user is a key load parameter  for discussing scalability since it determines the fan-out load.
6. Twitter now uses a combination of both approaches. For celebrities that a user may follow, their tweets are fetched separately and merged with that user's home timeline. 

### Describing performance
1. Once we define the load, we now investigate what happens when the load increases. Two ways to look at it
    1. When you increase the load parameter and keep system resources unchanged, how is the system performance affect?
    2. When you increase the load parameter, by how much you need to increase the resources to keep system performance unchanged?
2. For a batch processing system, the throughput i.e. number of jobs processed per unit time is important or total time it takes to run a job on data-set of a certain size. For online system, response time is more important i.e. the time taken to give response after a request is sent.
3. Latency is the time the service actually took to respond, whereas response time includes latency time along with queuing and network delays.
4. Response time varies a lot and we need to think of it as a distribution of values rather than as a single number.
5. The average response time is a common metric in which an arithematic mean of n values is taken to compute the average. It's not a good metric though because it does'nt give you an idea of how many users experienced the delay.
6. Better it is to use percentiles. Take the list of response time, sort it from fastest to lowest. Median is the halfway point. If 200 ms is your median, it makes about half of the requests took less than 200 ms. Half the user requests take less than the median response time and the remaining half take longer than that.
7. Look at higher percentiles i.e. the 95%, 99%, 99.9 percentile if you want to figure out the bad outliers. These are the response time thresholds at which 95%, 99%, 99.9% of requests are faster than that particular threshold. If 95% response time is 1.5 seconds, it means 95 out of 100 requests take less than 1.5 seconds and 5 requests take 1.5 seconds or more.
8. Amazon aims at 99.9 percentile response times. Producing response times at a higher percentiles is difficult because they are affected by random events out of your control and benefits are diminishing.
9. Percentiles are used while defining Service-Level Agreements (SLAs) and Service Level Objectives (SLO) are contracts that define the expected performance and availaibility of a service. An SLa may state that the service is considered up if it has a median response time of less then 200 ms and 99th percentile under 1 second and service should be 99.9 percentile up most of the time. If these are not met, the client may seek refunds.
10. Queuing delays account for most of the delay since a server can process small number of things in parallel limited by it's CPU cores. This may block subsequent requests which is known as head-of-the-line blocking. Therefore, the response time is something that should be measured from client side. 
11. During performance testing to avoid skewed measurements, the load generating client should keep on sending requests without waiting for prior requests to be processed.
12. If small percentage of back-end calls are slow, the chances of getting a slow back-end call increases  if a user request requires multiple back-end calls and a higher proportion of end user requests end up being slow. This is called tail latency amplification.
13. You need to efficiently keep calculating response times on an on-going basis say in a rolling window of last 10 minutes.

### Approaches for coping up with the load.
1. An architecture appropriate for one level of load is unlikely to cope with 10 times that load. We should rethink the architecture on every order of magnitude load increase. 
2. If we think of scaling, we can discuss about achieving it in two ways viz. Vertical scaling (moving to a more powerful machine, also called scale up) and scale out (horizontal scaling: distributing the load across several smaller machines). Scale-out is also called Shared-Nothing.
3. Good architecture involves a pragmatic mixture of approaches. Using several large powerful machines can still be simpler and cheaper than a number of smaller virtual machines.
4. Distribution of stateless services across multiple machines is straight-forward, taking stateful data systems from a single node to a distributed set-up can be whole lotta complex.
5. Distributed data systems will become the default in the future for use cases that don't handle large volumnes of traffic.
6. An architecture that scales well for a particular application is built around the assumptions of which operations will be common and which will be rare - the load parameters.
7. Scalable architectures even though specific to a product are usually built from general purpose application building blocks arranged in familiar patterns. 

## Maintainaibility
1. Majority of the cost of software is in it's maintenance - bug fixing, keeping the systems operational, investigating failures, adapting it to new platforms, repaying technical debt,  modifying it for new use cases
2. Many people dislike maintaining so-called legacy systems since each are unpleasant in their own way.However, we should design software such that it will minimize the pain during maintenance. 

### Operability - Making life easy for operations
1. Good software cannot run reliably with bad operations but good operations can work around the limitations of bad software.
2. Setting up automation and ensuring it works correctly is important.
3. Good operability means making routine tasks easy, allowing the operations team to focus their efforts on high-value activities

### Simplicity - Managing complexity
1. A software project that's mired in complexity is known as Big Ball of Mud. As projects get large, they often become very complex and difficult to understand.
2. Various symptoms of complexity - explosion of the state space, tight coupling of modules, tangled dependencies, inconsistent naming, hacks for solving performance problems.
3. Complexity makes maintenance hard and budgets and schedule are often overrun. Complex software is at a greater risk of introducing bugs when making a change, the system is hard for developers to understand and reason about.
4. Reducing complexity greatly improves maintainaibility of software. 
5. Making things simpler means reducing accidental complexity. Complexity is defined as accidental if it's not inherent in the problem the software solves as seen by the users but arises from the implementation. 
6. One of the best tools to hide complexity is abstraction. A good abstraction hides a great deal of implementation detail behind a cleaner, simple-to-understand facade. Quality improvements in an abstracted component can benefit all applications that use it.
   - High level programming languages are abstractions that hide machine code, CPU registers and sys calls
   - SQL is an abstraction that hides complex on-disk & in-memory structures and concurrent requests from other clients. 
7. Finding good abstractions is hard. It's less clearer in the field of distributed systems how we should package the many good algorithms that we have into abstractions that help us keep the complexity at a manageable level.

### Evolvability: Making the change easy
1. System requirements are in a constant flux. Learn new facts, business priorities change, new features, new platforms replace old ones, legal or regulatory framework change and sp pm
2. Agile working patterns such as test-driven development and refactoring provide a framework for adapting to change.
3. The ease with which you can modify data system and adapt it to changing requirements is closely linked to it's simplicity and abstractions.
4. Agility on a data system level - evolvability


Functional requirements include what an application should do such as allowing data to be stored, retrieved, searched and processed. 
Non-functional requirements include properties such as security, reliability, compliance, scalability, maintaniaiblity and compatibility.