Video: https://www.youtube.com/watch?v=hmTl5Y4ee_Y&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=4(16:00)

# Building a non-blocking web server
1. Single process handling Web requests may block other clients from sending their requests and getting them processed.
2. This is an approach to fork (cloning or copying a process) off the main process into multiple child processes for processing requests. Main process is free, it accepts the requests and assigns it to it's children.