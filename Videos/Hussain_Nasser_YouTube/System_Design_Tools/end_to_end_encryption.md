# End to end encryption

1. Consider an example: Chat application uses a centralized server. Parties connect themselves to this server for chatting. We can have two TLS encrypted-connections between Alice & Server and Bob & Server. Bob sends a message to the server through it's encrypted connection. Server decrypts and realizes it's for Alice. It re-encrypts it and sends it to Alice who decrypts it.


## True end-to-end
1. In true end-to-end encryption, encryption occurs at the device level. That is, messages and files are encrypted before they leave the phone or computer and isn’t decrypted until it reaches its destination. As a result, hackers cannot access data on the server because they do not have the private keys to decrypt the data. Instead, secret keys are stored with the individual user on their device which makes it much harder to access an individual’s data.

2. Let’s say Alice and Bob create accounts on the system. The end-to-end encrypted system provides each with a public-private key pair, whereby their public keys are stored on the server and their private keys are stored on their device.




3. Therefore, Whatsapp has fingerprinting. Go next to your friend and verify that fingerprint by scanning your friend's phone. Govts are trying to stop this if intercept communication between terrorists. Certificate Authoritys if they get hacked, you can have their private key and can issue certificates on their behalf.