Video: https://www.youtube.com/watch?v=O1PgqUqZKTA&list=PLQnljOFTspQUNnO4p00ua_C5mKTfldiYT&index=14
# Request-Response Vs PubSub

## Where Request-Response breaks
1. Client makes a request and once its done a request, it waits for response. Server provides a client response.
2. If you upload a video to Youtube, different things happen in background - video gets compressed, gets converted into different formats (720, 480, 4K). We cannot be blocked till all these processes complete. So request response can't be used here. 

## Request-Response Pros
1. Elegant & Simple
2. Stateless (HTTP)
3. Scalable (On a load balancer) at the receiver's end. Horizontal Scalability is implied here

## Request-Response Cons
1. Very bad for multiple consumers.
2. High coupling. Software should have social anxiety. It should be as oblivious as possible 
3. Client and server has to be running to communicate.
4. Chaining, circuit breaking. Time-out correctly, retry correctly.

## PubSub Via Youtube example:
1. Uploader service publishes a message on a PubSub system once video gets fully uploaded. 
2. Compressor service first picks that message and compresses the video. 
3. Once done, it pushes a message for Format service. Format service picks it up and converts it into different formats. Finally, it publishes a message for the Notification Service to notify the user. 4. Initially, too we can notify the user once the upload service completes uploading the video and leaving rest of the stuff for the background.

## PubSub Pros:
1. Scales well with multiple receivers having different needs, wants.
2. Great for microservices.
3. Loose coupling, producers and consumers not aware of each other's existence. More loose coupling, more easily the system can scale
4. Works while clients not running

## PubSub Cons

1. Message delivery issues: How does the Producer know the consumer has consumed a message?How do you know that the Consumer got actually the content?
2. Complex system
    - Two-way communication protocol (AQMP for RabbitMQ). Push model where in Producer pushes the message to the consumer via this protocol
    - Back-pressure: Publisher can be so fast and producing content that the consumer can be overwhelmed.
    - Polling: Polling the broker for messages, lots of empty response cycles. 
    - Long Polling: Make a request and block yourself till a new message comes. Used by Kafka. May not be best near real-time.
    - Network saturation