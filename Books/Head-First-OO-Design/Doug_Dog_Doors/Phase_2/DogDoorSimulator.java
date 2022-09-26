class DogDoorSimulator {
    public static void main(String[] args) {
        DogDoor door = new DogDoor();
        Remote remote = new Remote();

        // Fido barks to go outside, press button
        remote.pressButton();
    }
}