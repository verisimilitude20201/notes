Video: 

# MITM Proxy
1. Free tool to capture HTTP(S) traffic used by security researchers.
2. Should set up HTTP and http proxy on the client pointing to the IP address of an already running MITM proxy server (forward proxy). All layer 7 HTTP(S) pass through the MITM proxy.
3. How it connects
  - Client sends a CONNECT. Proxy accepts the CONNECT.
  - Client establishes a TLS connection using SNI to specify the hostname believing it's talking to the actual remote host. 
  - MITM proxy connects to the remote server using TLS and uses the SNI hostname to do so.
  - Server forwards the matching cert with CN and SAN values. 
  - MITM continues the SSL handshake paused earlier and the client completes the handshake
  - This is a single end-to-end TLS encrypted TCP connection in a normal forward proxy case. 
4. MITM is a TLS terminating proxy it serves its own certificate by generating those. So it can see the user's data.
5. MITM proxy certificates are especially refused by the modern browsers. You will need to install the mitm proxy's certificate authority to trust it.