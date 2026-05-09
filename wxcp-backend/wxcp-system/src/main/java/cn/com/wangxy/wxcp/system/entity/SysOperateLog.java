package cn.com.wangxy.wxcp.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_operate_log")
public class SysOperateLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String module;

    private String type;

    private String operator;

    private String ip;

    private String content;

    private LocalDateTime createTime;
}
