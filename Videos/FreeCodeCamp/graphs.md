Video: https://www.youtube.com/watch?v=09_LlHjoEiY (40:31)

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
4. Detect negative cycles in directed graphs: The overall traversal cost can get negative due to negative weights on graph edges. In some scenarios, this can be helpful for example determine arbitrage during currency exchange. Floyd Warshal and Bellman ford algorithms can be used to detect negative cycles
5. Strongly connected components: Every vertex in the graph connected to every other vertex. Tarzan's algoriths
6. Travelling salesman problem: Given N cities and starting from a certain city what is the shortest possible route visiting each city exactly once and comes back to the starting point. This is NP hard complexity. Healed Karp algorithm with Dynamic programming
7. Number of bridges in a graph: Bridges are edges if cut increases the number of connected components in the graph. Think of graphs as telephone networks between islands or countries, you can immediately think of the usefulness of this
8. Number of articulation points: These are vertices if cut increases the number of connected components in the graph.
9. Minimum Spanning Tree (MST): Subset of the edges of a connected, weighted graph that connects all vertexes together without any cycles and shortest possible total weight. All minumum spanning trees of the same graph may not be identical but they have the same minimum weight. Have lots of applications like transportation network, cabling, network and so on.
10. Network flow: Finding the flow through the flow network. With an infinite input source, how much flow we can push through the network? For a graph representing roadways between point A to point B, how much amount of cars we would send at maximum to ensure optimal flow through the network?

## Graph algorithms

### Depth first search
1. Important algorithm used to discover all vertexes and edges of a graph with O(V + E) time complexity
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
6. We could augment the DFS to do some more
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
4. It uses a queue to store all neighbors to be explored. 
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

#### BFS on a grid
1. Many problems in graph theory can be represented by using a grid.
2. Grids are a form of implicit graph because we can determie  our node's neighbors based on our location within the grid. For example: Path finding problems
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
5. Alternative state representation: Storing coordinates requires (x, y) pairs of coordinates which requires packing and unpacking. We can use one queue for each dimension, so in a 3D grid, we would need one queue for each dimension. We would need to enqueue and dequeue all from all queues at the same time.

### Topological sort
1. The motivation for top sort is many real world situations can be modelled as a graph with directed edges where some events must occur before another
    - School class prerequisites
    - Program dependencies
    - Event scheduling
    - Assembly instructions
2. Consider an example. If you are a student of university X, you need to take classes in order of their prerequisites.
3. A topological ordering is an ordering of the nodes of the directed graph where for each directed edge from node A to node B, node A appears before node B in the ordering. Topological sort can find a topological ordering in O(V + E) time. Topological orderings are not unique.
4. A graph containing a cycle can't have a valid ordering. Only directed acyclic graphs can have a valid ordering.
5. We can use Tarjan's algorithm to check if a Graph has cycles. 
6. Every tree has a topological ordering since by definition, trees don't have cycles
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
        