package highest.flow.taobaolive.taobao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.entity.RankingEntity;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface RankingService extends IService<RankingEntity> {

    List<RankingEntity> getTodaysTask(String today);

    RankingEntity addNewTask(SysMember sysMember, LiveRoomEntity liveRoomEntity,
                    String taocode,
                    int targetScore,
                    boolean doubleBuy,
                    Date startTime);

    boolean startTask(RankingEntity rankingEntity);

    boolean stopTask(RankingEntity rankingEntity);

    boolean isRunning(RankingEntity rankingEntity, Long jobId);

    boolean deleteTask(RankingEntity rankingEntity);
}
