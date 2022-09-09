public class Inventory {
    private List guitars;

    public Inventory() {
        guitars = new ArrayList<Guitar>();
    }

    public void addGuitar(String serialNumber, double price,Builder builder, String model, 
    Type type, Wood frontWood, Wood backWood) {
        GuitarSpec guitarSpec = new GuitarSpec(model, builder, type, frontWood, backWood)
        Guitar guitar = new Guitar(serialNumber, price, guitarSpec);
        guitars.add(guitar);
    }

    public List<Guitar> search(GuitarSpec searchGuitarSpec) {
        List<Guitar> searchResults  = new ArrayList();
        for(Iterator i = guitars.iterator(); i.hasNext;) {
            Guitar guitar = (Guitar) i.next();
            Builder builder = guitar.getGuitarSpec().getBuilder();
            if(builder != null &&  builder == searchGuitar.getBuilder()) {
                searchResults.add(guitar);
            }
            String model = guitar.getGuitarSpec().getModel();
            if(model != null && !model.equals("") && model.equals(searchGuitar.getModel())) {
                searchResults.add(guitar);
            }
            // So on for all guitar properties
        }
        return searchResults;
        
    }

}