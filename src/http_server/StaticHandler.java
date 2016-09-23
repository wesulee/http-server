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
	public void process(Request req) throws IOException {
		if (req.resp.statusCode == StatusCode._UNKNOWN) {
			if (Utility.methodGetOrHead(req.method)) {
				req.resp.statusCode = StatusCode.OK;
				req.resp.send(content, contentType);
			}
		}
		else {
			req.resp.send();
		}
	}
}
