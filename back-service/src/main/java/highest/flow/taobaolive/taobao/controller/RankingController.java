package highest.flow.taobaolive.taobao.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import highest.flow.taobaolive.api.param.*;
import highest.flow.taobaolive.common.annotation.SysLog;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.SiteConfig;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.sys.controller.AbstractController;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.taobao.defines.RankingEntityState;
import highest.flow.taobaolive.taobao.defines.RankingScore;
import highest.flow.taobaolive.taobao.defines.TaobaoAccountState;
import highest.flow.taobaolive.taobao.entity.LiveRoomEntity;
import highest.flow.taobaolive.taobao.entity.RankingEntity;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import highest.flow.taobaolive.taobao.service.RankingService;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/v1.0/ranking")
public class RankingController extends AbstractController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TaobaoAccountService taobaoAccountService;

    @Autowired
    private TaobaoApiService taobaoApiService;

    @Autowired
    private RankingService rankingService;

    @PostMapping("/parse_taocode")
    public R parseTaocode(@RequestBody Map<String, Object> param) {
        try {
            String taocode = (String) param.get("taocode");
            boolean doubleBuy = (boolean) param.get("double_buy");

            String url = "http://www.taofake.com/index/tools/gettkljm.html?tkl=" + URLEncoder.encode(taocode);

            Response<String> response = HttpHelper.execute(
                    new SiteConfig()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                            .addHeader("Referer", "http://www.taofake.com/tools/tkljm/")
                            .addHeader("X-Requested-With", "XMLHttpRequest"),
                    new Request("GET", url, ResponseType.TEXT));

            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return R.error("解析淘口令失败");
            }

            String respText = response.getResult();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> map = jsonParser.parseMap(respText);
            int code = (int) map.get("code");
            if (code != 1) {
                return R.error("解析淘口令失败");
            }

            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
            String talentUrl = (String) mapData.get("url");
            String content = (String) mapData.get("content");

            talentUrl = URLDecoder.decode(talentUrl);
            String liveId = "";
            int pos = talentUrl.indexOf("&id=");
            if (pos > 0) {
                pos += "&id=".length();
                int nextpos = talentUrl.indexOf("&", pos + 1);
                liveId = talentUrl.substring(pos, nextpos);
            }

            pos = talentUrl.indexOf("\"account_id\":");
            String accountId = "";
            if (pos > 0) {
                pos += "\"account_id\":".length();
                int nextPos = talentUrl.indexOf("\"", pos + 1);
                accountId = talentUrl.substring(pos + 1, nextPos);
            }

            String accountName = content;
            pos = content.indexOf("的直播");
            if (pos > 0) {
                accountName = content.substring(0, pos);
            }

            // 获取赛道信息
            LiveRoomEntity liveRoomEntity = new LiveRoomEntity();
            liveRoomEntity.setLiveId(liveId);
            liveRoomEntity.setAccountId(accountId);
            liveRoomEntity.setAccountName(accountName);

            TaobaoAccountEntity taobaoAccountEntity = this.taobaoAccountService.getActiveOne(null);

            if (taobaoAccountEntity != null) {
                this.taobaoApiService.getH5Token(taobaoAccountEntity);
                this.taobaoApiService.getLiveEntry(liveRoomEntity, taobaoAccountEntity);
            }

            // 可用热度值
            SysMember sysMember = this.getUser();

            List<TaobaoAccountEntity> taobaoAccountEntities = this.rankingService.availableAccounts(sysMember, liveId);

            int unitScore = doubleBuy ?
                    (RankingScore.DoubleBuyFollow.getScore() + RankingScore.DoubleBuyBuy.getScore() + RankingScore.DoubleBuyWatch.getScore()) :
                    (RankingScore.Follow.getScore() + RankingScore.Buy.getScore() + RankingScore.Watch.getScore());

            int count = taobaoAccountEntities == null || taobaoAccountEntities.size() < 1 ? 0 : taobaoAccountEntities.size();

            return R.ok()
                    .put("live_room", liveRoomEntity)
                    .put("limit_score", unitScore * count);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("解析淘口令失败");
    }

    @PostMapping("/limit_score")
    public R getLimitScore(@RequestBody Map<String, Object> param) {
        try {
            String liveId = (String) param.get("live_id");
            boolean doubleBuy = (boolean) param.get("double_buy");

            SysMember sysMember = this.getUser();

            List<TaobaoAccountEntity> taobaoAccountEntities = this.rankingService.availableAccounts(sysMember, liveId);

            int unitScore = doubleBuy ?
                    (RankingScore.DoubleBuyFollow.getScore() + RankingScore.DoubleBuyBuy.getScore() + RankingScore.DoubleBuyWatch.getScore()) :
                    (RankingScore.Follow.getScore() + RankingScore.Buy.getScore() + RankingScore.Watch.getScore());

            int count = taobaoAccountEntities == null || taobaoAccountEntities.size() < 1 ? 0 : taobaoAccountEntities.size();

            return R.ok().put("limit_score", unitScore * count);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("获取可用热度值失败");
    }

    @SysLog("添加新热度任务")
    @PostMapping("/add_task")
    public R addTask(@RequestBody AddRankingTaskParam param) {
        try {
            String taocode = param.getTaocode();
            int targetScore = param.getTargetScore();
            boolean doubleBuy = param.isDoubleBuy();
            Date startTime = param.getStartTime();

            if (startTime != null && startTime.getTime() < new Date().getTime()) {
                return R.error("请正确输入开始时间");
            }

            TaobaoAccountEntity taobaoAccountEntity = this.taobaoAccountService.getActiveOne(getUser());
            if (taobaoAccountEntity == null) {
                return R.error("找不到活跃的用户");
            }

            logger.debug("找到活跃用户：" + taobaoAccountEntity.getNick());

            R r = taobaoApiService.getLiveInfo(taocode, taobaoAccountEntity);
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            LiveRoomEntity liveRoomEntity = (LiveRoomEntity) r.get("live_room");

            SysMember sysMember = this.getUser();

            RankingEntity rankingEntity = this.rankingService.addNewTask(sysMember,
                    taocode,
                    liveRoomEntity,
                    targetScore,
                    doubleBuy,
                    startTime);

            if (rankingEntity == null) {
                return R.error();
            }

            return R.ok().put("task_id", rankingEntity.getId());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("添加助力任务失败");
    }

    @SysLog("添加新热度任务")
    @PostMapping("/add_task2")
    public R addTask2(@RequestBody AddRankingTaskParam2 param) {
        try {
            String taocode = param.getTaocode();
            String liveId = param.getLiveRoom().getLiveId();
            String accountId = param.getLiveRoom().getAccountId();
            String scopeId = param.getLiveRoom().getScopeId();
            String subScopeId = param.getLiveRoom().getSubScopeId();

            int targetScore = param.getTargetScore();
            boolean doubleBuy = param.isDoubleBuy();
            Date startTime = param.getStartTime();

            LiveRoomEntity liveRoomEntity = new LiveRoomEntity();

            liveRoomEntity.setLiveId(liveId);
            liveRoomEntity.setAccountId(accountId);
            liveRoomEntity.getHierarchyData().setScopeId(scopeId);
            liveRoomEntity.getHierarchyData().setSubScopeId(subScopeId);

            SysMember sysMember = this.getUser();

            RankingEntity rankingEntity = this.rankingService.addNewTask(sysMember,
                    taocode,
                    liveRoomEntity,
                    targetScore,
                    doubleBuy,
                    startTime);

            if (rankingEntity == null) {
                return R.error();
            }

            return R.ok().put("task_id", rankingEntity.getId());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("添加助力任务失败");
    }

    //@SysLog("获取当天的任务")
    @PostMapping("/todays")
    public R todays(@RequestBody TodayRankingParam param) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String date = sdf.format(param.getCurrentDate());
            List<RankingEntity> rankingEntities = this.rankingService.getTodaysTask(date);

            return R.ok().put("ranking", rankingEntities);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @SysLog("获取任务执行情况")
    @PostMapping("/get")
    public R get(@RequestBody Map<String, Object> param) {
        try {

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @SysLog("开始任务")
    @PostMapping("/start_task")
    public R startTask(@RequestBody ControlRankingTaskParam param) {
        try {
            int taskId = param.getTaskId();

            RankingEntity rankingEntity = this.rankingService.getById(taskId);
            if (rankingEntity == null) {
                return R.error("找不到任务");
            }

            if (rankingEntity.getState() == RankingEntityState.Running.getState()) {
                return R.error("已经开始了");
            }
            if (rankingEntity.getState() == RankingEntityState.Error.getState()) {
                return R.error("有错误");
            }
            if (rankingEntity.getState() == RankingEntityState.Finished.getState()) {
                return R.error("已经结束了");
            }

            if (!this.rankingService.startTask(rankingEntity)) {
                return R.error("找不到等待任务");
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @SysLog("停止任务")
    @PostMapping("/stop_task")
    public R stopTask(@RequestBody ControlRankingTaskParam param) {
        try {
            int taskId = param.getTaskId();

            RankingEntity rankingEntity = this.rankingService.getOne(Wrappers.<RankingEntity>lambdaQuery()
                    .eq(RankingEntity::getId, taskId));

            if (rankingEntity == null) {
                return R.error("找不到任务");
            }

            this.rankingService.stopTask(rankingEntity);

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/logs")
    public R logs(@RequestBody PageParam pageParam) {
        try {
            SysMember sysMember = this.getUser();
            PageUtils pageUtils = this.rankingService.queryPage(sysMember, pageParam);

            return R.ok().put("logs", pageUtils.getList()).put("total_count", pageUtils.getTotalCount());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }
}

