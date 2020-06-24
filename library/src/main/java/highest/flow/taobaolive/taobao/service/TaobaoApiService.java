package highest.flow.taobaolive.taobao.service;

import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.entity.*;
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

    /**
     * 延期扫码信息
     * @param taobaoAccount
     * @return
     */
    public R postpone(TaobaoAccount taobaoAccount);

    public R getUmtidToken();

    public R getH5Token(TaobaoAccount taobaoAccount);

    public R getNewDeviceId(TaobaoAccount taobaoAccount);

    /**
     * 解析淘口令
     * @param taocode
     * @return
     */
    public R parseTaoCode(String taocode);

    /**
     * 获取直播间详细信息
     * @param liveId
     * @return
     */
    public R getLiveDetail(String liveId);

    /**
     * 获取直播间商品
     * @param liveRoom
     * @param taobaoAccount
     * @return
     */
    public R getLiveProducts(LiveRoom liveRoom, TaobaoAccount taobaoAccount);

    /**
     * 查询直播间赛道
     * @param liveRoom
     * @param taobaoAccount
     * @return
     */
    public R getLiveEntry(LiveRoom liveRoom, TaobaoAccount taobaoAccount);

    /**
     * 助力：关注任务
     * @param liveRoom
     * @param taobaoAccount
     * @return
     */
    public R taskFollow(LiveRoom liveRoom, TaobaoAccount taobaoAccount);

    /**
     * 助力：在直播间等待几分钟
     * @param liveRoom
     * @param taobaoAccount
     * @param staySeconds
     * @return
     */
    public R taskStay(LiveRoom liveRoom, TaobaoAccount taobaoAccount, int staySeconds);

    /**
     * 助力：购买商品
     * @param liveRoom
     * @param taobaoAccount
     * @param productId
     * @return
     */
    public R taskBuy(LiveRoom liveRoom, TaobaoAccount taobaoAccount, String productId);

    /**
     * 发布预告（Web)
     * @param taobaoAccount
     * @param preLiveRoomSpec
     * @return
     */
    public R createLiveRoomWeb(PreLiveRoomSpec preLiveRoomSpec, TaobaoAccount taobaoAccount);

    /**
     * 发布预告（Mobile)
     * @param taobaoAccount
     * @param preLiveRoomSpec
     * @return
     */
    public R createLiveRoom(PreLiveRoomSpec preLiveRoomSpec, TaobaoAccount taobaoAccount);

    /**
     * 开始直播
     * @param liveRoom
     * @param taobaoAccount
     * @return
     */
    public R startLive(LiveRoom liveRoom, TaobaoAccount taobaoAccount);

    /**
     * 结束直播
     * @param liveRoom
     * @param taobaoAccount
     * @return
     */
    public R stopLive(LiveRoom liveRoom, TaobaoAccount taobaoAccount);

    /**
     * 删除回放
     * @param liveRoom
     * @param taobaoAccount
     * @return
     */
    public R deleteLive(LiveRoom liveRoom, TaobaoAccount taobaoAccount);

    /**
     * 获取商品信息
     * @param taobaoAccount
     * @param productId
     * @return
     */
    public R getProductItemInfo(TaobaoAccount taobaoAccount, String productId);

    /**
     * 在直播间上架商品
     * @param liveRoom
     * @param taobaoAccount
     * @param productId
     * @return
     */
    public R publishProductWeb(LiveRoom liveRoom, TaobaoAccount taobaoAccount, Product product);

    /**
     * 访问商品的详细页面
     * @param liveRoom
     * @param taobaoAccount
     * @param product
     * @return
     */
    public R openProduct(LiveRoom liveRoom, TaobaoAccount taobaoAccount, Product product);

    /**
     * 开始讲解
     * @param liveRoom
     * @param taobaoAccount
     * @param productId
     * @return
     */
    public R addTimestamp(LiveRoom liveRoom, TaobaoAccount taobaoAccount, String productId);
}
