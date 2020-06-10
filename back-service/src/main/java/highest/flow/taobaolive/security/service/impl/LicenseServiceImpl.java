package highest.flow.taobaolive.security.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import highest.flow.taobaolive.taobao.entity.TaobaoAccount;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Service("licenseService")
public class LicenseServiceImpl extends ServiceImpl<LicenseCodeDao, LicenseCode> implements LicenseService {

    @Autowired
    HFUserService hfUserService;

    @Value("${license.key}")
    private String encryptKey;

    @Autowired
    private TaobaoAccountService taobaoAccountService;

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
    public String generateCode(ServiceType serviceType, int hours) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String plain = sdf.format(new Date());
        plain += String.valueOf(hours);

        String code = encrypt(plain);

        LicenseCode licenseCode = new LicenseCode();
        licenseCode.setServiceType(serviceType.getServiceType());
        licenseCode.setHours(hours);
        licenseCode.setCode(code);
        licenseCode.setState(LicenseCodeState.Created.getState());
        licenseCode.setMachineCode("");
        licenseCode.setServiceStartTime(null);
        licenseCode.setServiceEndTime(null);
        licenseCode.setCreatedTime(new Date());
        licenseCode.setAcceptedTime(null);

        this.save(licenseCode);

        return code;
    }

    @Override
    public R acceptCode(String code, String machineCode) {
        LicenseCode licenseCode = baseMapper.selectOne(Wrappers.<LicenseCode>lambdaQuery().eq(LicenseCode::getCode, code));
        if (licenseCode == null) {
            return R.error(ErrorCodes.NOT_FOUND_LICENSE_CODE, "找不到卡密");
        }

        if (licenseCode.getState() == LicenseCodeState.Created.getState()) {
            licenseCode.setMachineCode(machineCode);
            licenseCode.setServiceStartTime(new Date());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.HOUR, licenseCode.getHours());
            licenseCode.setServiceEndTime(calendar.getTime());

            licenseCode.setState(LicenseCodeState.Accepted.getState());
            this.updateById(licenseCode);
        }

        if (licenseCode.getMachineCode().equals(machineCode) == false) {
            return R.error(ErrorCodes.UNAUTHORIZED_MACHINE, "未知的机器");
        }

        Date serviceEndTime = licenseCode.getServiceEndTime();
        if (serviceEndTime.getTime() <= new Date().getTime()) {
            return R.error(ErrorCodes.EXPIRED_CODE, "过期的卡密");
        }

        return R.ok().put("expires", licenseCode.getServiceEndTime().getTime());
    }

    @Override
    public R bindAccount(String code, String username, String accountId, String accountNick) {
        LicenseCode licenseCode = baseMapper.selectOne(Wrappers.<LicenseCode>lambdaQuery().eq(LicenseCode::getCode, code));
        if (licenseCode == null) {
            return R.error(ErrorCodes.NOT_FOUND_LICENSE_CODE, "找不到卡密");
        }

        HFUser hfUser = hfUserService.getUserByUsername(username);
        if (hfUser == null || HFStringUtils.isNullOrEmpty(hfUser.getMachineCode())) {
            return R.error(ErrorCodes.UNAUTHORIZED_MACHINE, "未知的机器");
        }

        if (licenseCode.getState() == LicenseCodeState.Created.getState()) {
            return R.error(ErrorCodes.UNEXPECTED_CALL, "未知的调用");
        }

        TaobaoAccount taobaoAccount = taobaoAccountService.getInfo(accountId);

        if (licenseCode.getState() == LicenseCodeState.Accepted.getState()) {
            hfUser.setCode(licenseCode.getCode());

            if (taobaoAccount == null) {
                return R.error(ErrorCodes.NOT_FOUND_TAOBAO_ACCOUNT, "找不着淘宝账号，先注册淘宝账号");
            }

            hfUser.setAccountId(accountId);
            licenseCode.setState(LicenseCodeState.Binded.getState());
            this.updateById(licenseCode);
        }

        if (hfUser.getAccountId().equals(accountId) == false) {
            return R.error(ErrorCodes.UNAUTHORIZED_LIVEROOM, "未知的账号");
        }

        return R.ok().put("expires", licenseCode.getServiceEndTime().getTime());
    }
}
