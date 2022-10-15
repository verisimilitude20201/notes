public class BarkRecognizer {
    private Dogdoor door;

    public BarkRecognizer(Dogdoor door) {
        this.door = door;
    }

    public void recognize(Bark bark) {
        for(Bark otherBark : door.getAllowedBark()) {
            if (otherBark.equals(bark)) {
                door.open();
                return;
            }
        }
        
    }
}