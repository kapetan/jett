package tt.ge.jett.live;

public interface NotificationListener {
	void download(String sharename, String fileid, String filename);
	void storagelimit(String sharename, String fileid, String filename);
	void filestat(String sharename, String fileid, String filename, long size);
	void violatedterms(String sharename, String fileid, String filename, String reason);
	void error(Exception e);
}
