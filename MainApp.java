import java.sql.SQLException;
import javax.swing.*;

/**
 * MainApp is the entry point for the Food Delivery application.
 * Runs on the Event Dispatch Thread and initializes all databases and UI components.
 * Displays initialization failures to the user via dialogs.
 */
public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FoodDeliveryLoginUI app = new FoodDeliveryLoginUI();
            // initialize SQLite DB
            try {
                // Initialize all databases in the correct order due to foreign key * Will Fail without correct order* 'Tried it'
                
                // #1. First initialize UserDatabase as it's the core database "based off of Diagram"
                app.userDb = new UserDataBase(java.nio.file.Path.of("users.db"));
                app.userDb.init();
                System.out.println("User database initialized successfully");

                // Initialize the default admin account (FoodDashAdmin)
                // Default password: "admin123" (hashed with SHA-256)
                // Default hash code: "ADMIN2024"
                String adminPassword = FoodDeliveryLoginUI.sha256Hex("admin123");
                app.userDb.initializeAdmin("FoodDashAdmin", adminPassword, "ADMIN2024");
                System.out.println("Admin account initialized");

                // 2. Initialize DriverDatabase as it depends on users table " to contain drivers information, not temporary"
                app.driverDb = new DriverDatabase(java.nio.file.Path.of("drivers.db"));
                app.driverDb.init();
                System.out.println("Driver database initialized successfully");

                // #3. Initialize OrderDatabase as it depends on both users and drivers, *needs for tracking delivery & orders*
                app.orderDb = new OrderDatabase(java.nio.file.Path.of("orders.db"));
                app.orderDb.init();
                System.out.println("Order database initialized successfully");

                // #4. Initialize PaymentDatabase as it depends on users and orders *if fails everything fails*
                app.paymentDb = new PaymentDatabase(java.nio.file.Path.of("payments.db"));
                app.paymentDb.init();
                System.out.println("Payment database initialized successfully");
                
            } catch (SQLException ex) { // catch any SQL exceptions from DB init, print out failure reason
                String errorMsg = "Database initialization failed: " + ex.getMessage();
                System.err.println(errorMsg);
                JOptionPane.showMessageDialog(null, errorMsg, "Database Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            app.createAndShow();
        });
    }
}


