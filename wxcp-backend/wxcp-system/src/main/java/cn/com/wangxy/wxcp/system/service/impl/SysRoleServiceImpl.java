package cn.com.wangxy.wxcp.system.service.impl;

import cn.com.wangxy.wxcp.system.entity.SysRole;
import cn.com.wangxy.wxcp.system.entity.SysRoleMenu;
import cn.com.wangxy.wxcp.system.mapper.SysRoleMapper;
import cn.com.wangxy.wxcp.system.mapper.SysRoleMenuMapper;
import cn.com.wangxy.wxcp.system.service.SysRoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public List<SysRole> list() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysRole::getId);
        return sysRoleMapper.selectList(wrapper);
    }

    @Override
    public void create(SysRole role) {
        sysRoleMapper.insert(role);
    }

    @Override
    public void update(SysRole role) {
        sysRoleMapper.updateById(role);
    }

    @Override
    public void delete(Long id) {
        sysRoleMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // delete old role-menu relations
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId);
        sysRoleMenuMapper.delete(wrapper);
        // batch insert new role-menu relations
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                sysRoleMenuMapper.insert(roleMenu);
            }
        }
    }
}
