import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

/**
 * SQLite-backed order database helper
 */
public class OrderDatabase {
    private final Path dbPath;
    private final String url;

    public OrderDatabase(Path dbPath) {
        this.dbPath = dbPath;
        this.url = "jdbc:sqlite:" + dbPath.toAbsolutePath().toString();
    }

    public String getConnectionUrl() {
        return url;
    }

    public void init() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found on classpath", e);
        }

        try (Connection c = DriverManager.getConnection(url);
             Statement s = c.createStatement()) {
            
            // Enable foreign key support
            s.executeUpdate("PRAGMA foreign_keys = ON");

            // Check if payment_status column exists
            boolean needsPaymentStatus = false;
            try (ResultSet rs = s.executeQuery("SELECT payment_status FROM orders LIMIT 1")) {
                // If this succeeds, the column exists
            } catch (SQLException ex) {
                needsPaymentStatus = true;
            }

            if (needsPaymentStatus) {
                try {
                    s.executeUpdate("ALTER TABLE orders ADD COLUMN payment_status TEXT DEFAULT 'PENDING'");
                } catch (SQLException ex) {
                    // Column might have been added by another process, ignore
                }
            }

            // Create orders table
            s.executeUpdate("CREATE TABLE IF NOT EXISTS orders ("
                    + "order_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "customer_username TEXT NOT NULL,"
                    + "restaurant_name TEXT NOT NULL,"
                    + "status TEXT NOT NULL DEFAULT 'PENDING'," // 'PENDING', 'ASSIGNED', 'IN_PROGRESS', 'DELIVERED', 'CANCELLED'
                    + "total_amount DECIMAL(10,2) NOT NULL,"
                    + "created_at INTEGER NOT NULL,"
                    + "assigned_at INTEGER,"
                    + "picked_up_at INTEGER,"
                    + "delivered_at INTEGER,"
                    + "driver_username TEXT,"
                    + "delivery_address TEXT,"
                    + "special_instructions TEXT,"
                    + "estimated_minutes INTEGER,"
                    + "actual_minutes INTEGER,"
                    + "item_count INTEGER NOT NULL DEFAULT 0,"
                    + "payment_type TEXT,"  // 'CARD' or 'BANK'
                    + "payment_status TEXT DEFAULT 'PENDING'," // 'PENDING', 'COMPLETED', 'FAILED'
                    + "FOREIGN KEY (customer_username) REFERENCES users(username) ON DELETE RESTRICT ON UPDATE CASCADE,"
                    + "FOREIGN KEY (driver_username) REFERENCES drivers(username) ON DELETE RESTRICT ON UPDATE CASCADE,"
                    + "CHECK (payment_type IN ('CARD', 'BANK'))"
                    + ")");

            // Create payment_transactions table
            s.executeUpdate("CREATE TABLE IF NOT EXISTS payment_transactions ("
                    + "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "order_id INTEGER NOT NULL,"
                    + "amount DECIMAL(10,2) NOT NULL,"
                    + "status TEXT NOT NULL DEFAULT 'PENDING'," // 'PENDING', 'COMPLETED', 'FAILED'
                    + "created_at INTEGER NOT NULL,"
                    + "completed_at INTEGER,"
                    + "payment_type TEXT NOT NULL," // 'CARD' or 'BANK'
                    + "payment_reference TEXT,"
                    + "error_message TEXT,"
                    + "FOREIGN KEY (order_id) REFERENCES orders(order_id)"
                    + ")");

            // Create order_items table
            s.executeUpdate("CREATE TABLE IF NOT EXISTS order_items ("
                    + "item_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "order_id INTEGER NOT NULL,"
                    + "item_name TEXT NOT NULL,"
                    + "quantity INTEGER NOT NULL,"
                    + "unit_price DECIMAL(10,2) NOT NULL,"
                    + "special_requests TEXT,"
                    + "FOREIGN KEY (order_id) REFERENCES orders(order_id)"
                    + ")");

            // Create order_updates table for tracking status changes
            s.executeUpdate("CREATE TABLE IF NOT EXISTS order_updates ("
                    + "update_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "order_id INTEGER NOT NULL,"
                    + "status TEXT NOT NULL,"
                    + "notes TEXT,"
                    + "updated_at INTEGER NOT NULL,"
                    + "updated_by TEXT NOT NULL,"
                    + "FOREIGN KEY (order_id) REFERENCES orders(order_id)"
                    + ")");

            // Add performance indexes for frequently queried columns
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_orders_customer ON orders(customer_username)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_orders_driver ON orders(driver_username)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_orders_created ON orders(created_at)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_order_updates_order ON order_updates(order_id)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_payment_trans_order ON payment_transactions(order_id)");
        }
    }

    public long createOrder(String customerUsername, String restaurantName, String deliveryAddress, 
                          String specialInstructions, double totalAmount, int itemCount, String paymentType) throws SQLException {
        String sql = "INSERT INTO orders (customer_username, restaurant_name, status, total_amount, "
                  + "created_at, delivery_address, special_instructions, estimated_minutes, item_count, payment_type) "
                  + "VALUES (?, ?, 'PENDING', ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, customerUsername);
            p.setString(2, restaurantName);
            p.setDouble(3, totalAmount);
            p.setLong(4, Instant.now().getEpochSecond());
            p.setString(5, deliveryAddress);
            p.setString(6, specialInstructions);
            p.setInt(7, estimateDeliveryTime(totalAmount));
            p.setInt(8, itemCount);
            p.setString(9, paymentType);
            p.executeUpdate();
            
            try (ResultSet rs = p.getGeneratedKeys()) {
                if (rs.next()) {
                    long orderId = rs.getLong(1);
                    // Record initial status
                    recordOrderUpdate(orderId, "PENDING", "Order created", customerUsername);
                    return orderId;
                }
                throw new SQLException("Failed to retrieve generated order ID");
            }
        }
    }

    public void addOrderItem(long orderId, String itemName, int quantity, 
                           double unitPrice, String specialRequests) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, item_name, quantity, unit_price, special_requests) "
                  + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, orderId);
            p.setString(2, itemName);
            p.setInt(3, quantity);
            p.setDouble(4, unitPrice);
            p.setString(5, specialRequests);
            p.executeUpdate();
        }
    }

    public void assignDriver(long orderId, String driverUsername) throws SQLException {
        String sql = "UPDATE orders SET driver_username = ?, status = 'ASSIGNED', assigned_at = ? "
                  + "WHERE order_id = ?";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            long now = Instant.now().getEpochSecond();
            p.setString(1, driverUsername);
            p.setLong(2, now);
            p.setLong(3, orderId);
            p.executeUpdate();
            
            recordOrderUpdate(orderId, "ASSIGNED", "Driver assigned: " + driverUsername, driverUsername);
        }
    }

    public void updateOrderStatus(long orderId, String status, String username) throws SQLException {
        String sql = "UPDATE orders SET status = ?";
        
        // Add timestamp updates based on status
        if (status.equals("IN_PROGRESS")) {
            sql += ", picked_up_at = ?";
        } else if (status.equals("DELIVERED")) {
            sql += ", delivered_at = ?, actual_minutes = ?";
        }
        
        sql += " WHERE order_id = ?";
        
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            int paramIndex = 1;
            p.setString(paramIndex++, status);
            
            long now = Instant.now().getEpochSecond();
            if (status.equals("IN_PROGRESS")) {
                p.setLong(paramIndex++, now);
            } else if (status.equals("DELIVERED")) {
                p.setLong(paramIndex++, now);
                // Calculate actual delivery time
                try (PreparedStatement p2 = c.prepareStatement(
                        "SELECT created_at FROM orders WHERE order_id = ?")) {
                    p2.setLong(1, orderId);
                    ResultSet rs = p2.executeQuery();
                    if (rs.next()) {
                        long createdAt = rs.getLong(1);
                        int actualMinutes = (int)((now - createdAt) / 60);
                        p.setInt(paramIndex++, actualMinutes);
                    }
                }
            }
            
            p.setLong(paramIndex, orderId);
            p.executeUpdate();
            
            recordOrderUpdate(orderId, status, "Status updated to: " + status, username);
        }
    }

    private void recordOrderUpdate(long orderId, String status, String notes, String username) throws SQLException {
        String sql = "INSERT INTO order_updates (order_id, status, notes, updated_at, updated_by) "
                  + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, orderId);
            p.setString(2, status);
            p.setString(3, notes);
            p.setLong(4, Instant.now().getEpochSecond());
            p.setString(5, username);
            p.executeUpdate();
        }
    }

    /**
     * Get order details by order ID.
     * WARNING: This method returns a ResultSet that must be closed by the caller,
     * along with the underlying PreparedStatement and Connection.
     * Consider refactoring to return a DTO instead to prevent resource leaks.
     * 
     * Usage example:
     *   ResultSet rs = db.getOrderDetails(orderId);
     *   try {
     *     // process results
     *   } finally {
     *     if (rs != null) {
     *       rs.getStatement().getConnection().close(); // Close connection
     *       rs.getStatement().close(); // Close statement
     *       rs.close(); // Close result set
     *     }
     *   }
     */
    public ResultSet getOrderDetails(long orderId) throws SQLException {
        String sql = "SELECT o.*, "
                  + "(SELECT GROUP_CONCAT(item_name || ' x' || quantity) FROM order_items WHERE order_id = o.order_id) as items "
                  + "FROM orders o WHERE o.order_id = ?";
        Connection c = DriverManager.getConnection(url);
        PreparedStatement p = c.prepareStatement(sql);
        p.setLong(1, orderId);
        return p.executeQuery();
    }

    /**
     * Get order items for a specific order.
     * WARNING: This method returns a ResultSet that must be closed by the caller,
     * along with the underlying PreparedStatement and Connection to prevent resource leaks.
     */
    public ResultSet getOrderItems(long orderId) throws SQLException {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        Connection c = DriverManager.getConnection(url);
        PreparedStatement p = c.prepareStatement(sql);
        p.setLong(1, orderId);
        return p.executeQuery();
    }

    /**
     * Get order history for a user.
     * WARNING: This method returns a ResultSet that must be closed by the caller,
     * along with the underlying PreparedStatement and Connection to prevent resource leaks.
     */
    public ResultSet getOrderHistory(String username, String userType) throws SQLException {
        String sql = "SELECT o.*, "
                  + "(SELECT GROUP_CONCAT(item_name || ' x' || quantity) FROM order_items WHERE order_id = o.order_id) as items "
                  + "FROM orders o WHERE ";
        
        if (userType.equals("CUSTOMER")) {
            sql += "o.customer_username = ?";
        } else if (userType.equals("DRIVER")) {
            sql += "o.driver_username = ?";
        } else {
            sql += "1=1";  // For admin, show all orders
        }
        
        sql += " ORDER BY o.created_at DESC";
        
        Connection c = DriverManager.getConnection(url);
        PreparedStatement p = c.prepareStatement(sql);
        if (!userType.equals("ADMIN")) {
            p.setString(1, username);
        }
        return p.executeQuery();
    }

    private int estimateDeliveryTime(double orderTotal) {
        // Basic estimation logic - can be made more sophisticated
        if (orderTotal <= 20) return 30;  // 30 minutes for small orders
        else if (orderTotal <= 50) return 45;  // 45 minutes for medium orders
        else return 60;  // 60 minutes for large orders
    }

    public void assignDriverToOrder(long orderId, String driverUsername) throws SQLException {
        String sql = "UPDATE orders SET driver_username = ?, status = 'ASSIGNED', assigned_at = ? " +
                    "WHERE order_id = ? AND status = 'PENDING' AND driver_username IS NULL";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, driverUsername);
            p.setLong(2, Instant.now().getEpochSecond());
            p.setLong(3, orderId);
            int updated = p.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Order not found or already assigned to another driver");
            }
        }
    }

    /**
     * Get pending orders.
     * WARNING: This method returns a ResultSet that must be closed by the caller,
     * along with the underlying PreparedStatement and Connection to prevent resource leaks.
     */
    public ResultSet getPendingOrders() throws SQLException {
        String sql = "SELECT o.*, "
                  + "(SELECT GROUP_CONCAT(item_name || ' x' || quantity) FROM order_items WHERE order_id = o.order_id) as items "
                  + "FROM orders o WHERE o.status = 'PENDING' ORDER BY o.created_at ASC";
        Connection c = DriverManager.getConnection(url);
        PreparedStatement p = c.prepareStatement(sql);
        return p.executeQuery();
    }

    /* Cancel an order */
    public void cancelOrder(long orderId) throws SQLException {
        String sql = "UPDATE orders SET status = 'CANCELLED' WHERE order_id = ?";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, orderId);
            int updated = p.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Order not found");
            }
            
            // Record the cancellation in order_updates
            recordOrderUpdate(orderId, "CANCELLED", "Order cancelled by admin", "admin");
        }
    }
}