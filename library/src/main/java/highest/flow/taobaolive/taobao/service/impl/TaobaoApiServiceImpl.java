package highest.flow.taobaolive.taobao.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.http.*;
import highest.flow.taobaolive.common.http.cookie.DefaultCookieStorePool;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.utils.CommonUtils;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.*;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import highest.flow.taobaolive.taobao.service.SignService;
import highest.flow.taobaolive.taobao.utils.DeviceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.cookie.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

@Service("taobaoApiService")
public class TaobaoApiServiceImpl implements TaobaoApiService {

    @Autowired
    private SignService signService;

    @Override
    public R getUserSimple(TaobaoAccount taobaoAccount) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("data", "{}");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "mtop.user.getusersimple";
            String url = "https://api.m.taobao.com/gw/" + subUrl + "/1.0/?data=" + URLEncoder.encode(jsonText);

            XHeader xHeader = new XHeader(taobaoAccount);
            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("1.0");
            xHeader.setData(jsonText);
            xHeader.setXsign(signService.xsign(xHeader));

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                        .setUserAgent("MTOPSDK/3.1.1.7 (Android;5.1.1)")
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
                return R.error(taobaoReturn.getErrorMsg());
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
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
                            .setUserAgent("MTOPSDK/3.1.1.7 (Android;5.1.1)")
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
                return R.error(taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            String creatorId = (String) mapData.get("taopwdOwnerId");
            String talentLiveUrl = (String) mapData.get("url");
            String liveId = "";
            String[] words = talentLiveUrl.split("[::?&/]");
            for (String word : words) {
                if (word.startsWith("id=")) {
                    liveId = word.substring("id=".length());
                    break;
                }
            }

            return R.ok()
                    .put("creatorId", creatorId)
                    .put("talentLiveUrl", talentLiveUrl)
                    .put("liveId", liveId);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getLiveDetail(String liveId) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("ignoreH265", "false");
            jsonParams.put("liveId", liveId);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "mtop.mediaplatform.live.livedetail";
            String url = "https://acs.m.taobao.com/gw/" + subUrl + "/3.0/?data=" + URLEncoder.encode(jsonText);

            XHeader xHeader = new XHeader(new Date());
            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("3.0");
            xHeader.setData(jsonText);
            xHeader.setXsign(signService.xsign(xHeader));

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("MTOPSDK/3.1.1.7 (Android;5.1.1)")
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
                return R.error(taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            Map<String, Object> mapBroadCaster = (Map<String, Object>) mapData.get("broadCaster");
            String accountId = (String) mapBroadCaster.get("accountId");
            String accountName = (String) mapBroadCaster.get("accountName");
            int fansNum = (int) mapBroadCaster.get("fansNum");

            String topic = (String) mapData.get("topic");
            int viewCount = (int) mapData.get("viewCount");
            int praiseCount = (int) mapData.get("praiseCount");
            int onlineCount = (int) mapData.get("joinCount");

            long startTimestamp = (int) mapData.get("startTime");
            Date startTime = CommonUtils.timestampToDate(startTimestamp);
            String coverImg = (String) mapData.get("coverImg");
            String coverImg169 = (String) mapData.get("coverImg169");
            String title = (String) mapData.get("title");
            String intro = (String) mapData.get("descInfo");
            int channelId = (int) mapData.get("liveChannelId");
            int columnId = (int) mapData.get("liveColumnId");
            String location = (String) mapData.get("location");

            return R.ok()
                    .put("accountId", accountId)
                    .put("accountName", accountName)
                    .put("fansNum", fansNum)
                    .put("topic", topic)
                    .put("viewCount", viewCount)
                    .put("praiseCount", praiseCount)
                    .put("onlineCount", onlineCount)
                    .put("startTime", startTime)
                    .put("coverImg", coverImg)
                    .put("coverImg169", coverImg169)
                    .put("title", title)
                    .put("intro", intro)
                    .put("channelId", channelId)
                    .put("columnId", columnId)
                    .put("location", location);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getLiveProducts(LiveRoom liveRoom, TaobaoAccount taobaoAccount) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("groupNum", "0");
            jsonParams.put("liveId", liveRoom.getId());

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            H5Header h5Header = new H5Header(taobaoAccount);
            String subUrl = "mtop.mediaplatform.video.livedetail.itemlist.withpagination";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", String.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "4.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/4.0/?";

            for (String key : jsonParams.keySet()) {
                url += key + "=" + URLEncoder.encode((String) jsonParams.get(key)) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccount.getCookieStore()));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            int totalNum = (int) mapData.get("totalNum");

            List<Product> products = new ArrayList<>();
            List<Map<String, Object>> itemList = (List<Map<String, Object>>) mapData.get("itemList");
            for (Map<String, Object> mapItem : itemList) {
                List<Map<String, Object>> goodsList = (List<Map<String, Object>>) mapItem.get("goodsList");
                if (goodsList.size() < 1) {
                    continue;
                }

                for (Map<String, Object> goodObj : goodsList) {
                    String itemId = (String) goodObj.get("itemId");
                    String itemName = (String) goodObj.get("itemName");
                    String itemPic = (String) goodObj.get("itemPic");
                    String itemPrice = (String) goodObj.get("itemPrice");
                    String itemUrl = (String) goodObj.get("itemUrl");

                    Product product = new Product();
                    product.setProductId(itemId);
                    product.setTitle(itemName);
                    product.setPicurl(itemPic);
                    product.setUrl(itemUrl);
                    product.setPrice(itemPrice);

                    Map<String, Object> mapExtendVal = (Map<String, Object>) goodObj.get("extendVal");
                    if (mapExtendVal != null) {
                        if (mapExtendVal.containsKey("timepoint")) {
                            product.setTimepoint((long) mapExtendVal.get("timepoint"));
                        }

                        String business = (String) mapExtendVal.get("business");
                        Map<String, Object> mapBusiness = jsonParser.parseMap(respText);

                        Map<String, Object> mapCpsTcpInfo = (Map<String, Object>) mapBusiness.get("cpsTcpInfo");
                        Map<String, Object> mapTaobaoLivetoc = (Map<String, Object>) mapCpsTcpInfo.get("taobaolivetoc");
                        product.setBusinessSceneId((int) mapTaobaoLivetoc.get("businessScenceId"));

                        Map<String, Object> mapItemBizInfo = (Map<String, Object>) mapBusiness.get("itemBizInfo");
                        String itemJumpUrl = (String) mapItemBizInfo.get("itemJumpUrl");
                        itemJumpUrl = URLDecoder.decode(itemJumpUrl);

                        String [] words = itemJumpUrl.split("&");
                        for (String word : words) {
                            if (word.toLowerCase().startsWith("pg1stepk=")) {
                                product.setPg1stepk(word.substring("pg1stepk=".length()));
                            } else if (word.toLowerCase().startsWith("liveinfo=")) {
                                product.setLiveInfo(word.substring("liveInfo=".length()));
                            } else if (word.toLowerCase().startsWith("descversion=")) {
                                product.setDescVersion(word.substring("descversion=".length()));
                            } else if (word.toLowerCase().startsWith("scm=")) {
                                product.setScm(word.substring("scm=".length()));
                            } else if (word.toLowerCase().startsWith("spm=")) {
                                product.setSpm(word.substring("spm=".length()));
                            } else if (word.toLowerCase().startsWith("utparam=")) {
                                product.setUtparam(word.substring("utparam=".length()));
                            } else if (word.toLowerCase().startsWith("biztype=")) {
                                product.setBizType(word.substring("biztype=".length()));
                            }
                        }
                    }

                    products.add(product);
                }
            }

            liveRoom.setProducts(products);

            return R.ok()
                    .put("products", products)
                    .put("totalNum", totalNum);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getLiveEntry(LiveRoom liveRoom, TaobaoAccount taobaoAccount) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("accountId", liveRoom.getAccountId());
            jsonParams.put("liveId", liveRoom.getLiveId());

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            H5Header h5Header = new H5Header(taobaoAccount);
            String subUrl = "mtop.mediaplatform.livedetail.entry";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", String.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "1.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/1.0/?";

            for (String key : jsonParams.keySet()) {
                url += key + "=" + URLEncoder.encode((String) jsonParams.get(key)) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccount.getCookieStore()));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            liveRoom.setHasRankingEntry((boolean) mapData.get("hasRankingListEntry"));
            if (!liveRoom.isHasRankingEntry()) {
                liveRoom.setRankingScore(0);
                liveRoom.setRankingNum(0);
                liveRoom.setRankingName("");

            } else {
                Map<String, Object> mapRankingListData = (Map<String, Object>) mapData.get("rankingListData");
                Map<String, Object> mapBizData = (Map<String, Object>) mapRankingListData.get("bizData");
                liveRoom.setRankingScore((int) mapBizData.get("score"));
                liveRoom.setRankingNum((int) mapBizData.get("rankNum"));
                liveRoom.setRankingName((String) mapBizData.get("name"));
            }

            Map<String, Object> mapHierachyData = (Map<String, Object>) mapData.get("hierarchyData");
            liveRoom.setScopeId(mapHierachyData == null ? "-1" : (String) mapHierachyData.get("scopeId"));
            liveRoom.setSubScopeId(mapHierachyData == null ? "-1" : (String) mapHierachyData.get("subScopeId"));

            return R.ok()
                    .put("liveRoom", liveRoom);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R taskFollow(LiveRoom liveRoom, TaobaoAccount taobaoAccount) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> mapTrackParams = new HashMap<>();
            mapTrackParams.put("activityId", liveRoom.getLiveId());
            mapTrackParams.put("broadcasterId", liveRoom.getAccountId());
            mapTrackParams.put("userId", taobaoAccount.getUid());

            Map<String, Object> mapParams = new HashMap<>();
            mapParams.put("accountId", liveRoom.getAccountId());

            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("scopeId", liveRoom.getScopeId());
            jsonParams.put("subScope", liveRoom.getSubScopeId());
            jsonParams.put("trackParams", objectMapper.writeValueAsString(mapTrackParams));
            jsonParams.put("action", "follow");
            jsonParams.put("params", objectMapper.writeValueAsString(mapParams));

            String jsonText = objectMapper.writeValueAsString(jsonParams);

            H5Header h5Header = new H5Header(taobaoAccount);
            String subUrl = "mtop.taobao.iliad.task.action";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", String.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "1.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/1.0/?";

            for (String key : jsonParams.keySet()) {
                url += key + "=" + URLEncoder.encode((String) jsonParams.get(key)) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccount.getCookieStore()));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorMsg());
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R taskStay(LiveRoom liveRoom, TaobaoAccount taobaoAccount, int staySeconds) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> mapTrackParams = new HashMap<>();
            mapTrackParams.put("activityId", liveRoom.getLiveId());
            mapTrackParams.put("broadcasterId", liveRoom.getAccountId());
            mapTrackParams.put("userId", taobaoAccount.getUid());

            Map<String, Object> mapParams = new HashMap<>();
            mapParams.put("stayTime", staySeconds);

            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("scopeId", liveRoom.getScopeId());
            jsonParams.put("subScope", liveRoom.getSubScopeId());
            jsonParams.put("trackParams", objectMapper.writeValueAsString(mapTrackParams));
            jsonParams.put("action", "stay");
            jsonParams.put("params", objectMapper.writeValueAsString(mapParams));

            String jsonText = objectMapper.writeValueAsString(jsonParams);

            H5Header h5Header = new H5Header(taobaoAccount);
            String subUrl = "mtop.taobao.iliad.task.action";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", String.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "1.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/1.0/?";

            for (String key : jsonParams.keySet()) {
                url += key + "=" + URLEncoder.encode((String) jsonParams.get(key)) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccount.getCookieStore()));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorMsg());
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R taskBuy(LiveRoom liveRoom, TaobaoAccount taobaoAccount, String productId) {
        try {


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R createLiveRoomWeb(PreLiveRoomSpec preLiveRoomSpec, TaobaoAccount taobaoAccount) {
        try {
            String url = "https://liveplatform.taobao.com/live/action.do?api=publish_pre_live&_tb_token_=" + taobaoAccount.getToken() + "&_input_charset=utf-8";
            String refererUrl = "https://liveplatform.taobao.com/live/addLive.htm";

            Map<String, String> postParams = new HashMap<>();
            postParams.put("landscape", "false");
            postParams.put("title", URLEncoder.encode(preLiveRoomSpec.getTitle()));
            postParams.put("descInfo", URLEncoder.encode(preLiveRoomSpec.getIntro()));
            postParams.put("coverImg", preLiveRoomSpec.getCoverImg().replace("https:", ""));
            postParams.put("coverImg169", preLiveRoomSpec.getCoverImg169().replace("https:", ""));
            postParams.put("coverImg916", "null");
            postParams.put("uploadId", "undefined");
            postParams.put("roomType", "0");
            postParams.put("liveChannelId", String.valueOf(preLiveRoomSpec.getChannelId()));
            postParams.put("liveColumnId", String.valueOf(preLiveRoomSpec.getColumnId()));
            postParams.put("syncYouku", "false");
            postParams.put("useLcps", "false");
            postParams.put("itemList", "[]");
            postParams.put("lbsPoiId", "");
            postParams.put("longitude", "131.9133");
            postParams.put("latitude", "43.1654");
            postParams.put("countryName", "");
            postParams.put("provinceName", "");
            postParams.put("cityName", "");
            postParams.put("districtName", "");
            postParams.put("countryCode", "");
            postParams.put("provinceCode", "");
            postParams.put("cityCode", "");
            postParams.put("districtCode", "");
            postParams.put("addressDetail", preLiveRoomSpec.getLocation());
            postParams.put("appId", "");
            postParams.put("notice", "");
            postParams.put("country", "");
            postParams.put("province", "");
            postParams.put("city", URLEncoder.encode(preLiveRoomSpec.getLocation()));
            postParams.put("appointmentTime", String.valueOf(CommonUtils.dateToTimestamp(preLiveRoomSpec.getStartTime())));
            postParams.put("liveEndTime", String.valueOf(CommonUtils.dateToTimestamp(CommonUtils.addDays(preLiveRoomSpec.getStartTime(), 30))));

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                            .addHeader("Referer", refererUrl)
                            .addHeader("Content-Type", "application/x-www-form-urlencoded"),
                    new Request("POST", url, ResponseType.TEXT)
                            .setParameters(postParams),
                    new DefaultCookieStorePool(taobaoAccount.getCookieStore()));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapModel = (Map<String, Object>) map.get("model");

            LiveRoom liveRoom = new LiveRoom();
            liveRoom.setLiveId((String) mapModel.get("preLiveId"));
            liveRoom.setStartTime(preLiveRoomSpec.getStartTime());
            liveRoom.setCoverImg(preLiveRoomSpec.getCoverImg());
            liveRoom.setCoverImg169(preLiveRoomSpec.getCoverImg169());
            liveRoom.setTitle(preLiveRoomSpec.getTitle());
            liveRoom.setIntro(preLiveRoomSpec.getIntro());
            liveRoom.setChannelId(preLiveRoomSpec.getChannelId());
            liveRoom.setColumnId(preLiveRoomSpec.getColumnId());
            liveRoom.setLocation(preLiveRoomSpec.getLocation());

            return R.ok()
                    .put("liveRoom", liveRoom);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R createLiveRoom(PreLiveRoomSpec preLiveRoomSpec, TaobaoAccount taobaoAccount) {
        try {
            Map<String, String> jsonParams = new HashMap<>();
            jsonParams.put("coverImg", preLiveRoomSpec.getCoverImg().replace("https:", ""));
            jsonParams.put("coverImg169", preLiveRoomSpec.getCoverImg169().replace("https:", ""));
            jsonParams.put("appointmentTime", String.valueOf(CommonUtils.dateToTimestamp(preLiveRoomSpec.getStartTime())));
            jsonParams.put("title", preLiveRoomSpec.getTitle());
            jsonParams.put("descInfo", preLiveRoomSpec.getIntro());
            jsonParams.put("itemIds", "");
            jsonParams.put("liveChannelId", String.valueOf(preLiveRoomSpec.getChannelId()));
            jsonParams.put("liveColumnId", String.valueOf(preLiveRoomSpec.getColumnId()));
            jsonParams.put("location", preLiveRoomSpec.getLocation());
            jsonParams.put("landScape", "false");
            jsonParams.put("useLcps", "false");
            jsonParams.put("tidbitsUrl", "");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "mtop.mediaplatform.live.create";
            String url = "https://acs.m.taobao.com/gw/" + subUrl + "/3.0/?data=" + URLEncoder.encode(jsonText);

            XHeader xHeader = new XHeader(taobaoAccount);
            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("3.0");
            xHeader.setData(jsonText);
            xHeader.setXsign(signService.xsign(xHeader));

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("MTOPSDK/3.1.1.7 (Android;5.1.1)")
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
                return R.error(taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            LiveRoom liveRoom = new LiveRoom();
            liveRoom.setLiveId((String) mapData.get("liveId"));
            liveRoom.setAccountId( (String) mapData.get("accountId"));
            liveRoom.setTopic((String) mapData.get("topic"));
            liveRoom.setViewCount((int) mapData.get("viewCount"));
            liveRoom.setPraiseCount((int) mapData.get("praiseCount"));
            liveRoom.setStartTime(preLiveRoomSpec.getStartTime());
            liveRoom.setCoverImg((String) mapData.get("coverImg"));
            liveRoom.setCoverImg169((String) mapData.get("coverImg169"));
            liveRoom.setTitle((String) mapData.get("title"));
            liveRoom.setIntro((String) mapData.get("intro"));
            liveRoom.setChannelId((int) mapData.get("liveChannelId"));
            liveRoom.setColumnId((int) mapData.get("liveColumnId"));
            liveRoom.setLocation((String) mapData.get("location"));

            return R.ok()
                    .put("liveRoom", liveRoom);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R startLive(LiveRoom liveRoom, TaobaoAccount taobaoAccount) {
        try {
            String url = "https://liveplatform.taobao.com/live/action.do?api=start_live_from_pre&_tb_token_=" + taobaoAccount.getToken();
            String refererUrl = "https://liveplatform.taobao.com/live/addLive.htm";

            Map<String, String> postParams = new HashMap<>();
            postParams.put("liveVideoId", liveRoom.getLiveId());
            postParams.put("accountId", taobaoAccount.getUid());
            postParams.put("location", HFStringUtils.isNullOrEmpty(liveRoom.getLocation()) ? "中国" : liveRoom.getLocation());

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                            .addHeader("Referer", refererUrl)
                            .addHeader("Content-Type", "application/x-www-form-urlencoded"),
                    new Request("POST", url, ResponseType.TEXT)
                            .setParameters(postParams),
                    new DefaultCookieStorePool(taobaoAccount.getCookieStore()));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorMsg());
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R stopLive(LiveRoom liveRoom, TaobaoAccount taobaoAccount) {
        try {
            String url = "https://liveplatform.taobao.com/live/action.do?api=stop_live";
            String refererUrl = "https://liveplatform.taobao.com/live/live_detail.htm?id=" + liveRoom.getLiveId() + "&openHlvPush=true";

            Map<String, String> postParams = new HashMap<>();
            postParams.put("pFeedId", liveRoom.getLiveId());
            postParams.put("_tb_token_", taobaoAccount.getToken());

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                            .addHeader("Referer", refererUrl)
                            .addHeader("Content-Type", "application/x-www-form-urlencoded"),
                    new Request("POST", url, ResponseType.TEXT)
                            .setParameters(postParams),
                    new DefaultCookieStorePool(taobaoAccount.getCookieStore()));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorMsg());
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R deleteLive(LiveRoom liveRoom, TaobaoAccount taobaoAccount) {
        try {
            String url = "https://liveplatform.taobao.com/live/action.do?api=delete_live&liveId=" + liveRoom.getLiveId();
            String refererUrl = "https://liveplatform.taobao.com/live/liveList.htm?spm=a1z9u.8142865.0.0.3d067997jRNQCS";

            Map<String, String> postParams = new HashMap<>();
            postParams.put("pFeedId", liveRoom.getLiveId());
            postParams.put("_tb_token_", taobaoAccount.getToken());

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                            .addHeader("Referer", refererUrl)
                            .addHeader("Content-Type", "application/x-www-form-urlencoded"),
                    new Request("POST", url, ResponseType.TEXT)
                            .setParameters(postParams),
                    new DefaultCookieStorePool(taobaoAccount.getCookieStore()));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorMsg());
            }

            return R.ok();


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getProductItemInfo(TaobaoAccount taobaoAccount, String productId) {
        try {
            String url = "https://liveplatform.taobao.com/live/action.do?api=item_getItem";
            String refererUrl = "https://liveplatform.taobao.com/live/liveDetail.htm?spm=a1z9u.8142865.0.0.1cb534edxcANsb&id=261569222510";

            String suburl = "https://item.taobao.com/item.htm?ft=t&id=" + productId;
            url += "&api=item_getItem&url=" + URLEncoder.encode(suburl) + "&_=" + productId;

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                            .addHeader("Referer", refererUrl)
                            .addHeader("Content-Type", "application/x-www-form-urlencoded"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccount.getCookieStore()));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapModel = (Map<String, Object>) map.get("model");
            String imgUrl = (String) mapModel.get("imgUrl");
            String title = (String) mapModel.get("itemTitle");
            String price = (String) mapModel.get("itemPrice");

            return R.ok()
                    .put("productId", productId)
                    .put("imgUrl", imgUrl)
                    .put("title", title)
                    .put("price", price);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R publishProductWeb(LiveRoom liveRoom, TaobaoAccount taobaoAccount, Product product) {
        try {
            Map<String, Object> mapNode = new HashMap<>();
            mapNode.put("type", "picItem");
            mapNode.put("path", product.getPicurl().replace("", ""));
            mapNode.put("content", product.getTitle());
            mapNode.put("bizId", product.getProductId());
            mapNode.put("right", "");
            List lstNodes = new ArrayList();
            lstNodes.add(mapNode);

            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("feedType", "502");
            jsonParams.put("roomType", 0);
            jsonParams.put("nodes", lstNodes);
            jsonParams.put("parentId", liveRoom.getLiveId());
            jsonParams.put("feedId", "");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String url = "https://liveplatform.taobao.com/live/action.do?api=publish_content_feed";
            String refererUrl = "https://liveplatform.taobao.com/live/liveDetail.htm?id=" + liveRoom.getLiveId();

            Map<String, String> postParams = new HashMap<>();
            postParams.put("_input_charset", "utf-8");
            postParams.put("draft", URLEncoder.encode(jsonText));
            postParams.put("_tb_token_", taobaoAccount.getToken());

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                            .addHeader("Referer", refererUrl)
                            .addHeader("Content-Type", "application/x-www-form-urlencoded"),
                    new Request("POST", url, ResponseType.TEXT)
                            .setParameters(postParams),
                    new DefaultCookieStorePool(taobaoAccount.getCookieStore()));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorMsg());
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R openProduct(LiveRoom liveRoom, TaobaoAccount taobaoAccount, Product product) {
        try {
            XHeader xHeader = new XHeader(taobaoAccount);
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> mapExParams = new HashMap<>();
            mapExParams.put("__application_id__", "taobaolive");
            mapExParams.put("action", "ipv");
            mapExParams.put("ad_type", "1.0");
            mapExParams.put("appReqFrom", "detail");
            mapExParams.put("container_type", "xdetail");
            mapExParams.put("countryCode", "CN");
            mapExParams.put("cpuCore", "8");
            mapExParams.put("cpuMaxHz", "1001000");
            mapExParams.put("dinamic_v3", "true");
            mapExParams.put("from", "search");
            mapExParams.put("id", product.getProductId());
            mapExParams.put("item_id", product.getProductId());
            mapExParams.put("list_type", "search");
            mapExParams.put("nick", taobaoAccount.getNick());
            mapExParams.put("osVersion", "23");
            mapExParams.put("phoneType", "NCE-AL10");
            mapExParams.put("search_action", "initiative");
            mapExParams.put("soVersion", "2.0");
            mapExParams.put("ultron2", "true");
            mapExParams.put("utdid", xHeader.getUtdid());
            mapExParams.put("latitude", "0");
            mapExParams.put("longitude", "0");

            Map<String, String> jsonParams = new HashMap<>();
            jsonParams.put("detail_v", "3.3.2");
            jsonParams.put("exParams", objectMapper.writeValueAsString(mapExParams));
            jsonParams.put("itemNumId", product.getProductId());

            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "mtop.taobao.detail.getdetail";
            String url = "https://trade-acs.m.taobao.com/gw/" + subUrl + "/6.0/?data=" + URLEncoder.encode(jsonText);

            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("6.0");
            xHeader.setData(jsonText);
            xHeader.setXsign(signService.xsign(xHeader));

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("MTOPSDK/3.1.1.7 (Android;5.1.1)")
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
                return R.error(taobaoReturn.getErrorMsg());
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R addTimestamp(LiveRoom liveRoom, TaobaoAccount taobaoAccount, String productId) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("liveId", liveRoom.getLiveId());
            jsonParams.put("itemId", productId);
            jsonParams.put("creatorId", liveRoom.getAccountId());

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            H5Header h5Header = new H5Header(taobaoAccount, "12574478");
            String subUrl = "mtop.mediaplatform.video.livedetail.itemlist.addTimeStamp";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("jsv", "2.5.1");
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", String.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "1.0");
            urlParams.put("data", jsonText);
            urlParams.put("type", "jsonp");
            urlParams.put("dataType", "jsonp");
            urlParams.put("_", "ji");
            urlParams.put("callback", "mtopjsonp21");
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/1.0/?";

            for (String key : jsonParams.keySet()) {
                url += key + "=" + URLEncoder.encode((String) jsonParams.get(key)) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccount.getCookieStore()));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            respText = respText.substring("mtopjsonp21(".length(), respText.length() - 1);

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorMsg());
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R autoLogin(TaobaoAccount taobaoAccount) {
        try {
            R r = this.getUserSimple(taobaoAccount);
            if (r.getCode() == ErrorCodes.SUCCESS) {
                taobaoAccount.setState(TaobaoAccountState.Normal.getState());
                return r;
            }

            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> apiReferMap = new HashMap<>();
            apiReferMap.put("apiName", "mtop.amp.ampService.getRecentContactsOfficialList");
            apiReferMap.put("appBackGround", false);
            apiReferMap.put("eventName", "SESSION_INVALID");
            apiReferMap.put("long_nick", "");
            apiReferMap.put("msgCode", "FAIL_SYS_SESSION_EXPIRED");
            apiReferMap.put("processName", "FAIL_SYS_SESSION_EXPIRED");
            apiReferMap.put("v", "4.0");

            Map<String, Object> extMap = new HashMap<>();
            extMap.put("apiRefer", objectMapper.writeValueAsString(apiReferMap));

            Map<String, Object> tokenInfo = new HashMap<>();
            apiReferMap.put("appName", "25443018");
            apiReferMap.put("appVersion", "android_7.6.0");
            apiReferMap.put("deviceId", taobaoAccount.getDevid());
            apiReferMap.put("deviceName", "");
            apiReferMap.put("locale", "zh_CN");
            apiReferMap.put("sdkVersion", "android_3.8.1");
            apiReferMap.put("site", 0);
            apiReferMap.put("t", new Date().getTime());
            apiReferMap.put("token", taobaoAccount.getAutoLoginToken());
            apiReferMap.put("ttid", "600000@taobao_android_7.6.0");
            apiReferMap.put("useAcitonType", true);
            apiReferMap.put("useDeviceToken", true);

            Map<String, Object> umidTokenMap = new HashMap<>();
            umidTokenMap.put("umidToken", taobaoAccount.getUmidToken());

            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("ext", objectMapper.writeValueAsString(extMap));
            jsonParams.put("userId", "0");
            jsonParams.put("tokenInfo", objectMapper.writeValueAsString(tokenInfo));
            jsonParams.put("riskControlInfo", objectMapper.writeValueAsString(umidTokenMap));

            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "com.taobao.mtop.mloginunitservice.autologin";
            String url = "https://acs.m.taobao.com/gw/" + subUrl + "/1.0/";

            XHeader xHeader = new XHeader(taobaoAccount);
            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("1.0");
            xHeader.setData(jsonText);
            xHeader.setXsign(signService.xsign(xHeader));

            Map<String, String> postParams = new HashMap<>();
            postParams.put("data", URLEncoder.encode(jsonText));

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("MTOPSDK/3.1.1.7 (Android;5.1.1)")
                            .addHeaders(xHeader.getHeaders())
                            .addHeader("Content-Type", "application/x-www-form-urlencoded"),
                    new Request("POST", url, ResponseType.TEXT)
                            .setParameters(postParams),
                    new DefaultCookieStorePool(taobaoAccount.getCookieStore()));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                taobaoAccount.setState(TaobaoAccountState.AutoLoginFailed.getState());
                return R.error();
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                taobaoAccount.setState(TaobaoAccountState.AutoLoginFailed.getState());
                return R.error(taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            Map<String, Object> mapReturnValue = (Map<String, Object>) mapData.get("returnValue");
            String data = (String) mapReturnValue.get("data");
            Map<String, Object> mapRetData = jsonParser.parseMap(data);

            String autoLoginToken = (String) mapRetData.get("autoLoginToken");
            long expires = (long) mapRetData.get("expires");
            List<String> lstCookieHeaders = (List<String>) mapRetData.get("cookies");

            List<Cookie> lstCookies = CookieHelper.parseCookieHeaders(url, lstCookieHeaders);

            String sid = (String) mapRetData.get("sid");
            String uid = (String) mapRetData.get("uid");
            String nick = (String) mapRetData.get("nick");

            taobaoAccount.setState(TaobaoAccountState.Normal.getState());

            return R.ok()
                    .put("expires", expires)
                    .put("autoLoginToken", autoLoginToken)
                    .put("cookie", lstCookies)
                    .put("sid", sid)
                    .put("uid", uid)
                    .put("nick", nick);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getLoginQRCodeURL() {
        try {
            String url = "https://qrlogin.taobao.com/qrcodelogin/generateNoLoginQRCode.do?lt=m";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)")
                            .addHeader("Referer", url)
                            .setContentType("application/x-www-form-urlencoded"),
                    new Request("GET", url, ResponseType.TEXT));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorMsg());
            }

            QRCode qrCode = new QRCode();
            String lgToken = (String) map.get("at");
            String qrCodeUrl = (String) map.get("url");
            long timestamp = (long) map.get("t");

            qrCode.setTimestamp(timestamp);
            qrCode.setAccessToken(lgToken);
            qrCode.setNavigateUrl(qrCodeUrl);

            return R.ok()
                    .put("qrCode", qrCode);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getLoginQRCodeCsrfToken(TaobaoAccount taobaoAccount, QRCode qrCode) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("_tbScancodeApproach_", "scan");
            jsonParams.put("ttid", "600000@taobao_android_8.7.1");
            jsonParams.put("shortURL", qrCode.getNavigateUrl());

            String url = "https://login.m.taobao.com/qrcodeLogin.htm?";
            for (String key : jsonParams.keySet()) {
                url += key + "=" + URLEncoder.encode((String) jsonParams.get(key)) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)")
                            .setAccept("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"),
                    new Request("GET", url, ResponseType.TEXT));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();

            // 获取csrfToken
            int startPos = respText.indexOf("name=\"csrftoken\"");
            if (startPos < 0)
                return null;
            startPos = respText.indexOf("value=\"", startPos);
            if (startPos < 0)
                return null;
            startPos += "value=\"".length();

            int endPos = respText.indexOf("\"", startPos);
            if (endPos < 0)
                return null;
            String csrfToken = respText.substring(startPos, endPos);

            return R.ok()
                    .put("csrfToken", csrfToken);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R authQRCode(TaobaoAccount taobaoAccount, QRCode qrCode, String csrfToken) {
        try {
            String url = "https://login.m.taobao.com/qrcodeLoginAuthor.do?qr_t=s";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)")
                            .setContentType("application/x-www-form-urlencoded"),
                    new Request("POST", url, ResponseType.TEXT)
                            .addParameter("csrftoken", csrfToken)
                            .addParameter("shortURL", qrCode.getNavigateUrl())
                            .addParameter("ql", "true"));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R checkLoginByQRCode(TaobaoAccount taobaoAccount, QRCode qrCode) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, String> tokenInfo = new HashMap<>();
            tokenInfo.put("utdid", taobaoAccount.getUtdid());
            tokenInfo.put("appName", "25443018");
            tokenInfo.put("token", qrCode.getAccessToken());
            tokenInfo.put("t", String.valueOf(qrCode.getTimestamp()));

            Map<String, String> umidToken = new HashMap<>();
            umidToken.put("umidToken", taobaoAccount.getUmidToken());

            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("tokenInfo", objectMapper.writeValueAsString(tokenInfo));
            paramMap.put("riskControlInfo", objectMapper.writeValueAsString(umidToken));
            paramMap.put("ext", "{}");

            String jsonText = objectMapper.writeValueAsString(paramMap);

            String subUrl = "mtop.taobao.havana.mlogin.qrcodelogin";
            String url = "https://api.m.taobao.com/gw/" + subUrl + "/1.0/?data=" + URLEncoder.encode(jsonText);

            XHeader xHeader = new XHeader(new Date());
            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("1.0");
            xHeader.setData(jsonText);
            xHeader.setXsign(signService.xsign(xHeader));

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)")
                            .setContentType("application/x-www-form-urlencoded")
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
                return R.error(taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            Map<String, Object> mapReturnValue = (Map<String, Object>) mapData.get("returnValue");
            String data = (String) mapReturnValue.get("data");
            Map<String, Object> mapRetData = jsonParser.parseMap(data);

            String autoLoginToken = (String) mapRetData.get("autoLoginToken");
            long expires = (long) mapRetData.get("expires");
            List<String> lstCookieHeaders = (List<String>) mapRetData.get("cookies");

            List<Cookie> lstCookies = CookieHelper.parseCookieHeaders(url, lstCookieHeaders);

            String sid = (String) mapRetData.get("sid");
            String uid = (String) mapRetData.get("uid");
            String nick = (String) mapRetData.get("nick");

            return R.ok()
                    .put("expires", expires)
                    .put("autoLoginToken", autoLoginToken)
                    .put("cookie", lstCookies)
                    .put("sid", sid)
                    .put("uid", uid)
                    .put("nick", nick);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R postpone(TaobaoAccount taobaoAccount) {
        try {
            R r = this.getUserSimple(taobaoAccount);
            if (r.getCode() == ErrorCodes.FAIL_SYS_SESSION_EXPIRED) {
                taobaoAccount.setState(TaobaoAccountState.Expired.getState());
                return r;
            }

            r = this.getLoginQRCodeURL();
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }
            QRCode qrCode = (QRCode) r.get("qrCode");
            r = this.getLoginQRCodeCsrfToken(taobaoAccount, qrCode);
            String csrfToken = (String) r.get("csrfToken");
            if (r.getCode() != ErrorCodes.SUCCESS || HFStringUtils.isNullOrEmpty(csrfToken)) {
                taobaoAccount.setState(TaobaoAccountState.AutoLoginFailed.getState());
                return r;
            }

            this.checkLoginByQRCode(taobaoAccount, qrCode);
            r = this.authQRCode(taobaoAccount, qrCode, csrfToken);

            boolean success = r.getCode() == ErrorCodes.SUCCESS ? true : false;
            int retry = 0;
            while (success && retry < Config.MAX_RETRY) {
                r = this.checkLoginByQRCode(taobaoAccount, qrCode);
                if (r.getCode() != ErrorCodes.SUCCESS) {
                    retry++;
                    Thread.sleep(100);
                    continue;
                }

                long expires = (long) r.get("expires");
                String autoLoginToken = (String) r.get("autoLoginToken");
                List<Cookie> lstCookies = (List<Cookie>) r.get("cookie");
                String sid = (String) r.get("sid");
                String uid = (String) r.get("uid");
                String nick = (String) r.get("nick");

                return R.ok()
                        .put("expires", expires)
                        .put("autoLoginToken", autoLoginToken)
                        .put("cookie", lstCookies)
                        .put("sid", sid)
                        .put("uid", uid)
                        .put("nick", nick);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getUmtidToken() {
        try {
            String url = "http://zb.dcdnz.com/api/notify/umtid.php?action=select";
            Response<String> response = HttpHelper.execute(
                new SiteConfig()
                    .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                new Request("GET", url, ResponseType.TEXT));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            respText = StringUtils.strip(respText, "\"\r\n");

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            List<Map> dataList = (List<Map>)map.get("data");
            if (dataList.size() > 0) {
                Map<String, Object> testMap = (Map<String, Object>)dataList.get(0);
                String umtid = (String) testMap.get("test");
                return R.ok()
                    .put("umtid", umtid.replace("{", "").replace("}", ""));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getH5Token(TaobaoAccount taobaoAccount) {
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

            String url = "https://acs.m.taobao.com/h5/" + subUrl + "/1.0/?";
            for (String key : paramMap.keySet()) {
                url += key + "=" + URLEncoder.encode((String) paramMap.get(key)) + "&";
            }

            Response<String> response = HttpHelper.execute(
                new SiteConfig()
                    .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                new Request("GET", url, ResponseType.TEXT),
                new DefaultCookieStorePool(taobaoAccount.getCookieStore()));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            List<Cookie> cookies = response.getCookieStore().getCookies();
            taobaoAccount.mergeCookies(cookies);

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getNewDeviceId(TaobaoAccount taobaoAccount) {
        try {
            String c2 = DeviceUtils.generateIMEI();
            String c3 = DeviceUtils.generatePhoneC3();
            String c6 = CommonUtils.randomAlphabetic("6mqe47k2o48k2b5d".length()).toLowerCase();
            String c4 = DeviceUtils.generateMAC();
            String c5 = CommonUtils.randomAlphabetic("88oikao7".length()).toLowerCase();

            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("c1", "HUAWEI(EVA-AL00)");
            jsonParams.put("c2", c2);
            jsonParams.put("c0", "HUAWEI");
            jsonParams.put("device_global_id", taobaoAccount.getUtdid() + c2 + c3);
            jsonParams.put("c6", c6);
            jsonParams.put("c4", c4);
            jsonParams.put("new_device", "true");
            jsonParams.put("c5", c5);
            jsonParams.put("c3", c3);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            H5Header h5Header = new H5Header(taobaoAccount, "21523971");
            String subUrl = "mtop.sys.newdeviceid";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", String.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "4.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/4.0/?";

            for (String key : jsonParams.keySet()) {
                url += key + "=" + URLEncoder.encode((String) jsonParams.get(key)) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccount.getCookieStore()));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            String deviceId = (String) mapData.get("device_id");

            return R.ok()
                    .put("device_id", deviceId);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }
}
