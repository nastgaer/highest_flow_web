// $Id: Response.java,v 1.3 2001/01/23 03:07:54 nconway Exp $
package disruptor.http;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Date;

public class Response {
	public final OutputStream rawOut;
	public final OutputStreamWriter out;

	private final Socket socket;
	public int responseCode;

	public Response(Request request) throws IOException {
		socket = request.getSocket();
		rawOut = socket.getOutputStream();
		out = new OutputStreamWriter(new BufferedOutputStream(rawOut, 4096), "ASCII");
	}

	public void sendStatus(int code) throws IOException {
		responseCode = code;
		final StringBuffer status = new StringBuffer(16);
		status.append("HTTP/1.1 ");
		status.append(code);
		status.append(" ");
		status.append(HTTP.getStatusStr(code));
		status.append("\r\n");
		out.write(status.toString());
	}

	public void finishHeaders() throws IOException {
		out.write("\r\n");
		out.flush();
	}

	public void sendHeaderEntry(String header) throws IOException {
		out.write(header);
		if (!header.endsWith("\r\n")) {
			out.write("\r\n");
		}
	}

	public void finishResponse() throws IOException {
		out.flush();
		socket.close();
	}

	/**
	 * Sends the HTTP headers to the client that are always sent. These headers
	 * are those used in common by all HTTP responses.
	 */
	public void sendBasicHeaders() throws IOException {
		sendHeaderEntry("Date: " + HTTP.formatDate(new Date()));
		sendHeaderEntry("Server: " + HttpConfiguration.getVersion());
		sendHeaderEntry("Connection: close");
	}

	/**
	 * Sends the headers about the URI the client asked for. This method does
	 * the "legwork" when dealing with files: it takes the URI from the client,
	 * uses {@link #translateURI(String)} to get a <code>File</code>, checks for
	 * errors, and then sends the relevant headers to the client about the
	 * specified URI. It is used by both the HTTP GET and HEAD methods.
	 */
	public void startHeader(Request request) {
		try {
			sendStatus(HTTP.OK);
			sendBasicHeaders();
			sendHeaderEntry("Content-Length: " + request.getRequestFile().length());
			sendHeaderEntry("Last-Modified: " + HTTP.formatDate(new Date(request.getRequestFile().lastModified())));
			sendHeaderEntry("Content-Type: " + request.getMimeType());

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
