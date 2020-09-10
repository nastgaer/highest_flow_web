package highest.flow.taobaolive.taobao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.taobao.defines.RankingScore;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.entity.RankingEntity;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface RankingService extends IService<RankingEntity> {

    int getRankingUnitScore(RankingScore rankingScore);

    PageUtils queryPage(SysMember sysMember, PageParam pageParam);

    List<RankingEntity> getTodaysTask(SysMember sysMember, String today);

    List<RankingEntity> getRunningTasks(SysMember sysMember);

    RankingEntity getRunningTask(SysMember sysMember, String liveId);

    /**
     * 创建新任务
     * @param sysMember
     * @param taocode
     * @param liveId
     * @param accountname
     * @param targetScore
     * @param hasFollow
     * @param hasStay
     * @param hasBuy
     * @param hasDoubleBuy
     * @param startTime null：立即开始，
     * @param comment
     * @return
     */
    RankingEntity addNewTask(SysMember sysMember,
                             String taocode,
                             String liveId, String accountId, String accountname,
                             int targetScore,
                             boolean hasFollow,
                             boolean hasStay,
                             boolean hasBuy,
                             boolean hasDoubleBuy,
                             Date startTime,
                             String comment);

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
     * 删除任务，不能删除执行中的任务
     * @param rankingEntity
     * @return
     */
    boolean deleteTask(RankingEntity rankingEntity);

    /**
     * 把执行中的任务，标记为发生错误
     * @param rankingEntity
     * @return
     */
    boolean errorTask(RankingEntity rankingEntity);

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
     * @param taobaoAccountEntities: 新标记的小号
     */
    void markAssist(SysMember sysMember, String liveId, List<TaobaoAccountEntity> taobaoAccountEntities);
}
