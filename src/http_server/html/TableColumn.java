package http_server.html;

import java.util.HashMap;

public class TableColumn implements Element {

	private final HashMap<String, String> attr = new HashMap<String, String>(0);
	private Element contents = null;
	private final boolean header;

	public TableColumn() {
		this.header = false;
	}

	public TableColumn(boolean header) {
		this.header = header;
	}

	void setContents(Element e) {
		contents = e;
	}

	@Override
	public void getHTML(StringBuilder sb) {
		if (header)
			sb.append("<th");
		else
			sb.append("<td");
		Builder.genTagAttributes(sb, attr);
		if (contents != null)
			contents.getHTML(sb);
		if (header)
			sb.append("</th>");
		else
			sb.append("</td>");
	}
}
