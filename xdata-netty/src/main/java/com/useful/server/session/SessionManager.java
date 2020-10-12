package com.useful.server.session;

import io.netty.channel.ChannelHandlerContext;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static Map<String, ChannelHandlerContext> sessions = new ConcurrentHashMap<String, ChannelHandlerContext>();

    private static Random random = new Random();

    public static void addOrReplace(String clientId, ChannelHandlerContext context) {
        try {
            if (sessions.containsKey(clientId)) {
                sessions.remove(clientId);
            }

            sessions.put(clientId, context);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void remove(String clientId) {
        sessions.remove(clientId);
    }

    public static int getCount() {
        return sessions.size();
    }

    public static ChannelHandlerContext getClient(String clientId) {
        return sessions.get(clientId);
    }

    public static Map.Entry<String, ChannelHandlerContext> random() {
        if (sessions.size() < 1) {
            return null;
        }
        int index = random.nextInt(sessions.size());

        Iterator<Map.Entry<String, ChannelHandlerContext>> iterator = sessions.entrySet().iterator();

        Map.Entry<String, ChannelHandlerContext> entry = null;
        for (; index >= 0 && iterator.hasNext(); index--) {
            entry = iterator.next();
        }
        return entry;
    }
}
