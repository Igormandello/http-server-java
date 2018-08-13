import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.security.InvalidParameterException;

import sun.misc.IOUtils;

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
				OutputStreamWriter writer = new OutputStreamWriter(client.getOutputStream());
				
				String message = reader.readLine();
				if (message != null && !message.equals("")) {
					String[] data = message.split(" ");
					if (data.length == 3 && data[0].equals("GET") && data[2].equals("HTTP/1.1")) {
						String actualMessage = new String(message);
						while (actualMessage != null && !actualMessage.equals("")) {
							actualMessage = reader.readLine();
							message += actualMessage;
						}
						
						String fileRequested = URLDecoder.decode(data[1]);
						System.out.println(fileRequested);
						
						writer.write("HTTP/1.1 200 OK");
						writer.write("Connection: close");
						writer.write("Date: Sat, 01 jan 2001 12:00:00 GMT");
						writer.write("Server: Apache/1.3.0 (Unix)");
						writer.write("Last-Modified: Sun, 6 May 2017 09:30:00 GMT");
						writer.write("Content-Length: 6821");
						writer.write("Content-Type: text/html");
						URL url = getClass().getResource("../example/example.html");
						File f = new File(url.getPath());
						System.out.println(Files.readAllBytes(f.toPath()));
						writer.flush();
					}
				} else {
					writer.write("Invalid request");
					writer.flush();
				}
			} catch (IOException e) {
				System.out.println(e);
				this.terminate();
			}
			
		}
	}
	
	public void terminate() {
		this.running = false;
		this.interrupt();
	}
}
