package cn.com.wangxy.wxcp.system.entity;

import cn.com.wangxy.wxcp.common.model.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    private Long parentId;

    private String name;

    private String icon;

    private String path;

    private String component;

    private String permission;

    private String type;

    private Integer sort;

    private Integer status;

    @TableField(exist = false)
    private List<SysMenu> children;
}
