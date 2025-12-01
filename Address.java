 /*
--> Address stores the user's street, city, state, zip code, latitude, and longitude.
--> All of these are later used in other aspects of the code, such as ordering or ETA calculations.
--> All address values are stored in a public address value in userDatabase, set whenever logging in.
--> This ensures all classes can access it, and that it will always correspond to the correct user.
 */

public class Address {
    // Used to tell drivers where to deliver food.
    private String street;
    private String city;
    private String state;
    // Used to determine restaurants in area, especially on resturantScreen.
    private String zip;
    // Coordinates are used to determine ETA values, as well as how many miles away a restaurant is.
    private double latitude;
    private double longitude;

    public Address(String street, String city, String state, String zip, double latitude, double longitude) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    // Ensures that the address value is never null.
    // Used to prompt user if they try to go to resturantScreen without a full address set.
    public boolean isValid() {
        if (street == null || street.trim().isEmpty()) {
            return false;
        }
        if (city == null || city.trim().isEmpty()) {
            return false;
        }
        if (state == null || state.trim().isEmpty()) {
            return false;
        }
        if (zip == null || zip.isEmpty()) {
            return false;
        }
        if (longitude == 0 || latitude == 0 || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            return false;
        }
        return true;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZip() {
        return zip;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
