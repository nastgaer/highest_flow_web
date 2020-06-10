package highest.flow.taobaolive.taobao.service;

import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.entity.QRCode;
import highest.flow.taobaolive.taobao.entity.TaobaoAccount;
import org.springframework.stereotype.Service;

@Service
public interface TaobaoApiService {

    public R getUserSimple(TaobaoAccount taobaoAccount);

    /**
     * 重登
     *
     * @param taobaoAccount
     * @return
     */
    public R autoLogin(TaobaoAccount taobaoAccount);

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
     * @param taobaoAccount
     * @param qrCode
     * @return
     */
    public R getLoginQRCodeCsrfToken(TaobaoAccount taobaoAccount, QRCode qrCode);

    /**
     * 自动扫二维码
     *
     * @param taobaoAccount
     * @param qrCode
     * @param csrfToken
     * @return
     */
    public R authQRCode(TaobaoAccount taobaoAccount, QRCode qrCode, String csrfToken);

    /**
     * 查询验证状态
     *
     * @param taobaoAccount
     * @param qrCode
     * @return
     */
    public R checkLoginByQRCode(TaobaoAccount taobaoAccount, QRCode qrCode);

    public R postpone(TaobaoAccount taobaoAccount);
}
