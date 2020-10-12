package com.useful.server.sync;

import java.util.concurrent.*;

public class PromiseFuture<T> implements Future<T> {

    private final Semaphore sem = new Semaphore(0);

    private volatile T result;

    private boolean canceled = false;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!isDone()) {
            canceled = true;
            sem.release();
            return true;
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    @Override
    public boolean isDone() {
        return canceled || !sem.hasQueuedThreads();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        if (!canceled) {
            sem.acquire();
            return result;
        }
        throw new InterruptedException();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!canceled) {
            if (sem.tryAcquire(timeout, unit)) {
                return result;
            }
            throw new TimeoutException();
        }
        throw new InterruptedException();
    }

    public void set(T result) {
        this.result = result;
        sem.release();
    }
}
