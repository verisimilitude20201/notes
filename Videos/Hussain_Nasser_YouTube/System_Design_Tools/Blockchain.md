Video: https://www.youtube.com/watch?v=k2caqvBkYv8&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=21 

# Blockchain
----------

1. Blockchain is a chain of blocks each containing data and a link from one block to the next that are very hard to break. This chain of blocks can be distributed and decentralized


## What is a Blockchain?
---------------------
1. Chain of blocks having a data element and a link to link them. We need to uniquely identify each block
2. We use SHA256 to uniquely identify each block may be first 12 characters of the hash. 
3. We add a PHash to the second block to link back to the first block which is also called as Genesis block

So overall structure


Block 1                          Block 2
Data: Hello                     Data: World
Hash: c0p912                    Hash: ab45123 
PHash: NULL                     PHash: c0p912






## Why is a Blockchain hard to break?
--------------------------------- 
1. This would be an easy to break chain

We can insert another block 3 as 

Block 3
Data: World2
Hash: ab45123
PHash: c0p912  


2. To make it harder to break we can calcuate PHash = SHA256(Previous Hash + Current Data). So any block that needs to insert itself in the middle it needs to replace the whole block set after that.

3. To make it more difficult to crack, make the PHash as

PHash = SHA256(Prev Block Hash, current data, current iteration)

You can even add a rule such that PHash should start with a zero, till then keep on iterating the iteration

4. Increase more difficulty - hash must start with 5 zeroes (Used in Bitcoin). That's why this is so compute intensive. Keep on generating the PHash until you have 5 zeroes at its start. This process is called mining.

## Adding a new block
1. Propose new data
2. Mine a block (Compute a hash/p-hash)
3. Node must agree on the block being added
4. Consensus protocol (e.g. Paxos)

And so we get a chain of blocks may be different chains of blocks since it's decentralized system. Eventually based on consensus, one of the chains win.

## Pros
----
1. Decentralized: No central entity that determines something.
2. Distributed on multiple nodes
3. Hard to break


## Cons
----
1. Compute intensive and waste of energy just to make it hard to break.
2. Can be broken 51% attack if you own 51 % of the chains of list. You can forge the network into a centralized network.