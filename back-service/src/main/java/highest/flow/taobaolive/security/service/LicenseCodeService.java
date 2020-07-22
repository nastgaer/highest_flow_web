package highest.flow.taobaolive.security.service;

import com.baomidou.mybatisplus.extension.service.IService;
import highest.flow.taobaolive.security.entity.LicenseCode;
import org.springframework.stereotype.Service;

@Service
public interface LicenseCodeService extends IService<LicenseCode> {

    LicenseCode getCodeDesc(String code);

}
