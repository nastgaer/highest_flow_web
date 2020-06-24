package highest.flow.taobaolive.sys.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import highest.flow.taobaolive.sys.entity.SysMemberToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberTokenDao extends BaseMapper<SysMemberToken> {

    public SysMemberToken getMemberTokenByToken(String token);
}
