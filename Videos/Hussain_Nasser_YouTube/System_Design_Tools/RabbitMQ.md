Video: https://www.youtube.com/watch?v=Cie5v59mrTg&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=8(33:39)

# RabbitMQ

1. RabbitMQ is an open source distributed queue written in Erlang and supports many distributed protocols
2. It was trying to solve a problem with Spaghettic Mesh architecture (Enterprise message bus). 

## RabbitMQ Components
1. Used whenever clients want to talk to another clients in the same or external system. Each client does not need to have knowledge of another client.
2. The server uses 5672
3. Publisher establishes a stateful TCP connection between itself and the server. 
4. The actual protocol in use is AMQP - Advanced Message Queue Protocol.
5. There's a consumer who wants to consume a message from the server.
6. The Server pushes messages to the consumer when they are ready.
7. Channel is a logical connection in the system. They wanted to separate the consumer connection from multiple consumers. A consumer can have 3 channels inside the same TCP connection. This is called multiplexing. You bring a lot of stuff in one pipe.
8. Queues are another abstraction. Publisher and consumer are not aware of the queues. They are aware of an exchanges.
9. Exchanges rout messages to queues to fan-out, round-robin messages to different queues.
10. Consumer keeps on receiving the message till we acknowledge it.
11. RabbitMQ can guarantee at-least once or atmost once delivery. Not Exactly Once.
12. Too many abstractions: If you build a system with so many abstractions, if you want to add a new abstraction, you have to remove one. RabbitMQ has exchanges, queues, publishers, consumers, 3 protocols - AMQP, HTTP and lots of stuff to learn. Too complex lots of stuff to learn.
13. It's a Push model. When a consumer consumes from a queue, RabbitMQ pushes a message to the consumer. If we have a 1000 messages in queue, it involves more work on the server to push messages to the consumer to process them. Kafka does a long polling model - pushes the complexity to the consumer it does long polling - whenever the consumer is ready, consume. 




