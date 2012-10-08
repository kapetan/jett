package tt.ge.jett.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tt.ge.jett.rest.url.Helper;

public class User {
	public static User login(String username, String password, String apikey) throws IOException {
		Map<String, String> auth = new HashMap<String, String>();
		auth.put("username", username);
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
	
	private String userid;
	private String fullname;
	private String email;
	private Storage storage;
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
		/*if(token.hasExpired()) {
			refreshToken();
		}*/
		
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
	
	public Storage getStorage() {
		return storage;
	}
	
	public List<Share> getShares() throws IOException {
		List<Share> shares = Share.find(token);
		
		for(Share share : shares) {
			share.setUser(this);
		}
		
		return shares;
	}
	
	public Share getShare(String sharename) throws IOException {
		Share share = Share.all(sharename);
		share.setUser(this);
		
		return share;
	}
	
	public Share createShare(Map<String, String> attributes) throws IOException {
		Share share = Share.create(token, attributes);
		share.setUser(this);
		
		return share;
	}
	
	public Share updateShare(String sharename, Map<String, String> attributes) throws IOException {
		Share share = Share.update(token, sharename, attributes);
		share.setUser(this);
		
		return share;
	}
	
	public void destroyShare(String sharename) throws IOException {
		Share.destroy(token, sharename);
	}
}
