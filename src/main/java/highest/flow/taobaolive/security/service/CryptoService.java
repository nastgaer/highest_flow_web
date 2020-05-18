package highest.flow.taobaolive.security.service;

import org.springframework.stereotype.Service;

@Service
public interface CryptoService {

    boolean verify(String data, String sign);

    String decrypt(String data);

    String encrypt(String data);

    String MD5(String data);
}
