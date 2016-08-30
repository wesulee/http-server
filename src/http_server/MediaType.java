package http_server;

public enum MediaType {

	HTML("text", "html");

	private final String type;
	private final String subtype;

	private MediaType(String type, String subtype) {
		this.type = type;
		this.subtype = subtype;
	}

	@Override
	public String toString() {
		return (type + '/' + subtype);
	}
}
