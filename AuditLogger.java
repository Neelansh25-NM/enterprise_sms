import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuditLogger {
    public static void log(String actionDetails, String actor) {
        String sql = "INSERT INTO audit_logs (action_details, actor) VALUES (?, ?)";
        
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, actionDetails);
            pstmt.setString(2, actor);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("CRITICAL: Failed to write to audit log.");
            e.printStackTrace();
        }
    }
}