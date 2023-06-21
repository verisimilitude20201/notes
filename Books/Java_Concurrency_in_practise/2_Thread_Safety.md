# Thread Safety

## Introduction

1. Building correct concurrent programs requires correct use of threads and locks
2. Writing thread-safe code is at its core managing access to shared, mutable state. 
3. An object's state consists of its instance variable, static variables, fields from other dependent objects. Any data that can affect its externally visible behavior 
4. Shared means an object that can be accessed by multiple threads, mutable meaning an object's value could be changed during it's lifetime. We are trying to protect the data from uncontrolled concurrent access.
5. Whether an object needs to be thread safe, depends on whether it would be accessed from multiple threads. How it is used in the program.
6. Whenever more than one thread accesses a state variable and one of them may write to it, they all must coordinate their access to it using synchronization.
7. The primary mechanism for synchronization is the "synchronized" keyword in Java. It also includes the use of atomic variables, volatile variables and explicit locks.
8. If multiple threads access the same mutable variable without synchronization, your program is broken. It can be fixed in below ways
    - Don't share the mutable variable across threads
    - Make the state variable immutable
    - Use synchronization while accessing the shared variable.
9. It's easier to design a class to be thread-safe than to retrofit it for thread safety later.
10. When desigining thread-safe classes, good object oriented techniques, encapsulation, immutability and clear specification of invariants are your best friends.
11. Sometimes good design is at odds with the requirements and at times the rules of good design are often compromised for the sake of performance or with legacy code. It's always a good practise to make the code right and then make it fast. Then also, optimization should be done only if your performance measurements tell you so and it makes a difference under realistic conditions.
12. Thread-safety is a term applied to code, but it is about state and it can only apply to the entire body of code that encapsulates it's state which may be an object or an entire program.

## What is Thread Safety ? 
1. Let's first define correctness. Correctness means a class conforming to its specifications. A good spec defines invariants consisting of an object's state and postconditions describing the effects of its operations. Single-threaded program correctness is something that can be recognized, we know when the program is working correctly.
2. A class is thread-safe if it behaves correctly when accessed from multiple threads regardless of the scheduling or interleaving of those threads by the run-time environment and with no additional coordination or synchronization on part of the calling code.
3. No sequence of operations performed sequentially or concurrently on instances of a thread-safe class can cause an instance to go in an invalid state. 
4. Thread-safe class encapsulate any synchronization needed so that clients need not provide their own.

### Thread-safe Servlet example
1. Very often thread-safety requirements stem not from using threads directly but from a decision to use a facility like the Servlets framework.
2. Let's write a simple Servlet-based factorization service and slowly extend it to add new features while preserving thread-safety.
```
@ThreadSafe
public class StatelessServletFactorizer implements Servlet {
    public void service(Servlet req, Servlet resp) {
        BigInteger i = extractFromReq(req)
        BigInteger factors[] = factor(i)
        encodeIntoResponse(resp, factors);
    }
} 
``` 
One thread accessing a StatelessServletFactorizer cannot influence the results of another thread accessing the same StatelessServletFactorizer because the two threads do not share state.
2. Since actions of a thread accessing a stateless object cannot influence the correctness of operations in other threads, stateless objects are always thread safe. 
3. It's only when Servlets need to remember things from one request to another that thread-safety becomes a requirement.

### Atomicity
1. Support we want to add a hit counter to measure the number of requests processed.
```
@NotThreadSafe
public class UnsafeCountingFactorizer implements Servlet {
     private int count = 0;

     public int getCount() {
        return count;
     }

     public void service(Servlet req, Servlet resp) {
        BigInteger i = extractFromReq(req)
        BigInteger factors[] = factor(i)
        count++;
        encodeIntoResponse(resp, factors);
    }
}
```
2. This is susceptible to lost updates. The increment action is not atomic, it's a shorthand sequence for 3 distinct operations
    - Fetch the current value (read)
    - Increment it (modify)
    - Write it back (write)
  This is an example of read-modify-write cycle in which the resulting state is derived from previous state.
3. Having a slightly inaccurate count of requests might be acceptable, but if the same counter is used to generate unique object identifiers, returning the same value from multiple invocations could cause serious data integrity problems.
4. The possibility of incorrect results in the presence of unlucky timing is called a race condition.
5. UnsafeCountingFactorizer has also the problem of stale data.

### Race conditions
1. A race condition occurs when a correctness of a computation depends on the relative timing or interleaving of multiple threas by the runtime, in other words getting the right answer relies on lucky timing. 
2. The most common type is check-then-act where a potentially stale observation is used to make a decision on what to do next.
3. An example of a race condition - Lazy initialization
    - A common goal of lazy initialization (an application of check-then-act) is to defer initializing an object till it's actually needed.
    - LazyInitRace illustrates this
    ```
    @NotThreadSafe
    public class LazyInitRace {
        private ExpensiveObject instance = null;
        public ExpensiveObject getInstance() {
            if(instance == null)
                instance = new ExpensiveObject();
            return instance;
        }
    }
    ```
    - This suffers from a race condition and two threads A and B may receive different copies of the ExpensiveObject instance due to an unlucky timing causing interleaving of their execution.
4. Race conditions don't always result in a failure some unlucky timing is required. 
5. If LazyInitRace is used to initialize an application-wide registry, having it return different instances from multiple invocations could cause having an inconsistent view of the set of registered objects. 
6. If UnsafeCountingFactorizer is used to generate identifiers for entity objects in an ORM framework, two distinct objects could end up with the same ID.

### Compound Action
1. If we have a sequence of check-then-act or read-modify-write operations, they need to be atomic or undivisible. No thread should be able to use a variable on which such compound actions are being carried on. That thread should observe or modify only before we start the compount action or after that compound actions complete.
2. Operations A and B are atomic with respect to each other if from the perspective of a thread executing A, either of all B has executed or none of it has. An atomic operation is atomic with respect to all operations including itself that operate on the same state.
3. We collectively refer to check-then-act and read-modify-write sequences as compound actions: sequences of actions that need to be executed atomically to remain thread-safe.
4. Making it thread safe using Atomic Integer
    - 
    ```
    @NotThreadSafe
    public class UnsafeCountingFactorizer implements Servlet {
        private final AtomicInteger count = 0;

        public int getCount() {
            return count.get();
        }

        public void service(Servlet req, Servlet resp) {
            BigInteger i = extractFromReq(req)
            BigInteger factors[] = factor(i)
            count.incrementAndGet();
            encodeIntoResponse(resp, factors);
        }
    }
    ```
    - The java.util.concurrent.atomic package contains atomic classes effecting atomic transitions on numbers and object references.
    - Thus we could add the element of thread safety by adding a thread-safe class AtomicLong to manage the counter state.

## Locking
1. Imagine that we want to improve our Servlet performance by caching recently computed factorization results. To implement this, we need the number and its factors
2. We use AtomicReference which is a thread-safe holder class for an object reference similar to AtomicLong which is a thread-safe holder class for a long. 
```
@NotThreadSafe
    public class UnsafeCachingFactorizer implements Servlet {
        private final AtomicReference<BigInteger> lastNumber = new AtomicReference<BigInteger>();
        private final AtomicReference<BigInteger[]> lastFactors = new AtomicReference<BigInteger[]>();

        public void service(Servlet req, Servlet resp) {
            BigInteger i = extractFromReq(req)
            if(i.equals(lastNumber.get())) {
                encodeIntoResponse(resp, lastFactors.get());
            } else {
                BigInteger factors[] = factor(i)
                lastNumber.set(i)
                lastFactors(factors)
                encodeIntoResponse(resp, factors);
            }
            
        }
    }
```
Even though AtomicReference are thread-safe, UnsafeCachingFactorizer has race conditions. 
3. According to definition of thread-safety, all invariants must be preserved irrespective of interleaving or scheduling of operations. One invariant of UnsafeCachingFactorizer is product of the factors cached in lastFactors must equal the value stored in lastNumber. When updating lastNumber, you must update lastFactors in the same atomic operation. With some unlucky timing, since this is not the case, there is still a window of vulnerability when lastNumber has been modified and lastFactors is not and during this time, other threads may see that invariant does not hold
4. To preserve state consistency, update related variables in same atomic operation.

### Java Intrinsic locks
1. We have built-in locking mechanism with "synchronized" block in Java. It has two parts: a reference to an oject that will serve as the lock and the block of code to be guarded by the lock. 
2. Synchronized method is a shorthand for a synchronized block which spanning an entire method body whose lock is the object on which the method is being invoked.
```
synchronised(object) {
    // Block of code having shared state guarded by lock
}
```
3. This lock is called an intrinsic lock or monitor lock. Every Java object can implicitely act as a lock for the purpose of synchronization. When the control flow enters the synchronized block, the lock is acquired by the thread and automatically released when the thread exits that block
4. Intrinsic locks act as mutual exclusion locks or mutexes. Only one thread may own the lock. Therefore, synchronized blocks guarded by the same lock execute atomically with respect to each other. No thread executing a synchronized block can observe another thread to be in the middle of the synchronized block guarded by a same lock.
5. We can fix the above UnsafeCachingFactorizer by making the method synchronized. However, this approach though it makes the class thread-safe affects performance since it inhibits multiple clients from using the factorizing servlet simultaenously.
```
@ThreadSafe
public class SafeCachingFactorizer implements Servlet {
    private final AtomicReference<BigInteger> lastNumber = new AtomicReference<BigInteger>();
    private final AtomicReference<BigInteger[]> lastFactors = new AtomicReference<BigInteger[]>();

    public synchronized void service(Servlet req, Servlet resp) {
        BigInteger i = extractFromReq(req)
        if(i.equals(lastNumber.get())) {
            encodeIntoResponse(resp, lastFactors.get());
        } else {
            BigInteger factors[] = factor(i)
            lastNumber.set(i)
            lastFactors(factors)
            encodeIntoResponse(resp, factors);
        }
        
    }
}
```
### Re-entrancy
1. If a thread tries to acquire a lock it already holds, the request succeeds. Intrinsic locks are reentrant. 
2. Reentrancy implies locks are acquired on a per-thread basis rather than per-invocation. Each intrinsic lock maintains an acquisition count and owning thread. When this count reaches 0, the lock is released.
3. Reentrancy facilitates the encapsulation of locking behavior and simplies development of object oriented code.
4. Had reentrancy not been supported, the below code would result in a deadlock at the  super.doSomething() since doSomething() methods in both parent class Widget and child class LoggingWidget are synchronized
```
public class Widget {
    public synchronized void doSomething() {

    }
}

class LoggingWidget extends Widget {
    public synchronized void doSomething() {
        super.doSomething()
    }
}
```

### Guarding State with Locks
1. We can use locks to construct protocols for guaranteeing exclusive access to shared state.
2. Holding a lock for an entire duration of a compound action such as read-modify-write and check-then-act would make it atomic.
3. If synchronization is used to coordinate shared access to a variable, it is needed everywhere that variable is accessed and not just while writing to that variable.
4. For each mutable state variable accessed by more than one thread, all accesses to that variable must be wih the same lock held.
5. Acquiring a lock associated with an object does not prevent other threads from access that object, the only thing it prevents any other thread from acquiring the same lock.
6. Every Java object having a built in lock is just a conveinience so that you don't create any new lock object.
7. Every shared, mutable variable should be guarded by exactly one lock.
8. We can encapsulate all mutable state within an object and protect it from concurrent access by synchronization using that object's intrinsic lock.
9. Only mutable data that will be accessed from multiple threads needs to be guarded by locks
10. For every invariant that involves more than one variable, all variables involved in that invariant should be guarded by the same lock.
11. Indiscriminate usage of synchronized might be either too much or too little synchronization. Merely synchronizing every method as Java's Vector class is not enough to render compound actions on a Vector atomic
```
if(!vector.contains(element)) {
    vector.add(element)
}
```
This code attempts a put-if-absent. Even though both contains() and add() are atomic, this too has a race condition if you consider the compond action. Additional locking is needed. At the same time, synchronizing every method leads to liveness or performance problems.

### Liveness and Performance
1. UnsafeCachingFactorizer used caching to improve performance. The way synchronization was used in UnsafeCachingFactorizer makes it perform badly. 
2. The synchronization policy of UnsafeCachingFactorizer guards the shared state variables with the Servlet object's intrinsic lock by synchronizing the whole of service() method. Because service() is synchronized, only one thread can use it at one time.
3. If the servlet is busy factoring a large number, other clients have to wait until the current request is completed before the system can start the new number. This leads to wastage of CPU time, processors remain idle.
4. It's reasonble to try exclude from synchronized blocks long running operations that do not affect shared state so that other threads are not prevented from accessing shared state. 
```
@ThreadSafe
public class SafeCachingFactorizer implements Servlet {
    private BigInteger lastNumber;
    private BigInteger[] lastFactors = new AtomicReference<BigInteger[]>();
    private long hits;
    private long cacheHits;

    public synchronized long getHits() {
        return hits
    }

    public synchronized long getCacheHits() {
        return cacheHits;
    }

    public synchronized void service(Servlet req, Servlet resp) {
        BigInteger i = extractFromReq(req)
        BigInteger[] factors;
        synchronized(this) {
            ++hits;
            if(i.equals(lastNumber.get())) {
                factors = lastFactors.clone();
                ++cacheHits;
            } 
        }
        if(factors == null) {
            factors = factor(i)
            synchronized(this) {
                lastNumber = i;
                lastFactors = factors.clone()
            }
        }
        encodeIntoResponse(resp, factors);
    }
}
```
SafeCachingFactorizer uses two separate synchronization blocks limited to a short section of code. One guards the check-then-act sequence testing if we can just return the cached result. The second guard updates both cached number and cached factors. The counter variables are also incremented in the same first synchronized block. We could have used AtomicLong for the counters but since we are already using synchronized blocks, using two different synchronization mechanisms would be confusing. 
5. The restructuring of CachedFactorizer provides a balance between simplicity and concurrency (synchronizing the shortest possible code path). There is frequently tension between simplicity and performance. When implementing a synchronization policy, resist the temptation to prematurely sacrifice simplicity (compromising safety) for the same of performance.
6. Avoid holding locks for lengthy computations or operations at risk of not completing quickly such as network or console I/O.