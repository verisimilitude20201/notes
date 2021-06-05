Video: https://www.youtube.com/watch?v=dh406O2v_1c&list=PLQnljOFTspQUNnO4p00ua_C5mKTfldiYT&index=19 

# What happens when we type google.com in the browser


## Assumptions
 - All latest version browsers as of 2019
 - Freshly installed chrome on which google.com is never ever visited


## Initial typing:
  - Browser starts looking in the history with the letter that you've typed. Some browsers may even look a locally cached index for this to display a list of websites that you visited with the starting letters that you type.
  - Some browsers may even visit the default search engine set by pass it the search term character-by-character

## URL Parsing:
  - google.com is finished typing and hit enter.
  - Browser starts parsing this if this is URL or a search term.
  - If it's a URL, the browser sends a request to that URL and tries to fetch it.
  - Browser tries to figure out the protocol. By default older browsers used to use plain, unencrypted HTTP. However, HTTP has its own drawbacks of man-in-the-middle attacks and its insecure. 
  - Browsers came up with HTTP Strict-Transport-Security which is a local cache that maintains a list of websites that force https.
  - In this case, the protocol is HTTPS and we establish a secure communication first. Had google.com not been in a HSTS list, the browser would estalish an HTTP port 80 connection.

## DNS Lookup
   - We need to know the IP address of the google.com. 
   - Browser checks its local cache of the DNS to find the IP address of google.com
   - It then asks the Operating system to ask if it knows google.com (/etc/hosts Linux and hosts.txt  in Windows)
   - DNS over HTTPS (DoH): DNS is a UDP service using port 53. It's unencrypted. Any one can know the domains you're going. DNS requests are visible to your ISP. But they cannot know what you are searching unless they are using a TLS-terminating proxy. So there were two solutions DNS over HTTPS and DNS over TLS. We know HTTPS (it's a beautiful bidirectional streaming technology) so instead of inventing something new (DNS over TLS) why not use the existing one
   - You cannot differentiate between normal HTTPS web traffic and DNS over HTTPS requests
   - If the browser supports DoH, it will do that. It checks the default DoH provider (google/cloud flare) and establish the TLS connection there.
   - If DoH is disabled, the final step is DNS lookup 53, we send a UDP datagram, on the default DNS provider on the router provided by the ISP and we can even change it to one for Cloud-flare there.

## Example DNS UDP request 
   - Assume that client IP address is 10.0.0.1, Mac: aa, GW is 10.0.0.1, The router (Internal IP: 10.0.0.1, External IP: 44.1.2.24, Mac=ff) and the DNS is 1.1.1.1:53
   - The Source IP Packet that is constructed is aa | 3333 | 10.0.0.2 | DNS | 1.1.1.1 | 53 | ????
   - We need a frame encapsulating the Mac address of 1.1.1.1
   - This packet is sent to the gateway i.e the router (Internal IP: 10.0.0.1, External IP: 44.1.2.24, Mac=ff)
   - This router NATs it give it a public IP aa | 3333 | 44.1.2.24 | DNS | 1.1.1.1 | 53 | ??? and adds an entry to the NAT table
                     10.0.0.2 -> 3333
                     44.1.2.4 -> 3333
   - The router sends this modified packet to 1.1.1.1 on port 53. 
   - The DNS responds to the router and the router forwards the packet to the requesting machine containing the IP address of Google.com 4.1.2.3:443

## TCP connection to 4.1.2.3:443
   - TCP connection packet built by client IP address is 10.0.0.1
       aa | 2222 | 10.0.0.2 | TCP | 4.1.2.3 | 443 | ????
   - This is redirected to the Gateway and to the router and router sends the below packet
       aa | 2222 | 44.1.2.4 | TCP | 4.1.2.3 | 443 | ????
   - 3-Way handshake happened and we have a full TCP connection. We also have the MAC of the Google.com
     aa | 2222 | 44.1.2.4 | TCP | 4.1.2.3 | 443 | FFFF
   - What happens next depends on if we have a proxy (either HTTP/HTTPS/SOCKS proxy/TLS terminating proxy). If it's an HTTP proxy, the destination IP will be that of the proxy.
   - If we are using HTTP 1.1, it performs 5 different connections corresponding to the number of resources to be downloaded on a web page.

## TLS, ALPN, SNI: 
   - After TCP, we establish a TLS 1.3 connection
   - This is done using Diffie-Hellman key exchange to generate a symmetric key.
   - Client merges the private and public key and sends in a client Hello and sends the ciphers it supports. Client Hello uses ALPN to provide which protocols it supports HTTP/1.1, 1.2, 1.3, QUIC, SPDY
   - ALPN (Application Layer Protocol Negotiation) is a TLS extension that allows application layer to negotiate when protocol should be performed over a secure connection that avoids additional round trips. 
   - Server name indication is a TLS extension by which client indicates which hostname it's attempting to connect. This aids in serving multiple secure https websites over same IP address without requiring all those sites to use the same certificate.
   - Server generates a private key/public key pair and generates a gold key by merging client's private + public key pair along with it's private key. Server sends a combination of private + public client key, it's public key to client. The client merges it to create a gold key
   - The client and server now have the gold symmetric key and they can start the communication.

## GET / First HTTP Request
   - Send the first request; with the required headers
   - We use HTTP/2 using streams so we build the required stream, we compress it.
   - We encrypt the compressed stream using the shared key
   - We go through the same gateway router to the Google server
   - Server decrypts and decompresses the stream and provides the response a simple index.html over the same connection compressed and encrypted using the session key
   - Client decrypts and decompresses the response and the browser parses the HTML to display it. It checks the type of content


## HTML Parsing
   - Depending on the content type, it processes content accordingly. HTML is parsed, image is rendered, text is displayed as-is, Javascript is executed.
   - MIME sniffing: Browsers if they too content-type, they try to infer the body and tries to parse to infer the data type
   - Any additional resources are fetched via the same TCP/HTTP connection and it gets sent in parallel.
   - HTTP/2 with Push: The server while sending index.html also sends the files referenced in index.html, so that browser does not request those again.
   - HTTP/1.1: Different connections for different connections.