Video: https://www.youtube.com/watch?v=Cie5v59mrTg&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=8(08:25)

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

## Spin RabbitMQ server with Docker



