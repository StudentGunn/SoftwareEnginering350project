/**
 * Store represents a restaurant or vendor with ID, name, and operational status.
 */
public class Store {
    int storeID;
    String storeName;
    boolean status;
    
    /**
     * Constructs a Store with the given information.
     * @param storeID Unique store identifier.
     * @param storeName Name of the store.
     * @param status Operational status (true = open, false = closed).
     */
    public Store(int storeID, String storeName, boolean status){
        this.storeID = storeID;
        this.storeName = storeName;
        this.status = status;
    }
    public void setStatus(Boolean newStatus){
        this.status = newStatus;
    }
}
