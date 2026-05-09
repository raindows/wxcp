package cn.com.wangxy.wxcp.system.entity;

import cn.com.wangxy.wxcp.common.model.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    private String name;

    private String roleKey;

    private String description;

    private Integer status;
}
