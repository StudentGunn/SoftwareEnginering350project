//DriverScreen.java
import java.awt.*;
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

       // header
        JPanel headerBar = new JPanel(new BorderLayout(10, 0));
        headerBar.setBackground(new Color(46, 125, 50));
        headerBar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);

        // logout button
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
        JButton confirmDropoffBtn = new JButton("Confrim Food Drop off");
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
        
        cashOutBtn.addActionListener(e->{
            JOptionPane.showMessageDialog(null,("Giving you your money"));
        
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
                JOptionPane.showMessageDialog(this, "Error updating order: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        logoutBtn.addActionListener(e -> logout());
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
