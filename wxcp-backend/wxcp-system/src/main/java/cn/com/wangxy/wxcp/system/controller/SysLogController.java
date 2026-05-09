package cn.com.wangxy.wxcp.system.controller;

import cn.com.wangxy.wxcp.common.model.PageQuery;
import cn.com.wangxy.wxcp.common.model.PageResult;
import cn.com.wangxy.wxcp.common.model.R;
import cn.com.wangxy.wxcp.system.entity.SysOperateLog;
import cn.com.wangxy.wxcp.system.service.SysOperateLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/log")
@RequiredArgsConstructor
public class SysLogController {

    private final SysOperateLogService sysOperateLogService;

    @GetMapping("/list")
    public R<PageResult<SysOperateLog>> list(@ModelAttribute PageQuery query,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String operator,
                                             @RequestParam(required = false) String startDate,
                                             @RequestParam(required = false) String endDate) {
        return R.ok(sysOperateLogService.page(query, keyword, operator, startDate, endDate));
    }
}
