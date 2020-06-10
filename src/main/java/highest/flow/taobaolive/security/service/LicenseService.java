package highest.flow.taobaolive.security.service;

import highest.flow.taobaolive.app.entity.HFUser;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.defines.ServiceType;
import highest.flow.taobaolive.common.utils.R;
import org.springframework.stereotype.Service;

@Service
public interface LicenseService {

    String generateCode(ServiceType serviceType, int hours);

    R acceptCode(String code, String machineCode);

    R bindAccount(String code, String username, String accountId, String accountNick);
}
