package http_server.html;

import java.util.ArrayList;

public class TableRow implements Element {

	public ArrayList<TableColumn> columns = new ArrayList<TableColumn>(4);

	public TableRow add(TableColumn col) {
		columns.add(col);
		return this;
	}

	@Override
	public void getHTML(StringBuilder sb) {
		sb.append("<tr>");
		for (TableColumn col : columns)
			col.getHTML(sb);
		sb.append("</tr>");
	}
}
