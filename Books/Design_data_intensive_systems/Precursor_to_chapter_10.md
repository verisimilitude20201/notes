# Derived Data

## Introduction
1. In a large application, you need to be able to process and access data in many different ways and there is no one database that can satisfy all those needs.
2. Applications use a combination of several datastores: caches, indexes, analytics systems and implement mechanisms for moving data from one store to another.
3. Integrating disparate systems is one of the most important things that needs to be done in a non-trivial application. 

## Systems of record and derived data
1. Systems that group and store data can be grouped into below categories 
    - Systems of record: Source of truth, the authoritative source of data. Each fact is represented once (the representation is normalized). This copy is assumed to be the correct one in case of any discrepancy.
    - Derived data systems: 
        - This is the result of taking some existing data from another system and transforming it or processing it in some way. 
        - Cache, denormalized values, materialized views fall into this category.
        - It duplicates existing information, but it is often essential for getting good performance on read queries. 
2. Most databases, storage engines and query languages are just a tool and it depends on how you use them as a system of record. 
3. By being clear on which data is derived from which other data, you can bring clarity to an otherwise confusing system architecture.