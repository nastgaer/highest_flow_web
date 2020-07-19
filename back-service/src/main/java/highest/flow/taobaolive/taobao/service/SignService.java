package highest.flow.taobaolive.taobao.service;

import highest.flow.taobaolive.taobao.entity.H5Header;
import highest.flow.taobaolive.taobao.entity.XHeader;
import org.springframework.stereotype.Service;

@Service
public interface SignService {

    public String xsign(XHeader xHeader);

    public String xsign1(XHeader xHeader);

    public String xsign2(XHeader xHeader);

    public String xsign3(XHeader xHeader);

    public String h5sign(H5Header h5Header, String data);
}
