public class GuitarSpec extends InstrumentSpec {
    
    private int numStrings;

    public Guitar(String model, Builder builder,
    Type type, Wood frontWood, Wood backWood, int numStrings) {
        super(mode, builder, type, frontWood, backWood);
        this.numStrings = numStrings;
    }

    public boolean matches(InstrumentSpec otherSpec) {
        if(!super.matches(otherSpec)) {
            return false;
        }
        if(!otherSpec instanceof GuitarSpec) {
            return false;
        }
        Guitarspec spec  = (GuitarSpec) otherSpec;
        if(numStrings != spec.numStrings) {
            return false;
        }
        return true;
    }

    // Additional Getter/Setters go in here.
}