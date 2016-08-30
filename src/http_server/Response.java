package http_server;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

public class Response {

	private final Request req;
	public StatusCode statusCode = StatusCode._UNKNOWN;
	public HashMap<ResponseHeaderField, String> headerFields;

	public Response(Request req) {
		this.req = req;
		this.headerFields = Utility.getDefaultResponseHeaderField();
	}

	private void sendHeader() throws IOException {
		sendHeaderStatusLine();
		// set date
		headerFields.put(ResponseHeaderField.DATE, getCurrentHeaderDate());
		for (Entry<ResponseHeaderField, String> entry : headerFields.entrySet()) {
			StringBuilder sb = new StringBuilder();
			sb.append(entry.getKey());
			sb.append(": ");
			sb.append(entry.getValue());
			String str = sb.toString();
			req.out.write(str.getBytes(Utility.charsetASCII));
			req.out.write(Utility.byteNewline);
		}
		req.out.write(Utility.byteNewline);
	}

	private void sendHeaderStatusLine() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/1.0 ");
		sb.append(statusCode);
		String str = sb.toString();
		req.out.write(str.getBytes(Utility.charsetASCII));
		req.out.write(Utility.byteNewline);
	}

	public void setContentType(String type) {
		headerFields.put(ResponseHeaderField.CONTENT_TYPE, type);
	}

	public void setContentLength(int length) {
		headerFields.put(ResponseHeaderField.CONTENT_LENGTH, String.valueOf(length));
	}

	public void send(byte[] content, String contentType) throws IOException {
		setContentType(contentType);
		setContentLength(content.length);
		sendHeader();
		req.out.write(content);
	}

	public void send() throws IOException {
		sendHeader();
	}

	private static String getCurrentHeaderDate() {
		Date d = new Date();
		return d.toString();
	}
}
