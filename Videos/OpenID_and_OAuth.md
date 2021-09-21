Video: https://www.youtube.com/watch?v=996OiexHze0(10:00)

# OpenID and OAuth

1. Simplest authentication form - username and password, back-end validates the username and password, hashes password, verifies hash in DB, looks up user and authorization info and sets a cookie (Set-Cookie: sessionid=f00b4r;Max-Age=865000).
2. Homegrown way of implementing auth goes down with all security and maintenance issue of maintaining an authentication system. Need to be aware of when security industry best practises change for password hashing, storing user information.
3. OpenAuth and OpenID are industry best practises for solving these problems with authentication.
4. Common identity use cases (2010)
   - Form based basic authentication
   - SSO using SAML. Useful where you have one master account to use a set of services
   - Mobile app login (stay logged in when you close the app no right away solution)
   - Delegated authoritzation: How can I access my website without having to entering the password. Allow this app to access my Facebook account without allowing it to post something.