import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidParameterException;

public class HTTPServer extends Thread {
	private int port;
	private boolean running = false;
	private ServerSocket socket;
	
	public HTTPServer(int serverPort) throws InvalidParameterException, IOException {
		if (serverPort < 0 || serverPort > 65535)
			throw new InvalidParameterException("Invalid port range, must be between 0 and 65535");
		
		this.port = serverPort;
		socket = new ServerSocket(this.port);
	}
	
	public HTTPServer() throws IOException {
		this.port = 80;
		socket = new ServerSocket(this.port);
	}
	
	public void run() {
		this.running = true;
		
		while (this.running) {
			try {
				Socket client = socket.accept();
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
				System.out.println("aceitou: " + reader.readLine());
			} catch (IOException e) {
				this.terminate();
			}
			
		}
	}
	
	public void terminate() {
		this.running = false;
		this.interrupt();
	}
}
