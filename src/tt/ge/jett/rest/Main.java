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
import tt.ge.jett.live.FileListener;
import tt.ge.jett.live.JsonSocket;
import tt.ge.jett.live.MessageListener;
import tt.ge.jett.live.Pool;
import tt.ge.jett.live.UploadTask;
import tt.ge.jett.rest.progress.ProgressInputStream;
import tt.ge.jett.rest.progress.ProgressListener;
import tt.ge.jett.rest.progress.ProgressListenerAdapter;
import tt.ge.jett.rest.url.Client;
import tt.ge.jett.rest.url.Helper;

public class Main {
	public static void main(String... args) throws IOException, InterruptedException {
		Logger logger = Logger.getLogger("tt.ge.jett");
		Handler handler = new ConsoleHandler();
		
		handler.setLevel(Level.ALL);
		handler.setFormatter(new SimpleFormatter());
		
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.ALL);
		logger.addHandler(handler);
		
		/*java.io.File f = new java.io.File("tmp/mastodon.mp3");
		Map<String, String> headers = new HashMap<String, String>();
		
		headers.put("Content-Length", String.valueOf(f.length()));
		
		InputStream in = new ProgressInputStream(new FileInputStream(f), new ProgressListener() {
			@Override
			public void start() {
				System.out.println("Started");
			}
			
			@Override
			public void progress(long progress, int percent) {
				System.out.println("Progress " + progress + " " + percent);
			}
			
			@Override
			public void end() {
				System.out.println("Ended");
			}
		}, f.length());
		
		Helper.URL_CLIENT.request("PUT", "http://192.168.0.13:8080", 
				new HashMap<String, String>(), in, headers);*/
		
		User user = User.login("mirza+test@ge.tt", "x17980", "trkkx27wybbo3whfrp8gf9l2jll3di");
		
		user.connect();
		
		Share share = user.createShare("My Share");
		
		for(int i = 1; i <= 3; i++) {
			java.io.File f = new java.io.File(String.format("tmp/m%s.mp3", i));
			final File file = share.createFile(f.getName());
			
			file.addListener(new FileListener.Adapter() {
				@Override
				public void download() {
					System.out.println(file.getFileid() + " Downlaoded");
				}

				@Override
				public void uploadStart() {
					System.out.println(file.getFileid() + " Started");
				}

				@Override
				public void uploadProgress(long progress, int percent) {
					System.out.println(file.getFileid() + " Progress " + progress +  " -> " + percent);
				}

				@Override
				public void uploadEnd() {
					System.out.println(file.getFileid() + " Ended");
				}
			});
			
			file.upload(f);
		}
		
		/*final File file = share.createFile("m.wmv");
		
		file.addListener(new FileListener.Adapter() {

			@Override
			public void uploadStart() {
				System.out.println(file.getFileid() + " - " + file.getFilename() + " Started");
			}

			@Override
			public void uploadProgress(long progress, int percent) {
				System.out.println(file.getFileid() + " Progress " + progress +  " -> " + percent);
			}

			@Override
			public void uploadEnd() {
				System.out.println(file.getFileid() + " Ended");
			}
		});
		
		file.upload(new java.io.File("tmp/r (1).wmv"));*/
	}
}
