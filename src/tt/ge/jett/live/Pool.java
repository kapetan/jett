package tt.ge.jett.live;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import tt.ge.jett.rest.File;
import tt.ge.jett.rest.Token;

public class Pool extends Thread {
	enum FileEvent {
		DOWNLOAD,
		UPLOADING,
		UPLOAD,
		ERROR;
	}
	
	private static int STOP_ACTION = 0;
	
	private BlockingQueue<Notification> messageQueue = new PriorityBlockingQueue<Notification>();
	
	private List<Uploader> pool = new ArrayList<Uploader>();
	private List<Uploader> uploading = new ArrayList<Uploader>();
	
	private Api notifier;
	
	public Pool(Token token) throws IOException {
		this(token.getAccesstoken());
	}
	
	public Pool(String accesstoken) throws IOException {
		notifier = new Api();
		notifier.addMessageListener(new PoolMessageListener(this));
		
		notifier.connect(accesstoken);
		
		this.start();
	}
	
	public void add(String filename, File file) {
		Notification msg = Notification.add();
		msg.put("file", file);
		msg.put("filename", file);
		
		message(msg);
	}
	
	public String getSession() {
		return notifier.getSession();
	}
	
	public void event(FileEvent type, File file, Object extra) {
		Notification msg = Notification.event();
		msg.put("type", type);
		msg.put("file", file);
		msg.put("extra", extra);
		
		message(msg);
	}
	
	public void download(String sharename, String fileid) {
		Notification msg = Notification.download();
		msg.put("sharename", sharename);
		msg.put("fileid", fileid);
		
		message(msg);
	}
	
	public void error(String sharename, String fileid) {
		System.out.println("Error received in pool");
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				Notification msg = messageQueue.take();
				System.out.println(msg);
				
				switch(msg.getType()) {
				case SELF:
					int action = msg.get("action");
					
					if(action == STOP_ACTION) {
						return;
					}
					
					break;
				case DOWNLOAD:
					break;
				case ADD:
					if(!activeUploads()) {
						/*Uploader uploader = pool.remove(0);
						uploading.add(uploader);
						
						uploader.start();*/
					}
					
					break;
				case EVENT:
					break;
				}
			}
		} 
		catch (InterruptedException e) {}
		
		System.out.println("Should not be here");
	}
	
	public void message(Notification msg) {
		try {
			messageQueue.put(msg);
		} catch (InterruptedException e) {}
	}
	
	private boolean activeUploads() {
		for(Uploader u : uploading) {
			if(u.isRunning()) {
				return true;
			}
		}
		
		return false;
	}
}
