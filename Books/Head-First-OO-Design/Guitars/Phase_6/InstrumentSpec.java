class InstrumentSpec {
    private Map<Object, Object> properties;

    public Guitar(Map<String, Object> properties) {
        // Initialize all private properties
        if (properties != null) {
            this.properties = properties;
        } else {
            this.properties = new HashMap<String, Object>();
        }
        
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public Map<Object, Object> getProperties() {
        return this.properties;
    }

    public boolean matches(Map otherSpec) {
        if(builder != otherSpec.builder) {
            return False

        }
        // So on for other properties compare not equals
        return True
    }
}