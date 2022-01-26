Video: https://www.youtube.com/watch?v=VnGYyqh4vgU&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=72(20:00)

# The back-end of this Fintech exposed user's personal information


## The Problem
1. The back-end of Fintech faced a side-effect that leads to one user able to see the data of other users

## Root-Cause
1. The engineers at Fintech faced this side effect due to a cache configuation issue.
2. The request goes to the back-end via a CDN that caches information. The information should be cached per user. It's a partitioned cache per user and each request has an identifier to identify the user.
3. The bug was this identification was gone and it was probably picking the wrong user from cache.