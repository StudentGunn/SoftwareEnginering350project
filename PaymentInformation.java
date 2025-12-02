/**
 * PaymentInformation holds payment details for customers.
 * Supports both card and bank payment methods with getters and setters.
 */
public class PaymentInformation {
    // Fields for payment information
    private String paymentType;  
    private String cardNumber;
    private String cardExpiry;
    private String cardName;
    private String routingNumber;
    private String accountNumber;
    private String bankName;
    
    /**
     * Returns the payment type (CARD or BANK).
     * @return Payment type.
     */
    public String getPaymentType() {
        return paymentType;
    }

    /**
     * Sets the payment type.
     * @param paymentType Payment type (CARD or BANK).
     */
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    /**
     * Returns the card number.
     * @return Card number.
     */
    public String getCardNumber() {
        return cardNumber;
    }

    /**
     * Sets the card number.
     * @param cardNumber Card number.
     */
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    /**
     * Returns the card expiry date.
     * @return Card expiry date.
     */
    public String getCardExpiry() {
        return cardExpiry;
    }

    /**
     * Sets the card expiry date.
     * @param cardExpiry Card expiry date.
     */
    public void setCardExpiry(String cardExpiry) {
        this.cardExpiry = cardExpiry;
    }

    /**
     * Returns the name on the card.
     * @return Cardholder name.
     */
    public String getCardName() {
        return cardName;
    }

    /**
     * Sets the name on the card.
     * @param cardName Cardholder name.
     */
    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    /**
     * Returns the bank routing number.
     * @return Routing number.
     */
    public String getRoutingNumber() {
        return routingNumber;
    }

    /**
     * Sets the bank routing number.
     * @param routingNumber Routing number.
     */
    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    /**
     * Returns the bank account number.
     * @return Account number.
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * Sets the bank account number.
     * @param accountNumber Account number.
     */
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    /**
     * Returns the bank name.
     * @return Bank name.
     */
    public String getBankName() {
        return bankName;
    }

    /**
     * Sets the bank name.
     * @param bankName Bank name.
     */
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    /**
     * Checks if the payment type is a card.
     * @return true if payment type is CARD, false otherwise.
     */
    public boolean isCard() {
        return "CARD".equals(paymentType);
    }

    /**
     * Checks if the payment type is a bank account.
     * @return true if payment type is BANK, false otherwise.
     */
    public boolean isBank() {
        return "BANK".equals(paymentType);
    }
}
