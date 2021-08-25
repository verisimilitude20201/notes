Video:  https://www.youtube.com/watch?v=fVmQCnQ_EPs&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=5(20:35)

# GraphQL
1. Open-source query language developed by Facebook to fetch different query results from APIs
2. Main goal is to combine multiple service endpoints into a single endpoint.

## What is GraphQL
1. Developed by Facebook in 2012 because of REST limitation
2. Facebook is a Graph-based system. Users -> makes friends -> each friend uploads photos -> Each photo has likes -> Each like has a count and timestamp and who liked. 
3. So for a query like given a post, give it's like count, get it's comments also get the resource count. So there will be one API call for GET to get the POST details, GET the like endpoint to get like count and user details who liked, GET the comments by calling the comments REST endpoint. So one requirement, 3 calls to 3 different services.
4. REST is schemaless, does'nt enforce a schema. If you are asking for a post, you get every property with a post and the response payload is huge, the deserialization is expensive and not every attribute is in use.
5. Facebook changed their front-end from Web to mobile to tablets. They needed a single new tool to be flexible enough to specify what resource and what attributes of that resource are needed in the query. 
6. They invented GraphQL as a query language to do just this. Designed for Web APIs.
7. Schema-based and heavily typed. REST is schemaless and it does'nt provide ability to provide what you want.
8. Transport agnostic (Built on top of HTTP by default, but can even use Websockets and other protocols)

## Properties
1. Schema: Can interrogate the API to get the objects and types of attributes of those object. This is called schema introspection. You can make a GET request for this.The rest of the GET request can be very huge if the query is huge. So in that case we use POST. POST requests can't be cached though.
2. Query: Can use query on the schema to specify the type of objects and attributes to fetch
3. Nesting: Same query you can nest. For example: Get a Facebook Post for all my friends and get the top 3 most recent post. 
4. Mutation: Can change the back-end. 
5. Subscription: Client can subscribe to changes to a certain resource.

## Examples (github API)

## Pros

## Cons

## When to use GraphQL over REST
