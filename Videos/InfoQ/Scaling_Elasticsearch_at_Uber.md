https://www.infoq.com/presentations/uber-elasticsearch-clusters/ (18:00)
# Scaling Elasticsearch at Uber as an geo temporal database

## Why do we need a geo-temporal database
1. Uber app is used to search for and book a cab. 
2. Realtime traffic tracking at a global scale.
3. Dynamic Pricing: Every single region we need to make decisions on which areas are in high demand (divided in hexagons) and determine the price
4. We need a lot of metrics to make these decisions like how many Ubers were in a trip in past 10 mins. We also need to visualize how data changes over time and over spaces
5. Market time analysis - the travel times is also necessary.
6. Forecasting of rider demands based on a variety of algorithms
7. Questions
    - What kind of geotemporal databases are used for changing business needs?

## What is the right abstraction?
1. Single OLAP (Online analytics processing) on geo-temporal data: Select a bunch of functions,on a data source, apply filters, group by dimensions and sort them.
```
SELECT <agg functions>, <dimensions>
FROM <data source>
WHERE <boolean filter>
GROUP BY <dimensions>
HAVING <boolean filter>
ORDER BY <sorting criteria>
LIMIT <n>
```
2. Why elastic search
    - Arbitrary boolean query
    - Sub-second response time
    - Built-in distributed aggregated functions
    - High cardinality queries
    - Idempotent insertion to dedup data
    - Scales with data volume
    - Operable by small team
    - Data freshness should be secondly

3. Current scale
    Ingestion: 850K to 1.3M messages/sec
    Ingestion Volume: 12 TB per day
    Doc scans: 100 M to 4 Billion per second
    Cluster size: 700 ES nodes
    Ingestion pipeline: 100+ data pipeline jobs
The query load varies and so using QPS is misleading that's why doc scans is used

## Our story of Scaling Elastic search
1. Dimensions of scale
    - Ingestion: Data arrives as fast as possible and is made availaible almost instantly
    - Query: Response time should be fast
    - Operations: Should be as simple as possible
2. Driving principles
    - Optimize for fast iteration, to correct fast
    - Optimize for simplicity and transparency
    - Optimize for automations and tools 
    - Optimize for being reasonably fast: Our system is not about transactions. 
3. Started small: 
    - 3 person team
    - 2 data centers
    - Small set of requirements - Analytics for machines: Simple analytics/aggregation queries.
4. Firstly get single node right tuning side.
    - One table -> multiple indices by time range 
    - Disable _source which stores as entire document. 
    - Disale _all fields. No analysis on entire strings
    - Disable _doc_values
    - Disable analyze fields. Every value of a field is treated as a whole
    - Treat memory JVM parameters. 
5. Lot of decisions to be made with real numbers
    - What's the maximum number of recovery rate?
    - Refresh Rate?
    - Throttling rate?
    - Request Queue size? 
    - How many shards
    Set up end-to-end stress testing framework to tune these parameters not to generate data but also test various queries.
6. Deployment in Two Data centers
    - Each data center has exclusive set of cities
    - Should tolerate the failure of a data-center
    - Querying any city should return correct results
7. Kafka synchronization helps to keep both the Elastic search clusters in both data centers in sink. Both data centers handle all the data-sets. This is called trading space for availaibility
8. Discretize Geo Locations: H3 library divides the world into Hexagons and Larger hexagons can contain smaller hexagons. We don't have to deal with R-trees, Quad trees. Plain deal with integers which are simpler to deal with.
9. Optimizations to Ingestion: Apache Samza/Flink used for ingestion with Kafka sync keeping both ES clusters in data centers in sync
10. Dealing with large volumes of data:
    - An event source produces 3 TB per day
    - Key insight: A human does not need granular data. For example: We don't need to know that the price updates for an ecommerce site every millisecond
    - Key insight: Streaming data has a lot of redundancy For example: A driver sending his location every 4 seconds, it may not change. Within 60 mins, he sends 15 pings which may be redundant
    - Prune unecessary fields. 
    - Devise algorithms to remove redundancy to achieve 70% reduction
    - Bulk writes
11. Data modelling really matters: Think about data modelling to serve different analytic usecases efficiently.
    - Example: Efficient and reliable joins: Calculate the join of two event streams using the ratio of requested trips and completed trips
        - We can use a streaming join. Streaming join will capture both streams and when completed trips comes, it can compute the join but this is not the most efficient because a trip can go on till hours
        - We can use a simple data model where each document(trip_ip, start_time, is_completed) is the trip and keep a completed flag. On completion, the same trip document can be upserted and this ratio becomes a simple aggregaton query
    - Aggregation on state transitions: This can be performed on a single query if store these transitions by edges
12. Optimizing on Elastic search:
    - Hide query optimization from users: Use a isolation layer. ask users not to write elastic query since they can be expensive
    - Creation of different Elastic search clusters in the same data center according to priorities and have the querying layer facing them. This has below advantages
        - Can generate efficient elasticsearch queries
        - Rejects expensive queries: Can compute the cardinality of each query and reject expensive queries. 
        - Determine routing queries: Routing configuration can define routing of queries to different clusters and use-case and demand, index See below sample config
        ```
        DEMAND:
            CLUSTERS:
                TIER0: 
                    - CLUSTER 0
                TIER1
                    - CLUSTER 1
            INDEX:
                MARKETPLACE_DEMAND_*
        ```

### Experience failures
1. Unexpected Surges: A certain query caused billions of documents to be scanned which brought down the cluster
2. Application went haywire due to a surge of requests

### Few solutions to failures 

#### Distributed Rate limiting
1. Reasonable rate limiting per cluster specified configured dynamically
2. The rate limiter gets applied individually to each query node in the querying layer 

#### Workload evolved
1. Insight: Data can be stale for long running queries. Users query months of data for complex and analytical queries so if data is stale by a day, it hardly matters
2. This system evovled to came to be used for machine learning models
3. Timeseries cache:
    - Readaside cache
    - Based on Redis
    - Cache key is based on normalized query content and time range.
    - A key is generated on the basis of a query content and not time range because it can be relative
    - We go to the timeseries datastore for this query content and pick the proper time-range based on the content of the query and go to Redis
4. Delayed execution
    - Dedicated clusters and queued execution for long running queries
    - Provide cached data for certain queries
    - Rationale: Query 3 months of data

### Scaling operations
1. Make system transparent
2. Optimize for Mean time to recovery: First thing is to bring the system up and then analyze the issue
3. Strive for consistency
4. Focus on automations to ensure consistency

#### Challenge - Cluster size becomes an enemy
1. Default configuration with a small size of cluster works good enough.
2. As cluster size increases, MTTR increases.
3. Multi-tenancy becomes an issue
4. The solution is a federation of smaller elastic search cluster with dynamic routing and a schema service storing the schema as well as this routing. This schema service is contacted by ingestion layer as well as the querying layer.

#### Challenge - How can we trust the data in case of multiple clusters, replicated data
1. Self-serving trust system: A rule services takes in user validation scripts and runs them on the data and displays the results in the form of dashboards

#### Challenge - Too much manual maintenance work
Auto-Ops is a component that continously monitors alers and metrics and takes decisions whether to restart query nodes/servers, readjust the queue size and so on.

## Summary
1. Three dimensions of scaling: ingestion, query and operations
2. Be simple and practical when designing systems
3. Abstraction & data modelling matter
4. Invest in thorough instrumentation
5. Invest in automation tools
