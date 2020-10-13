// $Id: ServerPool.java,v 1.18 2001/01/17 23:27:10 nconway Exp $
package tornado;

import java.net.Socket;
import java.util.ArrayList;

public class ServerPool {
	private final ArrayList<Socket> taskPool;
	private final int startThreads;
	private int busyThreads = 0;
	private final ThreadGroup serverGroup = new ThreadGroup("servers");

	/** Constructs a ServerPool with <b>10</b> initial threads. */
	ServerPool() {
		this(10);
	}

	/** Constructs a ServerPool with the specified number of initial threads. */
	ServerPool(int startThreads) {
		this.startThreads = startThreads;
		/*
		 * Set the size of the taskPool to one third the initial number of
		 * threads. This should be tuned.
		 */
		taskPool = new ArrayList<Socket>(startThreads / 3);
		// Fill the pool with threads
		for (int i = 0; i < startThreads; ++i) {
			addThread();
		}
	}

	/**
	 * Creates a new <code>ServerThread</code> and adds it to the pool. This
	 * method should be used whenever a <code>ServerThread</code> needs to be
	 * created -- constructing by hand is discouraged.
	 */
	public void addThread() {
		final ServerThread t = new ServerThread(serverGroup, taskPool, this);
		t.start();
	}

	/**
	 * Removes a <code>ServerThread</code> from the pool. The thread will stop
	 * serving connections and be immediately killed. This is synchronized so
	 * that we never try to kill the same thread twice -- although that should
	 * never occur currently.
	 */
	public synchronized void removeThread() {
		final Thread[] threadList = new Thread[getNumThreads()];
		serverGroup.enumerate(threadList);
		threadList[0].interrupt();
	}

	/**
	 * Dispatchs an incoming connection to a <code>ServerThread</code>. This
	 * method is internally synchronized, so it should be safe to call this
	 * concurrently from multiple threads.
	 */
	public void dispatch(Socket socket) {
		synchronized (taskPool) {
			taskPool.add(socket);
			/*
			 * Wake up one of the threads that is waiting for a connection. We
			 * can use notify() because all threads are identical.
			 */
			taskPool.notify();
			incrementBusyThreads();
		}
	}

	/** Returns the number of threads initially started. */
	public int getStartThreads() {
		return startThreads;
	}

	/** Returns the total number of threads. */
	public int getNumThreads() {
		return serverGroup.activeCount();
	}

	/** Returns the number of threads currently not servicing a client. */
	public int getIdleThreads() {
		return (getNumThreads() - getBusyThreads());
	}

	/** Returns the number of threads currently servicing a client. */
	public int getBusyThreads() {
		return busyThreads;
	}

	/** Notifies the pool that a thread is busy. */
	public synchronized void incrementBusyThreads() {
		++busyThreads;
	}

	/** Notifies the pool that a thread is no longer busy. */
	public synchronized void decrementBusyThreads() {
		--busyThreads;
	}

}
