package cn.com.wangxy.wxcp.system.service;

import cn.com.wangxy.wxcp.common.model.PageQuery;
import cn.com.wangxy.wxcp.common.model.PageResult;
import cn.com.wangxy.wxcp.system.entity.SysUser;

public interface SysUserService {

    PageResult<SysUser> page(PageQuery query, String keyword);

    void create(SysUser user);

    void update(SysUser user);

    void delete(Long id);

    void resetPassword(Long id);

    SysUser getByUsername(String username);
}
