Video: https://www.youtube.com/watch?v=cODCpXtPHbQ (08:36)


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