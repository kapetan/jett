package tt.ge.jett.live;

import java.util.List;

import tt.ge.jett.rest.File;
import tt.ge.jett.rest.progress.ProgressListener;

public class PoolProgressListener implements ProgressListener {
	private Pool pool;
	private File file;
	
	public PoolProgressListener(File file, Pool pool) {
		this.file = file;
		this.pool = pool;
	}

	@Override
	public void start() {
		pool.scheduleEvent(new Runnable() {
			@Override
			public void run() {
				List<FileListener> listeners = file.getListeners();
				
				synchronized(listeners) {
					for(FileListener listener : listeners) {
						listener.uploadStart();
					}
				}
			}
		});
	}

	@Override
	public void end() {
		pool.scheduleEvent(new Runnable() {
			@Override
			public void run() {
				List<FileListener> listeners = file.getListeners();
				
				synchronized(listeners) {
					for(FileListener listener : listeners) {
						listener.uploadEnd();
					}
				}
			}
		});
	}

	@Override
	public void progress(final long progress, final int percent) {
		pool.scheduleEvent(new Runnable() {
			@Override
			public void run() {
				List<FileListener> listeners = file.getListeners();
				
				synchronized(listeners) {
					for(FileListener listener : listeners) {
						listener.uploadProgress(progress, percent);
					}
				}
			}
		});
	}
}
