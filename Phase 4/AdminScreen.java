import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdminScreen extends JPanel {
    private final FoodDeliveryLoginUI parent;
    private final JTable customersTable;
    private final JTable ordersTable;
    private final DefaultTableModel customersModel;
    private final DefaultTableModel ordersModel;

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

    private void refreshData() {
    // Clear existing data
    customersModel.setRowCount(0);
    ordersModel.setRowCount(0);

    // --- Fetch customers from users.db ---
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
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
            "Error fetching customers: " + ex.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE);
    }

    // --- Fetch orders from orders.db ---
    try (Connection orderConn = DriverManager.getConnection("jdbc:sqlite:orders.db")) {
        try (PreparedStatement orderStmt = orderConn.prepareStatement(
                "SELECT o.order_id, o.customer_username, o.restaurant_name, o.status, " +
                "o.total_amount, o.item_count, o.estimated_minutes, " +
                "((o.created_at + (o.estimated_minutes * 60)) - strftime('%s', 'now')) / 60 as minutes_remaining " +
                "FROM orders o " +
                "ORDER BY o.created_at DESC")) {
            
            try (ResultSet rs = orderStmt.executeQuery()) {
                while (rs.next()) {
                    String etaDisplay = "N/A";
                    if (!"CANCELLED".equals(rs.getString("status")) && 
                        !"DELIVERED".equals(rs.getString("status"))) {
                        int minutesRemaining = rs.getInt("minutes_remaining");
                        etaDisplay = minutesRemaining > 0 ? minutesRemaining + " min" : "Due now";
                    }
                    
                    Object[] rowData = {
                        rs.getLong("order_id"),
                        rs.getString("customer_username"),
                        rs.getString("restaurant_name"),
                        rs.getString("status"),
                        String.format("$%.2f", rs.getDouble("total_amount")),
                        rs.getInt("item_count"),
                        etaDisplay
                    };
                    ordersModel.addRow(rowData);
                }
            }
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this,
            "Error fetching orders: " + ex.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE);
    }
}

    

    private void cancelSelectedOrder() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an order to cancel.",
                "No Order Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        long orderId = Long.parseLong(ordersTable.getValueAt(selectedRow, 0).toString());
        String customer = ordersTable.getValueAt(selectedRow, 1).toString();
        String currentStatus = ordersTable.getValueAt(selectedRow, 3).toString();

        if ("CANCELLED".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this,
                "This order is already cancelled.",
                "Order Status",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if ("DELIVERED".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this,
                "Cannot cancel a delivered order.",
                "Order Status",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel order #" + orderId + " for customer " + customer + "?",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                parent.userDb.cancelOrder(orderId);
                refreshData(); // Refresh to show updated status
                JOptionPane.showMessageDialog(this,
                    "Order #" + orderId + " has been cancelled.",
                    "Order Cancelled",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error cancelling order: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
