/**
 * Address represents a delivery address with geographic coordinates.
 * Includes validation for required fields and coordinate ranges.
 */
public class Address {
    private String street;
    private String city;
    private String state;
    private String zip;
    private double latitude;
    private double longitude;

    /**
     * Constructs an Address with the given information.
     * @param street Street address.
     * @param city City name.
     * @param state State code.
     * @param zip ZIP code.
     * @param latitude Latitude coordinate.
     * @param longitude Longitude coordinate.
     */
    public Address(String street, String city, String state, String zip, double latitude, double longitude) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    /**
     * Validates the address for completeness and valid coordinate ranges.
     * @return true if all fields are valid, false otherwise.
     */
    public boolean isValid() {
        // Check String fields for null or emptiness
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

    /**
     * Returns the street address.
     * @return Street address.
     */
    public String getStreet() {
        return street;
    }

    /**
     * Returns the city name.
     * @return City name.
     */
    public String getCity() {
        return city;
    }

    /**
     * Returns the state code.
     * @return State code.
     */
    public String getState() {
        return state;
    }

    /**
     * Returns the ZIP code.
     * @return ZIP code.
     */
    public String getZip() {
        return zip;
    }

    /**
     * Returns the latitude coordinate.
     * @return Latitude.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Returns the longitude coordinate.
     * @return Longitude.
     */
    public double getLongitude() {
        return longitude;
    }
}
