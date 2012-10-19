package tt.ge.jett.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tt.ge.jett.live.Pool;
import tt.ge.jett.rest.url.Helper;

public class User implements FileProxyImplementor {
	public static User login(String email, String password, String apikey) throws IOException {
		Map<String, String> auth = new HashMap<String, String>();
		auth.put("email", email);
		auth.put("password", password);
		auth.put("apikey", apikey);
		
		return login(auth);
	}
	
	public static User login(String refreshtoken) throws IOException {
		Map<String, String> auth = new HashMap<String, String>();
		auth.put("refreshtoken", refreshtoken);
		
		return login(auth);
	}
	
	public static User login(Map<String, String> auth) throws IOException {
		LoginResponse response = Helper.post("users/login", null, auth, LoginResponse.class);
		
		User user = response.getUser();
		user.setToken(response.getToken());
		
		return user;
	}
	
	public static User get(String accesstoken) {
		Token token = new Token(accesstoken);
		User user = new User();
		
		user.setToken(token);
		
		return user;
	}
	
	private String userid;
	private String fullname;
	private int files;
	private long downloads;
	private String email;
	private Storage storage;
	
	private transient List<FileProxyListener> listeners = new ArrayList<FileProxyListener>();
	private transient Pool pool;
	private transient Token token;
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof User) {
			User user = (User) other;
			return user.userid.equals(this.userid);
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return Helper.GSON.toJson(this);
	}
	
	public void connect() throws IOException {
		pool = new Pool(token.getAccesstoken());
	}
	
	public void disconnect() {
		if(pool == null) {
			return;
		}
		
		pool.close();
		
		pool = null;
	}
	
	public Pool getPool() {
		return pool;
	}
	
	public String getSession() {
		if(pool == null) {
			return null;
		}
		
		return pool.getSession();
	}
	
	public boolean isConnected() {
		return pool != null && pool.isConnected();
	}
	
	public Token refreshToken() throws IOException {
		User user = User.login(token.getRefreshtoken());
		token = user.getToken();
		
		return token;
	}
	
	public Storage refreshStorage() throws IOException {
		storage = Storage.get(token);
		return storage;
	}
	
	public Token getToken() {
		return token;
	}

	public void setToken(Token token) {
		this.token = token;
	}

	public String getUserid() {
		return userid;
	}
	
	public String getFullname() {
		return fullname;
	}
	
	public String getEmail() {
		return email;
	}
	
	public int getFiles() {
		return files;
	}
	
	public long getDownloads() {
		return downloads;
	}
	
	public Storage getStorage() {
		return storage;
	}
	
	public void addFileListener(FileProxyListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	
	public void removeFileListener(FileProxyListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	public List<FileProxyListener> getFileListeners() {
		return listeners;
	}
	
	public List<Share> getShares() throws IOException {
		List<Share> shares = Share.all(token);
		
		for(Share share : shares) {
			share(share);
		}
		
		return shares;
	}
	
	public Share getShare(String sharename) throws IOException {
		Share share = null;
		
		if(isConnected()) {
			share = pool.getShare(sharename);
		}
		if(share == null) {
			share = share(Share.find(sharename));
		}
		
		return share;
	}
	
	public Share createShare(Map<String, String> attributes) throws IOException {
		Share share = Share.create(token, attributes);
		
		return share(share);
	}
	
	public Share createShare(String title) throws IOException {
		Share share = Share.create(token, title);
		
		return share(share);
	}
	
	public Share createShare() throws IOException {
		Share share = Share.create(token);
		
		return share(share);
	}
	
	public Share updateShare(String sharename, Map<String, String> attributes) throws IOException {
		Share share = null; //Share.update(token, sharename, attributes);
		//share.setUser(this);
		
		if(isConnected()) {
			share = pool.getShare(sharename);
		}
		if(share == null) {
			share = share(Share.update(token, sharename, attributes));
		}
		
		return share;
	}
	
	public void destroyShare(String sharename) throws IOException {
		Share.destroy(token, sharename);
		pool.removeShare(sharename);
	}
	
	private Share share(Share share) {
		share.setUser(this);
		
		if(pool.isConnected()) {
			pool.addShare(share);
		}
		
		return share;
	}
}
