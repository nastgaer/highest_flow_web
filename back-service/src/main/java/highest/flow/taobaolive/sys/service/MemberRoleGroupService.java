package highest.flow.taobaolive.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.sys.entity.SysMemberRole;
import highest.flow.taobaolive.sys.entity.SysMemberRoleGroup;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MemberRoleGroupService extends IService<SysMemberRoleGroup> {

    void saveOrUpdate(int memberId, List<String> roles);

    List<String> queryRoleList(int memberId);

    void deleteRole(int memberId);
}
