1. UDP & TCP is a layer 4 transport protocol

2. Each machine has an IP address in a network. Port distinguihes applications running on a machine. To connect to an application running on other machine, you need an IP address and a port number designating the application running on that machine listening to that port.

3. TCP Pros:
    - Acknowledgement. Unreliability using the Internet. We can loose this information due to any reason such as sharks biting underwater Internet cables, cosmic activity causing random noise in transmission. So an acknowledgement is sent from the server to the client saying I received your message
    - Guaranteed delivery: Retransmission in case of no acknowledgement after some time. Retransmission is always slow
    - Connection Based: Client server need to establish connection before communication. The connection is stateful and is maintained at both ends. If not connection based, there cannot be acknowledgement or guaranteed delivery.
    - Congestion Control: Control the amount of Internet traffic. So if there is lot of traffic, it will stop, wait. It will send data only when network can handle it.
    - Ordered Packets: Ensures that packets are received in an ordered manner by the receiver. 

4. TCP Cons
    - Larger Packets: Headers for guaranteed delivery, acknowledge. TCP adds extra headers for these features. 
    - More bandwidth: Larger packets means more bandwidth
    - Slower than UDP
    - Stateful: If we can restart our server and the client is connected and resume work without disturbing, then it's stateless.
    - Server memory: Server has to allocate memory for each connection and listen to every open connection to see if there is a message sent. Denial of service is using TCP's features  against it. It happens when the client opens up a TCP connection with a server and does not complete the TCP handshake. Do that a million times and there will be millions of half-open connections taking the server down.

5. User Datagram Protocol - The Cons
    - No acknowledgement. Send the data and don't care
    - No Guaranteed delivery. No retransmissions. UDP adds a quick checksum to a packet just for a service to determine whether the packet is good or bad
    - Connectionless: No physical connection between client or server
    - No congestion control, does'nt wait for the traffic to clear, it just drives. 
    - No ordered packets. No sequencing and numbering packets.
    - Security: If the port is open, everyone can send you malicious/good content on UDP port.  Therefore, many firewalls disable UDP to avoid data flooding

6. UDP Pros
    - Smaller packets no need to add extra headers.
    - Utilizes less bandwidth. Games may use UDP with the internal logic of the game handling some of the TCP features.
    - Faster than TCP, there is no waiting for packets, no acknowledgement
    - Stateless. Client can continue to send information after server restarts. 

7. Applications
    
    a. Video multimedia streaming - UDP. We can tolerate bad frames received

    b. Chatting App - TCP
    
    c. Database - TCP
    
    d. DNS - UDP
    
    e. UDP can be horizontally scaled powerfully because it's stateless