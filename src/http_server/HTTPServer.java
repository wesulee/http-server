package http_server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTTPServer {

	public static final String HEADER_FIELD_SERVER = "java";
	public static final int URI_LIMIT = 10000;	// max length of a URI in request header
	private final String root;
	private final int port;
	private final ServerSocket ss;
	private final HandlerMatcher matcher = new HandlerMatcher();
	private final Logger logger;
	private boolean running = false;

	public HTTPServer(String root, int port) throws IOException {
		logger = Logger.getLogger(HTTPServer.class.getName());

		File rootDir = new File(root);
		if (!rootDir.isDirectory()) {
			logger.log(Level.SEVERE, rootDir + " is an invalid directory");
		}
		this.root = root;
		this.port = port;
		this.ss = new ServerSocket(port);

		Utility.init();
	}

	public void addHandler(String context, TestHandler handler) throws RuntimeException {
		if (!running) {
			matcher.add(context, handler);
		}
		else {
			throw new RuntimeException("cannot add handler to running server");
		}
	}

	public void run() {

		ExecutorService executor = Executors.newCachedThreadPool();
		running = true;
		while (true) {
			try {
				Socket conn = ss.accept();
				Request request = new Request(conn);
				ServerWorker worker = new ServerWorker(this, request);
				executor.submit(worker);
			}
			catch (IOException e) {
				logger.log(Level.WARNING, "unable to accept new connection", e);
			}
			catch (Exception e) {
				logger.log(Level.WARNING, "unhandled exception", e);
			}
		}
	}

	public RequestHandler getHandler(Request request) {
		return matcher.match(request);
	}

	public Logger getLogger() {
		return logger;
	}

	public boolean validFileLocation(File f) {
		return false;
	}

	public static void main(String[] args) {
		HTTPServer server = null;
		try {
			server = new HTTPServer(".", 80);
			server.addHandler("/", new TestHandler());
		}
		catch (Exception e) {
			System.err.println("unable to start server: " + e);
		}
		if (server != null)
			server.run();
	}
}
