package package1;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class Server implements Runnable{
	private int serverPort;
	
	private ArrayList<ServerWorker> workerList = new ArrayList<ServerWorker>();
	private HashMap<String, ArrayList> groups = new HashMap<String, ArrayList>();
	
	public Server(int serverPort) {
		this.serverPort=serverPort;
	}
	
	public ArrayList<ServerWorker> getWorkerList(){
		return workerList;
	}
	
	public HashMap<String, ArrayList> getGroups(){
		return groups;
	}
	
	public ServerSocket createServerSocket() {
		String serverIP = "10.0.0.132";
		int backLog = 5;
		InetAddress serverAddress = null; 
		ServerSocket serverSocket = null; 
	
		try { 
			serverAddress = InetAddress.getByName(serverIP);
			serverSocket = new ServerSocket(serverPort, backLog, serverAddress);
		}
		
		catch(UnknownHostException e) {
			e.printStackTrace();
		}
		
		catch(IOException e) {
			e.printStackTrace();
		}
		return serverSocket;
	}
	
	public void execute() {
		ServerSocket serverSocket = createServerSocket();

		while(true) {
			System.out.println("The server is waiting for a client");
			Socket clientSocket=null;
			try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("The server has accepted connection from "+clientSocket);
			ServerWorker worker = new ServerWorker(this, clientSocket);
			workerList.add(worker);
			Thread t = new Thread(worker);
			t.start();
		}
	}

	
	public void run() {
		execute();
	}

}
