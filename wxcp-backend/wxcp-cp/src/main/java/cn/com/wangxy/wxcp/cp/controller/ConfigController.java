package cn.com.wangxy.wxcp.cp.controller;

import cn.com.wangxy.wxcp.common.model.R;
import cn.com.wangxy.wxcp.cp.entity.CpConfig;
import cn.com.wangxy.wxcp.cp.service.CpConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 企业微信应用配置控制器
 */
@RestController
@RequestMapping("/config")
public class ConfigController {

    private final CpConfigService configService;

    public ConfigController(CpConfigService configService) {
        this.configService = configService;
    }

    /**
     * 获取所有启用的配置列表
     */
    @GetMapping("/config/list")
    public R<List<CpConfig>> list() {
        return R.ok(configService.list());
    }

    /**
     * 根据ID查询配置详情
     */
    @GetMapping("/config/detail/{id}")
    public R<CpConfig> detail(@PathVariable Long id) {
        return R.ok(configService.getById(id));
    }

    /**
     * 创建配置
     */
    @PostMapping("/config/create")
    public R<Void> create(@RequestBody CpConfig config) {
        configService.create(config);
        return R.ok();
    }

    /**
     * 更新配置
     */
    @PutMapping("/config/update")
    public R<Void> update(@RequestBody CpConfig config) {
        configService.update(config);
        return R.ok();
    }

    /**
     * 删除配置
     */
    @DeleteMapping("/config/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        configService.delete(id);
        return R.ok();
    }

    /**
     * 测试配置连接
     */
    @PostMapping("/config/test/{id}")
    public R<Map<String, Object>> test(@PathVariable Long id) {
        return R.ok(configService.testConnection(id));
    }
}
