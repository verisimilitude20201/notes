1. We removed all String constants and uses enums in place of them.
2. Enums provide type and value safety. You can't misstype an enum without getting a compiler error. You can also avoid getting bad data for anything that has a standard range or set of legal values. 
3. We have made the code less fragile i.e. more robust.
4. With this, we have made the Guitar application workable which was the step 1 of writing great code.
5. Now, if the client does not provide a serial number or price since they are unique to each guitar but just provides the type, builder and the frontwood and backwood. The client can do this, he can specify only general properties of a guitar without it's serial number or price.