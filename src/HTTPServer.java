import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.util.Base64;

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
				
				String message = reader.readLine();
				if (message != null && !message.equals("")) {
					String[] data = message.split(" ");
					if (data.length == 3 && data[0].equals("GET") && data[2].equals("HTTP/1.1")) {
						String actualMessage = new String(message);
						while (actualMessage != null && !actualMessage.equals("")) {
							actualMessage = reader.readLine();
							message += "\n" + actualMessage;
						}
						
						System.out.print("\n" + message);
						
						String fileRequested = URLDecoder.decode(data[1]);
						System.out.println("File requested: " + fileRequested);
						
						OutputStream out = client.getOutputStream();
						boolean image = isImage(fileRequested);
						if (!fileRequested.endsWith(".html") && !image) {
							System.out.println("Only HTML or image files allowed");
							String s = "HTTP/1.1 403 FORBIDDEN";
							out.write(s.getBytes());
							out.flush();
							out.close();
							continue;
						}
						
						URL url = getClass().getResource("./").toURI().resolve("../example" + fileRequested).toURL();
						File f = new File(url.getPath());
						
						String res = "";
						byte[] fileData = null;
						if (!f.exists())
							res = "HTTP/1.1 404 NOT FOUND\n";
						else {
							res = "HTTP/1.1 200 OK\n";
							fileData = Files.readAllBytes(f.toPath());
						}
						
						res += "Connection: close\n" +
							"Date: Sat, 01 jan 2001 12:00:00 GMT\n" +
							"Server: Apache/1.3.0 (Unix)\n" +
							"Last-Modified: Sun, 6 May 2017 09:30:00 GMT\n" +
							"Content-Length: \n" +
							"Content-Type: text/html\n\n";
						
						if (image) {
							String extension = fileRequested.substring(fileRequested.lastIndexOf('.'));
							res += "<html><body style=\"display: grid; justify-content: center; align-content: center; margin: 0px; background: #0e0e0e;\"><img type=\"image/" + extension + "\" src=\"data:image/" + extension + ";base64,";
							
							res += new String(Base64.getEncoder().encode(fileData));
							res += "\"></body></html>";
						} else
							res += String.join("\n", new String(fileData));
						
						out.write(res.getBytes());
						out.flush();
						out.close();
					}
				} else
					System.out.println("Invalid request");
			} catch (IOException e) {
				System.out.println(e);
				this.terminate();
			} catch (URISyntaxException e) {}
			
		}
	}
	
	public void terminate() {
		this.running = false;
		this.interrupt();
	}
	
	public boolean isImage(String s) {
		return s.endsWith(".png") || s.endsWith(".jpg") || s.endsWith(".jpeg") || s.endsWith(".bmp") || s.endsWith(".gif");
	}
}
