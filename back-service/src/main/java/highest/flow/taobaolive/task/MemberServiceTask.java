package highest.flow.taobaolive.task;

import highest.flow.taobaolive.job.entity.ScheduleJobEntity;
import highest.flow.taobaolive.taobao.defines.ServiceState;
import highest.flow.taobaolive.taobao.entity.MemberTaoAccEntity;
import highest.flow.taobaolive.taobao.service.LiveRoomStrategyService;
import highest.flow.taobaolive.taobao.service.MemberTaoAccService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component("memberServiceTask")
public class MemberServiceTask implements ITask {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MemberTaoAccService memberTaoAccService;

    @Autowired
    private LiveRoomStrategyService liveRoomStrategyService;

    @Override
    public void run(ScheduleJobEntity scheduleJobEntity) {
        try {
            List<MemberTaoAccEntity> memberTaoAccEntities = this.memberTaoAccService.list();

            for (MemberTaoAccEntity memberTaoAccEntity : memberTaoAccEntities) {
                int state = memberTaoAccEntity.getState();

                if (state != ServiceState.Normal.getState()) {
                    continue;
                }

                Date dateNow = new Date();
                if (memberTaoAccEntity.getServiceEndDate().getTime() < dateNow.getTime()) {
                    // 过期了，立即停止
                    memberTaoAccEntity.setState(ServiceState.Suspended.getState());

                    this.memberTaoAccService.updateById(memberTaoAccEntity);

                    this.liveRoomStrategyService.stopTask(memberTaoAccEntity);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
