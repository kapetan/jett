package tt.ge.jett.live;

public interface MessageListener {
	void download(String sharename, String fileid, String filename, boolean increment);
	void storagelimit(String sharename, String fileid, String filename);
	void filestat(String sharename, String fileid, String filename, long size);
	void violatedterms(String sharename, String fileid, String filename, String reason);
	void error(Exception e);
	
	public static class Adapter implements MessageListener {
		@Override
		public void download(String sharename, String fileid, String filename, boolean increment) {}

		@Override
		public void storagelimit(String sharename, String fileid,
				String filename) {}

		@Override
		public void filestat(String sharename, String fileid, String filename, long size) {}

		@Override
		public void violatedterms(String sharename, String fileid,
				String filename, String reason) {}

		@Override
		public void error(Exception e) {}
	}
}
