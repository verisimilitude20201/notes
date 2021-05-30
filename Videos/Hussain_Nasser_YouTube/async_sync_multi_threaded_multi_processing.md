Video: https://www.youtube.com/watch?v=0vFgKr5bjWI&list=PLQnljOFTspQUNnO4p00ua_C5mKTfldiYT&index=17

# Synchronous operation, Asynchronous operation, Multi-processing and Multi-threading

## Synchronous operation

1. When we make a request (to a database, network call) to read some data, we are blocked on the network request getting completed or the data to be fetched from the disk corresponding to our request. We are not doing anything meanwhile. This is Synchronicity.
2. Mostly single-threaded

## Multi-threading

1. Thread is a container that has same set of resources that the process has.
2. These threads can start racing to access these resources which can lead to critical bugs
3. Multi-threading is difficult to get write and lead to subtle bugs
4. Thread-safe means having stuff such as semaphores or mutexes that prevent threads from accessing the same set of shared resources at the same time.

## Asynchronous execution

1. Node.js is the perfect example of single-threaded, non-blocking execution framework
2. Asynchronicity implies not blocking on a request to get completed. Rather it triggers a request and supplies it a callback to call it back whenever the request gets completed.
3. Asynchronicity makes the code less readable.
4. JavaScript community got the concept of promises and solved the readability problem.

## Multi-processing


1. Spin up multiple process each with its own set of resources and enabling each process to communicate with sockets, IPC and message passing.
2. Multi-processing can scale up on multiple machines.
3. We can use multi-processing to brute force an MD5 to generate random strings that may match a given string's MD5