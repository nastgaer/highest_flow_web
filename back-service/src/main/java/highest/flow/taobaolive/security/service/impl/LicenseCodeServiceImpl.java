package highest.flow.taobaolive.security.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.security.dao.LicenseCodeDao;
import highest.flow.taobaolive.security.entity.LicenseCode;
import highest.flow.taobaolive.security.service.LicenseCodeService;
import org.springframework.stereotype.Service;

@Service("licenseCodeService")
public class LicenseCodeServiceImpl extends ServiceImpl<LicenseCodeDao, LicenseCode> implements LicenseCodeService {

    @Override
    public LicenseCode getCodeDesc(String code) {
        return this.baseMapper.getCodeDesc(code);
    }
}
