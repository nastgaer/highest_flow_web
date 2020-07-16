package highest.flow.taobaolive.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.sys.entity.SysMember;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public interface MemberService extends IService<SysMember> {

    PageUtils queryPage(PageParam pageParam);

    SysMember register(String memberName, String password, String mobile, String comment, List<String> roles, int state);

    boolean update(int id, String memberName, String password, String mobile, String comment, List<String> roles, int state);

    boolean deleteBatch(List<Integer> ids);

    SysMember getMemberByName(String memberName);

    List<String> getRoles(SysMember sysMember);
}
