package tt.ge.jett.rest.progress;

import java.util.ArrayList;
import java.util.List;

public class CompositeProgressListener implements ProgressListener {
	private List<ProgressListener> listeners = new ArrayList<ProgressListener>();
	
	public void addProgressListener(ProgressListener listener) {
		listeners.add(listener);
	}
	
	public void removeProgressListener(ProgressListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void start() {
		for(ProgressListener listener : listeners) {
			listener.start();
		}
	}

	@Override
	public void end() {
		for(ProgressListener listener : listeners) {
			listener.end();
		}
	}

	@Override
	public void progress(long progress, int percent) {
		for(ProgressListener listener : listeners) {
			listener.progress(progress, percent);
		}
	}
}
