# Hashing and Consistent Hashing


## Hashing
1. Goal of a hashmap is to save a key-value pair and retrieve it as fast as possible by using a key
2. We need
    - Key
    - Hash Function: A function that gives an arbitrarily large value when passed in a key. 
    - Hash Table: Hash table is an array which stores the value according to the hashed key.

3. We do not directly map the output of the hash function to the index of the array. We mod the large number with the length of the array and store the value at the index equal to the remainder.

4. Hash collision is when two different keys yield the same hash. To avoid this, we can have a singly linked list maintianing a list of collided values at the same bucket


## Consistent Hashing
1. Is an algorithm for distributing requests among a set of servers in case of servers getting added/removed dynamically.