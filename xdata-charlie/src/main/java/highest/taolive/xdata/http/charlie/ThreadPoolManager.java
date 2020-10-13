package highest.taolive.xdata.http.charlie;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {

    private RejectedExecutionHandler rejectedExecutionHandler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
            try {
                queue.put(runnable);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    private LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
    private ThreadPoolExecutor executor;

    public ThreadPoolManager(int coolPoolSize, int maxPoolSize) {
        executor = new ThreadPoolExecutor(coolPoolSize, maxPoolSize, 15, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(4), rejectedExecutionHandler);
        executor.execute(runnable);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                Runnable runnable = null;
                try {
                    runnable = queue.take();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (runnable != null) {
                    executor.execute(runnable);
                }
            }
        }
    };

    public void execute(Runnable runnable) {
        if (runnable != null) {
            try {
                queue.put(runnable);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean isShutdown() {
        return executor.isShutdown();
    }

    public void shutdown() {
        try {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            executor.shutdownNow();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
