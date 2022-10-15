public class Mandolin extends Instrument{
    private String serialNumber, model;
    private MandolinSpec spec;
    

    public Guitar(String serialNumber, double price, MandolinSpec spec) {
        super(serialNumber, price, spec);
        // Initialize all private properties
        this.spec = spec;
    }

    // Additional Getter/Setters go in here.
}