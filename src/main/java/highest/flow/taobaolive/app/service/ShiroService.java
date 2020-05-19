package highest.flow.taobaolive.app.service;

import highest.flow.taobaolive.app.entity.HFUserToken;
import org.springframework.stereotype.Service;

@Service
public interface ShiroService {

    public HFUserToken getUserTokenByToken(String token);
}
