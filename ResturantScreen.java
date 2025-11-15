// ResturantScreen.java 
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;
/*
 * ResturantScreen displays a list of available restaurants based on the user's zip code.
 * Users can select a restaurant to view its menu and place an order.
 * The RestaurantUI is designed for clarity and ease of use, with compact spacing and clear fonts.
 * Each restaurant is displayed with its name, address, and an "Order here" button.
 * Goal: Provide a user-friendly interface for browsing restaurants and placing orders. * will see if we get there*
 */
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
        
        // Create header panel 
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
        headerPanel.setBackground(new Color(245, 245, 245));
        
        // Header title
        JLabel header = new JLabel("Available Restaurants - " + zip, SwingConstants.LEFT);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 14f));
        headerPanel.add(header, BorderLayout.CENTER);

        // Back button in header (styled like "Order Here")
        JButton backBtnHeader = new JButton("Back");
        backBtnHeader.setFont(backBtnHeader.getFont().deriveFont(Font.BOLD, 12f));
        backBtnHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 1),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        backBtnHeader.setBackground(new Color(0, 123, 255));
        backBtnHeader.setForeground(Color.WHITE);
        backBtnHeader.setOpaque(true);
        backBtnHeader.setFocusPainted(false);
        backBtnHeader.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtnHeader.addActionListener(e -> {
            MainScreen mainScreen = new MainScreen(parent, username);
            try {
                parent.getSceneSorter().addScene("MainScreen", mainScreen);
            } catch (IllegalArgumentException ex) {
                // Scene already exists; ignore and just switch
            }
            parent.getSceneSorter().switchPage("MainScreen");
        });
        headerPanel.add(backBtnHeader, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content panel with improved spacing and subtle background
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        content.setBackground(new Color(250, 250, 250));
        // Sample restaurants based on zip code * bridgewater zip for demo *
        if ("02325".equals(zip)) {
            content.add(createRestaurantRow("Crimson Dining", "125 Burrill Ave"));
            content.add(Box.createVerticalStrut(6));
            content.add(createRestaurantRow("Barrett's Alehouse Bridgewater", "425 Bedford St"));
            content.add(Box.createVerticalStrut(6));
            content.add(createRestaurantRow("Greyhound Tavern", "39 Broad Street"));
            // space before footer back button
            content.add(Box.createVerticalStrut(8));
        } else { // No restaurants for other zip codes in this demo * for now *
            JPanel noResultsPanel = new JPanel(new BorderLayout());
            noResultsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            
            JLabel none = new JLabel("No restaurants available in " + zip, SwingConstants.CENTER);
            none.setFont(none.getFont().deriveFont(12f));
            none.setForeground(Color.GRAY);
            
            noResultsPanel.add(none, BorderLayout.CENTER);
            content.add(noResultsPanel);
            // space before footer back button
            content.add(Box.createVerticalStrut(8));
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.getViewport().setBackground(content.getBackground());
        add(scroll, BorderLayout.CENTER);

        
    }
    // Creates a row panel for a restaurant entry
    private JPanel createRestaurantRow(String name, String address) {
        JPanel row = new JPanel(new BorderLayout(8,6));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        row.setBackground(Color.WHITE);
        row.setOpaque(true);
        // Info panel with name and address
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        
        // Restaurant name in bold
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));
        info.add(nameLabel);
        
        // Address in smaller font
        JLabel addressLabel = new JLabel(address);
        addressLabel.setFont(addressLabel.getFont().deriveFont(10f));
        addressLabel.setForeground(Color.DARK_GRAY);
        info.add(addressLabel);
        
        row.add(info, BorderLayout.CENTER);
        // "Order here" button styled consistently
        JButton orderBtn = new JButton("Order Here");
        orderBtn.setFont(orderBtn.getFont().deriveFont(Font.BOLD, 12f));
        orderBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 1),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        orderBtn.setBackground(new Color(0, 123, 255));
        orderBtn.setForeground(Color.WHITE);
        orderBtn.setOpaque(true);
        orderBtn.addActionListener(e -> createOrder(name));
        row.add(orderBtn, BorderLayout.EAST);
        return row;
    }
    // Opens the order dialog for the selected restaurant, and shows what food is there to order *place holder for all restaurants*
    private void createOrder(String restaurantName) {
        // Restaurant addresses mapped to names
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
        // Show menu options (simplified for demo)
        String[] menuItems = {
            "Burger - $12.99",
            "Pizza - $15.99",
            "Salad - $8.99",
            "Pasta - $13.99",
            "Sandwich - $9.99"
        };
        double[] prices = {12.99, 15.99, 8.99, 13.99, 9.99}; // Corresponding prices for each item

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // Menu item checkboxes and quantity spinners, so you can order multiple of same item
        JLabel label = new JLabel("Select items to order from " + restaurantName);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(10));
        
        JCheckBox[] checkBoxes = new JCheckBox[menuItems.length];
        JSpinner[] quantities = new JSpinner[menuItems.length];
        // Create a row for each menu item, so its nice and neat * at least i tried*
        for (int i = 0; i < menuItems.length; i++) {
            JPanel itemPanel = new JPanel();
            itemPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
            itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            itemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            // Checkbox for item
            checkBoxes[i] = new JCheckBox(menuItems[i]);
            checkBoxes[i].setFont(checkBoxes[i].getFont().deriveFont(12f));
            //
            quantities[i] = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
            quantities[i].setPreferredSize(new Dimension(60, 25));
            ((JSpinner.DefaultEditor)quantities[i].getEditor()).getTextField().setColumns(2);
            
            itemPanel.add(checkBoxes[i]);
            itemPanel.add(quantities[i]);
            panel.add(itemPanel);
        }
        
        // Add a note about quantity, can choose how many to order
        JLabel noteLabel = new JLabel("* Use spinners to select quantity (1-10)");
        noteLabel.setFont(noteLabel.getFont().deriveFont(10f));
        noteLabel.setForeground(Color.GRAY);
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(Box.createVerticalStrut(10));
        panel.add(noteLabel);
        // Show dialog
        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Order from " + restaurantName,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
// If user clicked OK, process the order
        if (result == JOptionPane.OK_OPTION) {
            double total = 0.0;
            boolean anySelected = false;
            
            StringBuilder orderDetails = new StringBuilder();
            orderDetails.append("Order Summary:\n\n");
       // Calculate total and build order details     
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
        // Ensure at least one item is selected    
            if (!anySelected) {
                JOptionPane.showMessageDialog(this,
                    "Please select at least one item to order.",
                    "No Items Selected",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
           // Show order confirmation with total 
            orderDetails.append(String.format("\nTotal: $%.2f", total));
            // Show order confirmation dialog
            int confirm = JOptionPane.showConfirmDialog(this,
                orderDetails.toString(),
                "Confirm Order",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
               // If user confirmed the order, proceed to create it 
            if (confirm == JOptionPane.OK_OPTION) {
                try {
                    // Count total items
                    int totalItems = 0;
                    for (int i = 0; i < checkBoxes.length; i++) {
                        if (checkBoxes[i].isSelected()) {
                            totalItems += (Integer)quantities[i].getValue();
                        }
                    }
                    
                    // Get active payment method
                    PaymentInformation paymentInfo = parent.paymentDb.getActivePaymentMethod(username);
                    if (paymentInfo == null) {
                        JOptionPane.showMessageDialog(this,
                            "Please set up a payment method first.",
                            "Payment Required",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Create the order in the database
                    long orderId = parent.orderDb.createOrder(username, restaurantName, restaurantAddress,
                        "123 Main St", // TODO: Get actual delivery address
                        "No special instructions", // TODO: Add special instructions field
                        total,
                        totalItems,  // Using the total items count we calculated above
                        paymentInfo.getPaymentType());
                    
                    // Add each ordered item to the database
                    for (int i = 0; i < checkBoxes.length; i++) {
                        if (checkBoxes[i].isSelected()) {
                            int quantity = (Integer)quantities[i].getValue();
                            String itemName = menuItems[i].split(" - ")[0];
                            double price = prices[i];
                            parent.orderDb.addOrderItem(orderId, itemName, quantity, price, null);
                        }
                    }
                    
                    // Calculate and show ETA
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
                        
                    // Switch back to main screen to show order status
                    MainScreen mainScreen = new MainScreen(parent, username);
                    parent.getSceneSorter().addScene("MainScreen", mainScreen);
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
