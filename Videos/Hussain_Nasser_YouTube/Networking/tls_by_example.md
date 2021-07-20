# TLS
1. Secure communication in the client and server.
2. Used in HTTPS

# HTTPS
1. Exactly same as HTTP only the port is different 443
2. Handshake: Goal is to agree on a symmetric key used to encrypt and decrypt the data in transit. 
3. Client locks its GET request using a key i.e scrambles (does on layer 7 OSI), TCP blindly transfers it to the server
4. Server decrypts using the symmetric key and processes that request. Encrypt the response using the same key and send it back
5. Client decrypts and presents the response.


# TLS 1.2
1. Used recently, SSL was the past.
2. TLS was designed to be configurable with a lot of options 
3. The TLS Handshake:
	- Client sends a random stream of bytes called Client Hello. It informs the server the asymmetric and symmetric ciphers that it supports.
	- Server sends a public key + certificate
	- Client generates a pre-master secret key and encrypts it using the public key of the server and sends it accross to the server.
	- Server decrypts the symmetric key 
	- After the mutual key exchange happens,  the server client use the pre-master key for all their communication.
4. Problems with TLS 1.2
    - No Perfect forward secrecy (Unpatched OpenSSL were able to pull the private key of the server using HeartBleed)
    - It is slow


# Diffie Helman
1. RSA has a problem. A client encrypts the pre-master secret key using the server's public key. If someone got the private key of the server, he/she can intercept the messages intended for the server
2. The server and the client have two private keys (red - server, blue - client and pink both client and server. The client generates a pre-master secret key by combining the client's private keys (blue + pink) with the server's public key and send it across to the server.The server decrypts this using its own set of private keys (red, pink) and gets the pre-master secret key.

# TLS 1.3
1. No cert options. Only Diffie-Hellman algorithm for key exchange.
2. Client generates a blue key (private key) and pink key (public key). It merges the public + private key together and sends it to server.
3. Server generates a private (red key). It adds the client's private and public key to it and gets the symmetric key (golden key)
4. Server then takes the pink key and merges it with the red key and sends it back to client.
5. Now the client combines this with the blue key and generates the golden key
6. Golden key is the symmetric key.
7. Ephemeral is when the blue and red key are temporary i.e generated only for a session.