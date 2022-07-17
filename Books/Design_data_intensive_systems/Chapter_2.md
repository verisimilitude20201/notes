# Data Models and Query languages

## Data models

1. Most applications are built by layering one data model on top of the other. For each layer the key question is how is it represented in terms of the next lowest layer
   - As application developer, you view the real world (people, orgs, goods, actions) and model it in terms of objects and data structures and have APIs manipulating those data structures
   - While storing these structures, you express them in JSON, tables in RDBMS or Graph model
   - Database software represents these as bytes in memory or disk and the database representation may allow querying, searching and manipulation of these bytes
   - Hardware engineers figure out how to represent bytes in terms of magnetic field, electric current, pulses of light.
2. Each layer abstracts away the complexities of the layers underneath. It allow different sets of people to work together. 
3. Many different kinds of data models and every data model embodies assumptions about how it's going to be used. 


## Relational Mode Vs Document model
1. Relational model made popular by E.F. Codd's paper where data is organized into relations and each relation is a collection of unordered tuples
2. RDBMS became the tools of choice for most people who wanted to store and query data with some kind of regular structure. 
3. It's roots lie in business data processing on mainframe computers. Typically, transaction processing, batch processing, customer invoicing. 
4. The goal of the relational model was to hide implementation detail behind a cleaner interface. 
5. Each competitor to the relational model created a lot of hype but it never lasted. 
6. As computers became more powerful, networked and being used for a wide variety of purposes, RDBMSes too generalized very well beyond their originally intended use of business data processing.

## The Birth of NoSQL.
1. NoSQL was meant as a catchy Twitter hashtag for a meetup on open source, distriubuted, non-relational databases. Did'nt refer to any particular technology.
2. Retroactively interpreted as Not-Only-SQL (NoSQL)
3. Driving forces behind NoSQL Adoption
   - Greater scalability than relational databases can achieve
   - Very high write throughput and very large datasets.
   - Widespread preference for free and open source databases
   - Specialized query operations not well supported by relational model
   - Frustration with the restrictiveness of relational schemas, a need to have more dynamic and expressive data models.
3. In the forseeable future, relational databases would continue to be used with non-relational databases (Polyglot Persistence). Different applications have different requirements and technology for one use case may be quite different from another.

## Object-Relational Mismatch
1. A common criticism of the SQL data model when object oriented languages are used to develop applications is: an awkward translation layer is required between the objects in the application code and database model of tables, rows and columns. This disconnect is termed as impedance mismatch.
2. ORM frameworks attempt to reduce the amount of biolerplate code required for this translation, but they don't completely hide the differences. 
3. How a resume is represented in the relational world
   - users table contains fields like first name, last name, age, birthdate
   - jobs table holds all the current & past job positions that held by the user (one-to-many) relationship
   - education table holds all the users degrees and educational details
4. The above one-to-many relationships can be represented in several ways - 
   - Traditional SQL puts position, education information into separate tables with a foreign key reference in users table
   - Later SQL versions allowed for multi-valued data to be stored in the single row with support for querying and indexing inside those documents
   - Third way is to encode everything as a single JSON and store it inside the row. You cannot typically query for values encoded within the JSON.
5. JSON is quite appropriate for representing a resume, since a resume is mostly a self-contained document. JSON is much simpler than XML. Document-oriented databases like MongoDB, RethinkDB, CouchDB support JSON data-model
6. The JSON model has a better data locality, every information that you need is at one place. The one-to-many relationships between user's profile and contact positions explicitely represent a tree structure and JSON makes this explicit.

## Many-to-one & Many-to-many relationships
1. You normally have human meaningful information such as region IDs, industry IDs in a resume that you represent as IDs instead of storing it as text and duplicating in each profile. One advantage of using IDs is they never change even if the information identified by them changes (for example: changes to a city name) 
2. Anything that is meaningful to humans may change at some point in time and if that information is duplicated, all the redundant copies too must change. Removing such redundancy is the key idea behind normalization in databases. 
3. Normalizing these many-to-one relationships (many people living in one city) is difficult in a document model because support for joins is weak. The work of making the join is shifted to the application code.
4. If the initial version of an application fits into a join-free model, data has a tendency of becoming more connected as features get added to applications. Some examples of how this pans out in the case of a resume.
   - Organizations and schools where the user worked and studied can be separate entities in themselves. Each resume could link to the user's organizations and school entities 
   - Recommendations appear on the profile of the user who was recommended along with the photo and recommendation text of the user who recommended him. It should have a reference to the author's profile
5. All these require joins to be queried.

## Are Document databases repeating history?

1. Document databases have reopened the debate of how to best represent many-to-many relationships and joins in databases which goes back to the earliest computer systems.
2. Integrated Management Systems (IMS) was a database using in the Apollo Space program released in 1968. It used a simple hierarchical model having similarities to JSON model used in document database.
3. It represented all data as tree of records nested within records. Similar to document databases, it worked well for one-to-many relationships but made many-to-many relationships difficult.
4. Developers had to decide whether to manually resolve references or store denormalized data. These are similar to the problems that developers still face today with document databases.
5. To solve these problems, relational model and network model was proposed. Relational model took over the world and network model faded into oblivion.

### Network Model
1. Standardized by a committee  called CODASYL. 
2. It was a generalization of the hierarchical model, every record could have more than one parent. In hierarchical model, every record could have just one parent. For example: One record for Pune and every user who stays in Pune could be linked to that record. 
3. These links were more of like pointers in programming languages. The only way of accessing a record was to follow a path from a root record along these chain of links. It's called an access path.
4. Access path could be traversal of a linked list. In a world of many-to-many relationships, several different paths could lead to the same record and the programmer has to keep track of these mentally. 
5. If a record has multiple parents, the application code had to keep track of all relationships much like navigating through an N-dimensional space.
6. Manual access path used to make very efficient use of the hardware capabilities of the 1970s. But they made the code for querying data very unmanageable and inflexible.
7. Access paths could be changed but then you had to go through a lot of handwritten DB quer code and rewrite it to handle new access paths. 
8. All-in-all, very difficult to make changes to an application's data model.

### The Relational model
1. Relational model lays out all data as a collection of tuples, that's it. No nesting of records, no complicated acces paths.
2. You can read all rows in a table, apply filtering criteria, read rows by designating some columns as keys, insert new rows into any table.
3. Query optimizer decides which part of the query to execute in which order, which indexes to use. These choices are effectively the access path. 
4. To query data in newer ways, just declare a new index and queries will automatically use which-ever indexes are most appropriate. 
5. Query optimizers are complicated which have consumed years of development and research effort. But you only need to build the optimizer once and then all applications can benefit from it.

### Comparison to document databases
1. Document databases have reverted to the hierarchical model in one aspect: storing nested records within their parent record. 
2. Whe it comes to relationaships, document and relational models in both the related item is referenced by a unique identifier called a foreign key in relational model and a document reference in a document model.


## Relational Versus Document databases today (in terms of data model only)
1. Main arguments in favor of document data model are schema flexibility, better performance due to data locality, it's closer to the data structure that application uses. 
2. Relational model provides better support for joins and many-to-one and many-to-many relationships.

### Which is better document or relational  model? 
1. If the application data has a document like structure where entire tree is loaded at once, then it's a good case for a document data model. The relational structure of shredding a document into multiple tables can lead to complicated application code.
2. Document model however cannot refer directly to a nested item. You need to say something like 2nd organization in the list of orgs that that user has worked in. As long as you don't have too deep a nesting, that's not a problem.
3. Poor support for joins may not be a problem. An analytics application does not need joins. Too many many-to-many joins may make the document model not too appealing.
4. You can keep the data denormalized to eliminate joins. More work needs to be done on application side to keep denormalized data consistent.
5. If joins are done in application code, it moves complexity slightly more to the application side and is usualy slower than specialized code to handle joins directly in database.
6. It's difficult to say which model is better - it highly depends on the kinds of relationship between data items. 
7. For highly interconnected data, both models are awkward to use, better to resort to a Graph model.

### Schema flexibility in document model.
1. Most document databases and JSON in relational databases don't enforce any schema. XML support in relational databases comes with optional schema validation. 
2. No schema means adding arbitrary values to a document. While reading, clients have no guarantee as to what fields the documents might contain.
3. The term schemaless is misleading as the code that reads data assumes some schema.
4. A more accurate term is schema-on-read, the schema is interpreted when the data is read. Contrast this with schema on-write, where the schema is explicit and database ensures all written data conforms to it in relational model
5. Whether the Enforcing of schema should be strict or not is a contentioous topic, there is no right or wrong answer. 
6. The difference between the approaches becomes apparent when a schema migration is supposed to be done. For example: Splitting up the full name of a user into two separate first name and last name.
   - For document database, it's just the introduction of two new keys in the model. The handling code needs to change to handle both old and new data. 
   - For Relational model, it would mean an ALTER TABLE which has a bad reputation of being slow
7. The schema on read approach is advantageous if the data is heterogenous i that
   - Too many different objects and you can't have that many tables
   - the data structure is determined by external systems over which you have no control.
8. In case where all records have same structure, schemas are a useful mechanism for documenting and enforcing schema.

### Data locality for queries
1. A document is stored as a single continuous string encoded as JSON, XML or a binary variant such as MongoDB's BSON.
2. If the application needs to access the entire document, there is performance advantage to this data locality. Instead of that, if we store in relational DB, multiple index lookups and disk seeks are required to access different parts of the document.
3. Locality advantage applies only if you access large parts of the document at the same time.
4. On updates, the entire document usually needs to be rewritten. Only modifications that don't change the encoded size of the document can be performed in-place.
5. For document databases, it's recommended to keep the size fairly small and avoid writes that incease the size of a document.
6. Google's spanner db offers the same data locality in a relational model by allowing the schema to declare that the table's rows should be interleaved within the parent table. Oracle allows the same using a feature called multi-table index cluster. Column family concept within BigTable model has a similar way of managing locality.

### Convergence of document and relational databases
1. XML has been supported in relational systems since 2000s. This includes the ability to do local modifications and to index and query inside XML documents.
2. PostgrSQL and MySQL offer similar capabilities for JSON. It's likely that other relational databases will follow suit.
3. On the document DB side, Rethink DB supports joins in it's query language, MongoDB drivers automatically resolve document references (client side join, although this is much slower than join performed in the database.
4. Relational and Document DBs are becoming more-or-less similar over time. If a database is able to handle documents and perform relational queries on it, applications can use combination of feature that best fit their needs.

## Query languages for Data
1. Relational model introduced a new way of querying data. SQL is declarative whereas IMS and CODASYL were imperative. When SQL was defined, it followed the structure of relational algebra fairly closely.
2. An imperative language tells the computer to perform certain operations in a certain order. Like doing looping, evaluating conditionals, modifying state.
3. In a declarative query language, you define pattern of data you want, what conditions should the data meet, how you want the results to be transformed. It's upto the query optimizer to decide which join algorithm to use, which indexes, and in which order to execute the various parts of the query.
4. Declarative language hides the implementation details of the database system to introduce additional performance improvements without making any changes to the queries.
5. For example: if we have a function that loops through a list of animals and selects only "Sharks". This list of animals comes in a particular order. If the database wants to reclaim/coalesce unused disk space, it might need to move records around, changing the order in which the list is returned. Can this be done by the DB without breaking queries? SQL does'nt guarantee any ordering so it does'nt mind if the order changes.
6. The fact that SQL is more limited in functionality gives the database more room for optimizations.
7. Declarative languages lend themselves well to parallel execution. Today's CPUs are made faster by adding more cores and not through higher clock speeds. Imperative codes is hard to parallelize since it expects the execution to proceed in a certain order. Declarative languages just specify the pattern of results and the DB is more free to use a parallel implementation of the query language

## Declarative queries on the Web
1. On the Web, both XSL and CSS are declarative languages for specifying the styling of a document. 
2. Instead of that, if you had to use the imperative way using the JavaScript DOM model, you would've had to write awful amount of code to achieve the same thing. Plus you won't be able to take any performance advantage without rewriting the code. 
3. In a web browser, using declarative CSS styling is much better than manipulating styles imperatively in JavaScript. Similarly in databases, declarative query languages like SQL turn out to be better than imperative APIs.

## Map Reduce Querying
1. MapReduce is a programming model for processing large amounts of data in bulk across many machines. A form of MapReduce is supported by some NoSQL datastores like MongoDB and CouchchDB for performing read-only queries across many documents.
2. The logic of the query is expressed with snippets of code which are called repeatedly by the processing framework. It's based on map(collect) and reduce(fold or inject) functions existing in many programming languages.
3. Take an example. A marine biologist wants to add an observation record every time he sights animals in the ocean. Now, he wants to create a report of how many sharks are sighted per month. 
   - A simple PostgreSQL query
      ```
      SELECT MONTH(observation_timestamp), SUM(num_animals)
      FROM observations 
      WHERE family="Sharks"
      GROUP BY MONTH(observation_timestamp);
      ```
   - Same can be expressed in MongoDB's MapReduce feature
4. The map and reduce functions are restricted in what they are allowed to do. 5. They are pure functions in that they can use only the data passed to them as input and cannot do any additional DB queries. This restriction allows them to be run anywhere in any order and rerun on failure.
6. MapReduce is a fairly low-level model for distributed execution on a cluster of machines. Higher level SQL can be implemented as a pipeline of operations but there are also many distributed implementations of SQL that don't use MapReduce.
7. MapReduce has a slight disadvantage. You need to write two carefully coordinated functions which is harder than writing a single query. Declarative query language offers more opportunities for a query optimizer to improve the performance of a query. For these reasons, MongoDB added the support for a declarative aggregation pipeline.

## Graph-like models
1. If your applications has no relationships or one-to-many relationships, the document model is appropriate. 
2. As many-to-many relationships become more common, relational databases which can handle simpler many-to-one and many-to-many relationships become too unweildy. It's then that you shift to a Graph model / Graph database
3. Graph consists of two kinds of objects: vertices (nodes or entities) and edges(relationships or arcs)
4. Typical examples of Graphs
   - Social Graphs: Vertices are people, edges are which people know each other
   - Web graph
   - Rail-road network.
5. We can have various Graph algorithms applied to these graphs
   - PageRank determines the popularity of a web page and it's ranking
   - Navigational systems search for the shortest path.
6. A graph may not contain all homogenous data. We can have a graph storing different types of objects in a single datastore. For eg Facebook's social media graph.
7. Several different ways of structuring and querying graphs. Property Graph model is one way implemented by Titan and Triple Store model used by Datomic, AllegroGraph are two ways. 
8. There are 3 declarative query languages for Graphs: Cypher, SPARQL and Datalog. Gremlin is conceptually similar.

### Property Graph model
1. Each vertex consists of
   - Unique identifier
   - Set of outgoing edges
   - Set of incoming edges
   - Collection of key-value properties
2. Each edge consists of 
   - Unique identifier
   - Tail vertex: Edge starts at this vertex. 
   - Head vertex: Edge ends at this vertex.
   - Label describing the kind of relationship between the two vertices
   - Collection of key-value properties

3. Can be moddelled by this relational schema
```
CREATE TABLE vertices (
   vertex_id integer PK,
   properties json
)

CREATE TABLE edges (
   edge_id integer PK,
   tail_vertex integer references vertices(vertex_id),
   head_vertex integer references vertices(vertex_id),
   label text,
   properties json
)
```
4. Few imp properties
   - Any vertex can have an edge connecting with any other vertex.
   - Given a vertex, you can efficiently find both it's incoming and outgoing edges and thus traverse the graph.
   - We can use different kinds of labels for different relationships to store several different kinds of information. Thus we could use this to express different kinds of information that's difficult in a relational schema 
5. Graphs are good for evolvability. As you add features, a graph can be easily extended to accomodate changes to your schema.

### The Cypher query language
1. Declarative query language created for property graphs used in Neo4j database.
2. The query can select a node/vertex and follow all edges/vertices from that vertex that satisfy a certain query. For eg: Find all people born in any city of India and who have emigrated to UAE.
3. You just need to specify the query, the query optimizer automatically chooses the strategy that is predicated to be most efficient.

### Graph queries in SQL
1. In a relational database, you normally know how many joins are needed in your query. 
2. In a graph query, you may need to traverse a variable number of edges before you find the vertex that you're looking for. 
3. In a Cypher query language, the :WITHIN*0.. means follow a WITHIN edge zero or more times 
4. Since SQL:99, this idea of traversal of variable-length traversal paths is very common  and can be expressed using something called recursive common table expressions. 

### Triple Stores and SPARQL 
1. Triple Stores are similar to the Property-Graph model
2. A Triple Store model stores all informations in three part statements of (subject, object, predicate). For example: (Jim=subject, likes=predicate, bananas=object)
3. Subject is equivalent to a vertex
4. Object can be
   - Primitive data-type. In this case predicate and object of the triple are equivalent to the key-value pair for the subject. eg (Lucy, age, 33)
   - Another vertex in the graph. In this case, the subject is the tail vertex, the object is the head vertex and predicate is an edge (Abhijit, marriedTo, Deepali)
5. For example: TripleStore data in Turtle format
```
_:lucy a :Person
_:lucy :name "Lucy"
```
6. Semantic Web: 
   -  Websites publish informations for humans to consume so they should also publish data for machines to consume. 
   -  This is the idea behind Semantic Web. Resource description framework was intended as a mechanism for different websites to publish data to be automatically combined into a web of data
   - Semantic web did'nt really take off as intended due to overly complex proposals and standards. 
   - Triples can be a good internal model for applications even if we don't publish RDF data. Turtle language based on triple store can be a good format for RDF data.

### SPARQL query language
1. Is a query language used for triple data stores using RDF model. It predates Cypher. 
2. We can write a concise query  in SPARQL than it is in Cypher.
3. RDF does'nt distinguish between properties and edges and uses predicates for both, same syntax can be used for matching properties.

### Are Graph model the second coming of CODASYL Network data model? 
1. CODASYL had a schema that specified which record type could be nested in which other record.
   Graph does'nt have such restriction. Any vertex can connect with any other vertex via an edge.
2. In CODASYL, the only way to access a record was to traverse one of the access paths
   In Graph, you can access any vertex by it's unique ID or via an index on one of it's properties.

3. Codacyl records were ordered sets. Applications had to worry about that order while inserting the record.
   In Graph, vertices and edges are not ordered. You can apply sort on them. 

4. CODACYL had all queries imperative, difficult to write and broken easily by the schema. 
   Graph provides the flexibility of both imperative queries and high-level, declarative queries as well.

### Datalog - The Foundation
1. Datalog is an older query language than SPARQL or Cypher and provides the foundations on which most query languages are built on.
2. It's the query language of Datomic and Cascalog which is a datalog implementation for querying large datasets in Hadoop.
3. Datalog's query model is similar to triple-store. Instead of writing it as (subject, predicate, object), we write it as predicate(subject, object). For example:
    ```
    name(namerica, "North America")
    type(namerica, continent)
    name(usa, "United States")
    ```
4. When it comes to query, Cypher and SPARQL jump in right away with SELECT. For Datalog
   - we define rules that tell the database about the new predicate
   - Predicates are not stored in DB, but instead they are derived from other rules or data
   - Rules can refer to other rules.
   - In rules, words in upper case are variable names and rules are matched similar to SPARQL. 
   - A rule matches if the system can find a match for all predicates on the right hand side of the :- operator
   - 