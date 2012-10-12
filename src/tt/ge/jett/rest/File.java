package tt.ge.jett.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import tt.ge.jett.rest.progress.CompositeProgressListener;
import tt.ge.jett.rest.progress.ProgressInputStream;
import tt.ge.jett.rest.progress.ProgressListener;
import tt.ge.jett.rest.url.Helper;

public class File {
	public static File find(String sharename, String fileid) throws IOException {
		File file = Helper.get(String.format("files/%s/%s", sharename, fileid), null, File.class);
		return file;
	}
	
	public static File create(Token token, String sharename, Map<String, String> attributes) throws IOException {
		File file = Helper.post(String.format("files/%s/create", sharename), token, attributes, File.class);
		return file;
	}
	
	public static File create(Token token, String sharename, String filename) throws IOException {
		Map<String, String> attributes = new HashMap<String, String>();
		
		attributes.put("filename", filename);
		
		return create(token, sharename, attributes);
	}
	
	public static void destroy(Token token, String sharename, String fileid) throws IOException {
		Helper.post(String.format("files/%s/%s/destroy", sharename, fileid), token, null, null);
	}
	
	public static File upload(Token token, String sharename, String filename, InputStream in) throws IOException {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("filename", filename);
		
		File file = create(token, sharename, attributes);
		
		upload(file, in);
		
		return file;
	}
	
	public static File upload(Token token, String sharename, String filename, String in) throws IOException {
		InputStream body = new ByteArrayInputStream(in.getBytes());
		
		return upload(token, sharename, filename, body);
	}
	
	public static File upload(Token token, String sharename, String filename, java.io.File file) throws IOException {
		InputStream body = new FileInputStream(file);
		
		try {
			return upload(token, sharename, filename, body);
		} finally {
			try {
				body.close();
			} catch(IOException e) {}
		}
	}
	
	public static File upload(Token token, String sharename, java.io.File file) throws IOException {
		return upload(token, sharename, file.getName(), file);
	}
	
	public static InputStream getBlob(String sharename, String fileid) throws IOException {
		return Helper.URL_CLIENT.request("GET", 
				Helper.apiUrl("files/%s/%s/blob", sharename, fileid));
	}
	
	public static InputStream getScaled(String sharename, String fileid, int width, int height) throws IOException {
		return Helper.URL_CLIENT.request("GET", 
				Helper.apiUrl("files/%s/%s/blob/%sx%s", sharename, 
						fileid, String.valueOf(width), String.valueOf(height)));
	}
	
	public static InputStream getThumb(String sharename, String fileid) throws IOException {
		return Helper.URL_CLIENT.request("GET", 
				Helper.apiUrl("files/%s/%s/blob/thumb", sharename, fileid));
	}
	
	public static void download(String sharename, String fileid, OutputStream out) throws IOException {
		InputStream in = getBlob(sharename, fileid);
		
		try {
			byte[] buffer = new byte[65535];
			int read = 0;
			
			while((read = in.read(buffer)) > 0) {
				out.write(buffer, 0, read);
			}
		} finally {
			try {
				in.close();
			} catch(IOException e) {}
		}
	}
	
	public static void download(String sharename, String fileid, java.io.File file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		
		try {
			download(sharename, fileid, out);
		} finally {
			try {
				out.close();
			} catch(IOException e) {}
		}
	}
	
	public static String read(String sharename, String fileid) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		download(sharename, fileid, out);
		
		return new String(out.toByteArray(), "UTF-8");
	}
	
	private static void upload(File file, InputStream in) throws IOException {
		Upload upload = file.getUpload();
		
		if(upload == null) {
			upload = file.refreshUpload();
		}
		
		file.readystate = ReadyState.UPLOADING;
		
		in = Helper.URL_CLIENT.request("PUT", upload.getPuturl(), 
				new HashMap<String, String>(), in, new HashMap<String, String>());
		
		in.close();
		
		file.readystate = ReadyState.UPLOADED;
	}
	
	private String fileid;
	private String filename;
	private int downloads;
	private String sharename;
	private ReadyState readystate;
	private Date created;
	private String getturl;
	private Upload upload;
	
	private transient Share share;
	private transient CompositeProgressListener listener = 
		new CompositeProgressListener();
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof File) {
			File file = (File) other;
			return file.sharename.equals(this.sharename) && file.fileid.equals(this.fileid);
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return Helper.GSON.toJson(this);
	}
	
	public Share getShare() {
		return share;
	}

	public void setShare(Share share) {
		this.sharename = share.getSharename();
		this.share = share;
	}

	public String getFileid() {
		return fileid;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public int getDownloads() {
		return downloads;
	}
	
	public String getSharename() {
		return sharename;
	}
	
	public ReadyState getReadystate() {
		return readystate;
	}
	
	public Date getCreated() {
		return created;
	}
	
	public String getGetturl() {
		return getturl;
	}
	
	public Upload getUpload() {
		return upload;
	}
	
	public void addUploadProgressListener(ProgressListener listener) {
		this.listener.addProgressListener(listener);
	}
	
	public void removeUploadProgressListener(ProgressListener listener) {
		this.listener.removeProgressListener(listener);
	}
	
	public void upload(InputStream in) throws IOException {
		upload(this, new ProgressInputStream(in, listener));
	}
	
	public void upload(String in) throws IOException {
		byte[] str = in.getBytes();
		InputStream stream = new ByteArrayInputStream(str);
		stream = new ProgressInputStream(stream, listener, str.length);
		
		upload(this, stream);
	}
	
	public void upload(java.io.File file) throws IOException {
		InputStream in = new FileInputStream(file);
		in = new ProgressInputStream(in, listener, file.length());
		
		try {
			upload(this, in);
		} finally {
			try {
				in.close();
			} catch(IOException e) {}
		}
	}
	
	public InputStream getBlob() throws IOException {
		return getBlob(sharename, fileid);
	}
	
	public InputStream getScaled(int width, int height) throws IOException {
		return getScaled(sharename, fileid, width, height);
	}
	
	public void download(OutputStream out) throws IOException {
		download(sharename, fileid, out);
	}
	
	public void download(java.io.File file) throws IOException {
		download(sharename, fileid, file);
	}
	
	public String read() throws IOException {
		return read(sharename, fileid);
	}
	
	public InputStream getThumb() throws IOException {
		return getThumb(sharename, fileid);
	}
	
	public void destroy() throws IOException {
		File.destroy(share.getUser().getToken(), sharename, fileid);
	}
	
	public Upload refreshUpload() throws IOException {
		upload = Upload.get(share.getUser().getToken(), sharename, fileid);
		return upload;
	}
}
