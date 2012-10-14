package tt.ge.jett.rest;

public interface FileListener {
	void uploadStart();
	void uploadProgress(long progress, int percent);
	void uploadEnd();
	void download();
	void storagelimit();
	void filestat(long size);
	void violatedterms(String reason);
	void error(Exception e);
	
	static class Adapter implements FileListener {
		@Override
		public void download() {}

		@Override
		public void storagelimit() {}

		@Override
		public void filestat(long size) {}

		@Override
		public void violatedterms(String reason) {}

		@Override
		public void error(Exception e) {}

		@Override
		public void uploadStart() {}

		@Override
		public void uploadProgress(long progress, int percent) {}

		@Override
		public void uploadEnd() {}
	}
}
