Video: https://www.youtube.com/watch?v=hmTl5Y4ee_Y&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=4(06:37)

# Building a non-blocking multi-process Web server
1. Web server should account for requests that lead to expensive processing. 
2. Single process might be blocked for serving one request. So we should have multiple processes taking client requests.


## Blocking single-process

## Non-blocking multi-process back-end
1. Child process can send information to the parent. But parent cannot send anything to the child.
2. Parent process object is accessible as a global variable.

## Pros 
1. Efficient than single process. User does'nt stay blocked.
2. Can create a pool of available processes and limit the application to these processes.
3. Multiple instances of containers can be more efficient, maintenable than multi-processing or multi-threading.

## Cons
1. Avoid multi-processing application as much as possible. It's complex to maintain, debug, we can get orphan processes.
2. Need to add checks on the parent to check the list of processes if they are alive.


Instead of this, we can create multiple instances of single-process micro-services.Containers are isolated and they can be kept behind a load-balancer which keeps track of the load on each instance and redirect the requests accordingly. An alternative to multi-processing is creating a pool of processes that are pre-baked that can be used to do this.