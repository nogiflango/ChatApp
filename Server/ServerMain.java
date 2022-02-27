package package1;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class ServerMain {

	public static void main(String args[]) throws InterruptedException {
		int serverPort = 9998;
		Server server = new Server(serverPort);
		Thread t = new Thread(server);
		t.start();
		t.join();
				
	}
}
