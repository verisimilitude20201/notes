# Introduction

1. Threads simplify the development of complex systems by turning complex asynchrounous code into synchronous one. 
2. Threads are the easiest way to tap into the computing power of multi-processor systems. As processors increase, the ability to tap into it increases even more.

## Very brief history of concurrency
1. Earliest operating systems / computers allowed only a single program to run at a time. Inefficient use of computing resources. 
2. Operating systems evolved to allow more than one program at once: isolated independently runnin programs to which the OS allocates resources viz. memory, file handles. 
3. Motivating factors that lead to OS's support multiple processes that can execute at the same time
    - Resource utilization: Wait time that's there can be utilized more efficiently to let other program run. Wait time is because a program may require some resources such as I/O. 
    - Fairness: Multiple users/programs can lay equal claims on the machine's resources. It's preferable to share them via fine grained time-slicing rather than sequential execution of programs. 
    - Convienience: Often easy to write several programs to perform a task and have them coordinate with each other rather than write a single program that performs all the tasks. 
4. Nearly all modern programming languages follow a sequential model (that came from earliest Von Neumann timesharing systems) where the language specification clearly defines what comes next. Finding the right balance of asynchronony and sequentiality is a characteristic of efficient programs. 
5. The same motivation (fairness, resource utilization, convienience) that motivated the development of processes led to the development of threads. 
6. Threads allow multiple program control flows to coexist in a process. They share process wide resources such as memory and file handles but each thread has it's own program counter, stack and local variables.
7. Multiple threads within the same program can be scheduled on multiple CPUs. Most modern OS treat threads as units of scheduling so they are also called as lightweight processes. 
8. All threads have access to the same variables and objects from the same heap as the process. They require explicit synchronization coordinate access to shared data otherwise a thread may modify variables that another thread is using.

## Benefits of Threads
1. Multiprocesser systems were expensive once but are commonplace now. This trend will now accelerate. It's harder to scale up clock rates so processor manufacturers will put more processor cores on a single chip.
2. When designed properly, multithreaded programs can improve throughput by utilizing processor resources more effectively.
3. Using multiple threads helps to achieve better throughput. A single threaded program  keeps the processor idle while it waits for a blocking I/O operation. Multi-threading allows other thread to run while the first thread waits.

## Simplicity of modeling
1. As in real life, a program that processes one type of task sequentially is simpler to write, less error prone, and easier to test than running different types of tasks at once. 
2. We can assign a thread to each type of task to afford the illusion of sequentiality and insulates domain logic from the details of scheduling, interleaved operations, asynchronous I/O and resources waits.
3. A complex async workflow can be decomposed into number of simpler synchronous workflows running in separate threads interacting with each other at certain sync points.
4. Similar thing is done in Servlets; when it's service method is called in response to a web request, it can process the request synchronously as a single-threaded program. No need to worry about how many other requests are being processed at the same time.

## Simplified handling of asynchronous events
1. If each request to a socket server is processed in its own thread, if a certain request is blocked for I/O, it does'nt affect the processing of other request. Single-server programs use non-blocking I/O which is complex to use. 
2. Historically, Operating systems placed low limits on the number of threads that could be created by processes. Therefore, UNIX non-blocking I/O select and poll() system calls were introduced and Java had java.io.nio. 
3. However, OS support for large number of threads has improved, making the thread per client model practical even for large number of clients

## More responsive GUI frameworks
1. GUI applications used to be single-threaded. It would either require to poll throughout the code for input events or the entire code would be executed as a one large main event loop.
2. If code called from main event loop takes too long to execute, entire UI used to freeze until the code finishes and subsequent user interface events can't be processed till control returns to the main loop.
3. Modern GUI frameworks replace the main event loop with an event dispatch thread. All user interface events get called in an application-defined event handler in a separate thread.
4. For long running tasks, such as spell-check on a large document, the UI used to freeze till it completes in case of a single-threaded main event loop. IF this long running task is executed in a separate thread, the event thread remains free to process UI events. 

## Risks of threads
1. Mainstream developers now need to be aware of thread-safety issues.

### Safety Hazards
1. In the absence of sufficient synchronization, the ordering of operations in multiple threads is unpredictable and surprising
2. Class UnsafeSequence generates a series of unique integer values is an example that works correctly in a single-threaded environment but multi-threaded environment it does not.
```
Program 1
@NotThreadSafe
public class UnsafeSequence {
    private int value;
    public int getNext() {
        return value++;
    }
}
```
Two threads with some unlucky timing, may receive the same value
```
A    value -> 9 -----> value -> 10 -------> value -> 11

B           value -> 9  ----> value -> 10 -------> value -> 10
```
3. The increment operation is basically three operations
    - Read value
    - Increment it
    - Write the new value.
4. Due to arbitrary interleaved execution of two threads by the runtume, it's possible for two threads to see the value and add one to it and write the same value. This is called a race condition.
5. Threads share the same memory address space as of the process and so can access/modify other variables the other threads are using. This makes data sharing easy. But a significant risk is threads can confuse by having data change unexpectedly. It introduces an element of non-sequentiality into an otherwise sequential programming model. 
6. Access to shared variables must be synchronized and coordinated. Below is a predictable version of Program 1
```
Program 1
@ThreadSafe
public class UnsafeSequence {
    @GuardedBy("this") private int value;
    public synchronized int getNext() {
        return value++;
    }
}
```
7. In absence of synchronization, the compiler, hardware, and runtime are allowed to take substantial liberties with the timing, ordering of actions such as caching of variables in registers or processor local caches. These tricks aid in better performance but place a burden on developers to identify where data is being shared across threads. 

### Liveness Hazards
1. The use of threads introduces additional safety hazards not present in single-threaded programs. The use of threads introduces additional forms of liveness failures that do not occur in single-threaded programs.
2. Safety means nothing bad ever happens, liveness implies something good eventually happens. 
3. An example of a liveness failure is an activity that lands in a state wherein it's unable to make any forward progress. Say Infinite loop in single-threaded programs. For concurrent programs, if a thread A is waiting for a resource held by thread B, B never releases it, A waits forever. Other examples include deadlock, starvation, livelock. 
4. Bugs causing liveness failure are elusive since they depend on the relative timing of events in different threads. 

### Performance hazards
1. Liveness means eventually something good will happen. However, we want good things to happen quickly.
2. Performance hazards subsume a broad range of problems including poor service time, throughput, scalability, resource consumption. 
3. Threads nevertheless carry some degree of runtime overhead. 
4. Context switches when the scheduler suspends the currently active thread so another thread can run have signficant costs
    - Saving and restoring execution context
    - Loss of locality
    - CPU time spent in scheduling threads instead of running them.
5. Threads sharing data must use explicit synchronization mechanisms that inhibit compiler optimizations, flush or invalidate memory caches, create synchronization traffic on shared memory buses. 

## Threads are everywhere
1. Frameworks may create threads on your behalf and code called from these threads must be thread-safe. Even JVM uses threads. For example: JVM housekeeping tasks are performed in background threads.
2. You have to familiar with concurrency and thread safety because these frameworks create threads and call your components from them. 
3. Frameworks introduce concurrency by calling application components from framework threads. Components invariably access application state thus requiring code paths accessing that state to be thread safe.