public class DogDoor {
    private boolean open;

    private List<Bark> allowedBarks;

    public DogDoor() {
        this.open = false;
    }

    public void open() {
        this.open = true;
        final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    door.close();
                    timer.cancel();
                }
            }, 5000);
    }
    
    public void close() {
        this.close
    }

    public List<Bark> getAllowedBark() {
        return this.allowedBark;
    }
}