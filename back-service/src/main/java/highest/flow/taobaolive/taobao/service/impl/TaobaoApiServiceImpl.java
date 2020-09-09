package highest.flow.taobaolive.taobao.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.http.*;
import highest.flow.taobaolive.common.http.cookie.DefaultCookieStorePool;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.utils.CommonUtils;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.NumberUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.taobao.defines.LiveRoomState;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.*;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import highest.flow.taobaolive.taobao.service.SignService;
import highest.flow.taobaolive.taobao.utils.DeviceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.*;

@Service("taobaoApiService")
public class TaobaoApiServiceImpl implements TaobaoApiService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SignService signService;

    @Override
    public R getUserSimple(TaobaoAccountEntity taobaoAccountEntity) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("data", "{}");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "mtop.user.getusersimple";
            String url = "https://api.m.taobao.com/gw/" + subUrl + "/1.0/?data=" + URLEncoder.encode(jsonText);

            XHeader xHeader = new XHeader(taobaoAccountEntity);
            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("1.0");
            xHeader.setData(jsonText);
            xHeader.setXsign("ab24260090aaa8c2f96e2358c705f6e9d368f3f08ae4ee8b79"); // signService.xsign(xHeader));

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("MTOPSDK%2F3.1.1.7+%28Android%3B5.1.1%3Bsamsung%3BSM-J120F%29")
                            .addHeaders(xHeader.getHeaders()),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

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
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> extendMap = new HashMap<>();
            extendMap.put("version", "201903");

            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("ignoreH265", "false");
            jsonParams.put("liveId", liveId);
            jsonParams.put("extendJson", objectMapper.writeValueAsString(extendMap));

            String jsonText = objectMapper.writeValueAsString(jsonParams);

            H5Header h5Header = new H5Header(taobaoAccountEntity);
            String subUrl = "mtop.mediaplatform.live.livedetail";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", HFStringUtils.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "4.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/4.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(HFStringUtils.valueOf(urlParams.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("MTOPSDK%2F3.1.1.7+%28Android%3B5.1.1%3Bsamsung%3BSM-J120F%29")
                            .setContentType("application/x-www-form-urlencoded;charset=UTF-8"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

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
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("liveIdList", "[" + liveId + "]");
            jsonParams.put("source", "dbot");
            jsonParams.put("fillType", "[1]");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            H5Header h5Header = new H5Header(taobaoAccountEntity);
            String subUrl = "mtop.mediaplatform.live.batchGetByIdTypeList";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", HFStringUtils.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "1.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/1.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(HFStringUtils.valueOf(urlParams.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)")
                            .addHeader("Referer", url),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

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
            List<Map<String, Object>> lstModel = (List<Map<String, Object>>) mapData.get("model");

            for (Map<String, Object> objModel : lstModel) {
                LiveRoomEntity liveRoomEntity = new LiveRoomEntity();

                liveRoomEntity.setLiveId(HFStringUtils.valueOf(objModel.get("id")));
                liveRoomEntity.setAccountId(HFStringUtils.valueOf(objModel.get("accountId")));
                liveRoomEntity.setAccountName(HFStringUtils.valueOf(objModel.get("userNick")));
                liveRoomEntity.setTalentLiveUrl(HFStringUtils.valueOf(objModel.get("liveUrl")));
                liveRoomEntity.setTopic(HFStringUtils.valueOf(objModel.get("topic")));
                long appointmentTime = NumberUtils.valueOf(NumberUtils.parseLong(HFStringUtils.valueOf(objModel.get("appointmentTime"))));
                liveRoomEntity.setLiveAppointmentTime(CommonUtils.timestampToDate(appointmentTime));
                if (objModel.containsKey("startTime")) {
                    long startTime = NumberUtils.valueOf(NumberUtils.parseLong(HFStringUtils.valueOf(objModel.get("startTime"))));
                    liveRoomEntity.setLiveStartedTime(CommonUtils.timestampToDate(startTime));
                }
                if (objModel.containsKey("endTime")) {
                    long endTime = NumberUtils.valueOf(NumberUtils.parseLong(HFStringUtils.valueOf(objModel.get("endTime"))));
                    liveRoomEntity.setLiveEndTime(CommonUtils.timestampToDate(endTime));
                }
                liveRoomEntity.setLiveCoverImg(HFStringUtils.valueOf(objModel.get("coverImg")));
                if (objModel.containsKey("coverImg169")) {
                    liveRoomEntity.setLiveCoverImg169(HFStringUtils.valueOf(objModel.get("coverImg169")));
                }
                liveRoomEntity.setLiveTitle(HFStringUtils.valueOf(objModel.get("title")));
                liveRoomEntity.setLiveIntro(HFStringUtils.valueOf(objModel.get("descInfo")));
                int channelId = NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(objModel.get("liveChannelId"))));
                int columnId = NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(objModel.get("liveColumnId"))));
                liveRoomEntity.setLiveChannelId(channelId);
                liveRoomEntity.setLiveColumnId(columnId);
                liveRoomEntity.setLiveLocation(HFStringUtils.valueOf(objModel.get("location")));
                int status = NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(objModel.get("roomStatus"))));
                if (status == 0) { // 预告
                    liveRoomEntity.setLiveState(LiveRoomState.Published.getState());
                } else if (status == 1) { // 正在直播
                    liveRoomEntity.setLiveState(LiveRoomState.Started.getState());
                } else if (status == 2) { // 回放
                    liveRoomEntity.setLiveState(LiveRoomState.Stopped.getState());
                } else if (status == 4) { // 正在推流
                    liveRoomEntity.setLiveState(LiveRoomState.Pushing.getState());
                }

                return R.ok()
                        .put("live_room", liveRoomEntity);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getLiveProducts(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity, int count) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("groupNum", "0");
            jsonParams.put("liveId", liveRoomEntity.getLiveId());
            jsonParams.put("creatorId", liveRoomEntity.getAccountId());
            jsonParams.put("n", String.valueOf(count));
            jsonParams.put("type", "0");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "mtop.mediaplatform.video.livedetail.itemlist.withpaginationv5";
            String url = "https://acs.m.taobao.com/gw/" + subUrl + "/1.0/?";

            XHeader xHeader = new XHeader(taobaoAccountEntity);
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
                return R.error("获取直播间商品列表失败");
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorCode(), taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            int totalNum = NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapData.get("totalNum"))));

            List<ProductEntity> productEntities = new ArrayList<>();
            List<Map<String, Object>> itemList = (List<Map<String, Object>>) mapData.get("itemListv1");
            for (Map<String, Object> mapItem : itemList) {
                Map<String, Object> goodObj = (Map<String, Object>) mapItem.get("itemListv1");

                String itemId = HFStringUtils.valueOf(goodObj.get("itemId"));
                String itemName = HFStringUtils.valueOf(goodObj.get("itemName"));
                String itemPic = HFStringUtils.valueOf(goodObj.get("itemPic"));
                String itemPrice = HFStringUtils.valueOf(goodObj.get("itemPrice"));
                String itemUrl = "https://item.taobao.com/item.htm?id=" + itemId; // HFStringUtils.valueOf(goodObj.get("itemUrl"));

                ProductEntity productEntity = new ProductEntity();
                productEntity.setProductId(itemId);
                productEntity.setTitle(itemName);
                productEntity.setPicurl(itemPic);
                productEntity.setUrl(itemUrl);
                productEntity.setPrice(itemPrice);

                Map<String, Object> mapExtendVal = (Map<String, Object>) goodObj.get("extendVal");
                if (mapExtendVal != null) {
                    if (mapExtendVal.containsKey("timepoint")) {
                        productEntity.setTimepoint(NumberUtils.valueOf(NumberUtils.parseLong(HFStringUtils.valueOf(mapExtendVal.get("timepoint")))));
                    }

                    productEntity.setMonthSales(NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapExtendVal.get("buyCount")))));
                    productEntity.setCategoryId(HFStringUtils.valueOf(mapExtendVal.get("categoryLevelLeaf")));
                    productEntity.setCategoryTitle(HFStringUtils.valueOf(mapExtendVal.get("categoryLevelOneName")));

                    String business = HFStringUtils.valueOf(mapExtendVal.get("business"));
                    Map<String, Object> mapBusiness = jsonParser.parseMap(business);

                    Map<String, Object> mapCpsTcpInfo = (Map<String, Object>) mapBusiness.get("cpsTcpInfo");
                    Map<String, Object> mapTaobaoLivetoc = (Map<String, Object>) mapCpsTcpInfo.get("taobaolivetoc");
                    productEntity.setBusinessSceneId(NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapTaobaoLivetoc.get("businessScenceId")))));

                    Map<String, Object> mapItemBizInfo = (Map<String, Object>) mapBusiness.get("itemBizInfo");
                    String itemJumpUrl = HFStringUtils.valueOf(mapItemBizInfo.get("itemJumpUrl"));
                    itemJumpUrl = URLDecoder.decode(itemJumpUrl);

                    String [] words = itemJumpUrl.split("&");
                    for (String word : words) {
                        if (word.toLowerCase().startsWith("pg1stepk=")) {
                            productEntity.setPg1stepk(word.substring("pg1stepk=".length()));
                        } else if (word.toLowerCase().startsWith("liveinfo=")) {
                            productEntity.setLiveInfo(word.substring("liveInfo=".length()));
                        } else if (word.toLowerCase().startsWith("descversion=")) {
                            productEntity.setDescVersion(word.substring("descversion=".length()));
                        } else if (word.toLowerCase().startsWith("scm=")) {
                            productEntity.setScm(word.substring("scm=".length()));
                        } else if (word.toLowerCase().startsWith("spm=")) {
                            productEntity.setSpm(word.substring("spm=".length()));
                        } else if (word.toLowerCase().startsWith("utparam=")) {
                            productEntity.setUtparam(word.substring("utparam=".length()));
                        } else if (word.toLowerCase().startsWith("biztype=")) {
                            productEntity.setBizType(word.substring("biztype=".length()));
                        }
                    }
                }

                productEntity.setLiveId(HFStringUtils.valueOf(goodObj.get("liveId")));

                productEntities.add(productEntity);
            }

            liveRoomEntity.setProducts(productEntities);

            return R.ok()
                    .put("products", productEntities)
                    .put("total_num", totalNum);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("获取直播间商品列表失败");
    }

    @Override
    public R getLiveProductsWeb(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity, int count) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("groupNum", "0");
            jsonParams.put("liveId", liveRoomEntity.getLiveId());
            jsonParams.put("creatorId", liveRoomEntity.getAccountId());
            jsonParams.put("n", String.valueOf(count));
            jsonParams.put("type", "0");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            H5Header h5Header = new H5Header(taobaoAccountEntity);
            String subUrl = "mtop.mediaplatform.video.livedetail.itemlist.withpaginationv5";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", HFStringUtils.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "1.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/1.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(HFStringUtils.valueOf(urlParams.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error("获取直播间商品列表失败");
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorCode(), taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            int totalNum = NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapData.get("totalNum"))));

            List<ProductEntity> productEntities = new ArrayList<>();
            List<Map<String, Object>> itemList = (List<Map<String, Object>>) mapData.get("itemListv1");
            for (Map<String, Object> mapItem : itemList) {
                Map<String, Object> goodObj = (Map<String, Object>) mapItem.get("liveItemDO");

                String itemId = HFStringUtils.valueOf(goodObj.get("itemId"));
                String itemName = HFStringUtils.valueOf(goodObj.get("itemName"));
                String itemPic = HFStringUtils.valueOf(goodObj.get("itemPic"));
                String itemPrice = HFStringUtils.valueOf(goodObj.get("itemPrice"));
                String itemUrl = "https://item.taobao.com/item.htm?id=" + itemId; // HFStringUtils.valueOf(goodObj.get("itemUrl"));

                ProductEntity productEntity = new ProductEntity();
                productEntity.setProductId(itemId);
                productEntity.setTitle(itemName);
                productEntity.setPicurl(itemPic);
                productEntity.setUrl(itemUrl);
                productEntity.setPrice(itemPrice);

                Map<String, Object> mapExtendVal = (Map<String, Object>) goodObj.get("extendVal");
                if (mapExtendVal != null) {
                    if (mapExtendVal.containsKey("timepoint")) {
                        productEntity.setTimepoint(NumberUtils.valueOf(NumberUtils.parseLong(HFStringUtils.valueOf(mapExtendVal.get("timepoint")))));
                    }

                    productEntity.setMonthSales(NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapExtendVal.get("buyCount")))));
                    productEntity.setCategoryId(HFStringUtils.valueOf(mapExtendVal.get("categoryLevelLeaf")));
                    productEntity.setCategoryTitle(HFStringUtils.valueOf(mapExtendVal.get("categoryLevelOneName")));

                    String business = HFStringUtils.valueOf(mapExtendVal.get("business"));
                    Map<String, Object> mapBusiness = jsonParser.parseMap(business);

                    Map<String, Object> mapCpsTcpInfo = (Map<String, Object>) mapBusiness.get("cpsTcpInfo");
                    Map<String, Object> mapTaobaoLivetoc = (Map<String, Object>) mapCpsTcpInfo.get("taobaolivetoc");
                    productEntity.setBusinessSceneId(NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapTaobaoLivetoc.get("businessScenceId")))));

                    Map<String, Object> mapItemBizInfo = (Map<String, Object>) mapBusiness.get("itemBizInfo");
                    String itemJumpUrl = HFStringUtils.valueOf(mapItemBizInfo.get("itemJumpUrl"));
                    itemJumpUrl = URLDecoder.decode(itemJumpUrl);

                    String [] words = itemJumpUrl.split("&");
                    for (String word : words) {
                        if (word.toLowerCase().startsWith("pg1stepk=")) {
                            productEntity.setPg1stepk(word.substring("pg1stepk=".length()));
                        } else if (word.toLowerCase().startsWith("liveinfo=")) {
                            productEntity.setLiveInfo(word.substring("liveInfo=".length()));
                        } else if (word.toLowerCase().startsWith("descversion=")) {
                            productEntity.setDescVersion(word.substring("descversion=".length()));
                        } else if (word.toLowerCase().startsWith("scm=")) {
                            productEntity.setScm(word.substring("scm=".length()));
                        } else if (word.toLowerCase().startsWith("spm=")) {
                            productEntity.setSpm(word.substring("spm=".length()));
                        } else if (word.toLowerCase().startsWith("utparam=")) {
                            productEntity.setUtparam(word.substring("utparam=".length()));
                        } else if (word.toLowerCase().startsWith("biztype=")) {
                            productEntity.setBizType(word.substring("biztype=".length()));
                        }
                    }
                }

                productEntity.setLiveId(HFStringUtils.valueOf(goodObj.get("liveId")));

                productEntities.add(productEntity);
            }

            liveRoomEntity.setProducts(productEntities);

            return R.ok()
                    .put("products", productEntities)
                    .put("total_num", totalNum);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("获取直播间商品列表失败");
    }

    @Override
    public R getRankingListData(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
        try {
            if (HFStringUtils.isNullOrEmpty(liveRoomEntity.getHierarchyData().getScopeId()) ||
                    HFStringUtils.isNullOrEmpty(liveRoomEntity.getHierarchyData().getSubScopeId())) {
                R r = this.getLiveEntryWeb(liveRoomEntity, taobaoAccountEntity);
                if (r.getCode() == ErrorCodes.SUCCESS) {
                    return r;
                }
                r =  this.getLiveEntry(liveRoomEntity, taobaoAccountEntity);
                if (r.getCode() == ErrorCodes.SUCCESS) {
                    return r;
                }
            }

            R r = this.getLiveEntryWeb(liveRoomEntity, taobaoAccountEntity);
            if (r.getCode() != ErrorCodes.SUCCESS) {
                r = this.getLiveEntry(liveRoomEntity, taobaoAccountEntity);
                if (r.getCode() != ErrorCodes.SUCCESS) {
                    return this.getRankByMtop2(liveRoomEntity, taobaoAccountEntity);
                }
            }

            return r;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getLiveEntry(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("accountId", liveRoomEntity.getAccountId());
            jsonParams.put("liveId", liveRoomEntity.getLiveId());

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "mtop.mediaplatform.livedetail.entry";
            String url = "https://acs.m.taobao.com/gw/" + subUrl + "/2.0/?data=" + URLEncoder.encode(jsonText);

            XHeader xHeader = new XHeader(taobaoAccountEntity);
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
            // 总榜
            liveRoomEntity.setHasRankingListEntry(NumberUtils.valueOf(NumberUtils.parseBoolean(HFStringUtils.valueOf(mapData.get("hasRankingListEntry")))));
            if (!liveRoomEntity.isHasRankingListEntry()) {
                liveRoomEntity.getRankingListData().setRankingScore(0);
                liveRoomEntity.getRankingListData().setRankingNum(0);
                liveRoomEntity.getRankingListData().setRankingName("");

            } else {
                Map<String, Object> mapRankingListData = (Map<String, Object>) mapData.get("rankingListData");
                Map<String, Object> mapBizData = (Map<String, Object>) mapRankingListData.get("bizData");

                liveRoomEntity.getRankingListData().setRankingScore((int)NumberUtils.valueOf(NumberUtils.parseDouble(HFStringUtils.valueOf(mapBizData.get("score")))).doubleValue());
                liveRoomEntity.getRankingListData().setRankingNum(NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapBizData.get("rankNum")))));
                liveRoomEntity.getRankingListData().setRankingName(HFStringUtils.valueOf(mapBizData.get("name")));
            }

            // 小时榜
            liveRoomEntity.setHasHourRankingListEntry(
                    mapData.containsKey("hasRankingListEntry") ? NumberUtils.valueOf(NumberUtils.parseBoolean(HFStringUtils.valueOf(mapData.get("hasRankingListEntry")))) : false);
            if (!liveRoomEntity.isHasHourRankingListEntry()) {
                liveRoomEntity.getHourRankingListData().setRankingScore(0);
                liveRoomEntity.getHourRankingListData().setRankingNum(0);
                liveRoomEntity.getHourRankingListData().setRankingName("");

            } else {
                Map<String, Object> mapRankingListData = (Map<String, Object>) mapData.get("hourRankingListData");
                Map<String, Object> mapBizData = (Map<String, Object>) mapRankingListData.get("bizData");

                liveRoomEntity.getHourRankingListData().setRankingScore((int)NumberUtils.valueOf(NumberUtils.parseDouble(HFStringUtils.valueOf(mapBizData.get("score")))).doubleValue());
                liveRoomEntity.getHourRankingListData().setRankingNum(NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapBizData.get("rankNum")))));
                liveRoomEntity.getHourRankingListData().setRankingName(HFStringUtils.valueOf(mapBizData.get("name")));
            }

            Map<String, Object> mapHierachyData = (Map<String, Object>) mapData.get("hierarchyData");
            liveRoomEntity.getHierarchyData().setScopeId(mapHierachyData == null ? "-1" : HFStringUtils.valueOf(mapHierachyData.get("scopeId")));
            liveRoomEntity.getHierarchyData().setSubScopeId(mapHierachyData == null ? "-1" : HFStringUtils.valueOf(mapHierachyData.get("subScopeId")));

            return R.ok()
                    .put("live_room", liveRoomEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getLiveEntryWeb(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("accountId", liveRoomEntity.getAccountId());
            jsonParams.put("liveId", liveRoomEntity.getLiveId());

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            H5Header h5Header = new H5Header(taobaoAccountEntity);
            String subUrl = "mtop.mediaplatform.livedetail.entry";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", HFStringUtils.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "2.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/2.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(HFStringUtils.valueOf(urlParams.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

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
            // 总榜
            liveRoomEntity.setHasRankingListEntry(NumberUtils.valueOf(NumberUtils.parseBoolean(HFStringUtils.valueOf(mapData.get("hasRankingListEntry")))));
            if (!liveRoomEntity.isHasRankingListEntry()) {
                liveRoomEntity.getRankingListData().setRankingScore(0);
                liveRoomEntity.getRankingListData().setRankingNum(0);
                liveRoomEntity.getRankingListData().setRankingName("");

            } else {
                Map<String, Object> mapRankingListData = (Map<String, Object>) mapData.get("rankingListData");
                Map<String, Object> mapBizData = (Map<String, Object>) mapRankingListData.get("bizData");

                liveRoomEntity.getRankingListData().setRankingScore((int)NumberUtils.valueOf(NumberUtils.parseDouble(HFStringUtils.valueOf(mapBizData.get("score")))).doubleValue());
                liveRoomEntity.getRankingListData().setRankingNum(NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapBizData.get("rankNum")))));
                liveRoomEntity.getRankingListData().setRankingName(HFStringUtils.valueOf(mapBizData.get("name")));
            }

            // 小时榜
            liveRoomEntity.setHasHourRankingListEntry(
                    mapData.containsKey("hasRankingListEntry") ? NumberUtils.valueOf(NumberUtils.parseBoolean(HFStringUtils.valueOf(mapData.get("hasRankingListEntry")))) : false);
            if (!liveRoomEntity.isHasHourRankingListEntry()) {
                liveRoomEntity.getHourRankingListData().setRankingScore(0);
                liveRoomEntity.getHourRankingListData().setRankingNum(0);
                liveRoomEntity.getHourRankingListData().setRankingName("");

            } else {
                Map<String, Object> mapRankingListData = (Map<String, Object>) mapData.get("hourRankingListData");
                Map<String, Object> mapBizData = (Map<String, Object>) mapRankingListData.get("bizData");

                liveRoomEntity.getHourRankingListData().setRankingScore((int)NumberUtils.valueOf(NumberUtils.parseDouble(HFStringUtils.valueOf(mapBizData.get("score")))).doubleValue());
                liveRoomEntity.getHourRankingListData().setRankingNum(NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapBizData.get("rankNum")))));
                liveRoomEntity.getHourRankingListData().setRankingName(HFStringUtils.valueOf(mapBizData.get("name")));
            }

            Map<String, Object> mapHierachyData = (Map<String, Object>) mapData.get("hierarchyData");
            liveRoomEntity.getHierarchyData().setScopeId(mapHierachyData == null ? "-1" : HFStringUtils.valueOf(mapHierachyData.get("scopeId")));
            liveRoomEntity.getHierarchyData().setSubScopeId(mapHierachyData == null ? "-1" : HFStringUtils.valueOf(mapHierachyData.get("subScopeId")));

            return R.ok()
                    .put("live_room", liveRoomEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getRankByMtop2(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("creatorId", liveRoomEntity.getAccountId());
            jsonParams.put("liveId", liveRoomEntity.getLiveId());
            jsonParams.put("type", "activity");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "mtop.mediaplatform.livedetail.messinfo";
            String url = "https://acs.m.taobao.com/gw/" + subUrl + "/1.0/?data=" + URLEncoder.encode(jsonText);

            XHeader xHeader = new XHeader(taobaoAccountEntity);
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
            Map<String, Object> mapActivity = (Map<String, Object>) mapData.get("activity");
            Map<String, Object> mapBizData = (Map<String, Object>) mapActivity.get("bizData");

            if (mapBizData.containsKey("rankNum") && mapBizData.containsKey("score")) {
                liveRoomEntity.setHasRankingListEntry(true);
                liveRoomEntity.setHasHourRankingListEntry(true);

                liveRoomEntity.getRankingListData().setRankingScore((int)NumberUtils.valueOf(NumberUtils.parseDouble(HFStringUtils.valueOf(mapBizData.get("score")))).doubleValue());
                liveRoomEntity.getRankingListData().setRankingNum(NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapBizData.get("rankNum")))));
                liveRoomEntity.getRankingListData().setRankingName(HFStringUtils.valueOf(mapBizData.get("name")));

                liveRoomEntity.getHourRankingListData().setRankingScore((int)NumberUtils.valueOf(NumberUtils.parseDouble(HFStringUtils.valueOf(mapBizData.get("score")))).doubleValue());
                liveRoomEntity.getHourRankingListData().setRankingNum(NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapBizData.get("rankNum")))));
                liveRoomEntity.getHourRankingListData().setRankingName(HFStringUtils.valueOf(mapBizData.get("name")));

            } else {
                liveRoomEntity.setHasRankingListEntry(false);
                liveRoomEntity.setHasHourRankingListEntry(false);

            }

            return R.ok()
                    .put("live_room", liveRoomEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
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

            if (taobaoAccountEntity != null) {
                this.getH5Token(taobaoAccountEntity);
                this.getRankingListData(liveRoomEntity, taobaoAccountEntity);
            }

            return R.ok().put("live_room", liveRoomEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getLiveListWeb(TaobaoAccountEntity taobaoAccountEntity, int pageNo, int pageSize) {
        try {
            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("currentPage", HFStringUtils.valueOf(pageNo));
            urlParams.put("pagesize", HFStringUtils.valueOf(pageSize));
            urlParams.put("api", "get_live_list");

            String url = "https://liveplatform.taobao.com/live/action.do?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(HFStringUtils.valueOf(urlParams.get(key))) + "&";
            }

            String refererUrl = "https://liveplatform.taobao.com/live/liveList.htm";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                            .addHeader("Referer", refererUrl),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

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

            Map<String, Object> mapModel = (Map<String, Object>) map.get("model");
            List<Map<String, Object>> lstData = (List<Map<String, Object>>) mapModel.get("data");

            List<LiveRoomEntity> liveRoomEntities = new ArrayList<>();

            for (Map<String, Object> objData : lstData) {
                LiveRoomEntity liveRoomEntity = new LiveRoomEntity();

                liveRoomEntity.setLiveId(HFStringUtils.valueOf(objData.get("id")));
                liveRoomEntity.setAccountId(HFStringUtils.valueOf(objData.get("accountId")));
                liveRoomEntity.setAccountName(HFStringUtils.valueOf(objData.get("userNick")));
                liveRoomEntity.setTalentLiveUrl(HFStringUtils.valueOf(objData.get("liveUrl")));
                liveRoomEntity.setTopic(HFStringUtils.valueOf(objData.get("topic")));
                long appointmentTime = NumberUtils.valueOf(NumberUtils.parseLong(HFStringUtils.valueOf(objData.get("appointmentTime"))));
                liveRoomEntity.setLiveAppointmentTime(CommonUtils.timestampToDate(appointmentTime));
                long startTime = NumberUtils.valueOf(NumberUtils.parseLong(HFStringUtils.valueOf(objData.get("startTime"))));
                liveRoomEntity.setLiveStartedTime(CommonUtils.timestampToDate(startTime));
                if (objData.containsKey("endTime")) {
                    long endTime = NumberUtils.valueOf(NumberUtils.parseLong(HFStringUtils.valueOf(objData.get("endTime"))));
                    liveRoomEntity.setLiveEndTime(CommonUtils.timestampToDate(endTime));
                }
                liveRoomEntity.setLiveCoverImg(HFStringUtils.valueOf(objData.get("coverImg")));
                if (objData.containsKey("coverImg169")) {
                    liveRoomEntity.setLiveCoverImg169(HFStringUtils.valueOf(objData.get("coverImg169")));
                }
                liveRoomEntity.setLiveTitle(HFStringUtils.valueOf(objData.get("title")));
                liveRoomEntity.setLiveIntro(HFStringUtils.valueOf(objData.get("descInfo")));
                int channelId = NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(objData.get("liveChannelId"))));
                int columnId = NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(objData.get("liveColumnId"))));
                liveRoomEntity.setLiveChannelId(channelId);
                liveRoomEntity.setLiveColumnId(columnId);
                liveRoomEntity.setLiveLocation(HFStringUtils.valueOf(objData.get("location")));
                int status = NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(objData.get("roomStatus"))));
                if (status == 0) { // 预告
                    liveRoomEntity.setLiveState(LiveRoomState.Published.getState());
                } else if (status == 1) { // 正在直播
                    liveRoomEntity.setLiveState(LiveRoomState.Started.getState());
                } else if (status == 2) { // 回放
                    liveRoomEntity.setLiveState(LiveRoomState.Stopped.getState());
                } else if (status == 4) { // 正在推流
                    liveRoomEntity.setLiveState(LiveRoomState.Pushing.getState());
                }

                liveRoomEntities.add(liveRoomEntity);
            }

            return R.ok()
                    .put("live_rooms", liveRoomEntities);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getPlayingLiveRoom(TaobaoAccountEntity taobaoAccountEntity) {
        try {
            R r = this.getLiveListWeb(taobaoAccountEntity, 1, 20);
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            List<LiveRoomEntity> liveRoomEntities = (List<LiveRoomEntity>) r.get("live_rooms");

            for (LiveRoomEntity liveRoomEntity : liveRoomEntities) {
                if (liveRoomEntity.getLiveState() == LiveRoomState.Started.getState()) {
                    return R.ok().put("live_room", liveRoomEntity);
                }
            }

            return R.ok()
                    .put("live_room", null);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getSigninWeb(TaobaoAccountEntity taobaoAccountEntity) {
        try {
            H5Header h5Header = new H5Header(taobaoAccountEntity);
            String subUrl = "mtop.mediaplatform.lightlive.getsigninstatus";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", HFStringUtils.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "1.0");
            urlParams.put("data", "{}");
            urlParams.put("sign", signService.h5sign(h5Header, "{}"));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/1.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(HFStringUtils.valueOf(urlParams.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

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

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R routeToNewPlanWeb(TaobaoAccountEntity taobaoAccountEntity) {
        try {
            H5Header h5Header = new H5Header(taobaoAccountEntity);
            String subUrl = "mtop.taobao.livex.goldcoin.routeToNewPlan";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", HFStringUtils.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "1.0");
            urlParams.put("data", "{}");
            urlParams.put("sign", signService.h5sign(h5Header, "{}"));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/1.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(HFStringUtils.valueOf(urlParams.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

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

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R checkInStatusWeb(TaobaoAccountEntity taobaoAccountEntity) {
        try {
            H5Header h5Header = new H5Header(taobaoAccountEntity);
            String subUrl = "mtop.taobao.livex.checkin.status";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", HFStringUtils.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "1.0");
            urlParams.put("data", "{}");
            urlParams.put("sign", signService.h5sign(h5Header, "{}"));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/1.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(HFStringUtils.valueOf(urlParams.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

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

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getIntimacyDetail(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("liveId", liveRoomEntity.getLiveId());
            jsonParams.put("anchorId", liveRoomEntity.getAccountId());

            String jsonText = objectMapper.writeValueAsString(jsonParams);

            H5Header h5Header = new H5Header(taobaoAccountEntity);
            String subUrl = "mtop.taobao.iliad.task.hierarchy.detail";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", HFStringUtils.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "1.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/1.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(HFStringUtils.valueOf(urlParams.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

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

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R taskCompleteWeb(TaobaoAccountEntity taobaoAccountEntity) {
        try {
            H5Header h5Header = new H5Header(taobaoAccountEntity);
            String subUrl = "mtop.mediaplatform.lightlive.reportCompleteTask";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", HFStringUtils.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "1.0");
            urlParams.put("data", "{}");
            urlParams.put("sign", signService.h5sign(h5Header, "{}"));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/1.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(HFStringUtils.valueOf(urlParams.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

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

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
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

            XHeader xHeader = new XHeader(taobaoAccountEntity);
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

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R taskFollowWeb(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
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

            H5Header h5Header = new H5Header(taobaoAccountEntity);
            String subUrl = "mtop.taobao.iliad.task.action";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", HFStringUtils.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "1.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/1.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(HFStringUtils.valueOf(urlParams.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

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

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
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

            XHeader xHeader = new XHeader(taobaoAccountEntity);
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

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R taskStayWeb(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity, int staySeconds) {
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

            H5Header h5Header = new H5Header(taobaoAccountEntity);
            String subUrl = "mtop.taobao.iliad.task.action";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", HFStringUtils.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "1.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/1.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(HFStringUtils.valueOf(urlParams.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

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

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
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

            XHeader xHeader = new XHeader(taobaoAccountEntity);
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

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R taskBuyWeb(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity, String productId) {
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

            H5Header h5Header = new H5Header(taobaoAccountEntity);
            String subUrl = "mtop.taobao.iliad.task.action";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", HFStringUtils.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "1.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/1.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(HFStringUtils.valueOf(urlParams.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

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

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R createLiveRoomWeb(PreLiveRoomSpec preLiveRoomSpec, TaobaoAccountEntity taobaoAccountEntity) {
        try {
            String url = "https://liveplatform.taobao.com/live/action.do?api=publish_pre_live&_tb_token_=" + taobaoAccountEntity.getToken() + "&_input_charset=utf-8";
            String refererUrl = "https://liveplatform.taobao.com/live/addLive.htm";

            Map<String, String> postParams = new HashMap<>();
            postParams.put("landscape", "false");
            postParams.put("title", preLiveRoomSpec.getLiveTitle());
            postParams.put("descInfo", preLiveRoomSpec.getLiveIntro());
            postParams.put("coverImg", preLiveRoomSpec.getLiveCoverImg().replace("https:", ""));
            postParams.put("coverImg169", preLiveRoomSpec.getLiveCoverImg169().replace("https:", ""));
            postParams.put("coverImg916", "null");
            postParams.put("uploadId", "undefined");
            postParams.put("roomType", "0");
            postParams.put("liveChannelId", HFStringUtils.valueOf(preLiveRoomSpec.getLiveChannelId()));
            postParams.put("liveColumnId", HFStringUtils.valueOf(preLiveRoomSpec.getLiveColumnId()));
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
            postParams.put("addressDetail", preLiveRoomSpec.getLiveLocation());
            postParams.put("appId", "");
            postParams.put("notice", "");
            postParams.put("country", "");
            postParams.put("province", "");
            postParams.put("city", preLiveRoomSpec.getLiveLocation());
            postParams.put("appointmentTime", HFStringUtils.valueOf(CommonUtils.dateToTimestamp(preLiveRoomSpec.getLiveAppointmentTime())));
            postParams.put("liveEndTime", HFStringUtils.valueOf(CommonUtils.dateToTimestamp(CommonUtils.addDays(preLiveRoomSpec.getLiveAppointmentTime(), 30))));

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                            .addHeader("Referer", refererUrl)
                            .addHeader("Content-Type", "application/x-www-form-urlencoded"),
                    new Request("POST", url, ResponseType.TEXT)
                            .setParameters(postParams),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));
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

            Map<String, Object> mapModel = (Map<String, Object>) map.get("model");

            LiveRoomEntity liveRoomEntity = new LiveRoomEntity();
            liveRoomEntity.setLiveId(HFStringUtils.valueOf(mapModel.get("preLiveId")));
            liveRoomEntity.setLiveAppointmentTime(preLiveRoomSpec.getLiveAppointmentTime());
            liveRoomEntity.setLiveCoverImg(preLiveRoomSpec.getLiveCoverImg());
            liveRoomEntity.setLiveCoverImg169(preLiveRoomSpec.getLiveCoverImg169());
            liveRoomEntity.setLiveTitle(preLiveRoomSpec.getLiveTitle());
            liveRoomEntity.setLiveIntro(preLiveRoomSpec.getLiveIntro());
            liveRoomEntity.setLiveChannelId(preLiveRoomSpec.getLiveChannelId());
            liveRoomEntity.setLiveColumnId(preLiveRoomSpec.getLiveColumnId());
            liveRoomEntity.setLiveLocation(preLiveRoomSpec.getLiveLocation());
            liveRoomEntity.setLiveState(LiveRoomState.Published.getState());

            return R.ok()
                    .put("live_room", liveRoomEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R createLiveRoom(PreLiveRoomSpec preLiveRoomSpec, TaobaoAccountEntity taobaoAccountEntity) {
        try {
            Map<String, String> jsonParams = new HashMap<>();
            jsonParams.put("coverImg", preLiveRoomSpec.getLiveCoverImg());
            jsonParams.put("coverImg169", preLiveRoomSpec.getLiveCoverImg169());
            jsonParams.put("appointmentTime", HFStringUtils.valueOf(CommonUtils.dateToTimestamp(preLiveRoomSpec.getLiveAppointmentTime())));
            jsonParams.put("title", preLiveRoomSpec.getLiveTitle());
            jsonParams.put("intro", preLiveRoomSpec.getLiveIntro());
            jsonParams.put("itemIds", "");
            jsonParams.put("liveChannelId", HFStringUtils.valueOf(preLiveRoomSpec.getLiveChannelId()));
            jsonParams.put("liveColumnId", HFStringUtils.valueOf(preLiveRoomSpec.getLiveColumnId()));
            jsonParams.put("location", preLiveRoomSpec.getLiveLocation());
            jsonParams.put("landScape", "false");
            jsonParams.put("useLcps", "false");
            jsonParams.put("tidbitsUrl", "");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "mtop.mediaplatform.live.create";
            String url = "https://acs.m.taobao.com/gw/" + subUrl + "/3.0/?data=" + URLEncoder.encode(jsonText);

            XHeader xHeader = new XHeader(taobaoAccountEntity);
            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("3.0");
            xHeader.setData(jsonText);
            xHeader.setXsign(signService.xsign(xHeader));

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("MTOPSDK%2F3.1.1.7+%28Android%3B5.1.1%3Bsamsung%3BSM-J120F%29")
                            .addHeaders(xHeader.getHeaders()),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

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
            LiveRoomEntity liveRoomEntity = new LiveRoomEntity();
            liveRoomEntity.setLiveId(HFStringUtils.valueOf(mapData.get("liveId")));
            liveRoomEntity.setAccountId(HFStringUtils.valueOf(mapData.get("accountId")));
            liveRoomEntity.setTopic(HFStringUtils.valueOf(mapData.get("topic")));
            liveRoomEntity.setViewCount(NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapData.get("viewCount")))));
            liveRoomEntity.setPraiseCount(NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapData.get("praiseCount")))));
            liveRoomEntity.setLiveAppointmentTime(preLiveRoomSpec.getLiveAppointmentTime());
            liveRoomEntity.setLiveCoverImg(HFStringUtils.valueOf(mapData.get("coverImg")));
            liveRoomEntity.setLiveCoverImg169(HFStringUtils.valueOf(mapData.get("coverImg169")));
            liveRoomEntity.setLiveTitle(HFStringUtils.valueOf(mapData.get("title")));
            liveRoomEntity.setLiveIntro(HFStringUtils.valueOf(mapData.get("intro")));
            liveRoomEntity.setLiveChannelId(NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapData.get("liveChannelId")))));
            liveRoomEntity.setLiveColumnId(NumberUtils.valueOf(NumberUtils.parseInt(HFStringUtils.valueOf(mapData.get("liveColumnId")))));
            liveRoomEntity.setLiveLocation(HFStringUtils.valueOf(mapData.get("location")));
            liveRoomEntity.setAccountName(HFStringUtils.valueOf(mapData.get("userNick")));
            liveRoomEntity.setLiveState(LiveRoomState.Published.getState());

            return R.ok()
                    .put("live_room", liveRoomEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R startLive(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
        try {
            String url = "https://liveplatform.taobao.com/live/action.do?api=start_live_from_pre&_tb_token_=" + taobaoAccountEntity.getToken();
            String refererUrl = "https://liveplatform.taobao.com/live/liveDetail.htm?id=" + liveRoomEntity.getLiveId() + "&openHlvPush=true";

            Map<String, String> postParams = new HashMap<>();
            postParams.put("liveVideoId", liveRoomEntity.getLiveId());
            postParams.put("accountId", taobaoAccountEntity.getUid());
            postParams.put("location", HFStringUtils.isNullOrEmpty(liveRoomEntity.getLiveLocation()) ? "中国" : liveRoomEntity.getLiveLocation());

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                            .addHeader("Referer", refererUrl)
                            .addHeader("Content-Type", "application/x-www-form-urlencoded"),
                    new Request("POST", url, ResponseType.TEXT)
                            .setParameters(postParams),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));
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

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R stopLive(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
        try {
            String url = "https://liveplatform.taobao.com/live/action.do?api=stop_live";
            String refererUrl = "https://liveplatform.taobao.com/live/live_detail.htm?id=" + liveRoomEntity.getLiveId() + "&openHlvPush=true";

            Map<String, String> postParams = new HashMap<>();
            postParams.put("pFeedId", liveRoomEntity.getLiveId());
            postParams.put("_tb_token_", taobaoAccountEntity.getToken());

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                            .addHeader("Referer", refererUrl)
                            .addHeader("Content-Type", "application/x-www-form-urlencoded"),
                    new Request("POST", url, ResponseType.TEXT)
                            .setParameters(postParams),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));
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

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R deleteLive(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
        try {
            String url = "https://liveplatform.taobao.com/live/action.do?api=delete_live&liveId=" + liveRoomEntity.getLiveId();
            String refererUrl = "https://liveplatform.taobao.com/live/liveList.htm?spm=a1z9u.8142865.0.0.3d067997jRNQCS";

            Map<String, String> postParams = new HashMap<>();
            postParams.put("pFeedId", liveRoomEntity.getLiveId());
            postParams.put("_tb_token_", taobaoAccountEntity.getToken());

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                            .addHeader("Referer", refererUrl)
                            .addHeader("Content-Type", "application/x-www-form-urlencoded"),
                    new Request("POST", url, ResponseType.TEXT)
                            .setParameters(postParams),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));
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

            return R.ok();


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getProductItemInfo(TaobaoAccountEntity taobaoAccountEntity, String productId) {
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
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));
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

            Map<String, Object> mapModel = (Map<String, Object>) map.get("model");
            String imgUrl = "//gw.alicdn.com/img/bao/uploaded/" + HFStringUtils.valueOf(mapModel.get("imgUrl"));
            String title = HFStringUtils.valueOf(mapModel.get("itemTitle"));
            String price = HFStringUtils.valueOf(mapModel.get("itemPrice"));

            return R.ok()
                    .put("product_id", productId)
                    .put("img_url", imgUrl)
                    .put("title", title)
                    .put("price", price);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R publishProductWeb(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity, ProductEntity productEntity) {
        try {
            Map<String, Object> mapNode = new HashMap<>();
            mapNode.put("type", "picItem");
            mapNode.put("path", productEntity.getPicurl().replace("https:", ""));
            mapNode.put("content", productEntity.getTitle());
            mapNode.put("bizId", productEntity.getProductId());
            mapNode.put("right", "");
            List lstNodes = new ArrayList();
            lstNodes.add(mapNode);

            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("feedType", "502");
            jsonParams.put("roomType", 0);
            jsonParams.put("nodes", lstNodes);
            jsonParams.put("parentId", liveRoomEntity.getLiveId());
            jsonParams.put("feedId", "");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String url = "https://liveplatform.taobao.com/live/action.do?api=publish_content_feed";
            String refererUrl = "https://liveplatform.taobao.com/live/liveDetail.htm?id=" + liveRoomEntity.getLiveId();

            Map<String, String> postParams = new HashMap<>();
            postParams.put("_input_charset", "utf-8");
            postParams.put("draft", URLEncoder.encode(jsonText));
            postParams.put("_tb_token_", taobaoAccountEntity.getToken());

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                            .addHeader("Referer", refererUrl)
                            .addHeader("Content-Type", "application/x-www-form-urlencoded"),
                    new Request("POST", url, ResponseType.TEXT)
                            .setParameters(postParams),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

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

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R openProduct(TaobaoAccountEntity taobaoAccountEntity, String productId) {
        try {
            XHeader xHeader = new XHeader(taobaoAccountEntity);
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
            mapExParams.put("id", productId);
            mapExParams.put("item_id", productId);
            mapExParams.put("list_type", "search");
            mapExParams.put("nick", taobaoAccountEntity.getNick());
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
            jsonParams.put("itemNumId", productId);

            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "mtop.taobao.detail.getdetail";
            String url = "https://trade-acs.m.taobao.com/gw/" + subUrl + "/6.0/?data=" + URLEncoder.encode(jsonText);

            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("6.0");
            xHeader.setData(jsonText);
            xHeader.setXsign(signService.xsign(xHeader));

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("MTOPSDK%2F3.1.1.7+%28Android%3B5.1.1%3Bsamsung%3BSM-J120F%29")
                            .addHeaders(xHeader.getHeaders()),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

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

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R addTimestamp(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity, String productId) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("liveId", liveRoomEntity.getLiveId());
            jsonParams.put("itemId", productId);
            jsonParams.put("creatorId", liveRoomEntity.getAccountId());

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            H5Header h5Header = new H5Header(taobaoAccountEntity, "12574478");
            String subUrl = "mtop.mediaplatform.video.livedetail.itemlist.addTimeStamp";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("jsv", "2.5.1");
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", HFStringUtils.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "1.0");
            urlParams.put("data", jsonText);
            urlParams.put("type", "jsonp");
            urlParams.put("dataType", "jsonp");
            urlParams.put("_", "ji");
            urlParams.put("callback", "mtopjsonp21");
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/1.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(HFStringUtils.valueOf(urlParams.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            respText = respText.substring("mtopjsonp21(".length(), respText.length() - 1);

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorCode(), taobaoReturn.getErrorMsg());
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R autoLogin(TaobaoAccountEntity taobaoAccountEntity) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> apiReferMap = new HashMap<>();
            apiReferMap.put("apiName", "mtop.amp.ampService.getRecentContactsOfficialList");
            apiReferMap.put("appBackGround", false);
            apiReferMap.put("eventName", "SESSION_INVALID");
            apiReferMap.put("long_nick", "");
            apiReferMap.put("msgCode", "FAIL_SYS_SESSION_EXPIRED");
            apiReferMap.put("processName", "com.taobao.taobao");
            apiReferMap.put("v", "4.0");

            Map<String, Object> extMap = new HashMap<>();
            extMap.put("apiRefer", objectMapper.writeValueAsString(apiReferMap));

            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("appName", "25443018");
            tokenInfo.put("appVersion", "android_7.6.0");
            tokenInfo.put("deviceId", taobaoAccountEntity.getDevid());
            tokenInfo.put("deviceName", "");
            tokenInfo.put("locale", "zh_CN");
            tokenInfo.put("sdkVersion", "android_3.8.1");
            tokenInfo.put("site", 0);
            tokenInfo.put("t", new Date().getTime());
            tokenInfo.put("token", taobaoAccountEntity.getAutoLoginToken());
            tokenInfo.put("ttid", "600000@taobao_android_7.6.0");
            tokenInfo.put("useAcitonType", true);
            tokenInfo.put("useDeviceToken", true);

            Map<String, Object> umidTokenMap = new HashMap<>();
            umidTokenMap.put("umidToken", taobaoAccountEntity.getUmidToken());

            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("ext", objectMapper.writeValueAsString(extMap));
            jsonParams.put("userId", "0");
            jsonParams.put("tokenInfo", objectMapper.writeValueAsString(tokenInfo));
            jsonParams.put("riskControlInfo", objectMapper.writeValueAsString(umidTokenMap));

            String jsonText = objectMapper.writeValueAsString(jsonParams);

            String subUrl = "com.taobao.mtop.mloginunitservice.autologin";
            String url = "https://acs.m.taobao.com/gw/" + subUrl + "/1.0/";

            XHeader xHeader = new XHeader(taobaoAccountEntity);
            xHeader.setSubUrl(subUrl);
            xHeader.setUrlVer("1.0");
            xHeader.setData(jsonText);
            xHeader.setXsign(signService.xsign(xHeader));

            Map<String, String> postParams = new HashMap<>();
            postParams.put("data", jsonText);

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("MTOPSDK%2F3.1.1.7+%28Android%3B5.1.1%3Bsamsung%3BSM-J120F%29")
                            .addHeaders(xHeader.getHeaders())
                            .addHeader("Content-Type", "application/x-www-form-urlencoded"),
                    new Request("POST", url, ResponseType.TEXT)
                            .setParameters(postParams),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                taobaoAccountEntity.setState(TaobaoAccountState.AutoLoginFailed.getState());
                taobaoAccountEntity.setUpdatedTime(new Date());
                return R.error(taobaoReturn.getErrorCode(), taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            Map<String, Object> mapReturnValue = (Map<String, Object>) mapData.get("returnValue");
            String data = (String) mapReturnValue.get("data");
            Map<String, Object> mapRetData = jsonParser.parseMap(data);

            String autoLoginToken = HFStringUtils.valueOf(mapRetData.get("autoLoginToken"));
            long expires = NumberUtils.valueOf(NumberUtils.parseLong(HFStringUtils.valueOf(mapRetData.get("expires"))));
            List<String> lstCookieHeaders = (List<String>) mapRetData.get("cookies");

            List<Cookie> lstCookies = CookieHelper.parseCookieHeaders(url, lstCookieHeaders);

            String sid = HFStringUtils.valueOf(mapRetData.get("sid"));
            String uid = HFStringUtils.valueOf(mapRetData.get("userId"));
            String nick = HFStringUtils.valueOf(mapRetData.get("nick"));

            taobaoAccountEntity.setAutoLoginToken(autoLoginToken);
            taobaoAccountEntity.setSid(sid);
            taobaoAccountEntity.setUid(uid);
            taobaoAccountEntity.setNick(nick);
            taobaoAccountEntity.setExpires(CommonUtils.timestampToDate(expires * 1000));

            CookieStore cookieStore = new BasicCookieStore();
            for (Cookie cookie : lstCookies) {
                cookieStore.addCookie(cookie);
            }
            taobaoAccountEntity.setCookieStore(cookieStore);

            taobaoAccountEntity.setState(TaobaoAccountState.Normal.getState());
            taobaoAccountEntity.setUpdatedTime(new Date());

            return R.ok()
                    .put("taobao_account", taobaoAccountEntity);

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
                return R.error("获取二维码失败");
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorCode(), taobaoReturn.getErrorMsg());
            }

            QRCode qrCode = new QRCode();
            String lgToken = HFStringUtils.valueOf(map.get("at"));
            String qrCodeUrl = HFStringUtils.valueOf(map.get("url"));
            long timestamp = NumberUtils.valueOf(NumberUtils.parseLong(HFStringUtils.valueOf(map.get("t"))));

            qrCode.setTimestamp(timestamp);
            qrCode.setAccessToken(lgToken);
            qrCode.setNavigateUrl(qrCodeUrl);
            qrCode.setImageUrl("https://gqrcode.alicdn.com/img?type=hv&text=" + URLEncoder.encode(qrCodeUrl) + "&h=160&w=160");

            return R.ok()
                    .put("qrcode", qrCode);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("获取二维码失败");
    }

    @Override
    public R getLoginQRCodeCsrfToken(TaobaoAccountEntity taobaoAccountEntity, QRCode qrCode) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("_tbScancodeApproach_", "scan");
            jsonParams.put("ttid", "600000@taobao_android_8.7.1");
            jsonParams.put("shortURL", qrCode.getNavigateUrl());

            String url = "https://login.m.taobao.com/qrcodeLogin.htm?";
            for (String key : jsonParams.keySet()) {
                url += key + "=" + URLEncoder.encode(HFStringUtils.valueOf(jsonParams.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)")
                            .setAccept("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                            .addHeader("Accept-Language", "zh-CN,en-US;q=0.9")
                            .addHeader("x-requested-with", "com.taobao.taobao"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error();
            }

            String respText = response.getResult();

            // 获取csrfToken
            int startPos = respText.indexOf("name=\"csrftoken\"");
            if (startPos < 0)
                return R.error();
            startPos = respText.indexOf("value=\"", startPos);
            if (startPos < 0)
                return R.error();
            startPos += "value=\"".length();

            int endPos = respText.indexOf("\"", startPos);
            if (endPos < 0)
                return R.error();
            String csrfToken = respText.substring(startPos, endPos);

            return R.ok()
                    .put("csrftoken", csrfToken);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R authQRCode(TaobaoAccountEntity taobaoAccountEntity, QRCode qrCode, String csrfToken) {
        try {
            String url = "https://login.m.taobao.com/qrcodeLoginAuthor.do?qr_t=s";

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)")
                            .setContentType("application/x-www-form-urlencoded"),
                    new Request("POST", url, ResponseType.TEXT)
                            .addParameter("csrftoken", csrfToken)
                            .addParameter("shortURL", qrCode.getNavigateUrl())
                            .addParameter("ql", "true"),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));
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
    public R checkLoginByQRCode(TaobaoAccountEntity taobaoAccountEntity, QRCode qrCode) {
        try {
            XHeader xHeader = new XHeader(new Date());
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, String> tokenInfo = new HashMap<>();
            tokenInfo.put("utdid", taobaoAccountEntity.getUtdid());
            tokenInfo.put("appName", "25443018");
            tokenInfo.put("token", qrCode.getAccessToken());
            tokenInfo.put("t", HFStringUtils.valueOf(qrCode.getTimestamp()));

            Map<String, String> umidToken = new HashMap<>();
            umidToken.put("umidToken", taobaoAccountEntity.getUmidToken());

            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("tokenInfo", objectMapper.writeValueAsString(tokenInfo));
            paramMap.put("riskControlInfo", objectMapper.writeValueAsString(umidToken));
            paramMap.put("ext", "{}");

            String jsonText = objectMapper.writeValueAsString(paramMap);

            String subUrl = "mtop.taobao.havana.mlogin.qrcodelogin";
            String url = "https://api.m.taobao.com/gw/" + subUrl + "/1.0/?data=" + URLEncoder.encode(jsonText);

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
                return R.error("验证登录二维码失败");
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorCode(), taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            Map<String, Object> mapReturnValue = (Map<String, Object>) mapData.get("returnValue");
            String data = HFStringUtils.valueOf(mapReturnValue.get("data"));
            Map<String, Object> mapRetData = jsonParser.parseMap(data);

            String autoLoginToken = HFStringUtils.valueOf(mapRetData.get("autoLoginToken"));
            long expires = NumberUtils.valueOf(NumberUtils.parseLong(HFStringUtils.valueOf(mapRetData.get("expires"))));
            List<String> lstCookieHeaders = (List<String>) mapRetData.get("cookies");

            List<Cookie> lstCookies = CookieHelper.parseCookieHeaders(url, lstCookieHeaders);

            String sid = HFStringUtils.valueOf(mapRetData.get("sid"));
            String uid = HFStringUtils.valueOf(mapRetData.get("userId"));
            String nick = HFStringUtils.valueOf(mapRetData.get("nick"));

            taobaoAccountEntity.setAutoLoginToken(autoLoginToken);
            taobaoAccountEntity.setSid(sid);
            taobaoAccountEntity.setUid(uid);
            taobaoAccountEntity.setNick(nick);
            taobaoAccountEntity.setExpires(CommonUtils.timestampToDate(expires * 1000));

            CookieStore cookieStore = new BasicCookieStore();
            for (Cookie cookie : lstCookies) {
                cookieStore.addCookie(cookie);
            }
            taobaoAccountEntity.setCookieStore(cookieStore);

            taobaoAccountEntity.setState(TaobaoAccountState.Normal.getState());
            taobaoAccountEntity.setUpdatedTime(new Date());

            return R.ok()
                    .put("taobao_account", taobaoAccountEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("验证登录二维码失败");
    }

    @Override
    public R postpone(TaobaoAccountEntity taobaoAccountEntity) {
        try {
            R r = this.getLoginQRCodeURL();
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }
            QRCode qrCode = (QRCode) r.get("qrcode");
            r = this.getLoginQRCodeCsrfToken(taobaoAccountEntity, qrCode);
            String csrfToken = (String) r.get("csrftoken");
            if (r.getCode() != ErrorCodes.SUCCESS || HFStringUtils.isNullOrEmpty(csrfToken)) {
                return r;
            }

            this.checkLoginByQRCode(taobaoAccountEntity, qrCode);
            r = this.authQRCode(taobaoAccountEntity, qrCode, csrfToken);

            boolean success = r.getCode() == ErrorCodes.SUCCESS ? true : false;
            int retry = 0;
            while (success && retry < Config.MAX_RETRY) {
                r = this.checkLoginByQRCode(taobaoAccountEntity, qrCode);
                if (r.getCode() != ErrorCodes.SUCCESS) {
                    retry++;
                    Thread.sleep(100);
                    continue;
                }

                return r;
            }

            taobaoAccountEntity.setState(TaobaoAccountState.Expired.getState());
            taobaoAccountEntity.setUpdatedTime(new Date());

            return r;

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
                return R.error("生成UmtidToken失败");
            }

            String respText = response.getResult();
            respText = StringUtils.strip(respText, "\"\r\n");

            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            List<Map> dataList = (List<Map>)map.get("data");
            if (dataList.size() > 0) {
                Map<String, Object> testMap = (Map<String, Object>)dataList.get(0);
                String umtid = HFStringUtils.valueOf(testMap.get("test"));
                return R.ok()
                    .put("umtid", umtid.replace("{", "").replace("}", ""));
            }

//            // TODO
//            return R.ok().put("umtid", CommonUtils.randomAlphabetic("ax4WpF7jPU0DAEfs1bkDAGEcooO5rmzg".length()));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("生成UmtidToken失败");
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
            jsonParams.put("device_global_id", taobaoAccountEntity.getUtdid() + c2 + c3);
            jsonParams.put("c6", c6);
            jsonParams.put("c4", c4);
            jsonParams.put("new_device", "true");
            jsonParams.put("c5", c5);
            jsonParams.put("c3", c3);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            H5Header h5Header = new H5Header(taobaoAccountEntity, "21523971");
            String subUrl = "mtop.sys.newdeviceid";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", HFStringUtils.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "4.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/4.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(HFStringUtils.valueOf(urlParams.get(key))) + "&";
            }

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)"),
                    new Request("GET", url, ResponseType.TEXT),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error("获取机器码失败");
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);

            TaobaoReturn taobaoReturn = new TaobaoReturn(map);
            if (taobaoReturn.getErrorCode() != ErrorCodes.SUCCESS) {
                return R.error(taobaoReturn.getErrorCode(), taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            String deviceId = HFStringUtils.valueOf(mapData.get("device_id"));

            return R.ok()
                    .put("device_id", deviceId);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("获取机器码失败");
    }

    @Override
    public R uploadImage(Path path, TaobaoAccountEntity taobaoAccountEntity) {
        try {
            String url = "https://liveplatform.taobao.com/live/action.do";

            File file = path.toFile();
            HttpEntity httpEntity = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addBinaryBody("filedata", file, ContentType.DEFAULT_BINARY, file.getName())
                    .addTextBody("_tb_token_", taobaoAccountEntity.getToken())
                    .addTextBody("api", "pic_common_upload")
                    .addTextBody("name", path.toAbsolutePath().toString()).build();

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                            .addHeader("Referer", "https://liveplatform.taobao.com/live/addLive.htm"),
                    new Request("POST", url, ResponseType.TEXT)
                            .setEntity(httpEntity),
                    new DefaultCookieStorePool(taobaoAccountEntity.getCookieStore()));

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

            Map<String, Object> mapModel = (Map<String, Object>) map.get("model");
            String filePath = HFStringUtils.valueOf(mapModel.get("tfsFilePath"));

            return R.ok().put("file_path", "https://gw.alicdn.com/tfscom/" + filePath);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("上传图片失败");
    }

    @Override
    public String parseProductId(String url) {
        url = StringUtils.strip(url, "\"\r\n ");
        if (url.indexOf("taobao.com") < 0 && url.indexOf("tmall.com") < 0) {
            return null;
        }

        String[] words = url.split("/:&//?/");
        for (String word : words) {
            if (word.startsWith("itemId=")) {
                return word.substring("itemId=".length());
            } else if (word.startsWith("id=")) {
                return word.substring("id=".length());
            }
        }
        return null;
    }
}
