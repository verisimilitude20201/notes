# Video: https://www.youtube.com/watch?v=iQU39VQfy3s&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=74(09:00)
# Zero downtimes are hard to get right

1. Practise of restarting back-end services without a downtime so user does not feel anything
2. Restarting has several reasons
    - Discard corrupted state
    - Lots of problems go away through restart
    - Outages may require rolling restarts because load balancing components store state of the system
    - To pick up a new version. Only Erlang and Elixir allows hot code-swap.
3. May not have resources to self-heal the application


## How to achieve rolling restart (Zero downtime) restart effectively
1. Consider you have a load balancer and 10 instances of microservices that you need to restart for whatever reason.
2. Do a rolling restart such that the load balancer is even aware of that. Pull out that service out of the pool of the serivces from the back-end. Load balancer has a run-time API that will accept the list of active back-ends to route the requests to. Don't set the back-ends as a configuration in YAML file.
3. Use of an orchestration mechanism like Kubernetes that handles this out of the box. 