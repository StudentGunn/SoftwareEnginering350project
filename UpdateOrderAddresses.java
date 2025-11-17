import java.sql.*;
/*  
 --> Updates existing orders in the database to set restaurant addresses based on restaurant names
 --> connect to database orders.db
 --> for each order, if restaurant_name matches known names, set restaurant_address accordingly
 --> print out success message with number of updated orders
 --> catcjh and print any SQL exceptions if fails
 */

public class UpdateOrderAddresses {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:orders.db";
        
        try (Connection conn = DriverManager.getConnection(url)) {
            // Update existing orders with restaurant addresses
            String updateSql = "UPDATE orders SET restaurant_address = CASE " +
                "WHEN restaurant_name = 'Crimson Dining' THEN '125 Burrill Ave' " +
                "WHEN restaurant_name = 'Barrett''s Alehouse Bridgewater' THEN '425 Bedford St' " +
                "WHEN restaurant_name = 'Greyhound Tavern' THEN '39 Broad Street' " +
                "ELSE 'Address not available' END " +
                "WHERE restaurant_address IS NULL";
            
            try (Statement stmt = conn.createStatement()) {
                int updated = stmt.executeUpdate(updateSql);
                System.out.println("Updated " + updated + " orders with restaurant addresses.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating addresses: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
