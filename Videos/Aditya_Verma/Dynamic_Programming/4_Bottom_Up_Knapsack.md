Video: https://www.youtube.com/watch?v=ntCGbPMeqgg&list=PL_z_8CaSLPWekqhdCPmFohncHwz8TY2Go&index=5(33:00)

# Bottom Up Knapsack - Max Profit

1. Bottom-Up approach is better because we don't get Stackoverflow error. 
2. Overall Efficacy of Memoized version and Bottom up versions is similar.
3. For solving a dynamic programming problem we can either solve by memoization or bottom up. We can derive memoized version from recursive code. Similar way we can derive bottom up verison from recursive version.


## Actual method - Bottom up.

I/P

Weight = [1, 3, 4, 5]
Value  = [1, 4, 5, 7]
Capacity = 7

1. Create a matrix of size n + 1 (n = 4) and Capacity + 1 (Capacity = 7)
            
            0   1   2   3   4   5   6   7
        
        V W 0 

        1 1 1

        4 3 2

        5 4 3

        7 5 4

    
    - In this matrix, a value at m[2][3] represents the maximum profite when we have 2 items (1 and 2) and their weights viz. [1, 3] and values viz. [1, 4]
    - For m[3][5], it represents the maximum profit when we have 3 items (1, 2, 3) and their values [1, 4, 5] and weights [1, 3, 4]
    - m[3][7] represents the last maximum profit of 4 items and capacity 7.
    - Each grid cell solves a sub-problem.
       


2. We need to initialize the matrix because we're replacing iteration with recursion. So we need to initialize to handle the base condition. Add the 0s for base condition

        

               0   1   2   3   4   5   6   7
        
        V W 0  0   0   0   0   0   0   0   0

        1 1 1  0

        4 3 2  0

        5 4 3  0

        7 5 4  0

- Recursive base condition
    
        if n == 0 or W == 0:
            return 0

- Iterative Base condition
        
        for i in range(0, n + 1):
            for j in range(0, W + 1):
                if i == 0 or j == 0:
                    a[i][j] = 0

3. Now we need to write the iterative equivalent of the recursive code written for choice diagram. The code will be similar. Only we will replace n and W with i and j and do it in an iterative fashion.