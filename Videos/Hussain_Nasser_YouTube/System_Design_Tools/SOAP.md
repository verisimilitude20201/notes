# Soap 

1. In the 90s, people were writing software and there was no concept of network calls to call into other apps. 
2. Then came Remote Procedure calls to call procedures on remote machines
3. W3C came and proposed a standardization using XML with its own schema for procedure calls. It has it's own schema. Thus came SOAP.
4. It can be used with HTTP, TCP, SMTP anything. Just write your own SOAP server and SOAP client

Advantages:
1. Schema: Discard invalid messages
2. Extensible: XML can invent your own tags. Only SOAP server needs to understand the new tags.
3. Flexible Transport