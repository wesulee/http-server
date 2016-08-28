package http_server;

import java.io.IOException;

// prints Hello world
public class TestHandler implements RequestHandler {

	private static final String content = "<!DOCTYPE html>\n" +
		"<html>\n" +
		"<head>\n" +
		"<title>Hello World HTML</title>\n" +
		"</head>\n" +
		"<body>\n" +
		"<h1>Hello World</h1>\n" +
		"</body>\n" +
		"</html>";

	@Override
	public void process(Request req, Response resp) throws IOException {
		if (resp.statusCode == StatusCode._UNKNOWN) {
			if (req.method == RequestMethod.GET) {
				resp.statusCode = StatusCode.OK;
				resp.sendHTML(content);
			}
		}
		else {
			resp.send();
		}
	}
}
