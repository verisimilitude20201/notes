Video:

# What Does "State Transfer" mean in REST

1. You go to a doctor for the first time, doctor understands and questions your case history and doctor documents the case, prescribes medicines and tests. We visit the doctor later on with your file, doctor recollects from the file the case history and may prescribe additional medicines. This is Stateful. Stateful model does'nt scale. If the doctor leaves, you need to start from scratch
2. In case you visit a new doctor, you need to transfer the knowledge about your case history to the new doctor for him to diagnose you. This is Stateless. Similarly, when a client approaches a server with a request, it attaches every information feasible in the request. We go from scratch each time and rebuild the server's state. It's a heavy request payload. 

3. An Example: Twitter
   a.  You authenticate to Twitter giving your username and password. Server recognizes you and fetches and presents your home-timeline and an interface to post a tweet.
   b. User Posts a tweet. He sends his session_id and the post which changes the user's state at the server's end.