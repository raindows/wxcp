package cn.com.wangxy.wxcp.system.controller;

import cn.com.wangxy.wxcp.common.model.PageQuery;
import cn.com.wangxy.wxcp.common.model.PageResult;
import cn.com.wangxy.wxcp.common.model.R;
import cn.com.wangxy.wxcp.system.entity.SysUser;
import cn.com.wangxy.wxcp.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @GetMapping("/list")
    public R<PageResult<SysUser>> list(@ModelAttribute PageQuery query,
                                       @RequestParam(required = false) String keyword) {
        return R.ok(sysUserService.page(query, keyword));
    }

    @PostMapping("/create")
    public R<Void> create(@RequestBody SysUser user) {
        sysUserService.create(user);
        return R.ok();
    }

    @PutMapping("/update")
    public R<Void> update(@RequestBody SysUser user) {
        sysUserService.update(user);
        return R.ok();
    }

    @DeleteMapping("/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysUserService.delete(id);
        return R.ok();
    }

    @PostMapping("/reset-password/{id}")
    public R<Void> resetPassword(@PathVariable Long id) {
        sysUserService.resetPassword(id);
        return R.ok();
    }
}
