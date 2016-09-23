package http_server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Connection {

	public final Socket sock;
	public InputStream in = null;
	public OutputStream out = null;
	public final Request req;

	public Connection(Socket sock) {
		this.sock = sock;
		this.req = new Request(this);
	}

	public void close() {
		try {
			sock.close();
		}
		catch (IOException e) {
			// ignore
		}
	}
}
