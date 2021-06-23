
Video: 

# Response time Percentiles for Latency

1. Why Percentile another metric to measure response times?

   - Imagine that a  server receives requests of 1 million per second. 
   - Minimum response time of 1 ms to service a request does not make sense. There can be 99999 other requests that took more than 1 ms. 
   - Similarly maximum response time of 300 ms does not make sense. 
   - Average can be fair - 300 ms. But there can be occasional requests that took 20 mins. 
   - Percentiles fit the metric best here. When you say 99 percentile response time of 10 ms, it means about 99% of the requests took less than 10 ms.


2. To calculate the percentile say 75th percentile.
   - Sort the array of response times in a non-decreasing order
   - Multiply 0.75 with the total number of requests. So 0.75 * 10 ~ 8. Round it off to the next integer.
   - 8 is the index in the array where you find that value. 
   - To get an accurate percentile, we would need hundreds of thousands of requests

3. Disadvantages
   - It's an approximation
   - For million requests, we need to sort them all.