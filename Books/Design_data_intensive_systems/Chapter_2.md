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
