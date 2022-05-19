Video: 

- https://www.youtube.com/watch?v=r-4Xj2Fz6MQ&list=PLYp4IGUhNFmw8USiYMJvCUjZe79fvyYge&index=8
- https://www.youtube.com/watch?v=n2tuTtgkSlI&list=PLYp4IGUhNFmw8USiYMJvCUjZe79fvyYge&index=9 
- https://www.youtube.com/watch?v=br5J8MsduJo&list=PLYp4IGUhNFmw8USiYMJvCUjZe79fvyYge&index=10




1. Basic Single-Table Queries

SELECT [DISTINCT] <Column expression list>
FROM <Single Table>
[WHERE <Predicate>]
[GROUP BY <column list>]
[HAVING <predicate>]
[ORDER BY <column list>]
[LIMIT <count of rows>]


2. DISTINCT selects distinct rows

3. Typically we use ORDER BY with LIMIT. Without an order by, the LIMIT is non-deterministic. LIMIT is not a purely declarative construct.

4. GROUP BY partitions the table into groups with the same GROUP BY column values. Produce an aggregate result per group.

5. HAVING predicate filters groups 

6. SELECT COUNT(DISTINCT name) and SELECT DISTINCT COUNT(name) are different.

7. Modern SQL extends pure relational model by adding support for duplicate rows, ordering of output, aggregates

8. Typically many ways to write a query, DBMS is responsible for figuring out a fast way.