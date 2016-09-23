package http_server;

public enum PostContentType {
	_NONE, _INVALID, URLEncoded, MultipartFormData;

	public static PostContentType parse(String s) {
		if ((s == null) || s.isEmpty())
			return _NONE;
		else if (s.equals("application/x-www-form-urlencoded"))
			return URLEncoded;
		else if (s.equals("multipart/form-data"))
			return MultipartFormData;
		else
			return _INVALID;
	}
}
