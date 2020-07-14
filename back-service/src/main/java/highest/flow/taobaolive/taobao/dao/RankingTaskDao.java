package highest.flow.taobaolive.taobao.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import highest.flow.taobaolive.taobao.entity.RankingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RankingTaskDao extends BaseMapper<RankingEntity> {

    List<RankingEntity> queryTodaysTask(@Param("today") String today);
}
