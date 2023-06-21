1. Firstly, we make the InstrumentSpec non-abstract. We can have a single search() method. We can let clients pass an InstrumentSpec object to this search() method in InstrumentSpec class.
2. The main reason we create a sub-class is because the behavior of the sub-class is different from that of the super-class. Here the Instrument class represents a concept so it should be abstract. So we should have sub-classes for each specific instrument type. Each different type of instruments has different properties and uses different sub-class of InstrumemtSpec.
3. Below are OO principles and their usage in this Guitars application uptil now
    - Inheritance: Already used with Instrument and InstrumentSpec classes. But sub-classes don't do anything apart from inheriting from Instrument. They just have slightly different constructors. 
    - Polymorphism: We used Polymorphism successfully in the modifed search method in this phase but it can also be used while coding the addInstrument method in the Inventory sub-class.
    - Abstraction: InstrumentSpec abstracts specific details about each specific instrument spec so we can add new properties without affecting basic Instrument class. 
    - Encapsulation: Encapsulate what varies which we already done when we split our Instrument and InstrumentSpec sub-classes. Is it possible to encapsulate completely ?
