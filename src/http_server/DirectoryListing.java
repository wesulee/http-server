package http_server;

import java.io.File;
import java.util.ArrayList;

public class DirectoryListing {

	public DirectoryListing(File dir, ArrayList<String> uriPath) {
		File[] files = dir.listFiles();
	}

	public byte[] getBytes() {
		// TODO
		return null;
	}
}
