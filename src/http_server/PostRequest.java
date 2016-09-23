package http_server;

import java.io.IOException;
import java.util.LinkedHashMap;

public class PostRequest {

	public final Request req;
	public PostContentType contentType;
	public LinkedHashMap<String, String> fields;	// URLEncoded parameters

	PostRequest(Request req) throws HTTPException, IOException {
		this.req = req;
		this.contentType = PostContentType.parse(
			req.headerFields.get(RequestHeaderField.CONTENT_TYPE)
		);
		if ((contentType == PostContentType._NONE) || (contentType == PostContentType._INVALID)) {
			req.resp.statusCode = StatusCode.BAD_REQUEST;
			return;
		}
		if (contentType == PostContentType.URLEncoded)
			this.fields = new LinkedHashMap<String, String>();
		else
			this.fields = null;
		if (req.contentLength < 0) {
			req.resp.statusCode = StatusCode.BAD_REQUEST;
			return;
		}
		// parse body
		if (contentType == PostContentType.URLEncoded) {
			int contentLengthInt = (int) req.contentLength;
			String body = Utility.read(req.conn.in, contentLengthInt);
			Utility.parseURLEncodedStr(body, fields);
		}
		else if (contentType == PostContentType.MultipartFormData) {
			// TODO not implemented
			req.resp.statusCode = StatusCode.NOT_IMPLEMENTED;
		}
		else {
			// nothing to parse
		}
	}
}
