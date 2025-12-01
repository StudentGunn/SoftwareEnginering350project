import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

public class IntegrationCustomerTest {

    private static Path userDbPath;
    private static Path paymentDbPath;
    private static Path orderDbPath;

    private UserDataBase userDb;
    private PaymentDatabase paymentDb;
    private OrderDatabase orderDb;

    @BeforeAll
    static void setup() throws Exception {
        userDbPath = Files.createTempFile("test-users", ".db");
        paymentDbPath = Files.createTempFile("test-payments", ".db");
        orderDbPath = Files.createTempFile("test-orders", ".db");
    }

    @AfterAll
    static void cleanup() throws Exception {
        Files.deleteIfExists(userDbPath);
        Files.deleteIfExists(paymentDbPath);
        Files.deleteIfExists(orderDbPath);
    }

    @BeforeEach
    void init() throws Exception {
        userDb = new UserDataBase(userDbPath);
        userDb.init();

        paymentDb = new PaymentDatabase(paymentDbPath);
        paymentDb.init();

        orderDb = new OrderDatabase(orderDbPath);
        orderDb.init();
    }

    // does the tests for full customer flow: 
    // register -> login -> add payment -> place order
    @Test
    void testCustomerOrderFlow() throws SQLException {
        String username = "testcustomer";
        String password = "pass123";

        // register
        boolean registered = userDb.register(username, password, "CUSTOMER", "John Doe", "john@email.com", "555-1234");
        assertTrue(registered);

        // login
        boolean loggedIn = userDb.authenticate(username, password);
        assertTrue(loggedIn);

        // check user type
        String userType = userDb.getUserType(username);
        assertEquals("CUSTOMER", userType);

        // add payment
        long paymentId = paymentDb.addCardPayment(username, "4111111111111111", "12/25", "John Doe");
        assertTrue(paymentId > 0);

        // verify payment exists
        Long activePayment = paymentDb.getActivePaymentMethodId(username);
        assertNotNull(activePayment);

        // place order
        long orderId = orderDb.createOrder(username, "Pizza Place", "123 Main St", "456 Home St", "none", 19.99, 2, "CARD",20.10294,60.120,69.420,42.560);
        assertTrue(orderId > 0);

        // add items
        orderDb.addOrderItem(orderId, "Pizza", 1, 14.99, null);
        orderDb.addOrderItem(orderId, "Soda", 1, 2.99, null);

        System.out.println("Customer flow test passed - Order ID: " + orderId);
    }
}
