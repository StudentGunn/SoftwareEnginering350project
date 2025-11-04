import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

public class UserDataBase {
    private final Path dbPath;
    private final String url;

    public UserDataBase(Path dbPath) {
        this.dbPath = dbPath;
        this.url = "jdbc:sqlite:" + dbPath.toAbsolutePath().toString();
    }

    public String getConnectionUrl() {
        return url;
    }

    /* Create the users table if it doesn't exist. 
     * creates method init
     * trys to connect to the database
     * creates the users table with the following columns: username, password_hash, full_name, email, created_at
     * if the table already exists, it does nothing
     * throws SQLException if there is an error connecting to the database or creating the table
     * returns void
    */
    public void init() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found on classpath. Please add the sqlite-jdbc jar to your classpath.", e);
        }

        try (Connection c = DriverManager.getConnection(url);
             Statement s = c.createStatement()) {
            
            // Create the users table if it doesn't exist
            s.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                    + "username TEXT PRIMARY KEY,"
                    + "password_hash TEXT NOT NULL,"
                    + "user_type TEXT DEFAULT 'CUSTOMER',"
                    + "full_name TEXT,"
                    + "email TEXT,"
                    + "phone TEXT,"
                    + "admin_hash TEXT,"
                    + "created_at INTEGER"
                    + ")");
            
            // Handle migration: Add user_type column if it doesn't exist (for existing databases)
            try {
                s.executeUpdate("ALTER TABLE users ADD COLUMN user_type TEXT DEFAULT 'CUSTOMER'");
                System.out.println("Added user_type column to existing users table");
            } catch (SQLException e) {
                // Column probably already exists, which is fine
                if (!e.getMessage().contains("duplicate column name")) {
                    throw e;
                }
            }
            
            // Handle migration: Add phone column if it doesn't exist
            try {
                s.executeUpdate("ALTER TABLE users ADD COLUMN phone TEXT");
                System.out.println("Added phone column to existing users table");
            } catch (SQLException e) {
                // Column probably already exists, which is fine
                if (!e.getMessage().contains("duplicate column name")) {
                    throw e;
                }
            }
            
            // Handle migration: Add admin_hash column if it doesn't exist
            try {
                s.executeUpdate("ALTER TABLE users ADD COLUMN admin_hash TEXT");
                System.out.println("Added admin_hash column to existing users table");
            } catch (SQLException e) {
                // Column probably already exists, which is fine
                if (!e.getMessage().contains("duplicate column name")) {
                    throw e;
                }
            }
            
            // Add performance indexes
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_users_type ON users(user_type)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)");
        }
    }

    /* Insert a new user. Returns true on success. 
     * creates method register
     * trys to connect to the database
     * prepares an SQL statement to insert a new user into the users table
     *  throws SQLException if there is an error connecting to the database or executing the statement
     * returns true if the user was successfully inserted
    */
    public boolean register(String username, String passwordHash) throws SQLException {
        return register(username, passwordHash, "CUSTOMER", null, null, null);
    }

    public boolean register(String username, String passwordHash, String userType, 
                          String fullName, String email, String phone) throws SQLException {
        String sql = "INSERT INTO users(username,password_hash,user_type,full_name,email,phone,created_at) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.setString(2, passwordHash);
            p.setString(3, userType);
            p.setString(4, fullName);
            p.setString(5, email);
            p.setString(6, phone);
            p.setLong(7, Instant.now().getEpochSecond());
            p.executeUpdate();
            return true;
        }
    }

    /*Return true when passwordHash matches stored value. 
     * creates method authenticate
     * trys to connect to the database
     * prepares an SQL statement to select the password_hash from the users table where the username matches
     * throws SQLException if there is an error connecting to the database or executing the statement
     *  returns true if the provided passwordHash matches the stored password_hash for the given username
    */
    public boolean authenticate(String username, String passwordHash) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString(1);
                    return stored != null && stored.equals(passwordHash);
                }
                return false;
            }
        }
    }

    /* Return true if the username exists. 
     * creates method userExists
     * trys to connect to the database
     * prepares an SQL statement to select from the users table where the username matches
     * throws SQLException if there is an error connecting to the database or executing the statement
     * returns true if the username exists in the database
    */
    public boolean userExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next();
            }
        }
    }

    /* Get user type for a username */
    public String getUserType(String username) throws SQLException {
        String sql = "SELECT user_type FROM users WHERE username = ?";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return null;
            }
        }
    }

    /* Verify admin hash code */
    public boolean verifyAdminHash(String username, String hashCode) throws SQLException {
        String sql = "SELECT admin_hash FROM users WHERE username = ?";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString(1);
                    return stored != null && stored.equals(hashCode);
                }
                return false;
            }
        }
    }

    /* Initialize admin account if it doesn't exist */
    public void initializeAdmin(String adminUsername, String adminPassword, String adminHashCode) throws SQLException {
        if (!userExists(adminUsername)) {
            // Register admin with ADMIN user type
            register(adminUsername, adminPassword, "ADMIN", "System Administrator", "admin@fooddelivery.com", "000-000-0000");
            
            // Set the admin hash code
            String sql = "UPDATE users SET admin_hash = ? WHERE username = ?";
            try (Connection c = DriverManager.getConnection(url);
                 PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, adminHashCode);
                p.setString(2, adminUsername);
                p.executeUpdate();
            }
            // Note: In production, use proper logging framework instead of System.out
            // System.out.println("Admin account created: " + adminUsername);
        }
    }
}
