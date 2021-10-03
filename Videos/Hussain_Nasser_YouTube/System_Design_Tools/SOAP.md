# Soap 

1. In the 90s, people were writing software and there was no concept of network calls to call into other apps. 
2. Then came Remote Procedure calls to call procedures on remote machines
3. W3C came and proposed a standardization using XML with its own schema for procedure calls. It has it's own schema. Thus came SOAP.
4. It can be used with HTTP, TCP, SMTP anything. Just write your own SOAP server and SOAP client

Advantages:
1. Schema: Discard invalid messages
2. Extensible: XML can invent your own tags. Only SOAP server needs to understand the new tags.
3. Flexible Transport

Disadvantes:
1. Rigid Schema can be problematic at times it can slows downs adoption. REST is a more open architecture.
2. Choice of XML: Very large bandwidth sucking language. XML Parsing can be slow.
3. Thick SOAP clients that take care of serialization and deserialization. Not at all language agnostic and a lot of work is needed to make it agnostic
4. Scaling is harder. Addition of a new field tag needs equivalent changes at client and server end. No independent scaling of client and server