Video: https://www.youtube.com/watch?v=ixuSv0k-jWU(28:00)

# Communication Protocols in Database systems


## Conventional 90s single instance applications
1. Initially the communication between databases and client/server was designed to be 1:1. 
2. We just have TCP/IP used by all databases as a medium of communication between the DBMS server and clients. Databases can be strictly local like RocksDB and SQLite which are single instance DB writing to a local database only.
3. Started with applications logging into databases through specific database users. We would spin up multiple TCP connections to your database and execute queries concurrently instead of running synchronously on the same socket. All databases build their own binary protocol on top of TCP. Oracle has OCI, Redis has RESP, each DB have their own protocol
4. Same databases have SAML credentials to authentication. So application could login using the Windows credentials of a user instead of having a separate user.


## Web-Tier
1. Things changed with the Web tier. When we give a URL to a website to a user, does he have to know the database username? We cannot create a DB user for every user that visits our website. We just create one user for the database that's baked in the application (Server tier) and the application uses internally.
2. If 1000 users visit our website, should we spin up multiple TCP connection to serve queries? Clients have now become very thin, they spin up and multiply very quickly.

## Connection Pooling
1. Single Connection model shares a TCP connection across clients. Async frameworks like Node.js will send thousands of queries on the same TCP connection. Here, we have no idea whether Request 1, Request 2, Request 3 will finish first. All of a sudden, response of Request 2 is sent to the place expecting response for request 2. We never ever execute two queries on a single TCP connection parallely at the same time in parallel. We execute a query, we wait till we get a response, that socket remains busy till that response is received. That's why we moved to HTTP/1.1 with pipelining, one TCP connection with multiple streams.
2. Pooling: When an application starts it maintains a set of 20 TCP connections to the database with a variable marking it's state. Web application would pick one connection out of the pool, and marks it busy. For another query, it picks another connection of the pool. When you hit the limit of 20, either your application waits or it creates a new TCP connection, adds it to the pool and uses it. Pooling is expensive, cos we've to pre-heat to avoid problem of cold start (initial delay in servicing requests due to restart. Mostly when the cache is cleared, there won't be any data in cache so first requests will take longer). 
3. Cost of establishing TCP connection is expensive and also the memory to maintain so many TCP connections. Race conditions can occur if two or more threads access/set the status of the same connection.

## Database connection multiplexing
1. Multiplexing multiple connections onto a same TCP connection such as HTTP/2. If we tag every request with a channel or a stream, you can build multiple connection within this TCP connection.
2. QUIC solved the head-of-line blocking, allowed multiple logical connections. Can we use Quic to multiplex database connection requests. Today, no DB supports multiplexing, it's designed to have a single TCP connection built by a client. One connection multiple channels was designed by RabbitMQ. Databases may not handle so much throughput had we supported multiplexing. Spanner did a great job with atomic clocks.


## Databases and high concurrency
1. ACID databases  need to maintain concurrency. 
2. Long transactions kill the database performance. Rows are write-locked in a transaction while its being edited.
3. Do transactions see uncommited values of other transactions? We need to maintain the old version of a row. Undo log is maintained in Oracle/MySQL to maintain old values of a row. PostgreSQL keeps the old row till it's commited.
4. Isolation is expensive. 
5. LucidChart decided to move to HTTP/2 for their back-end and their CPU shot up. They did'nt put up limits on the number of streams per TCP connection. More requests -> More load on the back-end.