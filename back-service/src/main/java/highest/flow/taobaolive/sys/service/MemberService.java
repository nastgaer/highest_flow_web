package highest.flow.taobaolive.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.sys.entity.SysMember;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface MemberService extends IService<SysMember> {

    public SysMember register(String username, String password, String mobile, String comment, List<String> roles, int state);

    public SysMember getMemberByUsername(String username);
}
