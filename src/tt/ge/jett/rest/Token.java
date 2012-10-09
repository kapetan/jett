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
	
	public Token(String accesstoken) {
		this.accesstoken = accesstoken;
		
		String[] expires = accesstoken.split("\\.");
		
		if(expires.length >= 4) {
			try {
				this.expires = Long.parseLong(expires[3]) * 1000;
			} catch(NumberFormatException e) {
				this.expires = 0;
			}
		}
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
