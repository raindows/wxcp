package cn.com.wangxy.wxcp.system.controller;

import cn.com.wangxy.wxcp.common.model.R;
import cn.com.wangxy.wxcp.system.entity.SysMenu;
import cn.com.wangxy.wxcp.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;

    @GetMapping("/tree")
    public R<List<SysMenu>> tree() {
        return R.ok(sysMenuService.getMenuTree());
    }

    @PostMapping("/create")
    public R<Void> create(@RequestBody SysMenu menu) {
        sysMenuService.create(menu);
        return R.ok();
    }

    @PutMapping("/update")
    public R<Void> update(@RequestBody SysMenu menu) {
        sysMenuService.update(menu);
        return R.ok();
    }

    @DeleteMapping("/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysMenuService.delete(id);
        return R.ok();
    }
}
