package http_server;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class HandlerMatcher {

	private HandlerNode root = null;

	public void add(String context, RequestHandler handler) throws RuntimeException {
		if (root == null) {
			if (context != "/") {
				throw new RuntimeException("first handler's context must be '/'");
			}
			root = new HandlerNode("", handler);
		}
		else {
			ArrayList<String> path = Utility.splitPath(context);
			insert(path, handler);
		}
	}

	public RequestHandler match(Request req) {
		HandlerNode node = root;
		for (int i = 0; i < req.uriPath.size(); ++i) {
			if (node.children == null)
				break;
			int j;
			boolean found = false;
			for (j = 0; j < node.children.size(); ++j) {
				int cmp = req.uriPath.get(i).compareTo(node.children.get(j).name);
				if (cmp == 0) {
					node = node.children.get(j);
					found = true;
					break;
				}
				else if (cmp > 0) {
					break;
				}
			}
			if (!found)
				break;
		}
		return node.handler;
	}

	private void insert(ArrayList<String> path, RequestHandler h) {
		HandlerNode node = root;
		for (int pathIndex = 0; pathIndex < path.size(); ++pathIndex) {
			String name = path.get(pathIndex);
			if (node.children == null)
				node.children = new ArrayList<HandlerNode>();
			int i;
			int cmp = -1;	// default value
			for (i = 0; i < node.children.size(); ++i) {
				cmp = name.compareTo(node.children.get(i).name);
				if (cmp >= 0)
					break;
			}
			if (pathIndex == path.size()-1) {
				if (cmp == 0)
					node.children.get(i).handler = h;
				else
					node.children.add(i, new HandlerNode(name, h));
			}
			else {
				if (cmp != 0) {
					// extend the currently matched node to the new node
					node.children.add(i, new HandlerNode(name, node.handler));
				}
			}
			node = node.children.get(i);
		}
	}

	private void printTree() {
		printTreeNode(System.out, root, new Stack<String>());
	}

	// depth-first
	private void printTreeNode(PrintStream stream, HandlerNode node, Stack<String> context) {
		stream.print(String.join("", Collections.nCopies(context.size(), "\t")));
		stream.print("context:");
		stream.print(String.join("/", context));
		stream.print("/ ");
		stream.println(node);
		if (node.children != null) {
			context.push(node.name);
			for (HandlerNode n : node.children) {
				printTreeNode(stream, n, context);
			}
			context.pop();
		}

	}

	private class HandlerNode {

		public final String name;
		public RequestHandler handler;
		public ArrayList<HandlerNode> children;

		public HandlerNode(String name, RequestHandler handler) {
			this.name = name;
			this.handler = handler;
			this.children = null;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("name:" + name);
			sb.append(" handler:" + handler);
			sb.append(" children:");
			if (children == null)
				sb.append("null");
			else {
				sb.append(children.get(0).name);
				for (int i = 1; i < children.size(); ++i)
					sb.append("," + children.get(i).name);
			}
			return sb.toString();
		}
	}
}
