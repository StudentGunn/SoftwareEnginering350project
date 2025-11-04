// MainScreen.java
import java.awt.*;
import javax.swing.*;
/*
 * MainScreen.java
 * This class represents the main screen of the food delivery application.
 * It displays the user's profile information and provides buttons for
 * ordering food and entering a zip code.
 */
public class MainScreen extends JPanel {
    private String username;
    private String zipCode = "";
    private String email = "you@example.com"; // placeholder profile field
    private FoodDeliveryLoginUI parent;

    public MainScreen(FoodDeliveryLoginUI parent, String username) {
        this.parent = parent;
        this.username = (username == null || username.isEmpty()) ? "User" : username;
        initUI();
    }
    /*
     * Initializes the user interface components.
     * Sets up the layout and adds all necessary components to the panel.
     * Connects action listeners to the buttons.
     * Initializes any other required settings, like default values
     * Sets the initial state of the UI components.
     */

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // Top panel for welcome message and profile button
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!", SwingConstants.LEFT);
        welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(18f));
        
        // Create a smaller profile button
        JButton profileBtn = new JButton("My Profile");
        profileBtn.setPreferredSize(new Dimension(100, 30));
        
        // Add welcome label and profile button to top panel
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(profileBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Create left side panel for Order and Zip Code buttons
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        //Create buttons
        JButton orderBtn = new JButton("Order");
        JButton zipBtn = new JButton("Enter Zip Code");
        
        // Set preferred size for consistency
        orderBtn.setMaximumSize(new Dimension(200, 40));
        orderBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        zipBtn.setMaximumSize(new Dimension(200, 40));
        zipBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add spacing between buttons * avoid messing alignment
        leftPanel.add(orderBtn);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(zipBtn);

        // Add the left panel to a more concise layout for alignment
        JPanel leftWrapper = new JPanel(new BorderLayout());
        leftWrapper.add(leftPanel, BorderLayout.WEST);
        add(leftWrapper, BorderLayout.CENTER);

        // Actions
        orderBtn.addActionListener(e -> openOrderScreen());
        zipBtn.addActionListener(e -> promptZipCode());
        profileBtn.addActionListener(e -> openProfileDialog());
    }
    /*
     * Opens the order screen if zip code is set; otherwise prompts for zip code.
     * If the zip code is valid, it proceeds to the restaurant screen.
     * if not, it shows a warning message.
     */
    private void openOrderScreen() {
        if (zipCode.isEmpty()) {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                    "Please enter your zip code first to see available restaurants.",
                    "Zip Code Required", JOptionPane.WARNING_MESSAGE);
            promptZipCode();
            return;
        }
        
        // Create and show the restaurant screen
        ResturantScreen restaurantScreen = new ResturantScreen(parent, username, zipCode);
        parent.getSceneSorter().addScene("RestaurantScreen", restaurantScreen);
        parent.getSceneSorter().switchPage("RestaurantScreen");
    }

    private void promptZipCode() {
        String input = JOptionPane.showInputDialog(SwingUtilities.getWindowAncestor(this),
                "Enter your 5-digit zip code:", zipCode.isEmpty() ? "" : zipCode);
        if (input != null) {
            input = input.trim();
            if (input.matches("\\d{5}")) {
                zipCode = input;
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                        "Zip code set to: " + zipCode,
                        "Zip Code Saved", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                        "Please enter a valid 5-digit zip code.",
                        "Invalid Zip", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    /*
     * Opens the profile dialog displaying user information.
     * Allows editing of email address.
     * Shows options to edit email or close the the window.
     * If the email is changed, it updates the email variable, so new email is reflected next time.
     */
    private void openProfileDialog() {
        String message = String.format("Username: %s%nEmail: %s%nZip Code: %s",
                username, email, zipCode.isEmpty() ? "(not set)" : zipCode);
        int option = JOptionPane.showOptionDialog(SwingUtilities.getWindowAncestor(this),
                message,
                "My Profile",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[] { "Edit Email", "Close" },
                "Close");

        if (option == 0) {
            String newEmail = JOptionPane.showInputDialog(this, "Enter new email:", email);
            if (newEmail != null && !newEmail.trim().isEmpty()) {
                email = newEmail.trim();
                JOptionPane.showMessageDialog(this, "Email updated.", "Profile", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

}