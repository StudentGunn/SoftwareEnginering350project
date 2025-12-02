/**
 * Driver represents a driver user with vehicle information.
 * Extends the base User class.
 */
public class Driver extends User {
    String car;

    /**
     * Constructs a Driver with the given information.
     * @param name Driver's name.
     * @param ID Driver's ID.
     * @param phoneNumber Driver's phone number.
     * @param address Driver's address.
     * @param car Driver's vehicle description.
     */
    public Driver(String name, int ID, int phoneNumber, String address, String car) {
        super(name, ID, phoneNumber);
        this.car = car;

    }
}
