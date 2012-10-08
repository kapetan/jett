package tt.ge.jett.live;

public class PoolNotificationListener implements NotificationListener {
	
	private Pool pool;
	
	public PoolNotificationListener(Pool pool) {
		this.pool = pool;
	}

	@Override
	public void download(String sharename, String fileid, String filename) {
		pool.download(sharename, fileid);
	}

	@Override
	public void storagelimit(String sharename, String fileid, String filename) {
		// TODO Auto-generated method stub

	}

	@Override
	public void filestat(String sharename, String fileid, String filename,
			long size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void violatedterms(String sharename, String fileid, String filename,
			String reason) {
		// TODO Auto-generated method stub

	}

	@Override
	public void error(Exception e) {
		System.err.println(e);
	}
}
