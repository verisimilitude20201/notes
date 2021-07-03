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

## Example of a monolithic Amazon order occuring on a single database.
1. We have 3 components here - orchestrator, customer wallet, order having their own database
2. Customer places an order
  - Orchestrator contacts customer wallet service to check the account balance. 
  - If the customer has sufficient balance, then the orchestrator calls the order service to create a new order. 
  - On success, it then returns success to the customer.
4. Here in this case, each microservice has it's own database so single transaction across database may not be possible.