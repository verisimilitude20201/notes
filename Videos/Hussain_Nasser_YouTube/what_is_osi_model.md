Video: https://www.youtube.com/watch?v=7IS7gigunyI&list=PLQnljOFTspQUNnO4p00ua_C5mKTfldiYT&index=3

# What is OSI model

1. OSI is a model that standardized communication in computer systems. Everything that communicates over the Internet and is connected to internet uses this model

2. Stands for open systems interconnection

3. Consider below setting

    Mac Address  IP Address
    Host A       10.0.0.2
    Host B       10.0.0.3
    Host C       10.0.0.4
    Host D       10.0.0.5


    Each of these are connected to a router A 10.0.0.1

    What we want to do is use 10.0.0.5 and access an GET / index.html page hosted on 10.0.0.3:80

4. Layer 7 Application Layer: GET / index.html on 10.0.0.5 makes an HTTP request contain HTTP headers (cookies and may be a body if its a PUT/GET request)


5. Layer 6 Presentation: This layer handles encryption. Since we don't have encryption, it will just pass this over.

6. Layer 5 Session: The HTTP request is tagged with a Session ID. Stateful session

7. Layer 4 Transport layer: This breaks the request into smaller manageable segments. Each segment is tagged with destination port numbers and sequence numbers. Source port is auto-generated. Transport layer at the destination takes care of assembling the data in sequence.

8. Layer 3 Network: This attaches the source and destination IP to the segments. These segments are now called packets. 

9. Layer 2 Data link layer: Takes packets and breaks them down into frames. It adds Mac addresses to each frames. They have checksum based error detection. If we don't know the Mac address, the ARP protocol comes into play here which converts the IP address to Mac address

10. Layer 1 Physical Layer: Physical layer converts the digital 0s and 1s to electrical signals, Wifi Signals and light signals. Once it's converted, the buck passes back again to Layer 2. Everyone receives these frames and Network card running on these hosts and checks the destination address. If it's not intended for them, it drops the frame. Only the intended recipient receives it.

11. On the destination machine, the OSI layers are applied in the reverse order and information assembled and decrypted. And the destination machine now sends index.html