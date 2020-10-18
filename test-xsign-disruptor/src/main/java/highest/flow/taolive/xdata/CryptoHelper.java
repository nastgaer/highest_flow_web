package highest.flow.taolive.xdata;

import highest.flow.taolive.xdata.utils.CryptoUtils;
import highest.flow.taolive.xdata.utils.HFStringUtils;

public class CryptoHelper {

    private String prefix = "";
    private String suffix = "";
    private String method = "";

    private String encryptKey = "";

    public CryptoHelper(String method, String prefix, String suffix, String encryptKey) {
        this.method = method;
        this.prefix = prefix;
        this.suffix = suffix;
        this.encryptKey = encryptKey;
    }

    public boolean verify(String data, String sign) {
        data = prefix + data + suffix;

        String md5 = CryptoUtils.MD5(data).toUpperCase();

        if (md5.compareTo(sign.toUpperCase()) == 0) {
            return true;
        }

        return false;
    }

    public String decrypt(String data) {
        byte[] bytes = HFStringUtils.hexStringToByteArray(data);

        // DECRYPT BY USING AES
        return CryptoUtils.decryptAES(bytes, encryptKey);
    }

    public String encrypt(String data) {
        // ENCRYPT BY USING AES
        byte[] encryptBytes = CryptoUtils.encryptAES(data, encryptKey);
        return HFStringUtils.byteArrayToHexString(encryptBytes);
    }

    public String sign(String data) {
        return CryptoUtils.MD5(prefix + data + suffix);
    }
}
