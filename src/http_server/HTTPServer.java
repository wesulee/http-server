package http_server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum HTTPServer {

	INSTANCE;

	public static final String HEADER_FIELD_SERVER = "java";
	public static final int URI_LIMIT = 10000;	// max length of a URI in request header
	private File root;
	private int port;
	private ServerSocket ss;
	private final HandlerMatcher matcher = new HandlerMatcher();
	private final Logger logger = Logger.getLogger(HTTPServer.class.getName());
	private static boolean running = false;

	public void init(String root, int port) throws IOException {
		File rootDir = new File(root);
		logger.info("root directory: " + rootDir.getAbsolutePath());
		if (!rootDir.isDirectory()) {
			logger.log(Level.SEVERE, rootDir + " is an invalid directory");
		}
		this.root = rootDir;
		this.port = port;
		this.ss = new ServerSocket(port);

		Utility.init();
	}

	public void addHandler(String context, RequestHandler handler) throws RuntimeException {
		if (!running) {
			matcher.add(context, handler);
		}
		else {
			throw new RuntimeException("cannot add handler to running server");
		}
	}

	public void run() {
		{	// print HandlerMatcher tree
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(bos);
			matcher.printTree(ps);
			ps.close();
			logger.info(bos.toString());
		}

		ExecutorService executor = Executors.newCachedThreadPool();
		running = true;
		while (true) {
			try {
				Socket conn = ss.accept();
				Request request = new Request(conn);
				ServerWorker worker = new ServerWorker(request);
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

	public File getRoot() {
		return root;
	}

	public boolean validFileLocation(File f) {
		return false;
	}

	public static void main(String[] args) {
		try {
			HTTPServer.INSTANCE.init(".", 80);
			String defaultContent = "<!DOCTYPE html>\n" +
				"<html>\n" +
				"<body>\n" +
				"<h1>Hello World</h1>\n" +
				"</body>\n" +
				"</html>";
			HTTPServer.INSTANCE.addHandler(
				"/",
				new StaticHandler(defaultContent.getBytes(Utility.charsetUTF8), MediaType.HTML.toString())
			);
			HTTPServer.INSTANCE.addHandler(
				"/static",
				new DefaultHandler(
					Utility.defaultDirectoryIndex,
					true,
					new File(HTTPServer.INSTANCE.getRoot(), "static")
				)
			);
		}
		catch (Exception e) {
			System.err.println("unable to start server: " + e);
			return;
		}
		HTTPServer.INSTANCE.run();
	}
}
