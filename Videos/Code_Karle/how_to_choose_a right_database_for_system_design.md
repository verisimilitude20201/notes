Video: https://www.youtube.com/watch?v=cODCpXtPHbQ (16:17)


# How to choose a database for a system design interview

1. Functional databases can be satisfied by choosing any database. Non-functional databases are impacted by the choice of your database.

2. Choice of database depends on 
 a. Structure of data: If it's a totally structured or semi-structured data.
 b. Query pattern
 c. Amount of scale.

## Caching solutions
1. Don't want to query a database and repeatedly query for the same data. 
2. Locally cache a network service's response.
3. Normally has a key: value structure.
4. Can use Redis, Hazelcast, etcd, Memcached

## File storage options
1. Data store to store Product images, videos like Amazon. Specific to Netflix videos. 
2. Blob storage for all file based storage for serving files.
3. Amazon S3 common and fairly cheap.
4. Content Delivery network distributes the same content in geographically different regions. For example: Amazon's product image can be stored in S3 as a primary store and distributed to all Amazon servers the whole over for people to access. Mainly accessing content local to the people using it.

## Full Text searching capability
1. Searching products/descriptions on Amazon. Netflix offering searching capabilities based on genre, video titles, genres. 
2. Very Common -> Elasticsearch and Solr (Built on top of Lucene)
3. Fuzzy Search: Searching for words with wrong spelling, synonyms. For example: search for "Airport", you search for "Aurport". You can specify the fuzzy edit distance as 2 and this will work
4. Not databases but search engines. ES does not give any such guarantees about durability. Primary source of truth should be somewhere else.

# Metrics kind of data
1. Application metrics tracking systems. Lot of applications are pushing metrics related to their throughput, disk utilization, memory, CPU utilization. 
2. We then think of Timeseries databases as an extention of relational databases. Regular relational databases give capability to randomly update date and query it.
3. Timeseries are append-only write mode. Read queries query for last hours data, last years data and so on.
4. For example: InfluxDB, Prometheus, OpenTSDB

# Analytics requirements
1. Various kinds of analytics requirements like what geographies are ordering what products, what is the most sought after item and stuff like that (Amazon)
2. We need a Data Warehouse wherein we can dump all the data in a company and run various sorts of queries on them.
3. All transactional systems dump data into the data warehouse and we build capabilities to provide reporting capabilities on top of it.
4. For example: Hadoop

# Scenarios used for Relational and non-relational databases.

1. Structure of the data decides the database. 
2. If the information can be modelled in a tabular format, then go with a relational format.
3. If you need ACID guarantees you need a relational database. For example: Payment system, inventory management system. Can use any RDBMS: MySQL, PostgreSQL, Oracle.
4. If you have a user profile information, there are no hard-fast guarantees that we should go for ACID. So you can use either RDBMS or NoSQL
5. If you are buiding a catalog of items for a shopping site such as Amazon, each item has different sets of attributes. You need to go with Document DB. Document DBs are optimized for storing documents that have a wide variety of attributes and wide variety of queries that can be supported on them. Lot of providers of document DBs - MongoDBs
6. Ever increasing data. For example: Uber drivers continously send locations. These locations would'nt increase in a linear fashion. We would need to find location for a specific driver ID. So if you have less number of queries but more data, we could use Columnar databases (HBase/Cassandra). HBase has a lot of components dependencies to install.
7. If we don't have ACID, no multiple data-types no ever increasing data, then you can use any database for that matter. 
8. We want to ensure that we don't want to oversell an item from inventory (Amazon). RDBMS can be used to manage the inventory. But each day the number of orders of Amazon are ever increasing and so. We could use a combination of RDBMS and Cassandra. RDBMS can be used to store a recently placed order that has'nt been delivered. Upon delivery, we can shift that to Cassandra as a permanent store.
9. Let's say Amazon wants to find the number of users who bought sugar in the last 5 days. Sugars can be of diffrent types. So multiple item. So subset of order information can be stored in MongoDB. You can get order IDs from the document DB and query them in RDBMS or Cassandra.