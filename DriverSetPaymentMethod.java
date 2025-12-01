import java.awt.*;
import java.sql.*;
import java.text.ParseException;
import javax.swing.*;
import javax.swing.text.*;
/*
--> Driver specific payment methods. Used to set up card, or bank account.
--> Used to pay the driver for deliveries.
--> Money is added to the preferred account when conditions are met.
 */
public class DriverSetPaymentMethod extends JPanel {
    private final FoodDeliveryLoginUI parent;
    private final String username;
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    
    // Card payment fields
    private final JTextField cardNumberField;
    private final JTextField expiryField;
    private final JTextField ccvField;
    private final JTextField cardNameField;
    
    // Bank account fields
    private final JTextField routingNumberField;
    private final JTextField accountNumberField;
    private final JTextField bankNameField;

    public DriverSetPaymentMethod(FoodDeliveryLoginUI parent, String username) {
        this.parent = parent;
        this.username = username;
        this.cardLayout = new CardLayout();
        this.contentPanel = new JPanel(cardLayout);
        
        // Initialize input fields with formatting
        cardNumberField = createFormattedTextField("####-####-####-####");
        expiryField = createFormattedTextField("##/##");
        ccvField = createFormattedTextField("###");
        cardNameField = new JTextField(20);
        
        routingNumberField = createFormattedTextField("#########");
        accountNumberField = createFormattedTextField("############");
        bankNameField = new JTextField(20);
        
        initUI();
        loadSavedPaymentMethod();
    }
    
    private JTextField createFormattedTextField(String format) {
        try {
            MaskFormatter formatter = new MaskFormatter(format);
            formatter.setPlaceholderCharacter('_');
            return new JFormattedTextField(formatter);
        } catch (ParseException e) {
            return new JTextField(format.length());
        }
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Payment Method Settings", SwingConstants.LEFT);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        
        JButton backBtn = new JButton("Back to Driver Menu");
        backBtn.addActionListener(e -> parent.getSceneSorter().switchPage("DriverScreen"));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Method Selection Panel
        JPanel methodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton cardBtn = new JButton("Credit/Debit Card");
        JButton bankBtn = new JButton("Bank Account");
        
        cardBtn.addActionListener(e -> cardLayout.show(contentPanel, "card"));
        bankBtn.addActionListener(e -> cardLayout.show(contentPanel, "bank"));
        
        methodPanel.add(cardBtn);
        methodPanel.add(bankBtn);
        add(methodPanel, BorderLayout.CENTER);

        // Card Payment Panel
        JPanel cardPanel = createCardPanel();
        contentPanel.add(cardPanel, "card");

        // Bank Account Panel
        JPanel bankPanel = createBankPanel();
        contentPanel.add(bankPanel, "bank");

        add(contentPanel, BorderLayout.SOUTH);
        cardLayout.show(contentPanel, "card"); // Start with card panel
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Credit/Debit Card Information"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;

        // Card Number
        c.gridx = 0; c.gridy = 0;
        panel.add(new JLabel("Card Number:"), c);
        c.gridx = 1;
        panel.add(cardNumberField, c);

        // Expiry Date
        c.gridx = 0; c.gridy = 1;
        panel.add(new JLabel("Expiry Date (MM/YY):"), c);
        c.gridx = 1;
        panel.add(expiryField, c);

        // CCV
        c.gridx = 0; c.gridy = 2;
        panel.add(new JLabel("CCV:"), c);
        c.gridx = 1;
        panel.add(ccvField, c);

        // Cardholder Name
        c.gridx = 0; c.gridy = 3;
        panel.add(new JLabel("Cardholder Name:"), c);
        c.gridx = 1;
        panel.add(cardNameField, c);

        // Save Button
        c.gridx = 1; c.gridy = 4;
        JButton saveCardBtn = new JButton("Save Card Details");
        saveCardBtn.addActionListener(e -> saveCardDetails());
        panel.add(saveCardBtn, c);

        return panel;
    }

    private JPanel createBankPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Bank Account Information"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;

        // Routing Number
        c.gridx = 0; c.gridy = 0;
        panel.add(new JLabel("Routing Number:"), c);
        c.gridx = 1;
        panel.add(routingNumberField, c);

        // Account Number
        c.gridx = 0; c.gridy = 1;
        panel.add(new JLabel("Account Number:"), c);
        c.gridx = 1;
        panel.add(accountNumberField, c);

        // Bank Name
        c.gridx = 0; c.gridy = 2;
        panel.add(new JLabel("Bank Name:"), c);
        c.gridx = 1;
        panel.add(bankNameField, c);

        // Save Button
        c.gridx = 1; c.gridy = 3;
        JButton saveBankBtn = new JButton("Save Bank Details");
        saveBankBtn.addActionListener(e -> saveBankDetails());
        panel.add(saveBankBtn, c);

        return panel;
    }

    private void saveCardDetails() {
        if (!validateCardDetails()) {
            return;
        }

        try {
            // First deactivate any existing payment methods
            parent.paymentDb.deactivateAllPaymentMethods(username);
            
            // Add new card payment method
            parent.paymentDb.addCardPayment(
                username,
                cardNumberField.getText().replaceAll("-", ""),
                expiryField.getText(),
                cardNameField.getText());
                
            JOptionPane.showMessageDialog(this,
                "Card details saved successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            Logger.catchAndLogBug(ex, "DriverSetPaymentMethod");
            JOptionPane.showMessageDialog(this,
                "Error saving card details: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveBankDetails() {
        if (!validateBankDetails()) {
            return;
        }

        try {
            // First deactivate any existing payment methods
            parent.paymentDb.deactivateAllPaymentMethods(username);
            
            // Add new bank payment method
            parent.paymentDb.addBankPayment(
                username,
                routingNumberField.getText(),
                accountNumberField.getText(),
                bankNameField.getText());

            JOptionPane.showMessageDialog(this,
                "Bank details saved successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            Logger.catchAndLogBug(ex, "DriverSetPaymentMethod");
            JOptionPane.showMessageDialog(this,
                "Error saving bank details: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateCardDetails() {
        if (cardNumberField.getText().contains("_") ||
            expiryField.getText().contains("_") ||
            ccvField.getText().contains("_") ||
            cardNameField.getText().trim().isEmpty()) {
            
            JOptionPane.showMessageDialog(this,
                "Please fill in all card details correctly.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean validateBankDetails() {
        if (routingNumberField.getText().contains("_") ||
            accountNumberField.getText().contains("_") ||
            bankNameField.getText().trim().isEmpty()) {
            
            JOptionPane.showMessageDialog(this,
                "Please fill in all bank details correctly.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void loadSavedPaymentMethod() {
        try {
            PaymentInformation paymentInfo = parent.paymentDb.getActivePaymentMethod(username);
            if (paymentInfo != null) {
                String paymentType = paymentInfo.getPaymentType();
                if ("CARD".equals(paymentType)) {
                    cardNumberField.setText(paymentInfo.getCardNumber());
                    expiryField.setText(paymentInfo.getCardExpiry());
                    cardNameField.setText(paymentInfo.getCardName());
                    cardLayout.show(contentPanel, "card");
                } else if ("BANK".equals(paymentType)) {
                    routingNumberField.setText(paymentInfo.getRoutingNumber());
                    accountNumberField.setText(paymentInfo.getAccountNumber());
                    bankNameField.setText(paymentInfo.getBankName());
                    cardLayout.show(contentPanel, "bank");
                }
            }
        } catch (SQLException ex) {
            Logger.catchAndLogBug(ex, "DriverSetPaymentMethod");
            JOptionPane.showMessageDialog(this,
                "Error loading payment details: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}