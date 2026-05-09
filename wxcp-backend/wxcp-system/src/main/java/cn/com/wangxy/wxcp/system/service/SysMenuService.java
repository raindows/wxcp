package cn.com.wangxy.wxcp.system.service;

import cn.com.wangxy.wxcp.system.entity.SysMenu;

import java.util.List;

public interface SysMenuService {

    List<SysMenu> getMenuTree();

    void create(SysMenu menu);

    void update(SysMenu menu);

    void delete(Long id);

    List<Long> getMenuIdsByRoleId(Long roleId);
}
