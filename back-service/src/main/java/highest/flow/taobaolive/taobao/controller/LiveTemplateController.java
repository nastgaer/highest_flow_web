package highest.flow.taobaolive.taobao.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
@RequestMapping("/live/template")
public class LiveTemplateController extends AbstractController {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PreLiveTemplateService preLiveTemplateService;

    @GetMapping("/list")
    public R list() {
        try {
            List<TemplateEntity> templateEntities = templateService.list();

            List<Map> templates = new ArrayList<>();
            for (TemplateEntity templateEntity : templateEntities) {
                if (templateEntity.isDel()) {
                    continue;
                }

                List<PreLiveTemplateEntity> preLiveTemplateEntities = preLiveTemplateService.list(Wrappers.<PreLiveTemplateEntity>lambdaQuery().eq(PreLiveTemplateEntity::getTemplateId, templateEntity.getId()));

                Map<String, Object> templateMap = new HashMap<>();
                templateMap.put("id", templateEntity.getId());
                templateMap.put("name", templateEntity.getTemplateName());
                templateMap.put("blocks", preLiveTemplateEntities);

                templates.add(templateMap);
            }

            return R.ok().put("templates", templates);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/get")
    public R get(@RequestBody Map<String, Object> params) {
        try {
            String templateId = (String) params.get("id");

            TemplateEntity templateEntity = templateService.getById(templateId);
            if (templateEntity == null) {
                return R.error("找不到模板");
            }
            if (templateEntity.isDel()) {
                return R.error("已删除的模板");
            }

            List<PreLiveTemplateEntity> preLiveTemplateEntities = preLiveTemplateService.list(Wrappers.<PreLiveTemplateEntity>lambdaQuery().eq(PreLiveTemplateEntity::getTemplateId, templateEntity.getId()));

            return R.ok()
                    .put("id", templateEntity.getId())
                    .put("name", templateEntity.getTemplateName())
                    .put("blocks", preLiveTemplateEntities);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/add")
    public R add(@RequestBody Map<String, Object> params) {
        try {
            String templateName = (String) params.get("name");
            List<PreLiveTemplateEntity> preLiveTemplateEntities = (List<PreLiveTemplateEntity>) params.get("blocks");

            TemplateEntity templateEntity = templateService.getOne(Wrappers.<TemplateEntity>lambdaQuery().eq(TemplateEntity::getTemplateName, templateName));
            if (templateEntity != null) {
                return R.error("已注册的模板");
            }

            SysMember sysMember = this.getUser();

            templateEntity = new TemplateEntity();
            templateEntity.setMemberId(sysMember.getId());
            templateEntity.setTemplateName(templateName);
            templateEntity.setDel(false);
            templateEntity.setCreatedTime(new Date());
            templateEntity.setUpdatedTime(new Date());

            templateService.save(templateEntity);

            for (PreLiveTemplateEntity preLiveTemplateEntity : preLiveTemplateEntities) {
                preLiveTemplateEntity.setTemplateId(templateEntity.getId());
            }

            preLiveTemplateService.saveBatch(preLiveTemplateEntities);

            return R.ok()
                    .put("id", templateEntity.getId());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }

    @PostMapping("/delete")
    public R delete(@RequestBody Map<String, Object> params) {
        try {
            int templateId = (int) params.get("id");

            TemplateEntity templateEntity = templateService.getById(templateId);
            if (templateEntity == null) {
                return R.error("找不到模板");
            }

            templateService.removeById(templateId);

            return R.ok();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error();
    }
}
