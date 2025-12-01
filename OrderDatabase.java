import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import javax.swing.JOptionPane;

/*
 * Order database - handles everything related to food orders
 * this is probably the most complex database file
 * manages orders, order items, payment transactions, and status updates
 */
public class OrderDatabase {
    private final Path dbPath;
    private final String url;

    public OrderDatabase(Path dbPath) {
        this.dbPath = dbPath;
        this.url = "jdbc:sqlite:" + dbPath.toAbsolutePath().toString();
        System.err.println(url);
    }

    public String getConnectionUrl() {
        return url;
    }

    public void init() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            Logger.catchAndLogBug(e, "OrderDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while initializing the order database:\n" +
                e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            throw new SQLException("SQLite JDBC driver not found on classpath", e);
        }

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("PRAGMA foreign_keys = ON");

            // migration stuff - adding columns to existing tables if they dont have them yet
            // this way the database works even if someone has an older version

            // check for payment_status column
            boolean needsPaymentStatus = false;
            try (ResultSet rs = stmt.executeQuery("SELECT payment_status FROM orders LIMIT 1")) {
                // column exists if this works
            } catch (SQLException ex) {
                needsPaymentStatus = true;
            }
            if (needsPaymentStatus) {
                try {
                    stmt.executeUpdate("ALTER TABLE orders ADD COLUMN payment_status TEXT DEFAULT 'PENDING'");
                } catch (SQLException ex) {
                    // probably already added, just ignore
                }
            }

            // check for payment_type column
            boolean needsPaymentType = false;
            try (ResultSet rs = stmt.executeQuery("SELECT payment_type FROM orders LIMIT 1")) {
            } catch (SQLException ex) {
                needsPaymentType = true;
            }
            if (needsPaymentType) {
                try {
                    stmt.executeUpdate("ALTER TABLE orders ADD COLUMN payment_type TEXT");
                } catch (SQLException ex) {
                }
            }

            // check for restaurant_address
            boolean needsRestaurantAddress = false;
            try (ResultSet rs = stmt.executeQuery("SELECT restaurant_address FROM orders LIMIT 1")) {
            } catch (SQLException ex) {
                needsRestaurantAddress = true;
            }
            if (needsRestaurantAddress) {
                try {
                    stmt.executeUpdate("ALTER TABLE orders ADD COLUMN restaurant_address TEXT");
                } catch (SQLException ex) {
                }
            }

            //  make as a delivered_notified flag (for customer notifications- easy way to do it before .db convertion)
            boolean needsDeliveredNotified = false;
            try (ResultSet rs = stmt.executeQuery("SELECT delivered_notified FROM orders LIMIT 1")) {
            } catch (SQLException ex) {
                needsDeliveredNotified = true;
            }
            if (needsDeliveredNotified) {
                try {
                    stmt.executeUpdate("ALTER TABLE orders ADD COLUMN delivered_notified INTEGER DEFAULT 0");
                } catch (SQLException ex) {
                }
            }

            // main orders table - stores all the order info
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS orders ("
                    + "order_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "customer_username TEXT NOT NULL,"
                    + "restaurant_name TEXT NOT NULL,"
                    + "status TEXT NOT NULL DEFAULT 'PENDING',"
                    + "total_amount DECIMAL(10,2) NOT NULL,"
                    + "created_at INTEGER NOT NULL,"
                    + "assigned_at INTEGER,"  // when driver picked it up
                    + "picked_up_at INTEGER,"
                    + "delivered_at INTEGER,"
                    + "driver_username TEXT,"
                    + "delivery_address TEXT,"
                    + "special_instructions TEXT,"  // extra requests from customer
                    + "estimated_minutes INTEGER,"
                    + "actual_minutes INTEGER,"  
                    + "item_count INTEGER NOT NULL DEFAULT 0,"
                    + "payment_type TEXT,"
                    + "payment_status TEXT DEFAULT 'PENDING',"
                    + "restaurant_address TEXT,"
                    + "delivered_notified INTEGER DEFAULT 0,"
                    + "FOREIGN KEY (customer_username) REFERENCES users(username) ON DELETE RESTRICT ON UPDATE CASCADE,"
                    + "FOREIGN KEY (driver_username) REFERENCES drivers(username) ON DELETE RESTRICT ON UPDATE CASCADE,"
                    + "CHECK (payment_type IN ('CARD', 'BANK'))"
                    + ")");

            // payment transactions - separate from orders table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS payment_transactions ("
                    + "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "order_id INTEGER NOT NULL,"
                    + "amount DECIMAL(10,2) NOT NULL,"
                    + "status TEXT NOT NULL DEFAULT 'PENDING',"
                    + "created_at INTEGER NOT NULL,"
                    + "completed_at INTEGER,"
                    + "payment_type TEXT NOT NULL,"
                    + "payment_reference TEXT,"
                    + "error_message TEXT,"
                    + "FOREIGN KEY (order_id) REFERENCES orders(order_id)"
                    + ")");

            // individual items in each order (pizza x2, burger x1, etc)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS order_items ("
                    + "item_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "order_id INTEGER NOT NULL,"
                    + "item_name TEXT NOT NULL,"
                    + "quantity INTEGER NOT NULL,"
                    + "unit_price DECIMAL(10,2) NOT NULL,"
                    + "special_requests TEXT,"
                    + "FOREIGN KEY (order_id) REFERENCES orders(order_id)"
                    + ")");

            // keeps a log of all status changes for an order
            // useful for tracking when things went wrong
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS order_updates ("
                    + "update_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "order_id INTEGER NOT NULL,"
                    + "status TEXT NOT NULL,"
                    + "notes TEXT,"
                    + "updated_at INTEGER NOT NULL,"
                    + "updated_by TEXT NOT NULL,"
                    + "FOREIGN KEY (order_id) REFERENCES orders(order_id)"
                    + ")");

            // indexes for common searches
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_orders_customer ON orders(customer_username)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id)");
        }
    }

    private boolean tableExists(String tableName, Connection conn) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            return rs.next();
        }
    }

    private void addColumnIfNotExists(String tableName, String columnName, String columnDefinition, Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (rs.next()) {
                if (rs.getString("name").equalsIgnoreCase(columnName)) {
                    return; // Column already exists
                }
            }
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
        }
    }
    // creates a new order and returns the order id
    // also estimates delivery time based on order total
    public long createOrder(String customerUsername, String restaurantName, String restaurantAddress,
                            String deliveryAddress, String specialInstructions, double totalAmount,
                            int itemCount, String paymentType, double restaurantLat, double restaurantLon,
                            double deliveryLat, double deliveryLon) throws SQLException {
        String sql = "INSERT INTO orders (customer_username, restaurant_name, restaurant_address, status, total_amount, "
                  + "created_at, delivery_address, special_instructions, estimated_minutes, item_count, payment_type) "
                  + "VALUES (?, ?, ?, 'PENDING', ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customerUsername);
            ps.setString(2, restaurantName);
            ps.setString(3, restaurantAddress);
            ps.setDouble(4, totalAmount);
            ps.setLong(5, Instant.now().getEpochSecond());
            ps.setString(6, deliveryAddress);
            ps.setString(7, specialInstructions);
            ps.setInt(8, estimateDeliveryTime(totalAmount, restaurantLat, restaurantLon, deliveryLat, deliveryLon));  // calculate estimated time
            ps.setInt(9, itemCount);
            ps.setString(10, paymentType);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long orderId = rs.getLong(1);
                    recordOrderUpdate(orderId, "PENDING", "Order created", customerUsername);
                    return orderId;
                }
                throw new SQLException("Failed to retrieve generated order ID");
            } catch (SQLException ex) {
                Logger.catchAndLogBug(ex,"OrderDatabase");
                JOptionPane.showMessageDialog(null, "An error occurred while creating the order:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                return -1;
            }
            
        }
    }
    // adds an item to an existing order
    public void addOrderItem(long orderId, String itemName, int quantity,
                           double unitPrice, String specialRequests) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, item_name, quantity, unit_price, special_requests) "
                  + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setString(2, itemName);
            ps.setInt(3, quantity);
            ps.setDouble(4, unitPrice);
            ps.setString(5, specialRequests);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.catchAndLogBug(ex,"OrderDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while adding item to order:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // assigns a driver to an order and updates status to ASSIGNED
    public void assignDriver(long orderId, String driverUsername) throws SQLException {
        String sql = "UPDATE orders SET driver_username = ?, status = 'ASSIGNED', assigned_at = ? "
                  + "WHERE order_id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            long now = Instant.now().getEpochSecond();
            ps.setString(1, driverUsername);
            ps.setLong(2, now);
            ps.setLong(3, orderId);
            ps.executeUpdate();

            recordOrderUpdate(orderId, "ASSIGNED", "Driver assigned: " + driverUsername, driverUsername);
        } catch (SQLException ex) {
            Logger.catchAndLogBug(ex,"OrderDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while assigning driver to order:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    /*
     * updates order status and also sets timestamps depending on what status its changing to
     * if IN_PROGRESS - records pickup time
     * if DELIVERED - records delivery time AND calculates how long it took
     */
    public void updateOrderStatus(long orderId, String status, String username) throws SQLException {
        String sql = "UPDATE orders SET status = ?";

        // depending on status we need to update different timestamp fields
        if (status.equals("IN_PROGRESS")) {
            sql += ", picked_up_at = ?";
        } else if (status.equals("DELIVERED")) {
            sql += ", delivered_at = ?, actual_minutes = ?";
        }

        sql += " WHERE order_id = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            ps.setString(paramIndex++, status);

            long now = Instant.now().getEpochSecond();
            if (status.equals("IN_PROGRESS")) {
                ps.setLong(paramIndex++, now);
            } else if (status.equals("DELIVERED")) {
                ps.setLong(paramIndex++, now);
                // need to calculate actual delivery time
                try (PreparedStatement ps2 = conn.prepareStatement(
                        "SELECT created_at FROM orders WHERE order_id = ?")) {
                    ps2.setLong(1, orderId);
                    ResultSet rs = ps2.executeQuery();
                    if (rs.next()) {
                        long createdAt = rs.getLong(1);
                        int actualMinutes = (int)((now - createdAt) / 60);
                        ps.setInt(paramIndex++, actualMinutes);
                    }
                }
            }
            ps.setLong(paramIndex, orderId);
            ps.executeUpdate();

            recordOrderUpdate(orderId, status, "Status updated to: " + status, username);
        } catch (SQLException ex) {
            Logger.catchAndLogBug(ex,"OrderDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while updating order status:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // helper method to log status changes to order_updates table
    private void recordOrderUpdate(long orderId, String status, String notes, String username) throws SQLException {
        String sql = "INSERT INTO order_updates (order_id, status, notes, updated_at, updated_by) "
                  + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setString(2, status);
            ps.setString(3, notes);
            ps.setLong(4, Instant.now().getEpochSecond());
            ps.setString(5, username);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.catchAndLogBug(ex,"OrderDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while recording order update:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // gets full order details including all items
    // uses GROUP_CONCAT to combine all items into one string
    // remember to close the ResultSet when done
    public ResultSet getOrderDetails(long orderId) throws SQLException {
        String sql = "SELECT o.*, "
                  + "(SELECT GROUP_CONCAT(item_name || ' x' || quantity) FROM order_items WHERE order_id = o.order_id) as items "
                  + "FROM orders o WHERE o.order_id = ?";
        try {
            Connection conn = DriverManager.getConnection(url);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, orderId);
            return ps.executeQuery();
        } catch (SQLException e) {
            Logger.catchAndLogBug(e, "OrderDatabase");
            throw e;
        }
    }

    // gets just the items for an order
    public ResultSet getOrderItems(long orderId) throws SQLException {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        Connection conn = DriverManager.getConnection(url);
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, orderId);
        return ps.executeQuery();
    }

    // gets order history - different query depending on user type
    // customers see their orders, drivers see orders they delivered, admins see everything
    public ResultSet getOrderHistory(String username, String userType) throws SQLException {
        String sql = "SELECT o.*, "
                  + "(SELECT GROUP_CONCAT(item_name || ' x' || quantity) FROM order_items WHERE order_id = o.order_id) as items "
                  + "FROM orders o WHERE ";

        if (userType.equals("CUSTOMER")) {
            sql += "o.customer_username = ?";
        } else if (userType.equals("DRIVER")) {
            sql += "o.driver_username = ?";
        } else {
            sql += "1=1";  // show all orders for admin
        }

        sql += " ORDER BY o.created_at DESC";

        Connection conn = DriverManager.getConnection(url);
        PreparedStatement ps = conn.prepareStatement(sql);
        if (!userType.equals("ADMIN")) {
            ps.setString(1, username);
        }
        return ps.executeQuery();
    }

    // simple estimate based on order size and distance
    private int estimateDeliveryTime(double orderTotal, double restaurantLat, double restaurantLon, double deliveryLat, double deliveryLon) {
        int baseTime;
        if (orderTotal <= 20) {
            baseTime = 20;
        } else if (orderTotal <= 50) {
            baseTime = 30;
        } else {
            baseTime = 40;
        }

        try {
            double travelTime = MapCalculator.calculateETA(restaurantLat, restaurantLon, deliveryLat, deliveryLon);
            return baseTime + (int) Math.round(travelTime);
        } catch (Exception e) {
            // Fallback to a simpler estimate if coordinates are not available or invalid
            return baseTime + 15; // Add a default travel time
        }
    }

    // assigns driver but only if order is still pending and not already assigned
    public void assignDriverToOrder(long orderId, String driverUsername) throws SQLException {
        String sql = "UPDATE orders SET driver_username = ?, status = 'ASSIGNED', assigned_at = ? " +
                    "WHERE order_id = ? AND status = 'PENDING' AND driver_username IS NULL";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, driverUsername);
            ps.setLong(2, Instant.now().getEpochSecond());
            ps.setLong(3, orderId);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Order not found or already assigned to another driver");
            }
        } catch (SQLException e) {
            Logger.catchAndLogBug(e, "OrderDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while assigning driver to order:\n" +
                e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    // gets all pending orders sorted by oldest first
    public ResultSet getPendingOrders() throws SQLException {
        String sql = "SELECT o.*, "
                  + "(SELECT GROUP_CONCAT(item_name || ' x' || quantity) FROM order_items WHERE order_id = o.order_id) as items "
                  + "FROM orders o WHERE o.status = 'PENDING' ORDER BY o.created_at ASC";
        Connection conn = DriverManager.getConnection(url);
        PreparedStatement ps = conn.prepareStatement(sql);
        return ps.executeQuery();
    }

    // cancels an order
    public void cancelOrder(long orderId) throws SQLException {
        String sql = "UPDATE orders SET status = 'CANCELLED' WHERE order_id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Order not found");
            }

            recordOrderUpdate(orderId, "CANCELLED", "Order cancelled by admin", "admin");
        } catch (SQLException ex) {
            Logger.catchAndLogBug(ex,"OrderDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while cancelling the order:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // check if a customer has any delivered orders that haven't been notified yet
    public boolean hasUnnotifiedDelivered(String customerUsername) throws SQLException {
        String sql = "SELECT EXISTS(SELECT 1 FROM orders WHERE customer_username = ? AND status = 'DELIVERED' AND COALESCE(delivered_notified,0) = 0)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerUsername);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            } catch (SQLException ex) {
                Logger.catchAndLogBug(ex,"OrderDatabase");
                JOptionPane.showMessageDialog(null, "An error occurred while checking for unnotified delivered orders:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }


    // mark all delivered orders as notified;to avoid repeat notifications
    public void markDeliveredNotified(String customerUsername) throws SQLException {
        String sql = "UPDATE orders SET delivered_notified = 1 WHERE customer_username = ? AND status = 'DELIVERED' AND COALESCE(delivered_notified,0) = 0";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerUsername);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.catchAndLogBug(ex,"OrderDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while marking orders as notified:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    } 
    // Used to determine the oldest pending order accepted by a single driver.
    // This will be used to make sure that the order shown in the driver
    // main screen is the most important.
    public ResultSet getOldestActiveOrder(String driverUsername) throws SQLException {
        String sql = "SELECT * FROM orders WHERE driver_username = ? AND status != 'DELIVERED' AND status != 'CANCELLED' ORDER BY order_id ASC LIMIT 1";
        Connection conn = DriverManager.getConnection(url);
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, driverUsername);
        return ps.executeQuery();
    }
}