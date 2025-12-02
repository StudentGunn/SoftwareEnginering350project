import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * AdminScreen displays the admin interface for managing customers and orders.
 * Provides data tables for active customers and current orders with refresh and cancel capabilities.
 * Accesses UserDataBase for user management and OrderDatabase for order data.
 */
public class AdminScreen extends JPanel {
    private final FoodDeliveryLoginUI parent;
    private final JTable customersTable;
    private final JTable ordersTable;
    private final DefaultTableModel customersModel;
    private final DefaultTableModel ordersModel;

    /**
     * Constructs an AdminScreen with the given parent UI.
     * @param parent The main application UI frame.
     */
    public AdminScreen(FoodDeliveryLoginUI parent) {
        this.parent = parent;
        
        // Create table models
        String[] customerColumns = {"Username", "Full Name", "Email", "Phone", "Status"};
        String[] orderColumns = {"Order ID", "Customer", "Restaurant", "Status", "Total", "Items", "ETA (mins)"};
        
        customersModel = new DefaultTableModel(customerColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        ordersModel = new DefaultTableModel(orderColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        customersTable = new JTable(customersModel);
        ordersTable = new JTable(ordersModel);
        
        initUI();
        refreshData();
    }
/*
 * Initializes the user interface components for the Admin Screen
 * Sets up layout, panels, buttons, and tables
 * Configures action listeners for buttons
 * Initializes data retrieval for tables
 * Sets up table models and data rendering, for customers and orders
 */
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Admin header panel with title and logout button
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("Admin Control Panel", SwingConstants.LEFT);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            parent.getSceneSorter().switchPage("Login");
        });
        headerPanel.add(logoutBtn, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // Create split pane for customers and orders
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(200);

        // Customers panel
        JPanel customersPanel = new JPanel(new BorderLayout(5, 5));
        customersPanel.setBorder(BorderFactory.createTitledBorder("Active Customers"));
        
        JPanel customerButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshCustomersBtn = new JButton("Refresh");
        refreshCustomersBtn.addActionListener(e -> refreshData());
        customerButtonsPanel.add(refreshCustomersBtn);
        
        customersPanel.add(new JScrollPane(customersTable), BorderLayout.CENTER);
        customersPanel.add(customerButtonsPanel, BorderLayout.SOUTH);

        // Orders panel
        JPanel ordersPanel = new JPanel(new BorderLayout(5, 5));
        ordersPanel.setBorder(BorderFactory.createTitledBorder("Current Orders"));
        
        JPanel orderButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton cancelOrderBtn = new JButton("Cancel Selected Order");
        JButton refreshOrdersBtn = new JButton("Refresh Orders");
        
        cancelOrderBtn.addActionListener(e -> cancelSelectedOrder());
        refreshOrdersBtn.addActionListener(e -> refreshData());
        
        orderButtonsPanel.add(cancelOrderBtn);
        orderButtonsPanel.add(refreshOrdersBtn);
        
        ordersPanel.add(new JScrollPane(ordersTable), BorderLayout.CENTER);
        ordersPanel.add(orderButtonsPanel, BorderLayout.SOUTH);

        // Add panels to split pane
        splitPane.setTopComponent(customersPanel);
        splitPane.setBottomComponent(ordersPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    // Allows the user to refresh the page.
    private void refreshData() {
    // Clear existing data
    customersModel.setRowCount(0);
    ordersModel.setRowCount(0);

    //  Connect to grab info customers from users.db
    try (Connection userConn = DriverManager.getConnection(parent.userDb.getConnectionUrl())) {
        try (PreparedStatement custStmt = userConn.prepareStatement(
                "SELECT username, full_name, email, phone FROM users WHERE user_type = 'CUSTOMER'")) {
            
            try (ResultSet rs = custStmt.executeQuery()) {
                while (rs.next()) {
                    Object[] rowData = {
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        "Active" // Placeholder for session tracking
                    };
                    customersModel.addRow(rowData);
                }
            }
        }
        // Handle SQL exceptions
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
            "Error fetching customers: " + ex.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE);
    }

    // Connect to grab order info from orders.db 
    try (Connection orderConn = DriverManager.getConnection("jdbc:sqlite:orders.db")) { // Hardcoded path to orders.db
        try (PreparedStatement orderStmt = orderConn.prepareStatement( // Query to get order details
                "SELECT o.order_id, o.customer_username, o.restaurant_name, o.status, " +
                "o.total_amount, o.item_count, o.estimated_minutes, " +
                "((o.created_at + (o.estimated_minutes * 60)) - strftime('%s', 'now')) / 60 as minutes_remaining " +
                "FROM orders o " +
                "ORDER BY o.created_at DESC"))  { 
            // Execute query and populate orders table
            try (ResultSet rs = orderStmt.executeQuery()) {
                while (rs.next()) { // Iterate through results
                    String etaDisplay = "N/A"; // Default ETA display
                    if (!"CANCELLED".equals(rs.getString("status")) && 
                        !"DELIVERED".equals(rs.getString("status"))) { // Only calculate ETA for active orders
                        int minutesRemaining = rs.getInt("minutes_remaining"); // Get calculated minutes remaining
                        etaDisplay = minutesRemaining > 0 ? minutesRemaining + " min" : "Due now"; // Format ETA display
                    }
                    // Prepare row data for orders table
                    Object[] rowData = { 
                        rs.getLong("order_id"),// Order ID
                        rs.getString("customer_username"),// Customer username
                        rs.getString("restaurant_name"),// Restaurant name
                        rs.getString("status"),// Order status
                        String.format("$%.2f", rs.getDouble("total_amount")),// Total amount
                        rs.getInt("item_count"),// Item count
                        etaDisplay// Estimated time of arrival display
                    };
                    // Add row to orders table model
                    ordersModel.addRow(rowData);
                }
            }
        }
        // Handle SQL exceptions
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
            "Error fetching orders: " + ex.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE);
    }
}

    /*
     * Cancels the selected order from the orders table
     * Prompts for confirmation before cancelling
     * Updates the database and refreshes the data display
     * Handles SQL exceptions and shows error messages
     * Gives feedback to admin on success or failure, based on operation outcome
     */

    private void cancelSelectedOrder() {
        int selectedRow = ordersTable.getSelectedRow();// Get selected row index
        if (selectedRow == -1) { // No row selected
            JOptionPane.showMessageDialog(this, // Show warning dialog
                "Please select an order to cancel.", // Warning message
                "No Order Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        long orderId = Long.parseLong(ordersTable.getValueAt(selectedRow, 0).toString()); // Get order ID
        String customer = ordersTable.getValueAt(selectedRow, 1).toString(); // Get customer username
        String currentStatus = ordersTable.getValueAt(selectedRow, 3).toString(); // Get current order status

        if ("CANCELLED".equals(currentStatus)) {// Already cancelled
            JOptionPane.showMessageDialog(this, // Show info dialog
                "This order is already cancelled.", // Info message
                "Order Status", // Dialog title
                JOptionPane.INFORMATION_MESSAGE); // Information message type
            return;
        }

        if ("DELIVERED".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this, // Show warning dialog
                "Cannot cancel a delivered order.", // Warning message
                "Order Status", // Dialog title
                JOptionPane.WARNING_MESSAGE); // Warning message type
            return;
        }
        // Confirm cancellation
        int confirm = JOptionPane.showConfirmDialog(this, // Show confirmation dialog
            "Are you sure you want to cancel order #" + orderId + " for customer " + customer + "?",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION);
            // If confirmed, proceed with cancellation
        if (confirm == JOptionPane.YES_OPTION) {
            try { // Attempt to cancel order in database
                parent.orderDb.cancelOrder(orderId);
                refreshData(); // Refresh to show updated status
                JOptionPane.showMessageDialog(this,
                    "Order #" + orderId + " has been cancelled.",
                    "Order Cancelled",
                    JOptionPane.INFORMATION_MESSAGE); // Information message type
            } catch (SQLException ex) { // Handle SQL exceptions
                JOptionPane.showMessageDialog(this,
                    "Error cancelling order: " + ex.getMessage(),// Show error dialog
                    "Database Error",// Dialog title
                    JOptionPane.ERROR_MESSAGE);// Error message type
            }
        }
    }
}
