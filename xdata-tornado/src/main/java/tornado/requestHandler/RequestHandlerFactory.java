package tornado.requestHandler;

import tornado.Request;
import tornado.Response;

public interface RequestHandlerFactory {

	abstract RequestHandlerInterface getInstance(Request req, Response resp);
}
