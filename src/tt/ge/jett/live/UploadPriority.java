package tt.ge.jett.live;

public enum UploadPriority {
	NORMAL,
	MEDIUM,
	HIGH;
	
	public UploadPriority increase() {
		int next = ordinal() + 1;
		UploadPriority[] priorities = values();
		
		if(next >= priorities.length) {
			return this;
		}
		
		return priorities[next];
	}
	
	public UploadPriority decrease() {
		int next = ordinal() - 1;
		UploadPriority[] priorities = values();
		
		if(next < 0) {
			return this;
		}
		
		return priorities[next];
	}
}
