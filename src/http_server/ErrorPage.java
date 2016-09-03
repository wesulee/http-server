package http_server;

import java.io.IOException;

public class ErrorPage {
	public static void send(Request req, Response resp) throws IOException {
		switch (resp.statusCode) {
		case BAD_REQUEST:
		case NOT_FOUND:
		case INTERNAL_SERVER_ERROR:
			resp.send(generate(resp.statusCode), MediaType.HTML.toString());
			break;
		default:
			throw new RuntimeException("invalid status code: " + resp.statusCode);
		}
	}

	private static byte[] generate(StatusCode code) {
		String str = new String(
			"<!DOCTYPE html>\n" +
			"<html>\n" +
			"<head>\n" +
			"<title>" + code.toString() + "</title>\n" +
			"</head>\n" +
			"<body>\n" +
			"<h1>" + code.getPhrase() + "</h1>\n" +
			"</body>\n" +
			"</html>\n"
		);
		return str.getBytes(Utility.charsetUTF8);
	}
}
