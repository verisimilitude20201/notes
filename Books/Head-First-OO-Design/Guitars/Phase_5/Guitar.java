public class Guitar extends Instrument{
    private String serialNumber, model;
    private GuitarSpec guitarSpec;
    

    public Guitar(String serialNumber, double price, GuitarSpec guitarSpec) {
        super(serialNumber, price, guitarSpec);
        // Initialize all private properties
        this.guitarSpec = guitarSpec;
    }

    // Additional Getter/Setters go in here.
}