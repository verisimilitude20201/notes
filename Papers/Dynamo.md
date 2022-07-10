# Dynamo Paper by Amazon

## Abstract
1. Dynamo is a highly availaible key-value storage system that sacrifices consistency under certain failure scenarios to provide this level of availaiblity
2. Uses extensive object versioning and application-assisted conflict resolution in a novel way.


## Introduction
1. Strict operational requirements on Amazon in terms of performance, reliability and efficiency and scalability. 
2. Slightest outage can have worse financial outcomes.
3. Reliability & Scalability of a system running at Amazon scale depends on how well the application state is managed.
4. Amazon uses a decentralized, loosely coupled, service oriented architecture. 
5. Customers should be able to view/add items to their shopping cart even if there are tornadoes, network routes unavailaible. 
6. Failure handling should be treated as a normal case without impacting availaibility or performance.
7. Many services in Amazon only need a primary key access to a data-store. Such as session management, shopping cart, customer preferences, product catalog. Using the common pattern of a relational database would lead to inefficiences and limit scale and availaibility.
8. Dynamo provides a primary key interace to meet the requirement of this application
9. Technical overview
    - Data is partitioned by consistent hashing
    - consistency facilitated by object versioning
    - Consistency among replicas is maintained by a quorum-like technique and a decentralized replica synchronization protocol.
    - Employs a Gossip based distributed failure detection and membership protocol.
    - Completely decentralized.

## Background
1. Amazon's ecommerce platform consists of hundreds of services in an infrastructure consisting of thousands of servers hosted the world over.
2. Each services exposes a well-defined interface and is accessible over the network. 
3. Some of these services are stateless and others are stateful (service that generates response by executing business logic on it's state stored in persistent store.)
4. RDBMS is an overkill for Amazon's services that store and retrieve data only by a primary key. RDBMS requires expensive hardware and managment and highly skilled personnel. Availaible replication technologies are limited and choose consistency over availaibility
5. Dynamo has a simple key-value interface, highly availaible with a clearly defined consistency window, efficient in it's resource usage and simple scale out scheme to address groth in data-set or request rates.

## System assumption and requirements:

### Query Model
1. Simple read/write operations to a single data item uniquely identified by a key.
2. Value is a binary object.
3. Targets relatively small objects less than 1 MB
4. No operation can span multiple data items.

### ACID
1. Dynamo targets applications that operate with weaker consistency levels.
2. No isolation gurantees and permits only single key updates. 

### Efficiency:
1. Should perform on a commodity hardware infra.
2. Stringent latency requirements of 99.9 percentile.
3. No security requirements such as authentication and authorization. 
4. Each service should use it's own distinct version of Dynamo and it can scale upto 100 hosts.

## SLAs
1. To guarantee that an application can deliver it's functionality in a bounded time, each and every dependency in the platform needs to deliver it's functionality even within more tightly bounded time.
2. SLA is a negotiable contract between client & services where they agree on several system related characteristics prominently including client's expected request rate distribution for a particular API and expected service latency under those considerations
3. An example of an SLA is a service guaranteeing a provide a response within 300ms for 99.9% of its requests for a peak client load of 500 requests per second. 
4. Amazon uses percentiles 99.9% for several of it's SLAs and not mean, average or median.

## Design considerations