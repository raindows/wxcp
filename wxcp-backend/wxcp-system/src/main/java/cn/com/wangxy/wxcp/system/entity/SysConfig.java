package cn.com.wangxy.wxcp.system.entity;

import cn.com.wangxy.wxcp.common.model.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends BaseEntity {

    private String configName;

    private String configKey;

    private String configValue;

    private String configGroup;

    private String remark;
}
