package disruptor.http;

import disruptor.http.handler.RequestHandlerFactory;
import org.apache.log4j.lf5.LogLevel;

import java.io.File;
import java.util.HashMap;

public class HttpConfiguration {

    /** Mapping of the registered request handlers */
    private static HashMap<String, RequestHandlerFactory> RequestHandler = new HashMap<>();

    /**
     * Registers a request handler which implemets the
     * <code>RequestHandlerInterface</code> The pattern is used to determin
     * which request will be handled by this handler.
     */
    public static void registerRequestHandler(String pattern, RequestHandlerFactory interf) {
        getRequestHandler().put(pattern, interf);
    }

    public static HashMap<String, RequestHandlerFactory> getRequestHandler() {
        return RequestHandler;
    }

    public static final String getVersion() {
        return "1.0";
    }

    public static final File getErrorLog() {
        return new File("error.log");
    }

    /** Returns the <code>File</code> to use for access logging. */
    public static final File getAccessLog() {
        return new File("access.log");
    }

    public static final int getLogLevel() {
        return 2;
    }

    public static final File getMimeTypes() {
        return new File(getDocumentRoot() + File.separator + "conf/mime.types");
    }

    public static MIMEDictionary getMime() {
        if (mime == null) {
            mime = new MIMEDictionary(getMimeTypes());
        }
        return mime;
    }

    /** Looks up the MIME type for a specified file extension. */
    public static MIMEDictionary mime;

    private static String documentRoot = "./";

    /** Returns the root of the HTTP virtual filesystem. */
    public static final String getDocumentRoot() {
        return documentRoot;
    }

    public static void setDocumentRoot(String dir) {
        documentRoot = dir;
    }
}
