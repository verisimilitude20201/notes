# Encoding and Evolution

## Introduction
1. New features are added/modified, applications change over time. 
2. A change to an application's features also initiates a change to the data it stores i.e. new fields get added, the data type changes, new fields get added and so on.
3. Different data models have different ways of coping with such changes. RDBMS conform to one schema although that schema can be changed later through schema migration. 
4. Schema-on-read databases don't enforce a schema and database at times can be a mixture of old and new schemas.
5. For data format, schema changes, application code needs to change. With server side applications, you may perform a rolling upgrade, with old and new schema versions/data formats both supported at the same time, till they work properly and we gradually move to the new data formats.
6. This means old and new formats/schema tend to coexist for some time on the system and this means we need both back-ward and forward compatibility.
    - Backward compatibility: New code can work with old data. This is easy to accomplish since as the developer you know the format of the older data and can write new code to work along with that. 
    - Forward compatibility: Old code can work with new data. This is a bit tricky. It requires older code to ignore the newer additions to the data added by newer code.

## Formats for encoding data
1. Programs work with data in at-least two representations.
    - In-memory using lists, trees, hashmaps which is optimized to be efficiently accessed by CPU.
    - While sending data over a network, we have to encode it in some kind of self-contained sequence of bytes. This sequence-of-bytes representation looks quite different from the data structures used in memory.
2. The translation from the in-memory sequence to a byte sequence is called encoding (serialization/marshalling) and the reverse is called decoding (deserialization, unmarshalling)
3. Many different encoding format options

### Built-in programming languages
1. Different programming languages have built-in support for encoding in-memory objects into byte sequences. Java has java.io.Serializable, Ruby has Marshal, Python has Pickle.
2. Though they allow in-memory structures to be encoded and decoded with minimum code, they do have additional problems. It's generally a bad idea to use your language's built-in encoding
    - Encoding/Decoding restricts to a particular programming language. This may prevent integrating your system with those of other organizations
    - Decoding process needs to be able to instantiate arbitrary classes which may be a source of security problems. 
    - Versioning is an afterthought
    - Efficiency i.e CPU time to encode/decode and the size of the encoded structure is also an afterthought.

### JSON, XML and Binary variants
1. JSON/XML are widely, supported, standardized and can be read/written by many programming languages. 
2. XML is critisized for being too verbose and complicated. JSON is popular primarily due to it's builtin support in browsers and simplicity relative to XML. 
3. JSON/XML have a few problems 
    - They both support Unicode encoded strings but they don't support binary strings (sequence of bytes without an encoding). We could Base64 encode but that increases the data size by 33%. 
    -  Lots of ambiguity on encoding of numbers. JSON distinguishes between strings and numbers but no distinction between floating points and integers and it does'nt specify a precision. This becomes a problem to represent numbers larger than 2^53 in a IEEE 754 double precision format. 
    - Optional schema support for XML/JSON. Schema languages are powerful but are complicated to learn and implement. Applications that don't use schemas tend to hard-code the interpretation of a value in XML/JSON in the application itself.
    - As long as people aggree on what the data interchange format is, it often doesn't matter how pretty or efficient the format is. 
4. Binary encoding:
    - For internal data within an organization, one can use a lowest common denominator format. For a small dataset, the gains can be negligible but once you get to the TB range, choice of data format does have an impact. 
    - JSON is simpler than XML both both of them in their textual form occupy a lot of space. Lot of binary encodings for JSON have been developed as a result viz. BSON, BJSON, BISON, MessagePack. 
    - Since they don't prescribe a schema, they need to include the object field names within the encoded data. Some of them extend the datatypes by distinguishing between integers and floats / binary strings. 
    - For binary encodings of JSON, it's not clear whether such a small space reduction (speedup in parsing) is worth the loss of human readability

### Apache Thrift and Protocol buffers
1. Apache Thrift (originally by Facebook) and Google protocol buffers are libraries based on the same principle.
2. Both require a schema that's encoded. For example: Below for Google Protocol buffers
```
message Person {
    required string user_name = 1;
    optional int64 favorite_number  = 2;
    repeated string interests
}
```
3. They come with a schema generation tool that produces classes that implement the schema in various programming languges. You can use this code to encode or decoded records of the schema.
4. Thrift has two binary encoding formats viz. Binary Protocol and Compact Protocol. Binary Protocol 
    - Each field has a type annotation to indicate whether it's a string, integer, list. 
    - It also has a length indication (length of a string, items in a list)
    - Strings appearing in the data are encoded in UTF-8
    - Instead of field names, the encoded data contains field tags which are aliases for fields. 
5. Thrift compact protocol is semantically equivalent to this. It packs the type and field tag into a single byte and uses variable length integers. 
6. Protocol buffers is similar to Thrift although it encodes the same data differently. 
7. Required/Optional are check to indicate whether it's compulsory for the required field to have a value. They do not influence the binary encoding.

### Field tags and schema evolution
1. Schema evolution is when schemas change over time. 
2. For thrift and Protobufs, an encoded record is just a concatenation of its encoded fields with tag numbers identifying fields. Fields are annotated with data types. 

#### Forward & Backward compatiblity for added fields
1. Field tag numbers are critical in field identification, because they are used in encoded records and changing tag numbers would make all data invalid. You can change the field name / data type (viz making in32 int64).
2. New fields can be added to the schema provided you provide each field a new tag number. 
3. Old code reading new encoded records containing the new field will simply ignore that field. The datatype annotation allows the parser to determine how many bytes to skip. This maintains forward compatibility
4. As long as each field has a unique tag number, new code can always read old data because tag numbers have same meaning. The only thing is if you add a new field, you cannot make it required since old code would'nt be able to read this new encoded data and forward compatibility will fail. 
5. Every field that you add after the initial deployment of the schema should be optional or have a default value.

#### Forward & Backward compatiblity for deleted fields
1. You can only remove a field if it's optional.
2. You can never use the same tag number again because you may have data written somewhere that includes the old tag number and that field must be ignored by the new code.

### Changing the datatypes and schema evolution
1. Changing datatypes is possible but there is a risk involved that values may lose precision or get truncated. 
2. New code can read data written by old code because the parser can easily fill missing bits with 0s. If old code reads data written by new code, if in old code the integer datatype is changed to int64 and in old code if its int32 since the decoded data won't fit into 32 bits, it will get truncated.
3. Protobufs don't have an array or a list type. Instead, fields marked repeated will be repeated 0 or more times.For old code, reading new data of repeated type will see the last element of this list.
4. Thrift has a dedicated list datatype parameterized with the datatype of list elements. It can support nested lists. 


### Apache Avro
1. Started out of use in Hadoop because Thrift was found not suitable for Hadoop usecases.
2. It also has it's own schema, two schemas actually - Avro IDL which is meant for human readability and second based on JSON that's machine readable. Example Person schema
```
record Person {
    string userName;
    union {null, long} favoriteNumer = null;
    array<string> interests;
}
```
3. Avro schema does not have any tag numbers. It's binary encoding is 32 bytes, most compact. 
4. Avro encoding has nothing to identify the fields or their datatypes. It's just a simple byte sequence. A string has a length prefix followed by it's bytes. An integer is encoded using variable length encoding
5. To parse, you need the schema to go through the byte sequence in order of the fields declared in schema. This means you can decode the encoding provided the code is using the same schema that was used to encode the data.

#### The Avro reader and writer schema and schema evolution
1. Writers schema: With Avro, the application can encode the data using whatever version of the schema it knows about. This is known as the Writer's schema
2. Reader's schema: When application wants to decoded some data, it's exepcting the data to be in some schema. This is the reader's schema. 
3. The key difference with Avro is reader's and writer's schema don't have to be same, they only should be compatible. During reading, Avro library resolves the differences between the two schemas and translates the data from the writer's schema into the reader's schema.
4. Schema resolution matches the fields by name. If a field appears in the writer's schema but not in the reader's schema it is ignored. If a code reading the data expects a field in the reader's schema but it's not there in writer schema, it is filled with default values. 
5. Forward compatibility: You can have the new version of the schema as writer, and old version as reader. 
6. Backward compatibility: You can have the new version of the schema as the reader and old one as the writer. 
7. To maintain compatibility, you can only add/remove a field that has a default value. When a new reader reads in a record written with a old version of the writer schema, the newly added field is filled with it's default value.
8. If you remove a field with no default value, old readers cannot read data written by new writers so you break forward compatiblity. If you add a field with no default value, new readers cannot read data written by old writers and you break forward compatiblity.
9. In Avro if you want a field to have value null, you declare it as a union type. union {null, string} means this can be either null or a string.
10. Avro has union and default values instead of optional and repeated. 
11. Changing data type of a field is only if Avro can convert the type. 
12. Changing the name of the field is possible too. Since reader's schema contains aliases for field names, it can match old writer's schema field names against the aliases. So changing a field name is backward and not forward compatible.

```
Backward Compatibility --> New version of reader schema  <==> Old version of writer schema
Forward Comatibility --> Old version of reader schema <==> New version as writer.
```
#### How does the reader know the writer's schema? 
This depends on the context in which Avro is used
1. Large file with lots of records: If we use Avro in Hadoop context, for storing large files with millions of records all encoded with the same schema, we can include the writer's schema once at the beginning of the file. Avro specifes a file container format for that. 
2. Database with individually written records: Different records may be written to a database having their own versions of schemas and we can included a version number in each encoded record. Reader can use that version, extract the schema and use it.
3. For two processes connecting over a bidirectional network communication, they can negotiate schema on connection startup and then use that schema. 


#### Dynamically generated Schemas
1. An advantage of Avro's approach compared to Protocol buffers and Thrift is schema does not contain any tag numbers. This difference makes it friendlier to dynamically generated schemas.
2. If we use Avro to export a relational database containing data to a file, we can fairly easily generate an Avro schema from the relational schema and dump the database contents to an Avro object container file. If the database changes, a modified schema can be easily generated. The export process does'nt pay attention to the schema change - it can simply do the schema conversion each time it runs. 
3. Since the fields are identified by name, the updated writer's schema can still be matched up with the old reader's schema. With Protobufs and Thrift, the field tags would have to be manually assigned. 
4. Dynamically generated schemas was'nt a design goal for Protocol buffers. 

#### Code generation and dynamically typed languages.
1. For Thrift and Protobufs, once a schema is generated, you can generate code that implements this schema in a language of your choice. This is helpful for statically typed languages - C, C++ and Java because it allows efficient in-memory structures to be used for decoded data. 
2. For interpreted languages, code generation is not required, since an explicit compilation step is often avoided. For dynamically generated schema, code generation is an unnecessary obstacle to getting to the data.
3. Avro provides optional code generation for statically typed languages. 
4. An Avro object container file can simply be inspected in an object container file and can be inspected after opening in the Avro library. Like the way you open a JSON file and read it. The file is self-describing

### The Merits of Schemas
1. Avro, Protocol buffers, Thrift are schema-based binary encoding schemes whose schemas are much simpler than XML Schema or JSON schema and support detailed validation rules. 
2. These encodings have a lot in common with ASN.1, a language proposed in the 80s for defining network protocols and it's binary encodings. It supports schema evolution using tag numbers similar to Protocol buffers. It's badly documented.
3. Databases too consist of propreitary network protocols over which you can send queries to database and get results back. Each DB vendor provides drivers (ODBC/JDBC) that decodes responses from the network protocol in to in-memory programming structures. 
4. Binary encodings have many good properties
    - More compact than binary JSON variants because they can omit field names from encoded data.
    - Schema is a valuable form of documentation and since it's required for decoding, you can be sure it's upto date. 
    - DB of schemas allows to check forward and backward compatibility
    - The ability to generate code from the schema is useful since it enables type-checing at compile time for statically typed languages.
5. Schema evolution offers the same kind of flexibility as schemaless/schema on read JSON databases provide while also providing better guarantees about data and better tooling.

## Modes of dataflow
1. To send data to another process, with which you don't share memory, you encode it as a sequence of bytes and then may be send over a network or write it to a file.
2. Forward and backward compatibility aid in evolvability allowing to upgrade different parts of the application independently. 
3. Many ways in which data flows between processes. 

### Dataflow through databases
1. The process that writes data to the database encodes it and process that decodes data decodes it.
2. It's common for several different processes to be accessing the database at the same time. 
3. It's likely that some processes might be running newer code or some might be running older code because of a rolling upgrade. 
4. We would need both backward and forward compatibility for a database.
5. We can have a case where in an older version of a code not knowing about a newly added field by newer code updates the record and writes it back. In this situation, the old code should keep the new field intact. Encoding formats like Protocol Buffers, Avro, Thrift take care of this situation but sometimes, as an application developer, you need to be aware of this and take care at application level.

#### Different values written at different times
1. Data outlives code. While some values may be written 5 years ago, some are written milliseconds ago. The code is replaced in every release with newer versions of the code.
2. Most relational databases allow simple schema changes such as adding a column with a null default value. When an old row is read, the database fills in null for any columns that are missing from the encoded data on disk.
3. Schema evolution thus allows the entire database to appear as if it has been encoded with a single schema, even though underlying storage may contain records encoded with various historical versions of the schema.

#### Archival storage 
1. We take a snapshot of the database from time to time for backup purposes or for loading into a data warehouse. 
2. The data dump will then be encoded with the latest schema even though original encoding in the source database contains a mixture of schema versions from different databases. 
3. Avro container file format is a good fit. 
4. Good oppoportunity to encode the data in an analytics-friendly column oriented format.

### Dataflow through Services
1. For processes needing to communicate over a network, there are different ways to effect that communication. 
2. Most common way is to arrange them as clients and servers. The servers expose an API over the network and the clients can connect to the servers to make requests to that API. The API exposed by the server is called a service. The Web works in this way. 
3. The APIs in case of the Web consist of standardized sets of protocols and data formats. Because Website authors, web servers and web browsers agree on this, you can use a browser to access almost any website.
4. Native apps running on mobile devices and tablets can also make network requests to a server.
5. Client side JavaScript application can use XMLHTTPRequest to become an HTTP client (AJAX). The response is in data-format not displayable to humans but in an encoding that can be parsed and processed by the client-side application code.
6. A server can itself can be a client to another service. This approach is used to decompose a large application into smaller services by areas of functionality such that one service makes a request to another when it requires some functionality or data of other service. This way of building applications is called a service-oriented architecture and rebranded as microservices architecture.
7. If we compare services to databases, they allow clients to submit and query data. But, databases allow arbitrary queries and services expose an application-specific API that only allows inputs and outputs predetermined by the business logic. This provides a degree of encapsulation, services can impose fine-grained restrictions of what clients can and cannot do.
8. Microservices based architecture makes the application easier to change and maintain by making services independently deployable and evolvable. Each service can be owned by a team without having to coordinate with other teams. We can expect old and new versions of the service to be running together at the same time.

#### Web service
1. When HTTP is used as the underlying protocol for talking to the service, it's called a Web service.
2. Web services are not only used on the web but in several different contexts
    - Client application running on user's devices making requests to a service over HTTP. 
    - One service making a request to another service often located within the same data center as a part of a service oriented architecture. Software supporting this kind of use-case is sometimes called as middleware.
    - One service making requests to services owned by different organizations. This category includes public APIs, OAuth, credit card processing systems.