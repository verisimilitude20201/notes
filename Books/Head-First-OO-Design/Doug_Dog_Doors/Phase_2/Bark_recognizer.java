public class BarkRecognizer {
    private Dogdoor door;

    public BarkRecognizer(Dogdoor door) {
        this.door = door;
    }

    public void recognize(String bark) {
        door.open()
    }
}