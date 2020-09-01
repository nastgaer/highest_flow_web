package highest.flow.taobaolive.taobao.provider;

import highest.flow.taobaolive.sys.entity.SysMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class LiveCacheManager {

    @Autowired
    private CacheManager cacheManager;

    private String generateRankingKey(SysMember sysMember, String liveId, Date date) {
        String key = String.valueOf(sysMember.getId()) + "_" + liveId + "_" + new SimpleDateFormat("yyyyMMdd").format(new Date());
        return key;
    }

    public List<String> getCachedRankingAccounts(SysMember sysMember, String liveId, Date date) {
        try {
            String key = this.generateRankingKey(sysMember, liveId, date);

            Cache cache = this.cacheManager.getCache("assistRankUids");
            Cache.ValueWrapper valueWrapper = cache.get(key);
            return valueWrapper == null ? null : (List<String>) valueWrapper.get();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void cacheRankingAccounts(SysMember sysMember, String liveId, Date date, List<String> value) {
        try {
            String key = this.generateRankingKey(sysMember, liveId, date);

            Cache cache = this.cacheManager.getCache("assistRankUids");
            cache.put(key, value);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
