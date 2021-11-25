Video: https://www.youtube.com/watch?v=T0k-3Ze4NLo&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=38(40:00)

More Refs: https://jwt.io/introduction

# JSON Web Tokens


1. JWT or JSON web token is a standard that for creation of access tokens that assert some number of claims in the token itself thereby making it the interaction stateless.


## Session based Auth
1. Database stores username and hashed password and application does the actual credential verificiation.
2. After the successful authentication, it generates a big session ID and stores as much meta-data with the user as possible - userinfo, session expiry, any other user preferences. 
3. Server then returns the sessionId to the client and expects it to send it each time the user's requests should be identified.
4. Session ID can be sent via Set-Cookie header, query parameter and so on.
5. Here, the application is stateless but the database is having state.
6. Con for this is an extra latency to verify. If you maintain auth information in the application server in a local cache, application becomes stateful. Moreover, if the state gets cleaned but local cache does not, it will again cause inconsistency

## JWT Based Auth
1. Completely stateless. If we take a JWT and gives it to a completely different service with no access to the database, it will be able to authenticate the it.
2. 3 Parts: Header, Data and Signature.
3. Signature encryption can be symmetric or asymmetric.
4. Asymmetric private key signs JWT, public key validates
5. Symmetric its just the same key.


## JWT Structure

### Header
{
    "type": "JWT",
    "alg": "HS256"
}

### Payload
{
    "name: "Edmond Dantes",
    "role": "Admin",
    "eat": 1432423 
}

### Signature
HMACSHA256 (
    Base64_encode(header) + "." + base_64(payload) + randomvalue + secret
)

### How does it work?
1. Instead of generating a Session ID, the server generates a JWT token with the above structure and sends it to the client. 
2. On every request, the client responds with the JWT token which the server then verifies. The process is truely stateless and as long as the server knows how to verify the token, you can send it to any server at the back-end.
3. The con here is once this token is stolen, we cannot do anything about it. It does'nt expire. Everybody can use this to use your service.

## Refresh Tokens
1. Short-lived JWT tokens. We can't force users to login every 15 minutes except for financial institutions.
2. Need a way to get JWT tokens every 15 minutes. They behave similar to sessions.
3. Here we generate two tokens - refresh token which is long-lived and access token. We return both to user. Access token is used to authenticate the request. We store the refresh token.
4. It's the client's responsibility to generate a new access token on expiry. Client calls an endpoint /token and sends it the access token. 

## Asymmetric JWT
1. No one uses symmetric encryption because it's the same shared key for all microservices. 
2. Authentication service has the only private key. Public key can be used to authenticate.
3. Each microservice has it's own public key which it uses to verify the JWT.
4. With JWT, the idea of log out does'nt exist, since refresh token is still there even if the access token is gone.


## Pros and Cons

### Pros
1. Stateless: Throw this token to any service if it has access to the public key, it can verify.
2. Great for APIs. Talk to a central authentication service to get a token. As long as any service can provide an API, we can access an API.
3. It's very secure because we encrypt it in the token itself.
4. Carrys a lot of useful information. Session based auth, we can just give the session ID.
5. Can store info that drives UX
6. No need for centralized database.


### Cons
1. Sharing secrets in microservices architecture. We can use a centralized key management service like Vault to store secret keys. 
2. Very tricky to consume correctly.