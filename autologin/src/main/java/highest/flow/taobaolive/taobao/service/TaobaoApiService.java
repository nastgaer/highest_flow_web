package highest.flow.taobaolive.taobao.service;

import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.entity.*;
import org.springframework.stereotype.Service;

@Service
public interface TaobaoApiService {

    public R getUserSimple(TaobaoAccountEntity taobaoAccountEntity);

    /**
     * 重登
     *
     * @param taobaoAccountEntity
     * @return
     */
    public R autoLogin(TaobaoAccountEntity taobaoAccountEntity);

    /**
     * 获取二维码地址
     *
     * @param
     * @return
     */
    public R getLoginQRCodeURL();

    /**
     * 获取二维码的csrfToken
     *
     * @param taobaoAccountEntity
     * @param qrCode
     * @return
     */
    public R getLoginQRCodeCsrfToken(TaobaoAccountEntity taobaoAccountEntity, QRCode qrCode);

    /**
     * 自动扫二维码
     *
     * @param taobaoAccountEntity
     * @param qrCode
     * @param csrfToken
     * @return
     */
    public R authQRCode(TaobaoAccountEntity taobaoAccountEntity, QRCode qrCode, String csrfToken);

    /**
     * 查询验证状态
     *
     * @param taobaoAccountEntity
     * @param qrCode
     * @return
     */
    public R checkLoginByQRCode(TaobaoAccountEntity taobaoAccountEntity, QRCode qrCode);

    /**
     * 延期扫码信息
     * @param taobaoAccountEntity
     * @return
     */
    public R postpone(TaobaoAccountEntity taobaoAccountEntity);

    public R getUmtidToken();

    public R getH5Token(TaobaoAccountEntity taobaoAccountEntity);

    public R getNewDeviceId(TaobaoAccountEntity taobaoAccountEntity);
}
