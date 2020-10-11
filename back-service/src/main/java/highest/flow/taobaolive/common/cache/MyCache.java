package highest.flow.taobaolive.common.cache;

import highest.flow.taobaolive.common.utils.ConfigFileUtil;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MyCache {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Cache cache;
    private static final MyCache instance = new MyCache();
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    public static MyCache getInstance() {
        return instance;
    }

    public MyCache() {
        try {
            String EHCACHE_CONFIG = ResourceUtils.getFile("classpath:ehcache.xml").getPath();

            CacheManager cacheManager = CacheManager.create(EHCACHE_CONFIG);
            this.cache = cacheManager.getCache("assistRankUids");
            System.out.println("缓存超时时间：" + this.cache.getCacheConfiguration().getTimeToIdleSeconds() + "秒");
            Runnable runnable = new Runnable() {
                public void run() {
                    while(true) {
                        try {
                            long timeToIdleSeconds = MyCache.this.cache.getCacheConfiguration().getTimeToIdleSeconds();
                            if (timeToIdleSeconds > 0L) {
                                Thread.sleep((timeToIdleSeconds + 5L) * 1000L);

                                try {
                                    String date = MyCache.this.sdf.format(new Date());
                                    List<String> keys = MyCache.this.cache.getKeys();
                                    Iterator var6 = keys.iterator();

                                    while(var6.hasNext()) {
                                        String key = (String)var6.next();
                                        Element element = MyCache.this.cache.get(key);
                                        if (element == null) {
                                            System.out.println("缓存key=" + key + "无效，已被清除");
                                        }
                                    }

                                    long cacheSize = MyCache.this.cache.calculateInMemorySize();
                                    System.out.println(date + ": 当前使用内存：" + cacheSize / 1024L + "KB, ≈" + cacheSize / 1024L / 1024L + "MB");
                                } catch (Exception var8) {
                                    var8.printStackTrace();
                                }
                                continue;
                            }
                        } catch (Exception var9) {
                            var9.printStackTrace();
                        }

                        return;
                    }
                }
            };
            (new Thread(runnable)).start();
        } catch (Exception var3) {
            var3.printStackTrace();
        }
    }

    public Cache getCache() {
        return this.cache;
    }

    public synchronized Object getCache(String key) {
        Element element = this.cache.get(key);
        return element != null ? element.getObjectValue() : null;
    }

    public synchronized void setCache(String key, Object value) {
        Element element = new Element(key, value, true);
        this.cache.put(element);
    }

    public synchronized void setCache(String key, Object value, int timeout) {
        Element element = new Element(key, value, false, Integer.valueOf(0), timeout);
        this.cache.put(element);
    }
}
