public class Store {
    int storeID;
    String storeName;
    boolean status;
    public Store(int storeID, String storeName, boolean status){
        this.storeID = storeID;
        this.storeName = storeName;
        this.status = status;
    }
    public void setStatus(Boolean newStatus){
        this.status = newStatus;
    }
}
