package cn.com.wangxy.wxcp.cp.config;

import cn.com.wangxy.wxcp.common.util.AesUtil;
import cn.com.wangxy.wxcp.cp.entity.CpConfig;
import me.chanjar.weixin.cp.config.impl.WxCpRedissonConfigImpl;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
public class WxCpConfigStorageFactory {

    private final AesUtil aesUtil;

    public WxCpConfigStorageFactory(AesUtil aesUtil) {
        this.aesUtil = aesUtil;
    }

    public WxCpRedissonConfigImpl create(CpConfig config, RedissonClient redissonClient) {
        WxCpRedissonConfigImpl storage = new WxCpRedissonConfigImpl(redissonClient, "wxcp:cp:");
        storage.setCorpId(config.getCorpId());
        storage.setCorpSecret(aesUtil.decrypt(config.getCorpSecret()));
        storage.setAgentId(config.getAgentId());
        storage.setToken(config.getToken());
        storage.setAesKey(config.getAesKey());
        return storage;
    }
}
