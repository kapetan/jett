package tt.ge.jett.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tt.ge.jett.live.Pool;
import tt.ge.jett.live.PoolProgressListener;
import tt.ge.jett.live.UploadTask;
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
		File file = create(token, sharename, filename);
		upload(file, in, UNKNOWN_LENGTH);
		
		return file;
	}
	
	public static File upload(Token token, String sharename, String filename, String in) throws IOException {
		byte[] str = in.getBytes();
		InputStream body = new ByteArrayInputStream(str);
		File file = create(token, sharename, filename);
		
		upload(file, body, str.length);
		
		return file;
	}
	
	public static File upload(Token token, String sharename, String filename, java.io.File file) throws IOException {
		InputStream body = new FileInputStream(file);
		File f = create(token, sharename, filename);
		
		try {
			upload(f, body, file.length());
			
			return f;
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
	
	private static final int UNKNOWN_LENGTH = -1;
	
	private static void upload(File file, InputStream in, long length) throws IOException {
		Upload upload = file.getUpload();
		Map<String, String> headers = new HashMap<String, String>();
		
		if(upload == null) {
			upload = file.refreshUpload();
		}
		if(length >= 0) {
			headers.put("Content-Length", String.valueOf(length));
		}
		
		file.readystate = ReadyState.UPLOADING;
		
		in = Helper.URL_CLIENT.request("PUT", upload.getPuturl(), 
				new HashMap<String, String>(), in, headers);
		
		in.close();
		
		file.readystate = ReadyState.UPLOADED;
	}
	
	private String fileid;
	private String filename;
	private volatile int downloads;
	private String sharename;
	private ReadyState readystate;
	private Date created;
	private String getturl;
	private Upload upload;
	
	private transient volatile int uploadProgress;
	private transient Share share;
	//private transient CompositeProgressListener listener = 
	//	new CompositeProgressListener();
	private transient List<FileListener> listeners =
		new ArrayList<FileListener>();
	
	public File() {
		uploadProgress = readystate == ReadyState.UPLOADED ? 100 : 0;
		
		final FileProxyImplementor.Emitter share = new FileProxyImplementor.Emitter() {
			@Override
			public FileProxyImplementor getFileImplementor() {
				return File.this.share;
			}
		};
		
		addListener(new FileListener() {
			@Override
			public void download(boolean increment) {
				if(increment) {
					downloads++;
				}
				
				share.download(File.this, increment);
			}

			@Override
			public void uploadStart() {
				uploadProgress = 0;
				
				share.uploadStart(File.this);
			}

			@Override
			public void uploadProgress(long progress, int percent) {
				if(percent != ProgressListener.INDETERMINATE) {
					uploadProgress = percent;
				}
				
				share.uploadProgress(File.this, progress, percent);
			}

			@Override
			public void uploadEnd() {
				uploadProgress = 100;
				
				share.uploadEnd(File.this);
			}

			@Override
			public void storagelimit() {
				share.storagelimit(File.this);
			}

			@Override
			public void filestat(long size) {
				share.filestat(File.this, size);
			}

			@Override
			public void violatedterms(String reason) {
				share.violatedterms(File.this, reason);
			}

			@Override
			public void error(Exception e) {
				share.error(File.this, e);
			}
		});
	}
	
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
	
	public User getUser() {
		return share.getUser();
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
	
	public FileType getFileType() {
		return FileType.getType(filename);
	}
	
	public int getUploadProgress() {
		return uploadProgress;
	}
	
	public void addListener(FileListener listener) {
		synchronized (listeners) {
			listeners.add(listener);	
		}
	}
	
	public void removeListener(FileListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	public List<FileListener> getListeners() {
		return listeners;
	}
	
	/*public void addUploadProgressListener(ProgressListener listener) {
		this.listener.addProgressListener(listener);
	}
	
	public void removeUploadProgressListener(ProgressListener listener) {
		this.listener.removeProgressListener(listener);
	}*/
	
	/*public void upload(InputStream in) throws IOException {
		upload(this, new ProgressInputStream(in, listener), UNKNOWN_LENGTH);
	}
	
	public void upload(String in) throws IOException {
		byte[] str = in.getBytes();
		InputStream stream = new ByteArrayInputStream(str);
		stream = new ProgressInputStream(stream, listener, str.length);
		
		upload(this, stream, str.length);
	}
	
	public void upload(java.io.File file) throws IOException {
		InputStream in = new FileInputStream(file);
		in = new ProgressInputStream(in, listener, file.length());
		
		try {
			upload(this, in, file.length());
		} finally {
			try {
				in.close();
			} catch(IOException e) {}
		}
	}*/
	
	public void upload(final InputStream in) throws IOException {
		Pool pool = getPool();
		
		if(pool == null) {
			uploadInputStream(in);
			return;
		}
		
		pool.addUploadTask(new UploadTask(this) {
			@Override
			public void deferredUpload() throws IOException {
				uploadInputStream(in);
			}
		});
	}
	
	public void upload(final String in) throws IOException {
		Pool pool = getPool();
		
		if(pool == null) {
			uploadString(in);
			return;
		}
		
		pool.addUploadTask(new UploadTask(this) {
			@Override
			public void deferredUpload() throws IOException {
				uploadString(in);
			}
		});
	}
	
	public void upload(final java.io.File file) throws IOException {
		Pool pool = getPool();
		
		if(pool == null) {
			uploadFile(file);
			return;
		}
		
		pool.addUploadTask(new UploadTask(this) {
			@Override
			public void deferredUpload() throws IOException {
				uploadFile(file);
			}
		});
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
		share.destroyFile(fileid);
		//File.destroy(share.getUser().getToken(), sharename, fileid);
	}
	
	public Upload refreshUpload() throws IOException {
		upload = Upload.get(share.getUser().getToken(), sharename, fileid);
		return upload;
	}
	
	private void uploadInputStream(InputStream in) throws IOException {
		upload(this, new ProgressInputStream(in, getProgressListener()), UNKNOWN_LENGTH);
	}

	private void uploadString(String in) throws IOException {
		byte[] str = in.getBytes();
		InputStream stream = new ByteArrayInputStream(str);
		stream = new ProgressInputStream(stream, getProgressListener(), str.length);
		
		upload(this, stream, str.length);
	}
	
	private void uploadFile(java.io.File file) throws IOException {
		InputStream in = new FileInputStream(file);
		in = new ProgressInputStream(in, getProgressListener(), file.length());
		
		try {
			upload(this, in, file.length());
		} finally {
			try {
				in.close();
			} catch(IOException e) {}
		}
	}
	
	private ProgressListener getProgressListener() {
		Pool pool = getPool();
		
		return pool ==  null ? new FileProgressListener(this) : 
			new PoolProgressListener(this, pool);
	}
	
	private Pool getPool() {
		User user = getUser();
		
		return user.isConnected() ? user.getPool() : null;
	}
}
