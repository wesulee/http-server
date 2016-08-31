package http_server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DefaultHandler implements RequestHandler {

	private ArrayList<String> directoryIndex;
	private final File documentRoot;
	private final boolean directoryListing;

	// documentRoot is directory relative to root directory, or null
	public DefaultHandler(ArrayList<String> directoryIndex, boolean directoryListing,
			File documentRoot) throws IOException {
		this.directoryIndex = directoryIndex;
		this.directoryListing = directoryListing;
		if (documentRoot == null) {
			this.documentRoot = HTTPServer.INSTANCE.getRoot();
		}
		else {
			if (!documentRoot.isDirectory())
				throw new IOException(documentRoot.getPath() + " is not a directory");
			this.documentRoot = documentRoot;
		}
	}

	@Override
	public void process(Request req, Response resp) throws IOException {
		if (resp.statusCode == StatusCode._UNKNOWN) {
			if (req.method == RequestMethod.GET) {
				File reqFile = new File(documentRoot, req.getRelativeURIPathStr());
				if (!reqFile.exists()) {
					resp.statusCode = StatusCode.NOT_FOUND;
					resp.send();
					return;
				}

				if (reqFile.isDirectory()) {
					File indexFile = getDirectoryIndex(reqFile);
					if (indexFile == null) {
						if (directoryListing) {
							DirectoryListing dl = new DirectoryListing(reqFile, req.uriPath);
							resp.send(dl.getBytes(), MediaType.HTML.toString());
						}
						else {
							resp.statusCode = StatusCode.NOT_FOUND;
							resp.send();
						}
					}
					else {
						resp.send(indexFile);
					}
				}
				else {
					resp.send(reqFile);
				}
			}

		}
		else {
			resp.send();
		}
	}

	// returns null if none found
	private File getDirectoryIndex(File dir) {
		for (String name : directoryIndex) {
			File index = new File(dir, name);
			if (index.isFile())
				return index;
		}
		return null;
	}
}
