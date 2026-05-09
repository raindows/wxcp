package cn.com.wangxy.wxcp.system.service;

import cn.com.wangxy.wxcp.system.entity.SysRole;

import java.util.List;

public interface SysRoleService {

    List<SysRole> list();

    void create(SysRole role);

    void update(SysRole role);

    void delete(Long id);

    void assignMenus(Long roleId, List<Long> menuIds);
}
