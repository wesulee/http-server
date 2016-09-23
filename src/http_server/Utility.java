package http_server;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Utility {

	public static HashMap<String, RequestMethod> requestMethodMap;
	public static HashMap<String, RequestHeaderField> requestHeaderFieldMap;
	private static HashMap<ResponseHeaderField, String> defaultResponseHeaderField;
	public static ArrayList<String> defaultDirectoryIndex;
	public static MediaTypeMap MIMEType;
	private static SimpleDateFormat dfDirListing;
	public static Charset charsetASCII;
	public static Charset charsetUTF8;
	public static byte[] byteNewline;

	@SuppressWarnings("unchecked")
	public static HashMap<ResponseHeaderField, String> getDefaultResponseHeaderField() {
		return (HashMap<ResponseHeaderField, String>) defaultResponseHeaderField.clone();
	}

	public static ArrayList<String> splitPath(String path) {
		ArrayList<String> list = new ArrayList<String>();
		int j;
		for (int i = 0; i < path.length(); i = j+1) {
			j = path.indexOf('/', i);
			if (j == -1)
				j = path.length();
			String file = path.substring(i, j);
			if (!file.isEmpty())
				list.add(file);
		}
		return list;
	}

	public static String htmlEscape(String str) {
		StringBuilder esc = new StringBuilder();
		for (int i = 0; i < str.length(); ++i) {
			final char c = str.charAt(i);
			switch (c) {
			case '<':
				esc.append("&lt;");
				break;
			case '>':
				esc.append("&gt;");
				break;
			case '&':
				esc.append("&amp;");
				break;
			case '"':
				esc.append("&quot;");
				break;
			default:
				esc.append(c);
			}
		}
		return esc.toString();
	}

	public static String dirListingFormatDate(Date d) {
		return dfDirListing.format(d);
	}

	public static String urlEncode(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// should not happen since init() was called before this
			return null;
		}
	}

	public static String urlDecode(String str) {
		try {
			return URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	// Decode a 'application/x-www-form-urlencoded' encoded string
	//   and put key/value pairs into map.
	public static void parseURLEncodedStr(String str, Map<String, String> map) {
		if (str == null)
			return;
		int i, j;
		i = 0;
		// [i, j) index of name=value pair
		String[] pair;
		while (i < str.length()) {
			j = str.indexOf('&', i);
			if (j == -1)
				j = str.length();
			String pairStr = str.substring(i, j);
			pair = parseURLEncodedStrPair(pairStr);
			i = j + 1;
			// decode
			pair[0] = urlDecode(pair[0]);
			if (pair[1] != null) {
				pair[1] = urlDecode(pair[1]);
			}
			map.put(pair[0], pair[1]);
		}
	}

	// helper for parseURLEncodedStr
	// ret[1] is null if no value or empty value
	private static String[] parseURLEncodedStrPair(String str) {
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

	// href should be encoded
	public static void makeHTMLATag(StringBuilder sb, String href) {
		sb.append("<a href=\"");
		sb.append(href);
		sb.append("\">");
		sb.append(urlDecode(href));
		sb.append("</a>");
	}

	// href should be encoded, value should be escaped
	public static void makeHTMLATag(StringBuilder sb, String href, String value) {
		sb.append("<a href=\"");
		sb.append(href);
		sb.append("\">");
		sb.append(value);
		sb.append("</a>");
	}

	public static String getAbsURI(ArrayList<String> path, boolean trailingSlash) {
		StringBuilder uri = new StringBuilder();
		uri.append("http://");
		uri.append(HTTPServer.INSTANCE.getServerName());
		if (HTTPServer.INSTANCE.getPort() != 80) {
			uri.append(':');
			uri.append(HTTPServer.INSTANCE.getPort());
		}
		for (String name : path) {
			uri.append('/');
			uri.append(urlEncode(name));
		}
		if (trailingSlash)
			uri.append('/');
		return uri.toString();
	}

	// make a URI relative to root
	// if path is empty, returns "/"
	public static String getRootURI(ArrayList<String> path, boolean trailingSlash) {
		if (path.isEmpty())
			return "/";
		StringBuilder uri = new StringBuilder();
		for (String name : path) {
			uri.append('/');
			uri.append(urlEncode(name));
		}
		if (trailingSlash)
			uri.append('/');
		return uri.toString();
	}

	public static boolean methodGetOrHead(RequestMethod method) {
		return ((method == RequestMethod.GET) || (method == RequestMethod.HEAD));
	}

	// reads from in until end of line (returned String excludes newline).
	// if \r is not followed by \n, throws RequestHeaderException
	// if input ends before newline, throws RequestHeaderException
	public static String readLine(InputStream in) throws IOException, RequestHeaderException {
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

	public static String read(InputStream in, int numBytes) throws IOException, HTTPException {
		StringBuilder input = new StringBuilder(numBytes);
		int c;
		for (int i = 0; i < numBytes; ++i) {
			c = in.read();
			if (c == -1)
				throw new RequestHeaderException("unexpected end of input");
			input.append((char) c);
		}
		return input.toString();
	}

	public static void init() throws UnsupportedEncodingException {
		requestMethodMap = new HashMap<String, RequestMethod>();
		requestMethodMap.put("GET", RequestMethod.GET);
		requestMethodMap.put("HEAD", RequestMethod.HEAD);
		requestMethodMap.put("POST", RequestMethod.POST);
		requestHeaderFieldMap = new HashMap<String, RequestHeaderField>();
		requestHeaderFieldMap.put("connection", RequestHeaderField.CONNECTION);
		requestHeaderFieldMap.put("cookie", RequestHeaderField.COOKIE);
		requestHeaderFieldMap.put("content-length", RequestHeaderField.CONTENT_LENGTH);
		requestHeaderFieldMap.put("content-type", RequestHeaderField.CONTENT_TYPE);
		requestHeaderFieldMap.put("date", RequestHeaderField.DATE);
		requestHeaderFieldMap.put("host", RequestHeaderField.HOST);
		requestHeaderFieldMap.put("referer", RequestHeaderField.REFERER);
		requestHeaderFieldMap.put("user-agent", RequestHeaderField.USER_AGENT);
		defaultResponseHeaderField = new HashMap<ResponseHeaderField, String>();
		defaultResponseHeaderField.put(ResponseHeaderField.SERVER, HTTPServer.HEADER_FIELD_SERVER);
		defaultDirectoryIndex = new ArrayList<String>();
		defaultDirectoryIndex.add("index.html");
		MIMEType = new MediaTypeMap("application/octet-stream");
		dfDirListing = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		charsetASCII = Charset.forName("US-ASCII");
		charsetUTF8 = Charset.forName("UTF-8");
		String newline = "\r\n";
		byteNewline = newline.getBytes(charsetASCII);
		// test whether URLEncoder/URLDecoder supports UTF-8
		URLEncoder.encode("foo", "UTF-8");
		URLDecoder.decode("foo", "UTF-8");
	}
}
