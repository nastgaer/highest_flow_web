package highest.taolive.xdata.service;

import highest.taolive.xdata.utils.CryptoUtils;
import highest.taolive.xdata.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CryptoService {

//    @Value("${sign.prefix}")
//    private String prefix;
//    @Value("${sign.suffix}")
//    private String suffix;
//    @Value("${sign.method}")
//    private String method;
//
//    @Value("${secure.key}")
//    private String encryptKey;

    private String prefix = "gaoji";
    private String suffix = "yinliu";
    private String method = "md5";

    private String encryptKey = "1234!@#$";

    public boolean verify(String data, String sign) {
        data = prefix + data + suffix;

        String md5 = CryptoUtils.MD5(data).toUpperCase();

        if (md5.compareTo(sign.toUpperCase()) == 0) {
            return true;
        }

        return false;
    }

    public String decrypt(String data) {
        byte[] bytes = StringUtils.hexStringToByteArray(data);

        // DECRYPT BY USING AES
        return CryptoUtils.decryptAES(bytes, encryptKey);
    }

    public String encrypt(String data) {
        // ENCRYPT BY USING AES
        byte[] encryptBytes = CryptoUtils.encryptAES(data, encryptKey);
        return StringUtils.byteArrayToHexString(encryptBytes);
    }
}
