package cn.com.wangxy.wxcp.system.service.impl;

import cn.com.wangxy.wxcp.common.model.PageQuery;
import cn.com.wangxy.wxcp.common.model.PageResult;
import cn.com.wangxy.wxcp.system.entity.SysOperateLog;
import cn.com.wangxy.wxcp.system.mapper.SysOperateLogMapper;
import cn.com.wangxy.wxcp.system.service.SysOperateLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class SysOperateLogServiceImpl implements SysOperateLogService {

    private final SysOperateLogMapper sysOperateLogMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<SysOperateLog> page(PageQuery query, String keyword, String operator, String startDate, String endDate) {
        LambdaQueryWrapper<SysOperateLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysOperateLog::getContent, keyword);
        }
        if (StringUtils.hasText(operator)) {
            wrapper.like(SysOperateLog::getOperator, operator);
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(SysOperateLog::getCreateTime, LocalDateTime.parse(startDate, DATE_TIME_FORMATTER));
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(SysOperateLog::getCreateTime, LocalDateTime.parse(endDate, DATE_TIME_FORMATTER));
        }
        wrapper.orderByDesc(SysOperateLog::getCreateTime);
        Page<SysOperateLog> page = sysOperateLogMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), query.getPageNum(), query.getPageSize());
    }
}
