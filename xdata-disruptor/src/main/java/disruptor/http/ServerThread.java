// $Id: ServerThread.java,v 1.42 2001/01/23 03:08:28 nconway Exp $
package disruptor.http;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import disruptor.http.handler.*;

public class ServerThread {

	/** The message in the access log for this request. */
	private CommonLogMessage accessLog;

	// per-server resources: these are created once per ServerThread
	private RequestHandlerInterface requestHandler;
	private Request request;
	private Response response;

	public void run(Socket socket) {
		// start the HTTP transaction with the client
		try {
			handOffRequest(socket);
		} catch (final HTTPException e) {
			// we got a protocol error of some kind - expected
			sendErrorPage(e);
		} catch (final Exception e) {
			// we got a more serious error - these should not occur
			e.printStackTrace();
		} finally {
//				Tornado.logger.logAccess(accessLog);
			finishConnection();
		}
	}

	/**
	 * Decides what to do with a connection. This looks at the HTTP headers sent
	 * by the client, sends error pages as necessary, and then decides which
	 * method to use to actually handle this request.
	 * 
	 * @throws IOException
	 *             , HTTPException
	 */
	private void handOffRequest(Socket socket) throws HTTPException, IOException {
		request = new Request(socket);
		response = new Response(request);
		accessLog = new CommonLogMessage(request);

		requestHandler = findRequestHandler(request.getType());

		if (requestHandler != null) {
			requestHandler.handleRequest();
			accessLog.setStatusCode(requestHandler.response.responseCode);
		}

		response.finishResponse();
	}

	/***
	 * This method gets called to find the best request handler. Additional
	 * request handlers can be injected using
	 * <code>Tornado.registerRequestHandler</code>
	 * 
	 * @param method
	 *            the used method: GET/POST/PUT/HEAD, usw.
	 * @param request
	 * @param response
	 * @return Returns the appropriate request handler or null if request is
	 *         allready handeld or don't need to be handled
	 * @throws HTTPException
	 * @throws IOException
	 */
	private RequestHandlerInterface findRequestHandler(String method) throws HTTPException, IOException {

		if (method == null) {
			return null;
		} else if ((method.equals("GET")) || (method.equals("POST"))) {

			if (request.getRequestFile().isDirectory()) {
				return new DirectoryIndexHandler(request, response);
			}

			for (final Map.Entry<String, RequestHandlerFactory> entry : HttpConfiguration.getRequestHandler().entrySet()) {
				final String ext = request.getRequestFileExtension();
				final String handler = entry.getKey();
				if (ext.compareTo(handler) == 0) {
					return entry.getValue().getInstance(request, response);
				}
			}

			if (request.getRequestFile().exists())
				return new DefaultRequestHandler(request, response);
			else
			{
				DefaultRequestHandlerFactory defaultRequestHandlerFactory = new DefaultRequestHandlerFactory();
				RequestHandlerInterface requestHandler = defaultRequestHandlerFactory.getInstance(request, response);
				if (requestHandler == null) {
					throw new HTTPException(HTTP.NOT_FOUND);

				} else {
					return requestHandler;
				}
			}

		} else if (method.equals("HEAD")) {
			response.startHeader(request);
			response.finishHeaders();
		} else if (method.equals("PUT")) {
			throw new HTTPException(HTTP.NOT_IMPLEMENTED);
		} else if (method.equals("OPTIONS")) {
			throw new HTTPException(HTTP.NOT_IMPLEMENTED);
		} else if (method.equals("DELETE")) {
			throw new HTTPException(HTTP.NOT_IMPLEMENTED);
		} else if (method.equals("TRACE")) {
			handleTraceRequest();
		} else if (method.equals("CONNECT")) {
			throw new HTTPException(HTTP.NOT_IMPLEMENTED);
		} else {
			throw new HTTPException(HTTP.NOT_IMPLEMENTED);
		}
		return null;
	}

	/** Handles an HTTP TRACE request from the client. */
	private void handleTraceRequest() throws HTTPException {
		try {
			response.sendStatus(HTTP.OK);
			response.sendBasicHeaders();
			response.sendHeaderEntry("Content-Type: message/http");
			response.finishHeaders();
			// echo the client's request back to them
			response.out.write(request.getRawRequest());
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/** Concludes the work with the client and informs the ServerPool. */
	private void finishConnection() {

	}

	/** Sends the specified HTTP error code page to the client. */
	private void sendErrorPage(HTTPException httpE) {
		try {
			response.sendStatus(httpE.getCode());
			response.sendBasicHeaders();
			response.sendHeaderEntry("Content-Type: text/html");
			response.finishHeaders();
			response.out.write(httpE.getErrorPage());
			response.finishResponse();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
