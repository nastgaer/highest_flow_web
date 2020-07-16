package highest.flow.taobaolive.taobao.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.PageUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.sys.controller.AbstractController;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.taobao.entity.PreLiveTemplateEntity;
import highest.flow.taobaolive.taobao.entity.TemplateEntity;
import highest.flow.taobaolive.taobao.service.PreLiveTemplateService;
import highest.flow.taobaolive.taobao.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/v1.0/live/template")
public class LiveTemplateController extends AbstractController {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PreLiveTemplateService preLiveTemplateService;

    @PostMapping("/list")
    public R list(@RequestBody PageParam pageParam) {
        try {
            PageUtils pageUtils = this.templateService.queryPage(pageParam);

            List<TemplateEntity> templateEntities = pageUtils.getList();

            for (TemplateEntity templateEntity : templateEntities) {
                if (templateEntity.isDel()) {
                    continue;
                }

                List<PreLiveTemplateEntity> preLiveTemplateEntities = preLiveTemplateService.list(Wrappers.<PreLiveTemplateEntity>lambdaQuery().eq(PreLiveTemplateEntity::getTemplateId, templateEntity.getId()));
                templateEntity.setBlocks(preLiveTemplateEntities);
            }

            return R.ok().put("templates", templateEntities).put("total_count", pageUtils.getTotalCount());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/get")
    public R get(@RequestBody Map<String, Object> params) {
        try {
            String templateName = (String) params.get("template_name");

            TemplateEntity templateEntity = this.templateService.getOne(Wrappers.<TemplateEntity>lambdaQuery().eq(TemplateEntity::getTemplateName, templateName));
            if (templateEntity == null) {
                return R.error("找不到模板");
            }
            if (templateEntity.isDel()) {
                return R.error("已删除的模板");
            }

            return R.ok().put("template", templateEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/add")
    public R add(@RequestBody TemplateEntity templateEntity) {
        try {
            TemplateEntity templateEntityOther = this.templateService.getOne(Wrappers.<TemplateEntity>lambdaQuery().eq(TemplateEntity::getTemplateName, templateEntity.getTemplateName()));
            if (templateEntityOther != null) {
                return R.error("已注册的模板");
            }

            SysMember sysMember = this.getUser();

            templateEntity.setMemberId(sysMember.getId());
            templateEntity.setDel(false);
            templateEntity.setCreatedTime(new Date());
            templateEntity.setUpdatedTime(new Date());

            this.templateService.save(templateEntity);

            for (PreLiveTemplateEntity preLiveTemplateEntity : templateEntity.getBlocks()) {
                preLiveTemplateEntity.setTemplateId(templateEntity.getId());
            }

            preLiveTemplateService.saveBatch(templateEntity.getBlocks());

            return R.ok()
                    .put("template_id", templateEntity.getId());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/delete")
    public R delete(@RequestBody Map<String, Object> params) {
        try {
            String templateName = (String) params.get("template_name");

            TemplateEntity templateEntity = this.templateService.getOne(Wrappers.<TemplateEntity>lambdaQuery().eq(TemplateEntity::getTemplateName, templateName));
            if (templateEntity == null) {
                return R.error("找不到模板");
            }

            this.preLiveTemplateService.remove(Wrappers.<PreLiveTemplateEntity>lambdaQuery().eq(PreLiveTemplateEntity::getTemplateId, templateEntity.getId()));
            this.templateService.removeById(templateEntity.getId());

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }
}
