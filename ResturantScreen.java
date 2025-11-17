// ResturantScreen.java
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;

public class ResturantScreen extends JPanel {
    private FoodDeliveryLoginUI parent;
    private String username;
    private String zip;

    public ResturantScreen(FoodDeliveryLoginUI parent, String username, String zip) {
        this.parent = parent;
        this.username = username;
        this.zip = zip;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(5,5));

        // header with green style
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15,20,15,20));
        headerPanel.setBackground(new Color(46, 125, 50));

        JLabel header = new JLabel("Restaurants - " + zip, SwingConstants.LEFT);
        header.setFont(new Font("Arial", Font.BOLD, 16));
        header.setForeground(Color.WHITE);
        headerPanel.add(header, BorderLayout.CENTER);

        // back button
        JButton backBtn = new JButton("Back");
        backBtn.setFont(new Font("Arial", Font.BOLD, 12));
        backBtn.setBackground(Color.WHITE);
        backBtn.setForeground(new Color(46, 125, 50));
        backBtn.setOpaque(true);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> {
            MainScreen mainScreen = new MainScreen(parent, username);
            try {
                parent.getSceneSorter().addScene("MainScreen", mainScreen);
            } catch (IllegalArgumentException ex) {
                // already exists
            }
            parent.getSceneSorter().switchPage("MainScreen");
        });
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // restaurant list area
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        content.setBackground(new Color(250, 250, 250));

        // bridgewater zip for demo// if not 02325 display nothing
        if ("02325".equals(zip)) {
            content.add(createRestaurantRow("Crimson Dining", "125 Burrill Ave"));
            content.add(Box.createVerticalStrut(6));
            content.add(createRestaurantRow("Barrett's Alehouse Bridgewater", "425 Bedford St"));
            content.add(Box.createVerticalStrut(6));
            content.add(createRestaurantRow("Greyhound Tavern", "39 Broad Street"));
            content.add(Box.createVerticalStrut(8));
        } else {
            // no restaurants for other zips yet
            JPanel noResultsPanel = new JPanel(new BorderLayout());
            noResultsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            // TODO: Here should have a update instance  
            JLabel none = new JLabel("No restaurants available in " + zip, SwingConstants.CENTER);
            none.setFont(new Font("Arial", Font.PLAIN, 12));
            none.setForeground(Color.GRAY);

            noResultsPanel.add(none, BorderLayout.CENTER);
            content.add(noResultsPanel);
            content.add(Box.createVerticalStrut(8));
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.getViewport().setBackground(content.getBackground());
        add(scroll, BorderLayout.CENTER);
    }

    // makes each restaurant row
    private JPanel createRestaurantRow(String name, String address) {
        JPanel row = new JPanel(new BorderLayout(8,6));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        row.setBackground(Color.WHITE);
        row.setOpaque(true);

        // restaurant info
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        info.add(nameLabel);

        JLabel addressLabel = new JLabel(address);
        addressLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        addressLabel.setForeground(Color.DARK_GRAY);
        info.add(addressLabel);

        row.add(info, BorderLayout.CENTER);

        // order button
        JButton orderBtn = new JButton("Order Here");
        orderBtn.setFont(new Font("Arial", Font.BOLD, 12));
        orderBtn.setBackground(new Color(46, 125, 50));
        orderBtn.setForeground(Color.WHITE);
        orderBtn.setOpaque(true);
        orderBtn.setBorderPainted(false);
        orderBtn.setFocusPainted(false);
        orderBtn.addActionListener(e -> createOrder(name));
        row.add(orderBtn, BorderLayout.EAST);
        return row;
    }
    // shows menu and places order
    private void createOrder(String restaurantName) {
        // get restaurant address
        String restaurantAddress;
        switch (restaurantName) {
            case "Crimson Dining":
                restaurantAddress = "125 Burrill Ave";
                break;
            case "Barrett's Alehouse Bridgewater":
                restaurantAddress = "425 Bedford St";
                break;
            case "Greyhound Tavern":
                restaurantAddress = "39 Broad Street";
                break;
            default:
                restaurantAddress = "Address not available";
                break;
        }

        // menu items
        String[] menuItems = {
            "Burger - $12.99",
            "Pizza - $15.99",
            "Salad - $8.99",
            "Pasta - $13.99",
            "Sandwich - $9.99"
        };
        double[] prices = {12.99, 15.99, 8.99, 13.99, 9.99};

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
        noteLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        noteLabel.setForeground(Color.GRAY);
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(Box.createVerticalStrut(10));
        panel.add(noteLabel);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Order from " + restaurantName,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
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

            if (!anySelected) {
                JOptionPane.showMessageDialog(this,
                    "Please select at least one item to order.",
                    "No Items Selected",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            orderDetails.append(String.format("\nTotal: $%.2f", total));

            int confirm = JOptionPane.showConfirmDialog(this,
                orderDetails.toString(),
                "Confirm Order",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);

            if (confirm == JOptionPane.OK_OPTION) {
                try {
                    // count items
                    int totalItems = 0;
                    for (int i = 0; i < checkBoxes.length; i++) {
                        if (checkBoxes[i].isSelected()) {
                            totalItems += (Integer)quantities[i].getValue();
                        }
                    }

                    // check payment method
                    PaymentInformation paymentInfo = parent.paymentDb.getActivePaymentMethod(username);
                    if (paymentInfo == null) {
                        JOptionPane.showMessageDialog(this,
                            "Please set up a payment method first.",
                            "Payment Required",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // create order in db
                    long orderId = parent.orderDb.createOrder(username, restaurantName, restaurantAddress,
                        "123 Main St",
                        "No special instructions",
                        total,
                        totalItems,
                        paymentInfo.getPaymentType());

                    // add items to order
                    for (int i = 0; i < checkBoxes.length; i++) {
                        if (checkBoxes[i].isSelected()) {
                            int quantity = (Integer)quantities[i].getValue();
                            String itemName = menuItems[i].split(" - ")[0];
                            double price = prices[i];
                            parent.orderDb.addOrderItem(orderId, itemName, quantity, price, null);
                        }
                    }

                    // show eta
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

                    // go back to main screen
                    MainScreen mainScreen = new MainScreen(parent, username);
                    try {
                        parent.getSceneSorter().addScene("MainScreen", mainScreen);
                    } catch (IllegalArgumentException ex) {
                        // already exists; reuse
                    }
                    parent.getSceneSorter().switchPage("MainScreen");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                        "Error creating order: " + ex.getMessage(),
                        "Order Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
