package highest.flow.taobaolive.taobao.service;

import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.entity.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;

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
    public R getLiveDetail(String liveId, TaobaoAccountEntity taobaoAccountEntity);

    /**
     * 获取直播间详细信息
     * @param liveId
     * @return
     */
    public R getLivePreGet(String liveId);

    /**
     * 获取直播间详细信息
     * @param liveId
     * @param taobaoAccountEntity
     * @return
     */
    public R getLiveDetailWeb(String liveId, TaobaoAccountEntity taobaoAccountEntity);

    /**
     * 获取直播间商品
     * @param liveRoomEntity
     * @param taobaoAccountEntity
     * @return
     */
    public R getLiveProducts(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity);

    /**
     * 查询直播间赛道
     * @param liveRoomEntity
     * @param taobaoAccountEntity
     * @return
     */
    public R getLiveEntry(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity);

    /**
     * 解析淘口令获取直播间信息
     * @param taocode
     * @param taobaoAccountEntity
     * @return
     */
    public R getLiveInfo(String taocode, TaobaoAccountEntity taobaoAccountEntity);

    /**
     * 获取直播列表
     * @param taobaoAccountEntity
     * @param pageNo
     * @param pageSize
     * @return
     */
    public R getLiveList(TaobaoAccountEntity taobaoAccountEntity, int pageNo, int pageSize);

    public R getPlayingLiveRoom(TaobaoAccountEntity taobaoAccountEntity);

    /**
     * 助力：关注任务
     * @param liveRoomEntity
     * @param taobaoAccountEntity
     * @return
     */
    public R taskFollow(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity);

    /**
     * 助力：在直播间等待几分钟
     * @param liveRoomEntity
     * @param taobaoAccountEntity
     * @param staySeconds
     * @return
     */
    public R taskStay(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity, int staySeconds);

    /**
     * 助力：购买商品
     * @param liveRoomEntity
     * @param taobaoAccountEntity
     * @param productId
     * @return
     */
    public R taskBuy(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity, String productId);

    /**
     * 发布预告（Web)
     * @param taobaoAccountEntity
     * @param preLiveRoomSpec
     * @return
     */
    public R createLiveRoomWeb(PreLiveRoomSpec preLiveRoomSpec, TaobaoAccountEntity taobaoAccountEntity);

    /**
     * 发布预告（Mobile)
     * @param taobaoAccountEntity
     * @param preLiveRoomSpec
     * @return
     */
    public R createLiveRoom(PreLiveRoomSpec preLiveRoomSpec, TaobaoAccountEntity taobaoAccountEntity);

    /**
     * 开始直播
     * @param liveRoomEntity
     * @param taobaoAccountEntity
     * @return
     */
    public R startLive(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity);

    /**
     * 结束直播
     * @param liveRoomEntity
     * @param taobaoAccountEntity
     * @return
     */
    public R stopLive(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity);

    /**
     * 删除回放
     * @param liveRoomEntity
     * @param taobaoAccountEntity
     * @return
     */
    public R deleteLive(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity);

    /**
     * 获取商品信息
     * @param taobaoAccountEntity
     * @param productId
     * @return
     */
    public R getProductItemInfo(TaobaoAccountEntity taobaoAccountEntity, String productId);

    /**
     * 在直播间上架商品
     * @param liveRoomEntity
     * @param taobaoAccountEntity
     * @param productEntity
     * @return
     */
    public R publishProductWeb(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity, ProductEntity productEntity);

    /**
     * 访问商品的详细页面
     * @param taobaoAccountEntity
     * @param productId
     * @return
     */
    public R openProduct(TaobaoAccountEntity taobaoAccountEntity, String productId);

    /**
     * 开始讲解
     * @param liveRoomEntity
     * @param taobaoAccountEntity
     * @param productId
     * @return
     */
    public R addTimestamp(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity, String productId);

    public R uploadImage(Path path, TaobaoAccountEntity taobaoAccountEntity);

    public String parseProductId(String url);
}
