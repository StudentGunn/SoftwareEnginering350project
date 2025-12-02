/**
 * User represents a base user entity with name, ID, and phone number.
 * Serves as the parent class for Customer and Driver.
 */
public class User {
    String name;
    int ID;
    int phoneNumber;

    /**
     * Constructs a User with the given information.
     * @param name User's name.
     * @param ID User's ID.
     * @param phoneNumber User's phone number.
     */
    public User(String name, int ID, int phoneNumber){
    this.name = name;
    this.ID = ID;
    this.phoneNumber = phoneNumber;
    }



}

