package highest.flow.taobaolive.taobao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.entity.RankingEntity;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface RankingService extends IService<RankingEntity> {

    PageUtils queryPage(SysMember sysMember, PageParam pageParam);

    List<RankingEntity> getTodaysTask(String today);

    RankingEntity addNewTask(SysMember sysMember,
                             String taocode,
                             LiveRoomEntity liveRoomEntity,
                             int targetScore,
                             boolean doubleBuy,
                             Date startTime);

    /**
     * 开始刷
     * @param rankingEntity
     * @return
     */
    boolean startTask(RankingEntity rankingEntity);

    /**
     * 停止刷
     * @param rankingEntity
     * @return
     */
    boolean stopTask(RankingEntity rankingEntity);

    /**
     * 删除任务
     * @param rankingEntity
     * @return
     */
    boolean deleteTask(RankingEntity rankingEntity);

    /**
     * 返回打助力可行的小号列表
     * @param sysMember
     * @param liveId
     * @return
     */
    List<TaobaoAccountEntity> availableAccounts(SysMember sysMember, String liveId);

    /**
     * 标记该小号已经打好
     * @param sysMember
     * @param liveId
     * @param taobaoAccountEntity
     */
    List<TaobaoAccountEntity> markAssist(SysMember sysMember, String liveId, List<TaobaoAccountEntity> taobaoAccountEntities);
}
