import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

public class IntegrationAdminTest {

    private static Path userDbPath;
    private static Path orderDbPath;

    private UserDataBase userDb;
    private OrderDatabase orderDb;

    @BeforeAll
    static void setup() throws Exception {
        userDbPath = Files.createTempFile("test-users", ".db");
        orderDbPath = Files.createTempFile("test-orders", ".db");
    }

    @AfterAll
    static void cleanup() throws Exception {
        Files.deleteIfExists(userDbPath);
        Files.deleteIfExists(orderDbPath);
    }

    @BeforeEach
    void init() throws Exception {
        userDb = new UserDataBase(userDbPath);
        userDb.init();

        orderDb = new OrderDatabase(orderDbPath);
        orderDb.init();
    }

    // tests admin flow: login -> view orders -> cancel order
    @Test
    void testAdminCancelOrderFlow() throws SQLException {
        String adminUser = "testadmin";
        String adminPass = "admin123";
        String adminHash = "secrethash";

        // create admin account
        userDb.initializeAdmin(adminUser, adminPass, adminHash);

        // login
        boolean loggedIn = userDb.authenticate(adminUser, adminPass);
        assertTrue(loggedIn);

        // check user type
        String userType = userDb.getUserType(adminUser);
        assertEquals("ADMIN", userType);

        // verify admin hash
        boolean hashValid = userDb.verifyAdminHash(adminUser, adminHash);
        assertTrue(hashValid);

        // create a test order
        long orderId = orderDb.createOrder("customer1", "Taco Shop", "50 Taco Ave", "75 Home St", "extra salsa", 12.50, 2, "CARD", 20.10294,60.120,69.420,42.560);
        assertTrue(orderId > 0);

        // admin cancels the order
        orderDb.cancelOrder(orderId);

        System.out.println("Admin flow test passed - Cancelled Order ID: " + orderId);
    }
}
