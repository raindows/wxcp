package cn.com.wangxy.wxcp.system.controller;

import cn.com.wangxy.wxcp.common.model.R;
import cn.com.wangxy.wxcp.system.entity.SysRole;
import cn.com.wangxy.wxcp.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;

    @GetMapping("/list")
    public R<List<SysRole>> list() {
        return R.ok(sysRoleService.list());
    }

    @PostMapping("/create")
    public R<Void> create(@RequestBody SysRole role) {
        sysRoleService.create(role);
        return R.ok();
    }

    @PutMapping("/update")
    public R<Void> update(@RequestBody SysRole role) {
        sysRoleService.update(role);
        return R.ok();
    }

    @DeleteMapping("/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysRoleService.delete(id);
        return R.ok();
    }

    @PostMapping("/assign-menus/{roleId}")
    public R<Void> assignMenus(@PathVariable Long roleId,
                               @RequestBody Map<String, List<Long>> body) {
        List<Long> menuIds = body.get("menuIds");
        sysRoleService.assignMenus(roleId, menuIds);
        return R.ok();
    }
}
