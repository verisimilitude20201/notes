Video: https://www.youtube.com/watch?v=IJDJ0kBx2LM&list=LL&index=6 (34:34)

# Recursion

1. ATM Analogy: If you are standing in line for an ATM, we want to know what's our number in line
	a. Brute Force way: We leave our position, go all the way upto the first person, count and store the count in notebook, and increment one by one as we approach our position passing each person
	b. Recursive way: We just tap on the shoulder of the person next to us asking their number in line. She asks the next person and so on and so forth till the first person. First person replies 1. Then second person realizes her number is 2 and so on till our position.
    c. We ask the first question - what is the least amount of work i can do? How do I break the problem into sub-problems 

        def get_my_position_in_line(person: Person) -> int:
            if person.nextInLine is None:
                return 1
            return 1 + get_my_position_in_line(person.nextInLine)
            
    d. We're actually passing an object that progresses closer to the problem that we're trying to solve.


2. Essay Revision analogy: 

        def revise(essay):
            read(essay)
            get_feedback_on(essay)
            apply_changes_to(essage)
            revise(essay) until essay.is_complete 

4. What is recursion?
   - Recursion is a method that can call itself. 
   - At some point in time, you hit a condition known as a base case that is the stopping condition until we no longer recurse.


5. Recursion Pros
	- Bridges the gap between elegance and complexity: Really 3-4 lines of code while traversing trees/graphs
	- Reduces the need for complex loops and auxillary data structures. Recursion uses internal call-stack
	- Can reduce time complexity with memoization
	- Works very well for recursive data structures - JSON, XML, trees, graphs, 

6. Recursion Cons
    - Slowness due to CPU overhead due to repeated recursive calls. Time/Space trade-off.
    - Can lead to out of memory / stack overflow errors due to poorly written recursions
    - Can need to unnecessarily complex poorly constructed code.

7. Call Stack
    - On work someday, you start your day attending your email.
    - Boss interrupts and says you have to attend a meeting.
    - In the meeting, the Boss interrupts you again and says you've to go to an investor meeting
    - Then again, boss interrupts and asks you to help Jason with his code. 
    - Once you finish helping Jason, you resume the investor meeting, after it completes, you resume the prior meeting and then finally go back to checking your email. 
    - The 4 items pop off the to-do list one-by-one. This is exactly how the call stack works.
    - Call stack is a sort of abstraction that the operating system uses to store method invocation, return addresses and method local variables. 
    - For each method invocation, a stack frame goes into the call stack

8. Divide and Conquer
   - Divide into sub-problems. Normally sub-problems are similar to the original one.
   - Conquer sub-problems by solving them recursively.
   - Combine the solutions to get a solution to the subproblems. Finally a solution to the original sub-problem

9. Tail call recursion is a compiler optimization especially in functional programming languages to reduce stack overflows. It looks at ensuring that the last function call is a recursive one. Rule of thumb is always make the recursive call the last instruction. Supported by mostly all functional languages.