package highest.flow.taobaolive.sys.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.sys.entity.SysMemberRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberRoleDao extends BaseMapper<SysMemberRole> {

}
