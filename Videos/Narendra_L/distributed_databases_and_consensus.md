Video: https://www.youtube.com/watch?v=S4FnmSeRpAY (09:40)

# Distributed Transactions
Single database transactions are easy - it's all or nothing. But if we have more than one database and still we want atomicity/consistency it's a bit challenging.

## Example of a monolithic Amazon order occuring on a single database.
1. User places order
2. Amazon intiates a transaction
   - Updates balance wallet.
   - Update order
   - Commit
3. Amazon notifies the user that the order got placed successfully.
4. In case of any error (no sufficient balance, no products in inventory), nothing will be commited. 

## Example of a micro-service based architecture with different databases.
1. We have 3 components here - orchestrator, customer wallet, order having their own database
2. Customer places an order
  - Orchestrator contacts customer wallet service to check the account balance. 
  - If the customer has sufficient balance, then the orchestrator calls the order service to create a new order. 
  - On success, it then returns success to the customer.
4. Here in this case, each microservice has it's own database so single transaction across database may not be possible.
5. What if one more order for the same user gets in the system before the order gets placed. One more scenario that can happen is the balance can get updated but the order fails cause there is no product remaining in the inventory.

### Some ways to solve the distributed transaction problem.
1. We cannot connect two services to the same database. But it's an anti-pattern in micro-service design.
2. We can have a replication strategy connecting the two databases.

## Some algorithms for distributed transactions

### 2-Phase commit 
1. Have two phases - prepare and commit.
2. We have a coordinator node that coordinates the implementation of 2-Phase commits across nodes.
   - Coordinator creates a transaction id
   - It will initiate a prepare on the customer wallet and the order inventory. It will lock the wallet and the inventory and for this customer.
   - Both databases send an ok-prepare to the coordinator node. Coordinator will be waiting for both the prepare will be completed.
   - Coordinator will then issue a commit to both the DBs and respond back with ok-commit.
   - If both the DBs respond with an ok-commit, then only the transaction will be commited otherwise it will be rolled-back
3. Error scenarios
   - If the customer does not have enough balance, the customer wallet service does'nt return an ok-prepare in the first phase. 
4. In the prepare phase itself, both DBs will lock both rows
5. Coordinator should have a timeout. Coordinator should'nt keep on waiting forever for both DBs to return ok-prepare and ok-commit.
6. This has strong consistency but it uses locks and difficult to handle case where coordinator itself goes down.

### 3-Phase commit
1. Has an extra pre-commit step that handles cases of coordinator failures or participant failures.
2. Stages
  - Pre-commit: Coordinator understands how many participants are there.
  - Commit phase: Coordinator asks every participant to commit and waits for each participant to do so.
  - Finalize phase: Coordinator commits the whole transaction depending on whether all participant respond ok-commit within a timeout period.
3. New coordinator (in case of coordinator failure) talks to all participants to understand the current state. Depending on how the participants respond, the new coordinator will pick up from that phase.


### Saga 
1. Order service adds a message to the queue after checking the inventory. 
2. Customer wallet service takes up from there and deducts the customer account balance. If the customer does'nt have sufficient balance or payment details are invalid, customer wallet service adds a different message to a different topic for order service to roll back.
3. No locks are used, all communication based on the event bus.
4. All messages are sequential and concurrent requests are not sent.