# Over-engineering in Software

Over-engineering solves the problems which the users don't ask for or they don't really care about. 

## Over-engineering in Coding
1. Using design patterns where they are not necessary. 
2. Using all sorts of base classes, abstract classes where they are not needed. Gives rise to a spaghetti code.
3. RabbitMQ has so many abstractions. Nginx has so many low-level documentation which is of importance only to developers.


## Over-engineering in System design
1. Starts with micro-services since 4-5 years. Mid 2012s service bus became popular. We start breaking stuff into components just for the sake of breaking it.
2. Make solution simple and elegant. For example: A Website for a parking can get away with a few VMs rather than a Kubernetes cluster/Mesos cluster.