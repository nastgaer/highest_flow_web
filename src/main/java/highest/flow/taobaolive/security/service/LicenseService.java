package highest.flow.taobaolive.security.service;

import highest.flow.taobaolive.app.entity.HFUser;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.defines.ServiceType;
import highest.flow.taobaolive.common.utils.R;
import org.springframework.stereotype.Service;

@Service
public interface LicenseService {

    String generateCode(HFUser hfUser, ServiceType serviceType, int hours);

    R bindMachine(String code, String machineCode);

    R bindTaobaoAccount(String code, String accountNick);
}
