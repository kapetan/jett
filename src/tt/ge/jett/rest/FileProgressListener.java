package tt.ge.jett.rest;

import tt.ge.jett.live.FileListener;
import tt.ge.jett.rest.progress.ProgressListener;

public class FileProgressListener implements ProgressListener {
	private File file;
	
	public FileProgressListener(File file) {
		this.file = file;
	}

	@Override
	public void start() {
		for(FileListener listener : file.getListeners()) {
			listener.uploadStart();
		}
	}

	@Override
	public void end() {
		for(FileListener listener : file.getListeners()) {
			listener.uploadEnd();
		}
	}

	@Override
	public void progress(long progress, int percent) {
		for(FileListener listener : file.getListeners()) {
			listener.uploadProgress(progress, percent);
		}
	}

}
