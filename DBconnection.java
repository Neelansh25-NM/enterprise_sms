import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBconnection {
    
    private static final String URL = "jdbc:mysql://localhost:3306/enterprise_sms";
    private static final String USER = "root";
    private static final String PASS = "N3El@12E4";

    public static Connection getConnection() throws SQLException {
        try {
          
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Add the Connector/J .jar to your build path.", e);
        }
    }
}