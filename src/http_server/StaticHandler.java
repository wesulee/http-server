package http_server;

import java.io.IOException;

public class StaticHandler implements RequestHandler {

	private final byte[] content;
	private final String contentType;

	public StaticHandler(byte[] content, String contentType) {
		this.content = content;
		this.contentType = contentType;
	}

	@Override
	public void process(Request req, Response resp) throws IOException {
		if (resp.statusCode == StatusCode._UNKNOWN) {
			if (Utility.methodGetOrHead(req.method)) {
				resp.statusCode = StatusCode.OK;
				resp.send(content, contentType);
			}
		}
		else {
			resp.send();
		}
	}
}
