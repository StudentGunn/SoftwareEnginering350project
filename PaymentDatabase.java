import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import javax.swing.JOptionPane;

// handles payment stuff for users - both credit cards and bank accounts
// took forever to get the table setup right with all the foreign keys
public class PaymentDatabase {
    private final Path dbPath;
    private final String url;

    public PaymentDatabase(Path dbPath) {
        this.dbPath = dbPath;
        this.url = "jdbc:sqlite:" + dbPath.toAbsolutePath().toString();
    }

    public String getConnectionUrl() {
        return url;
    }
    // sets up the payment tables when app starts
    public void init() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            Logger.catchAndLogBug(e, "PaymentDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while initializing the payment database:\n" +
                e.getMessage(), "Database Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            throw new SQLException("SQLite JDBC driver not found on classpath", e);
        }

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            // sqlite doesnt enforce foreign keys by default apparently... had to enable this
            stmt.executeUpdate("PRAGMA foreign_keys = ON");

            // main table for storing payment info
            // can handle either card payments OR bank payments
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS payment_methods ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "username TEXT NOT NULL,"
                    + "payment_type TEXT NOT NULL,"  // either CARD or BANK
                    + "card_number TEXT,"
                    + "card_expiry TEXT,"
                    + "card_name TEXT," 
                    + "bank_routing TEXT,"
                    + "bank_account TEXT,"
                    + "bank_name TEXT,"
                    + "created_at INTEGER NOT NULL,"
                    + "is_active BOOLEAN DEFAULT 1,"  // so users can turn off old payment methods
                    + "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "CHECK (payment_type IN ('CARD', 'BANK')), "
                    // the big check constraint below makes sure card payments have card info and bank payments have bank info
                    + "CHECK ((payment_type = 'CARD' AND card_number IS NOT NULL AND card_expiry IS NOT NULL AND card_name IS NOT NULL) OR "
                    + "(payment_type = 'BANK' AND bank_routing IS NOT NULL AND bank_account IS NOT NULL AND bank_name IS NOT NULL))"
                    + ")");

            // keeps track of all payment transactions
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS payment_transactions ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "payment_method_id INTEGER NOT NULL,"
                    + "order_id INTEGER,"  // can be null if its not for an order
                    + "amount DECIMAL(10,2) NOT NULL,"
                    + "status TEXT NOT NULL,"
                    + "created_at INTEGER,"
                    + "completed_at INTEGER,"
                    + "error_message TEXT,"  // stores why payment failed if it does
                    + "FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id),"
                    + "FOREIGN KEY (order_id) REFERENCES orders(id)"
                    + ")");

            // adding indexes so the queries dont take forever when theres lots of data
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_payment_methods_user ON payment_methods(username)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_payment_trans_order ON payment_transactions(order_id)");
        }
    }

    // adds a credit card to someones account, returns the id we assigned it
    public long addCardPayment(String username, String cardNumber, String cardExpiry, String cardName) throws SQLException {
        String sql = "INSERT INTO payment_methods (username, payment_type, card_number, card_expiry, card_name, created_at) "
                  + "VALUES (?, 'CARD', ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, cardNumber);
            ps.setString(3, cardExpiry);
            ps.setString(4, cardName);
            ps.setLong(5, Instant.now().getEpochSecond());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to retrieve generated payment method ID");
            }
        } catch (SQLException e) {
            Logger.catchAndLogBug(e, "PaymentDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while adding card payment:\n" +
                e.getMessage(), "Database Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    // same as addCardPayment but for bank accounts instead
    public long addBankPayment(String username, String routingNumber, String accountNumber, String bankName) throws SQLException {
        String sql = "INSERT INTO payment_methods (username, payment_type, bank_routing, bank_account, bank_name, created_at) "
                  + "VALUES (?, 'BANK', ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, routingNumber);
            ps.setString(3, accountNumber);
            ps.setString(4, bankName);
            ps.setLong(5, Instant.now().getEpochSecond());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to retrieve generated payment method ID");
            }
        } catch (SQLException e) {
            Logger.catchAndLogBug(e, "PaymentDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while adding bank payment:\n" +
                e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    /*
     * grabs whatever payment method the user has active right now
     * returns null if they dont have one set up yet
     */
    public PaymentInformation getActivePaymentMethod(String username) throws SQLException {
        String sql = "SELECT payment_type, card_number, card_expiry, card_name, "
                  + "bank_routing, bank_account, bank_name FROM payment_methods "
                  + "WHERE username = ? AND is_active = 1 ORDER BY id DESC LIMIT 1";  // gets most recent one
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PaymentInformation info = new PaymentInformation();
                    info.setPaymentType(rs.getString("payment_type"));
                    // fill in the right fields depending on if its a card or bank account
                    if ("CARD".equals(info.getPaymentType())) {
                        info.setCardNumber(rs.getString("card_number"));
                        info.setCardExpiry(rs.getString("card_expiry"));
                        info.setCardName(rs.getString("card_name"));
                    } else if ("BANK".equals(info.getPaymentType())) {
                        info.setRoutingNumber(rs.getString("bank_routing"));
                        info.setAccountNumber(rs.getString("bank_account"));
                        info.setBankName(rs.getString("bank_name"));
                    }
                    return info;
                }
                return null;  // no payment method found
            }
        } catch (SQLException e) {
            Logger.catchAndLogBug(e, "PaymentDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while getting active payment method:\n" +
                e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    /*
    --> Connects to the database and retrieves the most recent active payment method ID for the specified user.
    --> Asks for username as parameter to identify the user.
    --> Connects to to payment database.
    --> Return the id of the most-recent active payment method for a user, or null if none.
    --> create coloum in payment_methods table to store is_active boolean
     */
    public Long getActivePaymentMethodId(String username) throws SQLException {
        String sql = "SELECT id FROM payment_methods WHERE username = ? AND is_active = 1 ORDER BY id DESC LIMIT 1";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
                return null;
            }
        } catch (SQLException e) {
            Logger.catchAndLogBug(e, "PaymentDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while getting payment method ID:\n" +
                e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    // turns off all payment methods for a user (like if theyre switching to a new one)
    public void deactivateAllPaymentMethods(String username) throws SQLException {
        String sql = "UPDATE payment_methods SET is_active = 0 WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.catchAndLogBug(e, "PaymentDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while deactivating payment methods:\n" +
                e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    // creates a transaction record when someone tries to pay
    // orderId can be null if its not related to an order
    public long createTransaction(long paymentMethodId, Long orderId, double amount) throws SQLException {
        String sql = "INSERT INTO payment_transactions (payment_method_id, order_id, amount, status, created_at) "
                  + "VALUES (?, ?, ?, 'PENDING', ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, paymentMethodId);
            // this was annoying to figure out - have to handle null orderIds differently
            if (orderId != null) {
                ps.setLong(2, orderId);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setDouble(3, amount);
            ps.setLong(4, Instant.now().getEpochSecond());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to retrieve generated transaction ID");
            }
        } catch (SQLException e) {
            Logger.catchAndLogBug(e, "PaymentDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while creating transaction:\n" +
                e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    // updates transaction after payment goes through (or fails)
    public void updateTransactionStatus(long transactionId, String status, String errorMessage) throws SQLException {
        String sql = "UPDATE payment_transactions SET status = ?, completed_at = ?, error_message = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, Instant.now().getEpochSecond());
            ps.setString(3, errorMessage);
            ps.setLong(4, transactionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.catchAndLogBug(e, "PaymentDatabase");
            JOptionPane.showMessageDialog(null, "An error occurred while updating transaction status:\n" +
                e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }
}