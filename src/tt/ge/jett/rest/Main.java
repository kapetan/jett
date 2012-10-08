package tt.ge.jett.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import tt.ge.jett.live.JsonSocket;
import tt.ge.jett.live.Pool;
import tt.ge.jett.rest.url.Client;

public class Main {
	
	public static void main(String... args) throws IOException, InterruptedException {
		Logger logger = Logger.getLogger("tt.ge.jett");
		Handler handler = new ConsoleHandler();
		
		handler.setLevel(Level.ALL);
		handler.setFormatter(new SimpleFormatter());
		
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.ALL);
		logger.addHandler(handler);
		
		User user = User.login("mirza+test@ge.tt", "x17980", "trkkx27wybbo3whfrp8gf9l2jll3di");
		
		Share share = user.createShare("Hello");
		File file = share.createFile("text.txt");
		
		file.write("Hello you");
		
		/*User user = User.login("r.0.uAdqxG7tSsP6qxLzVBXhUhJXcHGBSbL6Gck2m-" +
				"fc.0.0.e97b008c894f064567b4e66cae9d9b271d595312");
		
		Pool pool = new Pool(user.getToken());
		
		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put("filename", "test.txt");
		attrs.put("session", pool.getSession());
		File file = user.getShare("5PvolNB").createFile(attrs);
		
		pool.add("C:\\Users\\mirza\\Pictures\\pic\\1246395372269.jpg", file);
		
		//file.write("Hello this is so wrong");
		
		System.out.println("Session: " + pool.getSession());
		
		pool.join();*/
		
		/*Map<String, String> attrs = new HashMap<String, String>();
		attrs.put("filename", "test.txt");
		
		File file = user.getShare("5PvolNB").uploadFile("C:\\Users\\mirza\\Pictures\\pic\\1246395372269.jpg");
		
		InputStream in = file.getScaled(100, 100);
		
		System.out.println(in.read());
		in.close();*/
		
		/*JsonSocket socket = new JsonSocket("open.ge.tt", 443);
		socket.connect();
		
		socket.send("{\"type\":\"ping\"}");
		socket.receive();
		
		socket.close();*/
	}
}
