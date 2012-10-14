package tt.ge.jett.live;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import tt.ge.jett.rest.Token;
import tt.ge.jett.rest.User;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class Api implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(Api.class.getName());
	private static final String HOST = "open.ge.tt";
	private static final int PORT = 443;
	
	private static final SecureRandom random = new SecureRandom();
	
	private static String generateSession() {
		return new BigInteger(130, random).toString(32);
	}
	
	private JsonSocket socket;
	private String session;
	private String accesstoken;
	
	private volatile boolean connected = false;
	private volatile boolean run = true;
	
	private ArrayList<MessageListener> listeners = new ArrayList<MessageListener>();
	
	public void connect(String accesstoken, String session) throws IOException {
		this.accesstoken = accesstoken;
		
		socket = new JsonSocket(HOST, PORT);
		socket.setGson(new GsonBuilder().registerTypeAdapter(MessageType.class, 
			new JsonDeserializer<MessageType>() {
				@Override
				public MessageType deserialize(JsonElement json, Type arg1,
						JsonDeserializationContext arg2) throws JsonParseException {
					
					String eventType = json.getAsJsonPrimitive().getAsString();
					return MessageType.valueOf(eventType.toUpperCase());
				}
		}).create());
		
		socket.connect();
		
		LOGGER.finer("Opening JsonSocket");
		
		this.session = session;
		
		Map<String, String> connect = new HashMap<String, String>();
		connect.put("type", "connect");
		connect.put("accesstoken", accesstoken);
		connect.put("session", session);
		
		socket.send(connect);
		
		connected = true;
	}
	
	public void connect(User user, String session) throws IOException {
		connect(user.getToken().getAccesstoken(), session);
	}
	
	public void connect(Token token, String session) throws IOException {
		connect(token.getAccesstoken(), session);
	}
	
	public void connect(String accesstoken) throws IOException {
		connect(accesstoken, generateSession());
	}
	
	public void connect(User user) throws IOException {
		connect(user.getToken().getAccesstoken());
	}
	
	public void connect(Token token) throws IOException {
		connect(token, generateSession());
	}
	
	@SuppressWarnings("unchecked")
	public Api reconnect() throws IOException {
		if(connected) {
			close();
		}
		
		Api api = new Api();
		api.listeners = (ArrayList<MessageListener>) listeners.clone();
		
		api.connect(accesstoken);
		
		return api;
	}
	
	public boolean isConnected() {
		return connected;
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
	
	public void addMessageListener(MessageListener listener) {
		listeners.add(listener);
	}
	
	public void removeMessageListener(MessageListener listener) {
		listeners.remove(listener);
	}
	
	@Override
	public void run() {
		try {
			while(run) {
				Message msg = socket.receiveJson(Message.class);
				
				LOGGER.fine("Message received");
				
				switch(msg.type) {
				case DOWNLOAD:
					for(MessageListener l : listeners) {
						l.download(msg.sharename, msg.fileid, msg.filename);
					}
					
					break;
				case FILESTAT:
					for(MessageListener l : listeners) {
						l.filestat(msg.sharename, msg.fileid, msg.filename, msg.size);
					}
					
					break;
				case STORAGELIMIT:
					for(MessageListener l : listeners) {
						l.storagelimit(msg.sharename, msg.fileid, msg.filename);
					}
					
					break;
				case VIOLATEDTERMS:
					for(MessageListener l : listeners) {
						l.violatedterms(msg.sharename, msg.fileid, msg.filename, msg.reason);
					}
				}
			}
		}
		catch(IOException e) {
			LOGGER.severe("Error reading message: " + e.getMessage());
			
			for(MessageListener l : listeners) {
				l.error(e);
			}
		}
		finally {
			connected = false;
			
			try {
				socket.close();
			} catch (IOException e) {}
		}
	}
}
