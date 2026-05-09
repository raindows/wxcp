package cn.com.wangxy.wxcp.auth.details;

import cn.com.wangxy.wxcp.system.entity.SysUser;
import cn.com.wangxy.wxcp.system.mapper.SysRoleMenuMapper;
import cn.com.wangxy.wxcp.system.mapper.SysUserMapper;
import cn.com.wangxy.wxcp.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UserDetailsService implementation.
 * Loads user by userId (passed as String).
 * Depends on wxcp-system mappers for database access.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public UserDetails loadUserByUsername(String userIdStr) throws UsernameNotFoundException {
        Long userId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Invalid user id: " + userIdStr);
        }

        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser == null) {
            throw new UsernameNotFoundException("User not found with id: " + userId);
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(sysUser.getId());
        loginUser.setUsername(sysUser.getUsername());
        loginUser.setPassword(sysUser.getPassword());
        loginUser.setRealName(sysUser.getRealName());
        loginUser.setEnabled(sysUser.getStatus() != null && sysUser.getStatus() == 1);

        // Load permissions: user -> roles -> menus -> permissions
        List<Long> roleIds = sysUserRoleMapper.selectRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            loginUser.setPermissions(Collections.emptyList());
        } else {
            List<String> permissions = sysRoleMenuMapper.selectPermissionsByRoleIds(roleIds);
            loginUser.setPermissions(permissions);
        }

        return loginUser;
    }
}
