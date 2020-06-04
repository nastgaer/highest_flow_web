package highest.flow.taobaolive.taobao.entity;

import lombok.Data;

@Data
public class QRCode {

    private long timestamp = 0;

    private String accessToken;

    private String navigateUrl;

    private String imageUrl;
}
