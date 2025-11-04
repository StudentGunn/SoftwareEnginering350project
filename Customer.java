public class Customer extends User{
    String address;
    PaymentInformation paymentInformation;
    public Customer(String name, int ID, int phoneNumber, String address, PaymentInformation paymentInformation) {
        super(name, ID, phoneNumber);
        this.address = address;
        this.paymentInformation = paymentInformation;
    }
}
