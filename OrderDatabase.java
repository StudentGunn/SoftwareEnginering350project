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

            // Check if payment_type column exists
            boolean needsPaymentType = false;
            try (ResultSet rs = s.executeQuery("SELECT payment_type FROM orders LIMIT 1")) {
                // If this succeeds, the column exists
            } catch (SQLException ex) {
                needsPaymentType = true;
            }

            if (needsPaymentType) {
                try {
                    s.executeUpdate("ALTER TABLE orders ADD COLUMN payment_type TEXT");
                } catch (SQLException ex) {
                    // Column might have been added by another process, ignore
                }
            }

            // Check if restaurant_address column exists
            boolean needsRestaurantAddress = false;
            try (ResultSet rs = s.executeQuery("SELECT restaurant_address FROM orders LIMIT 1")) {
                // If this succeeds, the column exists
            } catch (SQLException ex) {
                needsRestaurantAddress = true;
            }

            if (needsRestaurantAddress) {
                try {
                    s.executeUpdate("ALTER TABLE orders ADD COLUMN restaurant_address TEXT");
                    System.out.println("Added restaurant_address column to orders table");
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
    /*
     * Create a new order and return its generated order ID.
     * @return generated order ID
     * @throws SQLException if a database access error occurs
     */
    public long createOrder(String customerUsername, String restaurantName, String restaurantAddress,
                          String deliveryAddress, String specialInstructions, double totalAmount, 
                          int itemCount, String paymentType) throws SQLException {
        String sql = "INSERT INTO orders (customer_username, restaurant_name, restaurant_address, status, total_amount, "
                  + "created_at, delivery_address, special_instructions, estimated_minutes, item_count, payment_type) "
                  + "VALUES (?, ?, ?, 'PENDING', ?, ?, ?, ?, ?, ?, ?)";
        // connect to database and grab, CustomeruserName, RestaurantName, RestaurantAddress, DeliveryAddress, SpecialInstructions, TotalAmount, ItemCount, PaymentType
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, customerUsername);
            p.setString(2, restaurantName);
            p.setString(3, restaurantAddress);
            p.setDouble(4, totalAmount);
            p.setLong(5, Instant.now().getEpochSecond());
            p.setString(6, deliveryAddress);
            p.setString(7, specialInstructions);
            p.setInt(8, estimateDeliveryTime(totalAmount));
            p.setInt(9, itemCount);
            p.setString(10, paymentType);
            p.executeUpdate(); // update database at the end
            
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
    /*
      --> Add an item to an existing order.
      --> update the order_items table with the new item details.
      --> @throws SQLException if a database access error occurs
     */
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
    /*
      --> Assign a driver to an order.
      --> Update the orders table with the driver username and status.
      --> @throws SQLException if a database access error occurs
     */
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
    /* 
      --> Update the status of an order.
      --> Update timestamp fields based on status changes.
      --> @throws SQLException if a database access error occurs
     */
    public void updateOrderStatus(long orderId, String status, String username) throws SQLException {
        String sql = "UPDATE orders SET status = ?";
        
        // Add timestamp updates based on status
        if (status.equals("IN_PROGRESS")) {
            sql += ", picked_up_at = ?"; // set picked up time
        } else if (status.equals("DELIVERED")) {
            sql += ", delivered_at = ?, actual_minutes = ?";// set delivered time and actual minutes
        }
        
        sql += " WHERE order_id = ?";
       // connect to database and update order status
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            int paramIndex = 1; // parameter index starts at 1
            p.setString(paramIndex++, status); // set status
            // Set timestamps if applicable
            long now = Instant.now().getEpochSecond();// get current time
            if (status.equals("IN_PROGRESS")) {
                p.setLong(paramIndex++, now);
            } else if (status.equals("DELIVERED")) {
                p.setLong(paramIndex++, now);
                // Calculate actual delivery time
                try (PreparedStatement p2 = c.prepareStatement(
                        "SELECT created_at FROM orders WHERE order_id = ?")) { //
                    p2.setLong(1, orderId); // set orderId
                    ResultSet rs = p2.executeQuery();
                    if (rs.next()) { // should always be true
                        long createdAt = rs.getLong(1);// get created at time
                        int actualMinutes = (int)((now - createdAt) / 60); // in minutes
                        p.setInt(paramIndex++, actualMinutes); // set actual_minutes
                    }
                }
            }
            
            p.setLong(paramIndex, orderId); // set orderId at the end
            p.executeUpdate(); // update database
            
            recordOrderUpdate(orderId, status, "Status updated to: " + status, username); // log status change
        }
    }

    private void recordOrderUpdate(long orderId, String status, String notes, String username) throws SQLException {
        String sql = "INSERT INTO order_updates (order_id, status, notes, updated_at, updated_by) "
                  + "VALUES (?, ?, ?, ?, ?)"; // insert into order_updates table
        try (Connection c = DriverManager.getConnection(url); // connect to database
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, orderId); // set orderId
            p.setString(2, status); // set status
            p.setString(3, notes); // set notes
            p.setLong(4, Instant.now().getEpochSecond()); // set updated_at
            p.setString(5, username); // set updated_by
            p.executeUpdate(); // execute insert
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