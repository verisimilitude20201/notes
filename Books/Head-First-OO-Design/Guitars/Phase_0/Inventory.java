public class Inventory {
    private List guitars;

    public Inventory() {
        guitars = new ArrayList<Guitar>();
    }

    public void addGuitar(String serialNumber, double price,String builder, String model, 
    String type, String frontWood, String backWood) {
        Guitar guitar = new Guitar(serialNumber, price, builder, model, type, frontWood, backWood);
        guitars.add(guitar);
    }

    public Guitar search(Guitar searchGuitar) {
        for(Iterator i = guitars.iterator(); i.hasNext;) {
            Guitar guitar = (Guitar) i.next();
            String builder = guitar.getBuilder();
            if(builder != null && !builder.equals("") && builder.equals(searchGuitar.getBuilder())) {
                return guitar;
            }
            String model = guitar.getModel();
            if(model != null && !model.equals("") && model.equals(searchGuitar.getModel())) {
                return guitar;
            }
            // So on for all guitar properties
        }
        
    }

}