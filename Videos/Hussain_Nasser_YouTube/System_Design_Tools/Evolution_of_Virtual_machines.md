Video: https://www.youtube.com/watch?v=8qU3hZOXlBE&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=17&t=1s(10:00)

# Evolution of Virtual machines

1. In the 90s we used a single machine as a server to deploy applications for customers.
   - Single machine has it's own operating system.
   - With each operating system has its own baggage - GUI, Graphic cards, audio drivers, I/O drivers, printer drivers whether we use it or not.
   - We need specific software for specific use-cases - for example: we use Oracle 11G client 32-Bit as the database.
   - Then we install on top the actual application.
   - Our application needs a different application for running that requires Oracle 12G client and database
   - Since it's the same physical machine, we run into all sorts of problems.

2. Then came virtualization which allowed to run multiple virtual operating systems on the same physical computer.
   - So, we will have two virtual operating systems running different versions of Oracle 11 G and 12 G and Applications 1 and 2 deployed on them.
   - Here we are installing 2 operating systems with all baggage - drivers, GUI cards, audio drivers which are not needed for all use-cases. On the server side, we may not need all these unnecessary drivers - audio, video, printer. This lead to the rise of containers.

3. Google came up with the idea of containers. Take an operating system and use it to it's maximum capacity. The application will be run in a single process (jailed) having it's own world - virtualized CPU, network, disk, memory, I/O and whatever you tell to pull from the host operating system. If you install any additional software in that jailed environment, it will only be restricted to that environment. This leads to rapid, application deployment we can run as many containers as we can.

4. Docker came and made it easy to create containers and they revolutionized containers.

5. If the host machine dies, the containers installed on it die and we need a container management and orchestration mechanism. We need Kubernetes!