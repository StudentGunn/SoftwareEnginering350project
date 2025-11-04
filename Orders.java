public class Orders {
    int orderID;
    String customer;
    int customerID;
    String driver;
    int driverID;
    String[] items;
    int StoreID;
    int status;
/*Everything needed to make an order
* Status 0: unaccepted and Being made by store, 1:Accpected and Being made by store, 2: ready for pickup, 3: en route 4: Completed, 5:Cancelled
* Items is what was ordered by the customer */
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


