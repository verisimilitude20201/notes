Video: https://www.youtube.com/watch?v=RG97rvw1eUo&list=PLQnljOFTspQUNnO4p00ua_C5mKTfldiYT&index=11

# Network Address Translation

1. NAT is a process of mapping an IP-address to another IP address or (IP-address, port) pair to another (IP address, port) pair
2. NAT was designed to solve the limited number of IPv4 addresses (4 billion). With IoT devices connected to the Internet, IP addresses are scarce.
3. Consider the below setup.

    - Host A (Mac: AAA, IP: 192.168.1.2) wants to connect to Host B (Mac: CCC, IP: 192.168.1.4) on port 80. They have a router (Mac: DDD, IP: 192.168.1.1) connected between them that makes the set up in the same sub-net

    - Since they are in the same subnet, they can find each other's Mac addresses through address resolution protocol

4. Where does NAT come into play? 

   - Consider Host A (Mac: AAA, IP: 192.168.1.2) now wishes to send a request to a public host (Mac: FFF, IP: 44.12.1.9)

   - Router has two IP addresses 
      - Private Address (Mac: DDD, IP: 192.168.1.1)
      - Public address (Mac: DDD, IP: 44.11.5.7)

   - Host A cannot directly find (Mac: FFF, IP: 44.12.1.9) because it's in a different subnet. So this gets directed to the default gateway of Host A i.e the router. Host A looks up the router's MAC and forwards this packet to it.

   - Router changes the source IP address of the original packet to its own. Router persists a mapping  a mapping of (original_source_IP, router_ip, destination_ip:destination_port) and forwards the request to the desired server (Mac: FFF, IP: 44.12.1.9)

   - If we get a response from the website, we first send it to the router. Router looks up the earlier mapping and forwards it to Host A (Mac: AAA, IP: 192.168.1.2)

5. NAT is used for Port forwarding. For example: If any request is coming to port 80, change the port to 8081

6. L4 load balancing: It takes a packet that's coming into a server and creates a virtual IP. It is entered in the NAT table. This VIP can have 3 entries in the table. So if any request comes for this, we take it to either server1, server2 or server3