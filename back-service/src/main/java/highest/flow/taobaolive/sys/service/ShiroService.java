package highest.flow.taobaolive.sys.service;

import highest.flow.taobaolive.sys.entity.SysMemberToken;
import org.springframework.stereotype.Service;

@Service
public interface ShiroService {

    public SysMemberToken getMemberTokenByToken(String token);
}
