# Equal Sum Partition

## Problem statement: 

Given an array [1, 5, 11, 5]. Divide this into two subsets and check whether it's possible to have subsets whose sum is equal. In this case, it will be [1, 5, 5] and [11]

## Similarity with subset sum problem
This is similar to subset sum problem. There we were given an array and a sum and we had to figure out whether a subset exists in the array whose sum is equals the given sum.

## Reasoning
1. If we can split the array into two partitions P1 and P2 and there sums are equal say S. If we add that S, we get an even number.
2. So if we sum the elements of the array, and the sum is odd, we straight away return False.
3. If the sum is odd, divide the sum by 2. This problem then reduces to the Subset sum problem..