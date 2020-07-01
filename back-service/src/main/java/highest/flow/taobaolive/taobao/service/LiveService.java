package highest.flow.taobaolive.taobao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.taobao.entity.LiveChannel;
import highest.flow.taobaolive.taobao.entity.LiveColumn;
import highest.flow.taobaolive.taobao.entity.RankingEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LiveService {

    List<LiveChannel> getChannels();

    List<LiveColumn> getColumns(int channelId);

    LiveColumn getColumn(int channelId, int columnId);
}
