package highest.flow.taobaolive;

import highest.flow.taobaolive.common.cache.MyCache;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest
public class CacheTest {

    @Test
    public void testCache() {
        List<String> uids = (List<String>)MyCache.getInstance().getCache("test");
        System.out.println(uids == null ? "empty" : uids.size());

        int count = new Random().nextInt(1000);
        uids = new ArrayList<>();
        for (int idx = 0; idx < count; idx++) {
            uids.add(String.valueOf(idx));
        }
        MyCache.getInstance().setCache("test", uids, 86400);
        System.out.println("successfully set cache");

        List<String> fetchUids = (List<String>)MyCache.getInstance().getCache("test");
        System.out.println(fetchUids == null ? "empty" : fetchUids.size());
    }
}
