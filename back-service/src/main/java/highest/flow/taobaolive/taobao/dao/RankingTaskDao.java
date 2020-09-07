package highest.flow.taobaolive.taobao.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import highest.flow.taobaolive.taobao.entity.RankingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RankingTaskDao extends BaseMapper<RankingEntity> {

    IPage<RankingEntity> queryTasks(IPage<RankingEntity> page,
                                    @Param("member_id") int memberId,
                                    @Param("keyword") String keyword);

    List<RankingEntity> queryTodaysTask(@Param("member_id") int memberId,
                                        @Param("today") String today);
}
