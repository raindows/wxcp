package cn.com.wangxy.wxcp.system.service.impl;

import cn.com.wangxy.wxcp.common.model.PageQuery;
import cn.com.wangxy.wxcp.common.model.PageResult;
import cn.com.wangxy.wxcp.system.entity.SysUser;
import cn.com.wangxy.wxcp.system.mapper.SysUserMapper;
import cn.com.wangxy.wxcp.system.service.SysUserService;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    @Override
    public PageResult<SysUser> page(PageQuery query, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getRealName, keyword);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> page = sysUserMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), query.getPageNum(), query.getPageSize());
    }

    @Override
    public void create(SysUser user) {
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        sysUserMapper.insert(user);
    }

    @Override
    public void update(SysUser user) {
        sysUserMapper.updateById(user);
    }

    @Override
    public void delete(Long id) {
        sysUserMapper.deleteById(id);
    }

    @Override
    public void resetPassword(Long id) {
        String rawPassword = generateRandomPassword(8);
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(BCrypt.hashpw(rawPassword));
        sysUserMapper.updateById(user);
    }

    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return sysUserMapper.selectOne(wrapper);
    }

    private String generateRandomPassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
