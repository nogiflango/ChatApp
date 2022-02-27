package dbPackage;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class JDBCMain {

	public void fetchData() {
		try {
			// Register the Oracle JDBC driver:
			Class oracleclass = Class.forName("oracle.jdbc.OracleDriver");

			// Establish Connection to Oracle Database:
			String dbURL = "jdbc:oracle:thin:@10.0.0.31:1521:XE";
			String username = "rgoudar";
			String password = "gudur58";
			Connection conn = DriverManager.getConnection(dbURL, username, password);

			// Execute Query:
			Statement stmt = conn.createStatement();
			String SQL = "INSERT INTO CHATUSERS(IDNUM, USERNAME, PASSCODE) VALUES(1,'bob','bob')";
			ResultSet rs = stmt.executeQuery(SQL);

			SQL = "SELECT * FROM CHATUSERS";
			rs = stmt.executeQuery(SQL);
			
			while (rs.next()) {
				System.out.println("ID Number: " + rs.getInt(1) + " Username: " + rs.getString(2) + " Password: "+ rs.getString(3));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		JDBCMain jdbcObj = new JDBCMain();
		jdbcObj.fetchData();
	}
}
