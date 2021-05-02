Video: https://www.youtube.com/watch?v=V3ZPPPKEipA&list=PLQnljOFTspQUNnO4p00ua_C5mKTfldiYT&index=1

# Skills that a back-end engineer needs

1. Communication protocols: 
	- Lowest we can get is understand TCP, UDP and IP.
	- Why is TCP is slow, why is it connection-oriented.
	- How HTTP works on top of TCP? What is it exactly?
	- To understand why is it slow to open 700 connections
	- Why would we use Websockets - Bidirectional web communication. Like we are building a chat application, a game
	- What is gRPC? Why would we use it? Any protocol is implemented because of some discrepancies with previous protocols


2. Web servers:
	a. Ability to serve content (static or dynamic)
	b. Different types of web servers. 
	c. Is it a single-threaded (Node.js) or multi-threaded like an Apache Webserver?
	d. Nginx acts like a Web server or a reverse proxy

3. Database Engineering
	- Log Structured Merge trees in index text-based searching.
	- Relational Databases (B-Tree) based
	- ACID should be understand. 
	- NoSQL scalability and availability

4. Proxies:
	- Reverse proxy - Caching layers, load balancers, TLS terminations
	- Anything that makes a request on behalf of a client or anything that receives a request and makes a request to other servers that hides the identity of the original client or hides the destination of the destination server
	- Layer-4, 3 and 7 proxying
	- Forward Proxy Vs Reverse Proxy
	- Why do we have layered proxies?
	- Service meshes are proxies that are Forward + Reverse that have become popular due to microservice architectures.


5. Caching: 
	- Reverse proxies are inherently caching layers
	- When to use caching?
	- Databases are designed for caching
	- Stateful Vs Stateless caches
	- Cache eviction is one of the hardest problems in Computer science

6. Messaging systems:
	- System communicating with each other to coordinate communication
	- Queue - Simple elegant FIFO queue. eg. ZeroMQ, RabbitMQ
	- Pub-Sub systems such as Kafka
	- Why did we build a messaging system? Where does it exist in a system architecture?

7. API Web frameworks:
	- Designed to build web APIs (REST/gRPC)

8. Message Formats:
	- Protocol Buffers, XML, JSONs
	- Human-readable message formats - XML, little bit friendlier format that is JsON
	- Why Protobuffs -> to send fewer bytes over the wire

9. Security
	- Encryption
	- How do you communicate between nodes to stop man-in-the-middle, replay attacks, credential leaks
	- Firewalls
	- Denial of Service attacks
	- Web Security / Network security