import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * DriverPaymentHistory displays the driver's payment transaction history.
 * Shows payment dates, amounts, status, order IDs, payment methods, and details.
 */
public class DriverPaymentHistory extends JPanel {
	private final FoodDeliveryLoginUI parent;
	private final String username;
	private final JTable table;
	private final DefaultTableModel model;
	private final DecimalFormat currency = new DecimalFormat("$#,##0.00");
    
	/**
	 * Constructs a DriverPaymentHistory with the given parent and username.
	 * @param parent The main application UI frame.
	 * @param username The logged-in driver's username.
	 */
	public DriverPaymentHistory(FoodDeliveryLoginUI parent, String username) {
		this.parent = parent;
        // Default to "Driver" if username is null/empty *tricky*
		this.username = username == null || username.isEmpty() ? "Driver" : username;
        // Set up table model; based on payment transaction fields
		String[] cols = { "Date", "Amount", "Status", "Order ID", "Method", "Details" };
		model = new DefaultTableModel(cols, 0) {
            // Override to make cells non-editable; so it doesnt get messed up
			@Override 
			public boolean isCellEditable(int r, int c) { return false; }
		};
        // Create JTable with the model
		table = new JTable(model);
        // call initUI to set up the layout and loadPayments to fetch data/payment history
		try {
			initUI();
			loadPayments();
		} catch (Exception e) {
			Logger.catchAndLogBug(e, "DriverPaymentHistory.constructor");
		}
	}
    /* Create UI Interface for Payment History button for driver Screen
       --> sets layout, adds title, back button, table, and refresh button
       --> 
    */
	private void initUI() {
		try {
			setLayout(new BorderLayout(10, 10));
			setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			JPanel header = new JPanel(new BorderLayout());
			JLabel title = new JLabel("Payment History", SwingConstants.LEFT);
			title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
			JButton back = new JButton("Back to Driver Menu");
			back.addActionListener(e -> parent.getSceneSorter().switchPage("DriverScreen"));
			header.add(title, BorderLayout.WEST);
			header.add(back, BorderLayout.EAST);
			add(header, BorderLayout.NORTH);

			add(new JScrollPane(table), BorderLayout.CENTER);

			JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JButton refresh = new JButton("Refresh");
			refresh.addActionListener(e -> loadPayments());
			actions.add(refresh);
			add(actions, BorderLayout.SOUTH);
		} catch (Exception e) {
			Logger.catchAndLogBug(e, "DriverPaymentHistory.initUI");
		}
	}
    /* Load payment history from the database and populate the table 
     --> queries payment_transactions and payment_methods tables for the driver's username
     --> populates the table model with formatted payment data
     --> Connects to PaymentDatabase to get connection URL
     --> creates SQL query with PreparedStatement to prevent SQL injection
     --> grab columns: date, amount, status, order ID, method, details
     --> if any SQL error occurs, show error dialog
    */
	private void loadPayments() {
		model.setRowCount(0); // Clear existing rows
        // Load payment history from the database for this driver
		try (Connection conn = DriverManager.getConnection(parent.paymentDb.getConnectionUrl()); // Get connection URL from PaymentDatabase
			 PreparedStatement ps = conn.prepareStatement(
				"SELECT t.id, t.order_id, t.amount, t.status, t.created_at, t.completed_at, t.error_message, " +
				"       m.payment_type, m.card_number, m.card_name, m.bank_name " +
				"FROM payment_transactions t " +
				"JOIN payment_methods m ON t.payment_method_id = m.id " +
				"WHERE m.username = ? " +
				"ORDER BY COALESCE(t.completed_at, t.created_at) DESC")) {
			ps.setString(1, username); // Set username parameter to prevent SQL injection
			try (ResultSet rs = ps.executeQuery()) { // Execute query
				while (rs.next()) { // Iterate through results
					long created = rs.getLong("created_at");
					long completed = rs.getLong("completed_at");
					long ts = completed > 0 ? completed : created; // Use completed time if available, else created time * if doesnt exist*
					String dateStr = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss") // Format timestamp to readable date string
						.format(new java.util.Date(ts * 1000L)); // Convert seconds to milliseconds

					double amount = rs.getDouble("amount"); // Get payment amount
					String status = rs.getString("status"); // Get payment status
					Long orderId = rs.getObject("order_id") != null ? rs.getLong("order_id") : null; // Get order ID if exists

					String type = rs.getString("payment_type"); // Get payment method type
					String method; 
					String details; 
					if ("CARD".equals(type)) { // Format card details for display
						String card = rs.getString("card_number"); // Get card number
						String last4 = card != null && card.length() >= 4 ? card.substring(card.length()-4) : "????"; // Get last 4 digits
						String name = rs.getString("card_name"); // Get cardholder name
						method = "CARD"; // Set method to CARD
						details = "Card •••• " + last4 + (name != null ? " (" + name + ")" : ""); // Format details string
					} else if ("BANK".equals(type)) { // Format bank details for display
						String bank = rs.getString("bank_name"); // Get bank name
						method = "BANK"; // Set method to BANK
						details = bank != null ? bank : "Bank Account"; // Format details string
					} else {
						method = type != null ? type : "?"; // Unknown method type
						details = rs.getString("error_message"); // Use error message as details if available
					}
                    // Add row to table model with formatted data
					Object[] row = new Object[] {
						dateStr,
						currency.format(amount),
						status,
						orderId,
						method,
						details != null ? details : ""
					};
                    // Add row to table model with formatted data
					model.addRow(row);
				}
			}
            // If any SQL error occurs, show error dialog
		} catch (SQLException ex) {
			Logger.catchAndLogBug(ex, "DriverPaymentHistory.loadPayments");
			JOptionPane.showMessageDialog(this,
				"Error loading payment history: " + ex.getMessage(),
				"Database Error",
				JOptionPane.ERROR_MESSAGE);
		} catch (Exception ex) {
			Logger.catchAndLogBug(ex, "DriverPaymentHistory.loadPayments");
			JOptionPane.showMessageDialog(this,
				"Unexpected error loading payment history: " + ex.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}
}
