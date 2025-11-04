public class Driver extends User {
    String car;


    public Driver(String name, int ID, int phoneNumber, String address, String car) {
        super(name, ID, phoneNumber);
        this.car = car;

    }
}
