// $Id: HTTP.java,v 1.10 2001/01/25 02:17:09 nconway Exp $
package disruptor.http;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class HTTP {
	private static SimpleDateFormat dateMaker = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
	private static TimeZone gmt = TimeZone.getTimeZone("GMT");

	public static int CONTINUE = 100;
	public static int OK = 200;
	public static int CREATED = 201;
	public static int ACCEPTED = 202;
	public static int NO_CONTENT = 204;
	public static int RESET_CONTENT = 205;
	public static int PARTIAL_CONTENT = 206;
	public static int MULTIPLE_CHOICES = 300;
	public static int MOVED = 301;
	public static int FOUND = 302;
	public static int SEE_OTHER = 303;
	public static int NOT_MODIFIED = 304;
	public static int USE_PROXY = 305;
	public static int TEMP_REDIRECT = 306;
	public static int BAD_REQUEST = 400;
	public static int UNAUTH = 401;
	public static int FORBIDDEN = 403;
	public static int NOT_FOUND = 404;
	public static int METHOD_NOT_ALLOWED = 405;
	public static int NOT_ACCEPTABLE = 406;
	public static int TIMEOUT = 408;
	public static int CONFLICT = 409;
	public static int GONE = 410;
	public static int LENGTH_REQUIRED = 411;
	public static int PRECONDITION_FAILED = 412;
	public static int REQUEST_TOO_LARGE = 413;
	public static int URI_TOO_LONG = 414;
	public static int BAD_MEDIA_TYPE = 415;
	public static int EXPECTATION_FAILED = 417;
	public static int SERVER_ERROR = 500;
	public static int NOT_IMPLEMENTED = 501;
	public static int UNAVAILABLE = 503;
	public static int BAD_HTTP_VERSION = 505;

	public static String STATUS_100 = "Continue";
	public static String STATUS_200 = "OK";
	public static String STATUS_201 = "Created";
	public static String STATUS_202 = "Accepted";
	public static String STATUS_203 = "No Content";
	public static String STATUS_204 = "Reset Content";
	public static String STATUS_206 = "Partial Content";
	public static String STATUS_300 = "Multiple Choices";
	public static String STATUS_301 = "Moved";
	public static String STATUS_302 = "Found";
	public static String STATUS_303 = "See Other";
	public static String STATUS_304 = "Not Modified";
	public static String STATUS_305 = "Use Proxy";
	public static String STATUS_306 = "Temporary Redirect";
	public static String STATUS_400 = "Bad Request";
	public static String STATUS_401 = "Unauthorized";
	public static String STATUS_403 = "Forbidden";
	public static String STATUS_404 = "Not Found";
	public static String STATUS_405 = "Method Not Allowed";
	public static String STATUS_406 = "Not Acceptable";
	public static String STATUS_408 = "Request Timeout";
	public static String STATUS_409 = "Conflict";
	public static String STATUS_410 = "Gone";
	public static String STATUS_411 = "Length Required";
	public static String STATUS_412 = "Precondition Failed";
	public static String STATUS_413 = "Request Entity Too Large";
	public static String STATUS_414 = "Request-URI Too Long";
	public static String STATUS_415 = "Unsupported Media Type";
	public static String STATUS_417 = "Expectation Failed";
	public static String STATUS_500 = "Internal Server Error";
	public static String STATUS_501 = "Not Implemented";
	public static String STATUS_503 = "Unavailable";
	public static String STATUS_505 = "HTTP Version Not Supported";

	/**
	 * Returns the <code>String</code> form of the specified statusCode. In
	 * order to do this, it uses reflection to return the value of one the
	 * appropriate STATUS_xxx constants.
	 */
	public static String getStatusStr(int statusCode) {
		final String fieldName = "STATUS_" + statusCode;
		try {
			final String statusStr = (String) Class.forName("disruptor.http.HTTP").getField(fieldName).get(null);
			return statusStr;
		} catch (final NoSuchFieldException e) {
			throw new IllegalArgumentException("Invalid status code: " + statusCode);
		} catch (final Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static String formatDate(Date date) {
		// bad, don't call this every time
		dateMaker.setTimeZone(gmt);
		return dateMaker.format(date);
	}
}
