import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

import javax.swing.JOptionPane;

// database class for managing driver accounts and their delivery stuff
// has 3 tables: drivers, delivery_history, and driver_schedule
public class DriverDatabase {
    private final Path dbPath;
    private final String url;

    public DriverDatabase(Path dbPath) {
        this.dbPath = dbPath;
        this.url = "jdbc:sqlite:" + dbPath.toAbsolutePath().toString();
    }

    // initializes all the driver tables
    public void init() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            Logger.catchAndLogBug(e, "DriverDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while initializing the driver database:\n" +
                e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            throw new SQLException("SQLite JDBC driver not found on classpath", e);
        }

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("PRAGMA foreign_keys = ON");  // need this or foreign keys dont work

            /*
             * main drivers table - stores info about each driver
             * username links to the users table
             */
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS drivers ("
                    + "username TEXT PRIMARY KEY,"
                    + "vehicle_type TEXT,"
                    + "license_number TEXT,"
                    + "current_status TEXT DEFAULT 'OFFLINE',"  // tracks if driver is available or busy
                    + "total_deliveries INTEGER DEFAULT 0,"
                    + "rating DECIMAL(3,2) DEFAULT 0.0,"  // average customer rating out of 5
                    + "service_area TEXT,"  // what area they deliver in
                    + "account_status TEXT DEFAULT 'ACTIVE',"
                    + "joined_date INTEGER NOT NULL,"
                    + "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE ON UPDATE CASCADE"
                    + ")");

            // history of all deliveries a driver has done
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS delivery_history ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "driver_username TEXT NOT NULL,"
                    + "order_id INTEGER NOT NULL,"
                    + "pickup_time INTEGER,"
                    + "delivery_time INTEGER,"
                    + "delivery_status TEXT,"
                    + "customer_rating INTEGER,"  // rating from 1-5
                    + "customer_feedback TEXT,"
                    + "earnings DECIMAL(10,2),"  // how much they made from this delivery
                    + "FOREIGN KEY (driver_username) REFERENCES drivers(username),"
                    + "FOREIGN KEY (order_id) REFERENCES orders(order_id)"
                    + ")");

            // schedule table for when drivers want to work (not really using this much yet)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS driver_schedule ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "driver_username TEXT NOT NULL,"
                    + "day_of_week INTEGER,"  // 1 = sunday, 7 = saturday
                    + "start_time TEXT,"
                    + "end_time TEXT,"
                    + "is_active BOOLEAN DEFAULT 1,"
                    + "FOREIGN KEY (driver_username) REFERENCES drivers(username)"
                    + ")");

            // just adding a couple indexes for the main queries we do
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_drivers_status ON drivers(current_status)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_delivery_history_driver ON delivery_history(driver_username)");
        }
    }

    // adds a new driver to the system
    public void registerDriver(String username, String vehicleType, String licenseNumber,
                             String serviceArea) throws SQLException {
        String sql = "INSERT INTO drivers (username, vehicle_type, license_number, service_area, "
                  + "current_status, account_status, joined_date) "
                  + "VALUES (?, ?, ?, ?, 'OFFLINE', 'ACTIVE', ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, vehicleType);
            ps.setString(3, licenseNumber);
            ps.setString(4, serviceArea);
            ps.setLong(5, Instant.now().getEpochSecond());
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.catchAndLogBug(e, "DriverDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while registering driver:\n" +
                e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    // change driver status (AVAILABLE, ON_DELIVERY, or OFFLINE)
    public void updateDriverStatus(String username, String status) throws SQLException {
        String sql = "UPDATE drivers SET current_status = ? WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.catchAndLogBug(e, "DriverDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while updating driver status:\n" +
                e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    /*
     * saves a delivery to the drivers history
     * also increments their total delivery count if it was successful
     */
    public void recordDelivery(String username, long orderId, long pickupTime,
                             long deliveryTime, String status) throws SQLException {
        String sql = "INSERT INTO delivery_history (driver_username, order_id, pickup_time, "
                  + "delivery_time, delivery_status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setLong(2, orderId);
            ps.setLong(3, pickupTime);
            ps.setLong(4, deliveryTime);
            ps.setString(5, status);
            ps.executeUpdate();

            // if delivery was successful add to their total count
            if ("DELIVERED".equals(status)) {
                sql = "UPDATE drivers SET total_deliveries = total_deliveries + 1 WHERE username = ?";
                try (PreparedStatement ps2 = conn.prepareStatement(sql)) {
                    ps2.setString(1, username);
                    ps2.executeUpdate();
                }
            }
        } catch (SQLException e) {
            Logger.catchAndLogBug(e, "DriverDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while saving delivery to driver history:\n" +
                e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    // when a customer rates a driver we need to update 2 things:
    // 1. save the rating/feedback to that specific delivery
    // 2. recalculate the drivers overall average rating
    public void updateRating(String username, long orderId, int rating, String feedback) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url)) {
            // first update the specific delivery record
            String sql1 = "UPDATE delivery_history SET customer_rating = ?, customer_feedback = ? "
                      + "WHERE driver_username = ? AND order_id = ?";
            try (PreparedStatement ps1 = conn.prepareStatement(sql1)) {
                ps1.setInt(1, rating);
                ps1.setString(2, feedback);
                ps1.setString(3, username);
                ps1.setLong(4, orderId);
                ps1.executeUpdate();
            }

            // then update their average rating by calculating from all their deliveries
            String sql2 = "UPDATE drivers SET rating = (SELECT AVG(customer_rating) FROM delivery_history "
                + "WHERE driver_username = ? AND customer_rating IS NOT NULL) WHERE username = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                ps2.setString(1, username);
                ps2.setString(2, username);
                ps2.executeUpdate();
            }
        } catch (SQLException e) {
            Logger.catchAndLogBug(e, "DriverDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while updating driver rating:\n" +
                e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    // lets drivers set their work schedule
    public void setSchedule(String username, int dayOfWeek, String startTime,
                          String endTime) throws SQLException {
        String sql = "INSERT INTO driver_schedule (driver_username, day_of_week, start_time, end_time) "
                  + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, dayOfWeek);
            ps.setString(3, startTime);
            ps.setString(4, endTime);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.catchAndLogBug(e, "DriverDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while setting driver schedule:\n" +
                e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    // get all past deliveries for a driver
    // joins with orders table to get restaurant info too
    // NOTE: whoever calls this needs to close the ResultSet when done
    public ResultSet getDeliveryHistory(String username) throws SQLException {
        String sql = "SELECT dh.*, o.restaurant_name, o.total_amount "
                  + "FROM delivery_history dh "
                  + "JOIN orders o ON dh.order_id = o.order_id "
                  + "WHERE driver_username = ? "
                  + "ORDER BY delivery_time DESC";  // newest first
        Connection conn = DriverManager.getConnection(url);
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        return ps.executeQuery();
    }

    // gets stats for a driver - total orders, average rating, total earnings
    // using subqueries here which might be slow with lots of data but works for now
    public ResultSet getDriverStats(String username) throws SQLException {
        String sql = "SELECT d.*, "
                  + "(SELECT COUNT(*) FROM delivery_history WHERE driver_username = d.username) as total_orders, "
                  + "(SELECT AVG(customer_rating) FROM delivery_history WHERE driver_username = d.username) as avg_rating, "
                  + "(SELECT SUM(earnings) FROM delivery_history WHERE driver_username = d.username) as total_earnings "
                  + "FROM drivers d WHERE d.username = ?";
        Connection conn = DriverManager.getConnection(url);
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        return ps.executeQuery();
    }
}