Video: https://www.youtube.com/watch?v=d-Bfi5qywFo&list=PLQnljOFTspQVMeBmWI2AhxULWEeo7AaMC&index=16

# High Availaibility using Keep Alived

Keep alived is a routing software written in C that provides simple and robust facilities for load balancing and high availaibility in Linux systems

## Failover
1. Idea of clients having to point to single server and if it fails, the clients without knowing will redirect requests to another server. Servers sync up and agree amongst themselves for the virtual IP.
2. Servers agree on a virtual IP address and only one master can use that IP for now. 
3. Clients redirect the request to the VIP which will be serviced by the master

### KeepAlived Configuration.
Create a VRRP instance on one mode
    
        vrrp_instance pi1 {
            state MASTER/BACKUP
            interface eth0
            virtual_router_id 101
            priority: 100
            authentication {
                auth_type PASS
                auth_pass 1234
            }
            virtual_ipaddress {
                192.168.254.100
            }
        }