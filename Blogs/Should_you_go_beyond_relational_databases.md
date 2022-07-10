# Should you go beyond relational databases?


1. Pointers when you should look for beyond SQL

- Tables with lots of columns
- Attribute tables with foreign key combinations of (foreign key to row in another table, attribute name, attribute value) and you need ugly joins in your queries to deal with those tables)
- Just serializing data as XML/JSON and saving it in a column
- large number of many-to-many join tables or tree-like structures (a foreign key that refers to a different row in the same table)
- Frequently needing to make schema changes so that you can properly represent incoming data
- Are you reaching the limit of the write capacity of a single database server (If read capacity is your problem, you should set up master-slave replication. Also make sure that you have first given your database the fattest hardware you can afford, you have optimised your queries, and your schema cannot easily be split into shards.)
- Is your amount of data greater than a single server can sensibly hold
- Are your page loads being slowed down unacceptably by background batch processes ?


2. Document databases and BigTable

- BigTable data model is quite different from relational databases: columns don’t need to be pre-defined, and rows can be added with any set of columns. Empty columns are not stored at all.
- BigTable model inspired many to write their own implementations of this model: Cassandra, HBase are few examples.
- The lack of a pre-defined schema can make these databases attractive in applications where the attributes of objects are not known in advance, or change frequently
- Document databases have a related model, there is a difference in the way in which these handle scalability and distributed servers.
- Document databases can actually work with the structure of the documents, for example extracting, indexing, aggregating and filtering based on attribute values within the documents
- The disadvantage is that it does not support joins which is a design decision taken to give priority to scaling.


3. Graph databases
- Standard SQL cannot query transitive relationships, i.e. variable-length chains of joins which continue until some condition is reached.
- graph databases focus on the relationships between items — a better fit for highly interconnected data models
- Lesser Options: Neo4J, Janusgraph (which uses Cassandra as backing store)

4. MapReduce
- MapReduce is a way of writing batch processing jobs without having to worry about infrastructure
- Different databases lend themselves more or less well to MapReduce — something to keep in mind when choosing a database to fit your needs.
- Hadoop and CouchDB. Hadoop is an open source MapReduce implementation and CouchDB too supports it on a smaller scale though

5. Distributed key-value stores
- Simple HashTable. Key is a string and value can be anything, an opaque sequence of bytes.
- You don’t need to figure out a sharding scheme to decide on which server you can find a particular piece of data; the database can locate it for you. If one server dies, no problem — others can immediately take over. If you need more resources, just add servers to the cluster, and the database will automatically give them a share of the load and the data
- When choosing a key-value store you need to decide whether it should be opimised for low latency (for lightning-fast data access during your request-response cycle) or for high throughput (which is what you need for batch processing jobs).
- Scalaris, Dynomite and Ringo provide data consistency guarantees while taking care of distribution and partitioning. MemcacheDB focuses on latency

5. The caveat about limited transactions and joins applies even more strongly for distributed databases. Different implementations take different approaches, but in general, if you need to read several items, manipulate them in some way and then write them back, there is no guarantee that you will end up in a consistent state immediately (although many implementations try to become eventually consistent by resolving write conflicts or using distributed transaction protocols; see the algorithm of Amazon’s Dynamo for an example). You should therefore only use these databases if your data items are independent, and if availability and performance are more important than ACID properties