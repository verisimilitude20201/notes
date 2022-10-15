Video: https://www.youtube.com/watch?v=09_LlHjoEiY (02:10:36)

# Graphs

1. Graph theory is the mathematical thoery of properties of graphs and their applications. For example: A social network of friends. It allows us to answer how many friends a given person has, the nth connections of that person, mutual connections and so on.

## Types of graphs
1. Undirected graphs: Have no orientation. If an edge is there from node u to node v, it's identical to node from v to u
2. Digraphs: Have directions or orientation. If you go from node u to node v, you can only go one way and not the other way round.
3. Weighted Graphs: Edges on graphs can contain weights to represent arbitrary values such as cost, distance. These come in directed/undirected flavors
4. A Tree is a special type of undirected Graph with N nodes and N - 1 edges with no cycles
5. A Rooted tree is a special type of tree where every edge either points away or towards the root node. When they point towards the root node, it's called an arborescence (out-tree fairly common). IF they point away, it's called anti-arborescence (in-tree)
6. Directed Acyclic graphs: Directed graphs with no cycles. Represent structures with dependencies such as a build system, scheduler. All Out-trees are DAGs but not all DAGs are out-trees. Topological sort is an important application of DAGs which allows us to order tasks per depencies. Tasks with 0 dependency should be performed before tasks with 1 dependency and so on.
7. Bipartite graphs: Vertices can be split into 2 groups u and v such that every edge connects between u and v. A problem that be asked is what is the maximum matching that can be created on a bipartite graph? Suppose white nodes are jobs and red nodes are people. We can ask ask how many people-job matches can be possible. Critical in network flow application
8. Complete Graph has a unique edge between every node. To test your graph for performance, a complete graph is a way to start

## Representation of graphs

### Simplest Way - 2D Adjancent matrix

A        C               
                  
B        D

[ A  B   C   D 
A 0  1   1   1
B 1  0   0   1
C 1  0   0   1
D 1  1   1   1
]

1. The idea here is cell M[i][j] represents the weight of going from node i to node j. Or it may also indicate that nodes i and j are connected using a simple 0 and 1
2. Pros:
    - Very space efficient for dense graphs
    - Edge weight lookup / connectivity can be constant time
    - Simplest
3. Cons:
    - Worse space complexity O(V^2) space
    - O(V^2) to iterate which is fine for dense but for graph

### Adjancency List
1. A way to list a graph as a map of edges. 
2. Each node tracks all it's out-going edges.
3. We only need to track two things in the list the node we are going to and may be its cost if its weighted

A       C                        {A: [C, B], C: [A, D], D: [], B: [A]}
                          => 
B       D
4. Pros
    - Efficience for Sparse graphs
    - Iterating over all edges is very efficient
5. Cons
    - Less space efficient for dense graphs
    - O(E) edge weight lookup
    - Complex representation.

### Edge list
1. Represents a graph of unordered list of edges (u, v, w) where u and v are vertexes and w is its cost.
2. This structure is very simple but lacks structure so seldomly used
3. Pros
    - Efficience for Sparse graphs
    - Iterating over all edges is very efficient
    - Very simple
4. Cons
    - Expensive to store/process dense graphs.
    - O(E) time complexity to iterate edges.

## Common Graph theory problems
1. While dealing with graph theory problems should ask below questions
    - Is Graph directed or undirected?
    - Are the edges of the graph weighted or unweighted?
    - IS it spare or dense
    - What representation is most efficient for the problem?
2. Shortest Path problem: Given a weighted graph, find the shortest path between A and B in terms of cost. BFS for undirected graph, Bellman ford and Dikshtra, A* for directed graphs can be used to find shortest path
3. Connectivity between two nodes: Does there exist a path between node A and B? Typical way is to use Union Find or DFS
4. Detect negative cycles in directed graphs: The overall traversal cost can get negative due to negative weights on graph edges. In some scenarios, this can be helpful for example determine arbitrage during currency exchange. So, it's possible to cycle through different currencies and come back at a currency that you started with. At the end, you either have a risk-free gain or a loss. Floyd Warshal and Bellman ford algorithms can be used to detect negative cycles
5. Strongly connected components: Every vertex in the graph connected to every other vertex. Tarzan's algorithm can be used for this case.
6. Travelling salesman problem: Given N cities and starting from a certain city what is the shortest possible route visiting each city exactly once and comes back to the starting point. This is NP hard complexity. Healed Karp algorithm with Dynamic programming
7. Number of bridges in a graph: Bridges are edges if cut increases the number of connected components in the graph. Think of graphs as telephone networks between islands or countries, you can immediately think of the usefulness of this. Bridges often hint at weak points, bottlenecks or vulnerabilities in graph
8. Number of articulation points: These are vertices if cut increases the number of connected components in the graph.
9. Minimum Spanning Tree (MST): Subset of the edges of a connected, weighted graph that connects all vertexes together without any cycles and shortest possible total weight. All minumum spanning trees of the same graph may not be identical but they have the same minimum weight. Have lots of applications like transportation network, cabling, network and so on.
10. Network flow: Finding the flow through the flow network. With an infinite input source, how much flow we can push through the network? For a graph representing roadways between point A to point B, how much amount of cars we would send at maximum to ensure optimal flow through the network? With this maximum flow problems, we can identify the bottlenecks that slow down the whole network and fix edges at a lower capacity.

## Graph algorithms

### Depth first search
1. Important algorithm used to discover all vertexes and edges of a graph with O(V + E) time complexity directly proportional to the size of the graph
2. Really easy to code.
3. Not useful by itself but when augmented to perform other tasks such as count connected components, find bridges, articulation points, A DFS is very useful.
4. Don't visit already visited nodes. Backtrack to the last node. 
5. Pseudo code
```
visited_nodes = {}
g = Graph
def dfs(node):
    if node in visited_nodes:
        return
    neighbors = g[node]
    visited_nodes.add(node)
    for neighbor in neighbors"
        dfs(neighbor)
    
```
6. Simple use-case - Connected components are multiple disjoint components in a graph. We can color these nodes in the same component with the same color. We can use DFS for this.
```
g = Graph
n = number of nodes
count = component count
components = [0] * n
visited = set()
def count_components():
    for i in range(n):
        if i not in visited:
            count += 1
            dfs(i)
        
    return (components, count)

def dfs(node):
    if visited[node]:
        return
    components[node] = count
    visited[node] = True
    for neighbor in g[node]:
        dfs(neighbor)
```

7. We could augment the DFS to do some more
    - Compute a graph's MST
    - Detect and find cycles in a graph
    - Check if a Graph is bipartite
    - Topological sort the nodes of a graph
    - Find bridges and articulation points
    - Find augmenting paths in a flow network
    - Generate mazes

### Breadth first search
1. Again O(V + E)
2. Particularly useful for finding shortest path.
3. Visits graph in a layered fashion. Visits a node, then all it's neighbors. Then it's neighbors' neighbors
4. It uses a queue to store all neighbors to be explored. Queue data structure  
5. Implementation
```
# Global/class-level variables
g = Graph
n = number of adjacent nodes in the graph
def bfs(start_node):
    q = [start_node]
    visited = set(start_node)
    while len(q):
        node = q.popleft()
        for neighbor in g[node]:
            if neighbor not in visited:
                visited.add(neighbor)
                q.append(neighbor)

```
6. We can use a BFS to find shortest path between two nodes. So we can proceed in two steps
    - Start at the start node and construct an array storing the previous vertex of each vertex. Stop when all vertexes are reached.
    - Then we can trace backwards from the end node to the start node and connect the path in an array

    ```
    g = Graph
    n = number of adjacent nodes in the graph
    def shortest_path(a, b):
        prev = bfs(a)
        path  = []
        at = e
        while at or at in prev:
            path.append(at)
            at = prev[at]
        if path[0] == a:
            return path.reverse()
        return []

    def bfs(start_node):
        q = [start_node]
        visited = set(start_node)
        prev = []
        while len(q):
            node = q.popleft()
            for neighbor in g[node]:
                if neighbor not in visited:
                    visited.add(neighbor)
                    q.append(neighbor)
                    prev[neighbor] = node

    ````

#### BFS on a grid
1. Many problems in graph theory can be represented by using a grid.
2. Grids are a form of implicit graph because we can determine  our node's neighbors based on our location within the grid. For example: Path finding problem through a maze
3. A common approach to solving graph theory problems is to convert the grid into a familiar format such as an adjacency list/matric
4. Transformations to a grid structure are generally avoided due to the structure of the grid
    ```
    directions  = [(0, 1), (0, -1), (1, 0), (-1, 0)]
    for i in range(4):
        for direction in directions:
            rr = r + direction[0]
            cc = c + direction[1]
            if rr < 0 or rr >= 4 or cc < 0 or cc >= 4:
                continue
    ```
5. Dungeon problem - shortest path problem: You are trapped in a dungeon and finding your way out! Consider the below dungeoen
        S = Start Node
        E = End Node
        # = Rock
        . = Empty cell
        Find the shortest path between S and E
        [
           [S . . # . . . .],
           [. # . . . . # .],
           [. # . . . . . .],
           [# . # E . . # .],
        ]

```
R = No of Rows
C = No of Columns
m = matrix of size R * C
sr, sc = 'S' start coordinatoes
Q = Queue of coordinates 

# Keep track of the number of steps taken to reach the next step
move_count = 0
nodes_left_in_layer = 1 # Nodes to visit in BFS 
nodes_in_next_layer = 0 # How many nodes we added in BFS expansion

# Track whether 'E' character gets reached
reached_end = false

# R * C matrix of false values
visited = []

# Direction vectors
dvs = [(-1, 0), (1, 0), (0, 1), (0, -1)]

def bfs():
    Q.enqueue((sr, sc))
    visited[sr][sc] = True
    while Q:
        r, c = Q.dequeue()
        if m[r][c] == "E":
            reached_end = True
            break
        explore_neighbors(r, c)
        nodes_left_in_layer -= 1
        if nodes_left_in_layer == 0:
            node_left_in_layer = node_in_next_layer
            nodes_in_next_layer = 0
            move_count += 1
    if reached_end:
        return move_count
    return -1

def explore_neighbors(r, c):
    for direction in directions:
        rr = r + direction[0]
        cc = c + direction[1]
        if rr < 0 or rr >= R or cc <= 0 or cc >= C: continue
        if m[rr][cc] == "#": continue
        if visited[rr][cc]: continue
        Q.enqueue((rr, cc))
        nodes_in_next_layer += 1
        
```
6. Alternative state representation: Storing coordinates requires (x, y) pairs of coordinates which requires packing and unpacking. We can use one queue for each dimension, so in a 3D grid, we would need one queue for each dimension. We would need to enqueue and dequeue all from all queues at the same time.

### Topological sort
1. The motivation for top sort is many real world situations can be modelled as a graph with directed edges where some events must occur before another
    - School class prerequisites
    - Program dependencies
    - Event scheduling
    - Assembly instructions
2. Consider an example. If you are a student of university X, you need to take classes in order of their prerequisites.
3. A topological ordering is an ordering of the nodes of the directed graph where for each directed edge from node A to node B, node A appears before node B in the ordering. Topological sort can find a topological ordering in O(V + E) time. Topological orderings are not unique.
4. A graph containing a cycle can't have a valid ordering. Only directed acyclic graphs can have a valid ordering. We can use Tarjan's strongly connected components algorithm to check if a Graph has cycles. 
6. Every tree has a topological ordering since by definition, trees don't have cycles. To find the top sort of a tree, first visit the leaf nodes and then visit the other nodes.
7. Topological sort algorithm pseudocode
```
def top_sort(graph):
    top_order = []
    visited = set(start_node)
    def dfs(node):
        for neighbor in graph[node]:
            if neighbor not in visited:
                visited.add(neighbor)
                dfs(neighbor)
        top_order.append(node)
    dfs(start_node)

    return top_order
```

### Shortest and Longest Paths on DAGs 
1. DAG is a graph with directed edges and no cycles. All trees are DAGs.
2. Important problem types are shortest and longest paths on DAGs

#### Single Source Shortest Path Problem
2. The single source shortest path problem (SSSP) can be solved efficiently on a DAG in O(V + E) time. 
3. This algorithm does'nt care about positive or negative edge weights. It finds the topological sort and finds the shortest path by relaxing an edge as it is seen. It means updating an edge with a shorter value if a path with a shorter weight can be obtained.
4. The first step is to compute a topological ordering of the nodes. The Second step is to update the shortest possible distance to each node and perform node relaxation if a path with a shorter weight can be obtained.

#### Longest path in a DAG
1. This problem is NP-Hard, but solvable in linear time on a DAG
2. The trick is find the shortest path, multiply all edge values by -1 and then multiply edge values by -1 again

```
# Some pseudo-code for shortest path in DAG
# 
g = Adjancency list of weighted DAG. We store the DAG as
{
    0: [(1, 10), (2, 3)]
}
Each edge also stores the weight.

#
def shortest_path(G, start_node, num_nodes):
    top_sort = get_topological_sort(g)
    distance = [None] * num_nodes
    distance[start_node] = 0
    for i in range(num_nodes):
        node_index = top_sort[i]
        if distance[node_index] is not None:
            for edge in g.get(node_index):
                new_distance = distance[node_index] + edge[1]
                if distance[edge[0]] is None:
                    distance[edge[0]] = new_distance
                else:
                    distance[edge[0]] = min(distance[edge[0]], new_distance)
    
    return distance
```

#### Dijkshtra's shortest path algorithm
1. It is a single source shortest path algorithm for graphs.
2. You need to specify a starting node indicating the relative starting point for the algorithm. Once you do this, this algorithm can tell you the shortest path between that node and all other nodes in the Graph.
3. The time complexity of this is O(E log V) which is fairly competitive. The time complexity depends on what data structures you use and how you implement it
4. All edge weights should be non-negative. Once a node has been visited, it's optimal distance cannot be improved by taking into account the negative weight. 
5. This enables the algorithm to take a greedy nature to take the best of what's availaible

##### Basic lazy version of the algorithm
1. Maintain an array called distance where the distance to each node is positive infinity
2. Mark start node distance to 0.
3. Maintain a priority queue of key value pairs of (node, distance ) which tells you which node to visit on the basis of sorted distance values.
4. Insert (s, 0) in the priority queue
5. Loop while the priority queue is not empty
    - Iterate over all outward edges from the current node 
    - Relax each edge by appending a new (node, distance) tuple to the Priority Queue for every relaxation
6. Once the Priority queue is empty, the distance array will contain the shortest distance to every node in the graph
7. Here we explore all paths and lazily delete the paths with a larger weight.

```
# g = adjacency list of weighted graph
# n = number of nodes in the graph
# s = index of the starting node
def dikshtra(g, n, s):
    visited = {}
    distance = [math.inf] * n
    distance[s] = 0
    pq = heapq((s, 0))
    while len(pq):
        node_index, min_value = pq.poll()
        visited.append(node_index)
        if distance[index] < min_value:
            continues
        for neighbor_index, cost in g[node_index]:
            if neighbor_index in visited:
                continue
            new_distance = disdistancet[index] + cost
            if new_distance < distance[neighbor_index]:
                distance[neighbor_index] = new_distance
                pq.add((neighbor_index, new_distance))
    
    return distance
```
Here we re-insert the duplicate key-values in the same priority queue Hence it's lazt. We include an optimization that checks if the minvalue is greater than the value at the index, skip it.
8. Next along with finding the shortest distance, we also try to find the shortest path. We just would need to keep track of the prev node.

```
# g = adjacency list of weighted graph
# n = number of nodes in the graph
# s = index of the starting node
# e = index of the ending node
def dikshtra(g, n, s):
    visited = {}
    distance = [math.inf] * n
    distance[s] = 0
    pq = heapq((s, 0))
    prev = [None] * n
    while len(pq):
        node_index, min_value = pq.poll()
        visited.append(node_index)
        if distance[node_index] < min_value:
            continues
        for neighbor_index, cost in g[node_index]:
            if neighbor_index in visited:
                continue
            new_distance = disdistancet[index] + cost
            if new_distance < distance[neighbor_index]:
                prev[node_index] = neighbor_index
                distance[neighbor_index] = new_distance
                pq.add((neighbor_index, new_distance))
    
    return (distance, prev_index)

def shortest_path(g, s, e, n):
    distance, prev = dikshtra(g, n, s)
    path = []
    node_index = e
    while prev[node_index]:
        path.append(node_index)
        node_index = prev[node_index]
    return path.reverse()
```
9. Few optimizations - 
    - Stopping Early: Do we still have to visit every node when we know the start node and the end node? It's possible to stop as soon as we visit the destination node. Dikstra's algorithm processes each next nodes in the most promising order. If the destination node has been visited, it's shortest distance will not change as more future nodes are added

##### Eager Dikshtra using an indexed priority queue
1. Current implementation inserts duplicate key-value pairs (key = node index, value = node distance to get to that node) because it's more efficient to insert a new key value pair in priority queue.
2. The eager algorithm improves this by using an indexed priority queue.
3. Indexed priority queue: Allows access to key-value pairs within the priority queue in constant time and updates in log time. 

```
g =  Graph representation as an adjacency list
n =  number of nodes
s = index of the starting node
def dikshtra(g, n, s):
    visted = set()
    distance = [math.inf] * n
    ipq = empty indexed priority queue
    ipq.insert(s, 0)
    distance[s] = 0
    while len(ipq):
        node_index, min_value = ipq.poll()
        visited.add(node_index)
        if distance[index] < min_value:
            continue
        for neighbor_index, cost in g[index]:
            if neighbor_index in visited:
                continue
            new_distance = distance[index] + cost
            if new_distance < distance[neighbor_index]:
                distance[neighbor_index] = new_distance
                if not ipq.contains(neighbor_index):
                    ipq.insert(neighbor_index, new_distance)
                else:
                    ipq.decreaseKey(neighbor_index, new_distance)
    
    return dist

```

##### D-ary Heap optimization
1. When executing Dikshtra's there are lot more updates on dense graphs (decrease key) operations to key-value pairs than there are dequeue operations
2. D-ary heap is a heap variant having D children for each node. Removals are expensive but decreaseKey is optimal
3. The value of D should be E/V to use to balance removals against decreaseKey operations improving Dikshtra's time complexity to O(E * log  (V)) which is much better for dense graphs having lot of decrease key operations
                                      (E/V)
4. A Fibonacci heap gives Dikshtra's algorithm a time complexity of O(E + log(V)). But it's fairly complex to implement and provides a very large amortized time complexity                                      

#### Bellman-Ford shortest path algorithm
1. Single source shortest path algorithm from one node to any other node.
2. It's not ideal because it has much worse time complexity of O(E * V). Diksthtra can do better with O(E + log V) with a binary heap
3. Diksthtra fails when a graph has negative edge weights. We should be able to detect it. Dikshtra's get stuck in a infinite loop in a negative cycle.
4. Negative cycles can manifest in several ways, for example negative self loops or a cycle of loops whose net change is -1.
5. Implementation Steps - pseudo code
```
Let E be the number of edges
Let V be the number of vertices
Let S be the id of the starting node
Let D be the array of size V that tracks the best distance from S to each node.
Let graph be an edge list representation of a graph

1. Set every entry in D to Infinity
2. Set D[S] = 0
3) Relax every edge V - 1 times

for i in range(V - 1):
    for edge in graph.edgesL
        if D[edge.from] + edge.cost < D[edge.to]:
            D[edge.to] = D[edge.from] + edge.cost

4. Repeat to find nodes caught in a negative cycle, i.e there should be an optimal path found even after node relaxation
    for i in range(V - 1):
    for edge in graph.edgesL
        if D[edge.from] + edge.cost < D[edge.to]:
            D[edge.to] = -INF
```

#### Floyd-Warshal shortest path algorithm
1. This is an all-pairs shortest path algorithm. This means it can find the shortest path between all pairs of nodes. 
2. With Floyd-Warshal, the optimal way to represent our Graph is a 2-D adjacency matrix where the cell M[i][j] represents the edge weight of going from edge i to edge j.
3. When two nodes are not directly connected, represent this as positive infinity on the graph
4. The main idea behind Floyd-Warshal algorihtth is to gradually build all intermediate routes between i and j to find the optimal path. 
5. The goal of Floyd Warshal is to eventually consider all possible intermediate nodes on paths of different length 
6. How do we compute all intermediate paths? We use dynamic programming to cache all previously optimal solutions

### Performance of Shortest Path algorithms
                 BFS            Dijkshtra           Bellman Ford            Floyd Warshal

Complexity      O(V + E)       O((E + V) log V)      O(VE)                    O(V^3)

Recommended     Large          Large/Medium          Medium/Small             Small       
Graph
Size

Good           Only unweighted  Ok                    Bad                      Yes     
for 
All-Pair
Shortest Path

Can detect         No           No                    Yes                      Yes       
negative cycles

SP on graph        Incorrect     Best                 Works                   Bad            
with weighted 
edges

SP on Graph         Best        Ok                     Bad                 Bad in general 
with unweighted 
edges


