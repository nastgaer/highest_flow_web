package highest.flow.taobaolive.taobao.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.SiteConfig;
import highest.flow.taobaolive.common.http.cookie.DefaultCookieStorePool;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.utils.CommonUtils;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.defines.RankingScore;
import highest.flow.taobaolive.taobao.entity.*;
import highest.flow.taobaolive.taobao.service.RankingService;
import highest.flow.taobaolive.taobao.service.SignService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import org.apache.http.HttpStatus;
import org.apache.http.cookie.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("taobaoApiDemoService")
public class TaobaoApiDemoServiceImpl implements TaobaoApiService {

    @Autowired
    private SignService signService;

    @Autowired
    private RankingService rankingService;

    @Override
    public R getUserSimple(TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R autoLogin(TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R getLoginQRCodeURL() {
        return null;
    }

    @Override
    public R getLoginQRCodeCsrfToken(TaobaoAccountEntity taobaoAccountEntity, QRCode qrCode) {
        return null;
    }

    @Override
    public R authQRCode(TaobaoAccountEntity taobaoAccountEntity, QRCode qrCode, String csrfToken) {
        return null;
    }

    @Override
    public R checkLoginByQRCode(TaobaoAccountEntity taobaoAccountEntity, QRCode qrCode) {
        return null;
    }

    @Override
    public R postpone(TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R getUmtidToken() {
        return null;
    }

    @Override
    public R getH5Token(TaobaoAccountEntity taobaoAccountEntity) {
        try {
            String subUrl = "mtop.taobao.baichuan.smb.get";

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("appKey", "21523971");
            paramMap.put("t", "");
            paramMap.put("sign", "");
            paramMap.put("api", subUrl);
            paramMap.put("v", "1.0");
            paramMap.put("type", "originaljson");
            paramMap.put("dataType", "jsonp");
            paramMap.put("timeout", "10000");

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/1.0/?";
            for (String key : paramMap.keySet()) {
                url += key + "=" + URLEncoder.encode(HFStringUtils.valueOf(paramMap.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            List<Cookie> cookies = response.getCookieStore().getCookies();
            taobaoAccountEntity.mergeCookies(cookies);

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getNewDeviceId(TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R parseTaoCode(String taocode) {
        return null;
    }

    @Override
    public R getLiveDetail(String liveId, TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R getLivePreGet(String liveId) {
        return null;
    }

    @Override
    public R getLiveDetailWeb(String liveId, TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R getLiveProducts(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity, int count) {
        if (liveRoomEntity.getProducts().size() > 0) {
            return R.ok();
        }

        for (int idx = 0; idx < count; idx++) {
            ProductEntity productEntity = new ProductEntity();
            productEntity.setProductId(CommonUtils.randomNumeric(8));
            productEntity.setTitle("商品_" + productEntity.getProductId());

            liveRoomEntity.getProducts().add(productEntity);
        }
        return R.ok();
    }

    @Override
    public R getLiveProductsWeb(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity, int count) {
        return this.getLiveProducts(liveRoomEntity, taobaoAccountEntity, count);
    }

    @Override
    public R getRankingListData(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
        if (!liveRoomEntity.isHasRankingListEntry()) {
            liveRoomEntity.setHasRankingListEntry(true);
            liveRoomEntity.getRankingListData().setRankingScore(0);
            liveRoomEntity.getRankingListData().setRankingNum(10);
            liveRoomEntity.getRankingListData().setRankingName("人气主播");
        }

        if (!liveRoomEntity.isHasHourRankingListEntry()) {
            liveRoomEntity.setHasHourRankingListEntry(true);
            liveRoomEntity.getHourRankingListData().setRankingScore(0);
            liveRoomEntity.getHourRankingListData().setRankingNum(10);
            liveRoomEntity.getHourRankingListData().setRankingName("人气主播");
        }
        return R.ok();
    }

    @Override
    public R getLiveEntry(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R getLiveEntryWeb(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R getRankByMtop2(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R getLiveInfo(String taocode, TaobaoAccountEntity taobaoAccountEntity) {
        LiveRoomEntity liveRoomEntity = new LiveRoomEntity();
        liveRoomEntity.setLiveId(CommonUtils.randomNumeric(8));
        liveRoomEntity.setAccountId(CommonUtils.randomNumeric(12));
        return R.ok().put("live_room", liveRoomEntity);
    }

    @Override
    public R getLiveListWeb(TaobaoAccountEntity taobaoAccountEntity, int pageNo, int pageSize) {
        return null;
    }

    @Override
    public R getPlayingLiveRoom(TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R getSigninWeb(TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R routeToNewPlanWeb(TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R checkInStatusWeb(TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R getIntimacyDetail(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R taskCompleteWeb(TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R taskFollow(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> mapTrackParams = new HashMap<>();
            mapTrackParams.put("activityId", liveRoomEntity.getLiveId());
            mapTrackParams.put("broadcasterId", liveRoomEntity.getAccountId());
            mapTrackParams.put("userId", taobaoAccountEntity.getUid());

            Map<String, Object> mapParams = new HashMap<>();
            mapParams.put("accountId", liveRoomEntity.getAccountId());

            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("scopeId", liveRoomEntity.getHierarchyData().getScopeId());
            jsonParams.put("subScope", liveRoomEntity.getHierarchyData().getSubScopeId());
            jsonParams.put("trackParams", objectMapper.writeValueAsString(mapTrackParams));
            jsonParams.put("action", "follow");
            jsonParams.put("params", objectMapper.writeValueAsString(mapParams));

            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "mtop.taobao.iliad.task.action";
            String url = "https://acs.m.taobao.com/gw/" + subUrl + "/1.0/?data=" + URLEncoder.encode(jsonText);

            XHeader xHeader = new XHeader(new Date());
            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("1.0");
            xHeader.setData(jsonText);
            xHeader.setXsign(signService.xsign(xHeader));

            int score = liveRoomEntity.getHourRankingListData().getRankingScore();
            score += rankingService.getRankingUnitScore(RankingScore.Follow);
            liveRoomEntity.getHourRankingListData().setRankingScore(score);

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R taskFollowWeb(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R taskStay(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity, int staySeconds) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> mapTrackParams = new HashMap<>();
            mapTrackParams.put("activityId", liveRoomEntity.getLiveId());
            mapTrackParams.put("broadcasterId", liveRoomEntity.getAccountId());
            mapTrackParams.put("userId", taobaoAccountEntity.getUid());

            Map<String, Object> mapParams = new HashMap<>();
            mapParams.put("stayTime", staySeconds);

            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("scopeId", liveRoomEntity.getHierarchyData().getScopeId());
            jsonParams.put("subScope", liveRoomEntity.getHierarchyData().getSubScopeId());
            jsonParams.put("trackParams", objectMapper.writeValueAsString(mapTrackParams));
            jsonParams.put("action", "stay");
            jsonParams.put("params", objectMapper.writeValueAsString(mapParams));

            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "mtop.taobao.iliad.task.action";
            String url = "https://acs.m.taobao.com/gw/" + subUrl + "/1.0/?data=" + URLEncoder.encode(jsonText);

            XHeader xHeader = new XHeader(new Date());
            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("1.0");
            xHeader.setData(jsonText);
            xHeader.setXsign(signService.xsign(xHeader));

            int score = liveRoomEntity.getHourRankingListData().getRankingScore();
            score += 1; // rankingService.getRankingUnitScore(RankingScore.Stay);
            liveRoomEntity.getHourRankingListData().setRankingScore(score);

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R taskStayWeb(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity, int staySeconds) {
        return null;
    }

    @Override
    public R taskBuy(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity, String productId) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> mapTrackParams = new HashMap<>();
            mapTrackParams.put("activityId", liveRoomEntity.getLiveId());
            mapTrackParams.put("broadcasterId", liveRoomEntity.getAccountId());
            mapTrackParams.put("userId", taobaoAccountEntity.getUid());

            Map<String, Object> mapParams = new HashMap<>();
            mapParams.put("itemId", productId);
            mapParams.put("cost", 50);

            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("scopeId", liveRoomEntity.getHierarchyData().getScopeId());
            jsonParams.put("subScope", liveRoomEntity.getHierarchyData().getSubScopeId());
            jsonParams.put("trackParams", objectMapper.writeValueAsString(mapTrackParams));
            jsonParams.put("action", "payCarts");
            jsonParams.put("params", objectMapper.writeValueAsString(mapParams));

            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "mtop.taobao.iliad.task.action";
            String url = "https://acs.m.taobao.com/gw/" + subUrl + "/1.0/?data=" + URLEncoder.encode(jsonText);

            XHeader xHeader = new XHeader(new Date());
            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("1.0");
            xHeader.setData(jsonText);
            xHeader.setXsign(signService.xsign(xHeader));

            int score = liveRoomEntity.getHourRankingListData().getRankingScore();
            score += rankingService.getRankingUnitScore(RankingScore.Buy);
            liveRoomEntity.getHourRankingListData().setRankingScore(score);

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R taskBuyWeb(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity, String productId) {
        return null;
    }

    @Override
    public R createLiveRoomWeb(PreLiveRoomSpec preLiveRoomSpec, TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R createLiveRoom(PreLiveRoomSpec preLiveRoomSpec, TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R startLive(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R stopLive(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R deleteLive(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R getProductItemInfo(TaobaoAccountEntity taobaoAccountEntity, String productId) {
        return null;
    }

    @Override
    public R publishProductWeb(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity, ProductEntity productEntity) {
        return null;
    }

    @Override
    public R openProduct(TaobaoAccountEntity taobaoAccountEntity, String productId) {
        return null;
    }

    @Override
    public R addTimestamp(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity, String productId) {
        return null;
    }

    @Override
    public R uploadImage(Path path, TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public String parseProductId(String url) {
        return null;
    }
}
