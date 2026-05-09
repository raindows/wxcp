package cn.com.wangxy.wxcp.system.controller;

import cn.com.wangxy.wxcp.common.model.PageQuery;
import cn.com.wangxy.wxcp.common.model.PageResult;
import cn.com.wangxy.wxcp.common.model.R;
import cn.com.wangxy.wxcp.system.entity.SysConfig;
import cn.com.wangxy.wxcp.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService sysConfigService;

    @GetMapping("/list")
    public R<PageResult<SysConfig>> list(@ModelAttribute PageQuery query,
                                         @RequestParam(required = false) String configGroup) {
        return R.ok(sysConfigService.page(query, configGroup));
    }

    @PutMapping("/update")
    public R<Void> update(@RequestBody SysConfig config) {
        sysConfigService.update(config);
        return R.ok();
    }
}
