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
	
	public static void destroy(Token token, String sharename, String fileid) throws IOException {
		Helper.post(String.format("files/%s/%s/destroy", sharename, fileid), token, null, null);
	}
	
	public static File upload(Token token, String sharename, String filepath) throws IOException {
		java.io.File f = new java.io.File(filepath);
		
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("filename", f.getName());
		
		File file = create(token, sharename, attributes);
		
		InputStream in = new FileInputStream(filepath);
		file.write(in, f.length());
		
		in.close();
		
		return file;
	}
	
	public static File download(String sharename, String fileid, String path) throws IOException {
		File file = find(sharename, fileid);
		
		java.io.File filepath = new java.io.File(path);
		
		if(filepath.isDirectory()) {
			filepath = new java.io.File(filepath, file.filename);
		}
		
		OutputStream out = new FileOutputStream(filepath);
		InputStream in = file.getBlob();
		
		byte[] buffer = new byte[65535];
		int read = 0;
		
		while((read = in.read(buffer)) > 0) {
			out.write(buffer, 0, read);
		}
		
		out.flush();
		out.close();
		
		in.close();
		
		return file;
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
		/*if(upload ==  null) {
			upload = Upload.get(share.getUser().getToken(), sharename, fileid);
		}*/
		
		return upload;
	}
	
	public InputStream getThumb() throws IOException {
		if(readystate == ReadyState.UPLOADED) {
			Map<String, String> empty = new HashMap<String, String>();
			
			return Helper.URL_CLIENT.request("GET", 
					Helper.apiUrl("files/%s/%s/blob/thumb", sharename, fileid), empty, null, empty);
		}
		else {
			throw new IllegalStateException("File must be uploaded, can't retrieve thumb");
		}
	}
	
	public InputStream getScaled(int width, int height) throws IOException {
		if(readystate == ReadyState.UPLOADED) {
			Map<String, String> empty = new HashMap<String, String>();
			Map<String, String> query = new HashMap<String, String>();
			query.put("size", String.format("%sx%s", width, height));
			
			return Helper.URL_CLIENT.request("GET", 
					Helper.apiUrl("files/%s/%s/blob/scale", sharename, fileid), query, null, empty);
		}
		else {
			throw new IllegalStateException("File must be uploaded, can't retrieve thumb");
		}
	}
	
	public InputStream getBlob() throws IOException {
		if((readystate == ReadyState.UPLOADED || readystate == ReadyState.UPLOADING) || 
				(readystate == ReadyState.REMOTE && share.isLive())) {
			
			Map<String, String> query = new HashMap<String, String>();
			Map<String, String> headers = new HashMap<String, String>();
			
			return Helper.URL_CLIENT.request("GET", 
					Helper.apiUrl("files/%s/%s/blob", sharename, fileid), query, null, headers);
		}
		else {
			throw new IllegalStateException("Wrong state, can't retrieve blob");
		}
	}
	
	public String read() throws IOException {
		InputStream in = getBlob();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		byte[] buffer = new byte[65535];
		int read = 0;
		
		while((read = in.read(buffer)) > 0) {
			out.write(buffer, 0, read);
		}
		
		in.close();
		
		return new String(out.toByteArray(), "UTF-8");
	}
	
	public void download(String path) throws IOException {
		File.download(sharename, fileid, path);
	}
	
	public void write(InputStream in, long contentLength) throws IOException {
		Upload upload = this.upload;
		
		if(upload == null) {
			upload = refreshUpload();
		}
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Length", String.valueOf(contentLength));
		
		readystate = ReadyState.UPLOADING;
		
		in = Helper.URL_CLIENT.request("PUT", 
				upload.getPuturl(), new HashMap<String, String>(), in, headers);
		
		in.close();
		
		readystate = ReadyState.UPLOADED;
	}
	
	public void write(String str) throws IOException {
		byte[] body = str.getBytes();
		ByteArrayInputStream in = new ByteArrayInputStream(body);
		
		write(in, body.length);
	}
	
	public void destroy() throws IOException {
		File.destroy(share.getUser().getToken(), sharename, fileid);
	}
	
	public Upload refreshUpload() throws IOException {
		upload = Upload.get(share.getUser().getToken(), sharename, fileid);
		return upload;
	}
}
