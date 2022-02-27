package dbPackage;

import java.sql.ResultSet;

public interface ChatUsersQueriesIF {
	
	public boolean validateUsernameAndPassword(String username, String password);
	public void printAllRegisteredUsernames();
	public boolean messageOfflineUser(String receiver, String sender, String msg);
	public ResultSet retrieveMessages(String receiver);
	
}
