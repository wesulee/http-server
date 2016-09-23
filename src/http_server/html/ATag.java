package http_server.html;

public class ATag implements Element {

	private String href;
	private Element contents;

	public ATag setHref(String href) {
		this.href = href;
		return this;
	}

	public ATag setContents(Element contents) {
		this.contents = contents;
		return this;
	}

	@Override
	public void getHTML(StringBuilder sb) {
		sb.append("<a href=\"").append(href).append("\">");
		contents.getHTML(sb);
		sb.append("</a>");
	}
}
