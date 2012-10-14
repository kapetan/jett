package tt.ge.jett.rest.progress;

import java.io.IOException;
import java.io.InputStream;

public class ProgressInputStream extends InputStream {
	private InputStream in;
	private long expected = 0;
	private long soFar = 0;
	private int lastPercent = -1;
	private ProgressListener listener;
	
	public ProgressInputStream(InputStream in, ProgressListener listener){
		this(in, listener, 0);
	}
	
	public ProgressInputStream(InputStream in, ProgressListener listener, long expected) {
		this.in = in;
		this.expected = expected;
		this.listener = listener;
	}

	@Override
	public int read(byte[] b) throws IOException {
		if(soFar == 0) {
			listener.start();
		}
		
		int len = in.read(b);

		if(len > 0) {
			progress(len);
		}
		if(len == -1) {
			listener.end();
		}
		
		return len;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if(soFar == 0) {
			listener.start();
		}
		
		int length = in.read(b, off ,len);
		
		if(length > 0) {
			progress(length);
		}
		if(length == -1) {
			listener.end();
		}
		
		return length;
	}

	@Override
	public long skip(long n) throws IOException {
		if(soFar == 0) {
			listener.start();
		}
		
		long len = in.skip(n);
		
		if(len > 0) {
			progress(len);
		}
		if(len == -1) {
			listener.end();
		}
		
		return len;
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		in.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		in.reset();
	}

	@Override
	public boolean markSupported() {
		return in.markSupported();
	}

	@Override
	public int read() throws IOException {
		if(soFar == 0) {
			listener.start();
		}
		
		int b = in.read();
		
		if(b >= 0) {
			progress(1);
		}
		if(b == -1) {
			listener.end();
		}
		
		return b;
	}
	
	private void progress(long len) {
		if(len == 0) {
			return;
		}
		
		soFar += len;
		int percent = 0;
		
		if(expected != 0) {
			percent = (int) (100 * soFar / expected);
			
			if(lastPercent == percent) {
				return;
			}
			
			lastPercent = percent;
		}
		
		listener.progress(soFar, expected == 0 ? ProgressListener.INDETERMINATE : percent);
	}
}
