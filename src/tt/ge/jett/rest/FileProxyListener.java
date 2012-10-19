package tt.ge.jett.rest;

public interface FileProxyListener {
	void uploadStart(File file);
	void uploadProgress(File file, long progress, int percent);
	void uploadEnd(File file);
	void download(File file, boolean increment);
	void storagelimit(File file);
	void filestat(File file, long size);
	void violatedterms(File file, String reason);
	void error(File file, Exception e);
	
	public static class Adapter implements FileProxyListener {
		@Override
		public void uploadStart(File file) {}

		@Override
		public void uploadProgress(File file, long progress, int percent) {}

		@Override
		public void uploadEnd(File file) {}

		@Override
		public void download(File file, boolean increment) {}

		@Override
		public void storagelimit(File file) {}

		@Override
		public void filestat(File file, long size) {}

		@Override
		public void violatedterms(File file, String reason) {}

		@Override
		public void error(File file, Exception e) {}
	}
}
