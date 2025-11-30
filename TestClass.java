// Work in Progress *Sky Broke it*
import org.junit.Test;
import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;


public class TestClass {
    
    

    @Test    
    public void testOrderDB() throws SQLException{
        OrderingSystem orderingSystem = new OrderingSystem( );
        Customer bob = new Customer("bob", 0, 0, "null", null);
        int id = orderingSystem.createNewOrder(10, bob, null, 10);
        assertEquals(10, id, "id should be 10");

    }
}
class UserDataBaseTest {


    private static Path testDbPath;
    private UserDataBase db;

    @BeforeAll
    static void setupDatabaseFile() throws Exception {
        // Create a temporary database file for testing
        testDbPath = Files.createTempFile("test-users", ".db");
    }

    @AfterAll
    static void cleanupDatabaseFile() throws Exception {
        Files.deleteIfExists(testDbPath);
    }

    @BeforeEach
    void setUp() throws Exception {
        db = new UserDataBase(testDbPath);
        db.init(); // create the users table
    }

    @Test
    void testRegisterNewUser() throws SQLException {
        String username = "newuser";
        String passwordHash = "hashedpassword123";

        // Register user
        boolean registered = db.register(username, passwordHash);
        assertTrue(registered, "User should be registered successfully");

        // Verify user exists
        assertTrue(db.userExists(username), "User should exist in the database");

        // Verify correct password
        assertTrue(db.authenticate(username, passwordHash), "Password hash should match");

        // Verify wrong password
        assertFalse(db.authenticate(username, "wrongpassword"), "Wrong password hash should fail authentication");
    }

    //@Test
    void testRegisterDuplicateUserFails() throws SQLException {
        String username = "duplicateUser";
        String passwordHash = "abc123";

        assertTrue(db.register(username, passwordHash));
        
        // Attempt to register the same user again - should throw an exception due to PRIMARY KEY constraint
        assertThrows(SQLException.class, () -> {
            db.register(username, passwordHash);
        }, "Registering a duplicate user should throw SQLException");
    }
}

//import static org.junit.jupiter.api.Assertions.assertEquals;
//public class TestClass
//{

//}
