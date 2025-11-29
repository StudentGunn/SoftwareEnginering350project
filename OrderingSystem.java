import java.util.ArrayList;

public class OrderingSystem{
    ArrayList<Orders> unaccpetedOrders = new ArrayList<>();
    ArrayList<Orders> acceptedOrders = new ArrayList<>();
    /*Method to create a new order,
    * This will be submitted to the store of store.ID
    * to be put in a queue of unaccpeted orders*/
    public boolean preOderCheck(Customer customer, double total, Store store){
        if (total >= customer.balance){
            return false;
        }
        return store.status;
    };
    private void createNewOrder (int orderID,Customer customer, String items[], int storeID){
        Orders newOrder = new Orders(orderID, customer.name, customer.ID,null, 0,items,storeID,0);
        unaccpetedOrders.add(newOrder);
    };
    /*Method for when a driver accpets an order,
    * The oder will be taken out of the unaccepted queue and the order object will have the added properties */
    public void acceptOrder(Driver driver, Orders order){
        order.driverID = driver.ID;
        order.driver = driver.name;
        unaccpetedOrders.remove(order);
        acceptedOrders.add(order);
        order.status = 1;
    } 

    /*
    --> Just stores to use update an order status
    --> Should also change where it does in the database
    */
    public void updateStatus(int newStatus, Orders order){
        order.status = newStatus;
        cancelOrder(order);
    }
    /*  
     --> Cancels an order if its status is 5 (canceled)
     --> Removes it from unacceptedOrders if no driver assigned
     */
    private void cancelOrder (Orders order){
        if (order.status == 5){
            if (order.driverID == 0){
                unaccpetedOrders.remove(order);
            }
            else {
                acceptedOrders.remove(order);
            } 
        }
    }

}
