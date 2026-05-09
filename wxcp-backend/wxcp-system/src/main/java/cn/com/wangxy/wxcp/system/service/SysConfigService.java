package cn.com.wangxy.wxcp.system.service;

import cn.com.wangxy.wxcp.common.model.PageQuery;
import cn.com.wangxy.wxcp.common.model.PageResult;
import cn.com.wangxy.wxcp.system.entity.SysConfig;

public interface SysConfigService {

    PageResult<SysConfig> page(PageQuery query, String configGroup);

    void update(SysConfig config);
}
