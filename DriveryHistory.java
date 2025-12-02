import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * DriveryHistory displays the driver's completed delivery history.
 * Shows order details including customer, restaurant, total, status, date, and payment.
 */
public class DriveryHistory extends JPanel {
    private final FoodDeliveryLoginUI parent;
    private final String username;
    private final JTable historyTable;
    private final DefaultTableModel tableModel;

    /**
     * Constructs a DriveryHistory with the given parent and username.
     * @param parent The main application UI frame.
     * @param username The logged-in driver's username.
     */
    public DriveryHistory(FoodDeliveryLoginUI parent, String username) {
        this.parent = parent;
        this.username = username;

        // Create table model with columns
        String[] columns = {
            "Order ID", 
            "Customer", 
            "Restaurant", 
            "Total", 
            "Status", 
            "Date",
            "Payment"
        };
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        historyTable = new JTable(tableModel);
        initUI();
        loadDeliveryHistory();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Delivery History", SwingConstants.LEFT);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        
        JButton backBtn = new JButton("Back to Driver Menu");
        backBtn.addActionListener(e -> parent.getSceneSorter().switchPage("DriverScreen"));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table Panel
        JScrollPane scrollPane = new JScrollPane(historyTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadDeliveryHistory());
        buttonPanel.add(refreshBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadDeliveryHistory() {
        // Clear existing data
        tableModel.setRowCount(0);

        try {
            String sql = "SELECT o.order_id, o.customer_username, o.restaurant_name, " +
                        "o.status, o.total_amount, o.created_at, " +
                        "COALESCE(o.payment_status, 'PENDING') as payment_status, " +
                        "o.delivery_address, o.assigned_at " +
                        "FROM orders o " +
                        "WHERE o.driver_username = ? " +
                        "AND o.status IN ('ASSIGNED', 'IN_PROGRESS', 'DELIVERED', 'CANCELLED') " +
                        "ORDER BY o.created_at DESC";

            try (Connection conn = DriverManager.getConnection(parent.orderDb.getConnectionUrl());
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String status = rs.getString("status");
                        String paymentStatus = rs.getString("payment_status");
                        if (paymentStatus == null) {
                            paymentStatus = "PENDING";
                        }

                        // Format the date
                        long timestamp = rs.getLong("created_at");
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                        String dateStr = sdf.format(new java.util.Date(timestamp * 1000L));

                        Object[] row = {
                            rs.getLong("order_id"),
                            rs.getString("customer_username"),
                            rs.getString("restaurant_name"),
                            String.format("$%.2f", rs.getDouble("total_amount")),
                            status,
                            dateStr,
                            paymentStatus
                        };
                        tableModel.addRow(row);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.catchAndLogBug(ex, "DriveryHistory");
            JOptionPane.showMessageDialog(this,
                "Error loading delivery history: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
