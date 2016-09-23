package http_server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.logging.Level;

public class ServerWorker implements Runnable {

	private final Connection conn;

	public ServerWorker(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void run() {
		try {
			conn.out = new BufferedOutputStream(conn.sock.getOutputStream());
			conn.in = new BufferedInputStream(conn.sock.getInputStream());
		}
		catch (IOException e) {
			HTTPServer.INSTANCE.getLogger().log(Level.WARNING, "unable to get socket stream", e);
			conn.close();
			return;
		}
		// parse request
		try {
			parseHeader();
		}
		catch (RequestHeaderException e) {
			HTTPServer.INSTANCE.getLogger().log(Level.WARNING, "invalid request header", e);
			// if response status code has not been set, assume Bad Request
			if (conn.req.resp.statusCode == StatusCode._UNKNOWN) {
				conn.req.resp.statusCode = StatusCode.BAD_REQUEST;
				try {
					conn.req.resp.send();
				} catch (IOException ex) {
					// ignore
					System.out.println("unable to send");
				}
			}
			conn.close();
			return;
		}
		catch (IOException e) {
			HTTPServer.INSTANCE.getLogger().log(Level.WARNING, "error reading input stream", e);
			conn.close();
			return;
		}
		// match to a handler
		try {
			HTTPServer.INSTANCE.getHandler(conn.req).process(conn.req);
			conn.out.flush();
		}
		catch (HTTPException e) {
			HTTPServer.INSTANCE.getLogger().log(Level.WARNING, "error sending response", e);
		}
		catch (IOException e) {
			HTTPServer.INSTANCE.getLogger().log(Level.WARNING, "error sending response", e);
		}
		conn.close();
	}

	private void parseHeader() throws RequestHeaderException, IOException {
		String line = Utility.readLine(conn.in);
		int i, j;
		// parse request method
		i = line.indexOf(' ');
		if (i == -1)
			throw new RequestHeaderException("invalid request");
		String requestMethodStr = line.substring(0, i);
		RequestMethod requestMethod = Utility.requestMethodMap.get(requestMethodStr);
		if (requestMethod == null)
			throw new RequestHeaderException("invalid method: " + requestMethodStr);
		else
			conn.req.method = requestMethod;
		// parse URI
		j = line.indexOf(' ', i+1);
		if (j == -1) {
			throw new RequestHeaderException("unable to get URI");
		}
		conn.req.URI = line.substring(i+1, j);
		if (conn.req.URI.isEmpty()) {
			throw new RequestHeaderException("unable to get URI");
		}
		if (conn.req.URI.length() > HTTPServer.URI_LIMIT) {
			conn.req.resp.statusCode = StatusCode.INTERNAL_SERVER_ERROR;
			throw new RequestHeaderException("uri exceeds maximum allowed length");
		}
		parseURI();
		// get version
		if (j+1 >= line.length()) {
			// missing version
		}
		else {
			parseVersion(line.substring(j+1));
		}
		// parse fields
		parseHeaderFields();
		// set contentLength if provided
		String contentLengthStr = conn.req.headerFields.get(RequestHeaderField.CONTENT_LENGTH);
		if (contentLengthStr != null) {
			try {
				conn.req.contentLength = Long.parseUnsignedLong(contentLengthStr);
			}
			catch (NumberFormatException e) {
				throw new RequestHeaderException("invalid Content-Length value");
			}
		}
	}

	private void parseURI() throws RequestHeaderException {
		if (conn.req.URI.charAt(0) != '/') {
			throw new RequestHeaderException("invalid URI");
		}
		URI uri = null;
		try {
			uri = new URI("http", conn.req.URI, null);
		}
		catch (URISyntaxException e) {
			throw new RequestHeaderException("invalid URI: " + e);
		}
		// generate uriPath
		String uriPath = uri.getPath();
		String decodedURIPath = Utility.urlDecode(uriPath);
		conn.req.uriPath = Utility.splitPath(decodedURIPath);
		// parse query
		String uriQuery = uri.getQuery();
		Utility.parseURLEncodedStr(uriQuery, conn.req.queryParam);
	}

	private void parseVersion(String str) throws RequestHeaderException {
		// str should look like "HTTP/X.X"
		int major;
		int minor;
		if (str.length() != 8) {
			// invalid version string
			throw new RequestHeaderException("invalid http version: " + str);
		}
		final String pre = "HTTP/";
		int i;
		for (i = 0; i < pre.length(); ++i) {
			if (pre.charAt(i) != str.charAt(i))
				throw new RequestHeaderException("invalid http version: " + str);
		}
		if (str.charAt(i+1) != '.')
			throw new RequestHeaderException("invalid http version: " + str);
		try {
			major = Integer.parseInt(str.substring(i, i+1));
			minor = Integer.parseInt(str.substring(i+2, i+3));
		}
		catch (NumberFormatException e) {
			throw new RequestHeaderException("invalid http version: " + str);
		}
		conn.req.versionMajor = major;
		conn.req.versionMinor = minor;
	}

	// https://tools.ietf.org/html/rfc7230#section-3.2
	// Each header field consists of a case-insensitive field name followed
	//   by a colon (":"), optional leading whitespace, the field value, and
	//   optional trailing whitespace.
	private void parseHeaderFields() throws RequestHeaderException, IOException {
		// read remainder of header
		String key = null;
		String value = null;
		while (true) {
			String line = Utility.readLine(conn.in);
			if (line.isEmpty())
				break;	// end of header
			if ((line.charAt(0) == ' ') || (line.charAt(0) == '\t')) {
				// continuation of previous value
				if ((key == null) || (value == null)) {
					throw new RequestHeaderException("unexpected whitespace");
				}
				value = value + line.substring(1);
			}
			else {
				if (key == null) {
					// first call to readLine()
					// do nothing
				}
				else {
					// insert current field
					parseHeaderFieldsInsert(key, value);
				}
				// parse new field
				int i = line.indexOf(':');
				int j = line.length()-1;
				if (i == -1)
					throw new RequestHeaderException("invalid header field");
				key = line.substring(0, i);
				// find first non-whitespace
				++i;
				while ((i < line.length()) && ((line.charAt(i) == ' ') || (line.charAt(i) == '\t')))
					++i;
				if (i >= line.length())
					throw new RequestHeaderException("invalid header field");
				while ((j > i) && ((line.charAt(i) == ' ') || (line.charAt(i) == '\t')))
					--j;
				value = line.substring(i, j+1);
			}
			if (key != null)
				parseHeaderFieldsInsert(key, value);
		}
	}

	// ignore unknown fields
	private void parseHeaderFieldsInsert(String key, String value) {
		key = key.toLowerCase(Locale.ENGLISH);
		RequestHeaderField fieldName = Utility.requestHeaderFieldMap.get(key);
		if (fieldName != null) {
			conn.req.headerFields.put(fieldName, value);
		}
	}
}
