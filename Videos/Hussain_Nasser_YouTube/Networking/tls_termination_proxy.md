Video: 

# TLS Termination Proxy


## TLS 1.2 
- Client sends a random stream of bytes called Client Hello. It informs the server the asymmetric and symmetric ciphers that it supports.
- Server sends a public key + certificate
- Client generates a pre-master secret key and encrypts it using the public key of the server and sends it accross to the server.
- Server decrypts the symmetric key 
- After the mutual key exchange happens,  the server client use the pre-master key for all their communication.

## TLS Termination Proxy
1. Proxy that terminates the TLS and sends unencrypted traffic to the main server.
2. Client and proxy do the TLS communication and encrypt the traffic between them.
3. Benefits of decrypting is to make better decisions on the data by the proxy to do the beautiful stuff that a proxy does (load balancing, routing, caching.)
4. Proxy transfers the unencrypted traffic to the back-end server.

## TLS Termination Proxy Pros
1. Offloads the crypto to proxy. TLS 1.2 has 4 handshakes and TLS 1.3 has 2 handshakes. The initial handshakes are compute intensive but after that it's all usual stuff.
2. TLS close to the client.If you use a Google.com proxy in India to visit a Google server in US, the TLS handshaking happens closer to the client and is faster than it would be if a client in India accesses the google server in USA.
3. HTTP Accelerator can be taken advantages of.
4. Intrustion detection systems can be used to sniff the data to detect attacks such as DoS
5. Load balancing at layer 7/Service Mesh

## TLS Termination Proxy Cons
1. Limited number of max connections of proxy. Can quickly run out of TCP connections. Layer 4 is one single TCP connections between client - layer4 - back-end server. We can have multiple layer-4/layer 7 load balancers, add a DNS load-balancer, add multiple entries to the DNS to funnel requests to the backend
2. If compromised, all data is available.

## TLS Forward proxy
1. The only difference between this TLS termination proxy is it re-encrypts another TLS session between the actual back-end server and proxy.
2. Two encrypted TCP connections
3. May be fast because symmetric key may be used.
4. Used if you want to have more security such that you are hosting your services on a cloud provider that you do not trust.