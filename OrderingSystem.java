import java.util.ArrayList;

/**
 * OrderingSystem manages order queues and order state transitions.
 * Maintains lists of unaccepted and accepted orders.
 */
public class OrderingSystem {
    ArrayList<Orders> unacceptedOrders = new ArrayList<>();
    ArrayList<Orders> acceptedOrders = new ArrayList<>();

    /**
     * Checks if a customer can place an order based on balance and store status.
     * @param customer The customer placing the order.
     * @param total The total amount of the order.
     * @param store The store receiving the order.
     * @return true if the order can proceed, false otherwise.
     */
    public boolean preOrderCheck(Customer customer, double total, Store store) {
        try {
            if (total >= customer.balance){
                return false;
            }
            return store.status;
        } catch (Exception e) {
            Logger.catchAndLogBug(e, "OrderingSystem.preOrderCheck");
            return false;
        }
    };
    public int createNewOrder (int orderID,Customer customer, String items[], int storeID){
        Orders newOrder = new Orders(orderID, customer.name, customer.ID,null, 0,items,storeID,0);
        try {
            unacceptedOrders.add(newOrder);
            // return newOrder.orderID;
        } catch (Exception e) {
            Logger.catchAndLogBug(e, "OrderingSystem.createNewOrder");
            
        }
        return newOrder.orderID;

    };
    /*Method for when a driver accpets an order,
    * The oder will be taken out of the unaccepted queue and the order object will have the added properties */
    public void acceptOrder(Driver driver, Orders order){
        try {
            order.driverID = driver.ID;
            order.driver = driver.name;
            unacceptedOrders.remove(order);
            acceptedOrders.add(order);
            order.status = 1;
        } catch (Exception e) {
            Logger.catchAndLogBug(e, "OrderingSystem.acceptOrder");
        }
    } 

    /*
    --> Just stores to use update an order status
    --> Should also change where it does in the database
    */
    public void updateStatus(int newStatus, Orders order){
        try {
            order.status = newStatus;
            cancelOrder(order);
        } catch (Exception e) {
            Logger.catchAndLogBug(e, "OrderingSystem.updateStatus");
        }
    }
    /*  
     --> Cancels an order if its status is 5 (canceled)
     --> Removes it from unacceptedOrders if no driver assigned
     */
    private void cancelOrder (Orders order){
        try {
            if (order.status == 5){
                if (order.driverID == 0){
                    unacceptedOrders.remove(order);
                }
                else {
                    acceptedOrders.remove(order);
                } 
            }
        } catch (Exception e) {
            Logger.catchAndLogBug(e, "OrderingSystem.cancelOrder");
        }
    }

}
