import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

public class IntegrationDriverTest {

    private static Path userDbPath;
    private static Path driverDbPath;
    private static Path orderDbPath;

    private UserDataBase userDb;
    private DriverDatabase driverDb;
    private OrderDatabase orderDb;

    @BeforeAll
    static void setup() throws Exception {
        userDbPath = Files.createTempFile("test-users", ".db");
        driverDbPath = Files.createTempFile("test-drivers", ".db");
        orderDbPath = Files.createTempFile("test-orders", ".db");
    }

    @AfterAll
    static void cleanup() throws Exception {
        Files.deleteIfExists(userDbPath);
        Files.deleteIfExists(driverDbPath);
        Files.deleteIfExists(orderDbPath);
    }

    @BeforeEach
    void init() throws Exception {
        userDb = new UserDataBase(userDbPath);
        userDb.init();

        driverDb = new DriverDatabase(driverDbPath);
        driverDb.init();

        orderDb = new OrderDatabase(orderDbPath);
        orderDb.init();
    }

    // tests full driver flow: register -> login -> accept order -> complete delivery
    @Test
    void testDriverDeliveryFlow() throws SQLException {
        String username = "testdriver";
        String password = "driver123";

        // register user account
        boolean registered = userDb.register(username, password, "DRIVER", "Jane Smith", "jane@email.com", "555-5678");
        assertTrue(registered);

        // register driver info
        driverDb.registerDriver(username, "Car", "DL12345", "Downtown");

        // login
        boolean loggedIn = userDb.authenticate(username, password);
        assertTrue(loggedIn);

        // check user type
        String userType = userDb.getUserType(username);
        assertEquals("DRIVER", userType);

        // create a test order (normally customer does this)
        long orderId = orderDb.createOrder("somecustomer", "Burger Joint", "100 Food St", "200 Customer Rd", "none", 15.99, 1, "CARD",20.10294,60.120,69.420,42.560);
        assertTrue(orderId > 0);

        // driver accepts order
        orderDb.assignDriver(orderId, username);

        // driver updates status to picked up
        orderDb.updateOrderStatus(orderId, "IN_PROGRESS", username);

        // driver updates status to delivered
        orderDb.updateOrderStatus(orderId, "DELIVERED", username);

        // record delivery in driver history
        long now = java.time.Instant.now().getEpochSecond();
        driverDb.recordDelivery(username, orderId, now - 1800, now, "DELIVERED");

        System.out.println("Driver flow test passed - Order ID: " + orderId);
    }
}
