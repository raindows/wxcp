package cn.com.wangxy.wxcp.cp.service.impl;

import cn.com.wangxy.wxcp.common.util.AesUtil;
import cn.com.wangxy.wxcp.cp.entity.CpConfig;
import cn.com.wangxy.wxcp.cp.mapper.CpConfigMapper;
import cn.com.wangxy.wxcp.cp.service.CpConfigService;
import cn.com.wangxy.wxcp.cp.service.WxCpServiceManager;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.config.impl.WxCpDefaultConfigImpl;
import me.chanjar.weixin.cp.api.impl.WxCpServiceImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CpConfigServiceImpl implements CpConfigService {

    private final CpConfigMapper cpConfigMapper;
    private final WxCpServiceManager wxCpServiceManager;
    private final AesUtil aesUtil;

    public CpConfigServiceImpl(CpConfigMapper cpConfigMapper,
                               WxCpServiceManager wxCpServiceManager,
                               AesUtil aesUtil) {
        this.cpConfigMapper = cpConfigMapper;
        this.wxCpServiceManager = wxCpServiceManager;
        this.aesUtil = aesUtil;
    }

    @Override
    public List<CpConfig> list() {
        LambdaQueryWrapper<CpConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CpConfig::getStatus, 1);
        return cpConfigMapper.selectList(wrapper);
    }

    @Override
    public CpConfig getById(Long id) {
        return cpConfigMapper.selectById(id);
    }

    @Override
    public void create(CpConfig config) {
        config.setCorpSecret(aesUtil.encrypt(config.getCorpSecret()));
        cpConfigMapper.insert(config);
        wxCpServiceManager.createService(config);
    }

    @Override
    public void update(CpConfig config) {
        CpConfig existing = cpConfigMapper.selectById(config.getId());
        if (existing == null) {
            throw new RuntimeException("配置不存在");
        }
        if (config.getCorpSecret() != null && !config.getCorpSecret().isEmpty()
                && !config.getCorpSecret().equals(existing.getCorpSecret())) {
            try {
                aesUtil.decrypt(config.getCorpSecret());
            } catch (Exception e) {
                config.setCorpSecret(aesUtil.encrypt(config.getCorpSecret()));
            }
        }
        cpConfigMapper.updateById(config);
        wxCpServiceManager.refreshService(config.getAgentId());
    }

    @Override
    public void delete(Long id) {
        CpConfig config = cpConfigMapper.selectById(id);
        if (config != null) {
            cpConfigMapper.deleteById(id);
            wxCpServiceManager.removeService(config.getAgentId());
        }
    }

    @Override
    public Map<String, Object> testConnection(Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            CpConfig config = cpConfigMapper.selectById(id);
            if (config == null) {
                result.put("success", false);
                result.put("message", "配置不存在");
                return result;
            }
            WxCpService tempService = wxCpServiceManager.createTempService(config);
            String accessToken = tempService.getAccessToken();
            if (accessToken != null && !accessToken.isEmpty()) {
                result.put("success", true);
                result.put("message", "连接成功");
            } else {
                result.put("success", false);
                result.put("message", "获取AccessToken失败");
            }
        } catch (Exception e) {
            log.error("测试企业微信连接失败: configId={}", id, e);
            result.put("success", false);
            result.put("message", "连接失败: " + e.getMessage());
        }
        return result;
    }
}
