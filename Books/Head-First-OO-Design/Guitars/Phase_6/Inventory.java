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



    public List<Guitar> search(InstrumentSpec instrumentSpec) {
        List<Instrument> searchResults  = new ArrayList();
        for(Iterator i = inventory.iterator(); i.hasNext;) {
            Instrument instrument = (Instrument) i.next();
            if(instrument.getSpec().matches(instrumentSpec)) {
                searchResults.add(instrument);
            }
        }
        return searchResults;
        
    }

}