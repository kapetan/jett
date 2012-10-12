package tt.ge.jett.rest.progress;

public interface ProgressListener {
	public static final int INDETERMINATE = -1;
	
	void start();
	void end();
	void progress(long progress, int percent);
}
