package highest.flow.taobaolive.taobao.service;

import highest.flow.taobaolive.taobao.entity.XHeader;
import org.springframework.stereotype.Service;

@Service
public interface XSignService {

    public String sign(XHeader xHeader);

    public String sign1(XHeader xHeader);

    public String sign2(XHeader xHeader);
}
