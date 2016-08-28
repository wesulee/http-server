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
	public final ArrayList<String> uriPath;
	public final HashMap<String, String> queryParam;
	public final HashMap<RequestHeaderField, String> headerFields;
	public int versionMajor = 0;
	public int versionMinor = 0;

	public Request(Socket conn) {
		this.conn = conn;
		this.uriPath = new ArrayList<String>();
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
}
