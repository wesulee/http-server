package http_server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.logging.Level;

public class ServerWorker implements Runnable {

	private final HTTPServer server;
	private final Request req;
	private final Response resp;

	public ServerWorker(HTTPServer server, Request req) {
		this.server = server;
		this.req = req;
		this.resp = new Response(req);
	}

	@Override
	public void run() {
		try {
			req.out = new BufferedOutputStream(req.conn.getOutputStream());
			req.in = new BufferedInputStream(req.conn.getInputStream());
		}
		catch (IOException e) {
			server.getLogger().log(Level.WARNING, "unable to get socket stream", e);
			req.close();
			return;
		}
		// parse request
		try {
			parseHeader();
		} catch (RequestHeaderException e) {
			server.getLogger().log(Level.WARNING, "invalid request header", e);
			// if response status code has not been set, assume Bad Request
			if (resp.statusCode == StatusCode._UNKNOWN) {
				resp.statusCode = StatusCode.BAD_REQUEST;
			}
			// send
			req.close();
			return;
		}
		catch (IOException e) {
			server.getLogger().log(Level.WARNING, "error reading input stream", e);
			req.close();
			return;
		}
		// match to a handler
		try {
			server.getHandler(req).process(req, resp);
			req.out.flush();
		}
		catch (IOException e) {
			server.getLogger().log(Level.WARNING, "error sending response", e);
		}
		req.close();
	}

	private void parseHeader() throws RequestHeaderException, IOException {
		String line = readLine(req.in);
		int i, j;
		// parse request method
		i = line.indexOf(' ');
		if (i == -1)
			throw new RequestHeaderException("invalid request");
		String requestMethodStr = line.substring(0, i);
		RequestMethod requestMethod = Utility.requestMethodMap.get(requestMethodStr);
		if (requestMethod == null) {
			throw new RequestHeaderException("invalid method: " + requestMethodStr);
		} else {
			req.method = requestMethod;
		}
		// parse URI
		j = line.indexOf(' ', i+1);
		if (j == -1) {
			throw new RequestHeaderException("unable to get URI");
		}
		req.URI = line.substring(i+1, j);
		if (req.URI.isEmpty()) {
			throw new RequestHeaderException("unable to get URI");
		}
		if (req.URI.length() > HTTPServer.URI_LIMIT) {
			resp.statusCode = StatusCode.INTERNAL_SERVER_ERROR;
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
	}

	private void parseURI() throws RequestHeaderException {
		if (req.URI.charAt(0) != '/') {
			throw new RequestHeaderException("invalid URI");
		}
		URI uri = null;
		try {
			uri = new URI("http", req.URI, null);
		}
		catch (URISyntaxException e) {
			throw new RequestHeaderException("invalid URI: " + e);
		}
		// generate uriPath
		String uriPath = uri.getPath();
		req.uriPath = Utility.splitPath(uriPath);
		// parse query
		parseURIQuery(uri);
	}

	private void parseURIQuery(URI uri) {
		int i, j;
		String uriQuery = uri.getQuery();
		if (uriQuery == null)
			return;
		i = 0;
		// [i, j) index of name=value pair
		String[] pair;
		while (i < uriQuery.length()) {
			j = uriQuery.indexOf('&', i);
			if (j == -1)
				j = uriQuery.length();
			String pairStr = uriQuery.substring(i, j);
			pair = parseURIQueryPair(pairStr);
			i = j + 1;
			// decode
			try {
				pair[0] = URLDecoder.decode(pair[0], "UTF-8");
				if (pair[1] != null) {
					pair[1] = URLDecoder.decode(pair[1], "UTF-8");
				}
			}
			catch (UnsupportedEncodingException e) {
				server.getLogger().log(Level.SEVERE, "unable to decode request URI", e);
			}
			req.queryParam.put(pair[0], pair[1]);
		}
	}

	// ret[1] is null if no value or empty value
	private String[] parseURIQueryPair(String str) {
		String[] ret = new String[2];
		int i = str.indexOf('=');
		if (i == -1) {
			ret[0] = str;
			ret[1] = null;
		}
		else {
			ret[0] = str.substring(0, i);
			if (i == str.length()-1) {
				// = is last character
				ret[1] = null;
			}
			else {
				ret[1] = str.substring(i+1);
			}
		}
		return ret;
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
		req.versionMajor = major;
		req.versionMinor = minor;
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
			String line = readLine(req.in);
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
			req.headerFields.put(fieldName, value);
		}
	}

	// reads from in until end of line (returned String excludes newline).
	// if \r is not followed by \n, throws RequestHeaderException
	// if input ends before newline, throws RequestHeaderException
	private static String readLine(InputStream in) throws RequestHeaderException, IOException {
		StringBuilder input = new StringBuilder();
		int c;
		while (true) {
			c = in.read();
			if (c == -1)
				throw new RequestHeaderException("unexpected end of input");
			if (c == '\r') {
				// make sure next character is \n
				c = in.read();
				if (c == -1)
					throw new RequestHeaderException("unexpected end of input");
				else if (c != '\n')
					throw new RequestHeaderException("invalid newline");
				else
					break;
			}
			input.append((char) c);
		}
		return input.toString();
	}
}
