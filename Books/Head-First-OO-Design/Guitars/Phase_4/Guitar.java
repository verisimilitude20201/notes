public class Guitar {
    private String serialNumber, model;
    private GuitarSpec guitarSpec;
    

    public Guitar(String serialNumber, double price, GuitarSpec guitarSpec) {
        // Initialize all private properties
        this.guitarSpec = guitarSpec;
    }

    // Additional Getter/Setters go in here.
}