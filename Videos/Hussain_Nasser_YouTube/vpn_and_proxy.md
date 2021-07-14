Video: https://www.youtube.com/watch?v=npnqyRT77Zc(7:00)

# VPN and Proxy

1. Both VPN and proxies make requests on behalf of a client.

## How does VPN work?

1. Typical network setup: 
    - Network A: Router with a public IP and private IP. That also is a DHCP that gives you public IP
    - Network B: Also an independent network having it's own setup of private and public IP addresses.
    - Router transfers a packet to google.com via it's public interface. Google knows the public IP address.
    - A host within network A cannot access host within network B.

2. VPN literally makes you a part of private network. VPN uses layer 2 tunneling protocol (L2TP), IPsec, Internet Key Exchange Protocol(IKEv2). 
3. Once a VPN tunnel is established, the host becomes a part of the network. If the protocol supports encryption, each packet exchanged between the two hosts in two different networks is encrypted. 
4. The router simply forwards the packet to the VPN tunnel.