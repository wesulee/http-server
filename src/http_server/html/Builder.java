package http_server.html;

import java.util.ArrayList;
import java.util.Map;

// A very basic HTML generator
public class Builder {

	private String title;
	private ArrayList<Element> headElements = new ArrayList<Element>();
	private ArrayList<Element> bodyElements = new ArrayList<Element>();

	public void setTitle(String s) {
		title = s;
	}

	public void add(Element e) {
		bodyElements.add(e);
	}

	public void addH(Element e) {
		headElements.add(e);
	}

	public String getHTML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html>\n<html><head>");
		sb.append("<title>").append(title).append("</title>");
		for (Element e : headElements)
			e.getHTML(sb);
		sb.append("</head><body>");
		for (Element e : bodyElements)
			e.getHTML(sb);
		sb.append("</body></html>");
		return sb.toString();
	}

	// utility function
	public static void genTagAttributes(StringBuilder sb, Map<String, String> map) {
		for (Map.Entry<String, String> entry : map.entrySet()) {
			sb.append(' ');
			sb.append(entry.getKey());
			sb.append("=\"");
			sb.append(entry.getValue());
			sb.append('\"');
		}
		sb.append('>');
	}

	// utility function
	public static String emptyNull(String s) {
		if (s == null)
			return "";
		else
			return s;
	}
}
