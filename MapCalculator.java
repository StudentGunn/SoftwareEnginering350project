/**
 * MapCalculator provides utility methods for calculating distances and travel times
 * between geographic coordinates using the Haversine formula.
 */
public class MapCalculator {
    private static final double EARTH_RADIUS_KM = 6371; // Earth's radius in kilometers

    /**
     * Calculates the estimated travel time in minutes between two coordinate pairs.
     * Uses the Haversine formula to determine distance and assumes average car speed of 30 km/h.
     * @param lat1 Latitude of the first location.
     * @param lon1 Longitude of the first location.
     * @param lat2 Latitude of the second location.
     * @param lon2 Longitude of the second location.
     * @return Estimated travel time in minutes, rounded to one decimal place.
     */
    public static double calculateETA (double lat1, double lon1, double lat2, double lon2){
        // The Haversine equation is used to deterime the distance in kilometers between two sets of coordinates.
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double distanceKm = EARTH_RADIUS_KM * c; // Distance in kilometers
        // Now we need to find the speed. This can be done by dividing distance by how fast a car is going.
        // The average global speed for a car is 30 km.
        double temp = distanceKm/30;
        temp=temp * 60; // Converting hours to minutes.
        return (double) Math.round(temp * 10) /10; // Rounding makes it more readable
    }
    public static double calculateMiles (double lat1, double lon1, double lat2, double lon2){
            // Redoing the first calculation prevents headaches.
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double distanceKm = EARTH_RADIUS_KM * c; // Distance in kilometers
        // We can now convert the kilometers to miles, since this app is based within Massachusetts.
        double miles = distanceKm/1.609;
        return (double) Math.round(miles * 10) /10; // Used to ensure miles only have a singular decimal point.
    }
}