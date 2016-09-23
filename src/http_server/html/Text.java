package http_server.html;

public class Text implements Element {

	private String text;
	private boolean bold;

	public Text() {
		this.bold = false;
	}

	public Text(String text) {
		this.text = text;
		this.bold = false;
	}

	public Text setContents(String s) {
		text = s;
		return this;
	}

	public Text setBold() {
		bold = true;
		return this;
	}

	@Override
	public void getHTML(StringBuilder sb) {
		if (bold) {
			sb.append("<b>").append(Builder.emptyNull(text)).append("</b>");
		}
		else {
			sb.append(text);
		}
	}
}
