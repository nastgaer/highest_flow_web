package tornado.requestHandler;

import java.io.FileInputStream;
import java.io.IOException;

import tornado.HTTPException;
import tornado.Request;
import tornado.Response;

public class DefaultRequestHandler extends RequestHandlerInterface {

	public DefaultRequestHandler(Request req, Response resp) {
		super(req, resp);
	}

	@Override
	public void handleRequest() throws HTTPException {

		requestedFileIsReadable();

		try {
			response.startHeader(request);
			response.finishHeaders();

			final FileInputStream file = new FileInputStream(request.getRequestFile());
			/* Read and send file in blocks. */
			final byte[] fileData = new byte[READ_BLOCK];
			for (int i = 0; i < request.getRequestFile().length(); i += READ_BLOCK) {
				final int bytesRead = file.read(fileData);
				response.rawOut.write(fileData, 0, bytesRead);
			}
			file.close();

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
