import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

/*
--> user database - handles registration and login stuff
--> stores usernames, password hashes, and user types (customer, driver, admin)
*/
public class UserDataBase {
    private final Path dbPath;
    private final String url;
/*
-->sets path to the database file
--> connects to the SQLite database at the given path
--> retines the database connection URL
*/
    public UserDataBase(Path dbPath) {
        this.dbPath = dbPath;
        this.url = "jdbc:sqlite:" + dbPath.toAbsolutePath().toString();
    }

    public String getConnectionUrl() {
        return url;
    }

    /*
    --> sets up the users table and handles migrations *from older database versions* * Updated refeactor methods*
    --> loads the JDBC driver
    --> creates indexes on frequently queried columns
    --> migrates the users table if needed
    --> create UsersTable 
    */ 
    public void init() throws SQLException {
        loadJDBCDriver();
        createUsersTable();
        migrateUsersTable();
        createIndexes();
    }
/*  
--> Refactored old loadJDBCDriver method, into separate method *Readabiltiy*
--> loads the SQLite JDBC driver
--> throws SQLException if the driver is not found
*/
    private void loadJDBCDriver() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found on classpath", e);
        }
    }
/*
--> sets up the users table if it doesn't exist
--> creates the users table with necessary columns
--> into the database
*/
    private void createUsersTable() throws SQLException {
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                    + "username TEXT PRIMARY KEY,"
                    + "password_hash TEXT NOT NULL,"
                    + "user_type TEXT DEFAULT 'CUSTOMER',"
                    + "full_name TEXT,"
                    + "email TEXT,"
                    + "phone TEXT,"
                    + "admin_hash TEXT,"
                    + "created_at INTEGER"
                    + ")");
        }
    }
/*  
--> handles migrations for older database versions
--> adds missing columns if they don't exist yet
--> also connects to the database
*/
    private void migrateUsersTable() throws SQLException {
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            addColumnIfNotExists(stmt, "user_type", "TEXT DEFAULT 'CUSTOMER'");
            addColumnIfNotExists(stmt, "phone", "TEXT");
            addColumnIfNotExists(stmt, "admin_hash", "TEXT");
        }
    }

    /*  
    --> adds a column to the users table if it doesn't already exist 
    --> used for database migrations; meaningful when updating older versions of the database
    --> catches SQL exception if column already exists
    --> And logger.CatchAndLogBug; catches other SQL exceptions and prints to bug.log
    */
    private void addColumnIfNotExists(Statement stmt, String columnName, String columnDef) {
        try {
            stmt.executeUpdate("ALTER TABLE users ADD COLUMN " + columnName + " " + columnDef);
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate column name")) {
                Logger.catchAndLogBug(e, "UserDataBase");
            }
        }
    }
    /*
    --> creates indexes on frequently queried columns for performance
    --> connects to the database    
    -->  create indexes on user_type and email columns
    */

    private void createIndexes() throws SQLException {
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_users_type ON users(user_type)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)");
        }
    }
    /* 
    --> // simple version - just registers a customer with username and password
    --> returns true if registration successful
    */
    public boolean register(String username, String passwordHash) throws SQLException {
        return register(username, passwordHash, "CUSTOMER", null, null, null);
    }
/*
--> create a new user in the database
--> can specify user type (CUSTOMER, DRIVER, ADMIN) and other info
--> sets userrname, password hash, user type, full name, email, phone, and creation timestamp
--> update in database 
--> returns true if registration successful * all values*
*/
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

   /*
    --> checks if username/password combo is correct
    --> connects to the database
    --> returns true if authentication successful
    --> return true if authentication successful
    --> returns false, if failed
    */
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

    /*
    --> checks if a username is already taken
    --> connects to the database
    --> returns true if username exists
    */
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

    /*  
    --> gets the user type (CUSTOMER, DRIVER, or ADMIN)
    --> connects to the database
    --> returns the user type or null if not found
    */
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
    /*
    --> verifies the admin hash code for extra security
    --> connects to the database
    --> returns true if hash matches, false otherwise
    --> calls the database for the admin_hash value for the given username
    */
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
    /* 
    --> creates the default admin account if it doesnt exist yet
    --> connects to the database
    --> creates the admin user with given username and password if not already present
    --> sets the admin hash code for extra security
    */
    public void initializeAdmin(String adminUsername, String adminPassword, String adminHashCode) throws SQLException {
        if (!userExists(adminUsername)) {
            createAdminUser(adminUsername, adminPassword);
            setAdminHash(adminUsername, adminHashCode);
        }
    }
    /*
    --> creates the default admin account if it doesnt exist yet
    --> connects to the database
    */
    private void createAdminUser(String adminUsername, String adminPassword) throws SQLException {
        register(adminUsername, adminPassword, "ADMIN", "System Administrator", "admin@fooddelivery.com", "000-000-0000");
    }
    /*
    --> sets the admin hash code for extra security
    --> connects to the database
    --> calls the database to update the admin_hash field for the given admin username
    --> sets the admin hash code in the database
    --> set adminUsername, adminHashCode
    --> update in database
    */
    private void setAdminHash(String adminUsername, String adminHashCode) throws SQLException {
        String sql = "UPDATE users SET admin_hash = ? WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, adminHashCode);
            ps.setString(2, adminUsername);
            ps.executeUpdate();
        }
    }
}
