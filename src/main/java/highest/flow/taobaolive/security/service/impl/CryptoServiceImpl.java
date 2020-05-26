package highest.flow.taobaolive.security.service.impl;

import highest.flow.taobaolive.common.utils.CryptoUtils;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.security.service.CryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;

@Service("cryptoService")
public class CryptoServiceImpl implements CryptoService {

    @Value("${sign.prefix}")
    private String prefix;
    @Value("${sign.suffix}")
    private String suffix;
    @Value("${sign.method}")
    private String method;

    @Value("${secure.key}")
    private String encryptKey;

    @Override
    public boolean verify(String data, String sign) {
        data = prefix + data + suffix;

        String md5 = CryptoUtils.MD5(data).toUpperCase();

        if (md5.compareTo(sign.toUpperCase()) == 0) {
            return true;
        }

        return false;
    }

    @Override
    public String decrypt(String data) {
        byte[] bytes = HFStringUtils.hexStringToByteArray(data);

        // DECRYPT BY USING AES
        return CryptoUtils.decryptAES(bytes, encryptKey);
    }

    @Override
    public String encrypt(String data) {
        // ENCRYPT BY USING AES
        byte[] encryptBytes = CryptoUtils.encryptAES(data, encryptKey);
        return HFStringUtils.byteArrayToHexString(encryptBytes);
    }
}
