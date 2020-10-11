package highest.flow.taobaolive.taobao.provider;

import highest.flow.taobaolive.common.cache.MyCache;
import highest.flow.taobaolive.sys.entity.SysMember;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class LiveCacheManager {

    private String generateRankingKey(SysMember sysMember, String liveId, Date date) {
        String key = /*String.valueOf(sysMember.getId()) + "_" + */liveId + "_" + new SimpleDateFormat("yyyyMMdd").format(new Date());
        return key;
    }

    public List<String> getCachedRankingAccounts(SysMember sysMember, String liveId, Date date) {
        try {
            String key = this.generateRankingKey(sysMember, liveId, date);

            return (List<String>) MyCache.getInstance().getCache(key);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void cacheRankingAccounts(SysMember sysMember, String liveId, Date date, List<String> value) {
        try {
            String key = this.generateRankingKey(sysMember, liveId, date);

            MyCache.getInstance().setCache(key, value);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
