public class BarkRecognizer {
    private Dogdoor door;

    public BarkRecognizer(Dogdoor door) {
        this.door = door;
    }

    public void recognize(Bark bark) {
        if door.getAllowedBark().equals(bark) {
            door.open();
        }
    }
}