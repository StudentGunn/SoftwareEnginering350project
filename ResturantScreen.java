// ResturantScreen.java
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

public class ResturantScreen extends JPanel {
    private FoodDeliveryLoginUI parent;
    private String username;
    private JPanel content;
    /*
    --> Sets Resturant Screen, calls initUI to initialize the user interface
    --> Parameters:
        - FoodDeliveryLoginUI parent: the main UI frame
        - String username: the username of the logged-in user
     */
    public ResturantScreen(FoodDeliveryLoginUI parent, String username) {
        this.parent = parent;
        this.username = username;
        initUI();
    }

    // Method to update the zip code and refresh the UI
    public void updateZipCode(String newZip) {
        this.zip = newZip;
        removeAll(); // Clear all components
        initUI(); // Rebuild UI with new zip
        revalidate(); // Revalidate the layout
        repaint(); // Repaint the panel
    }

    private void initUI() {
        setLayout(new BorderLayout(5,5));
        String zip = parent.address.getZip();

        // header with green style
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15,20,15,20));
        headerPanel.setBackground(new Color(46, 125, 50));

        JLabel header = new JLabel("Restaurants - " + zip, SwingConstants.LEFT);
        header.setFont(new Font("Times New Roman", Font.BOLD, 16));
        header.setForeground(Color.WHITE);
        headerPanel.add(header, BorderLayout.CENTER);

        // back button
        JButton backBtn = new JButton("Back");
        backBtn.setFont(new Font("Times New Roman", Font.BOLD, 12));
        backBtn.setBackground(Color.WHITE);
        backBtn.setForeground(new Color(46, 125, 50));
        backBtn.setOpaque(true);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> {
            // Just switch back to MainScreen without creating a new one
            parent.getSceneSorter().switchPage("MainScreen");
        });

        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content Panel
        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        content.setBackground(new Color(250, 250, 250));

        JScrollPane scroll = new JScrollPane(content);
        scroll.getViewport().setBackground(content.getBackground());
        add(scroll, BorderLayout.CENTER);

        // Add a listener to refresh the UI when the component is shown
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshUI();
            }
        });
    }

    // Allows the UI to refresh when pulled up. This allows it to reflect changes in address or zip code.
    private void refreshUI() {
        content.removeAll();
        String zip = parent.address != null ? String.valueOf(parent.address.getZip()) : "";

        if ("02325".equals(zip)) {
            content.add(createRestaurantRow("Crimson Dining", "125 Burrill Ave", 41.98656, 70.96437));
            content.add(Box.createVerticalStrut(6));
            content.add(createRestaurantRow("Barrett's Alehouse Bridgewater", "425 Bedford St", 41.97653, 70.97556));
            content.add(Box.createVerticalStrut(6));
            content.add(createRestaurantRow("Greyhound Tavern", "39 Broad Street", 41.99093, 70.97487));
            content.add(Box.createVerticalStrut(8));
        } else {
            // no restaurants for other zips yet
            JPanel noResultsPanel = new JPanel(new BorderLayout());
            noResultsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            JLabel none = new JLabel("No restaurants available in " + zip, SwingConstants.CENTER);
            none.setFont(new Font("Arial", Font.PLAIN, 12));
            none.setForeground(Color.GRAY);

            noResultsPanel.add(none, BorderLayout.CENTER);
            content.add(noResultsPanel);
            content.add(Box.createVerticalStrut(8));
        }

        content.revalidate();
        content.repaint();
    }

    // makes each restaurant row
    private JPanel createRestaurantRow(String name, String restAddress, double lat, double lon) {
        JPanel row = new JPanel(new BorderLayout(8,6));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        row.setBackground(Color.WHITE);
        row.setOpaque(true);

        // restaurant info, creation panel
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Times New Roman", Font.BOLD, 12));
        info.add(nameLabel);

        JLabel addressLabel = new JLabel(restAddress);
        addressLabel.setFont(new Font("Times New Roman", Font.PLAIN, 10));
        addressLabel.setForeground(Color.DARK_GRAY);
        info.add(addressLabel);

        row.add(info, BorderLayout.CENTER);

        // order button
        JButton orderBtn = new JButton("Order Here");
        orderBtn.setFont(new Font("Times New Roman", Font.BOLD, 12));
        orderBtn.setBackground(new Color(46, 125, 50));
        orderBtn.setForeground(Color.WHITE);
        orderBtn.setOpaque(true);
        orderBtn.setBorderPainted(false);
        orderBtn.setFocusPainted(false);
        orderBtn.addActionListener(e -> createOrder(name, lat, lon));
        row.add(orderBtn, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 0));
        bottomPanel.setOpaque(false);

        // Miles/ETA
        if (parent.address != null) {
            double miles = MapCalculator.calculateMiles(parent.address.getLatitude(),parent.address.getLongitude(), lat, lon);
            JLabel distanceLabel = new JLabel(String.format("%.1f miles away", miles));
            distanceLabel.setFont(new Font("Times New Roman", Font.PLAIN, 10));
            distanceLabel.setForeground(new Color(46, 125, 50));
            distanceLabel.setOpaque(false);
            distanceLabel.setBorder(BorderFactory.createCompoundBorder());
            bottomPanel.add(distanceLabel);
        }

        row.add(bottomPanel, BorderLayout.SOUTH);

        return row;
    }
    // shows menu and places order
    private void createOrder(String restaurantName, double restaurantLat, double restaurantLon) {
        String restaurantAddress = getRestaurantAddress(restaurantName);
        String[] menuItems = {"Burger - $12.99", "Pizza - $15.99", "Salad - $8.99", "Pasta - $13.99", "Sandwich - $9.99"};
        double[] prices = {12.99, 15.99, 8.99, 13.99, 9.99};

        MenuSelectionPanel menuPanel = buildMenuPanel(restaurantName, menuItems);
        int result = JOptionPane.showConfirmDialog(this, menuPanel.panel,
                "Order from " + restaurantName,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            OrderCalculation calc = calculateOrderTotal(menuItems, prices, menuPanel.checkBoxes, menuPanel.quantities);

            if (!calc.anySelected) {
                JOptionPane.showMessageDialog(this,
                    "Please select at least one item to order.",
                    "No Items Selected",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                calc.orderDetails,
                "Confirm Order",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);

            if (confirm == JOptionPane.OK_OPTION) {
                try {
                    saveOrderToDatabase(restaurantName, restaurantAddress, menuItems, prices, menuPanel.checkBoxes, menuPanel.quantities, calc.total, restaurantLat, restaurantLon);
                    navigateBackToMain();
                } catch (SQLException ex) {
                    Logger.catchAndLogBug(ex, "ResturantScreen");
                    JOptionPane.showMessageDialog(this,
                        "Error creating order: " + ex.getMessage(),
                        "Order Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private String getRestaurantAddress(String restaurantName) {
        Map<String, String> restaurantAddresses = new HashMap<>();
        restaurantAddresses.put("Crimson Dining", "125 Burrill Ave");
        restaurantAddresses.put("Barrett's Alehouse Bridgewater", "425 Bedford St");
        restaurantAddresses.put("Greyhound Tavern", "39 Broad Street");
        return restaurantAddresses.getOrDefault(restaurantName, "Address not available");
    }

    private MenuSelectionPanel buildMenuPanel(String restaurantName, String[] menuItems) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Select items from " + restaurantName);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(10));

        JCheckBox[] checkBoxes = new JCheckBox[menuItems.length];
        JSpinner[] quantities = new JSpinner[menuItems.length];

        for (int i = 0; i < menuItems.length; i++) {
            JPanel itemPanel = new JPanel();
            itemPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
            itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            itemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            checkBoxes[i] = new JCheckBox(menuItems[i]);
            checkBoxes[i].setFont(new Font("Arial", Font.PLAIN, 12));

            quantities[i] = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
            quantities[i].setPreferredSize(new Dimension(60, 25));
            ((JSpinner.DefaultEditor)quantities[i].getEditor()).getTextField().setColumns(2);

            itemPanel.add(checkBoxes[i]);
            itemPanel.add(quantities[i]);
            panel.add(itemPanel);
        }

        JLabel noteLabel = new JLabel("* Use spinners to select quantity (1-10)");
        noteLabel.setFont(new Font("Times New Roman", Font.PLAIN, 10));
        noteLabel.setForeground(Color.GRAY);
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(Box.createVerticalStrut(10));
        panel.add(noteLabel);

        return new MenuSelectionPanel(panel, checkBoxes, quantities);
    }

    private OrderCalculation calculateOrderTotal(String[] menuItems, double[] prices, JCheckBox[] checkBoxes, JSpinner[] quantities) {
        double total = 0.0;
        boolean anySelected = false;
        StringBuilder orderDetails = new StringBuilder();
        orderDetails.append("Order Summary:\n\n");

        for (int i = 0; i < checkBoxes.length; i++) {
            if (checkBoxes[i].isSelected()) {
                anySelected = true;
                int quantity = (Integer)quantities[i].getValue();
                double itemTotal = prices[i] * quantity;
                total += itemTotal;
                orderDetails.append(String.format("%dx %s: $%.2f\n",
                    quantity, menuItems[i].split(" - ")[0], itemTotal));
            }
        }

        orderDetails.append(String.format("\nTotal: $%.2f", total));
        return new OrderCalculation(total, anySelected, orderDetails.toString());
    }

    private void saveOrderToDatabase(String restaurantName, String restaurantAddress, String[] menuItems,
                                     double[] prices, JCheckBox[] checkBoxes, JSpinner[] quantities, double total,
                                     double restaurantLat, double restaurantLon) throws SQLException {
        // Check payment method
        PaymentInformation paymentInfo = parent.paymentDb.getActivePaymentMethod(username);
        if (paymentInfo == null) {
            JOptionPane.showMessageDialog(this,
                "Please set up a payment method first.",
                "Payment Required",
                JOptionPane.WARNING_MESSAGE);
            throw new SQLException("No payment method available");
        }

        // Count total items
        int totalItems = 0;
        for (int i = 0; i < checkBoxes.length; i++) {
            if (checkBoxes[i].isSelected()) {
                totalItems += (Integer)quantities[i].getValue();
            }
        }

        // Create order in database
        long orderId = parent.orderDb.createOrder(username, restaurantName, restaurantAddress,
            parent.address.getStreet(),
            "No Special Instructions",
            total,
            totalItems,
            paymentInfo.getPaymentType(),
            restaurantLat, restaurantLon,
            parent.address.getLatitude(), parent.address.getLongitude());

        // Add items to order
        for (int i = 0; i < checkBoxes.length; i++) {
            if (checkBoxes[i].isSelected()) {
                int quantity = (Integer)quantities[i].getValue();
                String itemName = menuItems[i].split(" - ")[0];
                double price = prices[i];
                parent.orderDb.addOrderItem(orderId, itemName, quantity, price, null);
            }
        }

        // Show ETA confirmation
        ETA eta = new ETA((int)orderId, totalItems);
        String confirmMessage = String.format(
            "Order #%d placed successfully!\n" +
            "Total: $%.2f\n\n" +
            "%s",
            orderId, total, eta.getETAMessage());

        JOptionPane.showMessageDialog(this,
            confirmMessage,
            "Order Confirmation",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void navigateBackToMain() {
        MainScreen mainScreen = new MainScreen(parent, username);
        try {
            parent.getSceneSorter().addScene("MainScreen", mainScreen);
        } catch (IllegalArgumentException ex) {
            // already exists; reuse
        }
        parent.getSceneSorter().switchPage("MainScreen");
    }

    // Inner classes to encapsulate related data
    private static class MenuSelectionPanel {
        JPanel panel;
        JCheckBox[] checkBoxes;
        JSpinner[] quantities;

        MenuSelectionPanel(JPanel panel, JCheckBox[] checkBoxes, JSpinner[] quantities) {
            this.panel = panel;
            this.checkBoxes = checkBoxes;
            this.quantities = quantities;
        }
    }

    private static class OrderCalculation {
        double total;
        boolean anySelected;
        String orderDetails;

        OrderCalculation(double total, boolean anySelected, String orderDetails) {
            this.total = total;
            this.anySelected = anySelected;
            this.orderDetails = orderDetails;
        }
    }
}
