package tt.ge.jett.live;

import java.util.HashMap;
import java.util.Map;

public class Message implements Comparable<Message> {
	enum Type {
		SELF,
		DOWNLOAD,
		ADD,
		EVENT;
	}
	
	public static Message self() {
		return new Message(Type.SELF);
	}
	
	public static Message download() {
		return new Message(Type.DOWNLOAD);
	}
	
	public static Message add() {
		return new Message(Type.ADD);
	}
	
	public static Message event() {
		return new Message(Type.EVENT);
	}
	
	private Type type;
	private Map<String, Object> parameters = new HashMap<String, Object>();
	private long time;
	
	public Message(Type type) {
		this.type = type;
		this.time = System.currentTimeMillis();
	}
	
	public Type getType() {
		return type;
	}
	
	public void put(String name, Object obj) {
		parameters.put(name, obj);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String name) {
		return (T) parameters.get(name);
	}
	
	@Override
	public String toString() {
		return String.format("[Message { type: %s, time: %s, parameters: %s }]", type, time, parameters);
	}

	@Override
	public int compareTo(Message o) {
		int type = this.type.ordinal() - o.type.ordinal();
		if(type == 0) {
			return (int) (time - o.time);
		}
		
		return type;
	}
}
