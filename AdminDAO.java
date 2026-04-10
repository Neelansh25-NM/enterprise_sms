import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.table.DefaultTableModel;
import java.util.*;


public class AdminDAO {

    // Authenticate user and return their Name if successful (or null if failed)
    public String login(String email, String password) {
        String sql = "SELECT admin_name FROM admins WHERE email = ? AND password = ?";
        
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("admin_name"); // Login success
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Login failed
    }

    // Register a new Admin (Forces 'pass123' as the default password per your rules)
// Register a new Admin with extended details
    public boolean registerAdmin(String name, String email, String department, String subject, String qualifications) {
        String defaultPassword = "pass@123";
        String sql = "INSERT INTO admins (admin_name, email, password, department, subject, qualifications) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, defaultPassword);
            pstmt.setString(4, department);
            pstmt.setString(5, subject);
            pstmt.setString(6, qualifications);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Registration failed. Email might already exist.");
            e.printStackTrace();
            return false;
        }
    }


    public boolean hasSchedule(String adminName) {
        String sql = "SELECT COUNT(*) AS total FROM admin_schedule WHERE admin_name = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, adminName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addScheduleSlot(String adminName, String course, String semester, String day, String start, String end, String subject, String division) {
        String sql = "INSERT INTO admin_schedule (admin_name, course, semester, day_of_week, start_time, end_time, subject, division) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, adminName);
            pstmt.setString(2, course);
            pstmt.setString(3, semester); // The new Semester field
            pstmt.setString(4, day);
            pstmt.setString(5, start);
            pstmt.setString(6, end);
            pstmt.setString(7, subject);
            pstmt.setString(8, division);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to post a new assignment
    // Method to post a new assignment (Now includes Semester!)
    public boolean postAssignment(String course, String semester, String division, String title, String description, String dueDate, String adminName) {
        String sql = "INSERT INTO assignments (course, semester, division, title, description, due_date, posted_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, course);
            pstmt.setString(2, semester); // New Parameter
            pstmt.setString(3, division);
            pstmt.setString(4, title);
            pstmt.setString(5, description);
            pstmt.setString(6, dueDate); // Format: YYYY-MM-DD
            pstmt.setString(7, adminName);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


 // UPGRADED: Now accepts a divisionFilter parameter
  public DefaultTableModel getAdminSchedule(String adminName, String divisionFilter) {
        DefaultTableModel model = new DefaultTableModel();
        String sql;
        boolean filterActive = !divisionFilter.equals("All Divisions");

        // Added 'semester' to both queries
        if (filterActive) {
            sql = "SELECT course, semester, day_of_week, start_time, end_time, subject, division FROM admin_schedule WHERE admin_name = ? AND division = ?";
        } else {
            sql = "SELECT course, semester, day_of_week, start_time, end_time, subject, division FROM admin_schedule WHERE admin_name = ?";
        }

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, adminName);
            if (filterActive) {
                pstmt.setString(2, divisionFilter);
            }
            
            ResultSet rs = pstmt.executeQuery();

            // Add the new column to the UI Table
            model.addColumn("Course");
            model.addColumn("Semester"); // New UI Column
            model.addColumn("Day");
            model.addColumn("Start Time");
            model.addColumn("End Time");
            model.addColumn("Subject");
            model.addColumn("Division");

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("course"),
                    rs.getString("semester"), // Extracting the semester
                    rs.getString("day_of_week"),
                    rs.getString("start_time"),
                    rs.getString("end_time"),
                    rs.getString("subject"),
                    rs.getString("division")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return model;
    }

    public List<String> getAssignedBatches(String adminName) {
        List<String> batches = new ArrayList<>();
        // DISTINCT ensures we don't get duplicates if you teach the same batch twice a week
        String sql = "SELECT DISTINCT course, semester, division FROM admin_schedule WHERE admin_name = ?";
        
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, adminName);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                // Formats it neatly: "BTech IT - Semester 3 - Div A"
                String batchStr = rs.getString("course") + " - " + rs.getString("semester") + " - " + rs.getString("division");
                batches.add(batchStr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return batches;
    }

    // Update the query status and add the professor's response
    public boolean respondToQuery(int queryId, String response) {
        String sql = "UPDATE student_queries SET response = ?, status = 'Resolved' WHERE query_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, response);
            pstmt.setInt(2, queryId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper to get the specific subject an admin teaches for a batch
    public String getSubjectForBatch(String adminName, String course, String semester, String division) {
        String sql = "SELECT subject FROM admin_schedule WHERE admin_name = ? AND course = ? AND semester = ? AND division = ? LIMIT 1";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, adminName);
            pstmt.setString(2, course);
            pstmt.setString(3, semester);
            pstmt.setString(4, division);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("subject");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown Subject";
    }
}
