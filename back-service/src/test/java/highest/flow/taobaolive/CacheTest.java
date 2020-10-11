package highest.flow.taobaolive;

import highest.flow.taobaolive.common.cache.MyCache;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
public class CacheTest {

    @Test
    public void testCache() {
        long startTime = System.currentTimeMillis();
        List<String> uids = (List<String>)MyCache.getInstance().getCache("test");
        long endTime = System.currentTimeMillis();
        System.out.println(uids == null ? "empty" : uids.size() + ", elapsed = " + (endTime - startTime));

        int count = new Random().nextInt(30000) + 10000;
        uids = new ArrayList<>();
        for (int idx = 0; idx < count; idx++) {
            uids.add(String.valueOf(idx));
        }

        startTime = System.currentTimeMillis();
        MyCache.getInstance().setCache("test", uids, 86400);
        endTime = System.currentTimeMillis();
        System.out.println("successfully set cache, " + uids.size() + ", elapsed = " + (endTime - startTime));

        startTime = System.currentTimeMillis();
        List<String> fetchUids = (List<String>)MyCache.getInstance().getCache("test");
        endTime = System.currentTimeMillis();
        System.out.println(fetchUids == null ? "empty" : fetchUids.size() + ", elapsed = " + (endTime - startTime));
    }

    @Test
    public void testSyncCache() {
        int threadCount = 5;
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        for (int idx = 0; idx < threadCount; idx++) {
            final int threadIndex = idx;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String key = "test" + String.valueOf(threadIndex);

                    for (int repeat = 0; repeat < 10; repeat++) {
                        long startTime = System.currentTimeMillis();
                        List<String> uids = (List<String>) MyCache.getInstance().getCache(key);
                        long endTime = System.currentTimeMillis();

                        System.out.println(key + ", get=" + (uids == null ? 0 : uids.size()) + ", elapsed=" + (endTime - startTime));

                        int count = new Random().nextInt(30000) + 10000;
                        List<String> newUids = new ArrayList<>();
                        for (int idx = 0; idx < count; idx++) {
                            newUids.add(String.valueOf(idx));
                        }

                        startTime = System.currentTimeMillis();
                        MyCache.getInstance().setCache(key, newUids, 86400);
                        endTime = System.currentTimeMillis();

                        System.out.println(key + ", set=" + (newUids == null ? 0 : newUids.size()) + ", elapsed=" + (endTime - startTime));

                        startTime = System.currentTimeMillis();
                        List<String> fetchUids = (List<String>) MyCache.getInstance().getCache(key);
                        endTime = System.currentTimeMillis();

                        System.out.println(key + ", reget=" + (fetchUids == null ? 0 : fetchUids.size()) + ", elapsed=" + (endTime - startTime));
                    }

                    countDownLatch.countDown();
                }
            });
            thread.start();
        }

        try {
            countDownLatch.await();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
