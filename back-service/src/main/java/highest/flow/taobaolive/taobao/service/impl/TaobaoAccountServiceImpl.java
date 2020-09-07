package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.common.utils.Query;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.taobao.dao.TaobaoAccountDao;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountLogKind;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountLogEntity;
import highest.flow.taobaolive.taobao.service.TaobaoAccountLogService;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("taobaoAccountService")
public class TaobaoAccountServiceImpl extends ServiceImpl<TaobaoAccountDao, TaobaoAccountEntity> implements TaobaoAccountService {

    @Autowired
    private TaobaoAccountLogService taobaoAccountLogService;

    private List<TaobaoAccountEntity> cachedTaobaoAccountEntities = null;

    @Override
    public PageUtils queryPage(SysMember sysMember, PageParam pageParam) {
        int pageNo = pageParam.getPageNo();
        int pageSize = pageParam.getPageSize();
        String keyword = pageParam.getKeyword();

        int memberId = sysMember == null || sysMember.isAdministrator() || sysMember.isNormal() ? 0 : sysMember.getId();

        Map<String, Object> params = new HashMap<>();
        params.put(Query.PAGE, pageNo);
        params.put(Query.LIMIT, pageSize);
        params.put(Query.ORDER_FIELD, "id");
        params.put(Query.ORDER, "ASC");

        QueryWrapper<TaobaoAccountEntity> queryWrapper = new QueryWrapper<>();

        IPage<TaobaoAccountEntity> page = this.baseMapper.queryAccounts(new Query<TaobaoAccountEntity>().getPage(params), memberId, keyword);
        return new PageUtils<TaobaoAccountEntity>(page);
    }

    @Override
    public int getNormalCount(SysMember sysMember, PageParam pageParam) {
        int memberId = sysMember == null || sysMember.isAdministrator() || sysMember.isNormal() ? 0 : sysMember.getId();

        String keyword = pageParam == null ? "" : pageParam.getKeyword();

        return this.baseMapper.getNormalCount(memberId, keyword);
    }

    @Override
    public int getExpiredCount(SysMember sysMember, PageParam pageParam) {
        int memberId = sysMember == null || sysMember.isAdministrator() || sysMember.isNormal() ? 0 : sysMember.getId();

        String keyword = pageParam == null ? "" : pageParam.getKeyword();

        return this.baseMapper.getExpiredCount(memberId, keyword);
    }

    @Override
    public TaobaoAccountEntity register(SysMember sysMember, String nick, String uid, String sid, String utdid, String devid,
                                        String autoLoginToken, String umidToken, List<Cookie> cookies, long expires, int state,
                                        Date created, Date updated) {
        try {
            TaobaoAccountEntity taobaoAccountEntity = new TaobaoAccountEntity();

            taobaoAccountEntity.setMemberId(sysMember == null || sysMember.isAdministrator() ? 0 : sysMember.getId());
            taobaoAccountEntity.setNick(nick);
            taobaoAccountEntity.setUid(uid);
            taobaoAccountEntity.setSid(sid);
            taobaoAccountEntity.setUtdid(utdid);
            taobaoAccountEntity.setDevid(devid);
            taobaoAccountEntity.setAutoLoginToken(autoLoginToken);
            taobaoAccountEntity.setUmidToken(umidToken);

            CookieStore cookieStore = new BasicCookieStore();
            for (Cookie cookie : cookies) {
                cookieStore.addCookie(cookie);
            }
            taobaoAccountEntity.setCookieStore(cookieStore);

            Date expireDate = new Date();
            expireDate.setTime(expireDate.getTime() + expires);
            taobaoAccountEntity.setExpires(expireDate);
            taobaoAccountEntity.setState(TaobaoAccountState.fromInt(state).getState());
            taobaoAccountEntity.setCreatedTime(created);
            taobaoAccountEntity.setUpdatedTime(updated);

            TaobaoAccountEntity selected = this.getInfoByUid(uid);
            if (selected != null) {
                taobaoAccountEntity.setId(selected.getId());
                this.updateById(taobaoAccountEntity);
            } else {
                this.save(taobaoAccountEntity);
            }

            TaobaoAccountLogEntity taobaoAccountLogEntity = new TaobaoAccountLogEntity();
            taobaoAccountLogEntity.setMemberId(taobaoAccountEntity.getMemberId());
            taobaoAccountLogEntity.setKind(selected != null ? TaobaoAccountLogKind.Update.getKind() : TaobaoAccountLogKind.New.getKind());
            taobaoAccountLogEntity.setUid(taobaoAccountEntity.getUid());
            taobaoAccountLogEntity.setNick(taobaoAccountEntity.getNick());
            taobaoAccountLogEntity.setSuccess(1);
            taobaoAccountLogEntity.setExpires(taobaoAccountEntity.getExpires());
            taobaoAccountLogEntity.setContent("");
            taobaoAccountLogEntity.setCreatedTime(new Date());
            taobaoAccountLogService.save(taobaoAccountLogEntity);
            return taobaoAccountEntity;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public TaobaoAccountEntity getInfo(String nick) {
        return this.baseMapper.selectOne(Wrappers.<TaobaoAccountEntity>lambdaQuery().eq(TaobaoAccountEntity::getNick, nick));
    }

    @Override
    public TaobaoAccountEntity getInfoByUid(String uid) {
        return this.baseMapper.selectOne(Wrappers.<TaobaoAccountEntity>lambdaQuery().eq(TaobaoAccountEntity::getUid, uid));
    }

    @Override
    public List<TaobaoAccountEntity> getActivesByMember(SysMember sysMember, int count) {
        int memberId = sysMember == null || sysMember.isAdministrator() || sysMember.isNormal() ? 0 : sysMember.getId();

        return this.baseMapper.queryAccounts2(memberId, null, -1, count);
    }

    @Override
    public synchronized void cacheAccount(TaobaoAccountEntity cacheAccountEntity) {
        if (cachedTaobaoAccountEntities == null) {
            cachedTaobaoAccountEntities = new ArrayList<>();
        }
        boolean found = false;
        for (int idx = 0; idx < cachedTaobaoAccountEntities.size(); idx++) {
            TaobaoAccountEntity taobaoAccountEntity = cachedTaobaoAccountEntities.get(idx);
            if (taobaoAccountEntity.getUid().compareTo(cacheAccountEntity.getUid()) == 0) {
                cachedTaobaoAccountEntities.set(idx, taobaoAccountEntity);
                found = true;
                break;
            }
        }
        if (!found) {
            cachedTaobaoAccountEntities.add(cacheAccountEntity);
        }
    }

    @Override
    public List<TaobaoAccountEntity> getActiveAll() {
        if (cachedTaobaoAccountEntities == null) {
            cachedTaobaoAccountEntities = this.baseMapper.queryAccounts2(0,
                    null, TaobaoAccountState.Normal.getState(), -1);
        }
        return cachedTaobaoAccountEntities;
    }

    @Override
    public Date getLastUpdated() {
        if (cachedTaobaoAccountEntities == null) {
            return null;
        }

        Date updated = null;
        for (TaobaoAccountEntity taobaoAccountEntity : cachedTaobaoAccountEntities) {
            if (updated == null) {
                updated = taobaoAccountEntity.getUpdatedTime();
            }
            if (updated.getTime() < taobaoAccountEntity.getUpdatedTime().getTime()) {
                updated = taobaoAccountEntity.getUpdatedTime();
            }
        }
        return updated;
    }

    @Override
    public synchronized int reloadUpdatedAccounts(Date updated) {
        List<TaobaoAccountEntity> updatedAccounts =
                this.baseMapper.queryAccounts2(0, updated, -1, -1);

        int updatedCount = 0;
        for (TaobaoAccountEntity taobaoAccountEntity : updatedAccounts) {
            // 更新缓冲的小号数据
            boolean found = false;
            for (int idx = 0; idx < cachedTaobaoAccountEntities.size(); idx++) {
                TaobaoAccountEntity cachedAccountEntity = cachedTaobaoAccountEntities.get(idx);
                if (taobaoAccountEntity.getUid().compareTo(cachedAccountEntity.getUid()) == 0) {
                    if (taobaoAccountEntity.getState() != TaobaoAccountState.Normal.getState()) {
                        cachedTaobaoAccountEntities.remove(idx);
                    } else {
                        cachedTaobaoAccountEntities.set(idx, taobaoAccountEntity);
                    }
                    updatedCount++;
                    found = true;
                    break;
                }
            }
            if (!found) {
                if (taobaoAccountEntity.getState() == TaobaoAccountState.Normal.getState()) {
                    cachedTaobaoAccountEntities.add(taobaoAccountEntity);
                }
                updatedCount++;
            }
        }
        return updatedCount;
    }
}
