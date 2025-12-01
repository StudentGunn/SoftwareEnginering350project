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
        try {
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(new Color(245, 245, 245));

            // header
            JPanel headerBar = new JPanel(new BorderLayout(10, 0));
            headerBar.setBackground(new Color(46, 125, 50));
            headerBar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

            JLabel titleLabel = new JLabel("Food Delivery Service", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
            titleLabel.setForeground(Color.WHITE);

            headerBar.add(titleLabel, BorderLayout.CENTER);
            mainPanel.add(headerBar, BorderLayout.NORTH);

            // login form
            JPanel centerPanel = new JPanel(new GridBagLayout());
            centerPanel.setBackground(new Color(245, 245, 245));
            centerPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(8, 8, 8, 8);

            c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.EAST;
            centerPanel.add(new JLabel("Username:"), c);
            c.gridx = 1; c.anchor = GridBagConstraints.WEST;
            centerPanel.add(userField, c);

            c.gridx = 0; c.gridy = 1; c.anchor = GridBagConstraints.EAST;
            centerPanel.add(new JLabel("Password:"), c);
            c.gridx = 1; c.anchor = GridBagConstraints.WEST;
            centerPanel.add(passField, c);

            // buttons
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            btns.setOpaque(false);

            JButton loginBtn = new JButton("Login");
            styleButton(loginBtn);
            loginBtn.addActionListener(this::onLogin);

            JButton registerBtn = new JButton("Register");
            styleButton(registerBtn);
            registerBtn.addActionListener(this::onRegister);

            btns.add(loginBtn);
            btns.add(registerBtn);

            c.gridx = 0; c.gridy = 2; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER;
            centerPanel.add(btns, c);

            mainPanel.add(centerPanel, BorderLayout.CENTER);

            return mainPanel;
        } catch (Exception e) {
            Logger.catchAndLogBug(e, "LoginUI.buildLoginPanel");
            return new JPanel(); // Return empty panel on error
        }
    }

    // style buttons
    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(new Color(46, 125, 50));
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(100, 35));
    }
    // login button click
    public void onLogin(ActionEvent e) {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter username and password.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // check if admin
        if (user.equals("FoodDashAdmin")) {
            try {
                if (!parent.userDb.authenticate(user, FoodDeliveryLoginUI.sha256Hex(pass))) {
                    JOptionPane.showMessageDialog(null, "Invalid admin credentials.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // ask for hash code
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

                // admin login success
                AdminScreen adminScreen = new AdminScreen(parent);
                try {
                    parent.getSceneSorter().addScene("AdminScreen", adminScreen);
                } catch (IllegalArgumentException ex) {
                    // already exists
                }
                parent.getSceneSorter().switchPage("AdminScreen");
                return;
            } catch (SQLException ex) {
                Logger.catchAndLogBug(ex, "LoginUI.onLogin");
                JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try {
            // regular user login
            String hash = FoodDeliveryLoginUI.sha256Hex(pass);
            if (parent.userDb == null) {
                JOptionPane.showMessageDialog(null, "User database not initialized.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean ok = parent.userDb.authenticate(user, hash);
            if (ok) {
                passField.setText("");
                parent.loadUserAddress(user);

                // check user type
                String userType = parent.userDb.getUserType(user);
                if ("DRIVER".equals(userType)) {
                    DriverScreen driverScreen = new DriverScreen(parent, user);
                    try {
                        parent.getSceneSorter().addScene("DriverScreen", driverScreen);
                    } catch (IllegalArgumentException ex) {
                        // already exists
                    }
                    parent.getSceneSorter().switchPage("DriverScreen");
                } else {
                    MainScreen mainScreen = new MainScreen(parent, user);
                    try {
                        parent.getSceneSorter().addScene("MainScreen", mainScreen);
                    } catch (IllegalArgumentException ex) {
                        // already exists
                    }
                    parent.getSceneSorter().switchPage("MainScreen");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            Logger.catchAndLogBug(ex, "LoginUI.onLogin");
            JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            Logger.catchAndLogBug(ex, "LoginUI.onLogin");
            JOptionPane.showMessageDialog(null, "Unexpected error during login: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // user type dialog for registration
    private String showUserTypeDialog() {
        try {
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
        } catch (Exception e) {
            Logger.catchAndLogBug(e, "LoginUI.showUserTypeDialog");
            return null;
        }
    }

    // register button click
    public void onRegister(ActionEvent e) {
        // let user pick customer or driver
        String userType = showUserTypeDialog();
        if (userType == null) return;

        String user = JOptionPane.showInputDialog(null, "Choose a username:", "Register", JOptionPane.QUESTION_MESSAGE);
        if (user == null) return;
        user = user.trim();
        if (user.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Username cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPasswordField pf = new JPasswordField();
        int ok = JOptionPane.showConfirmDialog(null, pf, "Enter password:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        String pass = new String(pf.getPassword()).trim();
        if (pass.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // get additional info
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

            if (parent.userDb.userExists(user)) {
                JOptionPane.showMessageDialog(null, "Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            parent.userDb.register(user, hash, userType, fullName.trim(), email.trim(), phone.trim());

            // driver registration needs extra info
            if ("DRIVER".equals(userType)) {
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

                parent.driverDb.registerDriver(user, vehicleType.trim(), licenseNumber.trim(), serviceArea.trim());

                DriverScreen driverScreen = new DriverScreen(parent, user);
                try {
                    parent.getSceneSorter().addScene("DriverScreen", driverScreen);
                } catch (IllegalArgumentException ex) {
                    // already exists
                }
                parent.getSceneSorter().switchPage("DriverScreen");
            } else {
                JOptionPane.showMessageDialog(null, "Registered successfully. You can now login.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            Logger.catchAndLogBug(ex, "LoginUI.onRegister");
            JOptionPane.showMessageDialog(null, "Failed to save user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            Logger.catchAndLogBug(ex, "LoginUI.onRegister");
            JOptionPane.showMessageDialog(null, "Unexpected error during registration: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

   
}
