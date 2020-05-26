package highest.flow.taobaolive.security.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.app.defines.HFUserLevel;
import highest.flow.taobaolive.app.entity.HFUser;
import highest.flow.taobaolive.app.service.HFUserService;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.defines.ServiceType;
import highest.flow.taobaolive.common.utils.CryptoUtils;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.security.dao.LicenseCodeDao;
import highest.flow.taobaolive.security.defines.LicenseCodeState;
import highest.flow.taobaolive.security.entity.LicenseCode;
import highest.flow.taobaolive.security.service.LicenseService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Wrapper;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service("licenseService")
public class LicenseServiceImpl extends ServiceImpl<LicenseCodeDao, LicenseCode> implements LicenseService {

    @Autowired
    HFUserService hfUserService;

    @Value("${license.key}")
    private String encryptKey;

    private String encrypt(String data) {
        // ENCRYPT BY USING AES
        byte[] encryptBytes = CryptoUtils.encryptAES(data, encryptKey);
        return HFStringUtils.byteArrayToHexString(encryptBytes);
    }

    private String decrypt(String data) {
        byte[] bytes = HFStringUtils.hexStringToByteArray(data);

        // DECRYPT BY USING AES
        return CryptoUtils.decryptAES(bytes, encryptKey);
    }

    @Override
    public String generateCode(HFUser hfUser, ServiceType serviceType, int hours) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String plain = sdf.format(new Date());
        plain += String.valueOf(hours);

        String code = encrypt(plain);

        LicenseCode licenseCode = new LicenseCode();
        licenseCode.setUsername(hfUser.getUsername());
        licenseCode.setServiceType(serviceType.getServiceType());
        licenseCode.setHours(hours);
        licenseCode.setCode(code);
        licenseCode.setState(LicenseCodeState.Created.getState());
        licenseCode.setTaobaoNick("");
        licenseCode.setLiveroom("");
        licenseCode.setCreatedTime(new Date());
        licenseCode.setAcceptedTime(null);

        this.save(licenseCode);

        return code;
    }

    @Override
    public R bindMachine(String code, String machineCode) {
        LicenseCode licenseCode = baseMapper.selectOne(Wrappers.<LicenseCode>lambdaQuery().eq(LicenseCode::getCode, code));
        if (licenseCode == null) {
            return R.error(ErrorCodes.NOT_FOUND_LICENSE_CODE, "找不到卡密");
        }

        HFUser hfUser = hfUserService.getUserByUsername(licenseCode.getUsername());
        if (hfUser == null) {
            hfUser = hfUserService.register("", "", machineCode, "", "",
                    HFUserLevel.Guest.getLevel(), licenseCode.getServiceType());
        }

        if (hfUser.getMachineCode().equals(machineCode) == false) {
            return R.error(ErrorCodes.UNAUTHORIZED_MACHINE, "未知的机器");
        }

        hfUser.setMachineCode(machineCode);
        hfUserService.update(hfUser, Wrappers.<HFUser>lambdaUpdate().eq(HFUser::getUsername, hfUser.getUsername()));

        licenseCode.setState(LicenseCodeState.Accepted.getState());
        this.updateById(licenseCode);

        return R.ok();
    }

    @Override
    public R bindTaobaoAccount(String code, String accountNick) {
        LicenseCode licenseCode = baseMapper.selectOne(Wrappers.<LicenseCode>lambdaQuery().eq(LicenseCode::getCode, code));
        if (licenseCode == null) {
            return R.error(ErrorCodes.NOT_FOUND_LICENSE_CODE, "找不到卡密");
        }

        HFUser hfUser = hfUserService.getUserByUsername(licenseCode.getUsername());
        if (hfUser == null || HFStringUtils.isNullOrEmpty(hfUser.getMachineCode())) {
            return R.error(ErrorCodes.UNAUTHORIZED_MACHINE, "未知的机器");
        }

        licenseCode.setTaobaoNick(accountNick);
        licenseCode.setState(LicenseCodeState.Binded.getState());
        this.updateById(licenseCode);

        return R.ok();
    }
}
