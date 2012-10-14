package tt.ge.jett.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum FileType {
	IMAGE("png", "jpeg", "jpg", "gif"),
	AUDIO("mp3"),
	DOC("txt", "readme", "nfo", "html", "htm", "doc", "docx", "pdf", "ppt", "pptx", "tiff"),
	MISC;
	
	public static FileType getType(String filename) {
		int dot = filename.lastIndexOf('.');
		String extension = null;
		
		if(dot > 0 && dot < filename.length()) {
			extension = filename.substring(dot + 1);
			FileType[] types = values();
			
			for(int i = 0; i < types.length - 1; i++) {
				FileType type = types[i];
				
				if(type.matchesExtension(extension)) {
					return type;
				}
			}
		}
		
		return MISC;
	}
	
	private List<String> extensions;
	
	private FileType() {
		this.extensions = new ArrayList<String>();
	}
	
	private FileType(String... extensions) {
		this.extensions = Arrays.asList(extensions);
	}
	
	public boolean matchesFilename(String filename) {
		int dot = filename.indexOf('.');
		
		if(dot > 0 && dot < filename.length()) {
			return matchesExtension(filename.substring(dot + 1));
		}
		
		return false;
	}
	
	public boolean matchesExtension(String extension) {
		if(extension.startsWith(".")) {
			extension = extension.substring(1);
		}
		
		return extensions.contains(extension);
	}
}
