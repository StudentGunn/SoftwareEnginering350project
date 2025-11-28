import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

// user database - handles registration and login stuff
// stores usernames, password hashes, and user types (customer, driver, admin)
// also stores address info for each user to determine distance between drivers, resturaunts, and customers.
public class UserDataBase {
    private final Path dbPath;
    private final String url;
    public Address address;

    public UserDataBase(Path dbPath) {
        this.dbPath = dbPath;
        this.url = "jdbc:sqlite:" + dbPath.toAbsolutePath().toString();
    }

    public String getConnectionUrl() {
        return url;
    }

    // sets up the users table and handles migrations for older database versions
    public void init() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found on classpath", e);
        }

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            // main users table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                    + "username TEXT PRIMARY KEY,"
                    + "password_hash TEXT NOT NULL,"
                    + "user_type TEXT DEFAULT 'CUSTOMER',"  // can be CUSTOMER, DRIVER, or ADMIN
                    + "full_name TEXT,"
                    + "email TEXT,"
                    + "phone TEXT,"
                    + "admin_hash TEXT," // Special hash code for admin verification
                    + "created_at INTEGER"
                    + ")");

            // migration stuff - adding columns if they dont exist yet
            // this way older databases still work when we add new features

            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN user_type TEXT DEFAULT 'CUSTOMER'");
            } catch (SQLException e) {
                // already exists, ignore
                if (!e.getMessage().contains("duplicate column name")) {
                    throw e;
                }
            }

            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN phone TEXT");
            } catch (SQLException e) {
                if (!e.getMessage().contains("duplicate column name")) {
                    throw e;
                }
            }

            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN admin_hash TEXT");
            } catch (SQLException e) {
                if (!e.getMessage().contains("duplicate column name")) {
                    throw e;
                }
            }

            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN address TEXT");
            } catch (SQLException e) {
                if (!e.getMessage().contains("duplicate column name")) {
                    throw e;
                }
            }

            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN zipCode TEXT");
            } catch (SQLException e) {
                if (!e.getMessage().contains("duplicate column name")) {
                    throw e;
                }
            }

            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN latitude REAL");
            } catch (SQLException e) {
                if (!e.getMessage().contains("duplicate column name")) {
                    throw e;
                }
            }

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS address ("
                    + "username TEXT PRIMARY KEY,"
                    + "street TEXT,"
                    + "city TEXT,"
                    + "state TEXT,"
                    + "zip INTEGER,"
                    + "latitude REAL,"
                    + "longitude REAL,"
                    + "FOREIGN KEY(username) REFERENCES users(username)"
                    + ")");

            // couple indexes to speed up lookups
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_users_type ON users(user_type)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)");
        }
    }

    // simple version - just registers a customer with username and password
    public boolean register(String username, String passwordHash) throws SQLException {
        return register(username, passwordHash, "CUSTOMER", null, null, null);
    }

    // full version - can register any user type with all their info
    public boolean register(String username, String passwordHash, String userType,
                          String fullName, String email, String phone) throws SQLException {
        String sql = "INSERT INTO users(username,password_hash,user_type,full_name,email,phone,created_at) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, userType);
            ps.setString(4, fullName);
            ps.setString(5, email);
            ps.setString(6, phone);
            ps.setLong(7, Instant.now().getEpochSecond());
            ps.executeUpdate();
            return true;
        }
    }

    // checks if username/password combo is correct
    public boolean authenticate(String username, String passwordHash) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString(1);
                    return stored != null && stored.equals(passwordHash);
                }
                return false;
            }
        }
    }

    //checks if a username is already taken
    public boolean userExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // gets the user type (CUSTOMER, DRIVER, or ADMIN)
    public String getUserType(String username) throws SQLException {
        String sql = "SELECT user_type FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return null;
            }
        }
    }

    // verifies the admin hash code for extra security
    public boolean verifyAdminHash(String username, String hashCode) throws SQLException {
        String sql = "SELECT admin_hash FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString(1);
                    return stored != null && stored.equals(hashCode);
                }
                return false;
            }
        }
    }

    // creates the default admin account if it doesnt exist yet
    public void initializeAdmin(String adminUsername, String adminPassword, String adminHashCode) throws SQLException {
        if (!userExists(adminUsername)) {
            register(adminUsername, adminPassword, "ADMIN", "System Administrator", "admin@fooddelivery.com", "000-000-0000");

            // set the special admin hash
            String sql = "UPDATE users SET admin_hash = ? WHERE username = ?";
            try (Connection conn = DriverManager.getConnection(url);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, adminHashCode);
                ps.setString(2, adminUsername);
                ps.executeUpdate();
            }
        }
    }
    
    public void updateUserAddress(String username, Address address) throws SQLException {
        String sql = "INSERT OR REPLACE INTO address (username, street, city, state, zip, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, address.getStreet());
            ps.setString(3, address.getCity());
            ps.setString(4, address.getState());
            ps.setString(5, address.getZip());
            ps.setDouble(6, address.getLatitude());
            ps.setDouble(7, address.getLongitude());
            ps.executeUpdate();
        }
    }

    public Address getUserAddress(String username) throws SQLException {
        String sql = "SELECT street, city, state, zip, latitude, longitude FROM address WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Address(rs.getString("street"), rs.getString("city"), rs.getString("state"), rs.getString("zip"), rs.getDouble("latitude"), rs.getDouble("longitude"));
                }
                return null;
            }
        }
    }
}
