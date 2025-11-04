//DriverScreen.java
import java.awt.*;
import javax.swing.*;

/* This class represents the screen displayed to drivers after they log in.
 * It provides options for drivers to manage their orders, view history, and update their payment methods.
 * Allows them to navigate to different driver-related functionalities.
 * payment methods.
 * order history, and update their payment methods.
 * make sure it is a JPanel, instead of JFrame, dont want multiple frames open
 */
public class DriverScreen extends JPanel {
	// Reference to the parent UI and the driver's username
    private final FoodDeliveryLoginUI parent;
    private final String username;
//create the driver screen with buttons to navigate to different functionalities
    //create the driver screen with buttons to navigate to different functionalities
    public DriverScreen(FoodDeliveryLoginUI parent, String username) {
	this.parent = parent;
	this.username = username == null || username.isEmpty() ? "Driver" : username;
	initUI();
    }

    private void initUI() {
	setLayout(new BorderLayout(8,8));
	setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		// Title
	JLabel title = new JLabel("Welcome Driver " + username, SwingConstants.LEFT);
	title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
	add(title, BorderLayout.NORTH);
// Center panel with buttons
	JPanel center = new JPanel();
	center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
	center.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
		//Create buttons; get order, delivery history, payment history, payment method
	JButton getOrderBtn = new JButton("GetOrder");
	JButton deliveryHistoryBtn = new JButton("Delivery History");
	JButton paymentHistoryBtn = new JButton("Payment History");
	JButton paymentMethodBtn = new JButton("Payment Method");
// Set uniform size and alignment for buttons, *tricky*
	Dimension btnSize = new Dimension(160, 30);
	getOrderBtn.setMaximumSize(btnSize);
	deliveryHistoryBtn.setMaximumSize(btnSize);
	paymentHistoryBtn.setMaximumSize(btnSize);
	paymentMethodBtn.setMaximumSize(btnSize);
		// Align buttons to the left
	getOrderBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
	deliveryHistoryBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
	paymentHistoryBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
	paymentMethodBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
		// Add buttons to center panel with spacing, so its neat
	center.add(getOrderBtn);
	center.add(Box.createRigidArea(new Dimension(0,6)));
	center.add(deliveryHistoryBtn);
	center.add(Box.createRigidArea(new Dimension(0,6)));
	center.add(paymentHistoryBtn);
	center.add(Box.createRigidArea(new Dimension(0,6)));
	center.add(paymentMethodBtn);

	add(center, BorderLayout.CENTER);

	

	

	// Connect to DriverGetOrder screen
	getOrderBtn.addActionListener(e -> { // Navigate to DriverGetOrder screen
		DriverGetOrder getOrderScreen = new DriverGetOrder(parent, username);
		try {
			parent.getSceneSorter().addScene("DriverGetOrder", getOrderScreen);
		} catch (IllegalArgumentException ex) {
			// Scene already exists, that's fine
		}
		parent.getSceneSorter().switchPage("DriverGetOrder");
	});

	deliveryHistoryBtn.addActionListener(e -> { // Navigate to DriverHistory screen
		DriveryHistory historyScreen = new DriveryHistory(parent, username);
		try {
			parent.getSceneSorter().addScene("DriverHistory", historyScreen);
		} catch (IllegalArgumentException ex) {
			// Scene already exists, that's fine
		}
		parent.getSceneSorter().switchPage("DriverHistory"); // Switch to DriverHistory screen
	});
	// Payment History button (not implemented) *work in progress*
	paymentHistoryBtn.addActionListener(e -> JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
		"Payment history not implemented yet.", "Payment History", JOptionPane.INFORMATION_MESSAGE));
	// Payment Method button
	paymentMethodBtn.addActionListener(e -> { // Navigate to DriverSetPaymentMethod screen
		DriverSetPaymentMethod paymentMethodScreen = new DriverSetPaymentMethod(parent, username);
		try {// Add the scene if it doesn't already exist
			parent.getSceneSorter().addScene("DriverSetPaymentMethod", paymentMethodScreen);
		} catch (IllegalArgumentException ex) {
			// Scene already exists, that's fine
		}
		parent.getSceneSorter().switchPage("DriverSetPaymentMethod");// Switch to DriverSetPaymentMethod screen
		
	});
	
    }
}
