# Sharing Objects

## Introduction
1. Here, we will see techniques for sharing and publishing objects so that they can be safely accessed by multiple threads.
2. It's a common misconception that synchronization is only about atomicity or demarcating critical sections. Synchronization has another significant but subtle aspect, memory visibility.
3. We want to not only prevent a thread from modifying the state of an object when another is using it but also when a thread modifies an object's state, other threads can actually see the changes that were made. Without synchronization (either explicitely or built into the library classes) this is not possible.

## Visibility
1. Visibility is subtle in case of a multi-threaded environment. In a single threaded program, you can guarantee that a value you write to a variable without any intervening writes you can expect to get the same value back.
2. To ensure visibility of writes across threads, you must use synchronization.
3. Sharing variables without synchronization
```
public class NoVisibility {
    private static boolean ready;
    private static int number;

    private static class ReaderThread extends Thread {
        public void run() {
            while(!ready)
                Thread.yield();
            System.out.println(number)
        }
    }

    public static void main(String[] args) {
        new ReaderThread().start();
        number = 42;
        ready = true;
    }
}
```
ReaderThread and main() thread share the variables ready and number. It may appear that main() thread sets ready to true and number to 42 and then ReaderThread checks ready, finds it true and stops after printing 43. Because this program does'nt use synchronization, there's no guarantee that the value of ready and number written by the main thread will be visible to reader thread. The reader thread will keep spinning continously printing 0. Also, the write to ready may be visible to the Reader thread before the write to number. So Reader thread may just print 0. This is called reordering. 
4. In absence of synchronization, the compiler, processor and runtime can do some weird things to the order in which operations execute. Any attempt to reason about the order in which memory actions may happen insufficiently synchronized multithreaded programs will almost be certainly incorrect. 
5. Always use proper synchronization when data is used across threads.
6. Stale Data:
    - Unless sufficient synchronization is used every time, it may see a stale value for that variable. Staleness is all-or-nothing. A thread can see an up-to-date value of one variable but a stale value of another variable that was written first.
    - Depending on the use case if you take decisions based on stale data, it can cause confusing failures, corrputed data structures, inaccurate computations and infinite loops.
    - Below MutableInteger is threadsafe, because we have "synchronized" both the getter and setter
    ```
    @ThreadSafe
    public class MutableInteger {
        private int value;

        public synchronized void set(int value) {
            this.value = value;
        }

        public synchronized int get() {
            return value;
        }
    }
    ```
7. Non-atomic 64-bit operations
    - When a thread reads a variable without synchronization, it may see a stale value but at-least it sees a value placed there by some thread rather than some random value. This safety guarantee is called "out-of-thin-air" safety.
    - This applies to all variables in Java except 64-bit numeric variables declared as double or long that are not volatile.
    - JVM requires fetch and store operations to be volatile but for nonvolatile long and double, the JVM is permitted to treat a 64-bit read or write as two separate 32-bit operations. If reads/writes occur in different threads, it is possible to read a non-volatile long and get back the high 32 bits of one value and low 32 bits of another value. 
    - Thus, It's not safe to share mutable long and double variables in multithreaded programs unless they are guarded by a lock or are declared volatile.
8. Locking and Visibility
    - Locking (Intrinsic locking using synchronized) can be used to guarantee that one thread sees the effects of another thread in a predictable manner. 
    - Everything a thread A did in or prior to a synchronization block is visible to B when it executes a synchronized block guarded by the same lock (in effect, when thread B enters the block, thread A has already completed its part and released the lock.)
    - Locking is just not about mutual exclusion, its also about memory visibility. To ensure all threads see the same most up-to-date value of shared mutable variables, the read and write threads must synchronize on a common lock.
9. Volatile
    - Volatile variables ensure that the updates to a variable are propagated predictably to other threads. 
    - When a field is declared volatile, the compiler and runtime are indicated that this variable is shared and operations on it should'nt be reordered with other memory operations. 
    - They are'nt cached in registers/caches so reading from a volatile variable always returns the most recent write by any thread.
    - Accessing a volatile variable performs no locking and so cannot cause the executing thread to block. It's a lighter-weight synchronization alternative.
    - From a memory visibility perspective, writing a volatile variable is like exiting a synchronized block and reading is like entering a synchronized block. 
    - Using volatile leads to fragile and harder to understand code. 
    - Example use of volatile
    ```
    volatile boolean asleep = false;
    while(!asleep) {
        countSheep();
    }
    ```
    For this to work, the asleep variable must be volatile. Otherwise, the thread might not be able to notice if some other thread set asleep to true.
    - Volatile variables do have limitations even though they are convinient. The most common use of them is as a status flag, interruption or complettion flag.
    - Semantics of volatile are not strong enough to make a count++ operation atomically on a normal integer variable unless you can guarantee that this operation is executed from a single thread. Atomic variables can be used as better volatile variables.
    - Locking can guarantee both visibility and atomicity, volatile variables can guarantee only visibility.
    - You can use volatile variables only when following criteria are met
        - Writes to a variable do not depend on its current value and you can ensure that only a single thread ever updates the value.
        - Variable does not participate in invariants with other state variables.
        - Locking is not required for any other reason while the variable is being accessed.

## Publishing and Escape
1. Publishing an object means making it availaible to code outside of its current scope such as by storing a reference to it where other code can find it, returning it from a non-private method or passing it to another method in a class.
2. For general use, we want to publish an object, to do it in a thread-safe manner requires synchronization.
3. Publishing objects before they are fully constructed may compromise thread-safety; publishing internal state variables can compromise encapsulation and make it difficult to preserve invariants. An object that is published when it should have not is called escape.
4. The most blatant form of escape is using a public static field to hold an object reference where any class or thread can see it.
```
public static Set<Secret> knownSecrets;

public void initialize() {
    knownSecrets = new HashSet<Secret>
}
```
5. Returning a reference from a non-private method too publishes the returned object as the below class shows
```
class UnsafeStates {
    private String states[] = {"AL", "AK"};

    public String[] getStates() {
        return states;
    }
}
```
Publishing states in this way is problematic, Any caller can modify its contents. The "states" array has escaped its intended scope. What was supposed to be private has become public.
6. Any object that is reachable from a published object by following a chain of nonprivate field references and method calls has also been published.
7. An alien method is the one whose behavior has not been fully specified by C. This includes methods in other classes as well as overrideable methods (neither private not final). Passing an object to the method must also be considered publishing that object since the alien method may publish that object or return a reference to it that may be used in some other thread.
8. Once an object escapes, another class may maliciously or carelessly misuse it. This is why encapsulation should be used, it makes it practical to analyze programs for correctness and harder to violate design constraints.
9. The below is another mechanism to publish an internal state of its object using an inner class. Inner class instances contain a reference to the enclosing instances and so the below code publishes the enclosing ThisEscape instance as well.
```
public class ThisEscape {
    public ThisEscape(EventSource source) {
        source.registerListener (
            new EventListener() {
                public void do() {
                    sout("print");
                }
            }
        );
    }
}
```
10. Safe construction practises:
    - An object is in a predictable state only when its constructor has executed. Publishing an object from within its constructor can publish an incompletely constructed object. 
    - The this reference should not escape from the thread until after the constructor returns. It can be stored somewhere so long as it is not used by another thread until after construction. Do not allow the this reference to escape during construction.
    - If an object creates a thread from its constructor, it almost shares its this reference with the new thread explicitely passing it to the constructor and implicitely because the new Thread or Runnable is an inner class of the owning object. We should'nt start this thread immediately. Instead have a start() method for doing so.
    - You should use a private constructor and public factory method to register an event listener or start a thread from a constructor
    ```
    public class SafeListener {
        private final EventListener listener;

        private SafeListener() {
            listener = new EventListener() {
                public void onEvent(Event e) {
                    sout("print"  + e);
                }
            }
        }

        public static SafeListener newInstance(EventSource source) {
            SafeListener safe = new SafeListener();
            source.register(safe.listener);
            return safe;
        }
    }
    ```
## Thread confinement
1. To access shared, mutable data requires synchronization, the only way to avoid this requirement is not to share. If data is accessed from a single thread, no synchronization is needed, this is called thread confinement. Even if the object itself is not thread-safe, when confined to a single thread it becomes thread safe.
2. One common use of thread confinement is Swing which confines visual and model components which themselves are'nt thread-safe to the event dispatch thread.
3. Another common use is pooled JDBC connection objects which are not required to be thread safe by the JDBC spec (on the contrary, connection pool implementations provided by application servers are thread safe). A thread acquires a connection object from the pool and returns it after processing a single request. Requests are processed per thread and pool does not dispense the same connection to another thread so this confines the Connection to that thread.
4. Thread confinement is an element of the program design. We have local variables and ThreadLocal class to help in maintaining thread confinement, but its the programmer's responsibility to ensure that thread confined objects do not escape from their intended thread.
5. Adhoc thread confinement:
    - Describes when the responsibility for maintaining thread confinement falls on the implementation
    - Fragile because none of the language modifiers such as visibility modifiers or local variable helps confine objects to the target thread.
    - Single threaded subsystems provide the benefit that outweights the fragility of adhoc thread confinement. Deadlock avoidance is one more benefit of single threaded subsystems.
    - Volatile variables are a special case of thread confinement. Read-modify-write operations are safe on a volatile variable as long as the variable is only written from a single thread. Visibility guarantees for volatile threads guarantee that other threads see the most up-to-date value.
    - Adhoc thread confinement should be used sparingly because of its fragility. Use stronger forms of thread confinement (stack or ThreadLocal confinement)
6. Stack confinement:
    - An object can be reached only through local variables.
    - Local variables are intrinsically confined to the executing thread; they exist on executable thread's stack which is not accessible to other threads.
    - In the below program, there is exactly one local reference to the TreeSet animals and so it does'nt escape the function 
    ```
    public int loadTheArk(Collection<Animal> candidates) {
        SortedSet<Animal> animals;
        Animal candidate = null;
        animals = new TreeSet<Animal>(new SpeciesGenderComparator())
        animals.addAll(candidates);
        int numPairs;
        for(Animal a: animals) {
            if (candidate == null || !candidate.isPotentialMate(a)) {
                candidate = a;
            } else {
                ark.load(new AnimalPair(candidate, a))
                ++numPairs;
                candidate = null;
            }
        }

        return numPairs
    }
    ``` 
    - If this assumption of confinement-within-thread is not documented clearly, future maintainers may modify this code to allow the TreeSet animals to escape.
7. ThreadLocal: 
    - Allows per-thread value within a value-holding object. 
    - It provides get() and set() accessors that maintain a separate copy of the value for each thread that uses it.
    - get() returns the most recent value passed to set() from the currently executing thread.
    - Often used to prevent sharing of mutable singletons or global variables.
    - We can use a ThreadLocal to store a JDBC connections which may not be thread-safe, so that each thread can have it's own connection
    ```
    private static ThreadLocal<Connection> connection = new ThreadLocal<Connection>() {
        public Connection initialValue() {
            return DriverManager.getConnection(DB_URL);
        }
    };

    public static Connection getConnection() {
        return connectionHolder.get();
    }
    ```
    - Can also be used when a freqently used operation requires a temporary object on each invocation and wants to avoid reallocating new memory on each invocation.
    - A thread calling ThreadLocal.get for the first time, initialValue is consulted to provide initial value for that thread. You can think of it as ThreadLocal<T> holding a Map<Thread, T> that stores thread specific value. The thread-specific values are stored in the thread, when the thread terminates, the values are garbage collected.
    - J2EE frameworks often store the execution context within a ThreadLocal object so that the running thread can query the ThreadLocal object to fetch it's execution context.
    - ThreadLocal objects can detract from usability and introduce couplings between classes and so should be used with care.

## Immutability
1. The other end around the need to synchronize is to have immutable objects. 
2. All atomicity and visibility hazards like stale values, losing updates, inconsistent state have to do with vagaries of multiple threads trying to access the same mutable state at the same time.
3. Immutable objects' state cannot be changed once constructed, so they are inherently thread safe.
4. They are simple since their state can be reasoned about. It never changes.
5. They are safer because their states can't be subverted by malicious or buggy states.
6. An object is considered immutable if
    - Its state can't be modified after construction
    - All its fields are declared final
    - It's properly constructed. The this reference does not escape during construction.
7. We can still have an immutable object storing it's underlying state in a mutable object. stooges is a mutable object whose visibility is private to the class and whose object does'nt escape. Outside code can't modify its contents nor change the reference.
```
@Immutable
public final class ThreeStoges {
    private final Set<String> stooges = new HashSet<String>();

    public ThreeStooges() {
        stooges.add("ABC");
        stooges.add("DEF");
        stooges.add("XYZ");
    }

    public boolean isStooge(String name) {
        return stooges.contains(name);
    }
}
```
8. There is a difference between object state being mutable and the reference being mutable. We can still replace the immutable object reference (not marked final) with a new immutable object.
9. Final fields:
    - Final fields can't be modified (though the objects they refer to can be modified if they are mutable) but they also have special semantics under Java memory model.
    - Final fields let immutable objects be freely accessed and shared without synchronization. This gives the guarantee of initialization safety.
    - Just as it is a good practise to make fields private unless they need greater visibility, its a good practise to make fields final unless they need to be mutable.
10. Using Volatile to publish immutable objects
    - Using immutable objects can sometimes provide a weak form of atomicity.
    - Immutable holder for caching a number and it's factors
    ```
    public class OneValueCache {
        private final BigInteger lastNumber;
        private final BigInteger[] lastFactors;

        public OneValueCache(BigInteger i, BigInteger[] factors) {
            lastNumber = i;
            lastFactors = Arrays.copyOf(factors, factors.length);
        }

        public BigInteger[] getFactors(BigInteger i) {
            if(lastNumber == null || !lastNumber.equals(i))
                return null;
            return Arrays.copyOf(lastFactors, factors.length);
        }
    }
    ```
    - Whenever a group of related data items must be acted upon atomically, consider an immutable class holder from them as the above example shows. 
    - It eliminates race conditions in accessing or updating multiple related variables. 
    - If variables are to be updated, a new holder object is created but any threads working with previous holder still sees it in consistent state.

## Safe Publication
1. Publishing an object without adequate synchronization
```
public Holder holder;

public void initialize() {
    holder = new Holder(42);
}
``` 
2. Because of visibility problems, Holder could appear to another thread of being in an inconsistent state even though its invariants were successfully established. This allows another thread to observe a partially constructed object.
3. Improper publication: When good objects go bad:
    - An observing thread could see a partially constructed object and later see its state suddenly change even though it has'nt been modified since publicatiion. 
    - Class at Risk of failure if not properly published
    ```
    public class Holder {
        private int n;

        public Holder(int n) {
            this.n = n;
        }

        public void assertSanity() {
            if(n != n) {
                throw new AssertionError("This statement is false");
            }
        }
    }
    ```
    A thread other than the publishing thread could throw an AssertionError. 
    - Two things can go wrong with improperly published objects. Other threads could see a stale value and see a NULL reference or some other value even though a value has been placed in holder. Far worse, other threads may see an up-to-date value for the holder reference but stale values for the state of the holder.
4. Immutable Objects & Initialization Safety
    - Java Memory model offers a special guarantee of initialization safety for sharing immutable objects. 
    - Immutable objects can be safely accessed when synchronization is not used to publish the object reference. For this guarantee to hold, all requirements for immutability must be met: unmodifiable state; all fields are final; proper construction.
    - Immutable objects can be used by any thread without additional synchronization even when synchronization is not used to publish them.
    - This guarantee extends to final fields of the immutable object. If those fields refer to immutable objects, still synchronization is necessary to access the state of the objects they refer to. 
5. Safe publication idioms:
    - Objects that are non-immutable must be safely published. That is using synchronization in the publishing and consuming thread. Let's just focus on ensuring that the consuming thread can see the object in its as published state. 
    - To publish an object safely, both reference to the object and object's state must be visible to other threads at the same time. 
    - A properly constructed object can safely be published by:
        - Initializing an object reference from a static identifier
        - Storing a reference to it into a volatile field or AtomicReference
        - Storing a reference to it into a final field of a properly constructed object
        - Storing a reference to it into a field that is properly guarded by a lock.
    - Internal synchronization in thread-safe collections means that placing an object in a thread-safe collection such as a Vector or synchronizedList fulfils the last of these requirements. 
    - If thread A places the object X in a thread-safe collection and B retrieves it, B is guaranteed to retrieve the state of X as A left it even though the application code that hands off X in this manner has no explicit synchronization.
    - Using a static initializer is often the safest and easiest way to publish objects. Static initializers are executed by the JVM at class initialization time. 
6. Effectively immutable objects:
    - Safe publication mechanisms all guarantee that as-published state of an object is visible to all accessing threads as soon as the reference to it is visible. If that state is not going to be change again, this is sufficient to ensure that the access is safe.
    - Objects that are not technically immutable but whose state will not be modified after publication are called effectively immutable.
    - Using effectively immutable objects can simplify development and improve performance by reducing the need for synchronization.
    - Safely published efffectively immutable objects can be used safely  by any thread without any additional synchronization.
    - For example: Date is mutable. If it's guaranteed to not be modified after creation, it can be published safely. 
    ```
    public Map<String, Date> lastLogin synchronizedMap = Collections.synchronizedMap(new HashMap<String, Date>());
    ```
    If Date values are not modified after placing in Map, then the synchronization is sufficient to publish the Date values safely. 
7. Mutable Objects:
    - Synchronization must be used not only while publishing the mutable object but also every time the object is accessed to ensure visibility of subsequent modifications. 
    - To share mutable objects safely, they must be safely published and be either thread-safe or guarded by a lock. 
    - Publication requirements of an object depend on mutability:
        - Immutable objects can be published via any mechanism
        - Effectively immutable objects must be safely published
        - Mutable objects must be safely published and be guarded by a lock or be thread-safe.
8. Sharing objects safely:
    - Many concurrency errors stem from failing to understand the rules of engagement when you have access to an object reference. Rules of engagement define what you're allowed to do with an object once you access its reference. Should a lock be acquired before use? Can we modify it's state or only read it? 
    - Below are some most useful policies for using and sharing objects in a concurrent program
        - Thread-confined: A thread-confined object is just confined and owned and modified by a single thread i.e. its owning thread. 
        - Shared readonly: Includes immutable and effectively immutable objects. Can be accessed concurrently by multiple threads without additional synchronization. Cannot be modified by any thread.
        - Shared thread-safe: Performs synchronization internally so that multiple threads can freely access it using it's public interface without further synchronization
        - Guarded: Guarded objects can be accessed only with a specific lock field. Encapsulated in other thread-safe and published objects that are known to be guarded by a specific lock.
