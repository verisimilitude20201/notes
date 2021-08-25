# Video: https://www.youtube.com/watch?v=_gPcYovP7wc&list=PL_z_8CaSLPWekqhdCPmFohncHwz8TY2Go&index=7(15:00)

# Subset Sum Problem

1. Problem: Given an arr = [2, 3, 7, 8, 10], does there exist a subset whose sum is 11? Only return true or False
2. How is this similar to Knapsack?
  - In Knapsack, we are given a maximum weight of Knapsack and two arrays containing the weight and the value array # In this problem, we are given a single array and a sum
  - Knapsack offers a choice whether to include or not include an element # In this problem, similar choice is there while selecting a given number as the first number of the sum. 

3. Initialization: Construct a similar matrix of size n + 1 and W + 1 where n is 6 and W = 11

        S ->   0   1   2   3   4   5   6   7   8   9   10   11
        
        N   0  T   F   F   F   F   F  F    F   F   F   F    F 
        |
            1  T

            2  T

            3  T

            4  T
            
            5  T
            
            6  T

    - 0th Column: We can have a empty subset of sum to be 0 and array including 0 to 5 elements. So 0th column we put True.
    - 0th Row: We cannot have a sum to be greater than 0 when we are not given any element in array. So 0th row will all have F - False 

4. Mapping with Knapsack problem: 
   - Map this array to the Knapsack's problem's weight array and sum to be the Knapsack's total weight.
   - Value array will be absent
   - Intialization will proceed as above
   - dp[i][j] = arr[i - 1][j - arr[i]] || arr[i - 1][j]


