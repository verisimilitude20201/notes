Link: https://bravenewgeek.com/you-cannot-have-exactly-once-delivery/
========================================================================

# You Cannot have exactly once delivery
1. People do have some fallacies about Distributed systems and it's frustrating at time to try to communicate them while discussing the constraints.
2. Within the context of a distributed system, you cannot have exactly-once semantics. Essentially there are 3 semantics - at-least once, ut-most once and exactly once. 
3. Distributed system is all about trade-offs. In a way, even at-least once is impossible because network partitions are not time bounded. But if we assume a model in which network resurrects itself, we can assume at-least once possible. 

## Why is exactly once impossible
1. Consider the Byzantine Generals Problem and the FLP result which says that it's impossible for a set of processes to agree if we have at-least one faulty process. 
2. I send you a letter asking you to call me as soon as you receive it. If I don't get a call, I don't know if the letter received you / if it got received you cared for the letter or no. I can send one letter or I can send 10 letters and assume you get one of them. Same case with distributed systems, if a node A sends a message to node B and does not receive an ack, either node B is down or the message got lost in the network. FLP and Two Generals are impossibility results.

3. The meaning of delivery is twisted to mean something different, to fit people's semantics of exactly-once.

4. Atomic broadcast protocols ensure messages are delivered reliably and in order. The truth is, we can’t deliver messages reliably and in order in the face of network partitions and crashes without a high degree of coordination.  This coordination, of course, comes at a cost (latency and availability), while still relying on at-least-once semantics.

5. State changes are idempotent and as long as application order is consistent with the delivery order applying the same state does'nt lead to inconsistencies. So at-least once semantics is fine here. 

6. When a message is delivered, it’s acknowledged immediately before processing. The sender receives the ack and calls it a day. However, if the receiver crashes before or during its processing, that data is lost forever. Customer transaction? Sorry, looks like you’re not getting your order. This is the worldview of at-most-once delivert

7.  If there are multiple workers processing tasks or the work queues are replicated, the broker must be strongly consistent (or CP in CAP theorem parlance) so as to ensure a task is not delivered to any other workers once it’s been acked. Apache Kafka uses ZooKeeper to handle this coordination

8. We can acknowledge messages after they are processed. If the process crashes after handling a message but before acking (or the ack isn’t delivered), the sender will redeliver. Hello, at-least-once delivery. Furthermore, if you want to deliver messages in order to more than one site, you need an atomic broadcast which is a huge burden on throughput. Fast or consistent. Welcome to the world of distributed systems

9. Every major message queue in existence which provides any guarantees will market itself as at-least-once delivery. For example: RabbitMQ says that any producers recovering from channel failures should retransmit messages for which they have'nt received any acknowledgement. It's the consumer's job to dedup and filter out duplicate messages.

10. The way we achieve exactly-once delivery in practice is by faking it. Either the messages themselves should be idempotent, meaning they can be applied more than once without adverse effects, or we remove the need for idempotency through deduplication. Ideally, our messages don’t require strict ordering and are commutative instead. There are design implications and trade-offs involved with whichever route you take, but this is the reality in which we must live

11. Rethinking operations as idempotent actions might be easier said than done, but it mostly requires a change in the way we think about state. This is best described by revisiting the replicated state machine. Rather than distributing operations to apply at various nodes, what if we just distribute the state changes themselves? Rather than mutating state, let’s just report facts at various points in time

12. Imagine we want to tell a friend to come pick us up. We send him a series of text messages with turn-by-turn directions, but one of the messages is delivered twice! Our friend isn’t too happy when he finds himself in the bad part of town. Instead, let’s just tell him where we are and let him figure it out. If the message gets delivered more than once, it won’t matter. The implications are wider reaching than this, since we’re still concerned with the ordering of messages, which is why solutions like commutative and convergent replicated data types are becoming more popular. That said, we can typically solve this problem through extrinsic means like sequencing, vector clocks, or other partial-ordering mechanisms

11. To reiterate, there is no such thing as exactly-once delivery. We must choose between the lesser of two evils, which is at-least-once delivery in most cases. This can be used to simulate exactly-once semantics by ensuring idempotency or otherwise eliminating side effects from operations.

