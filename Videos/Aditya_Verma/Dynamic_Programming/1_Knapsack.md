Video: https://www.youtube.com/watch?v=l02UxPYRmCQ&list=PL_z_8CaSLPWekqhdCPmFohncHwz8TY2Go&index=2

# 0/1 Knapsack

## Main types

1. Subset Sum
2. Equals Sum Partition
3. Count of subset sum
4. Minimum subset
5. Target Sum
6. No of subsets of a given sum

## What is Knapsack
1. Knapsack is a bag which can carry items with a fixed weight or capacity (say 7 KG)
2. We are given a set of items say 4. We are also given a weight array and value array
  
               I1 I2 I3 I4
        Weight 2  3  4  5
        Value  1  4  5  7
3. We have to find the maximum value of items that can be put in the sack such that the total weight is less than the total capacity of the sack.

## Types of Knapsack
1. Fractional Knapsack: Let's say we have capacity of 10 KG in Knapsack and we have filled it with items of 9 KG. If we have an item of 2 KG, we can split it and fill it in the knapsack. We use Greedy approach for this.
2. 0/1 Knapsack: Either add the whole item or ignore it. 0/1. Either item is present or it is not present.
3. Unbounded Knapsack: There is no limit to the number of times a given item can be added to the bag.

## How to identify
1. Choice: Each item can be included or cannot be included.
2. Optimal: We need the maximum profit. 
3. Dynamic Programming = Recursion + Storage.

For solving a DP problem,
1. First write a recursive solution
2. Then memoize
3. Then start writing a bottom-up solution.