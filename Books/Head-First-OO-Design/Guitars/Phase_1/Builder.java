public enum Builder {
    FENDER, MARTIN, GIBSON;

    public String toString() {
        switch(this):
            case FENDER: return "fender";
            case MARTIN: return "martin";
            case GIBSON: return "gibson";
    }
}