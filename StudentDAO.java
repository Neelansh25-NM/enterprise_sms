import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;
import javax.swing.table.DefaultTableModel;
import java.util.*;

public class StudentDAO {
    
   // Create/Register a new student directly into their assigned batch
    public boolean addStudent(String rollNumber, String name, String course, String semester, String division, int year, String adminName) {
        String sql = "INSERT INTO students (roll_number, name, course, semester, division, enrollment_year) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rollNumber);
            pstmt.setString(2, name);
            pstmt.setString(3, course);
            pstmt.setString(4, semester);
            pstmt.setString(5, division);
            pstmt.setInt(6, year);

            boolean success = pstmt.executeUpdate() > 0;
            
            // Log the action if it succeeded
            if (success) {
                AuditLogger.log("Registered new student: " + name + " (" + rollNumber + ") to " + course, adminName);
            }
            
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // READ Operation for the Search Tab
    public DefaultTableModel searchStudents(String keyword) {
        DefaultTableModel model = new DefaultTableModel();
        // Searches for partial matches in Roll Number OR Course
        String sql = "SELECT roll_number, name, course, enrollment_year FROM students WHERE roll_number LIKE ? OR course LIKE ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            // 1. Setup Table Columns dynamically based on the SQL result
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            model.addColumn("Roll No");
            model.addColumn("Name");
            model.addColumn("Course");
            model.addColumn("Year");

            // 2. Extract Data Rows
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i); // Indexes start at 1 for ResultSet, 0 for Arrays
                }
                model.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return model;
    }

    // READ Operation for Report Card Generation
// READ Operation for Report Card Generation (Updated to use roll_number)
    public String generateReportCard(String rollNumber) {
        // First get the internal student_id using the roll number
        String studentQuery = "SELECT student_id, name, course FROM students WHERE roll_number = ?";
        String gradesQuery = "SELECT subject, marks FROM grades WHERE student_id = ?";
        StringBuilder report = new StringBuilder();

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement studentStmt = conn.prepareStatement(studentQuery)) {
             
            studentStmt.setString(1, rollNumber);
            ResultSet studentRs = studentStmt.executeQuery();
            
            if (studentRs.next()) {
                int studentId = studentRs.getInt("student_id");
                
                report.append("=====================================\n");
                report.append("         OFFICIAL REPORT CARD        \n");
                report.append("=====================================\n");
                report.append("Name: ").append(studentRs.getString("name")).append("\n");
                report.append("Roll No: ").append(rollNumber).append("\n");
                report.append("Course: ").append(studentRs.getString("course")).append("\n");
                report.append("-------------------------------------\n");
                
                // Fetch Grades using the internal ID
                try (PreparedStatement gradesStmt = conn.prepareStatement(gradesQuery)) {
                    gradesStmt.setInt(1, studentId);
                    ResultSet gradesRs = gradesStmt.executeQuery();
                    
                    int totalMarks = 0;
                    int subjectCount = 0;
                    
                    while (gradesRs.next()) {
                        report.append(String.format("%-25s : %d\n", gradesRs.getString("subject"), gradesRs.getInt("marks")));
                        totalMarks += gradesRs.getInt("marks");
                        subjectCount++;
                    }
                    
                    report.append("-------------------------------------\n");
                    if (subjectCount > 0) {
                        double average = (double) totalMarks / subjectCount;
                        report.append(String.format("Average Percentage      : %.2f%%\n", average));
                    } else {
                        report.append("No grades recorded yet.\n");
                    }
                    report.append("=====================================\n");
                }
                
                AuditLogger.log("Generated Report Card for Roll No: " + rollNumber, "System");
                return report.toString();
            } else {
                return "Error: Student with Roll Number " + rollNumber + " not found.";
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error generating report card due to database exception.";
        }
    }
    // FETCH Operation: Get a single student's data by Roll Number
    public String[] getStudentByRollNumber(String rollNumber) {
        String sql = "SELECT name, course, enrollment_year FROM students WHERE roll_number = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new String[]{
                    rs.getString("name"),
                    rs.getString("course"),
                    String.valueOf(rs.getInt("enrollment_year"))
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Returns null if the roll number doesn't exist
    }

    // Fetch students strictly by their allocated Course, Semester, and Division
    public DefaultTableModel getStudentsByBatch(String course, String semester, String division) {
        DefaultTableModel model = new DefaultTableModel();
        String sql = "SELECT roll_number, name, enrollment_year FROM students WHERE course = ? AND semester = ? AND division = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course);
            pstmt.setString(2, semester);
            pstmt.setString(3, division);
            ResultSet rs = pstmt.executeQuery();

            model.addColumn("Roll No");
            model.addColumn("Student Name");
            model.addColumn("Enrollment Year");

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("roll_number"),
                    rs.getString("name"),
                    rs.getInt("enrollment_year")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return model;
    }

    // UPDATE Operation: Save the modified data
    public boolean updateStudent(String rollNumber, String name, String course, int year, String currentUser) {
        String sql = "UPDATE students SET name = ?, course = ?, enrollment_year = ? WHERE roll_number = ?";
        
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, course);
            pstmt.setInt(3, year);
            pstmt.setString(4, rollNumber); // The WHERE clause parameter
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                AuditLogger.log("Updated student data for Roll No: " + rollNumber, currentUser);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Fetch Attendance Stats for the Pie Chart
    public int[] getAttendanceStats(String rollNumber, String subject) {
        int present = 0;
        int total = 0;
        String sql = "SELECT status FROM attendance WHERE roll_number = ? AND subject = ?";
        
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, rollNumber);
            pstmt.setString(2, subject);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                total++;
                if (rs.getString("status").equalsIgnoreCase("Present")) {
                    present++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[]{present, total - present}; // Returns [Present Count, Absent/Late Count]
    }

    // Fetch Assignment Stats for the Bar Chart
    public int[] getAssignmentStats(String rollNumber) {
        int accepted = 0, pending = 0, rejected = 0;
        String sql = "SELECT status FROM student_submissions WHERE roll_number = ?";
        
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String status = rs.getString("status");
                if (status.equals("Accepted")) accepted++;
                else if (status.equals("Pending")) pending++;
                else if (status.equals("Rejected")) rejected++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[]{accepted, pending, rejected};
    }
public List<String> getRollNumbersInBatch(String course, String semester, String division) {
        List<String> rollNumbers = new ArrayList<>();
        String sql = "SELECT roll_number FROM students WHERE course = ? AND semester = ? AND division = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, course);
            pstmt.setString(2, semester);
            pstmt.setString(3, division);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                rollNumbers.add(rs.getString("roll_number"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return rollNumbers;
    }

    // 2. Save detailed marks
    public boolean saveDetailedMarks(String rollNo, String subject, int mtt1, int mtt2, int project, int lab, int participation) {
        int total = mtt1 + mtt2 + project + lab + participation;
        String sql = "INSERT INTO detailed_grades (roll_number, subject, mtt1, mtt2, project, lab, participation, total) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rollNo);
            pstmt.setString(2, subject);
            pstmt.setInt(3, mtt1);
            pstmt.setInt(4, mtt2);
            pstmt.setInt(5, project);
            pstmt.setInt(6, lab);
            pstmt.setInt(7, participation);
            pstmt.setInt(8, total);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. Upgraded Report Card Generator
    public String generateDetailedReportCard(String rollNumber) {
        String studentQuery = "SELECT name, course, semester FROM students WHERE roll_number = ?";
        String gradesQuery = "SELECT subject, mtt1, mtt2, project, lab, participation, total FROM detailed_grades WHERE roll_number = ?";
        StringBuilder report = new StringBuilder();

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement studentStmt = conn.prepareStatement(studentQuery)) {
             
            studentStmt.setString(1, rollNumber);
            ResultSet studentRs = studentStmt.executeQuery();
            
            if (studentRs.next()) {
                report.append("==================================================\n");
                report.append("            OFFICIAL SEMESTER REPORT CARD         \n");
                report.append("==================================================\n");
                report.append("Name: ").append(studentRs.getString("name")).append("\n");
                report.append("Roll No: ").append(rollNumber).append("\n");
                report.append("Course: ").append(studentRs.getString("course")).append(" (").append(studentRs.getString("semester")).append(")\n");
                report.append("--------------------------------------------------\n");
                
                try (PreparedStatement gradesStmt = conn.prepareStatement(gradesQuery)) {
                    gradesStmt.setString(1, rollNumber);
                    ResultSet gradesRs = gradesStmt.executeQuery();
                    
                    while (gradesRs.next()) {
                        report.append("Subject: ").append(gradesRs.getString("subject")).append("\n");
                        report.append(String.format("  - MTT1 (20)        : %d\n", gradesRs.getInt("mtt1")));
                        report.append(String.format("  - MTT2 (20)        : %d\n", gradesRs.getInt("mtt2")));
                        report.append(String.format("  - Project (20)     : %d\n", gradesRs.getInt("project")));
                        report.append(String.format("  - Lab (20)         : %d\n", gradesRs.getInt("lab")));
                        report.append(String.format("  - Participation(20): %d\n", gradesRs.getInt("participation")));
                        report.append(String.format("  TOTAL SCORE        : %d / 100\n", gradesRs.getInt("total")));
                        report.append("--------------------------------------------------\n");
                    }
                }
                return report.toString();
            } else {
                return "Student not found.";
            }
        } catch (SQLException e) { return "Database error."; }
    }

}