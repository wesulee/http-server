package http_server.html;

public class TextInput implements Element {

	private String name;
	private String value;

	public TextInput(String name) {
		this.name = name;
	}

	public void setValue(String s) {
		value = s;
	}

	@Override
	public void getHTML(StringBuilder sb) {
		sb.append("<input type=\"text\" name=\"").append(name).append('\"');
		if (value != null)
			sb.append(" value=\"").append(value).append('\"');
		sb.append('>');
	}
}
