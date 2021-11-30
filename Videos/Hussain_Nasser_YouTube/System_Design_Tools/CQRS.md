# CQRS

1. Command Query Responsibility segragation is the basic idea of separating reads from writes.
2. Don't use a single server or service to host or server your writes or reads
3. Without segragating anything, you have a customer service that does add, update, getCustomers() and getRecommendations(), total()
4. We can separate the writes and reads into different services for adding and updating and deleting customers and for getCustomers() and getRecommendations(), total().
5. We can separate the both database users. For write different database user and for read different user. 
6. We can even have different databses for both for write OLTP database and for read OLAP databases.
7. We can even scale the different microservices scaling more for reads.
8. Cons are complexity. Sometimes your workload is normal CRUD. Some writes actually need reads which does not make any sense in some workloads.