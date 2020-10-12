package com.useful.server.sync;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WaitingPool {

    private static WaitingPool instance = null;

    public static WaitingPool getInstance() {
        if (instance == null) {
            instance = new WaitingPool();
        }
        return instance;
    }

    private Map<Long, PromiseFuture<String>> waitingTask = new ConcurrentHashMap<>();

    public void add(long messageId, PromiseFuture<String> task) {
        waitingTask.put(messageId, task);
    }

    public PromiseFuture<String> get(long messageId) {
        return waitingTask.get(messageId);
    }

    public void remove(long messageId) {
        waitingTask.remove(messageId);
    }
}
