import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import javax.swing.*;

public class LoginUI {

    private final FoodDeliveryLoginUI parent;
    private final JTextField userField = new JTextField(15);
    private final JPasswordField passField = new JPasswordField(15);

    public LoginUI(FoodDeliveryLoginUI parent) {
        this.parent = parent;
    }

    public JPanel buildLoginPanel() {
        JPanel centerPanel = new JPanel(new GridBagLayout());


        // - creates the username and password label+field pairs and positions
        //   them using GridBagLayout constraints
        // - creates Login and Register buttons and attaches action listeners
        //   (loginBtn -> onLogin, registerBtn -> onRegister)
        // - the buttons panel is added to the center area so user can submit
        //   the form; all components are standard Swing components (JLabel,
        //   JTextField, JPasswordField, JButton) and listeners receive
        //   ActionEvent when triggered.
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);

    c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.EAST;
    centerPanel.add(new JLabel("Username:"), c);
    c.gridx = 1; c.anchor = GridBagConstraints.WEST;
    centerPanel.add(userField, c);

    c.gridx = 0; c.gridy = 1; c.anchor = GridBagConstraints.EAST;
    centerPanel.add(new JLabel("Password:"), c);
    c.gridx = 1; c.anchor = GridBagConstraints.WEST;
    centerPanel.add(passField, c);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
    JButton loginBtn = new JButton("Login");
    loginBtn.addActionListener(this::onLogin);
    JButton registerBtn = new JButton("Register");
    registerBtn.addActionListener(this::onRegister);
        btns.add(loginBtn);
        btns.add(registerBtn);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER;
        centerPanel.add(btns, c);

        // wrap the centerPanel in a translucent white card so controls are readable
        JPanel cardWrapper = new JPanel(new GridBagLayout());
        cardWrapper.setOpaque(false);
        JPanel translucentCard = new JPanel(new GridBagLayout());
        translucentCard.setOpaque(true);
        translucentCard.setBackground(new Color(255,255,255,220));
        translucentCard.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        translucentCard.add(centerPanel);
        cardWrapper.add(translucentCard);

        return cardWrapper;
    }
    // buildLoginPage Summary:

    // This is used by sceneSorter as the logic for the
    // "Login" UI. When switchPage is used to call "Login",
    // this logic will be used.

    // Upload background removed; background controlled programmatically.
    public void onLogin(ActionEvent e) {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter username and password.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check if this is an admin login attempt
        if (user.equals("FoodDashAdmin")) {
            try {
                if (!parent.userDb.authenticate(user, FoodDeliveryLoginUI.sha256Hex(pass))) {
                    JOptionPane.showMessageDialog(null, "Invalid admin credentials.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Admin credentials correct, now prompt for hash code
                String hashCode = JOptionPane.showInputDialog(null, 
                    "Please enter your admin hash code:", 
                    "Admin Authentication", 
                    JOptionPane.QUESTION_MESSAGE);
                
                if (hashCode == null || hashCode.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Hash code required for admin access.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (!parent.userDb.verifyAdminHash(user, hashCode.trim())) {
                    JOptionPane.showMessageDialog(null, "Invalid admin hash code.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Admin authentication successful
                AdminScreen adminScreen = new AdminScreen(parent);
                try {
                    parent.getSceneSorter().addScene("AdminScreen", adminScreen);
                } catch (IllegalArgumentException ex) {
                    // Scene already exists, that's fine
                }
                parent.getSceneSorter().switchPage("AdminScreen");
                return;
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try {
            String hash = FoodDeliveryLoginUI.sha256Hex(pass);
            if (parent.userDb == null) {
                JOptionPane.showMessageDialog(null, "User database not initialized.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Ask the database to verify the provided password hash
            // matches the stored value for this username.
            boolean ok = parent.userDb.authenticate(user, hash);
            if (ok) {
                passField.setText("");
                
                // Check user type and redirect accordingly
                String userType = parent.userDb.getUserType(user);
                if ("DRIVER".equals(userType)) {
                    // For drivers, open DriverScreen
                    DriverScreen driverScreen = new DriverScreen(parent, user);
                    try {
                        parent.getSceneSorter().addScene("DriverScreen", driverScreen);
                    } catch (IllegalArgumentException ex) {
                        // Scene already exists, that's fine
                    }
                    parent.getSceneSorter().switchPage("DriverScreen");
                } else {
                    // For customers, open MainScreen
                    MainScreen mainScreen = new MainScreen(parent, user);
                    parent.getSceneSorter().addScene("MainScreen", mainScreen);
                    parent.getSceneSorter().switchPage("MainScreen");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // onLogin(ActionEvent):
    // - validates non-empty inputs
    // - computes SHA-256 hex of password
    // - calls userDb.authenticate(username, hash) to check credentials
    // - updates messageLabel with success or failure messages
    private String showUserTypeDialog() {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(userField), "Select User Type", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(null);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton customerBtn = new JButton("Customer");
        JButton driverBtn = new JButton("Driver");
        
        final String[] result = {null};
        
        customerBtn.addActionListener(e -> {
            result[0] = "CUSTOMER";
            dialog.dispose();
        });
        
        driverBtn.addActionListener(e -> {
            result[0] = "DRIVER";
            dialog.dispose();
        });

        buttonPanel.add(customerBtn);
        buttonPanel.add(driverBtn);
        dialog.add(buttonPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
        
        return result[0];
    }

    public void onRegister(ActionEvent e) {
        // First, let user choose their type
        String userType = showUserTypeDialog();
        if (userType == null) return; // User cancelled

        String user = JOptionPane.showInputDialog(null, "Choose a username:", "Register", JOptionPane.QUESTION_MESSAGE);
        if (user == null) return;
        user = user.trim();
        if (user.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Username cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get password
        JPasswordField pf = new JPasswordField();
        int ok = JOptionPane.showConfirmDialog(null, pf, "Enter password:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        String pass = new String(pf.getPassword()).trim();
        if (pass.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get additional information
        String fullName = JOptionPane.showInputDialog(null, "Enter your full name:", "Registration", JOptionPane.QUESTION_MESSAGE);
        if (fullName == null || fullName.trim().isEmpty()) return;

        String email = JOptionPane.showInputDialog(null, "Enter your email:", "Registration", JOptionPane.QUESTION_MESSAGE);
        if (email == null || email.trim().isEmpty()) return;

        String phone = JOptionPane.showInputDialog(null, "Enter your phone number:", "Registration", JOptionPane.QUESTION_MESSAGE);
        if (phone == null || phone.trim().isEmpty()) return;

        String hash = FoodDeliveryLoginUI.sha256Hex(pass);
        try {
            if (parent.userDb == null) {
                JOptionPane.showMessageDialog(null, "User database not initialized.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Check DB to ensure the username isn't already taken.
            if (parent.userDb.userExists(user)) {
                JOptionPane.showMessageDialog(null, "Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Insert the new user record into the SQLite database.
            parent.userDb.register(user, hash, userType, fullName.trim(), email.trim(), phone.trim());

            // If registering a driver, collect additional driver information and register in driver database
            if ("DRIVER".equals(userType)) {
                // Get vehicle information
                String vehicleType = JOptionPane.showInputDialog(null, 
                    "Enter your vehicle type (e.g., Car, Motorcycle, Bicycle):", 
                    "Driver Registration", 
                    JOptionPane.QUESTION_MESSAGE);
                if (vehicleType == null || vehicleType.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Vehicle type is required for drivers.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String licenseNumber = JOptionPane.showInputDialog(null, 
                    "Enter your driver's license number:", 
                    "Driver Registration", 
                    JOptionPane.QUESTION_MESSAGE);
                if (licenseNumber == null || licenseNumber.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "License number is required for drivers.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String serviceArea = JOptionPane.showInputDialog(null, 
                    "Enter your preferred service area (ZIP code):", 
                    "Driver Registration", 
                    JOptionPane.QUESTION_MESSAGE);
                if (serviceArea == null || serviceArea.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Service area is required for drivers.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Register in driver database
                parent.driverDb.registerDriver(user, vehicleType.trim(), licenseNumber.trim(), serviceArea.trim());

                // Create and switch to driver screen
                DriverScreen driverScreen = new DriverScreen(parent, user);
                try {
                    parent.getSceneSorter().addScene("DriverScreen", driverScreen);
                } catch (IllegalArgumentException ex) {
                    // scene already exists; ignore and switch to it
                }
                parent.getSceneSorter().switchPage("DriverScreen");
            } else {
                JOptionPane.showMessageDialog(null, "Registered successfully. You can now login.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Failed to save user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // onRegister:

    // The logic used when the user clicks the "Register" button on the "Login" page.

    // No CSV fallback: persistence is provided by the SQLite-backed UserDatabase only.

}
// Summary:
// Login UI handles the JPanel for the login page,
// as well as the logic for both registering and logging in.