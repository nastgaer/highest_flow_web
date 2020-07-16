package highest.flow.taobaolive.taobao.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LiveRoomDao extends BaseMapper<LiveRoomEntity> {

    List<LiveRoomEntity> queryTodays(@Param("today") String today);
}
