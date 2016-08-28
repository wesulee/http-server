package http_server;

public enum StatusCode {

	_UNKNOWN(0, "???"), OK(200, "OK"), MOVED_PERMANENTLY(301, "Moved Permanently"),
	MOVED_TEMPORARILY(302, "Moved Temporarily"), NOT_MODIFIED(304, "Not Modified"),
	BAD_REQUEST(400, "Bad Request"), NOT_FOUND(404, "Not Found"),
	INTERNAL_SERVER_ERROR(500, "Internal Server Error"), NOT_IMPLEMENTED(501, "Not Implemented");

	private final String phrase;
	private final int code;

	private StatusCode(int code, String phrase) {
		this.phrase = phrase;
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public String getPhrase() {
		return phrase;
	}

	@Override
	public String toString() {
		return (code + " " + phrase);
	}
}
