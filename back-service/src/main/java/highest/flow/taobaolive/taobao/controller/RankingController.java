package highest.flow.taobaolive.taobao.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import highest.flow.taobaolive.api.param.*;
import highest.flow.taobaolive.common.annotation.SysLog;
import highest.flow.taobaolive.common.defines.ErrorCodes;
import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.http.HttpHelper;
import highest.flow.taobaolive.common.http.Request;
import highest.flow.taobaolive.common.http.ResponseType;
import highest.flow.taobaolive.common.http.SiteConfig;
import highest.flow.taobaolive.common.http.httpclient.response.Response;
import highest.flow.taobaolive.common.utils.CommonUtils;
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
import highest.flow.taobaolive.taobao.provider.TaobaoApiProvider;
import highest.flow.taobaolive.taobao.service.RankingService;
import highest.flow.taobaolive.taobao.service.TaobaoAccountService;
import highest.flow.taobaolive.taobao.service.TaobaoApiService;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
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
    private RankingService rankingService;

    @Autowired
    private TaobaoApiService taobaoLiveApiService;

    @Value("${ranking.max-sync-count:20}")
    private int maxSyncCount; // 同时刷的最多线程数

    @PostConstruct
    public void init() {

    }

    @PostMapping("/parse_taocode")
    public R parseTaocode(@RequestBody Map<String, Object> param) {
        try {
            String taocode = ((String) param.get("taocode")).trim();
            boolean hasFollow = (boolean) param.get("has_follow");
            boolean hasStay = (boolean) param.get("has_stay");
            boolean hasBuy = (boolean) param.get("has_buy");
            boolean hasDoubleBuy = (boolean) param.get("has_double_buy");

//            String url = "http://www.taofake.com/index/tools/gettkljm.html?tkl=" + URLEncoder.encode(taocode);
//
//            Response<String> response = HttpHelper.execute(
//                    new SiteConfig()
//                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
//                            .addHeader("Referer", "http://www.taofake.com/tools/tkljm/")
//                            .addHeader("X-Requested-With", "XMLHttpRequest"),
//                    new Request("GET", url, ResponseType.TEXT));
//
//            if (response.getStatusCode() != HttpStatus.SC_OK) {
//                return R.error("解析淘口令失败");
//            }
//
//            String respText = response.getResult();
//            JsonParser jsonParser = JsonParserFactory.getJsonParser();
//            Map<String, Object> map = jsonParser.parseMap(respText);
//            int code = (int) map.get("code");
//            if (code != 1) {
//                return R.error("解析淘口令失败");
//            }
//
//            Map<String, Object> mapData = (Map<String, Object>) map.get("data");
//            String talentUrl = (String) mapData.get("url");
//            String content = (String) mapData.get("content");
//
//            talentUrl = URLDecoder.decode(talentUrl);
//            String liveId = "";
//            int pos = talentUrl.indexOf("\"feed_id\":");
//            if (pos > 0) {
//                pos += "\"feed_id\":".length();
//                int nextpos = talentUrl.indexOf("\"", pos + 1);
//                liveId = talentUrl.substring(pos + 1, nextpos);
//            }
//
//            pos = talentUrl.indexOf("\"account_id\":");
//            String accountId = "";
//            if (pos > 0) {
//                pos += "\"account_id\":".length();
//                int nextPos = talentUrl.indexOf("\"", pos + 1);
//                accountId = talentUrl.substring(pos + 1, nextPos);
//            }
//
//            String accountName = content;
//            pos = content.indexOf("的直播");
//            if (pos > 0) {
//                accountName = content.substring(0, pos);
//            }
//
//            // 获取赛道信息
//            LiveRoomEntity liveRoomEntity = new LiveRoomEntity();
//            liveRoomEntity.setLiveId(liveId);
//            liveRoomEntity.setAccountId(accountId);
//            liveRoomEntity.setAccountName(accountName);

            int pos = taocode.indexOf("￥");
            if (pos >= 0) {
                taocode = taocode.substring(pos + 1);
            }
            pos = taocode.indexOf("￥");
            if (pos >= 0) {
                taocode = taocode.substring(0, pos);
            }

            R r = this.taobaoLiveApiService.getLiveInfo(taocode, null);
            if (r.getCode() != ErrorCodes.SUCCESS) {
                return r;
            }

            LiveRoomEntity liveRoomEntity = (LiveRoomEntity) r.get("live_room");

            List<TaobaoAccountEntity> activeAccounts = this.taobaoAccountService.getActivesByMember(null, Config.MAX_RETRY_ACCOUNTS);

            for (int retry = 0; activeAccounts != null && retry < activeAccounts.size(); retry++) {
                this.taobaoLiveApiService.getH5Token(activeAccounts.get(retry));
                r = this.taobaoLiveApiService.getRankingListData(liveRoomEntity, activeAccounts.get(retry));
                if (r.getCode() == ErrorCodes.SUCCESS)
                    break;
            }

            // 可用热度值
            SysMember sysMember = this.getUser();

            List<TaobaoAccountEntity> taobaoAccountEntities = this.rankingService.availableAccounts(sysMember, liveRoomEntity.getLiveId());

            int unitScore = hasDoubleBuy ? this.rankingService.getRankingUnitScore(RankingScore.DoubleBuy) :
                    (hasBuy ? this.rankingService.getRankingUnitScore(RankingScore.Buy) : 0);
            unitScore += hasFollow ? this.rankingService.getRankingUnitScore(RankingScore.Follow) : 0;
            unitScore += hasStay ? this.rankingService.getRankingUnitScore(RankingScore.Stay) : 0;

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
            boolean hasFollow = (boolean) param.get("has_follow");
            boolean hasStay = (boolean) param.get("has_stay");
            boolean hasBuy = (boolean) param.get("has_buy");
            boolean hasDoubleBuy = (boolean) param.get("has_double_buy");

            SysMember sysMember = this.getUser();

            List<TaobaoAccountEntity> taobaoAccountEntities = this.rankingService.availableAccounts(sysMember, liveId);

            int unitScore = hasDoubleBuy ? this.rankingService.getRankingUnitScore(RankingScore.DoubleBuy) :
                    (hasBuy ? this.rankingService.getRankingUnitScore(RankingScore.Buy) : 0);
            unitScore += hasFollow ? this.rankingService.getRankingUnitScore(RankingScore.Follow) : 0;
            unitScore += hasStay ? this.rankingService.getRankingUnitScore(RankingScore.Stay) : 0;

            int count = taobaoAccountEntities == null || taobaoAccountEntities.size() < 1 ? 0 : taobaoAccountEntities.size();

            return R.ok()
                    .put("live_id", liveId)
                    .put("limit_score", unitScore * count);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("获取可用热度值失败");
    }

    @SysLog("创建刷热度任务")
    @PostMapping("/add_task")
    public R addTask(@RequestBody AddRankingTaskParam param) {
        try {
            String taocode = param.getTaocode();
            String liveId = param.getLiveId();
            String accountId = param.getAccountId();
            String accountName = param.getAccountName();
            int targetScore = param.getTargetScore();
            boolean hasFollow = param.isHasFollow();
            boolean hasStay = param.isHasStay();
            boolean hasBuy = param.isHasBuy();
            boolean hasDoubleBuy = param.isHasDoubleBuy();
            Date startTime = param.getStartTime();
            String comment = param.getComment();

            if (startTime != null && startTime.getTime() < new Date().getTime()) {
                return R.error("请正确输入开始时间");
            }

            SysMember sysMember = this.getUser();

            RankingEntity currentEntity = this.rankingService.getRunningTask(sysMember, liveId);
            if (currentEntity != null) {
                return R.error("已经添加好了该直播间");
            }

            List<RankingEntity> runningEntities = this.rankingService.getRunningTasks(sysMember);
            if (runningEntities != null && runningEntities.size() >= maxSyncCount) {
                return R.error("已经刷热度任务上限了，请等到任务完成");
            }

            // 创建刷热度任务
            RankingEntity rankingEntity = this.rankingService.addNewTask(sysMember,
                    taocode,
                    liveId, accountId, accountName,
                    targetScore,
                    hasFollow,
                    hasStay,
                    hasBuy,
                    hasDoubleBuy,
                    startTime,
                    comment);

            if (rankingEntity == null) {
                return R.error("创建刷热度任务失败");
            }

            return R.ok().put("task_id", rankingEntity.getId());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("创建刷热度任务失败");
    }

    //@SysLog("获取当天的任务")
    @PostMapping("/todays")
    public R todays(@RequestBody TodayRankingParam param) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            // 前一天没有结束的也要返回
            Date today = param.getCurrentDate();
            Date yesterday = CommonUtils.addDays(today, -1);
            List<RankingEntity> yesterdayEntities = this.rankingService.getTodaysTask(sdf.format(yesterday));
            List<RankingEntity> todayEntities = this.rankingService.getTodaysTask(sdf.format(today));

            for (int idx = yesterdayEntities.size() - 1; idx >= 0; idx--) {
                RankingEntity rankingEntity = yesterdayEntities.get(idx);
                if (rankingEntity.getState() != RankingEntityState.Running.getState()) {
                    yesterdayEntities.remove(idx);
                }
            }

            yesterdayEntities.addAll(todayEntities);

            return R.ok().put("ranking", yesterdayEntities);

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
        return R.error("开始任务失败");
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

            if (this.rankingService.stopTask(rankingEntity) == false) {
                rankingEntity.setEndTime(new Date());
                rankingEntity.setUpdatedTime(new Date());
                rankingEntity.setState(RankingEntityState.Stopped.getState());
                this.rankingService.updateById(rankingEntity);
            }

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("找不到任务");
    }

    @SysLog("删除任务")
    @PostMapping("/delete_task")
    public R deleteTask(@RequestBody Map<String, Object> param) {
        try {
            int taskId = (int) param.get("task_id");

            RankingEntity rankingEntity = this.rankingService.getOne(Wrappers.<RankingEntity>lambdaQuery()
                    .eq(RankingEntity::getId, taskId));

            if (rankingEntity == null) {
                return R.error("找不到任务");
            }

            SysMember sysMember = this.getUser();
            if (!sysMember.isAdministrator()) {
                return R.error("只有管理员能删除");
            }

            boolean success = this.rankingService.deleteTask(rankingEntity);

            return success ? R.ok() : R.error("删除任务失败");


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("删除任务失败");
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

