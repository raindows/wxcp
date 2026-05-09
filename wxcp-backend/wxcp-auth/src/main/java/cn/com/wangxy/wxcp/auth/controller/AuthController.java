package cn.com.wangxy.wxcp.auth.controller;

import cn.com.wangxy.wxcp.auth.details.LoginUser;
import cn.com.wangxy.wxcp.auth.jwt.JwtTokenProvider;
import cn.com.wangxy.wxcp.common.model.R;
import cn.com.wangxy.wxcp.system.entity.SysUser;
import cn.com.wangxy.wxcp.system.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Authentication controller.
 * Endpoints match frontend api/auth.ts exactly.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final SysUserMapper sysUserMapper;

    /**
     * WeChat Work OAuth2 login (placeholder).
     * Accepts { code: String }, returns fail since real WxJava integration needs config.
     */
    @PostMapping("/login")
    public R<Map<String, String>> login(@RequestBody Map<String, String> body) {
        return R.fail("企微OAuth2未配置");
    }

    /**
     * Get current user info from SecurityContext.
     * Returns { userId, name, avatar, corpId, corpName, permissions[] }.
     */
    @GetMapping("/userinfo")
    public R<Map<String, Object>> userinfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            return R.unauthorized("未登录或登录已过期");
        }

        // Load fresh user data from DB for avatar, corpId, corpName
        SysUser sysUser = sysUserMapper.selectById(loginUser.getUserId());

        Map<String, Object> data = new HashMap<>();
        data.put("userId", loginUser.getUserId());
        data.put("name", loginUser.getRealName());
        data.put("permissions", loginUser.getPermissions());

        if (sysUser != null) {
            data.put("avatar", sysUser.getAvatar());
            data.put("corpId", sysUser.getCorpId());
            data.put("corpName", "");
        }

        return R.ok(data);
    }

    /**
     * Get WeChat Work OAuth2 URL (placeholder).
     */
    @GetMapping("/oauth-url")
    public R<Map<String, String>> oauthUrl(@RequestParam String redirectUri) {
        Map<String, String> data = new HashMap<>();
        data.put("url", "");
        return R.ok(data);
    }

    /**
     * Logout - invalidate the current token in Redis.
     */
    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            jwtTokenProvider.invalidateToken(loginUser.getUserId());
        }
        return R.ok();
    }

    /**
     * Mock login for development environment.
     * Accepts { username: String }, loads user by username, generates JWT.
     * Only available when profile "dev" is active.
     */
    @Profile("dev")
    @PostMapping("/mock-login")
    public R<Map<String, String>> mockLogin(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null || username.isBlank()) {
            return R.fail("用户名不能为空");
        }

        SysUser sysUser = sysUserMapper.selectByUsername(username);
        if (sysUser == null) {
            return R.fail("用户不存在: " + username);
        }

        // Build permissions list from role-menu associations
        List<String> permissions = List.of();
        String token = jwtTokenProvider.generateToken(sysUser.getId(), sysUser.getRealName(), permissions);

        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        return R.ok(data);
    }
}
