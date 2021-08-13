Video: https://www.youtube.com/watch?v=M3XQ6yEC51Q&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=1 (12:10)

# REST

1. Architecture that became immensely popular for designing web APIs.
2. It was a dissertation of Roy Fielding

## Representation
1. Back-end API represents way to manipulate and view a resource say users. The API gives the user in a representation (JSON/Protobuff). The back-end may store data in any format (CSV, Cassandra row, Oracle)
2. The API representation decouples the back-end storage representation. 

## State Transfer
1. Don't store state information on the server, all state information should be stored in a database on the server side.
2. Each request should include all information needed by the server to process the request for stateless requests. For stateful requests, a unique identifier is sent that identifies this user that associates a state over the previous requests that the user has sent.
3. Stateless requests are good for load-balancing.

## REST Constraints
1. Client-Server
   - Client and server must be independent
   - Server drives the whole flow if you upgrade the server, this should not affect the client. Separation of concerns

2. Statelessness
   - Can restart the server any time and the next request should be accepted.

3. Cacheability
   - Should be able to cache resource. 
   - Server should be able to tell when to cache and when it's a stale resource.

4. Layer Systems
   - Should work normally irrespective of how many proxies, load balancers and caching layers we add.
   - HTTP protocol should support proxying

5. Uniform interface (HATEOS - Hyper media as the engine of application state)
   - HATEOS: One URL would show all the endpoints available to manipulate all REST available resource URIs. The client does not hard-code any URL. For example: To find the connections of a given user, first you call http://api.facebook.com/ , GET the actual endpoint for user connections and then call it. GraphQL was developed just to counter this.
   - Uniform HTTP methods querying interface for common CRUD operations on resources.
   - GraphQL replaced REST for certain workflows.
