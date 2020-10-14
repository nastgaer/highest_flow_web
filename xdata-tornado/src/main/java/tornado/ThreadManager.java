// $Id: ThreadManager.java,v 1.9 2001/01/23 14:42:23 nconway Exp $
package tornado;

public class ThreadManager implements Runnable {
	/** By default, wake up once per second. */
	private final static int DEFAULT_SLEEP_TIME = 1000;

	private final ServerPool threadPool;
	private final int sleepTime;

	/** Constructs the manager, using the specified values. */
	ThreadManager(ServerPool threadPool, int sleepTime) {
		this.threadPool = threadPool;
		this.sleepTime = sleepTime;
	}

	/**
	 * Constructs the manager, using the specified pool and the default sleep
	 * time.
	 */
	ThreadManager(ServerPool threadPool) {
		this(threadPool, DEFAULT_SLEEP_TIME);
	}

	/** Begin the infinite loop of sleeping and monitoring threads. */
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(sleepTime);
			} catch (final InterruptedException e) {
				/*
				 * A thread called interrupt() on us, so we wake up early and
				 * begin execution as normal. Currently, no code interrupts
				 * ThreadManager, but this may occur in the future.
				 */
			}

			final int idleThreads = threadPool.getIdleThreads();
			final int minIdleThreads = Tornado.getConfig().getMinIdleThreads();
			final int maxIdleThreads = Tornado.getConfig().getMaxIdleThreads();
			final int maxThreads = Tornado.getConfig().getMaxThreads();
			Tornado.logger.info(idleThreads + " idle; " + threadPool.getBusyThreads() + " busy; " + maxThreads
					+ " maximum number of threads.");

			if (threadPool.getNumThreads() > maxThreads) {
				continue;
			}

			if (idleThreads < minIdleThreads) {
				// Spawn additional threads, as necessary
				spawnThreads(minIdleThreads - idleThreads);
			} else if (idleThreads > maxIdleThreads) {
				// Kill additional threads, as necessary
				killThreads(idleThreads - maxIdleThreads);
			}
		}
	}

	/**
	 * Spawns the specified number of <code>ServerThread</code>s and adds them
	 * to the <code>ServerPool</code.
	 * 
	 * @see ServerPool#addThread()
	 */
	private void spawnThreads(int num) {
		Tornado.logger.debug(num + " new threads spawned");
		for (int i = 0; i < num; ++i) {
			threadPool.addThread();
		}
	}

	/**
	 * Kill the specified number of <code>ServerThread</code>s and removes them
	 * from the <code>ServerPool</code>.
	 * 
	 * @see ServerPool#removeThread()
	 */
	private void killThreads(int num) {
		Tornado.logger.debug(num + " idle threads killed");
		for (int i = 0; i < num; ++i) {
			threadPool.removeThread();
		}
	}

}
