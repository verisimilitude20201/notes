Video: https://www.youtube.com/watch?v=O1PgqUqZKTA&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=2(15:00)

# Publish Subscribe Vs Request Response

## Request-response
1. Designed in the 90s whole internet works on this. 
2. Client makes a request to the server and stays blocked till the server satisfies the request. Once request comes back, the client does something with the content. At times, the client isn't blocked, it handles this asyncly, after initiating the request it does other tasks.
3. Where does this break? 
 - Consider a video uploader service having Upload Service (handles upload), Compress Service(compresses the video), Format Service(converts videos into different formats on the basis of viewing platforms) & Notification service (notification service to notify the client that video is successfully uploaded)
 - If this uses Request-Response, the client will need to get indefinitely for the video to get uploaded and ready to be consumed.
 - If you are chaining multiple services servicing a request, the Request-Response fails.

### Request response Pros
1. Elegant and Simple
2. Stateless (HTTP)
3. Scalable - can scale horizontally to handle requests. Put it behind a load balancer.

### Request response Cons
1. Bad for multiple receivers
2. High coupling: Services start knowing each other. Software should have social anxiety. It should be as oblivious as possible about the whole system.
3. Client and Server have to be running.
4. Chaining, circuit breaking


## Pub Subb
1. Same set of services but we introduce a messaging broker. Message broker accepts data from a producer which publishes it to a topic and Consumers subscribe to a topic of their interest and start consuming from it.
    - Upload service handles the video uploading in the back-ground. Client is given an acknowledgement once it sends a video request to this service.
    - Uploader services copies the video to a distributed storage and adds a message to a compressor topic on the message broker
    - Compresser service listens to this topic. It downloads the video from storage, compresses and reuploads it back to storage. It adds a message to format topic.
    - Format Service consumes from this topic and downloads the video from storage. It converts the video into different formats - 4K, 160 dpi, 480 dpi and pushes them to the storage. It adds a message to the notification topic once done
    - Notification service notifies the client when the entire process is done.
2. In this case, the entire process is decoupled and asynchronous


### Pros
1. Scales with multiple distinct receivers.
2. Great for microservices. Can avoid spaghetti topology of everything connected to everything.
3. Loose coupling. Things can be added and system can be modified easily.
4. Works while clients is not running.

### Cons
1. Message Delivery issues (2-Generals Problems): Producer does'nt know whether the consumer has consumed the message successfully.
2. Complex: 
  - Back-pressure to restrict the producer to reduce it's flow so to allow the consumer to process. 
  - Push model of pushing events to subscriber is complex and that too when the subscriber is down.
  - Pull model of processing i.e periodically polling the broker to ask do you have a message? 
  - Kafka uses long polling. 
3. Network saturation: Shoving the network with a huge amount of content. Consumer may fail processing some of the content in which case you retry.