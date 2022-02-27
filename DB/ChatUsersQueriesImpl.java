package dbPackage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ChatUsersQueriesImpl implements ChatUsersQueriesIF {
	Connection conn = null;

	private boolean isUsernameExists(String username) {
		try {
			String SQL = "select username from chatusers where username='" + username + "'";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(SQL);
			boolean isUsernameExists = rs.next();
			return isUsernameExists;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void establishDBConnection() {
		try {
			// Register JDBC Driver
			Class.forName("oracle.jdbc.OracleDriver");

			/*
			 * Connect to Database with DriverManager. Function 'getConnection' is
			 * implemented by someone else.
			 */
			String url = "jdbc:oracle:thin:@10.0.0.31:1521:XE";
			String dbUsername = "rgoudar";
			String dbPassword = "gudur58";
			conn = DriverManager.getConnection(url, dbUsername, dbPassword);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean validateUsernameAndPassword(String username, String password) {
		try {
			establishDBConnection();
			if (isUsernameExists(username)) {
				if (isPaswordValid(username, password)) {
					return true;
				}
				return false;
			}
			return false;
		} finally {
			try {
				conn.close();
				conn = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean isPaswordValid(String username, String password) {
		try {
			String SQL = "SELECT PASSCODE FROM CHATUSERS WHERE USERNAME='" + username + "'";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(SQL);
			if (rs.next()) {
				if (password.equals(rs.getString(1))) {
					return true;
				}
				return false;
			}
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void printAllRegisteredUsernames() {
		try {
			establishDBConnection();
			String SQL = "SELECT USERNAME FROM CHATUSERS";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(SQL);
			while (rs.next()) {
				System.out.println(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
				conn = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	public boolean messageOfflineUser(String receiver, String sender, String msg) {
		establishDBConnection();
		boolean receiverExists = isUsernameExists(receiver);
		boolean senderExists = isUsernameExists(sender);
		try {
		if(receiverExists && senderExists) {
			String SQL = "INSERT INTO NEWMESSAGES(USERNAME,MESSAGE,SENTBY)VALUES('"+receiver+"','"+msg+"','"+sender+"')";
			Statement stmt = conn.createStatement();
			stmt.executeQuery(SQL);
			return true;
			}
		return false;
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public ResultSet retrieveMessages(String receiver){
		establishDBConnection();
		boolean receiverExists = isUsernameExists(receiver);
		try {
		if(receiverExists) {
			String SQL = "SELECT MESSAGE,SENTBY FROM NEWMESSAGES WHERE USERNAME='"+receiver+"'";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(SQL);
			if(rs.next()) {
				return rs;
			 }
			return null;
			}
		return null;
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return null;

	}

}
