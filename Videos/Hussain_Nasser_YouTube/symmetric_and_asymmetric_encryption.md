Video: https://www.youtube.com/watch?v=Z3FwixsBE94&list=PLQnljOFTspQUNnO4p00ua_C5mKTfldiYT&index=8

# Encryption

1. Encryption is the process of scrambling data to hide identity, secure communications and protect personal files


## Symmetric encryption
1. If you have something secured to keep, you use a safe with a lock. To open the safe, you supply the same key.
2. Symmetric encryption is the same, use the same key to encrypt and decrypt. 
3. The key scrambles the text into an unintelligible format that cannot be interpreted or linked back to the original text without the key. 
4. The key itself is generated through a cryptographically random generation process.
5. Examples: AES (Rijndael), DES, Serpent and Twofish
6. DES was brute-forced and broken.
7. AES is the most popular and most secure.
8. Mutually both the parties need to share the same key before the communication begins.
9. People can sniff the key while in transit and can go in malicious hands.

### Pros
1. Extremely efficient and fast.
2. Simple, reversible operation.
3. Efficient for large data.

### Cons
1. Hard to transport the shared key.


## Asymmetric encryption

1. We need one key for encryption (Public key) and one key for decryption (Private key). 
2. It's okay if the public key gets stolen because the text cannot be decrypted using the public key.
3. They are actually huge prime numbers. The prime numbers are huge, in the billions. The processing needs to be done by the computer is huge. Mostly employ power and mod on large primary numbers.
4. Absolutely expensive for the sheer amount of computation involved.
5. RSA (Rivest - Shamir - Adleman), Diffie-Hellman, ElGamal are some examples.
6. In networking, we initiate connection using asymmetric encryption and transfer a shared session key for the communication. When the shared session key is shared with both parties, that is used as the basis of encrypted communication between the two parties

### Pros
1. Public key can be shared or can even be stolen
2. Designed for small data

### Cons
1. Very, very slow. 
2. Inefficient for large data
