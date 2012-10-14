package tt.ge.jett.live;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Logger;

import tt.ge.jett.rest.File;

public class Pool {
	private static final int MAX_WORKERS = 2;
	private static final Logger LOGGER = Logger.getLogger(Pool.class.getName());
	
	private PriorityBlockingQueue<UploadTask> tasks;
	private List<Worker> workers;
	private Api api;
	private EventHandler events;
	private Map<String, File> cache = new HashMap<String, File>();
	
	private int uploaded = 0;
	
	public Pool(String accesstoken) throws IOException {
		tasks = new PriorityBlockingQueue<UploadTask>(10, new Comparator<UploadTask>() {
			@Override
			public int compare(UploadTask t1, UploadTask t2) {
				return -t1.getPriority().compareTo(t2.getPriority());
			}
		});
		
		workers = new ArrayList<Worker>(MAX_WORKERS);
		
		for(int i = 0; i < MAX_WORKERS; i++) {
			Worker worker = new Worker();
			
			worker.start();
			workers.add(worker);
		}
		
		events = new EventHandler();
		
		events.start();
		
		api = new Api();
		
		api.addMessageListener(new PoolMessageListener(this));
		api.connect(accesstoken);
		
		(new Thread(api)).start();
	}
	
	public void reconnect() throws IOException {
		api.close();
		
		api = api.reconnect();
		
		(new Thread(api)).start();
	}
	
	public void scheduleEvent(Runnable runnable) {
		events.schedule(runnable);
	}
	
	public void addMessageListener(MessageListener listener) {
		api.addMessageListener(listener);
	}
	
	public void removeMessageListener(MessageListener listener) {
		api.removeMessageListener(listener);
	}
	
	public String getSession() {
		return api.getSession();
	}
	
	public boolean isConnected() {
		return api.isConnected();
	}
	
	public void addUploadTask(UploadTask task) {
		tasks.offer(task);
	}
	
	public void prioritizeUploadTask(String sharename, String fileid) {
		synchronized (tasks) {
			for(UploadTask task : tasks) {
				File file = task.getFile();
				
				if(file.getSharename().equals(sharename) && file.getFileid().equals(fileid)) {
					task.prioritize();
					
					LOGGER.finest("Prioritizing task " + task);
				}
			}
		}
	}
	
	public void addFile(File file) {
		synchronized (cache) {
			cache.put(file.getSharename() + "-" + file.getFileid(), file);
		}
	}
	
	public File getFile(String sharename, String fileid) {
		synchronized (cache) {
			return cache.get(sharename + "-" + fileid);
		}
	}
	
	public void close() {
		LOGGER.info("Shutingdown Pool");
		
		api.close();
		
		for(int i = 0; i < workers.size(); i++) {
			Worker worker = workers.get(i);
			
			worker.run = false;
			tasks.offer(new NullUploadTask());
		}
	}
	
	class Worker extends Thread {
		private volatile boolean run = true;
		
		@Override
		public void run() {
			try {
				while(run) {
					final UploadTask task = tasks.take();
					
					if(task instanceof NullUploadTask) {
						break;
					}
					
					LOGGER.info("Worker uploading file " + task);
					
					try {
						task.upload();
						uploaded++;
					} catch (final IOException e) {
						LOGGER.severe("Upload failed: " + e.getMessage());
						
						if(task.shouldRetry()) {
							LOGGER.fine("Retrying file");
							
							tasks.offer(task);
						} else {
							LOGGER.warning("Upload aborted");
							
							scheduleEvent(new Runnable() {
								@Override
								public void run() {
									for(FileListener listener : task.getFile().getListeners()) {
										listener.error(e);
									}
								}
							});
						}
					}
				}
				
				LOGGER.finer("Worker finished");
			} catch (InterruptedException e) {}
		}
	}
	
	public static class NullUploadTask extends UploadTask {
		public NullUploadTask() {
			super(null);
		}
		
		@Override
		public boolean equals(Object obj) {
			return obj instanceof NullUploadTask;
		}
		
		@Override
		public String toString() {
			return "[File null]";
		}

		@Override
		public void deferredUpload() throws IOException {}
	}
}
