package highest.flow.taobaolive.app.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import highest.flow.taobaolive.app.entity.HFUserToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HFUserTokenDao extends BaseMapper<HFUserToken> {

    public HFUserToken getUserTokenByToken(String token);
}
