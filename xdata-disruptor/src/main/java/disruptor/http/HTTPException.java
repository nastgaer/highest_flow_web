// $Id: HTTPException.java,v 1.5 2001/01/11 05:16:27 nconway Exp $
package disruptor.http;

public class HTTPException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 4635061012494228393L;
    private final int statusCode;

    /**
     * Constructs a new exception with the specified error code. Numeric error
     * codes are specified by the HTTP standard.
     */
    public HTTPException(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Returns the error code of this exception.
     */
    public int getCode() {
        return statusCode;
    }

    /**
     * Returns the HTML error page that should be shown to the HTTP client.
     */
    public String getErrorPage() {
        final StringBuffer page = new StringBuffer(120);
        page.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\"");
        page.append("<HTML><BODY><H1 ALIGN=CENTER>Error ");
        page.append(statusCode);
        page.append("</H1></BODY></HTML>");
        return page.toString();
    }

}
