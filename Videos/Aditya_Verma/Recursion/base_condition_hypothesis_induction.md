Video: https://www.youtube.com/watch?v=Xu5RqPdABRE&list=PL_z_8CaSLPWeT1ffjiImo0sYTcnLzo-wY&index=3

# Base Condition / Hypothesis / Induction

1. Sometimes if instead of taking decisions, if we focus on making input smaller it helps more than thinking about decisions to solve a recursive problem

## Methods to solve a recursive problem
1. Recursion Tree - I/p - O/p method.
2. Base condition - induction - hypothesis
3. Choice Diagram
4. 

### Base condition - induction - hypothesis

1. We design hypothesis i.e. this function will be designed to solve this problem and it will successfully solve this problem.
2. Base condition is a smallest valid output or smallest invalid output.
3. Used specifically for Tree or Linkedlist type questions.
4. Consider a sample problem to print from 1 to n
    - Hypothesis: If print(n) prints 1,2,3,4,n, print(n-1) prints 1,2, 3, (n-1)
    - Induction: Non-recursive step that does actual step
    - Base condition handles the smallest valid input.

            def n_print(n):
           B     if n == 1:
                    return 1
           H     n_print(n - 1)
           I     print(n)

5. This creates only a singular recursion tree like this
            
            print(6)
            print(5)
            print(4)
            print(3)
            print(2)
            print(1)

