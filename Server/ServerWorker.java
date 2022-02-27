package package1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.io.PrintWriter;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import dbPackage.ChatUsersQueriesIF;
import dbPackage.ChatUsersQueriesImpl;

public class ServerWorker implements Runnable {
	private PrintWriter pw = null;
	private BufferedReader readIncoming = null;
	private Socket clientSocket;
	private Server server;
	private String username = null;
	private ChatUsersQueriesIF queryObj = new ChatUsersQueriesImpl();

	public ServerWorker(Server server, Socket clientSocket) {
		this.clientSocket = clientSocket;
		this.server = server;
	}

	public void run() {
		handleClientSocket();
	}

	private void handleClientSocket() {
		try {
			establishReaderAndWriter();
			listenAndRespondToClient();
			clientSocket.close();
		}

		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void establishReaderAndWriter() {
		InputStream inputStream = null;
		OutputStream outputStream = null;

		try {
			inputStream = clientSocket.getInputStream();
			outputStream = clientSocket.getOutputStream();
			readIncoming = new BufferedReader(new InputStreamReader(inputStream));
			pw = new PrintWriter(outputStream, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void listenAndRespondToClient() {
		try {
			String incoming;
			while ((incoming = readIncoming.readLine()) != null) {
				String tokens[] = incoming.split(" ");
				if (tokens != null && tokens.length != 0) {
					String cmd = tokens[0];
					if (cmd.equals("quit") || cmd.contains("logoff")) {
						handleLogoff();
						break;
					} else if (cmd.equals("login")) {
						handleLogin(pw, tokens);
					} else if (cmd.equals("create")) {
						if (tokens.length > 1) {
							if (tokens[1].charAt(0) == '#') {
								handleGroupChat(tokens);
							} else {
								send("You did not enter a valid chat name. Please try creating a group again (Ex: create #group).");
							}
						} else {
							send("You did not enter a chat name. Please try creating a group again (Ex: create #group).");
						}
					} else if (cmd.equals("msg")) {
						String tokensMsg[] = incoming.split(" ", 3);
						handleMessage(tokensMsg);
					} else if (cmd.equals("add")) {
						handleAddingUser(tokens);
					} else {
						String msg = "unknown " + cmd + "\n";
						pw.println(msg);
					}
				}

			}
			if (incoming == null) {
				goOffline();
			}
		}

		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleAddingUser(String[] tokens) {
		ArrayList<ServerWorker> workerList = server.getWorkerList();
		HashMap<String, ArrayList> groups = server.getGroups();
		ArrayList<String> onlineUsernames = getAllOnlineUsernames(workerList);

		try {
			if (tokens.length == 2) {
				if (groups.containsKey(tokens[1])) {
					ArrayList<String> members = groups.get(tokens[1]);
					if (members.contains(getUsername())) {
						send("Chose an online user that you would like to add to the " + tokens[1] + " groups chat.");
						showOnlineUsers();
						String incoming = readIncoming.readLine();
						if (incoming != null) {
							if (onlineUsernames.contains(incoming)) {
								if (!members.contains(incoming)) {
									members.add(incoming);

								} else {
									send("You've added a user that is already part of the chat. Try again by adding a user that is online");
								}

							} else {
								send("You've added a user that is not online. Try again by adding a user that is online");
							}
						}

					} else {
						send("You are not part of the " + tokens[1]
								+ " group chat. Try adding a user to a group chat you're already a part of.");
					}

				} else {
					send("The " + tokens[1]
							+ " group chat has not been created. Try adding a user to a group chat you're already a part of.");
				}
			} else {
				send("You did not enter a chat name. Try again by adding to a group chat you are a part of.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleGroupChat(String tokens[]) {
		try {
			String topic = tokens[1];
			showOnlineUsers();
			send("Pick 2 or more users to add to your chat");
			String incoming;
			if ((incoming = readIncoming.readLine()) != null) {
				String tokens2[] = incoming.split(" ");
				String cmd = tokens2[0];
				if (cmd.equals("add")) {
					boolean success = createGroupChat(topic, tokens2);
					if (success) {
						informInvitedMembers(topic);
					} else {
						handleGroupChat(tokens);
					}
				} else {
					String msg = "unknown " + cmd + "\n";
					pw.println(msg);
				}
			}
			if (incoming == null) {
				goOffline();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean createGroupChat(String topic, String[] tokens2) {
		ArrayList<ServerWorker> workerList = server.getWorkerList();
		ArrayList<String> onlineUsernames = getAllOnlineUsernames(workerList);
		HashMap<String, ArrayList> groups = server.getGroups();

		boolean flag = true;
		if (tokens2.length > 2) {
			for (int i = 1; i < tokens2.length; i++) {
				if (!onlineUsernames.contains(tokens2[i])) {
					send("You've added a username that is not online. Please try adding 2 or more users who are online.");
					flag = false;
					return false;
				}
			}
			if (flag == true) {
				ArrayList<String> members = new ArrayList<String>();
				members.add(getUsername());
				for (int i = 1; i < tokens2.length; i++) {
					members.add(tokens2[i]);
				}
				groups.put(topic, members);
				return true;
			}
			return true;

		} else {
			send("You've added fewer than 2 online users. Please try adding 2 or more users who are online.");
			return false;
		}
	}

	private ArrayList<String> getAllOnlineUsernames(ArrayList<ServerWorker> workerList) {
		ArrayList<String> onlineUsernames = new ArrayList<String>();
		for (ServerWorker worker : workerList) {
			onlineUsernames.add(worker.getUsername());
		}
		return onlineUsernames;
	}

	private void informInvitedMembers(String topic) {
		HashMap<String, ArrayList> groups = server.getGroups();
		ArrayList<String> members = groups.get(topic);

		ArrayList<ServerWorker> workerList = server.getWorkerList();

		for (ServerWorker worker : workerList) {
			if (members.contains(worker.getUsername())) {
				worker.send("You're added to the " + topic + " group chat!");
				showAllGroupMembers(members, worker, topic);

			}
		}

	}

	private void showAllGroupMembers(ArrayList<String> members, ServerWorker worker, String topic) {
		worker.send("ALL MEMBERS IN " + topic + " GROUP CHAT:");
		for (String member : members) {
			worker.send(member);

		}

	}

	private void handleMessage(String[] tokensMsg) {
		String sendTo = tokensMsg[1];
		String body = tokensMsg[2];
		ArrayList<ServerWorker> workerList = server.getWorkerList();

		boolean isTopic = sendTo.charAt(0) == '#';

		if (isTopic) {
			handleGroupMessages(sendTo, body);
		} else {

			for (ServerWorker worker : workerList) {
				if (sendTo.equals(worker.getUsername())) {
					String outMsg = "Message from " + username + ": " + body;
					worker.send(outMsg);
				}
			}
			send("You did not send a message to a user that is online. Try sending the message again.");
		}
	}

	private void handleGroupMessages(String sendTo, String body) {
		ArrayList<ServerWorker> workerList = server.getWorkerList();
		HashMap<String, ArrayList> groups = server.getGroups();

		boolean isValidTopic = groups.containsKey(sendTo);

		if (isValidTopic) {
			ArrayList<String> members = groups.get(sendTo);
			if (members.contains(getUsername())) {
				for (ServerWorker worker : workerList) {
					if (members.contains(worker.getUsername())) {
						if (!username.equals(worker.getUsername())) {
							worker.send("Message from " + username + " in " + sendTo + " group chat" + ": " + body);
						}
					}
				}
			} else {
				send("You are not part of the " + sendTo
						+ " group chat. Try sending a message to a group chat you're already a part of.");

			}
		} else {
			send("The " + sendTo
					+ " group chat has not been created. Try sending a message to a group chat you're already a part of.");
		}
	}

	public void handleLogin(PrintWriter pw, String[] tokens) {
		if (tokens.length == 3) {
			String username = tokens[1];
			String password = tokens[2];
			String msg;

			try {
				boolean success = queryObj.validateUsernameAndPassword(username, password);
				if (success) {
					acceptUser(username);
					informMyLoginToAllUsers();
					showOnlineUsers();
					showAllRegisteredUsers();
					retrieveMessages(username);

				} else {
					msg = "error login\n";
					pw.println(msg);
				}
			}

			catch (Exception e) {
				e.printStackTrace();
			}
		}
		send("You did not enter your username and password correctly. PLease try logging in again.");

	}

	private void retrieveMessages(String receiver) {
		ResultSet rs = queryObj.retrieveMessages(receiver);
		try {
			while (rs.next()) {
				send("Archived message from " + rs.getString(2) + ":" + " " + rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void acceptUser(String username) {
		System.out.println("ok login\n");
		this.username = username;
		System.out.println(username + " logged in successfully");

		send("You've successfully logged in!");

	}

	private void informMyLoginToAllUsers() {
		ArrayList<ServerWorker> workerList = server.getWorkerList();

		String onlineMsg = username + " has just joined\n";
		for (ServerWorker worker : workerList) {

			System.out.println("worker.login: " + worker.getUsername());
			if (!username.equals(worker.getUsername())) {
				worker.send(onlineMsg);
			}
		}
	}

	private void showOnlineUsers() {
		ArrayList<ServerWorker> workerList = server.getWorkerList();
		send("ALL OTHER USERS ONLINE:\n");
		for (ServerWorker worker : workerList) {

			if (worker.getUsername() != null) {
				if (!username.equals(worker.getUsername())) {
					String msg2 = "online " + worker.getUsername() + "\n";
					send(msg2);
				}
			}
		}
	}

	private void showAllRegisteredUsers() {
		send("ALL REGISTERED USERS:\n");
		queryObj.printAllRegisteredUsernames();
	}

	private void sendUsersAllPeopleOnline() {
		ArrayList<ServerWorker> workerList = server.getWorkerList();
		for (ServerWorker worker : workerList) {
			if (worker.getUsername() != null) {
				if (!username.equals(worker.getUsername())) {
					worker.showOnlineUsers();
				}
			}
		}
	}

	private String getUsername() {
		return username;
	}

	private void handleLogoff() {
		send("You've logged off!");
		informAllUsersMyLogOff();
		goOffline();

		closeConnection();
		System.out.println(username + " logged off successfully.");
	}

	private void closeConnection() {
		try {
			readIncoming.close();
			pw.close();
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void goOffline() {
		ArrayList<ServerWorker> workerList = server.getWorkerList();
		ServerWorker temp = null;
		int i = 0;
		for (ServerWorker worker : workerList) {
			if (username != null) {
				if (username.equals(worker.getUsername())) {
					temp = worker;
					break;
				}
			}
			i++;
		}

		if (temp != null) {
			workerList.remove(temp);
			System.out.println(username + " is removed");
		} else {
			System.out.println("The anonymous user did not even login");
		}

	}

	private void informAllUsersMyLogOff() {
		ArrayList<ServerWorker> workerList = server.getWorkerList();

		String onlineMsg = username + " is logging off\n";
		for (ServerWorker worker : workerList) {
			if (!username.equals(worker.getUsername())) {
				worker.send(onlineMsg);
			}
		}
	}

	public void send(String onlineMsg) {
		if (username != null) {
			pw.println(onlineMsg);
		}
	}

}
