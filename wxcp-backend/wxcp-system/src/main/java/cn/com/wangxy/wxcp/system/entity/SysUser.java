package cn.com.wangxy.wxcp.system.entity;

import cn.com.wangxy.wxcp.common.model.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    @TableField("username")
    private String username;

    @TableField("password")
    @JsonIgnore
    private String password;

    @TableField("real_name")
    private String realName;

    @TableField("dept_id")
    private Long deptId;

    @TableField("dept_name")
    private String deptName;

    @TableField("phone")
    private String phone;

    @TableField("role_id")
    private Long roleId;

    @TableField("status")
    private Integer status;

    @TableField("avatar")
    private String avatar;

    @TableField("corp_id")
    private String corpId;

    @TableField("wxcp_userid")
    private String wxcpUserid;
}
