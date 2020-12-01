import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    static public final String USERNAME = "student";
    static public final String PASSWORD = "student";
    static public final String URL = "jdbc:mysql://localhost:3306/comp1011?useUnicode=true&" +
            "useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

    /**
     * gets a connection to the database
     *
     * @return mysql database connection
     * @throws SQLException if getConnect not found
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

}