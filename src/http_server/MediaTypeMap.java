package http_server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.logging.Level;

import javax.activation.FileTypeMap;

public class MediaTypeMap {

	private final String defaultType;
	private final HashMap<String, String> map;
	private final FileTypeMap sysMap;

	public MediaTypeMap(String defaultType) {
		this.defaultType = defaultType;
		this.map = new HashMap<String, String>();
		this.sysMap = FileTypeMap.getDefaultFileTypeMap();
	}

	public void put(String ext, String type) {
		map.put(ext, type);
	}

	public String get(File f) {
		String path = f.getName();
		String type = sysMap.getContentType(path);
		if (!type.equals(defaultType))
			return type;
		int index = path.lastIndexOf('.');
		if ((index == -1) || (index+1 >= path.length())) {
			return probeFile(f);
		}
		String extension = path.substring(index+1);
		type = map.get(extension);
		if (type == null)
			return probeFile(f);
		else
			return type;
	}

	private String probeFile(File f) {
		Path path = f.toPath();
		String type = null;
		try {
			type = Files.probeContentType(path);
		} catch (IOException e) {
			HTTPServer.INSTANCE.getLogger().log(Level.WARNING, "unable to probe content type", e);
		}
		if (type == null)
			return defaultType;
		else
			return type;
	}
}
