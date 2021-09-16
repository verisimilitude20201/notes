Video: https://www.youtube.com/watch?v=1o7bB4hUPew&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=18(5:00)

# Why is REST Scalable?


1. Going back to 2000 - 2005 when the client-server architecture was very popular and we have a set of common clients on a single LAN connecting to an Oracle database. On each client, we need the Oracle 11G client installed on each client to connect to the Oracle database. So there is a high coupling here - each machine needs the same (version preferably of the client and there are N connections to the database at a point in time)

2. After a few years, if we need to upgrade the database to 12G, we would have to go to each machine, upgrade the client to 12G (assuming 11G is incomptabile with Oracle 12G). Consider 1000s of machines and we have to bring the application down to make this upgrade. The problem is server is tied to the client.