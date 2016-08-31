package http_server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Request {

	public final Socket conn;
	public InputStream in = null;
	public OutputStream out = null;
	public RequestMethod method = RequestMethod.INVALID;
	public String URI = null;	// request-URI
	public ArrayList<String> uriPath = null;
	public int uriPathIndex = -1;
	public final HashMap<String, String> queryParam;
	public final HashMap<RequestHeaderField, String> headerFields;
	public int versionMajor = 0;
	public int versionMinor = 0;

	public Request(Socket conn) {
		this.conn = conn;
		this.queryParam = new HashMap<String, String>();
		this.headerFields = new HashMap<RequestHeaderField, String>();
	}

	public void close() {
		try {
			conn.close();
		}
		catch (IOException e) {
			// ignore
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
