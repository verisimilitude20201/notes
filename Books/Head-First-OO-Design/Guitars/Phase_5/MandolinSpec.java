public class MandolinSpec extends InstrumentSpec {
    
    private Style style;

    public Guitar(String model, Builder builder,
    Type type, Wood frontWood, Wood backWood, int numStrings, Style style) {
        super(mode, builder, type, frontWood, backWood);
        this.style = style;
    }

    public boolean matches(InstrumentSpec otherSpec) {
        if(!super.matches(otherSpec)) {
            return false;
        }
        if(!otherSpec instanceof MandolinSpec) {
            return false;
        }
        MandolinSpec spec  = (MandolinSpec) otherSpec;
        if(style != spec.style) {
            return false;
        }
        return true;
    }

    // Additional Getter/Setters go in here.
}