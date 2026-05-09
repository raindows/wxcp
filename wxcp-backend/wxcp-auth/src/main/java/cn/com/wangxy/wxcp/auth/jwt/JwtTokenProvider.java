package cn.com.wangxy.wxcp.auth.jwt;

import cn.com.wangxy.wxcp.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JWT Token Provider
 * Wraps JwtUtil and manages token storage in Redis for invalidation support.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String TOKEN_PREFIX = "wxcp:token:";
    private static final long TOKEN_TTL_HOURS = 24;

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Generate a JWT token for the given user and store it in Redis.
     *
     * @param userId      user ID
     * @param name        user real name
     * @param permissions user permission list
     * @return JWT token string
     */
    public String generateToken(Long userId, String name, List<String> permissions) {
        String token = jwtUtil.generateToken(userId, name, permissions);
        redisTemplate.opsForValue().set(TOKEN_PREFIX + userId, token, TOKEN_TTL_HOURS, TimeUnit.HOURS);
        return token;
    }

    /**
     * Extract userId from the JWT token.
     */
    public Long getUserId(String token) {
        return jwtUtil.getUserId(token);
    }

    /**
     * Extract name from the JWT token.
     */
    public String getName(String token) {
        return jwtUtil.getName(token);
    }

    /**
     * Extract permissions from the JWT token.
     */
    public List<String> getPermissions(String token) {
        return jwtUtil.getPermissions(token);
    }

    /**
     * Check if the given token is valid (signature + expiration) and matches the stored token in Redis.
     */
    public boolean isTokenValid(String token) {
        try {
            if (!jwtUtil.isTokenValid(token)) {
                return false;
            }
            Long userId = getUserId(token);
            String storedToken = redisTemplate.opsForValue().get(TOKEN_PREFIX + userId);
            return token.equals(storedToken);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Invalidate the token stored in Redis for the given user (used on logout).
     *
     * @param userId user ID
     */
    public void invalidateToken(Long userId) {
        redisTemplate.delete(TOKEN_PREFIX + userId);
    }
}
