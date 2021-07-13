Video: 

# Recursion

1. Making input smaller
  - In recursion, we do not intentionally make input smaller.
  - Primarily we make some decisions and as a result the input gets smaller and smaller (secondary effect)


2. Recursion - Decision Space: As an identification of recursive problems. We are given some choices and from those we have to pick some on the basis of some decisions.


3. Recursion - Recursive Tree:

    - Consider a subset problem to print substrings of "abc". The subsets are {"", "a", "b", "c", ab", "bc", "ac", "abc"}
    - Choices here are either to include or not to include.
    - Focus on decisions and not on making the input smaller.
    - Recursive tree represents the decisions that we take at each level of processing the input
    - Input-Output method to construct a recursive tree. For example: Below is a recursive tree of subsets problem of "ab"



                    OP = []
                    IP = "ab"

                                [""] : "ab"

                [""]: "b"                             ["a"]: "b"



            [""]: ""        ["b"]: ""            ["ab"]: ""            ["a"]: ""


4. Two-steps to solve a recursive problem
    - Design a recursive tree
    - Write the code.