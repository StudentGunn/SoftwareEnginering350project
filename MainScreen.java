// MainScreen.java
import java.awt.*;
import java.sql.SQLException;
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
        JButton paymentBtn = new JButton("Payment Methods");
        
        // Set preferred size for consistency
        orderBtn.setMaximumSize(new Dimension(200, 40));
        orderBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        zipBtn.setMaximumSize(new Dimension(200, 40));
        zipBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        paymentBtn.setMaximumSize(new Dimension(200, 40));
        paymentBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add spacing between buttons * avoid messing alignment
        leftPanel.add(orderBtn);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(zipBtn);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(paymentBtn);

        // Add the left panel to a more concise layout for alignment
        JPanel leftWrapper = new JPanel(new BorderLayout());
        leftWrapper.add(leftPanel, BorderLayout.WEST);
        add(leftWrapper, BorderLayout.CENTER);

        // Actions
        orderBtn.addActionListener(e -> openOrderScreen());
        zipBtn.addActionListener(e -> promptZipCode());
        profileBtn.addActionListener(e -> openProfileDialog());
        paymentBtn.addActionListener(e -> openPaymentMethodDialog());
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

    /*
     * Opens a dialog for managing payment methods.
     * Allows customers to add credit card or bank account payment methods.
     * Payment information is stored securely in the payments.db database.
     */
    private void openPaymentMethodDialog() {
        try {
            // First, check if there's an active payment method
            PaymentInformation activeMethod = parent.paymentDb.getActivePaymentMethod(username);
            
            String currentMethod = "No payment method on file";
            if (activeMethod != null) {
                if ("CARD".equals(activeMethod.getPaymentType())) {
                    // Mask card number for security
                    String cardNum = activeMethod.getCardNumber();
                    String masked = "****-****-****-" + (cardNum.length() >= 4 ? cardNum.substring(cardNum.length() - 4) : cardNum);
                    currentMethod = "Credit Card: " + masked + " (Exp: " + activeMethod.getCardExpiry() + ")";
                } else if ("BANK".equals(activeMethod.getPaymentType())) {
                    String acctNum = activeMethod.getAccountNumber();
                    String masked = "****" + (acctNum.length() >= 4 ? acctNum.substring(acctNum.length() - 4) : acctNum);
                    currentMethod = "Bank Account: " + masked + " (" + activeMethod.getBankName() + ")";
                }
            }
            
            // Show dialog with options
            String message = "Current Payment Method:\n" + currentMethod + "\n\nWhat would you like to do?";
            int option = JOptionPane.showOptionDialog(SwingUtilities.getWindowAncestor(this),
                    message,
                    "Payment Methods",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new Object[] { "Add Credit Card", "Add Bank Account", "Close" },
                    "Close");

            if (option == 0) {
                addCreditCard();
            } else if (option == 1) {
                addBankAccount();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error accessing payment database: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /*
     * Prompts user to enter credit card information and saves it to the database.
     */
    private void addCreditCard() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField cardNumberField = new JTextField(16);
        JTextField expiryField = new JTextField(5);
        JTextField nameField = new JTextField(20);
        
        panel.add(new JLabel("Card Number:"));
        panel.add(cardNumberField);
        panel.add(new JLabel("Expiry (MM/YY):"));
        panel.add(expiryField);
        panel.add(new JLabel("Cardholder Name:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(this),
                panel, "Add Credit Card", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String cardNumber = cardNumberField.getText().trim();
            String expiry = expiryField.getText().trim();
            String name = nameField.getText().trim();

            // Basic validation
            if (cardNumber.isEmpty() || expiry.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!cardNumber.matches("\\d{13,19}")) {
                JOptionPane.showMessageDialog(this, "Invalid card number. Must be 13-19 digits.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate card number using Luhn algorithm
            if (!isValidCardNumber(cardNumber)) {
                JOptionPane.showMessageDialog(this, "Invalid card number. Failed checksum validation.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!expiry.matches("\\d{2}/\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Invalid expiry format. Use MM/YY.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Deactivate existing payment methods and add new one
                parent.paymentDb.deactivateAllPaymentMethods(username);
                parent.paymentDb.addCardPayment(username, cardNumber, expiry, name);
                JOptionPane.showMessageDialog(this, "Credit card added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error saving payment method: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /*
     * Validates credit card number using Luhn algorithm (mod 10 check).
     * This is the industry standard for validating credit card numbers.
     */
    private boolean isValidCardNumber(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        
        // Process digits from right to left
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (sum % 10 == 0);
    }

    /*
     * Prompts user to enter bank account information and saves it to the database.
     */
    private void addBankAccount() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField routingField = new JTextField(9);
        JTextField accountField = new JTextField(17);
        JTextField bankNameField = new JTextField(20);
        
        panel.add(new JLabel("Routing Number:"));
        panel.add(routingField);
        panel.add(new JLabel("Account Number:"));
        panel.add(accountField);
        panel.add(new JLabel("Bank Name:"));
        panel.add(bankNameField);

        int result = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(this),
                panel, "Add Bank Account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String routing = routingField.getText().trim();
            String account = accountField.getText().trim();
            String bankName = bankNameField.getText().trim();

            // Basic validation
            if (routing.isEmpty() || account.isEmpty() || bankName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!routing.matches("\\d{9}")) {
                JOptionPane.showMessageDialog(this, "Invalid routing number. Must be 9 digits.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!account.matches("\\d{4,17}")) {
                JOptionPane.showMessageDialog(this, "Invalid account number. Must be 4-17 digits.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Deactivate existing payment methods and add new one
                parent.paymentDb.deactivateAllPaymentMethods(username);
                parent.paymentDb.addBankPayment(username, routing, account, bankName);
                JOptionPane.showMessageDialog(this, "Bank account added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error saving payment method: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}