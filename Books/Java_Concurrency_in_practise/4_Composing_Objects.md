# Composing Objects
Let's examine patterns for designing thread-safe classes and strategies for not undermining their safety guarantees.

## Designing a thread-safe class
1. Encapsulation makes it possible to determine that a class is thread-safe without having to examine the entire program.
2. The design process for a thread-safe class includes:
    - Identify the variables that forms the object's state
    - Identify the invariants that constrain the state variables
    - Establish a policy for managing concurrent access to the object's state.
3. An object's state starts with all it's fields. If the fields are themselves references to some other objects, its state will encompass fields from the referenced objects as well.
4. Synchronization policy defines how an object coordinates access to its state without violating its invariants or postconditions. It specifies what combination of immutability, thread confinement, locking is used to maintain thread safety, which variables are guarded by which locks.
5. Simple thread-safe counter using Java Monitor
```
@ThreadSafe
public class Counter {
    private long value;

    private synchronized getValue() {
        return value;
    }

    private synchronized long increment() {
        if(value == Long.MAX_VALUE) {
            throw new IllegalStateException("Counter overflow");
        }
        return ++value;
    }
}
```
6. Gathering synchronization requirements
    - Making a class thread-safe means ensuring that it's invariants hold under concurrent access. 
    - Objects/variables have a state space: the range of possible values that they can take on. The smaller this is is, the simpler it is to reason about. 
    - Invariants identify certain states as valid or invalid. For a long Counter value, negative values are an invalid state. Post-conditions identify certain state transitions as invalid. If the current Counter is 17, the next valid value is 18.
    - When the next state of a computation is derived from a current state, it's a compound action.
    - If certain states are invalid, the underlying state variables must be encapsulated otherwise, the client code could put the object into an invalid state. If an operation is a compound one and has invalid state transitions, it must be made atomic. 
    - If not, then we may relax the encapsulation requirements / serialization requirements to obtain greater flexibility or performance.
    - A class may also have multi-variable invariants for example: A NumberRange class maintaining state variables for upper and lower bounds of the range. Multi-variable invariants such as these create atomicity requirements, related variables must be fetched or updated in a single atomic operation. Lock guardin multi-variable invariants must be held for the duration of any operation that accesses related variables.
    - To conclude, you cannot ensure thread safety without understanding an object's invariants and postconditions. Constraints on the valid values or state transitions for state variables create atomicity and encapsulation requirements.
7. State-dependent operations
    - Certain objects also have methods with state-based preconditions. For example: You cannot remove an item from a non-empty queue. 
    - In a concurrent program, a precondition may become true later due to the action of another thread (In their single-threaded counterparts, if a precondition does not hold, an operation fails).
    - To create operations that wait for a precondition to become true before proceeding, its easier to use existing library classes such as BlockingQueue, Semaphores instead of wait() & notify() methods that are difficult to use correctly.
8. State ownership:
    - When defining which variables are a part of an object's state, we want to consider only data that object owns. 
    - Ownership is an element of class design and not embodied in the language. For example: the logical state of a HashMap includes the state of all its Map.Entry and internal objects. 
    - In many cases, ownership and encapsulation go together. An object encapsulates the state it owns. It's the owner of the state variable that gets to decide the locking protocol to maintain the state integrity.
    - Once you publish a reference to a mutable object, you no longer have exclusive control, you have shared ownership.
    - Collection classes exhibit a form of split ownership. The collection owns the state of the collection infrastructure but client code owns the objects stored. For example: ServletContext provides a Map-like container object service in which it can store objects by name using getAttribute() & setAttribute(). Servlets need not use synchronization while accessing this service (Servlets are inherently multi-threaded) but synchronization should be used while accessing objects stored in this container.

## Instance Confinement
1. If an object is not thread-safe, we can either use a lock while accessing it or ensure that it gets accessed only from a single thread to ensure safety.
2. Encapsulating data within an object confines access to the data to the object's methods making it easy to ensure data is always accessed with the appropriate lock held.
3. Confined objects must not escape their scope. Objects don't escape on their own, they need assistance from the developer who publishes them beyond their intended scope.
4. How instance confinement makes mySet thread-safe
```
@ThreadSafe
public class PersonSet {
    private final Set<Person> mySet = new HashSet<Person>();

    public synchronized void addPerson(Person p) {
        mySet.add(p);
    }

    public synchronized boolean containsPerson(Person p) {
        return mySet.contains(p);
    }
}
```
5. Instance confinement is one of the easiest ways of building thread-safe classes, it allows flexibility in the choice of locking strategy. PersonSet uses it's own intrisic lock to guard it's state, but any consistently used lock will be suffice.
6. Many examples in platform class libraries exist for turning non-thread safe classes thread-safe. The basic collection classes ArrayList, HashMap are not thread-safe but they provide wrapper factory methods (Collections.synchronizedList) that use a Decorator pattern wrapping the collection with a synchronized wrapper. The wrapper implements each method of the appropriate interface as a synchronized method, forwarding calls to the underlying object. The underlying collection is confined to the wrapper.
7. Confinement makes it easy to build thread-safe classes because a class that confines it's state can be analyzed for thread safety without having to examine the whole program.
8. The Java monitor pattern:
    - An object follows the Java monitor pattern which encapsulates all its mutable state and guards it with its own intrinsic lock. 
    - It's used by many library classes such as Vector and HashTable. 
    - Sometimes a more sophisticated synchronization policy is needed, more fine-grained to improve on scalability.
    - Monitor is merely a convention, any lock object can be used so long as it's used consistently
    - Guarding  state with a private lock
    ```
    public class PrivateLock {
        private final Object myLock = new Object();
        private Widget widget;
        void someMethod() {
            synchronized(mylock) {
                // Modify widget
            }
        }
    }
    ```
    - Making the lock private encapsulates the lock so client code cannot acquire it. Publicly accessible lock allows client code to participate in its synchronization policy correctly/incorrectly.
    - Whether a publicly accessible lock is used consistency requires analysis of the entire program.
9. Example - Tracking Fleet vehicles
    - Vehicle tracker dispatches fleet vehicles such as taxi cabs, police cars or delivery trucks
    - VehicleTracker class encapsulate the identity and locations of known vehicles. Each vehicle's location is represented by coordinates x, y
    - There will be two threads: View thread and Updater thread. View thread fetches names and locations of vehicles and renders them on the UI. Updater thread would modify vehicle locations with data received from GPS interface or data entered manually by a dispatcher through a GUI interface. Thus, the data model should be thread safe.
    - Monitor-based Vehicle Tracker implementation
    ```
    @NotThreadSafe
    public class MutablePoint {
        public int x, y;
        public MutablePoint() {
            x = 0;
            y = 0;
        }
        public MutablePoint(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    @ThreadSafe
    public class MonitorVehicleTracker {
        private final Map<String, MutablePoint> locations;

        public MonitorVehicleTracker(Map<String, MutablePoint> locations) {
            this.locations = locations;
        }

        public synchronized Map<String, MutablePoint> getLocations() {
            return deepCopy(locations);
        }

        public synchronized MutablePoint getLocation(String id) {
            MutablePoint loc = locations.get(id);
            return loc == null ? null : new MutablePoint(id);
        }

        public synchronized setLocation(String id, int x, int y) {
            MutablePoint loc = locations.get(id);
            if(Objects.isNull(loc)) {
                throw new IllegalArgumentException("Invalid location passed");
            }
            loc.x = x
            loc.y = y
        }

        public static final deepCopy(Map<String, MutablePoint> m) {
            Map<String, MutablePoint> result = new Map<String, MutablePoint>();
            for(String id : m.keySet()) {
                result.put(id, new MutablePoint(m.get(id)))
            }

            return Collections.unmodifiableMap(result);
        }
    }
    ```
    - Neither the map nor any of the mutable points it contains is ever published. The appropriate values are copied using MutablePoint copy constructor or deepCopy which creates a new Map
    - A performance issue would result if the set of vehicles is very large.
    - The contents of the returned collection do not change even if the underlying locations change. This could be a benefit if there are internal consistency requirements on the location set in which case returning a consistent snapshot is critical OR a drawback if callers require an up-to-date information for each vehicle

## Delegating Thread safety
1. Java monitor pattern is useful when building classes from scratch or composing out of objects that are not thread-safe.
2. Do we need to add thread-safety also whenever the objects are already thread-safe? It depends. In case of CountingFactorizer, we can say that it delegates its thread-safety responsibilities to the AtomicLong.
3. Vehicle Tracker using Delegation
    - As an example of delegation, let's use a version of Vehicle tracker delegating to a thread-safe class. We start with a thread-safe Map implementation - ConcurrentHashMap
    - Immutable Point class: Immutable values are freely shared and published, we no longer need to copy the locations before returning them.
    ```
    @Immutable
    public class Point {
        public int x, y;
        public Point() {
            x = 0;
            y = 0;
        }
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    ```
    - DelegatingVehicleTracker does'nt use any explicit synchronization and all access to state is managed by ConcurrentHashMap. All keys and values of the Map are immutable.
    ```
    @ThreadSafe
    public class DelegationVehicleTracker {
        private final ConcurrentMap<String, Point> locations;
        private final Map<String, Point> unmodifiableMap;

        public DelegationVehicleTracker(Map<String, Point> locations) {
            locations = new ConcurrentHashMap<String, Point>();
            unmodifiableMap = Collections.unmodifiableMap(locations);
        }

        public Map<String, MutablePoint> getLocations() {
            return unmodifiableMap;
        }

        public Point getLocation(String id) {
            return locations.get(id);
        }

        public void setLocation(String id, int x, int y) {
            if(locations.replace(id, new Point(x, y)) == null) {
                throw new IllegalArgumentException("Invalid exception name");
            }
        }
    }
    ```
    - Publishing original MutablePoint would break encapsulation by letting getLocations() publish a reference to a mutable state that's not thread-safe.
    - Delegating view returns an unmodifiable but live view of the vehicle locations differin it with earlier vehicle tracker. This can be a benefit: more up-to-date data or liability (potential inconsistent view of the fleet)
    - We can also return a static copy of location set instead of a live one
    ```
    public Map<String, MutablePoint> getLocations() {
        return Collections.unmodifiableMap(new HashMap<String, Point>(locations));
    }
    ```
4. Independent State Variables
    - Uptil now, all examples delegate to a single thread-safe variable. We can also delegate thread-safety to more than one underlying state variables provided they are independent.
    - Underlying class should'nt impose any constraints involving those variables. 
    - Delegating thread safety to multiple underlying state variables: In the below code, there is no relationship between the set of key and mouse listeners and therefore VisualComponent can delegate it's thread safety obligations to two lists.
    ```
    public class VisualComponent {
        private final List<KeyListener> keyListeners = new CopyOnWriteArrayList<>();
        private final List<MouseListener> mouseListeners = new CopyOnWriteArrayList<>();
        public void addKeyListener(KeyListener keylistener) {
            keyListeners.add(keylistener)
        }
        public void addMouseListener(MouseListener mouselistener) {
            keyListeners.add(mouselistener)
        }
    }
    ```
    CopyOnWriteArrayList is a thread-safe implementation. No state couples the state of each list and Visual component can dedicate it's thread-safety responsibilities to the underlying lists.
5. When delegation fails:
    - Classes can have invariants that relate their component state variables. Consider below example that uses two atomic integers to manage state, but imposes an additional constraint, the first number should be less than second number
    ```
    public class NumberRange {
        private final AtomicInteger lower = new AtomicInteger(0);
        private final AtomicInteger upper = new AtomicInteger(0);
    }
    public void setLower(int i) {
        // Unsafe check-then-act
        if(i > upper.get()) {
            throw new IllegalStateException(i + " cannot be greater than " + upper);
        }
        lower = i;
    }
    public void setUpper(int i) {
        // Unsafe check-then-act
        if(i < lower.get()) {
            throw new IllegalStateException(i + " cannot be greater than " + upper);
        }
        upper = i;
    }
    ```
    Both setLower and setUpper are not thread-safe, they don't use sufficient locking to make their check-then-act sequences atomic. Because the underlying state variables lower and upper are not independent, NumberRange can't delegate it's thread-safety to its thread-safe variables. We can use locking here and also ensure that lower and upper are not published.
    - If a class is composed of multiple independent state variables, and has no operations that have any invalid state transitions, then it can delegate thread-safety to the underlying state variables.
    - The problem that prevented NumberRange from being thread-safe is actually very related to one of the rules about volatile variables "a variable is suitable for being declared volatile if it does not participate in invariants involving other state variables"
6. Publishing underlying state variables:
    - When you delegate thread safety to underlying state variables of your class, the conditions under which you publish those depend on the invariants governing those state variables.  
    - For example: 
        - For the Counter class, the value field must be positive and increment operation constrains the set of valid states given a current state. 
        - For a certain Temperature class containing a state variable holding the last temperature measured, any other class can safely modify this value without violating any invariants.
    - If a state variable is thread-safe, does not participate in any invariants constraining it's value, has no prohibited state transitions for any of its operations then it can be safely published.
7. Vehicle Tracker Publishing it's State
    - Thread-Safe Mutable Point class
    ```
    public class SafePoint {
        private int x, y;
        private SafePoint(int a[]) {
            this(a[0], a[1]);
        }
        public SafePoint(SafePoint p) {
            this(p.get())
        }
        public SafePoint(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public synchronized int[] get() {
            return new int[]{x, y};
        }

        public synchronized void set(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    ```
    - The Getter gets both x and y as an two-element array. Had we provided separate getters/setters, values could change between the time one coordinate is retrieved and the other coordinate is retrieved. 
    - Private constructor exists to avoid the race condition that would occur if the copy constructor were implemented as this(p.x, p.y) (Private constructor capture idiom)
    - Vehicle Tracker that Safely publishes Underlying State
    ```
    public class PublishingVehicleTracker
    ```
