// Test file to generate fake bugs for each database to verify bug logging works- test UI Components as well adn Database connections
import java.sql.SQLException;

public class TestBugLogging {
    
    public static void main(String[] args) {
        System.out.println("Testing bug logging system...");
        System.out.println("=".repeat(80));
        
        // Test 1: OrderDatabase - test database bug logging
        testOrderDatabaseBug();
        
        // Test 2: DriverDatabase - test driver database bug logging
        testDriverDatabaseBug();
        
        // Test 3: PaymentDatabase - test payment database bug logging
        testPaymentDatabaseBug();
        
        // Test 4: UserDatabase - test user database bug logging
        testUserDatabaseBug();
        
        // Test 5: UI Components - test UI component bug logging
        testUIComponentBug();
        
        // Test 6: Manual bug report with all fields- test the logging of a manually reported bug
        testManualBugReport();
        
        System.out.println("=".repeat(80));
        System.out.println("Bug logging test complete! Check bugs.log file for results.");
    }
    
    private static void testOrderDatabaseBug() {
        try {
            throw new SQLException("Foreign key constraint failed: order_items references non-existent order_id 99999");
        } catch (SQLException e) {
            Logger.catchAndLogBug(e, "OrderDatabase.addOrderItem");
            System.out.println("Logged bug from OrderDatabase");
        }
    }
    
    private static void testDriverDatabaseBug() {
        try {
            String nullUsername = null;
            // This will cause NullPointerException
            if (nullUsername.isEmpty()) {
                System.out.println("This won't execute");
            }
        } catch (Exception e) {
            Logger.catchAndLogBug(e, "DriverDatabase.updateDriverStatus");
            System.out.println("Logged bug from DriverDatabase");
        }
    }
    
    private static void testPaymentDatabaseBug() {
        try {
            throw new SQLException("CHECK constraint failed: payment_type must be 'CARD' or 'BANK'");
        } catch (SQLException e) {
            Logger.catchAndLogBug(e, "PaymentDatabase.addCardPayment");
            System.out.println("Logged bug from PaymentDatabase");
        }
    }
    
    private static void testUserDatabaseBug() {
        try {
            throw new SQLException("UNIQUE constraint failed: users.username");
        } catch (SQLException e) {
            Logger.catchAndLogBug(e, "UserDatabase.register");
            System.out.println("Logged bug from UserDatabase");
        }
    }
    
    private static void testUIComponentBug() {
        try {
            // Simulate array index out of bounds
            int[] testArray = new int[5];
            int value = testArray[10];  // This will throw ArrayIndexOutOfBoundsException
        } catch (Exception e) {
            Logger.catchAndLogBug(e, "DriverGetOrder.refreshOrders");
            System.out.println("Logged bug from UI Component");
        }
    }
    
    private static void testManualBugReport() {
        long bugId = Logger.reportBug(
            "OrderingSystem.acceptOrder",
            "Order acceptance fails when driver is already assigned to another order",
            "1. Driver logs in\n2. Accepts order #123\n3. Attempts to accept order #456 while still on delivery\n4. System allows duplicate assignment",
            "System should prevent driver from accepting multiple orders simultaneously",
            "Driver successfully accepts both orders, causing delivery conflicts",
            "HIGH",
            "OPEN"
        );
        System.out.println("Logged manual bug report with ID: " + bugId);
    }
}
