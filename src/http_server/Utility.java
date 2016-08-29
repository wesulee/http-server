package http_server;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

public class Utility {

	public static HashMap<String, RequestMethod> requestMethodMap;
	public static HashMap<String, RequestHeaderField> requestHeaderFieldMap;
	private static HashMap<ResponseHeaderField, String> defaultResponseHeaderField;
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

	public static void init() {
		requestMethodMap = new HashMap<String, RequestMethod>();
		requestMethodMap.put("GET", RequestMethod.GET);
		requestMethodMap.put("HEAD", RequestMethod.HEAD);
		requestMethodMap.put("POST", RequestMethod.POST);
		requestHeaderFieldMap = new HashMap<String, RequestHeaderField>();
		requestHeaderFieldMap.put("connection", RequestHeaderField.CONNECTION);
		requestHeaderFieldMap.put("cookie", RequestHeaderField.COOKIE);
		requestHeaderFieldMap.put("content-length", RequestHeaderField.CONTENT_LENGTH);
		requestHeaderFieldMap.put("date", RequestHeaderField.DATE);
		requestHeaderFieldMap.put("host", RequestHeaderField.HOST);
		requestHeaderFieldMap.put("referer", RequestHeaderField.REFERER);
		requestHeaderFieldMap.put("user-agent", RequestHeaderField.USER_AGENT);
		defaultResponseHeaderField = new HashMap<ResponseHeaderField, String>();
		defaultResponseHeaderField.put(ResponseHeaderField.SERVER, HTTPServer.HEADER_FIELD_SERVER);
		charsetASCII = Charset.forName("US-ASCII");
		charsetUTF8 = Charset.forName("UTF-8");
		String newline = "\r\n";
		byteNewline = newline.getBytes(charsetASCII);
	}
}
