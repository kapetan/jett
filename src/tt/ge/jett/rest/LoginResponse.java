package tt.ge.jett.rest;

public class LoginResponse {
	private String accesstoken;
	private String refreshtoken;
	private long expires;
	private User user;
	
	public Token getToken() {
		return new Token(accesstoken, refreshtoken, expires);
	}
	
	public User getUser() {
		return user;
	}
}
