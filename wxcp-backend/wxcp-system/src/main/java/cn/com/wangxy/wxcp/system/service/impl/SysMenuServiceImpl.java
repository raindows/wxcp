package cn.com.wangxy.wxcp.system.service.impl;

import cn.com.wangxy.wxcp.system.entity.SysMenu;
import cn.com.wangxy.wxcp.system.entity.SysRoleMenu;
import cn.com.wangxy.wxcp.system.mapper.SysMenuMapper;
import cn.com.wangxy.wxcp.system.mapper.SysRoleMenuMapper;
import cn.com.wangxy.wxcp.system.service.SysMenuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {

    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public List<SysMenu> getMenuTree() {
        List<SysMenu> allMenus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSort));
        return buildTree(allMenus);
    }

    @Override
    public void create(SysMenu menu) {
        sysMenuMapper.insert(menu);
    }

    @Override
    public void update(SysMenu menu) {
        sysMenuMapper.updateById(menu);
    }

    @Override
    public void delete(Long id) {
        sysMenuMapper.deleteById(id);
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId);
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(wrapper);
        return roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
    }

    private List<SysMenu> buildTree(List<SysMenu> menus) {
        Map<Long, List<SysMenu>> childrenMap = menus.stream()
                .filter(m -> m.getParentId() != null && m.getParentId() != 0)
                .collect(Collectors.groupingBy(SysMenu::getParentId));
        List<SysMenu> roots = menus.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0)
                .collect(Collectors.toList());
        for (SysMenu root : roots) {
            root.setChildren(findChildren(root.getId(), childrenMap));
        }
        return roots;
    }

    private List<SysMenu> findChildren(Long parentId, Map<Long, List<SysMenu>> childrenMap) {
        List<SysMenu> children = childrenMap.getOrDefault(parentId, new ArrayList<>());
        for (SysMenu child : children) {
            child.setChildren(findChildren(child.getId(), childrenMap));
        }
        return children;
    }
}
