package highest.flow.taobaolive.taobao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.entity.RankingEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface RankingService extends IService<RankingEntity> {

    PageUtils queryPage(SysMember sysMember, PageParam pageParam);

    List<RankingEntity> getTodaysTask(String today);

    RankingEntity addNewTask(SysMember sysMember, LiveRoomEntity liveRoomEntity,
                    int targetScore,
                    boolean doubleBuy,
                    Date startTime);

    boolean startTask(RankingEntity rankingEntity);

    boolean stopTask(RankingEntity rankingEntity);

    boolean isRunning(RankingEntity rankingEntity, Long jobId);

    boolean deleteTask(RankingEntity rankingEntity);
}
