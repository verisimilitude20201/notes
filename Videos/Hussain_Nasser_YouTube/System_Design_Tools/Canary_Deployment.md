Video: https://www.youtube.com/watch?v=3IJ5ko8jSIA&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=17

# Canary Deployment

1. Youtube has been conducting an experiment where 3% of all Youtube users see an auto-generated thumbnail as a feature on the video list. 
2. A Reverse proxy load-balances Youtube requests across multiple back-end servers. Back-end servers are completely stateless and identical via content wise.
3. For this feature, Youtube adds the functionality to generate thumbnails for videos.
4. Reverse Proxy now includes the logic to target 3 % of requests to the new server that generates thumbnails. Rest 97% get redirected to other servers. This is called Canary deployments.