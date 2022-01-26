Video: https://www.youtube.com/watch?v=6THVzuUtwJs&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=70 (20:00)

# Tor Connection Establishment

1. Let's say we want to go to http://example.com via Tor browser which is an insecure. It tries to abstract using proxies trying to make it all unknown for the Tor client which uses the Socks proxy.
2. SOCKS proxy (Onion Proxy) talks the TOR language. It takes a pure IP packet. It receives a request to visit example.com on port 80. We first need a TCP connection to example.com. Tor Client never does a DNS request on it's own and gets IP address. Because it's a DNS request that goes through your ISP and ISP knows the domain you're visiting. This defeats the purpose of Tor.
3. Tor client connects to the Tor directory and asks for 3 Tor server hostnames - T1, T2, T3. It marks T1 as entry node, T2 as intermediate node, T3 as exit node. T1 knows T2 and T2 knows T1 and T3. T1 does'nt know T3.
4. Onion proxy creates a circuit creation request. We ask T1 to create a circuit C1. Establish TLS in the same C1.  Onion proxy has the secret TLS key between itself and the T1.
5. Now we want to extend our circuit to T2. So onion proxy requests that and T1 creates a new circuit C2 with a new shared secret key.
6. Now we ask T2 to extend it's circuit to T3 
7. Tor is a stateful protocol. Content is first encrypted using C1's key, then C2's key and C3's key. Data is first sent to T1 via C1 from the input. T1 decrypts and realizes it's for T1 via C2. 
8. Layer-by-layer encryption with each node peeling off the encryption when it receives the content.
9. The last node does'nt know who actually sent the request to go to example.com
10. Each and every Tor node has a domain name and a certificate. If a Tor node is shady, does'nt handle certificates properly, you can expect man-in-the-middle attack



