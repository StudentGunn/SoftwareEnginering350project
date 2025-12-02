/**
 * Item represents a menu item with name and price.
 */
public class Item {
    String name;
    double price;
    
    /**
     * Constructs an Item with the given information.
     * @param name Name of the item.
     * @param price Price of the item.
     */
    public Item(String name, double price){
        this.name = name;
        this.price = price;
    }
}
