package tt.ge.jett.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import tt.ge.jett.live.JsonSocket;
import tt.ge.jett.live.Pool;

public class Main {
	
	public static void main(String... args) throws IOException, InterruptedException {
		User user = User.login("r.0.uAdqxG7tSsP6qxLzVBXhUhJXcHGBSbL6Gck2m-" +
				"fc.0.0.e97b008c894f064567b4e66cae9d9b271d595312");
		
		Pool pool = new Pool(user.getToken());
		
		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put("filename", "test.txt");
		attrs.put("session", pool.getSession());
		File file = user.getShare("5PvolNB").createFile(attrs);
		
		pool.add("C:\\Users\\mirza\\Pictures\\pic\\1246395372269.jpg", file);
		
		//file.write("Hello this is so wrong");
		
		System.out.println("Session: " + pool.getSession());
		
		pool.join();
		
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
