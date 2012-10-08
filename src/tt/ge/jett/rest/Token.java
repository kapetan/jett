package tt.ge.jett.rest;

public class Token {
	private String accesstoken;
	private String refreshtoken;
	private long expires;
	
	public Token(String accesstoken, String refreshtoken, long expires) {
		this.accesstoken = accesstoken;
		this.refreshtoken = refreshtoken;
		this.expires = expires * 1000 + System.currentTimeMillis();
	}
	
	public boolean hasExpired() {
		return expires <= System.currentTimeMillis();
	}
	
	public String getAccesstoken() {
		return accesstoken;
	}
	
	public String getRefreshtoken() {
		return refreshtoken;
	}
	
	public long getExpires() {
		return expires;
	}
}
