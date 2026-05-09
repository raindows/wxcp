package cn.com.wangxy.wxcp.system.service.impl;

import cn.com.wangxy.wxcp.common.model.PageQuery;
import cn.com.wangxy.wxcp.common.model.PageResult;
import cn.com.wangxy.wxcp.system.entity.SysConfig;
import cn.com.wangxy.wxcp.system.mapper.SysConfigMapper;
import cn.com.wangxy.wxcp.system.service.SysConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements SysConfigService {

    private final SysConfigMapper sysConfigMapper;

    @Override
    public PageResult<SysConfig> page(PageQuery query, String configGroup) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(configGroup)) {
            wrapper.eq(SysConfig::getConfigGroup, configGroup);
        }
        wrapper.orderByAsc(SysConfig::getId);
        Page<SysConfig> page = sysConfigMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), query.getPageNum(), query.getPageSize());
    }

    @Override
    public void update(SysConfig config) {
        sysConfigMapper.updateById(config);
    }
}
