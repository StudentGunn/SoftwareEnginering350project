import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CustomerOrderHistory extends JPanel {
    private FoodDeliveryLoginUI parent;
    private String username;
    private DefaultTableModel tableModel;

    public CustomerOrderHistory(FoodDeliveryLoginUI parent, String username) {
        this.parent = parent;
        this.username = username;

        String[] columns = {"Order ID", "Restaurant", "Total", "Status", "Date"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);

        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton("Back");
        JButton refreshBtn = new JButton("Refresh");
        backBtn.addActionListener(e -> parent.getSceneSorter().switchPage("MainScreen"));
        refreshBtn.addActionListener(e -> loadHistory());
        topPanel.add(backBtn);
        topPanel.add(refreshBtn);
        add(topPanel, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        loadHistory();
    }

    private void loadHistory() {
        tableModel.setRowCount(0);
        try {
            String sql = "SELECT order_id, restaurant_name, total_amount, status, created_at FROM orders WHERE customer_username = ? ORDER BY created_at DESC";
            Connection conn = DriverManager.getConnection(parent.orderDb.getConnectionUrl());
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getLong("order_id"),
                    rs.getString("restaurant_name"),
                    String.format("$%.2f", rs.getDouble("total_amount")),
                    rs.getString("status"),
                    new java.text.SimpleDateFormat("MM/dd/yyyy").format(new java.util.Date(rs.getLong("created_at") * 1000L))
                });
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
