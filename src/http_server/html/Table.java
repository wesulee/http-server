package http_server.html;

import java.util.ArrayList;

public class Table implements Element {

	ArrayList<TableRow> rows = new ArrayList<TableRow>();

	public Table add(TableRow row) {
		rows.add(row);
		return this;
	}

	@Override
	public void getHTML(StringBuilder sb) {
		sb.append("<table>");
		for (TableRow row : rows)
			row.getHTML(sb);
		sb.append("</table>");
	}
}
