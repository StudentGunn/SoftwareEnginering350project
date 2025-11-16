import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

/*
 --> Manages payment methods and transactions in the database
 --> Creates payment_methods and payment_transactions tables if they don't exist
 --> Provides methods to add payment methods, create transactions, and update transaction status
 --> Uses foreign keys to link payment methods to users and transactions to orders
 --> Includes data integrity checks and performance indexes * Hard I hated doing this *
 --> Throws SQLException for any database errors
 */
public class PaymentDatabase {
    //Set varaibles; can't be changed
    private final Path dbPath;
    private final String url;
    //set database path and connection URL
    public PaymentDatabase(Path dbPath) {
        this.dbPath = dbPath;
        this.url = "jdbc:sqlite:" + dbPath.toAbsolutePath().toString();
    }
    // Get the connection URL for the database
    public String getConnectionUrl() {
        return url;
    }
// Initialize the database by creating necessary tables and indexes
    public void init() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC"); // Load SQLite JDBC driver
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found on classpath", e); // throw SQLException if driver not found
        }
        // Create tables and indexes
        try (Connection c = DriverManager.getConnection(url);
             Statement s = c.createStatement()) {
            
            // Enable foreign key support
            s.executeUpdate("PRAGMA foreign_keys = ON");

            // Create payment_methods table
            s.executeUpdate("CREATE TABLE IF NOT EXISTS payment_methods ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "username TEXT NOT NULL,"
                    + "payment_type TEXT NOT NULL,"  // 'CARD' or 'BANK'
                    + "card_number TEXT,"
                    + "card_expiry TEXT,"
                    + "card_name TEXT,"
                    + "bank_routing TEXT,"
                    + "bank_account TEXT,"
                    + "bank_name TEXT,"
                    + "created_at INTEGER NOT NULL,"
                    + "is_active BOOLEAN DEFAULT 1,"
                    + "last_used INTEGER,"
                    + "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "CHECK (payment_type IN ('CARD', 'BANK')),"
                    + "CHECK ((payment_type = 'CARD' AND card_number IS NOT NULL AND card_expiry IS NOT NULL AND card_name IS NOT NULL) OR"
                    + "       (payment_type = 'BANK' AND bank_routing IS NOT NULL AND bank_account IS NOT NULL AND bank_name IS NOT NULL))"
                    + ")");

            // Create payment_transactions table
            s.executeUpdate("CREATE TABLE IF NOT EXISTS payment_transactions ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "payment_method_id INTEGER NOT NULL,"
                    + "order_id INTEGER,"
                    + "amount DECIMAL(10,2) NOT NULL,"
                    + "status TEXT NOT NULL," // 'PENDING', 'COMPLETED', 'FAILED'
                    + "created_at INTEGER,"
                    + "completed_at INTEGER,"
                    + "error_message TEXT,"
                    + "FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id),"
                    + "FOREIGN KEY (order_id) REFERENCES orders(id)"
                    + ")");

            // Add performance indexes for frequently queried columns
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_payment_methods_user ON payment_methods(username)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_payment_methods_active ON payment_methods(is_active)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_payment_trans_method ON payment_transactions(payment_method_id)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_payment_trans_order ON payment_transactions(order_id)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_payment_trans_status ON payment_transactions(status)");
        }
    }

    public long addCardPayment(String username, String cardNumber, String cardExpiry, String cardName) throws SQLException {
        String sql = "INSERT INTO payment_methods (username, payment_type, card_number, card_expiry, card_name, created_at) "
                  + "VALUES (?, 'CARD', ?, ?, ?, ?)";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, username);
            p.setString(2, cardNumber);
            p.setString(3, cardExpiry);
            p.setString(4, cardName);
            p.setLong(5, Instant.now().getEpochSecond());
            p.executeUpdate();
            
            try (ResultSet rs = p.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to retrieve generated payment method ID");
            }
        }
    }
    /*
     --> Adds a bank payment method for the given username
        --> Returns the generated payment method ID
        --> generates a new payment method entry in the payment_methods table with type 'BANK'
        -->throws SQLException for any database errors
     */
    public long addBankPayment(String username, String routingNumber, String accountNumber, String bankName) throws SQLException {
        String sql = "INSERT INTO payment_methods (username, payment_type, bank_routing, bank_account, bank_name, created_at) "
                  + "VALUES (?, 'BANK', ?, ?, ?, ?)";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, username);
            p.setString(2, routingNumber);
            p.setString(3, accountNumber);
            p.setString(4, bankName);
            p.setLong(5, Instant.now().getEpochSecond());
            p.executeUpdate();
            
            try (ResultSet rs = p.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to retrieve generated payment method ID");
            }
        }
    }
    
    /*
     --> Retrieves the active payment method for the given username
        --> Returns a PaymentInformation object with the payment details, or null if none found
        --> throws SQLException for any database errors
     */
    public PaymentInformation getActivePaymentMethod(String username) throws SQLException {
        String sql = "SELECT payment_type, card_number, card_expiry, card_name, "
                  + "bank_routing, bank_account, bank_name FROM payment_methods "
                  + "WHERE username = ? AND is_active = 1 ORDER BY id DESC LIMIT 1";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    PaymentInformation info = new PaymentInformation();
                    info.setPaymentType(rs.getString("payment_type"));
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
                return null;
            }
        }
    }
    
    /*
     --> Deactivates all payment methods for the given username
        --> throws SQLException for any database errors
     */
    public void deactivateAllPaymentMethods(String username) throws SQLException {
        String sql = "UPDATE payment_methods SET is_active = 0 WHERE username = ?";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.executeUpdate();
        }
    }
    /*
     --> Creates a new payment transaction
        --> Returns the generated transaction ID
        --> throws SQLException for any database errors
     */
    public long createTransaction(long paymentMethodId, Long orderId, double amount) throws SQLException {
        String sql = "INSERT INTO payment_transactions (payment_method_id, order_id, amount, status, created_at) "
                  + "VALUES (?, ?, ?, 'PENDING', ?)";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setLong(1, paymentMethodId);
            if (orderId != null) {
                p.setLong(2, orderId);
            } else {
                p.setNull(2, java.sql.Types.INTEGER);
            }
            p.setDouble(3, amount);
            p.setLong(4, Instant.now().getEpochSecond());
            p.executeUpdate();
            
            try (ResultSet rs = p.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to retrieve generated transaction ID");
            }
        }
    }
    /*
     --> Updates the status of a payment transaction
        --> Sets the completed_at timestamp and error message if provided
        --> throws SQLException for any database errors
     */
    public void updateTransactionStatus(long transactionId, String status, String errorMessage) throws SQLException {
        String sql = "UPDATE payment_transactions SET status = ?, completed_at = ?, error_message = ? WHERE id = ?";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, status);
            p.setLong(2, Instant.now().getEpochSecond());
            p.setString(3, errorMessage);
            p.setLong(4, transactionId);
            p.executeUpdate();
        }
    }
}