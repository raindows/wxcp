package cn.com.wangxy.wxcp.cp.service;

import cn.com.wangxy.wxcp.cp.entity.CpConfig;

import java.util.List;
import java.util.Map;

/**
 * 企业微信应用配置服务接口
 */
public interface CpConfigService {

    /**
     * 查询所有启用的配置列表
     *
     * @return 配置列表
     */
    List<CpConfig> list();

    /**
     * 根据ID查询配置
     *
     * @param id 配置ID
     * @return 配置信息
     */
    CpConfig getById(Long id);

    /**
     * 创建配置
     *
     * @param config 配置信息
     */
    void create(CpConfig config);

    /**
     * 更新配置
     *
     * @param config 配置信息
     */
    void update(CpConfig config);

    /**
     * 删除配置（软删除）
     *
     * @param id 配置ID
     */
    void delete(Long id);

    /**
     * 测试配置连接
     *
     * @param id 配置ID
     * @return 测试结果
     */
    Map<String, Object> testConnection(Long id);
}
