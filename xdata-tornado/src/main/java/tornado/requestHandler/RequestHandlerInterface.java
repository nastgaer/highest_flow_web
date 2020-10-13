/**
 * 
 */
package tornado.requestHandler;

import java.io.IOException;

import tornado.HTTP;
import tornado.HTTPException;
import tornado.Request;
import tornado.Response;

/**
 * @author Michael Sprauer
 * 
 */
public abstract class RequestHandlerInterface {

	protected final static int READ_BLOCK = 8192;
	// per-client resources: these are recreated for every connection
	public Request request;
	public Response response;

	public RequestHandlerInterface(Request req, Response resp) {
		request = req;
		response = resp;
	}

	public abstract void handleRequest() throws HTTPException, IOException;

	/***
	 * Makes sure the requested file is available and readable. If not it
	 * HTTPException.
	 * 
	 * @throws HTTPException
	 */
	protected void requestedFileIsReadable() throws HTTPException {
		if (request.getRequestFile().exists() == false)
			throw new HTTPException(HTTP.NOT_FOUND);

		if (request.getRequestFile().canRead() == false)
			throw new HTTPException(HTTP.FORBIDDEN);
	}

}
