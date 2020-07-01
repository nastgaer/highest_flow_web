package highest.flow.taobaolive.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import highest.flow.taobaolive.common.utils.CommonUtils;
import highest.flow.taobaolive.common.utils.R;
import highest.flow.taobaolive.sys.entity.PageEntity;
import highest.flow.taobaolive.sys.entity.RegisterUserEntity;
import highest.flow.taobaolive.sys.entity.SysLog;
import highest.flow.taobaolive.sys.entity.SysMember;
import highest.flow.taobaolive.sys.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sys")
public class MemberController extends AbstractController {

    @Autowired
    private MemberService memberService;

    @PostMapping("/register")
    public R register(@RequestBody RegisterUserEntity registerUserEntity) {
        try {
            SysMember member = memberService.register(registerUserEntity.getMemberName(),
                    registerUserEntity.getPassword(),
                    registerUserEntity.getMobile(),
                    registerUserEntity.getComment(),
                    registerUserEntity.getRole(),
                    registerUserEntity.getState());

            if (member == null) {
                return R.error("注册用户失败");
            }

            return R.ok().put("member_id", member.getId());

        } catch (Exception ex){
            return R.error("注册用户失败");
        }
    }

    @PostMapping("/list")
    public R list(@RequestBody PageEntity pageEntity) {
        try {
            int pageNo = pageEntity.getPageNo();
            int pageSize = pageEntity.getPageSize();
            IPage<SysMember> page = this.memberService.page(new Page<>((pageNo - 1) * pageSize, pageSize));
            List<SysMember> members = page.getRecords();

            List<Map<String, Object>> memberList = new ArrayList<>();
            for (SysMember sysMember : members) {
                List<String> roles = memberService.getRoles(sysMember);

                Map<String, Object> memberMap = new HashMap<>();
                memberMap.put("id", sysMember.getId());
                memberMap.put("member_name", sysMember.getMemberName());
                memberMap.put("role", roles);
                memberMap.put("mobile", sysMember.getMobile());
                memberMap.put("comment", sysMember.getComment());
                memberMap.put("state", sysMember.getState());
                memberMap.put("created_time", CommonUtils.dateToTimestamp(sysMember.getCreatedTime()));
                memberMap.put("updated_time", CommonUtils.dateToTimestamp(sysMember.getUpdatedTime()));

                memberList.add(memberMap);
            }

            return R.ok().put("users", memberList).put("total_count", memberList.size());

        } catch (Exception ex){
            return R.error("注册用户失败");
        }
    }

    @PostMapping("/update")
    public R update(@RequestBody SysMember sysMember) {
        try {
            if (this.memberService.update(sysMember, Wrappers.<SysMember>lambdaQuery().eq(SysMember::getId, sysMember.getId()))) {
                return R.ok();
            }

            return R.error("注册用户失败");

        } catch (Exception ex){
            return R.error("注册用户失败");
        }
    }
}
