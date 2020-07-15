package highest.flow.taobaolive.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import highest.flow.taobaolive.api.param.IdsParam;
import highest.flow.taobaolive.api.param.PageParam;
import highest.flow.taobaolive.api.param.RegisterMemberParam;
import highest.flow.taobaolive.api.param.UpdateMemberParam;
import highest.flow.taobaolive.common.config.Config;
import highest.flow.taobaolive.common.utils.CommonUtils;
import highest.flow.taobaolive.common.utils.HFStringUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.sys.entity.*;
import highest.flow.taobaolive.sys.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/v1.0/sys")
public class MemberController extends AbstractController {

    @Autowired
    private MemberService memberService;

    @PostMapping("/register")
    public R register(@RequestBody RegisterMemberParam registerMemberParam) {
        try {
            SysMember sysMember = memberService.getOne(Wrappers.<SysMember>lambdaQuery().eq(SysMember::getMemberName, registerMemberParam.getMemberName()));
            if (sysMember != null) {
                return R.error("已经注册的会员");
            }

            sysMember = memberService.register(registerMemberParam.getMemberName(),
                    registerMemberParam.getPassword(),
                    registerMemberParam.getMobile(),
                    registerMemberParam.getComment(),
                    registerMemberParam.getRoles(),
                    registerMemberParam.getState());

            if (sysMember == null) {
                return R.error("注册用户失败");
            }

            return R.ok().put("member_id", sysMember.getId());

        } catch (Exception ex){
            ex.printStackTrace();
        }
        return R.error("注册用户失败");
    }

    @PostMapping("/list")
    public R list(@RequestBody PageParam pageParam) {
        try {
            int pageNo = pageParam.getPageNo();
            int pageSize = pageParam.getPageSize();
            String keyword = pageParam.getKeyword();
            IPage<SysMember> page = HFStringUtils.isNullOrEmpty(keyword) ?
                    this.memberService.page(new Page<>((pageNo - 1) * pageSize, pageSize)) :
                    this.memberService.page(new Page<>((pageNo - 1) * pageSize, pageSize),
                            Wrappers.<SysMember>lambdaQuery().like(SysMember::getMemberName, keyword)
                                    .or()
                                    .like(SysMember::getComment, keyword));
            List<SysMember> members = page.getRecords();

            List<Map<String, Object>> memberList = new ArrayList<>();
            for (SysMember sysMember : members) {
                List<String> roles = memberService.getRoles(sysMember);

                sysMember.setRoles(roles);
            }

            return R.ok().put("users", members).put("total_count", memberList.size());

        } catch (Exception ex){
            ex.printStackTrace();
        }
        return R.error("获取会员列表失败");
    }

    @PostMapping("/update")
    public R update(@RequestBody UpdateMemberParam updateMemberParam) {
        try {
            SysMember sysMember = memberService.getOne(Wrappers.<SysMember>lambdaQuery().eq(SysMember::getMemberName, updateMemberParam.getMemberName()));
            if (sysMember == null) {
                return R.error("已经注册的会员");
            }

            SysMember memberOther = memberService.getOne(Wrappers.<SysMember>lambdaQuery().eq(SysMember::getMemberName, updateMemberParam.getMemberName()));
            if (memberOther.getId() != sysMember.getId()) {
                return R.error("已经注册的会员名称");
            }

            if (this.memberService.update(updateMemberParam.getId(),
                    updateMemberParam.getMemberName(),
                    updateMemberParam.getPassword(),
                    updateMemberParam.getMobile(),
                    updateMemberParam.getComment(),
                    updateMemberParam.getRoles(),
                    updateMemberParam.getState())) {
                return R.ok();
            }

            return R.error("更新会员信息失败");

        } catch (Exception ex){
            ex.printStackTrace();
        }
        return R.error("更新会员信息失败");
    }

    @PostMapping("/batch_delete")
    public R batchDelete(@RequestBody IdsParam idsParam) {
        try {
            // 获取管理员的id
            SysMember administrator = this.memberService.getOne(Wrappers.<SysMember>lambdaQuery().eq(SysMember::getMemberName, Config.ADMINISTRATOR));

            List<Integer> newIds = new ArrayList<>();
            for (Integer id : idsParam.getIds()) {
                if (id == administrator.getId()) {
                    continue;
                }

                newIds.add(id);
            }

            if (this.memberService.deleteBatch(newIds)) {
                return R.ok();
            }

            return R.error("批量删除会员失败");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return R.error("批量删除会员失败");
    }
}
