/**
 * Orders represents a food delivery order with customer, driver, items, and status tracking.
 * Status codes: 0 = unaccepted and being made by store, 1 = accepted and being made by store,
 * 2 = ready for pickup, 3 = en route, 4 = completed, 5 = cancelled.
 */
public class Orders {
    int orderID;
    String customer;
    int customerID;
    String driver;
    int driverID;
    String[] items;
    int StoreID;
    int status;
    
    /**
     * Constructs an Orders object with complete order information.
     * @param orderID Unique order identifier.
     * @param customer Customer's name.
     * @param customerID Customer's ID.
     * @param driver Driver's name.
     * @param driverID Driver's ID.
     * @param items Array of ordered item names.
     * @param storeID Store identifier.
     * @param status Order status code (0-5).
     */
    public Orders(int orderID, String customer, int customerID, String driver, int driverID, String[] items, int storeID, int status){
        this.orderID = orderID;
        this.customer =  customer;
        this.customerID = customerID;
        this.driver = driver;
        this.driverID = driverID;
        this.items = items;
        this.status = status;
        this.StoreID = storeID;

    }

    public int getOrderID() {
        return orderID;
    }


}


