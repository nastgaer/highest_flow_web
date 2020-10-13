// $Id: ListenThread.java,v 1.3 2001/01/17 01:47:12 nconway Exp $
package tornado;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ListenThread extends Thread {
	private final ServerPool serverPool;
	private final int port;
	private ServerSocket serverSocket;

	/** Constructs a new thread with the specified values. */
	public ListenThread(ServerPool serverPool, int port) {
		this.serverPool = serverPool;
		this.port = port;
	}

	/**
	 * Begins an infinite loop, accepting and dispatching incoming connections.
	 */
	@Override
	public void run() {
		bindToPort();
		while (true) {
			listen();
		}
	}

	/**
	 * Connects Tornado to a local system port. If an error is encountered,
	 * Tornado aborts.
	 */
	private void bindToPort() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (final IOException e) {
			// treat all I/O errors as fatal
			throw new RuntimeException(e.getMessage());
		}
	}

	/** Waits for, accepts and dispatches a single incoming connection. */
	private void listen() {
		try {
			final Socket connection = serverSocket.accept();
			serverPool.dispatch(connection);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
