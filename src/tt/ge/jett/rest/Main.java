package tt.ge.jett.rest;

import java.io.FileInputStream;
import java.io.FileOutputStream;
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

import javax.imageio.stream.FileImageInputStream;

import tt.ge.jett.live.Api;
import tt.ge.jett.live.JsonSocket;
import tt.ge.jett.live.MessageListener;
import tt.ge.jett.live.Pool;
import tt.ge.jett.rest.url.Client;
import tt.ge.jett.rest.url.ProgressListener;

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
		
		/*Api api = new Api();
		
		api.addMessageListener(new MessageListener.Adapter() {

			@Override
			public void download(String sharename, String fileid,
					String filename) {
				System.out.println("Donwload " + sharename + " " + filename);
			}
		});
		
		api.connect(user);
		api.run();*/
		
		/*Share share = user.getShare("8EkKhCP");
		
		share.uploadFile(new File("tmp/boromir.gif"));*/
		
		/*File file = share.getFile("0");
		
		file.download(new java.io.File("tmp/st.jpg"));*/
		
		Share share = user.createShare("Hello");
		/*File file = share.createFile("a.txt");
		
		file.upload("hello you!");*/
		File file = share.createFile("mastodon.mp3");
		
		file.addProgressListener(new ProgressListener.Adapter() {
			@Override
			public void start() {
				System.out.println("Started");
			}

			@Override
			public void end() {
				System.out.println("Ended");
			}

			@Override
			public void progress(long progress, int percent) {
				System.out.println("Progress " + progress + " - " + percent);
			}
		});
		
		file.upload(new java.io.File("tmp/mastodon.mp3"));
		
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
