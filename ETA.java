import java.time.LocalDateTime;

public class ETA {
    private final int orderId;
    private final LocalDateTime orderTime;
    private final int numberOfItems;
    private final int estimatedMinutes;
    
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
