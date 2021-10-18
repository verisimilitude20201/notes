Video: https://www.youtube.com/watch?v=l_P6m3JTyp0(51:07)

# REST, gRPC or GraphQL ? Resolving the API developer's dilemma
1. 2010 we used to say SOAP is dead and REST is en vogue
2. 2015 we said REST is dead and GraphQL is en vogue
3. Let's not stick with one technology, rather let's just use one efficient one for the use case at hand
4. APIs we need to have contract between two parties. We need to have protocols and specifications for communicating between two separate parties.

## API styles over time
1. 1991 - Corba: RPC framework and one of the oldest style of distributed computing. Deemed the future of eCommerce. Fell out of favor for XML
2. 1993 - Remote Data access: Way of querying data in a database over a network interface. It's a query interface. 
3. 1998 XML-RPC: Formal spec on RPC spec based on XML. 
4. 1999 SOAP: Microsoft released SOAP based on XML-RPC and WSDL
5. 2000 - REST: Roy Fielding released the REST disseration. Sometimes SOAP faded into background and sometimes REST. 
6. 2005 JSON-RPC: JSON started picking up rather than XML
7. 2007 ODATA: Released by microsoft similar to Remote Data Access. Client can define any shape of data that they want rather than simple query.
8. 2015 GraphQL: Influenced by ODATA. Query APIs from the Read side.
9. 2016 gRPC: Very much performance and flexibility built on JSON-RPC and XML-RPC

## API Paradigm shifts
1. In the 2000s, the APIs were built for one provider and one consumer
2. 2010 Second wave generic APIs were built for many consumers and these were very flexible APIs
3. The next wave of APIs were more autonomous allowing more flexibility

## API Styles
1. Query APIs: Querying various back-end services
2. Streaming APIs: Streaming videos, stock price updates
3. RPC APIs: gRPC APIs pretending one component calling another component in a distributed network always pretending that component is not distributed. 
4. Web APIs: HTTP clients that brace the web
5. Flat File APIs: That which accept files as input or provide reports in the form of output files

## Which APIs to choose?
1. There is no best API style, we always choose one depending on the shape of the characteristics of the problem at hand and work backwards. Don't choose a technology and fit a problem to it.
2. Constraints define which APIs to build
   - Properties are induced by the set of constraints within an architecture. For eg: Uniform interface is a constraint of REST. Any resource can be accessed by a canonical URI and HTTP verbs to interact with it.  
   - Business constraints: Who are you building for? Customer/Business/Product Requirements. 
   - Technical constraints: How the system is going to operate? Various trade-offs, implications of Distributed computing, 8 Fallacies of distributed computing, evolutionary requirements
   - Socialtechnical constraints: How do we keep products sustainable over time? Knowledge, expertise, System of work, Conway's law: how to align teams to the products that we build
   - (Business + Technical + Socialtechnical) Constraints => (Software System, Ecosystem) => Properties

3. What is GraphQL? Remote Data Access + RPC => GraphQL. HTTP/REST semantics are thrown in. GraphQL is language agnostic.
4. HTTP/2 (Transport) + ProtoBuf(Interface Definition Language + Data Format/API Contract) = gRPC. Leverages lots of capabilities of HTTP/2 including streaming. Suports request-response, Streaming(both-side or bi-directional). Wonderful for realtime interactons.
5. REST is State-machine over HTTP (not restricted to HTTP protocol). State machine implies hypermedia controls. RESTful response can have links so that the client can visit them and determine where to go next. Each link has pre-defined semantics on the server side that the client is aware of. Client controls the valid state transitions. REST requires HATEOS(Hypermedia as the engine of application state). Don't build RESTish APIs.
6. If an API is mostly actions, maybe it should be RPC. If an action is mostly CRUD and manipulating related data, maybe it should be REST. For example: Slack API is mostly action-based. If we do it using REST, we have APIs like /post/like/increment
7. GraphQL is not the best choice for server-to-server communications. gRPC, REST and Thrift is faster for that domain
8. GraphQL now allows GET queries and may also allow persisted queries. No GraphQL spec for HTTP
9. Whether APIs are authenticated or not. Intermediary caches will not cache authenticated traffic.
10. More customizable your API is, less cacheable it would be. For example: /users/1 is cacheable. If we add more number of parameters to it, it reduces the cacheability. With GraphQL, you have infinite ways to query. If caching is not important for you, GraphQL is the way to go. If network caching is valuable, use REST.
11. REST APIs are inefficient?
   - REST was never about efficiency. It was for systems that could evolve over decades, reduced client coupling, longevity in client-server architectures.
   - HTTP/2 has remove a lot of pain with REST APIs.
   - OVER/UNder fetching: If we may have just one representation we may be serving a large data. If we have a parent record and we dereference it's children independently, we under-fetch (N+1 problem). For overfetching, we can allow clients to fields it wants. For underfetching, we can use compound documents.
   - GraphQL enables each client to retrieve exactly what data it requires in a single round-trip server. Slowest field dictates overall response time. Deferred queries can be used. 
12. HTTP/1.1 was expensive to make network connection for every request. HTTP/2 resolved huge amount of these, we can create multiple streams over a single connection. HTTP/2 also has server push which makes the server to push resources to the client without it asking for them. If client requests GET /users, server can proactively push the count of the users. Library volcane. GRPC uses HTTP/2 heavily for streaming.
13. GraphQL eliminates the need for versioning. Versioning was a strategy to prevent breaking API changes. You'd changed the contract on the server and you wanted to insulate the clients from that change. Client could decide if they want to adopt that new version. Graceful evolution should always be the goal. Supporting multiple versions gets easily complicated. For graceful evolution - don't add required inputs, don't remove outputs or make them optional. Don't change the type of a field. Follow Postel's law - Robustness principle. Be conservative in what you give out and liberal with what you accept. No over communication with users.
13. Client should not break if the server sends additional fields or changes the order of the fields. Tolerant readers are model clients. GraphQL can mark certain fields if they are to be deprecated.
14. With a sufficient number of users of an API, it does not matter what you promise in the contract, all observable behaviours of your system will depending on by somebody - Hyrem's law. So look not only for contractual changes but also for behavioral changes
15. Make a system evolvable by makking the interfaces right. Use a contract-first design. GraphQL  (Schema), gRPC(has protocol buffers), REST (Open API, RAML, Blueprints) as the common contract. Then that can be integrated into the app and product. Shared design discussions based on contract between producers and consumers.
16. REST and gRPC the server decides the shape of the queries and then design them out. Always design with your users front of mind. GraphQL delays the last responsible moment for identifying query needs. GraphQL doesnt define individual operations or endpoints rather we define a graph of data. Each query maps to a section of that Graph. The con is higher cost but it's a great way given if you don't know how users will query your data.

## When to choose which API style
1. API styles address different problem spaces
2. Ask whether RESTish APIs would be better as GraphQL or RPC
3. gRPC is perfect for synchronous communication between internal services
4. GraphQL breaks caching. Different types of caching - client side, server side.

## HTTP Caching
1. With HTTP Caching, we can intermediate caching proxies like Squid or Varnish.
2. With GraphQL intermediate caching proxies may not be possible. It uses POST.
3. If you get data, localise the data, it will be faster.


## Sample Scenarios
1. Application Web Form: CRUD / REST. 
2. Composite API: Aggregate data from multiple sources into one convenient API. Perfect for GraphQL - multiple clients with heterogenous data requirements.    

          GraphQL               ====>  (Microservice, Relational DB, Graph DB, Web Service)

3. Native mobile Backend-For-Froentend: APIs owned by the client team. One protocol end-to-end in Stack. Can use HTTP/2 end-to-end. Pure gRPC cannot be used in a browser.
4. Polyglot microservices: gRPC is perfect for synchronous microservices.
5. gRPC-Web: Purpose of gRPC-Web is to create compatible traffic over HTTP/1.1. Need a gRPC Proxy like Envoy to convert HTTP/1.1 to HTTP/2.0. There is a component that does this in .NET that without an intermediary proxy.