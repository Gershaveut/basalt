package dev.code_offline.basalt_server;

import dev.code_offline.basalt_share.Util;
import org.h2.store.fs.FilePath;
import org.h2.store.fs.FilePathWrapper;


public class ApplicationFilePathWrapper extends FilePathWrapper {
	private static final String[][] MAPPING = {
			{".mv.db", Util.APPLICATION_FORMAT},
			{".lock.db", Util.APPLICATION_FORMAT + ".lock"}
	};
	
	@Override
	public String getScheme() {
		return "save";
	}
	
	@Override
	public FilePathWrapper wrap(FilePath base) {
		ApplicationFilePathWrapper wrapper = (ApplicationFilePathWrapper) super.wrap(base);
		wrapper.name = getPrefix() + wrapExtension(base.toString());
		return wrapper;
	}
	
	@Override
	protected FilePath unwrap(String path) {
		String newName = path.substring(getScheme().length() + 1);
		newName = unwrapExtension(newName);
		return FilePath.get(newName);
	}
	
	protected static String wrapExtension(String fileName) {
		for (String[] pair : MAPPING) {
			if (fileName.endsWith(pair[1])) {
				fileName = fileName.substring(0, fileName.length() - pair[1].length()) + pair[0];
				break;
			}
		}
		return fileName;
	}
	
	protected static String unwrapExtension(String fileName) {
		for (String[] pair : MAPPING) {
			if (fileName.endsWith(pair[0])) {
				fileName = fileName.substring(0, fileName.length() - pair[0].length()) + pair[1];
				break;
			}
		}
		return fileName;
	}
}
