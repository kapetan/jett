package tt.ge.jett.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tt.ge.jett.rest.url.Helper;

import com.google.gson.reflect.TypeToken;

public class Share {
	public static List<Share> all(Token token) throws IOException {
		String response = Helper.request("GET", "shares", token, null);
		Type type = new TypeToken<ArrayList<Share>>() {}.getType();
		
		return Helper.GSON.fromJson(response, type);
	}
	
	public static Share find(String sharename) throws IOException {
		return Helper.get(String.format("shares/%s", sharename), null, Share.class);
	}
	
	public static Share create(Token token, Map<String, String> attributes) throws IOException {
		return Helper.post("shares/create", token, attributes, Share.class);
	}
	
	public static Share create(Token token, String title) throws IOException {
		Map<String, String> attributes = new HashMap<String, String>();
		
		attributes.put("title", title);
		
		return create(token, attributes);
	}
	
	public static Share create(Token token) throws IOException {
		Map<String, String> attributes = new HashMap<String, String>();
		return create(token, attributes);
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
		if(user.isConnected()) {
			attributes.put("session", user.getSession());
		}
		
		File file = File.create(user.getToken(), sharename, attributes);
		
		file.setShare(this);
		files.add(0, file);
		
		return file;
		
		//return addFile(File.create(user.getToken(), sharename, attributes));
	}
	
	public File createFile(String filename) throws IOException {
		Map<String, String> attributes = new HashMap<String, String>();
		
		attributes.put("filename", filename);
		
		return createFile(attributes);
		
		//return addFile(File.create(user.getToken(), sharename, filename));
	}
	
	public void destroyFile(String fileid) throws IOException {
		File file = getFile(fileid);
		File.destroy(user.getToken(), sharename, fileid);
		
		files.remove(file);
	}
	
	public File uploadFile(String filename, InputStream in) throws IOException {
		//return addFile(File.upload(user.getToken(), sharename, filename, in));
		File file = createFile(filename);
		
		file.upload(in);
		
		return file;
	}
	
	public File uploadFile(String filename, String in) throws IOException {
		//return addFile(File.upload(user.getToken(), sharename, filename, in));
		File file = createFile(filename);
		
		file.upload(in);
		
		return file;
	}
	
	public File uploadFile(java.io.File file) throws IOException {
		//return addFile(File.upload(user.getToken(), sharename, file));
		
		File f = createFile(file.getName());
		
		f.upload(file);
		
		return f;
	}
	
	public InputStream getBlobFile(String fileid) throws IOException {
		return File.getBlob(sharename, fileid);
	}
	
	public InputStream getScalledFile(String fileid, int width, int height) throws IOException {
		return File.getScaled(sharename, fileid, width, height);
	}
	
	public InputStream getThumbFile(String fileid) throws IOException {
		return File.getThumb(sharename, fileid);
	}
	
	public void downloadFile(String fileid, OutputStream out) throws IOException {
		File.download(sharename, fileid, out);
	}
	
	public void downloadFile(String fileid, java.io.File file) throws IOException {
		File.download(sharename, fileid, file);
	}
	
	public String readFile(String fileid) throws IOException {
		return File.read(sharename, fileid);
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
	
	/*private File addFile(File file) {
		file.setShare(this);
		files.add(0, file);
		
		return file;
	}*/
	
	/*private File createFile(String filename) {
		Map<String, String> attrs = new HashMap<String, String>();
		
		attrs.put("filename", filename);
		
		if(user.isConnected()) {
			attrs.put("session", user.getSession());
		}
		
		File file = File.create(user.getToken(), sharename, attrs);
				
		file.setShare(this);
		files.add(0, file);
		
		return file;
	}*/
}
