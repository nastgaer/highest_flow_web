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
                            .setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)")
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
                return R.error(taobaoReturn.getErrorCode(), taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            String creatorId = String.valueOf(mapData.get("taopwdOwnerId"));
            String talentLiveUrl = String.valueOf(mapData.get("url"));
            String liveId = "";
            String[] words = talentLiveUrl.split("[::?&/]");
            for (String word : words) {
                if (word.startsWith("id=")) {
                    liveId = word.substring("id=".length());
                    break;
                }
            }

            return R.ok()
                    .put("creator_id", creatorId)
                    .put("talent_live_url", talentLiveUrl)
                    .put("live_id", liveId);

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
                return R.error(taobaoReturn.getErrorCode(), taobaoReturn.getErrorMsg());
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            Map<String, Object> mapBroadCaster = (Map<String, Object>) mapData.get("broadCaster");
            String accountId = String.valueOf(mapBroadCaster.get("accountId"));
            String accountName = String.valueOf(mapBroadCaster.get("accountName"));
            int fansNum = Integer.parseInt(String.valueOf(mapBroadCaster.get("fansNum")));

            String topic = String.valueOf(mapData.get("topic"));
            int viewCount = Integer.parseInt(String.valueOf(mapData.get("viewCount")));
            int praiseCount = Integer.parseInt(String.valueOf(mapData.get("praiseCount")));
            int onlineCount = Integer.parseInt(String.valueOf(mapData.get("joinCount")));

            long startTimestamp = Long.parseLong(String.valueOf(mapData.get("startTime")));
            Date startTime = CommonUtils.timestampToDate(startTimestamp);
            String coverImg = String.valueOf(mapData.get("coverImg"));
            String coverImg169 = String.valueOf(mapData.get("coverImg169"));
            String title = String.valueOf(mapData.get("title"));
            String intro = String.valueOf(mapData.get("descInfo"));
            int channelId = Integer.parseInt(String.valueOf(mapData.get("liveChannelId")));
            int columnId = Integer.parseInt(String.valueOf(mapData.get("liveColumnId")));
            String location = String.valueOf(mapData.get("location"));
            int roomStatus = Integer.parseInt(String.valueOf(mapData.get("roomStatus")));

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
    public R getLiveProducts(LiveRoomEntity liveRoomEntity, TaobaoAccountEntity taobaoAccountEntity) {
        try {
            Map<String, Object> jsonParams = new HashMap<>();
            jsonParams.put("groupNum", "0");
            jsonParams.put("liveId", liveRoomEntity.getLiveId());
            jsonParams.put("n", "350");
            jsonParams.put("type", "0");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonText = objectMapper.writeValueAsString(jsonParams);

            H5Header h5Header = new H5Header(taobaoAccountEntity);
            String subUrl = "mtop.mediaplatform.video.livedetail.itemlist.withpagination";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", String.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "4.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/4.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(String.valueOf(urlParams.get(key))) + "&";
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
            int totalNum = Integer.parseInt(String.valueOf(mapData.get("totalNum")));

            List<ProductEntity> productEntities = new ArrayList<>();
            List<Map<String, Object>> itemList = (List<Map<String, Object>>) mapData.get("itemList");
            for (Map<String, Object> mapItem : itemList) {
                List<Map<String, Object>> goodsList = (List<Map<String, Object>>) mapItem.get("goodsList");
                if (goodsList.size() < 1) {
                    continue;
                }

                for (Map<String, Object> goodObj : goodsList) {
                    String itemId = String.valueOf(goodObj.get("itemId"));
                    String itemName = String.valueOf(goodObj.get("itemName"));
                    String itemPic = String.valueOf(goodObj.get("itemPic"));
                    String itemPrice = String.valueOf(goodObj.get("itemPrice"));
                    String itemUrl = "https://item.taobao.com/item.htm?id=" + itemId; // String.valueOf(goodObj.get("itemUrl"));

                    ProductEntity productEntity = new ProductEntity();
                    productEntity.setProductId(itemId);
                    productEntity.setTitle(itemName);
                    productEntity.setPicurl(itemPic);
                    productEntity.setUrl(itemUrl);
                    productEntity.setPrice(itemPrice);

                    Map<String, Object> mapExtendVal = (Map<String, Object>) goodObj.get("extendVal");
                    if (mapExtendVal != null) {
                        if (mapExtendVal.containsKey("timepoint")) {
                            productEntity.setTimepoint(Long.parseLong(String.valueOf(mapExtendVal.get("timepoint"))));
                        }

                        productEntity.setMonthSales(Integer.parseInt(String.valueOf(mapExtendVal.get("buyCount"))));
                        productEntity.setCategoryId(String.valueOf(mapExtendVal.get("categoryLevelLeaf")));
                        productEntity.setCategoryTitle(String.valueOf(mapExtendVal.get("categoryLevelOneName")));

                        String business = String.valueOf(mapExtendVal.get("business"));
                        Map<String, Object> mapBusiness = jsonParser.parseMap(business);

                        Map<String, Object> mapCpsTcpInfo = (Map<String, Object>) mapBusiness.get("cpsTcpInfo");
                        Map<String, Object> mapTaobaoLivetoc = (Map<String, Object>) mapCpsTcpInfo.get("taobaolivetoc");
                        productEntity.setBusinessSceneId(Integer.parseInt(String.valueOf(mapTaobaoLivetoc.get("businessScenceId"))));

                        Map<String, Object> mapItemBizInfo = (Map<String, Object>) mapBusiness.get("itemBizInfo");
                        String itemJumpUrl = String.valueOf(mapItemBizInfo.get("itemJumpUrl"));
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

                    productEntity.setLiveId(String.valueOf(goodObj.get("liveId")));

                    productEntities.add(productEntity);
                }
            }

            liveRoomEntity.setProducts(productEntities);

            return R.ok()
                    .put("products", productEntities)
                    .put("total_num", totalNum);

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

            H5Header h5Header = new H5Header(taobaoAccountEntity);
            String subUrl = "mtop.mediaplatform.livedetail.entry";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", String.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "1.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/1.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(String.valueOf(urlParams.get(key))) + "&";
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
            liveRoomEntity.setHasRankingEntry(Boolean.parseBoolean(String.valueOf(mapData.get("hasRankingListEntry"))));
            if (!liveRoomEntity.isHasRankingEntry()) {
                liveRoomEntity.getRankingListData().setRankingScore(0);
                liveRoomEntity.getRankingListData().setRankingNum(0);
                liveRoomEntity.getRankingListData().setRankingName("");

            } else {
                Map<String, Object> mapRankingListData = (Map<String, Object>) mapData.get("rankingListData");
                Map<String, Object> mapBizData = (Map<String, Object>) mapRankingListData.get("bizData");

                liveRoomEntity.getRankingListData().setRankingScore(Integer.parseInt(String.valueOf(mapBizData.get("score"))));
                liveRoomEntity.getRankingListData().setRankingNum(Integer.parseInt(String.valueOf(mapBizData.get("rankNum"))));
                liveRoomEntity.getRankingListData().setRankingName(String.valueOf(mapBizData.get("name")));
            }

            Map<String, Object> mapHierachyData = (Map<String, Object>) mapData.get("hierarchyData");
            liveRoomEntity.getHierarchyData().setScopeId(mapHierachyData == null ? "-1" : String.valueOf(mapHierachyData.get("scopeId")));
            liveRoomEntity.getHierarchyData().setSubScopeId(mapHierachyData == null ? "-1" : String.valueOf(mapHierachyData.get("subScopeId")));

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

            LiveRoomEntity liveRoomEntity = new LiveRoomEntity();
            liveRoomEntity.setLiveId((String) r.get("live_id"));
            liveRoomEntity.setCreatorId((String) r.get("creator_id"));
            liveRoomEntity.setTalentLiveUrl((String) r.get("talent_live_url"));

            String liveId = (String) r.get("live_id");
            r = this.getLiveDetail(liveId);
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
                r = this.getLiveEntry(liveRoomEntity, taobaoAccountEntity);
                if (r.getCode() != ErrorCodes.SUCCESS) {
                    return r;
                }
            }

            return R.ok().put("live_room", liveRoomEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @Override
    public R getLiveList(TaobaoAccountEntity taobaoAccountEntity, int pageNo, int pageSize) {
        try {
            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("currentPage", String.valueOf(pageNo));
            urlParams.put("pagesize", String.valueOf(pageSize));
            urlParams.put("api", "get_live_list");

            String url = "https://liveplatform.taobao.com/live/action.do?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(String.valueOf(urlParams.get(key))) + "&";
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

                liveRoomEntity.setLiveId(String.valueOf(objData.get("id")));
                liveRoomEntity.setAccountId(String.valueOf(objData.get("accountId")));
                liveRoomEntity.setAccountName(String.valueOf(objData.get("nick")));
                liveRoomEntity.setTalentLiveUrl(String.valueOf(objData.get("liveUrl")));
                liveRoomEntity.setTopic(String.valueOf(objData.get("topic")));
                long startTimestamp = Long.parseLong(String.valueOf(objData.get("startTime")));
                liveRoomEntity.setLiveAppointmentTime(CommonUtils.timestampToDate(startTimestamp));
                liveRoomEntity.setLiveCoverImg(String.valueOf(objData.get("coverImg")));
                int channelId = Integer.parseInt(String.valueOf(objData.get("liveChannelId")));
                int columnId = Integer.parseInt(String.valueOf(objData.get("liveColumnId")));
                liveRoomEntity.setLiveChannelId(channelId);
                liveRoomEntity.setLiveColumnId(columnId);
                liveRoomEntity.setLiveLocation(String.valueOf(objData.get("location")));
                int status = Integer.parseInt(String.valueOf(objData.get("roomStatus")));
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
            R r = this.getLiveList(taobaoAccountEntity, 1, 20);
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

            H5Header h5Header = new H5Header(taobaoAccountEntity);
            String subUrl = "mtop.taobao.iliad.task.action";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", String.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "1.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/1.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(String.valueOf(urlParams.get(key))) + "&";
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

            H5Header h5Header = new H5Header(taobaoAccountEntity);
            String subUrl = "mtop.taobao.iliad.task.action";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", String.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "1.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/1.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(String.valueOf(urlParams.get(key))) + "&";
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

            H5Header h5Header = new H5Header(taobaoAccountEntity);
            String subUrl = "mtop.taobao.iliad.task.action";

            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("appKey", h5Header.getAppKey());
            urlParams.put("t", String.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "1.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/1.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(String.valueOf(urlParams.get(key))) + "&";
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
            postParams.put("liveChannelId", String.valueOf(preLiveRoomSpec.getLiveChannelId()));
            postParams.put("liveColumnId", String.valueOf(preLiveRoomSpec.getLiveColumnId()));
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
            postParams.put("appointmentTime", String.valueOf(CommonUtils.dateToTimestamp(preLiveRoomSpec.getLiveAppointmentTime())));
            postParams.put("liveEndTime", String.valueOf(CommonUtils.dateToTimestamp(CommonUtils.addDays(preLiveRoomSpec.getLiveAppointmentTime(), 30))));

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
            liveRoomEntity.setLiveId(String.valueOf(mapModel.get("preLiveId")));
            liveRoomEntity.setLiveAppointmentTime(preLiveRoomSpec.getLiveAppointmentTime());
            liveRoomEntity.setLiveCoverImg(preLiveRoomSpec.getLiveCoverImg());
            liveRoomEntity.setLiveCoverImg169(preLiveRoomSpec.getLiveCoverImg169());
            liveRoomEntity.setLiveTitle(preLiveRoomSpec.getLiveTitle());
            liveRoomEntity.setLiveIntro(preLiveRoomSpec.getLiveIntro());
            liveRoomEntity.setLiveChannelId(preLiveRoomSpec.getLiveChannelId());
            liveRoomEntity.setLiveColumnId(preLiveRoomSpec.getLiveColumnId());
            liveRoomEntity.setLiveLocation(preLiveRoomSpec.getLiveLocation());

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
            jsonParams.put("appointmentTime", String.valueOf(CommonUtils.dateToTimestamp(preLiveRoomSpec.getLiveAppointmentTime())));
            jsonParams.put("title", preLiveRoomSpec.getLiveTitle());
            jsonParams.put("intro", preLiveRoomSpec.getLiveIntro());
            jsonParams.put("itemIds", "");
            jsonParams.put("liveChannelId", String.valueOf(preLiveRoomSpec.getLiveChannelId()));
            jsonParams.put("liveColumnId", String.valueOf(preLiveRoomSpec.getLiveColumnId()));
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
                            .setUserAgent("MTOPSDK/3.1.1.7 (Android;5.1.1)")
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
            liveRoomEntity.setLiveId(String.valueOf(mapData.get("liveId")));
            liveRoomEntity.setAccountId(String.valueOf(mapData.get("accountId")));
            liveRoomEntity.setTopic(String.valueOf(mapData.get("topic")));
            liveRoomEntity.setViewCount(Integer.parseInt(String.valueOf(mapData.get("viewCount"))));
            liveRoomEntity.setPraiseCount(Integer.parseInt(String.valueOf(mapData.get("praiseCount"))));
            liveRoomEntity.setLiveAppointmentTime(preLiveRoomSpec.getLiveAppointmentTime());
            liveRoomEntity.setLiveCoverImg(String.valueOf(mapData.get("coverImg")));
            liveRoomEntity.setLiveCoverImg169(String.valueOf(mapData.get("coverImg169")));
            liveRoomEntity.setLiveTitle(String.valueOf(mapData.get("title")));
            liveRoomEntity.setLiveIntro(String.valueOf(mapData.get("intro")));
            liveRoomEntity.setLiveChannelId(Integer.parseInt(String.valueOf(mapData.get("liveChannelId"))));
            liveRoomEntity.setLiveColumnId(Integer.parseInt(String.valueOf(mapData.get("liveColumnId"))));
            liveRoomEntity.setLiveLocation(String.valueOf(mapData.get("location")));

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
            String imgUrl = "//gw.alicdn.com/img/bao/uploaded/" + String.valueOf(mapModel.get("imgUrl"));
            String title = String.valueOf(mapModel.get("itemTitle"));
            String price = String.valueOf(mapModel.get("itemPrice"));

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
                            .setUserAgent("MTOPSDK/3.1.1.7 (Android;5.1.1)")
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

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(String.valueOf(urlParams.get(key))) + "&";
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
                            .setUserAgent("MTOPSDK/3.1.1.7 (Android;5.1.1)")
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

            String autoLoginToken = String.valueOf(mapRetData.get("autoLoginToken"));
            long expires = Long.parseLong(String.valueOf(mapRetData.get("expires")));
            List<String> lstCookieHeaders = (List<String>) mapRetData.get("cookies");

            List<Cookie> lstCookies = CookieHelper.parseCookieHeaders(url, lstCookieHeaders);

            String sid = String.valueOf(mapRetData.get("sid"));
            String uid = String.valueOf(mapRetData.get("userId"));
            String nick = String.valueOf(mapRetData.get("nick"));

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
            String lgToken = String.valueOf(map.get("at"));
            String qrCodeUrl = String.valueOf(map.get("url"));
            long timestamp = Long.parseLong(String.valueOf(map.get("t")));

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
                url += key + "=" + URLEncoder.encode(String.valueOf(jsonParams.get(key))) + "&";
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
            tokenInfo.put("t", String.valueOf(qrCode.getTimestamp()));

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
            String data = String.valueOf(mapReturnValue.get("data"));
            Map<String, Object> mapRetData = jsonParser.parseMap(data);

            String autoLoginToken = String.valueOf(mapRetData.get("autoLoginToken"));
            long expires = Long.parseLong(String.valueOf(mapRetData.get("expires")));
            List<String> lstCookieHeaders = (List<String>) mapRetData.get("cookies");

            List<Cookie> lstCookies = CookieHelper.parseCookieHeaders(url, lstCookieHeaders);

            String sid = String.valueOf(mapRetData.get("sid"));
            String uid = String.valueOf(mapRetData.get("userId"));
            String nick = String.valueOf(mapRetData.get("nick"));

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
                String umtid = String.valueOf(testMap.get("test"));
                return R.ok()
                    .put("umtid", umtid.replace("{", "").replace("}", ""));
            }

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

            String url = "https://acs.m.taobao.com/h5/" + subUrl + "/1.0/?";
            for (String key : paramMap.keySet()) {
                url += key + "=" + URLEncoder.encode(String.valueOf(paramMap.get(key))) + "&";
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
            urlParams.put("t", String.valueOf(h5Header.getLongTimestamp()));
            urlParams.put("api", subUrl);
            urlParams.put("v", "4.0");
            urlParams.put("data", jsonText);
            urlParams.put("sign", signService.h5sign(h5Header, jsonText));

            String url = "https://h5api.m.taobao.com/h5/" + subUrl + "/4.0/?";

            for (String key : urlParams.keySet()) {
                url += key + "=" + URLEncoder.encode(String.valueOf(urlParams.get(key))) + "&";
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
            String deviceId = String.valueOf(mapData.get("device_id"));

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
            String filePath = String.valueOf(mapModel.get("tfsFilePath"));

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
