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
    //Getter and Setter methods; getPaymentType, setPaymentType, getCardNumber, setCardNumber, getCardExpiry, setCardExpiry, getCardName, setCardName, getRoutingNumber, setRoutingNumber, getAccountNumber, setAccountNumber, getBankName, setBankName
    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardExpiry() {
        return cardExpiry;
    }

    public void setCardExpiry(String cardExpiry) {
        this.cardExpiry = cardExpiry;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public boolean isCard() {
        return "CARD".equals(paymentType);
    }

    public boolean isBank() {
        return "BANK".equals(paymentType);
    }
}
