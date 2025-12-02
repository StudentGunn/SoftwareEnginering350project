import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

/**
 * CheckAdmin is a utility class for verifying the admin account in the database.
 * Used for testing and validation purposes.
 */
public class CheckAdmin {
    
    /**
     * Computes the SHA-256 hash of the input string.
     * @param input String to hash.
     * @return Hexadecimal representation of the SHA-256 hash.
     */
    public static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    public static void main(String[] args) {
        try {
            UserDataBase userDb = new UserDataBase(java.nio.file.Path.of("users.db"));
            
            String adminUsername = "FoodDashAdmin";
            
            // Check if admin exists, Ultize the UserDataBase methods to verify details; PASS/FAIL output
            if (userDb.userExists(adminUsername)) {
                System.out.println("[PASS] FoodDashAdmin account exists in database");
                
                // Check user type
                String userType = userDb.getUserType(adminUsername);
                System.out.println("User Type: " + userType);
                if ("ADMIN".equals(userType)) {
                    System.out.println("[PASS] User type is correctly set to ADMIN");
                } else {
                    System.out.println("[FAILED] User type is incorrect. Expected: ADMIN, Actual: " + userType);
                }
                
                // Check admin hash code
                String expectedHashCode = "ADMIN2024";
                boolean hashValid = userDb.verifyAdminHash(adminUsername, expectedHashCode);
                if (hashValid) {
                    System.out.println("[PASS] Admin hash code is correctly set to: " + expectedHashCode);
                } else {
                    System.out.println("[FAIL] Admin hash code verification failed");
                }
                
                // Check password authentication
                String expectedPassword = sha256Hex("admin123");
                boolean authValid = userDb.authenticate(adminUsername, expectedPassword);
                if (authValid) {
                    System.out.println("[PASS] Admin password authentication works (password: admin123)");
                } else {
                    System.out.println("[FAILED] Admin password authentication failed");
                }
                
            } else {
                System.out.println("[FAILED] FoodDashAdmin account does NOT exist in database");
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}