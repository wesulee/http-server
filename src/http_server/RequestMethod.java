package http_server;

public enum RequestMethod {

	INVALID("?"), GET("GET"), HEAD("HEAD"), POST("POST");

	private final String str;

	RequestMethod(String str) {
		this.str = str;
	}

	@Override
	public String toString() {
		return str;
	}
}
