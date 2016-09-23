package http_server;

import java.io.IOException;

import http_server.html.*;

public class TestPostHandler implements RequestHandler {

	@Override
	public void process(Request req) throws HTTPException, IOException {
		if ((req.resp.statusCode == StatusCode._UNKNOWN) && (req.method == RequestMethod.POST))
			req.parseBody(); // read POST content
		if (req.resp.statusCode == StatusCode._UNKNOWN) {
			Builder html = new Builder();
			html.setTitle("Test POST");
			Heading postStatus = new Heading(3);
			if (req.method == RequestMethod.POST)
				postStatus.setContents("POST: yes");
			else
				postStatus.setContents("POST: no");
			html.add(postStatus);
			Form form = new Form();
			html.add(form);
			form.setMethod("post");
			TextInput param1 = new TextInput("param1");
			TextInput param2 = new TextInput("param2");
			form.add(new Text("param1: ")).add(param1).add(new BrTag());
			form.add(new Text("param2: ")).add(param2).add(new BrTag());
			form.add(new SubmitButton("Submit"));
			if (req.method == RequestMethod.POST) {
				param1.setValue(Utility.htmlEscape(Builder.emptyNull(req.post.fields.get("param1"))));
				param2.setValue(Utility.htmlEscape(Builder.emptyNull(req.post.fields.get("param2"))));
			}
			// send page
			req.resp.send(html);
		}
		else {
			req.resp.send();
		}
	}
}
