package tt.ge.jett.rest;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import tt.ge.jett.rest.url.Helper;

import com.google.gson.reflect.TypeToken;

public class Share {
	public static List<Share> find(Token token) throws IOException {
		String response = Helper.request("GET", "shares", token, null);
		Type type = new TypeToken<ArrayList<Share>>() {}.getType();
		
		return Helper.GSON.fromJson(response, type);
	}
	
	public static Share all(String sharename) throws IOException {
		return Helper.get(String.format("shares/%s", sharename), null, Share.class);
	}
	
	public static Share create(Token token, Map<String, String> attributes) throws IOException {
		return Helper.post("shares/create", token, null, Share.class);
	}
	
	public static Share update(Token token, String sharename, Map<String, String> attributes) throws IOException {
		return Helper.post(String.format("shares/%s/update", sharename), token, attributes, Share.class);
	}
	
	public static void destroy(Token token, String sharename) throws IOException {
		Helper.post(String.format("shares/%s/destroy", sharename), token, null, null);
	}
	
	private String sharename;
	private String title;
	//private ReadyState readystate;
	private Date created;
	private boolean live;
	private List<File> files;
	
	private transient User user;
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Share) {
			Share share = (Share) other;
			return share.sharename.equals(this.sharename);
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return Helper.GSON.toJson(this);
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getSharename() {
		return sharename;
	}
	
	public String getTitle() {
		return title;
	}
	
	/*public ReadyState getReadystate() {
		return readystate;
	}*/
	
	public Date getCreated() {
		return created;
	}
	
	public boolean isLive() {
		return live;
	}
	
	public List<File> getFiles() {
		return files;
	}
	
	public File getFile(String fileid) {
		for(File file : files) {
			if(file.getFileid().equals(fileid)) {
				file.setShare(this);
				return file;
			}
		}
		
		return null;
	}
	
	public File createFile(Map<String, String> attributes) throws IOException {
		File file = File.create(user.getToken(), sharename, attributes);
		file.setShare(this);
		
		files.add(0, file);
		
		return file;
	}
	
	public void destroyFile(String fileid) throws IOException {
		File file = getFile(fileid);
		File.destroy(user.getToken(), sharename, fileid);
		
		files.remove(file);
	}
	
	public File uploadFile(String filepath) throws IOException {
		File file = File.upload(user.getToken(), sharename, filepath);
		file.setShare(this);
		
		return file;
	}
	
	public void destroy() throws IOException {
		Share.destroy(user.getToken(), sharename);
	}
	
	public Share update(Map<String, String> attributes) throws IOException {
		Share share = Share.update(user.getToken(), sharename, attributes);
		this.files = share.files;
		this.title = share.title;
		this.live = share.live;
		
		return this;
	}
}
