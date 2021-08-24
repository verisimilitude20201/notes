Video:  https://www.youtube.com/watch?v=fVmQCnQ_EPs&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=5(07:00)

# GraphQL
1. Open-source query language developed by Facebook to fetch different query results from APIs
2. Main goal is to combine multiple service endpoints into a single endpoint.

## What is GraphQL
1. Developed by Facebook in 2012 because of REST limitation
2. Facebook is a Graph-based system. Users -> makes friends -> each friend uploads photos -> Each photo has likes -> Each like has a count and timestamp and who liked. 
3. So for a query like given a post, give it's like count, get it's comments also get the resource count. So there will be one API call for GET to get the POST details, GET the like endpoint to get like count and user details who liked, GET the comments by calling the comments REST endpoint. So one requirement, 3 calls to 3 different services.
4. REST is schemaless, does'nt enforce a schema. If you are asking for a post, you get every property with a post and the response payload is huge, the deserialization is expensive and not every attribute is in use.
5. They built a query language

## Examples (github API)

## Pros

## Cons

## When to use GraphQL over REST
