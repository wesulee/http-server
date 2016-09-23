package http_server.html;

public class Heading implements Element {

	private String contents;
	private final int n;

	public Heading(int n) {
		this.n = n;
	}

	public void setContents(String s) {
		contents = s;
	}

	@Override
	public void getHTML(StringBuilder sb) {
		String nStr = Integer.toString(n);
		sb.append("<h").append(nStr).append('>');
		sb.append(Builder.emptyNull(contents));
		sb.append("</h").append(nStr).append('>');
	}
}
