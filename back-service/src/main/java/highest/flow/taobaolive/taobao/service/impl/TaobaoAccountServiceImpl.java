package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("taobaoAccountService")
public class TaobaoAccountServiceImpl extends ServiceImpl<TaobaoAccountDao, TaobaoAccountEntity> implements TaobaoAccountService {

    @Autowired
    private TaobaoAccountLogService taobaoAccountLogService;

    @Override
    public int getNormalCount(SysMember sysMember) {
        if (sysMember == null) {
            return this.baseMapper.selectCount(Wrappers.<TaobaoAccountEntity>lambdaQuery()
                    .eq(TaobaoAccountEntity::getState, 0));
        } else {
            return this.baseMapper.selectCount(Wrappers.<TaobaoAccountEntity>lambdaQuery()
                    .eq(TaobaoAccountEntity::getState, 0)
                    .eq(TaobaoAccountEntity::getMemberId, sysMember.getId()));
        }
    }

    @Override
    public int getExpiredCount(SysMember sysMember) {
        if (sysMember == null) {
            return this.baseMapper.selectCount(Wrappers.<TaobaoAccountEntity>lambdaQuery()
                    .ne(TaobaoAccountEntity::getState, 0));
        } else {
            return this.baseMapper.selectCount(Wrappers.<TaobaoAccountEntity>lambdaQuery()
                    .eq(TaobaoAccountEntity::getMemberId, sysMember.getId())
                    .ne(TaobaoAccountEntity::getState, 0));
        }
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

            TaobaoAccountEntity selected = this.getOne(Wrappers.<TaobaoAccountEntity>lambdaQuery().eq(TaobaoAccountEntity::getUid, uid));
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
    public PageUtils queryPage(SysMember sysMember, PageParam pageParam) {
        int pageNo = pageParam.getPageNo();
        int pageSize = pageParam.getPageSize();
        String keyword = pageParam.getKeyword();

        int memberId = sysMember == null || sysMember.isAdministrator() ? 0 : sysMember.getId();

        Map<String, Object> params = new HashMap<>();
        params.put(Query.PAGE, pageNo);
        params.put(Query.LIMIT, pageSize);

        QueryWrapper<TaobaoAccountEntity> queryWrapper = new QueryWrapper<>();
        if (memberId > 0) {
            queryWrapper.like("member_id", memberId);
        }
        if (!HFStringUtils.isNullOrEmpty(keyword)) {
            queryWrapper.like("nick", keyword);
        }

        IPage<TaobaoAccountEntity> page = this.page(new Query<TaobaoAccountEntity>().getPage(params), queryWrapper);
        return new PageUtils<TaobaoAccountEntity>(page);
    }

    @Override
    public TaobaoAccountEntity getActiveOne(SysMember sysMember) {
        if (sysMember == null || sysMember.isAdministrator()) {
            return this.baseMapper.getActiveOne();
//            return this.getOne(Wrappers.<TaobaoAccountEntity>lambdaQuery()
//                    .eq(TaobaoAccountEntity::getType, TaobaoAccountState.Normal.getType()));
        }

        return this.baseMapper.getActiveOneByMember(sysMember.getId());
//        return this.getOne(Wrappers.<TaobaoAccountEntity>lambdaQuery()
//                .eq(TaobaoAccountEntity::getType, TaobaoAccountState.Normal.getType())
//                .eq(TaobaoAccountEntity::getMemberId, sysMember.getId()));
    }

    @Override
    public List<TaobaoAccountEntity> getActiveAllByMember(SysMember sysMember) {
        if (sysMember == null || sysMember.isAdministrator()) {
            return this.baseMapper.getActiveAll();
        }

        return this.baseMapper.getActiveAllByMember(sysMember.getId());
    }
}
