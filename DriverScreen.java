//DriverScreen.java
import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class DriverScreen extends JPanel {
    private final FoodDeliveryLoginUI parent;
    private final String username;

    public DriverScreen(FoodDeliveryLoginUI parent, String username) {
        this.parent = parent;
        this.username = username == null || username.isEmpty() ? "Driver" : username;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 245, 245));

        // Header
        JPanel headerBar = new JPanel(new BorderLayout(10, 0));
        headerBar.setBackground(new Color(46, 125, 50));
        headerBar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Arial", Font.BOLD, 12));
        logoutBtn.setBackground(Color.WHITE);
        logoutBtn.setForeground(new Color(46, 125, 50));
        logoutBtn.setOpaque(true);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setPreferredSize(new Dimension(90, 32));

        headerBar.add(welcomeLabel, BorderLayout.WEST);
        headerBar.add(logoutBtn, BorderLayout.EAST);
        add(headerBar, BorderLayout.NORTH);

        // main buttons
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(245, 245, 245));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBackground(new Color(245, 245, 245));

        // driver buttons
        JButton getOrderBtn = new JButton("Get Order");
        JButton deliveryHistoryBtn = new JButton("Delivery History");
        JButton paymentHistoryBtn = new JButton("Payment History");
        JButton paymentMethodBtn = new JButton("Payment Method");
        JButton confirmDropoffBtn = new JButton("Confirm Food Drop off");
        JButton cashOutBtn = new JButton("Collect payment");

        // style buttons
        styleButton(getOrderBtn);
        styleButton(deliveryHistoryBtn);
        styleButton(paymentHistoryBtn);
        styleButton(paymentMethodBtn);
        styleButton(confirmDropoffBtn);
        styleButton(cashOutBtn);


        // add buttons with spacing
        buttonsPanel.add(getOrderBtn);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(deliveryHistoryBtn);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(paymentHistoryBtn);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(paymentMethodBtn);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(confirmDropoffBtn);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(cashOutBtn);

        contentPanel.add(buttonsPanel);

        /* currentOrder Panel
        --> Shows up on the right side of the screen
        --> Shows the current order, order id, restaurant address, and customer address.
        --> Collaborates with loadCurrentOrder to show oldest accepted order.
        */
        JPanel currentOrder = new JPanel();
        currentOrder.setLayout(new BoxLayout(currentOrder, BoxLayout.Y_AXIS));
        currentOrder.setBackground(new Color(245, 245, 245));
        currentOrder.setBorder(BorderFactory.createEtchedBorder(new Color(46, 125, 50), new Color(31, 91, 34)));
        currentOrder.add(Box.createVerticalStrut(10));

        JTextArea orderID = new JTextArea("Current Order:");
        orderID.setEditable(false);
        orderID.setFont(new Font("Arial", Font.PLAIN, 20));
        orderID.setForeground(new Color(46, 125, 50));
        orderID.setBackground(new Color(245, 245, 245));

        JLabel restaurantLabel = new JLabel("Restaurant Address:");
        restaurantLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        restaurantLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        restaurantLabel.setForeground(new Color(46, 125, 50));

        // Shows the street the restaurant is on for the current order.
        JTextArea restaurantAddress = new JTextArea();
        restaurantAddress.setAlignmentX(Component.LEFT_ALIGNMENT);
        restaurantAddress.setEditable(false);
        restaurantAddress.setFont(new Font("Arial", Font.PLAIN, 10));
        restaurantAddress.setForeground(Color.BLACK);
        restaurantAddress.setBackground(new Color(245, 245, 245));

        JLabel customerLabel = new JLabel("Customer Address: ");
        customerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        customerLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        customerLabel.setForeground(new Color(46, 125, 50));

        JTextArea customerAddress = new JTextArea();
        customerAddress.setAlignmentX(Component.LEFT_ALIGNMENT);
        customerAddress.setEditable(false);
        customerAddress.setFont(new Font("Arial", Font.PLAIN, 10));
        customerAddress.setForeground(Color.BLACK);
        customerAddress.setBackground(new Color(245, 245, 245));

        currentOrder.add(orderID);
        currentOrder.add(restaurantLabel);
        currentOrder.add(restaurantAddress);
        currentOrder.add(customerLabel);
        currentOrder.add(customerAddress);
        currentOrder.add(Box.createVerticalStrut(10));
        contentPanel.add(currentOrder);

        add(contentPanel, BorderLayout.CENTER);

        // button actionss
        getOrderBtn.addActionListener(e -> {
            DriverGetOrder getOrderScreen = new DriverGetOrder(parent, username);
            try {
                parent.getSceneSorter().addScene("DriverGetOrder", getOrderScreen);
            } catch (IllegalArgumentException ex) {
                // catches already exists
            }
            parent.getSceneSorter().switchPage("DriverGetOrder");
        });
        // cash out button action
        cashOutBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter order ID to collect payment for:", "Collect Payment", JOptionPane.QUESTION_MESSAGE);
            if (input == null) return; // cancelled
            input = input.trim();
            if (input.isEmpty() || !input.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric order ID.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return;
            }
            long orderId = Long.parseLong(input);

            // load order to compute driver pay
            ResultSet rs = null;
            try {
                rs = parent.orderDb.getOrderDetails(orderId);
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Order not found: " + orderId, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                double total = rs.getDouble("total_amount");
                // driver commission same as DriverGetOrder
                double driverPay = Math.round(total * 0.30 * 100.0) / 100.0;

                Long pmId = parent.paymentDb.getActivePaymentMethodId(username);
                if (pmId == null) {
                    JOptionPane.showMessageDialog(this, "No active payment method found for driver. Please set one up.", "Payment Method Required", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                long txId = parent.paymentDb.createTransaction(pmId, orderId, driverPay);
                parent.paymentDb.updateTransactionStatus(txId, "COMPLETED", null);

                JOptionPane.showMessageDialog(this, "Collected " + String.format("$%.2f", driverPay) + " for order #" + orderId, "Payment Collected", JOptionPane.INFORMATION_MESSAGE);

                // Open payment history so driver sees the new entry
                DriverPaymentHistory paymentHistoryScreen = new DriverPaymentHistory(parent, username);
                try {
                    parent.getSceneSorter().addScene("DriverPaymentHistory", paymentHistoryScreen);
                } catch (IllegalArgumentException ex) {
                    // already exists
                }
                parent.getSceneSorter().switchPage("DriverPaymentHistory");

            } catch (Exception ex) {
                Logger.catchAndLogBug(ex, "DriverScreen");
                JOptionPane.showMessageDialog(this, "Error collecting payment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (rs != null) {
                    try {
                        rs.getStatement().close();
                        rs.getStatement().getConnection().close();
                        rs.close();
                    } catch (Exception ignored) { }
                }
            }
        });

        deliveryHistoryBtn.addActionListener(e -> {
            DriveryHistory historyScreen = new DriveryHistory(parent, username);
            try {
                parent.getSceneSorter().addScene("DriverHistory", historyScreen);
            } catch (IllegalArgumentException ex) {
                // catches already exists
            }
            parent.getSceneSorter().switchPage("DriverHistory");
        });

        paymentHistoryBtn.addActionListener(e -> {
            DriverPaymentHistory paymentHistoryScreen = new DriverPaymentHistory(parent, username);
            try {
                parent.getSceneSorter().addScene("DriverPaymentHistory", paymentHistoryScreen);
            } catch (IllegalArgumentException ex) {
                // catches already exists
            }
            parent.getSceneSorter().switchPage("DriverPaymentHistory");
        });

        paymentMethodBtn.addActionListener(e -> {
            DriverSetPaymentMethod paymentMethodScreen = new DriverSetPaymentMethod(parent, username);
            try {
                parent.getSceneSorter().addScene("DriverSetPaymentMethod", paymentMethodScreen);
            } catch (IllegalArgumentException ex) {
                // catches already exists
            }
            parent.getSceneSorter().switchPage("DriverSetPaymentMethod");
        });

        // confirm drop-off: prompt for order ID and mark delivered
        confirmDropoffBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter delivered order ID:", "Confirm Drop-off", JOptionPane.QUESTION_MESSAGE);
            if (input == null) return; // cancelled
            input = input.trim();
            if (input.isEmpty() || !input.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric order ID.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return;
            }
            long orderId = Long.parseLong(input);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Mark order #" + orderId + " as DELIVERED?",
                "Confirm Delivery",
                JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            try {
                parent.orderDb.updateOrderStatus(orderId, "DELIVERED", username);
                try {
                    parent.driverDb.updateDriverStatus(username, "AVAILABLE");
                } catch (Exception ignored) { }
                JOptionPane.showMessageDialog(this, "Order #" + orderId + " marked as DELIVERED.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                Logger.catchAndLogBug(ex, "DriverScreen");
                JOptionPane.showMessageDialog(this, "Error updating order: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        logoutBtn.addActionListener(e -> logout());

        loadCurrentOrder(orderID, restaurantAddress, customerAddress, customerLabel);
    }
    /*
    --> Updates the currentOrder JPanel to show the most recent order.
    --> Uses the oldest order the driver has accepted.
    --> Shows the order id, restaurant address, and customer address.
    */
    private void loadCurrentOrder(JTextArea orderID, JTextArea restaurantAddress, JTextArea customerAddress, JLabel customerLabel) {
        try (ResultSet rs = parent.orderDb.getOldestActiveOrder(username)) {
            if (rs.next()) {
                orderID.setText("Order ID: " + rs.getLong("order_id"));
                restaurantAddress.setText(rs.getString("restaurant_address"));
                customerAddress.setText(rs.getString("delivery_address"));
            } else {
                orderID.setText("No active orders");
                restaurantAddress.setText("No active restaurants");
                customerAddress.setText("No active customers");
            }
        } catch (SQLException ex) {
            Logger.catchAndLogBug(ex, "DriverScreen");
            orderID.setText("Error loading order");
        }
    }

    // logout and go back to login
    private void logout() {
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
}