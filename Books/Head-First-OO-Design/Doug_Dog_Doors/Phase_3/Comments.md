1. We store the Bark to be recognized against the owner's dog and compare it with the actual bark in this phase.
2. We use a new Bark class to do just that. 
3. By delegating the comparison of barks to Bark object, we abstract the details of what makes two bark objects equal away from the BarkRecognizer class. So our object are shielded from implementation changes in other objects.
