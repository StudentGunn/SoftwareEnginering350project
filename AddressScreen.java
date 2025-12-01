
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

public class AddressScreen extends JPanel {
    private FoodDeliveryLoginUI parent;
    private String username;
    private JTextField streetField;
    private JTextField cityField;
    private JTextField stateField;
    private JTextField zipField;
    private JTextField latitudeField;
    private JTextField longitudeField;
    private JButton updateButton;
    private JButton backButton;

    public AddressScreen(FoodDeliveryLoginUI parent, String username) {
        this.parent = parent;
        this.username = username;
        initUI();
        loadAddress();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 245, 245));

        // Header
        JPanel headerBar = new JPanel(new BorderLayout(10, 0));
        headerBar.setBackground(new Color(46, 125, 50));
        headerBar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        backButton = new JButton("Back");
        styleHeaderButton(backButton);
        headerBar.add(backButton, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("Manage Your Address", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerBar.add(titleLabel, BorderLayout.CENTER);

        add(headerBar, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 245, 245));
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Street:"), gbc);
        gbc.gridx = 1;
        streetField = new JTextField(20);
        formPanel.add(streetField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("City:"), gbc);
        gbc.gridx = 1;
        cityField = new JTextField(20);
        formPanel.add(cityField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("State:"), gbc);
        gbc.gridx = 1;
        stateField = new JTextField(20);
        formPanel.add(stateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Zip:"), gbc);
        gbc.gridx = 1;
        zipField = new JTextField(20);
        formPanel.add(zipField, gbc);

        // Latitude and Longitude
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel cordHelp = new JLabel("You can find coordinates by placing the address into Google Maps!");
        cordHelp.setFont(new Font("Arial", Font.PLAIN, 10));
        cordHelp.setForeground(new Color(46, 125, 50));
        cordHelp.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(cordHelp, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Latitude:"), gbc);
        gbc.gridx = 1;
        latitudeField = new JTextField(20);
        formPanel.add(latitudeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(new JLabel("Longitude:"), gbc);
        gbc.gridx = 1;
        longitudeField = new JTextField(20);
        formPanel.add(longitudeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        updateButton = new JButton("Update Address");
        styleMainButton(updateButton);
        formPanel.add(updateButton, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Action Listeners
        backButton.addActionListener(e -> {
            MainScreen mainScreen = new MainScreen(parent, username);
            try {
                parent.getSceneSorter().addScene("MainScreen", mainScreen);
            } catch (IllegalArgumentException ex) {
                // already exists
            }
            parent.getSceneSorter().switchPage("MainScreen");
        });
        updateButton.addActionListener(this::updateAddress);
    }

    private void styleMainButton(JButton button) {
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

    private void styleHeaderButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(46, 125, 50));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(90, 32));
    }

    private void loadAddress() {
        if (parent.address != null) {
            streetField.setText(parent.address.getStreet());
            cityField.setText(parent.address.getCity());
            stateField.setText(parent.address.getState());
            zipField.setText(parent.address.getZip());
            latitudeField.setText(String.valueOf(parent.address.getLatitude()));
            longitudeField.setText(String.valueOf(parent.address.getLongitude()));
        }
    }

    private void updateAddress(ActionEvent e) {
        String street = streetField.getText().trim();
        if (street.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Street cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String city = cityField.getText().trim();
        if (city.isEmpty()) {
            JOptionPane.showMessageDialog(this, "City cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String state = stateField.getText().trim();
        if (state.length() != 2 || !state.matches("[a-zA-Z]+")) {
            JOptionPane.showMessageDialog(this, "State must be a 2-letter abbreviation.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String zip = zipField.getText().trim();
        if (!zip.matches("\\d{5}")) {
            JOptionPane.showMessageDialog(this, "ZIP code must be 5 digits.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double latitude = Double.parseDouble(latitudeField.getText().trim());
        double longitude = Double.parseDouble(longitudeField.getText().trim());
        if (!(longitude >= -180 && longitude <= 180) || !(latitude >= -90 && latitude <= 90)) {
            JOptionPane.showMessageDialog(this, "Latitude must be between -90 and 90.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Uploads the new address to the database,
            Address newAddress = new Address(street, city, state.toUpperCase(), zip, latitude, longitude);
            
            // Use the single, shared UserDataBase instance from the parent
            parent.userDb.updateUserAddress(username, newAddress);
            
            // Reload the address in the parent UI to ensure it's updated globally
            parent.loadUserAddress(username);
            
            JOptionPane.showMessageDialog(this, "Address updated successfully!");

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating address in the database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
