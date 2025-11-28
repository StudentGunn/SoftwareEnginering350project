import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.*;


 // Main UI class - handles the app window and scene switching.
 //  Uses SceneSorter to swap between different screens (login, customer, driver, admin).
 
public class FoodDeliveryLoginUI {

    // UI components
    private final JPanel main = new JPanel(new BorderLayout(10, 10));
    private final JFrame frame = new JFrame("Food Delivery Service");
    private final SceneSorter sceneSorter = new SceneSorter();
    public final JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);

    // database connections
    public UserDataBase userDb;
    public PaymentDatabase paymentDb;
    public DriverDatabase driverDb;
    public OrderDatabase orderDb;

    public SceneSorter getSceneSorter() {
        return sceneSorter;
    }

    // Creates and shows the main window 
    public void createAndShow() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(420, 260));

        // sets background color
        main.setBackground(Color.decode("#f7f9fc"));

        // build login screen
        LoginUI log = new LoginUI(this);
        sceneSorter.addScene("Login", log.buildLoginPanel());

        // layout
        main.add(sceneSorter.getCardsPanel(), BorderLayout.CENTER);
        main.add(messageLabel, BorderLayout.SOUTH);
        sceneSorter.switchPage("Login");

        frame.setContentPane(main);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Shows a notification message - auto disapears 
    public void showNotification(String message, Color bg, Color fg, int durationMs) {
        messageLabel.setText(message);
        messageLabel.setOpaque(true);
        if (bg != null) messageLabel.setBackground(bg);
        if (fg != null) messageLabel.setForeground(fg);

        Timer timer = new Timer(Math.max(1000, durationMs), e -> {
            messageLabel.setText(" ");
            messageLabel.setOpaque(false);
            messageLabel.repaint();
        });
        timer.setRepeats(false);
        timer.start();
    }

    // Closes the window 
    public void closeWindow() {
        if (frame != null) {
            frame.dispose();
        }
    }

    // Hashes the password using SHA-256 
    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b & 0xff));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
