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
	public void process(Request req) throws IOException {
		if (req.resp.statusCode == StatusCode._UNKNOWN) {
			if (Utility.methodGetOrHead(req.method)) {
				File reqFile = new File(documentRoot, req.getRelativeURIPathStr());
				if (!reqFile.exists()) {
					req.resp.statusCode = StatusCode.NOT_FOUND;
					req.resp.send();
					return;
				}

				if (reqFile.isDirectory()) {
					File indexFile = getDirectoryIndex(reqFile);
					if (indexFile == null) {
						if (directoryListing) {
							// check if redirect needed
							if (req.URI.charAt(req.URI.length()-1) != '/') {
								// redirect to same page URI, now with trailing slash
								req.resp.redirectPerm(Utility.getAbsURI(req.uriPath, true));
							}
							else {
								req.resp.send(
									DirectoryListing.getBytes(reqFile, req.uriPath),
									MediaType.HTML.toString()
								);
							}
						}
						else {
							req.resp.statusCode = StatusCode.NOT_FOUND;
							req.resp.send();
						}
					}
					else {
						req.resp.send(indexFile);
					}
				}
				else {
					req.resp.send(reqFile);
				}
			}
		}
		else {
			req.resp.send();
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
