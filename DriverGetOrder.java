import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.table.*;
 /*
--> The main screen used for a driver to pick orders to deliver.
--> Shows a table that displays currently unclaimed orders.
--> The driver will claim an order, at which point the driver will be assigned to it.
--> This order will be removed from the unclaimed orders, and will no longer appear.
 */
public class DriverGetOrder extends JPanel {
    private final FoodDeliveryLoginUI parent;
    private final String username;
    private final JTable ordersTable;
    private final DefaultTableModel ordersModel;
    private final DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    private static final double DRIVER_COMMISSION = 0.30; // 30% commission

    public DriverGetOrder(FoodDeliveryLoginUI parent, String username) {
        this.parent = parent;
        this.username = username;

        // Create table model
        String[] columns = {
            "Order ID", "Restaurant", "Address", "Items", "Total", "Driver Pay", 
            "Ready In", "Delivery Time", "Status"
        };
        
        ordersModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        ordersTable = new JTable(ordersModel);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        try {
            initUI();
            refreshOrders();
        } catch (Exception e) {
            Logger.catchAndLogBug(e, "DriverGetOrder.constructor");
        }
    }

    private void initUI() {
        try {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Header Panel
            JPanel headerPanel = new JPanel(new BorderLayout());
            JLabel titleLabel = new JLabel("Available Orders", SwingConstants.LEFT);
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
            
            JButton backBtn = new JButton("Back to Driver Menu");
            backBtn.addActionListener(e -> parent.getSceneSorter().switchPage("DriverScreen"));
            
            headerPanel.add(titleLabel, BorderLayout.WEST);
            headerPanel.add(backBtn, BorderLayout.EAST);
            add(headerPanel, BorderLayout.NORTH);

            // Orders Table
            JScrollPane scrollPane = new JScrollPane(ordersTable);
            add(scrollPane, BorderLayout.CENTER);

            // Button Panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            
            JButton acceptOrderBtn = new JButton("Accept Selected Order");
            JButton refreshBtn = new JButton("Refresh Orders");
            
            acceptOrderBtn.addActionListener(e -> acceptSelectedOrder());
            refreshBtn.addActionListener(e -> refreshOrders());
            
            buttonPanel.add(acceptOrderBtn);
            buttonPanel.add(refreshBtn);
            
            add(buttonPanel, BorderLayout.SOUTH);
        } catch (Exception e) {
            Logger.catchAndLogBug(e, "DriverGetOrder.initUI");
        }
    }
    // Refreshes the page, in case new orders appear while still on the screen.
    private void refreshOrders() {
        ordersModel.setRowCount(0);
        
        try (Connection conn = DriverManager.getConnection(parent.orderDb.getConnectionUrl());
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT o.order_id, o.restaurant_name, o.restaurant_address, o.total_amount, " +
                "o.estimated_minutes, o.status, o.created_at, " +
                "GROUP_CONCAT(oi.item_name || ' x' || oi.quantity) as items, " +
                "SUM(oi.quantity) as item_count " +
                "FROM orders o " +
                "LEFT JOIN order_items oi ON o.order_id = oi.order_id " +
                "WHERE o.status = 'PENDING' AND o.driver_username IS NULL " +
                "GROUP BY o.order_id " +
                "ORDER BY o.created_at DESC")) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int itemCount = rs.getInt("item_count");
                String items = rs.getString("items");
                double totalAmount = rs.getDouble("total_amount");
                double driverPay = totalAmount * DRIVER_COMMISSION;
                int baseEta = rs.getInt("estimated_minutes");
                int plusEta = MapCalculator.calculateETA(rs.getDouble("restaurantLat"), rs.getDouble("restaurantLon"), rs.getDouble("deliveryLat"), rs.getDouble("deliveryLon"));
                int deliveryEta = (baseEta+plusEta);
                Object[] row = {
                    rs.getLong("order_id"),
                    rs.getString("restaurant_name"),
                    rs.getString("restaurant_address"),
                    items != null ? items : "No items",
                    currencyFormat.format(totalAmount),
                    currencyFormat.format(driverPay),
                    baseEta + " mins",
                    deliveryEta + " mins",
                    rs.getString("status")
                };
                ordersModel.addRow(row);
            }
        } catch (SQLException ex) {
            Logger.catchAndLogBug(ex, "DriverGetOrder.refreshOrders");
            JOptionPane.showMessageDialog(this,
                "Error loading orders: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            Logger.catchAndLogBug(ex, "DriverGetOrder.refreshOrders");
            JOptionPane.showMessageDialog(this,
                "Unexpected error loading orders: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // The prompt used by the select Order button.
     // Used to assign orders to a specific driver, and switch their status accordingly.
    private void acceptSelectedOrder() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an order to accept.",
                "No Order Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        long orderId = (long)ordersTable.getValueAt(selectedRow, 0);
        String restaurant = (String)ordersTable.getValueAt(selectedRow, 1);
        String address = (String)ordersTable.getValueAt(selectedRow, 2);
        String driverPay = (String)ordersTable.getValueAt(selectedRow, 5);

        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Accept order #%d from %s?\nAddress: %s\nYou will earn %s for this delivery.",
                orderId, restaurant, address, driverPay),
            "Confirm Order Acceptance",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                parent.orderDb.assignDriverToOrder(orderId, username);
                // Update driver status to ON_DELIVERY
                parent.driverDb.updateDriverStatus(username, "ON_DELIVERY");
                JOptionPane.showMessageDialog(this,
                    "Order accepted successfully! Head to " + restaurant + " at " + address + " to pick up the order.",
                    "Order Accepted",
                    JOptionPane.INFORMATION_MESSAGE);
                refreshOrders();
            } catch (SQLException ex) {
                Logger.catchAndLogBug(ex, "DriverGetOrder.acceptSelectedOrder");
                JOptionPane.showMessageDialog(this,
                    "Error accepting order: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                Logger.catchAndLogBug(ex, "DriverGetOrder.acceptSelectedOrder");
                JOptionPane.showMessageDialog(this,
                    "Unexpected error accepting order: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
