package http_server.html;

public class SubmitButton implements Element {

	private String value;

	public SubmitButton(String value) {
		this.value = value;
	}

	@Override
	public void getHTML(StringBuilder sb) {
		sb.append("<input type=\"submit\" value=\"");
		sb.append(value);
		sb.append("\">");
	}
}
