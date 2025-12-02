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
    
    /**
     * Returns a formatted message with the estimated delivery time.
     * @return String message with ETA in minutes.
     */
    public String getETAMessage() {
        return String.format("Estimated delivery time: %d minutes", estimatedMinutes);
    }
    
    /**
     * Returns the estimated delivery time in minutes.
     * @return Estimated minutes for delivery.
     */
    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }
    
    /**
     * Returns the calculated delivery date and time.
     * @return LocalDateTime when the order is expected to be delivered.
     */
    public LocalDateTime getDeliveryTime() {
        return orderTime.plusMinutes(estimatedMinutes);
    }
    
    /**
     * Returns the order ID associated with this ETA.
     * @return The order identifier.
     */
    public int getOrderId() {
        return orderId;
    }
}
