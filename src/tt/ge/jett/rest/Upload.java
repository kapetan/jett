package tt.ge.jett.rest;

import java.io.IOException;

import tt.ge.jett.rest.url.Helper;

public class Upload {
	private String puturl;
	private String posturl;
	
	public static Upload get(Token token, String sharename, String fileid) throws IOException {
		return Helper.get(String.format("files/%s/%s/upload", sharename, fileid), token, Upload.class);
	}
	
	public String getPuturl() {
		return puturl;
	}
	
	public String getPosturl() {
		return posturl;
	}
}
