Video: https://www.youtube.com/watch?v=996OiexHze0(43:47)

# OpenID and OAuth

1. Simplest authentication form - username and password, back-end validates the username and password, hashes password, verifies hash in DB, looks up user and authorization info and sets a cookie (Set-Cookie: sessionid=f00b4r;Max-Age=865000).
2. Homegrown way of implementing auth goes down with all security and maintenance issue of maintaining an authentication system. Need to be aware of when security industry best practises change for password hashing, storing user information.
3. OpenAuth and OpenID are industry best practises for solving these problems with authentication.
4. Common identity use cases (2010)
   - Form based basic authentication
   - SSO using SAML. Useful where you have one master account to use a set of services
   - Mobile app login (stay logged in when you close the app no right away solution)
   - Delegated authorization: How can I access my website without having to entering the password. Allow this app to access my Facebook account without allowing it to post something. This is Oauth


## Why Oauth 
1. No common way to solve the delegated authorization problem. The ways that were available very seemingly bad
2. Yelp was the first one attempt to solve. They had a feature on sign up to grab Gmail, MSN, YahooMail! AOL Mail contacts and send the invite to them to join. They used Gmail/MSN/Yahoomail/AOL passwords to login to those services and get the contacts.
3. Today, Banks and other finanacial info aggregators still use this old way that ask the user to enter his bank password.

## Delegated authorization with OAuth 2.0
1. The user trusts Gmail and he trusts Yelp kinda too. He just wants Yelp to access to his contacts.
2. Yelp has a button - Connect with Google. On click, the user is put into an Oauth flow and he gets redirected to https://account.google.com. You should login to your Google credentials.
3. After login, the user gets a prompt to access the Google public profile and contacts. 
4. If the user clicks yes, it gets redirected to a special callback - http://yelp.com/callback that calls the Google API URL and fetches contacts.


## OAuth 2.0 terminology (1.0 is deprecated)
1. Resource owner: User who owns the data the app wants to get to. In this case, I have some contacts in Google accounts.
2. Client: Application that wants access to this data
3. Authorization Server: Authorizes the data access in this case https://accounts.google.comt
4. Resource server: Has the data. in this case the Google contacts
5. Authorization Grant: The user has accepted sharing his Google contacts list with the client
6. Redirect URI: Where should the user end up after the authorization grant is accepted.
7. Access Token: Access token is received by the client on authorization grant and then the app can do what it wants to do

## Oauth 2.0 flow
1. Resource owner clicks on connect with Google on Yelp. 
2. On clicking, it gets redirected to the authorization server - Google's or Okta or Facebook's. Client passes some configuration information - redirect URI, what type of authorization grants we want. In this case we are requesting an authorization code.
3. Auth server logs in, redirects to the redirect URI and returns the authorization code.
4. On the callback page, the client passes the authorization code and gets the access token from the Auth server. We use this to have the best of both worlds of front channel and back channel. Some one can see the authorization code if we use just the auth code. the exchange auth code is sent via a POST request which even if intercepted is SSL-encrypted
5. Using this access token, the client has a read-only access to the resource owner's Google contacts


## More Oauth 2.0 terminology
1. Scopes: Authorization has a list of scopes that it understands - contacts read, contacts delete, email read, location history read. Client just wants the contacts read that is indicated by it.
2. Consent: Using the actual scope, the authorization server generates a screen of consent.Facebook recently added a feature to the consent popup to say if the app can post to your Facebook wall.
3. Back Channel (Highly secure communication channel): If we have a server code through which we can access a Google API. The network call is SSL-encrypted. This is a highly secure channel.
4. Front Channel (less secure channel): Browser since there are ways and means where something can be leaked. Not complete trust on the browser since it has developer tools which you can debug the Javascript running on the page. 


## Actual Steps - Yelp needs to authorize using Google's auth server

1. We create a client app on the Google Dashboard. We get client_id and client_secret which identifies the Yelp request to the auth server. Every request by any of Yelp's clients using this will send the same client_id
2. Connection with Google when clicked what goes --> http://accounts.google.com/o/oauth/v2/auth?client_id=123abc&redirect_uri=http://yelp.com/callback&scope=profile&response_type=code&state=footbar
3. User then inputs the username and password and then Google redirects to the configured callback in the Google app. http://example.com?auth_code=7567SSSvvccREweweeeQQQQQ
4. The user can then take this exchange_auth_code and client secret send a POST request to send the access token


## Client password credentials flow (Back-channel flow)
1. Client posts credentials to the auth server and he straight away wants a token
2. Machine-to-machine or service-to-service flow

## Resource owner password credentials flow (Back-channel)
1. Used to make older applications work correctly.

## Implict Flow (Front-Channel only)
1. Single page app.
2. Go to auth server and request access token instead of access code.
3. Less secure since exchange step is not happening and exchange step is exposed to the browser 

## OAuth cons
1. People started using Oauth for everything - single-sign-on, mobile app login, authorization and authentication. 
2. Oauth was not designed for authentication. 
3. It was for authorization.
4. Oauth does'nt have a standard way of accessing user information. Oauth deals with user scopes and Login with FB, Google buttons all added their own hacks to make authentication work under Oauth.
5. Why not add one additional layer on top of Oauth to add authentication?
6. So OpenID connect came into being as an extension to Oauth 2.0


## What OpenID Connect adds
1. ID Token
2. UserInfo Endpoint for getting user information
3. Standard set of scopes
4. Standard implementation
5. On the technical level we just add a new scope openid profile to get an authorization code and an ID token (actually a  JSON Web token - consists of a signature and headers containing user info)

## Practical examples

1. Web applications with server backend with a login form: Use Open ID connect with the Auth server being Google or Linkedin or Facebook and use a session cookie to keep track of the user. Back-end holds the id token till the user is logged in.

2. Native mobile app: Use the same Open ID connect with PKCE and store the ID token on the mobile. 

3. SPA app with API back-end: Use just front-channel based OAuth flow

4. SSO with 3rd PArty services: Okta acting as a bridge between several different authentication protocols