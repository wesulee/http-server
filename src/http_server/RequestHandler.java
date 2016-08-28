package http_server;

import java.io.IOException;

public interface RequestHandler {
	public void process(Request req, Response resp) throws IOException;
}
