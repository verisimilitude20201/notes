Video: https://www.youtube.com/watch?v=VgI9P-KneYo&list=PLQnljOFTspQVMeBmWI2AhxULWEeo7AaMC&index=22 

# Envoy Proxy Fixes Two Zero Day vulnerabilities (UDP Proxy, TCP Proxy) - CVE-2020-35470

1. Envoy UPD proxy crashes if the datagram size is greater than 1500 MTU or if fragmented datagrams are forwarded and reassembled to a size of 1500 MTU or more
2. If a client (C) communicates with a TCP proxy HAproxy (H) set as a forward proxy which further contacts Envoy(E) reverse proxy hiding 3 MySQL databases. The TCP HAproxy Proxy adds the X-FORWARDED-TO HTTP header. It gets incorrectly set to the  IP address of the HAProxy whereas it should be set as the IP address of C.