class InstrumentSpec {
    private Builder builder;
    private Type type;
    private Wood frontWood;
    private Wood backWood;
    private String model;

    public Guitar(String model, Builder builder,
    Type type, Wood frontWood, Wood backWood) {
        // Initialize all private properties
    }

    public boolean matches(InstrumentSpec otherSpec) {
        if(builder != otherSpec.builder) {
            return False

        }
        // So on for other properties compare not equals
        return True
    }
}