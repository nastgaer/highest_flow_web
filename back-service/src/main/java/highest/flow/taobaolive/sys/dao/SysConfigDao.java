package highest.flow.taobaolive.sys.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import highest.flow.taobaolive.sys.entity.SysConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysConfigDao extends BaseMapper<SysConfigEntity> {

    public String getConfValue(@Param("key") String key);
}
