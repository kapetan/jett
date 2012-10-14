package tt.ge.jett.live;

import java.util.concurrent.LinkedBlockingQueue;

public class EventHandler extends Thread {
	private LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
	private volatile boolean run = true;
	
	public void schedule(Runnable run) {
		queue.offer(run);
	}
	
	public void close() {
		run = false;
		
		schedule(new NullRunnable());
	}
	
	@Override
	public void run() {
		try {
			while(run) {
				Runnable runnable = queue.take();
				
				if(runnable instanceof NullRunnable) {
					break;
				}
				
				runnable.run();
			}
		} catch (InterruptedException e) {}
	}
	
	static class NullRunnable implements Runnable {
		@Override
		public void run() {}
	}
}
