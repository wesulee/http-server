package http_server.html;

import java.util.ArrayList;

public class Form implements Element {

	private String method;
	private String action;
	private ArrayList<Element> elements = new ArrayList<Element>();

	public Form setMethod(String s) {
		method = s;
		return this;
	}

	public Form setAction(String s) {
		action = s;
		return this;
	}

	public Form add(Element e) {
		elements.add(e);
		return this;
	}

	@Override
	public void getHTML(StringBuilder sb) {
		sb.append("<form");
		if (method != null)
			sb.append(" method=\"").append(method).append('\"');
		if (action != null)
			sb.append(" action=\"").append(action).append('\"');
		sb.append('>');
		for (Element e : elements)
			e.getHTML(sb);
		sb.append("</form>");
	}
}
