1. We now need to add support for Mandolins along with Guitars.
2. We create an abstract base class called Instrument that share common properties between Mandolins and Guitars. An abstract class defines some basic behavior but it's the sub-classes of abstract class that add the implementation of those behaviors. 
3. Just as a GuitarSpec class, we need a MandolinSpec class. We create an InstrumentSpec abstract class containing the common properties of MandolinSpec and GuitarSpec.
4. Guitar and Mandolin class simply have a constructor
5. The addInstrument() method in Inventory.java becomes a pain. For each new Instrument added, we may add a new block to that method
6. Instead of having a single search() method for searching instruments, we have two different search methods that search guitars and mandolins. 
7. The points 5, 6 & 7 indicate a design problem
8. Every time we add a new instrument we new another subclass of Instrument and InstrumentSpec and we need to add a new search method. 
9. We need the below OO principles to change this
    - Encapsulate what varies
    - Code to an interface rather than an implementation
    - Each class in the application should only have one reason to change.