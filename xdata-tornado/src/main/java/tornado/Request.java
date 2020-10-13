// $Id: Request.java,v 1.11 2001/01/23 18:43:06 nconway Exp $
package tornado;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Socket;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class Request {
	private final static int BUFFER_SIZE = 4096;
	private final static int HEADER_SIZE = 8;
	private final static int HEADER_LENGTH = 20;

	private final Socket socket;
	private final InputStreamReader input;
	private final HashMap<String, String> headers = new HashMap<String, String>(HEADER_SIZE);

	private float protocolVersion;
	private String requestURI;
	private String requestType;
	private String rawRequest;
	private Map<String, String> queryPairs = new HashMap<>();

	/** The file asked for by the client, if appropriate. */
	private final File requestFile;
	private String requestFileExtension;
	private final String contentType;
	private String mimeType;

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public Request(Socket client) throws IOException, HTTPException {
		socket = client;
		final BufferedInputStream in = new BufferedInputStream(socket.getInputStream(), BUFFER_SIZE);
		input = new InputStreamReader(in, "ASCII");

		readHeaders();

		requestFile = translateURI();
		final String filename = requestFile.getName();
		final int lastPeriod = filename.lastIndexOf('.');
		if (lastPeriod == -1) {
			// if there was no extension, use an empty string
			requestFileExtension = "";
		} else {
			requestFileExtension = filename.substring(lastPeriod + 1, filename.length());
		}
		if (lastPeriod == -1) {
			// if there was no extension, use an empty string
			requestFileExtension = "";
		} else {
			requestFileExtension = filename.substring(lastPeriod + 1, filename.length());
		}
		contentType = Tornado.mime.getContentType(requestFileExtension);
	}

	private void readHeaders() throws IOException {
		// read the request line
		processRequest(readLine(input, true));

		// read the rest of the headers
		while (true) {
			final String header = readLine(input, false);
			if (header.equals("")) {
				break;
			} else {
				processHeader(header);
			}
		}
	}

	/**
	 * Translates the URI to a filename. This takes an <b>absolute</b> URI,
	 * performs some security checks, and translates this URI into the
	 * designated file on the local filesystem. It then returns this file.
	 */
	private File translateURI() throws HTTPException {
		final String relURI = getURI().substring(getURI().indexOf('/', 7));
		if (getURI().indexOf("..", 1) != -1) {
			throw new HTTPException(HTTP.NOT_FOUND);
		}
		return new File(Tornado.getConfig().getDocumentRoot() + relURI);
	}

	private String readLine(Reader r, boolean multiLine) throws IOException {
		final StringBuffer buffer = new StringBuffer(HEADER_LENGTH);
		while (true) {
			final char c = (char) r.read();

			if (c == '\r') {
				continue;
			} else if (c == '\n') {
				if (buffer.length() == 0 && multiLine) {
					continue;
				} else {
					return buffer.toString();
				}
			} else if (c == -1) {
				return buffer.toString();
			} else {
				buffer.append(c);
			}
		}
	}

	private void processRequest(String request) {
		rawRequest = request;

		final StringTokenizer st = new StringTokenizer(request);
		formatType(st);
		formatURI(st);
		formatQuery();
		formatProtocolVersion(st);
	}

	private void formatType(StringTokenizer st) {
		requestType = st.nextToken().toUpperCase();
	}

	private void formatURI(StringTokenizer st) {
		String uri = null;
		try {
			uri = st.nextToken(); // URLDecoder.decode(st.nextToken());
		} catch (final Exception e) {
			e.printStackTrace();
		}

		if (!uri.startsWith("http://")) {
			uri = "http://localhost" + uri;
		}
        requestURI = uri;
	}

	private void formatQuery() {
	    try {
            URI uri = new URI(requestURI);
            String query = uri.getQuery();

            queryPairs.clear();
            final String[] pairs = query.split("&");
            for (String pair : pairs) {
                final int idx = pair.indexOf("=");
                final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                if (!queryPairs.containsKey(key)) {
                    queryPairs.put(key, "");
                }
                final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
                queryPairs.put(key, value);
            }

        } catch (Exception ex) {
	        ex.printStackTrace();
        }

    }

	private void formatProtocolVersion(StringTokenizer st) {
		/*
		 * Be careful: we need to handle legacy clients who do not send an HTTP
		 * version.
		 */
		String str;
		try {
			str = st.nextToken();
		} catch (final NoSuchElementException e) {
			str = "";
		}

		if (str.endsWith("1.1")) {
			protocolVersion = (float) 1.1;
		} else if (str.endsWith("1.0")) {
			protocolVersion = (float) 1.0;
		} else {
			protocolVersion = (float) 0.9; // default to 0.9
		}
	}

	private void processHeader(String header) {
		final int split = header.indexOf(':', 3);
		if (split == -1)
			return; // ignore malformed headers

		final String key = header.substring(0, split);
		final String val = header.substring(split);
		headers.put(key, val);
	}

	public String getLogName() {
		return null;
	}

	public String getHostAddress() {
		return socket.getInetAddress().getHostAddress();
	}

	public int getPort() {
		return socket.getPort();
	}

	public float getProtocolVersion() {
		return protocolVersion;
	}

	public String getURI() {
		return requestURI;
	}

	public Map<String, String> getQueryPairs() {
	    return queryPairs;
    }

	public String getType() {
		return requestType;
	}

	// will never include CRLF
	public String getRawRequest() {
		return rawRequest;
	}

	public String getHeader(String key) {
		return headers.get(key);
	}

	public Socket getSocket() {
		return socket;
	}

	public File getRequestFile() {
		return requestFile;
	}

	public String getRequestFileExtension() {
		return requestFileExtension;
	}

	public String getContentType() {
		return contentType;
	}

	public String getMimeType() {
		if ((mimeType == null) || (mimeType == "")) {
			if (getRequestFile().isDirectory()) {
				mimeType = Tornado.mime.getContentType("html");
			} else {
				mimeType = getContentType();

				if (mimeType.compareTo("application/x-httpd-php") == 0) {
					mimeType = "text/html";
				}
			}
		}
		return mimeType;
	}
}
