package tt.ge.jett.rest;

import java.util.List;

public interface FileProxyImplementor {
	void addFileListener(FileProxyListener listener);
	void removeFileListener(FileProxyListener listner);
	List<FileProxyListener> getFileListeners();
	
	public static abstract class Emitter implements FileProxyListener {
		@Override
		public void download(File file, boolean increment) {
			List<FileProxyListener> listeners = getFileImplementor().getFileListeners();
			
			synchronized(listeners) {
				for(FileProxyListener listener : listeners) {
					listener.download(file, increment);
				}
			}
		}

		@Override
		public void uploadStart(File file) {
			List<FileProxyListener> listeners = getFileImplementor().getFileListeners();
			
			synchronized(listeners) {
				for(FileProxyListener listener : listeners) {
					listener.uploadStart(file);
				}
			}
		}

		@Override
		public void uploadProgress(File file, long progress, int percent) {
			List<FileProxyListener> listeners = getFileImplementor().getFileListeners();
			
			synchronized(listeners) {
				for(FileProxyListener listener : listeners) {
					listener.uploadProgress(file, progress, percent);
				}
			}
		}

		@Override
		public void uploadEnd(File file) {
			List<FileProxyListener> listeners = getFileImplementor().getFileListeners();
			
			synchronized(listeners) {
				for(FileProxyListener listener : listeners) {
					listener.uploadEnd(file);
				}
			}
		}

		@Override
		public void storagelimit(File file) {
			List<FileProxyListener> listeners = getFileImplementor().getFileListeners();
			
			synchronized(listeners) {
				for(FileProxyListener listener : listeners) {
					listener.storagelimit(file);
				}
			}
		}

		@Override
		public void filestat(File file, long size) {
			List<FileProxyListener> listeners = getFileImplementor().getFileListeners();
			
			synchronized(listeners) {
				for(FileProxyListener listener : listeners) {
					listener.filestat(file, size);
				}
			}
		}

		@Override
		public void violatedterms(File file, String reason) {
			List<FileProxyListener> listeners = getFileImplementor().getFileListeners();
			
			synchronized(listeners) {
				for(FileProxyListener listener : listeners) {
					listener.violatedterms(file, reason);
				}
			}
		}

		@Override
		public void error(File file, Exception e) {
			List<FileProxyListener> listeners = getFileImplementor().getFileListeners();
			
			synchronized(listeners) {
				for(FileProxyListener listener : listeners) {
					listener.error(file, e);
				}
			}
		}

		public abstract FileProxyImplementor getFileImplementor();
	}
}
