public class Inventory {
    private List inventory;

    public Inventory() {
        inventory = new ArrayList<Guitar>();
    }

    public void addInstrument(String serialNumber, double price, InstrumentSpec) {
       Instrument instrument = null;
       if(spec instanceof GuitarSpec) {
         instrument = new Guitar(serialNumber, price, (GuitarSpec)spec);
       } else if(spec instanceof MandolinSpec) {
         instrument = new MandolinSpec(serialNumber, price, (MandolinSpec)spec);
       }
       inventory.add(instrument);
    }



    public List<Guitar> search(GuitarSpec searchGuitarSpec) {
        List<Guitar> searchResults  = new ArrayList();
        for(Iterator i = inventory.iterator(); i.hasNext;) {
            Guitar guitar = (Guitar) i.next();
            GuitarSpec guitarSpec = guitar.getGuitarSpec();
            if(searchGuitarSpec.matches(guitarSpec)) {
                searchResults.add(guitar);
            }
        }
        return searchResults;
        
    }

    public List<Guitar> searchMandolins(MandolinSpec spec) {
        List<Mandolin> searchResults  = new ArrayList();
        for(Iterator i = inventory.iterator(); i.hasNext;) {
            Mandolin guitar = (Mandolin) i.next();
            MandolinSpec mandolinSpec = guitar.getGuitarSpec();
            if(mandolinSpec.matches(spec)) {
                searchResults.add(guitar);
            }
        }
        return searchResults;
        
    }

}