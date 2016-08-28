package http_server;

public enum ResponseHeaderField {

	CONTENT_LENGTH("Content-Length"), CONTENT_TYPE("Content-Type"), DATE("Date"),
	LAST_MODIFIED("Last-Modified"), LOCATION("Location"), SERVER("Server"),
	STATUS("Status");

	private final String str;

	private ResponseHeaderField(String str) {
		this.str = str;
	}

	@Override
	public String toString() {
		return str;
	}
}
