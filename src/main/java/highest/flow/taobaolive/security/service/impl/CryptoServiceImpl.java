package highest.flow.taobaolive.security.service.impl;

import highest.flow.taobaolive.security.service.CryptoService;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;

@Service("cryptoService")
public class CryptoServiceImpl implements CryptoService {

    @Override
    public boolean verify(String data, String sign) {
        // TODO
        return true;
    }

    @Override
    public String decrypt(String data) {
        // TODO
        return data;
    }

    @Override
    public String encrypt(String data) {
        // TODO
        return data;
    }

    @Override
    public String MD5(String s) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = s.getBytes("UTF-8");
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++)
            {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str).toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
