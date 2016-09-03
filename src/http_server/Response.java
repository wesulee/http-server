package http_server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;

public class Response {

	private static final int FILE_BUFFER_SZ = 8*1024;
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

	public void setContentType(File f) {
		setContentType(Utility.MIMEType.get(f));
	}

	public void setContentLength(long length) {
		headerFields.put(ResponseHeaderField.CONTENT_LENGTH, String.valueOf(length));
	}

	public void send(byte[] content, String contentType) throws IOException {
		setContentType(contentType);
		setContentLength(content.length);
		sendHeader();
		req.out.write(content);
	}

	public void send() throws IOException {
		if (statusCode.getCode()/100 >= 4) {
			ErrorPage.send(req, this);
		}
		else {
			sendHeader();
		}
	}

	public void send(File f) {
		HTTPServer.INSTANCE.getLogger().log(Level.INFO, "serving file " + f.getAbsolutePath());
		if (statusCode != StatusCode.OK)
			statusCode = StatusCode.OK;
		setContentType(f);
		setContentLength(f.length());
		try (
			FileInputStream fin = new FileInputStream(f);
			BufferedInputStream bin = new BufferedInputStream(fin);
			) {
			byte[] bytes = new byte[FILE_BUFFER_SZ];
			int n = bin.read(bytes, 0, FILE_BUFFER_SZ);
			while (n != -1) {
				req.out.write(bytes, 0, n);
				n = bin.read(bytes, 0, FILE_BUFFER_SZ);
			}
		}
		catch (IOException e) {
			HTTPServer.INSTANCE.getLogger().log(Level.WARNING, "unable to send file", e);
		}
	}

	private static String getCurrentHeaderDate() {
		Date d = new Date();
		return d.toString();
	}
}
