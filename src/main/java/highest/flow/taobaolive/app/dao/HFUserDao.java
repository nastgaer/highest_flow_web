package highest.flow.taobaolive.app.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import highest.flow.taobaolive.app.entity.HFUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HFUserDao extends BaseMapper<HFUser> {

    public HFUser getUserByUsername(String username);
}
