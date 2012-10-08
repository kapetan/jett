package tt.ge.jett.rest;

import java.io.IOException;

import tt.ge.jett.rest.url.Helper;

public class Storage {
	public static Storage get(Token token) throws IOException {
		User user = Helper.get("users/me", token, User.class);
		return user.getStorage();
	}
	
	private long used;
	private long limit;
	private long extra;
	
	public long left() {
		return limit - used;
	}
	
	public boolean isLimitExceeded() {
		return left() <= 0;
	}

	public long getUsed() {
		return used;
	}

	public long getLimit() {
		return limit;
	}

	public long getExtra() {
		return extra;
	}
}
