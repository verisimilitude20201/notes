Video:  https://www.youtube.com/watch?v=QjvyiyH4rr0 (40:00)

# Scaling Hotstar

1. Spiky traffic difficult to handle by back-end services. Auto-scaling may not help.
2. Hotstar uses Project Hulk for load testing, load generation, performance and tsunami tests, choas engineering, traffic patterns using ML. They use 3000 or more sci-fi class machines to generate the load that hits Hotstar API services or applications
3. Hotstar load testing affected other customers on the same public cloud and the CDN partner regions or edge locations used to get overwhelmed. To avoid other customers getting impacted, they moved to a geographically distributed load generation. So they've load generation machines in 8 different AWS regions and all of it generates loads together so a single region is not overwhelmed with traffic. This also helps for tsunami testing which are sudden spurts and dips in the load during a special event.

## Load-generation testing architecture
1. Sci-fi large machines located in 8 regions goes through the Internet via CDN to the load-balancer (ELB). Auto-scalar scales the back-end applications per load.
2. Capacity of each load-balancer is limited to peak. So we shard load balancers for each single application. For each application, 4 to 5 load balancers, weighted routing so the load is balanced and Hotstar is able to scale the application. 
3. Common applications hosted on EC2 or Kubernetes

## What does scaling look like
1. Growth rate is 1 million per minute. Advanced scale-up. Cannot start scaling when the peek load reaches 10 million because by the time EC2 is provisioned, the application becomes healthy after registering with load balancer 5-6 minutes are already gone. Scale up proactively in advance
2. Application boot time is in 1 minute and 90 second reaction time to decide to scale or not. If a strategic timeout is there, some breathing space is given.
3. Push notifications are given out by marketing teams. These push notifications get sent to 150 to 250 million users. These can go out at any time.
4. Hot-star uses fully baked AMI instead of insfrastructure as code. Because anything like Chef, puppet that does any configuration after the server is up adds delay to the application.
5. Hot-star does'nt use auto-scaling. 
    - Insufficient Capacity: Let's say you are operating 200 servers supporting 10 million users.  You need to support 15 million users and so you add 600 more servers. You get just 150 and the rest come with insufficient capacity error
    - Single instance type per auto-scale group: Only a single instance type per auto-scale group. If there is a different instance type using more capacity, we can scale up using that.
    - Step size during auto-scaling: Scaling group auto-scales in sizes of 10, 20 servers in all availaibility zones. This process is extremely slow each step of 10 servers gets added in few seconds. Lots of API throttling. Can request AWS to increase the step-size by adding 200 servers at one go. This will cause more damage to the AWS infra because these are like 200 API calls to the infra - bringing up the VMs, registering them to load balancers, adding monitoring for them to CloudWatch, disk attachment APIs. These use your control plane APIs that are transparent to user but at scale they've limits which cannot grow.
    - Game of Availaibility Zones: 3 availaibility zones - 1A, 1B and 1C. 1C has less capacity so you try to increase the capacity of your auto-scaling group by adding more servers. AWS algorithm tries to launch equal number of servers (say 10) in all the three zones equally which is successful. In the second cycle it tries to launch 10 more in 1A and 1B, but 1C goes out-of-capacity. Provisioning a server in an AZ is not in the user'c control since it's taken care by an internal algorithm that AZ has. When you face an error, application will retry in an exponential back-off. First it'll retry 10 seconds. Then 30 seconds, then 1 minute, 5 minute. This harms scaling because infra becomes skewed. 1A, 1B have more servers, 1C has just 10. So 1C goes down and all load comes on 1A which it not able to handle. At times, the provisioning time may increase to 25 minutes. You cannot wait on servers being provisioned in a live match.

### Battle-tested hot-star scaling strategy
1. Pre-warm infra before match time:
    - No automated scaling.
    - Own autoscaling tool. Instead of scaling on default metrics like CPU, memory, it scales on request rate and concurrency. Hot-star gets the total active users in the platform and on the basis of that, they've numbers defined like at 3 million each application will have this many servers. 
    - If the request count per app is high, it'll scale using the request count metric
    - CPU should'nt be a metric to scale upon unless it affects latency. Have benchmarks that each container can handle for the CPU
2. Secondary auto-scaling group: 
    - Problem of single instance type
    - Since ASG (Auto-scaling group) cannot have multiple instance type, Hot-star spins up a secondary auto-scaling group.
    - If there is a C4 large machine, there will be a secondary SGA having an M4 large machine
    - ASG gives notification in case it does'nt have capacity or is unable to scale. So, that is pushed to an SNS queue that triggers a lambda function that automatically scales the secondary auto-scaling group for that application.
3. Spot-Fleet
    - A Spot Fleet is set of Spot Instances and optionally On-Demand Instances that is launched based on criteria that you specify. The Spot Fleet selects the Spot capacity pools that meet your needs and launches Spot Instances to meet the target capacity for the fleet
    - With spot-fleet, you can mix and match multiple instance types either elastic compute or memory.
    - Can spread across 3 availaibity zones. 
    - Can diversify your infrastructure.

## Choas Engineering
1. Art of breaking things and finding breaking points in your system.
2. Push notifications:
    - Marketing team sends out huge number of notifications to 200 million people that sends periodic spikes in the back-end services. 
    - Back-end needs to cope up with the spikes.

3. Increased latency:
    - Even if one application has increased latency in servicing requests, it has impact on other services. 
    - If content APIs has increased latency to 50ms. Other services like personalization engine or recommendation engine depend on these APIs. This in turn will load slowly or increase the app startup time.
4. Network Failures
    - Scary
    - Dependence on CDN providers. If the Edge location goes down, they've to shift traffic. So all those requests may request to origin endpoint, so the application may go crazy to handle that sudden spike. 
5. Delayed Scale-up
    - If scaling script takes time, users may get affected.
6. Tsunamic Traffic: Sudden surge in traffic and sudden fall. Back-end servers and elastic cache RDS cannot be scaled on the fly.
7. Bandwidth constraints: More than 10 TB/s for video consumption which is almost 70% of India's capacity. If the concurrency goes to 10-15 million can the Internet bandwidth handle it? Can the local ISP handle traffic of 10 million people watching the match at the same time?


## Goals of choas engineering
1. Undiscovered issues or hidden patterns: Choas engineering does not bring down a system but determines what happens if your one availaibility zone goes down. Or 30% of your compute capacity is taken away. Any network issue between yu
2. Bottlenecks and choke points: Things that cannot be scaled in time. Datastores and back-end stores that don't have elastic scaling capability
3. Breaking point of each system: Developed an app that determines the RPS and TPS that an application can go down and may introduce a caching system
4. Death Wave: Sudden spike. Adding 1 million/2 million users per minute
5. Failures at any level - network, application, DB

## Panic Mode
1. Knowing the number of API calls your app can make, you can scale your back-end for sufficient number of users. Knowing user journey is important.
2.  Graceful degradation: If application A can only handle 10 million load, i can turn off that application. You can still return a 200 OK and the clients usually are smart enough to know that an application is in panic mode. If client retries the payment say for example, it will allow you to bypass the payment and just watch the match. If the login system say Dynamo system or DB issue, you can put the entire login service is put in panic mode and users without subscription can watch the match
3. Key services should always be up. Degradation is graceful. 
4. Turn off non-critical services. At 50 million users, 99.99 % of the user-base is watching the platform so non-essential things like personalization can be turned off making room for critical API services that deliver video, your ad, concurrency numbers and healthcheck services.
5. P0 systems - Ads, Video, payment systems must always be up
6. At each point, we need to take decision whether this application is critical for business need and whether its near its rated capacity. At 90% rated capacity, hot-star manually degrades it gracefully and clients are not impacted

## Key takeaways
1. Prepare for failure: Always be factors that you cannot do anything. Design applications to handle failures.
2. Understand your user journey: Understand what happens when a user opens an app, what API calls go through, what DB calls / cache calls. If you know that journey, you can script that.
3. Okay to degrade gracefully: Avoid showing errors to user that affects user experience
