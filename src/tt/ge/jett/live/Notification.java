package tt.ge.jett.live;

import java.util.HashMap;
import java.util.Map;

public class Notification implements Comparable<Notification> {
	enum Type {
		SELF,
		DOWNLOAD,
		ADD,
		EVENT;
	}
	
	public static Notification self() {
		return new Notification(Type.SELF);
	}
	
	public static Notification download() {
		return new Notification(Type.DOWNLOAD);
	}
	
	public static Notification add() {
		return new Notification(Type.ADD);
	}
	
	public static Notification event() {
		return new Notification(Type.EVENT);
	}
	
	private Type type;
	private Map<String, Object> parameters = new HashMap<String, Object>();
	private long time;
	
	public Notification(Type type) {
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
	public int compareTo(Notification o) {
		int type = this.type.ordinal() - o.type.ordinal();
		if(type == 0) {
			return (int) (time - o.time);
		}
		
		return type;
	}
}
