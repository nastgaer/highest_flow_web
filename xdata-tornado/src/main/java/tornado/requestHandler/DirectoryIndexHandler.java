package tornado.requestHandler;

import java.io.File;
import java.io.IOException;

import tornado.Request;
import tornado.Response;
import tornado.Tornado;

public class DirectoryIndexHandler extends RequestHandlerInterface {

	public DirectoryIndexHandler(Request req, Response resp) {
		super(req, resp);
	}

	@Override
	public void handleRequest() {
		try {
			response.startHeader(request);
			response.finishHeaders();

			response.out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");

			response.out.write("<html><head><title>" + request.getRequestFile().getName() + "</title></head><body>");

			final String path = request.getRequestFile().getAbsolutePath()
					.substring(Tornado.getConfig().getDocumentRoot().length());

			response.out.write("<h2>Index of " + request.getRequestFile().getName() + "</h2>");
			if (path != "")
				response.out.write("<a href='" + path + "/../'>..</a> <br/>\n");

			for (final File item : request.getRequestFile().listFiles()) {
				response.out.write("<a href='" + path + "/" + item.getName() + "'>" + item.getName() + "</a> <br/>\n");
			}
			response.out.write("</body></html>");
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
