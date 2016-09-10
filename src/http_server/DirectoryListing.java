package http_server;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class DirectoryListing {

	public static byte[] getBytes(File dir, ArrayList<String> uriPath) {
		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html>");
		html.append("<html>");
		html.append("<head>");
		html.append("<title>");
		html.append("Index of " + getDisplayURI(uriPath));
		html.append("</title>");
		html.append("</head>");
		html.append("<body>");
		html.append("<h1>");
		html.append("Index of " + getDisplayURI(uriPath));
		html.append("</h1>");
		html.append("<table>");
		// table header
		html.append("<tr>");
		html.append("<th>Type</th>");
		html.append("<th>Name</th>");
		html.append("<th>Last Modified</th>");
		html.append("<th>Size</th>");
		html.append("</tr>");
		html.append("<tr><th colspan=\"4\"><hr></th></tr>");
		// table body
		if (!uriPath.isEmpty()) {
			html.append("<tr><td colspan=\"4\">");
			@SuppressWarnings("unchecked")
			ArrayList<String> parentPath = (ArrayList<String>) uriPath.clone();
			parentPath.remove(parentPath.size()-1);
			Utility.makeHTMLATag(html, Utility.getRootURI(parentPath, true), "Parent Directory");
			html.append("</tr></tr>");
		}
		for (File f : dir.listFiles()) {
			String name = f.getName();
			final boolean isFile = f.isFile();
			if (name.isEmpty())
				continue;
			if (isFile)
				html.append("<tr><td>File</td>");
			else
				html.append("<tr><td>Directory</td>");
			html.append("<td><a href=\"");
			html.append(Utility.urlEncode(name));
			html.append("\">");
			html.append(Utility.htmlEscape(name));
			html.append("</a></td><td>");
			html.append(Utility.htmlEscape(Utility.dirListingFormatDate(new Date(f.lastModified()))));
			html.append("</td><td>");
			if (isFile)
				html.append(f.length());
			else
				html.append('-');
			html.append("</td></tr>");
		}
		html.append("<tr><th colspan=\"4\"><hr></th></tr>");
		html.append("</table>");
		html.append("</body>");
		html.append("</html>");
		return html.toString().getBytes(Utility.charsetUTF8);
	}

	private static String getDisplayURI(ArrayList<String> uriPath) {
		return "/" + String.join("/", uriPath);
	}
}
