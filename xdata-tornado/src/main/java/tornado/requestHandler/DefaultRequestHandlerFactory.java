package tornado.requestHandler;

import org.apache.commons.lang3.text.WordUtils;

import tornado.Request;
import tornado.Response;

import java.net.URI;

public class DefaultRequestHandlerFactory implements RequestHandlerFactory {

	@Override
	public RequestHandlerInterface getInstance(Request req, Response resp) {
		try {
			URI uri = new URI(req.getURI());

			String[] dirPath = uri.getPath().split("/");
			if (dirPath.length > 1) {
				String lastName = dirPath[dirPath.length - 1];

				lastName = WordUtils.capitalizeFully(lastName);

				String className = "tornado.requestHandler." + lastName + "RequestHandler";
				Class<?> rclass = Class.forName(className);
				Object classInstance = rclass.getDeclaredConstructor(new Class[] {
				        Request.class, Response.class
                }).newInstance(req, resp);

				if (classInstance instanceof RequestHandlerInterface) {
					return (RequestHandlerInterface) classInstance;
				}
			}

			return null;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

}
