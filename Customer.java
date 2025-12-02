/**
 * Customer represents a customer user with address, payment information, and balance.
 * Extends the base User class.
 */
public class Customer extends User{
    String address;
    PaymentInformation paymentInformation;
    double balance;
    
    /**
     * Constructs a Customer with the given information and initializes balance to 0.
     * @param name Customer's name.
     * @param ID Customer's ID.
     * @param phoneNumber Customer's phone number.
     * @param address Customer's address.
     * @param paymentInformation Customer's payment information.
     */
    public Customer(String name, int ID, int phoneNumber, String address, PaymentInformation paymentInformation) {
        super(name, ID, phoneNumber);
        this.address = address;
        this.paymentInformation = paymentInformation;
        this.balance = 0.0; // Initialize balance to 0
    }
    
    /**
     * Constructs a Customer with the given information and specified balance.
     * @param name Customer's name.
     * @param ID Customer's ID.
     * @param phoneNumber Customer's phone number.
     * @param address Customer's address.
     * @param paymentInformation Customer's payment information.
     * @param balance Customer's account balance.
     */
    public Customer(String name, int ID, int phoneNumber, String address, PaymentInformation paymentInformation, double balance) {
        super(name, ID, phoneNumber);
        this.address = address;
        this.paymentInformation = paymentInformation;
        this.balance = balance;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
}
