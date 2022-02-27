package package1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.UnknownHostException;

public class ChatClient {
	public Socket createSocket() {
		int serverPort = 9998; 
		String serverIP = "10.0.0.132";
		
		Socket socket =null;
		InetAddress serverAddress = null;
		try {
		  serverAddress = InetAddress.getByName(serverIP);
		  socket = new Socket(serverAddress, serverPort);
		}
		catch(UnknownHostException e) {
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return socket;
	}
	
	public void execute() {
		Socket socket= createSocket();
		
		InputStream inputStream = null;
		String incoming;
		
		OutputStream outputStream = null; 
		String outgoing;
		
		BufferedReader br_readIncoming = null;
		BufferedReader br_readOutgoing = null;
				
		try {	
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			br_readIncoming = new BufferedReader(new InputStreamReader(inputStream));
			br_readOutgoing = new BufferedReader(new InputStreamReader(System.in));
			
			while(true) {
				System.out.println("Send message to the server: ");
				outgoing = br_readOutgoing.readLine();
				outputStream.write(outgoing.getBytes());
				
				incoming = br_readIncoming.readLine();
				System.out.println(incoming);
			}
			
			
			
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		ChatClient client = new ChatClient();
		client.execute();
	}
	

}
