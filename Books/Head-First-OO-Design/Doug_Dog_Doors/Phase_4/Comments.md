1. Now there's a subtlety here. The focus should be on the dog and not the bark. In Phase 3, we compared the owner's just single type of bark. In reality, the owner's dog could bark in slightly different ways.
2. The allowedBarks should be an array of Barks
3. We should pay attention to the nouns in our use-case. We only need a class for the items we want to represent.
4. Textual analysis is figuring out the nouns and verbs in your use-case for classes and methods. It tells you what to focus on not just what classes we should create. In this example, it helps us get the dog in and out of the door regardless of how he barks. Dog may not bark the same way so we may need to store all the ways in which a dog barks. 
5. Pay attention to the nouns in your use case even when they are'nt classes in your system. Think about how the classes you do have can support the behavior your use case describes.
6. Verbs in the use-case describe methods in the system
7. Dog class was not created because
    - Dog is external to the system, we don't represent things external to the system unless we need to explicitely track their state.
    - Dog is'nt a software object, we don't represent living things unless we need to store long-term informaton about that thing.
    - Even if you had a Dog class, it would'nt help the rest of the system. 
