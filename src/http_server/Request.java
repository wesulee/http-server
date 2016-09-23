package http_server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Request {

	public final Connection conn;
	public final Response resp;
	public RequestMethod method = RequestMethod.INVALID;
	public String URI = null;	// request-URI
	public ArrayList<String> uriPath = null;
	public int uriPathIndex = -1;
	public final HashMap<String, String> queryParam;
	public final HashMap<RequestHeaderField, String> headerFields;
	public PostRequest post = null;
	public long contentLength = -1;
	public int versionMajor = 0;
	public int versionMinor = 0;

	public Request(Connection conn) {
		this.conn = conn;
		this.resp = new Response(this);
		this.queryParam = new HashMap<String, String>();
		this.headerFields = new HashMap<RequestHeaderField, String>();
	}

	void parseBody() throws HTTPException, IOException {
		if (method == RequestMethod.POST) {
			post = new PostRequest(this);
		}
	}

	public ArrayList<String> getRelativeURIPath() {
		if (uriPathIndex >= uriPath.size())
			return new ArrayList<String>(0);
		else
			return new ArrayList<String>(uriPath.subList(uriPathIndex, uriPath.size()));
	}

	public String getRelativeURIPathStr() {
		ArrayList<String> relPath = getRelativeURIPath();
		if (relPath.isEmpty())
			return "";
		else
			return String.join(File.separator, relPath);
	}
}
