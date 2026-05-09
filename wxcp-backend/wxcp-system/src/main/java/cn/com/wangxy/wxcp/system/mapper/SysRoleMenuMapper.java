package cn.com.wangxy.wxcp.system.mapper;

import cn.com.wangxy.wxcp.system.entity.SysRoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    @Select("<script>" +
            "SELECT DISTINCT m.permission FROM sys_role_menu rm " +
            "INNER JOIN sys_menu m ON rm.menu_id = m.id " +
            "WHERE rm.role_id IN " +
            "<foreach collection='roleIds' item='roleId' open='(' separator=',' close=')'>" +
            "#{roleId}" +
            "</foreach>" +
            "AND m.permission IS NOT NULL AND m.permission != '' " +
            "AND m.deleted = 0 AND m.status = 1" +
            "</script>")
    List<String> selectPermissionsByRoleIds(@Param("roleIds") List<Long> roleIds);
}
