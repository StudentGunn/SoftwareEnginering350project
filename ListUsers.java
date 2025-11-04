import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ListUsers {
    public static void main(String[] args) {
        try {
            String url = "jdbc:sqlite:" + java.nio.file.Path.of("users.db").toAbsolutePath().toString();
            
            String sql = "SELECT username, user_type, full_name, email, phone, admin_hash FROM users";
            try (Connection c = DriverManager.getConnection(url);
                 PreparedStatement p = c.prepareStatement(sql);
                 ResultSet rs = p.executeQuery()) {
                
                System.out.println("All users in database:");
                System.out.println("Username | User Type | Full Name | Email | Phone | Admin Hash");
                System.out.println("---------|-----------|-----------|-------|-------|----------");
                
                while (rs.next()) {
                    String username = rs.getString("username");
                    String userType = rs.getString("user_type");
                    String fullName = rs.getString("full_name");
                    String email = rs.getString("email");
                    String phone = rs.getString("phone");
                    String adminHash = rs.getString("admin_hash");
                    
                    System.out.printf("%-8s | %-9s | %-9s | %-5s | %-5s | %-10s%n", 
                        username, userType, fullName, email, phone, adminHash);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}