import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;

/**
 * MainScreen represents the customer's main interface for browsing restaurants and placing orders.
 * Provides navigation to profile, order history, and restaurant selection.
 */
public class MainScreen extends JPanel {
    private String username;
    private String zipCode = "";
    private String email = "you@example.com";
    private FoodDeliveryLoginUI parent;
    private javax.swing.Timer deliveredCheckTimer;

    /**
     * Constructs a MainScreen with the given parent and username.
     * @param parent The main application UI frame.
     * @param username The logged-in customer's username.
     */
    public MainScreen(FoodDeliveryLoginUI parent, String username) {
        this.parent = parent;
        this.username = (username == null || username.isEmpty()) ? "User" : username;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 245, 245));

        // green header bar 
        JPanel headerBar = new JPanel(new BorderLayout(10, 0));
        headerBar.setBackground(new Color(46, 125, 50));
        headerBar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel welcomeLabel = new JLabel("Welcome Customer, " + username + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);

        // profile and logout buttons
        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerButtons.setOpaque(false);

        JButton profileBtn = new JButton("My Profile");
        profileBtn.setFont(new Font("Arial", Font.BOLD, 12));
        profileBtn.setBackground(Color.WHITE);
        profileBtn.setForeground(new Color(46, 125, 50));
        profileBtn.setOpaque(true);
        profileBtn.setBorderPainted(false);
        profileBtn.setFocusPainted(false);
        profileBtn.setPreferredSize(new Dimension(110, 32));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Arial", Font.BOLD, 12));
        logoutBtn.setBackground(Color.WHITE);
        logoutBtn.setForeground(new Color(46, 125, 50));
        logoutBtn.setOpaque(true);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setPreferredSize(new Dimension(90, 32));

        headerButtons.add(profileBtn);
        headerButtons.add(logoutBtn);

        headerBar.add(welcomeLabel, BorderLayout.WEST);
        headerBar.add(headerButtons, BorderLayout.EAST);
        add(headerBar, BorderLayout.NORTH);

        // main buttons area
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(245, 245, 245));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBackground(new Color(245, 245, 245));

        // make the buttons
        JButton orderBtn = new JButton("Order Food");
        JButton zipBtn = new JButton("Set Zip Code");
        JButton paymentBtn = new JButton("Payment Methods");
        JButton historyBtn = new JButton("Order History");

        // style them
        styleButton(orderBtn);
        styleButton(zipBtn);
        styleButton(paymentBtn);
        styleButton(historyBtn);

        // add buttons w spacing between
        buttonsPanel.add(orderBtn);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(zipBtn);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(paymentBtn);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(historyBtn);

        contentPanel.add(buttonsPanel);
        add(contentPanel, BorderLayout.CENTER);

        // button clicks
        orderBtn.addActionListener(e -> openOrderScreen());
        zipBtn.addActionListener(e -> promptZipCode());
        profileBtn.addActionListener(e -> openProfileDialog());
        paymentBtn.addActionListener(e -> openPaymentMethodDialog());
        historyBtn.addActionListener(e -> openOrderHistory());
        logoutBtn.addActionListener(e -> logout());

        // One-time immediate check for delivered orders not yet notified
        try {
            if (parent.orderDb != null && parent.orderDb.hasUnnotifiedDelivered(username)) {
                parent.showNotification("Your food has been delivered!", new Color(46, 125, 50), Color.WHITE, 5000);
                parent.orderDb.markDeliveredNotified(username);
            }
        } catch (SQLException ex) {
            Logger.catchAndLogBug(ex, "MainScreen");
            JOptionPane.showMessageDialog(this, "Error checking for delivered orders: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        // Periodic check while this screen is shown
        deliveredCheckTimer = new javax.swing.Timer(5000, e -> {
            try {
                if (parent.orderDb != null && parent.orderDb.hasUnnotifiedDelivered(username)) {
                    parent.showNotification("Your food has been delivered!", new Color(46, 125, 50), Color.WHITE, 5000);
                    parent.orderDb.markDeliveredNotified(username);
                }
            } catch (SQLException ex) {
                Logger.catchAndLogBug(ex, "MainScreen");
                // Stop the timer if a database error occurs to prevent repeated errors
                if (deliveredCheckTimer != null) {
                    deliveredCheckTimer.stop();
                }
                JOptionPane.showMessageDialog(this, "Error checking for delivered orders: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        deliveredCheckTimer.start();
    }

    // logout and go back to login
    private void logout() {
        if (deliveredCheckTimer != null) {
            deliveredCheckTimer.stop();
            deliveredCheckTimer = null;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Logout",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            parent.getSceneSorter().switchPage("Login");
        }
    }

    // makes buttons look the same
    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(46, 125, 50));
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(220, 45));
        button.setMaximumSize(new Dimension(220, 45));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    // opens order history screen
    private void openOrderHistory() {
        CustomerOrderHistory historyScreen = new CustomerOrderHistory(parent, username);
        try {
            parent.getSceneSorter().addScene("CustomerOrderHistory", historyScreen);
        } catch (IllegalArgumentException ex) {
            // already exists
        }
        parent.getSceneSorter().switchPage("CustomerOrderHistory");
    }

    // opens order screen if zip is set
    private void openOrderScreen() {
        // Reload address to ensure we have the latest data
        parent.loadUserAddress(username);
        
        // Check if we have at least a zip code set
        if (parent.address == null || parent.address.getZip() == null || parent.address.getZip().isEmpty()) {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                    "Please set a zip code before you order so we can show you nearby restaurants!",
                    "Zip Code Required", JOptionPane.WARNING_MESSAGE);
            promptZipCode();
            return;
        }
        
        // Check if RestaurantScreen already exists and update it
        ResturantScreen restaurantScreen = parent.getSceneSorter().getScene("RestaurantScreen");
        if (restaurantScreen != null) {
            // Update existing screen with new zip code from the address
            restaurantScreen.updateZipCode(parent.address.getZip());
        } else {
            // Create new restaurant screen with the current zip code
            restaurantScreen = new ResturantScreen(parent, username);
            parent.getSceneSorter().addScene("RestaurantScreen", restaurantScreen);
        }
        parent.getSceneSorter().switchPage("RestaurantScreen");
    }
    
    private void promptZipCode() {
        // Load current address to get the current zip code
        parent.loadUserAddress(username);
        String currentZip = (parent.address != null && parent.address.getZip() != null) 
            ? parent.address.getZip() : "";
        
        // Simple dialog to ask for just the zip code
        String newZip = JOptionPane.showInputDialog(
            SwingUtilities.getWindowAncestor(this),
            "Enter your 5-digit ZIP code:",
            currentZip
        );
        
        // If user cancelled or entered nothing, return
        if (newZip == null || newZip.trim().isEmpty()) {
            return;
        }
        
        newZip = newZip.trim();
        
        // Validate zip code format (5 digits)
        if (!newZip.matches("\\d{5}")) {
            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "ZIP code must be exactly 5 digits.",
                "Invalid ZIP Code",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        // Update the zip code in the address
        try {
            Address currentAddress = parent.address;
            Address updatedAddress;
            
            if (currentAddress != null) {
                // Keep existing address data, just update zip
                updatedAddress = new Address(
                    currentAddress.getStreet(),
                    currentAddress.getCity(),
                    currentAddress.getState(),
                    newZip,
                    currentAddress.getLatitude(),
                    currentAddress.getLongitude()
                );
            } else {
                // Create a minimal address with just the zip code
                // Use placeholder values for other required fields
                updatedAddress = new Address(
                    "Address not set",
                    "City not set",
                    "XX",
                    newZip,
                    0.0,
                    0.0
                );
            }
            
            // Save to database
            parent.userDb.updateUserAddress(username, updatedAddress);
            parent.loadUserAddress(username);
            
            // Update local zipCode variable
            this.zipCode = newZip;
            
            // Update RestaurantScreen if it exists
            ResturantScreen restaurantScreen = parent.getSceneSorter().getScene("RestaurantScreen");
            if (restaurantScreen != null) {
                restaurantScreen.updateZipCode(newZip);
            }
            
            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "ZIP code updated to: " + newZip,
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );
            
        } catch (SQLException ex) {
            Logger.catchAndLogBug(ex, "MainScreen.promptZipCode");
            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "Error updating ZIP code: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // shows profile info, can edit email
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

    // payment method stuff
    private void openPaymentMethodDialog() {
        try {
            // check if they have payment method
            PaymentInformation activeMethod = parent.paymentDb.getActivePaymentMethod(username);

            String currentMethod = "No payment method on file";
            if (activeMethod != null) {
                if ("CARD".equals(activeMethod.getPaymentType())) {
                    // hide most of card number
                    String cardNum = activeMethod.getCardNumber();
                    String masked = "****-****-****-" + (cardNum.length() >= 4 ? cardNum.substring(cardNum.length() - 4) : cardNum);
                    currentMethod = "Credit Card: " + masked + " (Exp: " + activeMethod.getCardExpiry() + ")";
                } else if ("BANK".equals(activeMethod.getPaymentType())) {
                    String acctNum = activeMethod.getAccountNumber();
                    String masked = "****" + (acctNum.length() >= 4 ? acctNum.substring(acctNum.length() - 4) : acctNum);
                    currentMethod = "Bank Account: " + masked + " (" + activeMethod.getBankName() + ")";
                }
            }

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
            Logger.catchAndLogBug(ex, "MainScreen");
            JOptionPane.showMessageDialog(this,
                    "Error accessing payment database: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // add credit card
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

            // check if empty
            if (cardNumber.isEmpty() || expiry.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // validation removed -> accept any input for demonstrating

            try {
                parent.paymentDb.deactivateAllPaymentMethods(username);
                parent.paymentDb.addCardPayment(username, cardNumber, expiry, name);
                JOptionPane.showMessageDialog(this, "Credit card added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                // Re-open the payment dialog to show the updated payment method
                openPaymentMethodDialog();
            } catch (SQLException ex) {
                Logger.catchAndLogBug(ex, "MainScreen");
                JOptionPane.showMessageDialog(this,
                        "Error saving payment method: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // luhn algorithm to check if card num is valid
    private boolean isValidCardNumber(String cardNumber) {
        int sum = 0;
        boolean alternate = false;

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

    // add bank account
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
                parent.paymentDb.deactivateAllPaymentMethods(username);
                parent.paymentDb.addBankPayment(username, routing, account, bankName);
                JOptionPane.showMessageDialog(this, "Bank account added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                // Re-open the payment dialog to show the updated payment method
                openPaymentMethodDialog();
            } catch (SQLException ex) {
                Logger.catchAndLogBug(ex, "MainScreen");
                JOptionPane.showMessageDialog(this,
                        "Error saving payment method: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}