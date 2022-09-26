1. We handle the Phase 0's short-comings, the Timer task closes the door after 5 seconds once opened. 
2. We don't need a close() function on the Remote.
3. There's still one more problem here. Fido may remain outside for more than 5 seconds and the door may shut. This code does'nt handle that.