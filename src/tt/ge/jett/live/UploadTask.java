package tt.ge.jett.live;

import java.io.IOException;

import tt.ge.jett.rest.File;

public abstract class UploadTask {
	private static final int MAX_RETRIES = 3;
	
	private File file;
	private UploadStatus status;
	private UploadPriority priority;
	private int retries = 0;
	
	public UploadTask(File file) {
		this.file = file;
		this.priority = UploadPriority.NORMAL;
		this.status = UploadStatus.IDLE;
	}
	
	@Override
	public String toString() {
		return String.format("[File %s/%s (%s) {priority=%s, status=%s}]", 
				file.getSharename(), file.getFileid(), file.getFilename(), priority, status);
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof UploadTask)) {
			return false;
		}
		
		UploadTask task = (UploadTask) other;
		
		return task.file.equals(file);
	}
	
	public boolean shouldRetry() {
		boolean retry = retries++ < MAX_RETRIES;
		
		if(!retry) {
			status = UploadStatus.FAILED;
		}
		
		return retry;
	}
	
	public void prioritize(){
		priority = priority.increase();
	}
	
	public UploadPriority getPriority() {
		return priority;
	}

	public UploadStatus getStatus() {
		return status;
	}
	
	public File getFile() {
		return file;
	}

	public void upload() throws IOException {
		status = UploadStatus.UPLOADING;
		
		deferredUpload();
		
		status = UploadStatus.UPLOADED;
	}
	
	public abstract void deferredUpload() throws IOException;
}
