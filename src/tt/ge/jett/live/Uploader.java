package tt.ge.jett.live;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import tt.ge.jett.rest.File;
import tt.ge.jett.rest.Upload;
import tt.ge.jett.rest.progress.ProgressListener;
import tt.ge.jett.rest.progress.ProgressListenerAdapter;
import tt.ge.jett.rest.url.Helper;

public class Uploader extends Thread {
	private File file;
	private String filepath;
	private Pool pool;
	
	private volatile boolean started = false;
	private volatile boolean finished = false;
	private volatile boolean error = false;
	
	public Uploader(String filepath, File file, Pool pool) {
		this.file = file;
		this.filepath = filepath;
		this.pool = pool;
	}
	
	public boolean isRunning() {
		return started && !(finished || error);
	}
	
	public boolean isStarted() {
		return started;
	}

	public boolean isFinished() {
		return finished;
	}

	public boolean hasErrorOccurred() {
		return error;
	}

	@Override
	public void run() {
		InputStream in = null;
		InputStream resp = null;
		
		started = true;
		
		try {
			java.io.File path = new java.io.File(filepath);
			in = new FileInputStream(path);
			ProgressListener listener = new LiveProgressListner(path.length(), this.pool, this.file);
			
			Upload upload = file.getUpload();
			
			if(upload == null) {
				upload = file.refreshUpload();
			}
			
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Length", String.valueOf(path.length()));
			
			pool.event(Pool.FileEvent.UPLOADING, this.file, 0);
			
			//resp = Helper.URL_CLIENT.request("PUT", upload.getPuturl(), 
			//		new HashMap<String, String>(), in, headers, listener);
			
			finished = true;
			
			pool.event(Pool.FileEvent.UPLOAD, this.file, null);
		} 
		catch (IOException e) {
			error = true;
		}
		finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {}
			}
			
			if(resp != null) {
				try {
					resp.close();
				} catch (IOException e) {}
			}
		}
	}
	
	class LiveProgressListner extends ProgressListenerAdapter {
		private long total;
		private int current = 0;
		
		private Pool pool;
		private File file;
		
		public LiveProgressListner(long total, Pool pool, File file) {
			this.total = total;
			this.pool = pool;
			this.file = file;
		}
		
		@Override
		public void progress(long progress, int percent) {
			int currentProgres = (int) Math.floor(100.0 * progress / total);
			
			if(currentProgres > current) {
				current = currentProgres;
				pool.event(Pool.FileEvent.UPLOADING, file, current);
			}
		}
	}
}
