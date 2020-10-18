package disruptor.http.handler;

import disruptor.http.Request;
import disruptor.http.Response;

public interface RequestHandlerFactory {

	abstract RequestHandlerInterface getInstance(Request req, Response resp);
}
