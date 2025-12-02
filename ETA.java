import java.time.LocalDateTime;

/**
 * ETA calculates and tracks estimated delivery time for an order.
 * Estimates are based on the number of items: 5 minutes for 1 item, 10 minutes for 2+ items.
 */
public class ETA {
    private final int orderId;
    private final LocalDateTime orderTime;
    private final int numberOfItems;
    private final int estimatedMinutes;
    
    /**
     * Constructs an ETA for an order and calculates the estimated delivery time.
     * @param orderId Order identifier.
     * @param numberOfItems Number of items in the order.
     */
    public ETA(int orderId, int numberOfItems) {
        this.orderId = orderId;
        this.orderTime = LocalDateTime.now();
        this.numberOfItems = numberOfItems;
        this.estimatedMinutes = calculateETA(numberOfItems);
    }
    
    private int calculateETA(int items) {
        return items <= 1 ? 5 : 10;
    }
    
    public String getETAMessage() {
        return String.format("Estimated delivery time: %d minutes", estimatedMinutes);
    }
    
    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }
    
    public LocalDateTime getDeliveryTime() {
        return orderTime.plusMinutes(estimatedMinutes);
    }
    
    public int getOrderId() {
        return orderId;
    }
}
