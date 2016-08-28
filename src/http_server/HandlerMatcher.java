package http_server;

import java.util.ArrayList;

public class HandlerMatcher {

	private HandlerNode root = null;

	public void add(String context, RequestHandler handler) throws RuntimeException {
		if (root == null) {
			if (context != "/") {
				throw new RuntimeException("first handler's context must be '/'");
			}
			root = new HandlerNode(new ArrayList<String>(), handler);
		}
		else {
			// TODO
		}
	}

	public RequestHandler match(Request request) {
		// TODO not implemented
		return root.handler;
	}

	private class HandlerNode {

		public ArrayList<String> context;
		public RequestHandler handler;
		public ArrayList<HandlerNode> next;

		public HandlerNode(ArrayList<String> context, RequestHandler handler) {
			this.context = context;
			this.handler = handler;
			this.next = null;
		}
	}
}
