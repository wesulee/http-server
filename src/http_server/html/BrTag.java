package http_server.html;

public class BrTag implements Element {

	@Override
	public void getHTML(StringBuilder sb) {
		sb.append("<br>");
	}
}
