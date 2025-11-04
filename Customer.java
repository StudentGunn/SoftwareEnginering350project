public class Customer extends User{
    String address;
    PaymentInformation paymentInformation;
    double balance;
    
    public Customer(String name, int ID, int phoneNumber, String address, PaymentInformation paymentInformation) {
        super(name, ID, phoneNumber);
        this.address = address;
        this.paymentInformation = paymentInformation;
        this.balance = 0.0; // Initialize balance to 0
    }
    
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
