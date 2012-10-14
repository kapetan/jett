package tt.ge.jett.live;

import java.io.IOException;
import java.util.List;

import tt.ge.jett.rest.File;

public class PoolMessageListener implements MessageListener {
	private Pool pool;
	
	public PoolMessageListener(Pool pool) {
		this.pool = pool;
	}

	@Override
	public void download(final String sharename, final String fileid, String filename) {
		pool.prioritizeUploadTask(sharename, fileid);
		pool.scheduleEvent(new Runnable() {
			@Override
			public void run() {
				List<FileListener> listeners = getFileMessageListeners(sharename, fileid);
				
				if(listeners == null) {
					return;
				}
				
				synchronized (listeners) {
					for(FileListener listener : listeners) {
						listener.download();
					}
				}
			}
		});
	}

	@Override
	public void storagelimit(final String sharename, final String fileid, String filename) {
		pool.scheduleEvent(new Runnable() {
			@Override
			public void run() {
				List<FileListener> listeners = getFileMessageListeners(sharename, fileid);
				
				if(listeners == null) {
					return;
				}
				
				synchronized (listeners) {
					for(FileListener listener : listeners) {
						listener.storagelimit();
					}
				}
			}
		});
	}

	@Override
	public void filestat(final String sharename, final String fileid, String filename,
			final long size) {
		pool.scheduleEvent(new Runnable() {
			@Override
			public void run() {
				List<FileListener> listeners = getFileMessageListeners(sharename, fileid);
				
				if(listeners == null) {
					return;
				}
				
				synchronized (listeners) {
					for(FileListener listener : listeners) {
						listener.filestat(size);
					}
				}
			}
		});
	}

	@Override
	public void violatedterms(final String sharename, final String fileid, String filename,
			final String reason) {
		pool.scheduleEvent(new Runnable() {
			@Override
			public void run() {
				List<FileListener> listeners = getFileMessageListeners(sharename, fileid);
				
				if(listeners == null) {
					return;
				}
				
				synchronized (listeners) {
					for(FileListener listener : listeners) {
						listener.violatedterms(reason);
					}
				}
			}
		});
	}

	@Override
	public void error(Exception e) {
		try {
			pool.reconnect();
		} catch (IOException exception) {}
	}
	
	private List<FileListener> getFileMessageListeners(String sharename, String fileid) {
		File file = pool.getFile(sharename, fileid);
		
		if(file != null) {
			return file.getListeners();
		} 
		
		return null;
	}
}
