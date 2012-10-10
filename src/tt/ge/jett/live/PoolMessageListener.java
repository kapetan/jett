package tt.ge.jett.live;

public class PoolMessageListener extends MessageListener.Adapter {
	
	private Pool pool;
	
	public PoolMessageListener(Pool pool) {
		this.pool = pool;
	}

	@Override
	public void download(String sharename, String fileid, String filename) {
		pool.download(sharename, fileid);
	}

	@Override
	public void error(Exception e) {
		System.err.println(e);
	}
}
