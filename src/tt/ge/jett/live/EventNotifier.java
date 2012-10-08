package tt.ge.jett.live;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class EventNotifier extends Thread {
	private static final String HOST = "open.ge.tt";
	private static final int PORT = 443;
	
	private static final SecureRandom random = new SecureRandom();
	
	private static String generateSession() {
		return new BigInteger(130, random).toString(32);
	}
	
	private JsonSocket socket;
	private String session;
	
	private volatile boolean run = true;
	
	private List<NotificationListener> listeners = new ArrayList<NotificationListener>();
	
	public void connect(String accesstoken, String session) throws IOException {
		socket = new JsonSocket(HOST, PORT);
		socket.setGson(new GsonBuilder().registerTypeAdapter(EventType.class, 
			new JsonDeserializer<EventType>() {
				@Override
				public EventType deserialize(JsonElement json, Type arg1,
						JsonDeserializationContext arg2) throws JsonParseException {
					
					String eventType = json.getAsJsonPrimitive().getAsString();
					return EventType.valueOf(eventType.toUpperCase());
				}
		}).create());
		
		socket.connect();
		
		this.session = session;
		
		Map<String, String> connect = new HashMap<String, String>();
		connect.put("type", "connect");
		connect.put("accesstoken", accesstoken);
		connect.put("session", session);
		
		socket.send(connect);
		
		this.start();
	}
	
	public void connect(String accesstoken) throws IOException {
		connect(accesstoken, generateSession());
	}
	
	public void close() {
		run = false;
		
		try {
			socket.close();
		} catch (IOException e) {}
	}
	
	public String getSession() {
		return session;
	}
	
	public void addNotficationListener(NotificationListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void run() {
		try {
			while(run) {
				Notification msg = socket.receiveJson(Notification.class);
				
				System.out.println("Message " + msg);
				
				switch(msg.type) {
				case DOWNLOAD:
					for(NotificationListener l : listeners) {
						l.download(msg.sharename, msg.fileid, msg.filename);
					}
					
					break;
				case FILESTAT:
					for(NotificationListener l : listeners) {
						l.filestat(msg.sharename, msg.fileid, msg.filename, msg.size);
					}
					
					break;
				case STORAGELIMIT:
					for(NotificationListener l : listeners) {
						l.storagelimit(msg.sharename, msg.fileid, msg.filename);
					}
					
					break;
				case VIOLATEDTERMS:
					for(NotificationListener l : listeners) {
						l.violatedterms(msg.sharename, msg.fileid, msg.filename, msg.reason);
					}
				}
			}
		}
		catch(IOException e) {
			for(NotificationListener l : listeners) {
				l.error(e);
			}
		}
		finally {
			try {
				socket.close();
			} catch (IOException e) {}
		}
	}
}
