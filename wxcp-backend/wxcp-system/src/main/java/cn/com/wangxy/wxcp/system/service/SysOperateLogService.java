package cn.com.wangxy.wxcp.system.service;

import cn.com.wangxy.wxcp.common.model.PageQuery;
import cn.com.wangxy.wxcp.common.model.PageResult;
import cn.com.wangxy.wxcp.system.entity.SysOperateLog;

public interface SysOperateLogService {

    PageResult<SysOperateLog> page(PageQuery query, String keyword, String operator, String startDate, String endDate);
}
