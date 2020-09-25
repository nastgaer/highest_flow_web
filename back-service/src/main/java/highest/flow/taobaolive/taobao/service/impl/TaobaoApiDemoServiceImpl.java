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
import highest.flow.taobaolive.common.utils.NumberUtils;
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

import java.net.URLDecoder;
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
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("passwordContent", taocode);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "mtop.taobao.sharepassword.querypassword";
            String url = "https://acs.m.taobao.com/gw/" + subUrl + "/1.0/?data=" + URLEncoder.encode(jsonText);

            XHeader xHeader = new XHeader(new Date());
            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("1.0");
            xHeader.setData(jsonText);
            xHeader.setXsign(signService.xsign(xHeader));

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("MTOPSDK%2F3.1.1.7+%28Android%3B5.1.1%3Bsamsung%3BSM-J120F%29")
                            .setContentType("application/x-www-form-urlencoded;charset=UTF-8")
                            .addHeaders(xHeader.getHeaders()),
                    new Request("GET", url, ResponseType.TEXT));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorCode(), taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            String creatorId = HFStringUtils.valueOf(mapData.get("taopwdOwnerId"));
            String talentLiveUrl = HFStringUtils.valueOf(mapData.get("url"));
            String liveId = "";
            String liveUrl = URLDecoder.decode(talentLiveUrl);
            String[] words = liveUrl.split("[::?&/]");
            for (String word : words) {
                if (word.startsWith("id=")) {
                    liveId = word.substring("id=".length());
                    break;
                }
            }
            String accountId = "";
            int pos = liveUrl.indexOf("\"account_id\":");
            if (pos >= 0) {
                pos += "\"account_id\":".length();
                int nextpos = liveUrl.indexOf("\"", pos + 1);
                accountId = liveUrl.substring(pos + 1, nextpos - 1);
            }
            String content = HFStringUtils.valueOf(mapData.get("content"));
            String accountName = content;
            pos = content.indexOf("的直播");
            if (pos >= 0) {
                accountName = content.substring(0, pos);
            }

            return R.ok()
                    .put("creator_id", creatorId)
                    .put("talent_live_url", talentLiveUrl)
                    .put("live_id", liveId)
                    .put("account_id", accountId)
                    .put("account_name", accountName);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getAnchorInfo(String creatorId) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("broadcasterId", creatorId);
            jsonParams.put("start", 0);
            jsonParams.put("limit", 10);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "mtop.mediaplatform.anchor.info";
            String url = "https://acs.m.taobao.com/gw/" + subUrl + "/1.0/?data=" + URLEncoder.encode(jsonText);

            XHeader xHeader = new XHeader(new Date());
            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("1.0");
            xHeader.setData(jsonText);
            xHeader.setXsign(signService.xsign(xHeader));

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("MTOPSDK%2F3.1.1.7+%28Android%3B5.1.1%3Bsamsung%3BSM-J120F%29")
                            .setContentType("application/x-www-form-urlencoded;charset=UTF-8")
                            .addHeaders(xHeader.getHeaders()),
                    new Request("GET", url, ResponseType.TEXT));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorCode(), taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            Map<String, Object> mapLiveVideo = (Map<String, Object>) mapData.get("liveVideo");
            String liveId = mapLiveVideo == null ? "" : (String)mapLiveVideo.get("liveId");

            return R.ok()
                    .put("live_id", liveId);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getLiveDetail(String liveId, TaobaoAccountEntity taobaoAccountEntity) {
        return null;
    }

    @Override
    public R getLivePreGet(String liveId) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("feedId", liveId);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "mtop.mediaplatform.live.pre.get";
            String url = "https://acs.m.taobao.com/gw/" + subUrl + "/2.0/?data=" + URLEncoder.encode(jsonText);

            XHeader xHeader = new XHeader(new Date());
            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("2.0");
            xHeader.setData(jsonText);
            xHeader.setXsign(signService.xsign(xHeader));

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("MTOPSDK%2F3.1.1.7+%28Android%3B5.1.1%3Bsamsung%3BSM-J120F%29")
                            .setContentType("application/x-www-form-urlencoded;charset=UTF-8")
                            .addHeaders(xHeader.getHeaders()),
                    new Request("GET", url, ResponseType.TEXT));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error("解析直播间信息失败");
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorCode(), taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            Map<String, Object> mapBroadCaster = (Map<String, Object>) mapData.get("broadCaster");
            String accountId = HFStringUtils.valueOf(mapBroadCaster.get("accountId"));
            String accountName = HFStringUtils.valueOf(mapBroadCaster.get("accountName"));
            int fansNum = NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapBroadCaster.get("fansNum"))));

            String topic = HFStringUtils.valueOf(mapData.get("topic"));
            int viewCount = NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapData.get("viewCount"))));
            int praiseCount = NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapData.get("praiseCount"))));
            int onlineCount = NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapData.get("joinCount"))));

            long startTimestamp = NumberUtils.valueOf(NumberUtils.parseLong(HFStringUtils.valueOf(mapData.get("startTime"))));
            Date startTime = CommonUtils.timestampToDate(startTimestamp);
            String coverImg = HFStringUtils.valueOf(mapData.get("coverImg"));
            String coverImg169 = HFStringUtils.valueOf(mapData.get("coverImg169"));
            String title = HFStringUtils.valueOf(mapData.get("title"));
            String intro = HFStringUtils.valueOf(mapData.get("descInfo"));
            int channelId = NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapData.get("liveChannelId"))));
            int columnId = NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapData.get("liveColumnId"))));
            String location = HFStringUtils.valueOf(mapData.get("location"));
            int roomStatus = NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapData.get("roomStatus"))));

            return R.ok()
                    .put("account_id", accountId)
                    .put("account_name", accountName)
                    .put("fans_num", fansNum)
                    .put("topic", topic)
                    .put("view_count", viewCount)
                    .put("praise_count", praiseCount)
                    .put("online_count", onlineCount)
                    .put("start_time", startTime)
                    .put("cover_img", coverImg)
                    .put("cover_img169", coverImg169)
                    .put("title", title)
                    .put("intro", intro)
                    .put("channel_id", channelId)
                    .put("column_id", columnId)
                    .put("location", location)
                    .put("room_status", roomStatus);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
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
        try {
            R r = this.parseTaoCode(taocode);
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            String liveId = (String) r.get("live_id");
            String creatorId = (String) r.get("creator_id");
            String talentLiveUrl = (String) r.get("talent_live_url");
            if (HFStringUtils.isNullOrEmpty(liveId)) {
                r = this.getAnchorInfo(creatorId);
                if (r.getCode() != ErrorCodes.SUCCESS) {
                    return r;
                }
                liveId = (String) r.get("live_id");
            }

            if (HFStringUtils.isNullOrEmpty(liveId)) {
                return R.error("没有直播内容");
            }

            LiveRoomEntity liveRoomEntity = new LiveRoomEntity();
            liveRoomEntity.setLiveId(liveId);
            liveRoomEntity.setCreatorId(creatorId);
            liveRoomEntity.setTalentLiveUrl(talentLiveUrl);

            r = this.getLivePreGet(liveId);
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            liveRoomEntity.setAccountId((String) r.get("account_id"));
            liveRoomEntity.setAccountName((String) r.get("account_name"));
            liveRoomEntity.setFansNum((int) r.get("fans_num"));
            liveRoomEntity.setTopic((String) r.get("topic"));
            liveRoomEntity.setViewCount((int) r.get("view_count"));
            liveRoomEntity.setPraiseCount((int) r.get("praise_count"));
            liveRoomEntity.setOnlineCount((int) r.get("online_count"));
            liveRoomEntity.setLiveCoverImg((String) r.get("cover_img"));
            liveRoomEntity.setLiveCoverImg169((String) r.get("cover_img169"));
            liveRoomEntity.setLiveTitle((String) r.get("title"));
            liveRoomEntity.setLiveIntro((String) r.get("intro"));
            liveRoomEntity.setLiveChannelId((int) r.get("channel_id"));
            liveRoomEntity.setLiveColumnId((int) r.get("column_id"));
            liveRoomEntity.setLiveLocation((String) r.get("location"));

            this.getRankingListData(liveRoomEntity, taobaoAccountEntity);

            return R.ok().put("live_room", liveRoomEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
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
    public R intimacyTracker(TaobaoAccountEntity taobaoAccountEntity) {
        return R.ok();
    }

    @Override
    public R logTrackerJavascript(TaobaoAccountEntity taobaoAccountEntity) {
        return R.ok();
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
