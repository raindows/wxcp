package cn.com.wangxy.wxcp.cp.service;

import cn.com.wangxy.wxcp.cp.config.WxCpConfigStorageFactory;
import cn.com.wangxy.wxcp.cp.entity.CpConfig;
import cn.com.wangxy.wxcp.cp.mapper.CpConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.api.impl.WxCpServiceImpl;
import me.chanjar.weixin.cp.config.impl.WxCpDefaultConfigImpl;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WxCpServiceManager {

    private final ConcurrentHashMap<Integer, WxCpService> serviceMap = new ConcurrentHashMap<>();
    private final CpConfigMapper cpConfigMapper;
    private final WxCpConfigStorageFactory storageFactory;
    private final RedissonClient redissonClient;

    public WxCpServiceManager(CpConfigMapper cpConfigMapper,
                             WxCpConfigStorageFactory storageFactory,
                             RedissonClient redissonClient) {
        this.cpConfigMapper = cpConfigMapper;
        this.storageFactory = storageFactory;
        this.redissonClient = redissonClient;
    }

    public void init() {
        LambdaQueryWrapper<CpConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CpConfig::getStatus, 1);
        List<CpConfig> configs = cpConfigMapper.selectList(wrapper);
        for (CpConfig config : configs) {
            try {
                createService(config);
                log.info("初始化WxCpService成功: agentId={}, name={}", config.getAgentId(), config.getName());
            } catch (Exception e) {
                log.error("初始化WxCpService失败: agentId={}, name={}", config.getAgentId(), config.getName(), e);
            }
        }
    }

    public WxCpService getService(Integer agentId) {
        return serviceMap.get(agentId);
    }

    public void createService(CpConfig config) {
        WxCpServiceImpl service = new WxCpServiceImpl();
        service.setWxCpConfigStorage(storageFactory.create(config, redissonClient));
        serviceMap.put(config.getAgentId(), service);
    }

    public void refreshService(Integer agentId) {
        serviceMap.remove(agentId);
        CpConfig config = cpConfigMapper.selectById(agentId);
        if (config != null && config.getStatus() == 1) {
            createService(config);
        }
    }

    public void removeService(Integer agentId) {
        serviceMap.remove(agentId);
    }

    /**
     * Create a temporary WxCpService for testing connection (not stored in map).
     */
    public WxCpService createTempService(CpConfig config) {
        WxCpDefaultConfigImpl defaultConfig = new WxCpDefaultConfigImpl();
        defaultConfig.setCorpId(config.getCorpId());
        defaultConfig.setCorpSecret(storageFactory.create(config, redissonClient).getCorpSecret());
        defaultConfig.setAgentId(config.getAgentId());
        defaultConfig.setToken(config.getToken());
        defaultConfig.setAesKey(config.getAesKey());

        WxCpServiceImpl service = new WxCpServiceImpl();
        service.setWxCpConfigStorage(defaultConfig);
        return service;
    }
}
