-- WXCP Database Schema
-- Auto-executed on startup (dev profile)

CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(64)  NOT NULL UNIQUE COMMENT '登录用户名',
    password    VARCHAR(128) NOT NULL COMMENT 'BCrypt密码',
    real_name   VARCHAR(64)  DEFAULT '' COMMENT '真实姓名',
    dept_id     BIGINT       DEFAULT 0 COMMENT '部门ID',
    dept_name   VARCHAR(128) DEFAULT '' COMMENT '部门名称',
    phone       VARCHAR(20)  DEFAULT '' COMMENT '手机号',
    role_id     BIGINT       DEFAULT NULL COMMENT '角色ID',
    status      TINYINT      DEFAULT 1 COMMENT '状态 0=禁用 1=启用',
    avatar      VARCHAR(256) DEFAULT '' COMMENT '头像URL',
    corp_id     VARCHAR(64)  DEFAULT '' COMMENT '企微企业ID',
    wxcp_userid VARCHAR(64)  DEFAULT '' COMMENT '企微用户ID',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by   VARCHAR(64)  DEFAULT 'system',
    update_by   VARCHAR(64)  DEFAULT 'system',
    deleted     TINYINT      DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(64)  NOT NULL COMMENT '角色名称',
    role_key    VARCHAR(64)  NOT NULL COMMENT '角色标识',
    description VARCHAR(256) DEFAULT '' COMMENT '描述',
    status      TINYINT      DEFAULT 1 COMMENT '状态 0=禁用 1=启用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by   VARCHAR(64)  DEFAULT 'system',
    update_by   VARCHAR(64)  DEFAULT 'system',
    deleted     TINYINT      DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

CREATE TABLE IF NOT EXISTS sys_menu (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id   BIGINT       DEFAULT 0 COMMENT '父菜单ID',
    name        VARCHAR(64)  NOT NULL COMMENT '菜单名称',
    icon        VARCHAR(64)  DEFAULT '' COMMENT '图标',
    path        VARCHAR(256) DEFAULT '' COMMENT '路由路径',
    component   VARCHAR(256) DEFAULT '' COMMENT '组件路径',
    permission  VARCHAR(128) DEFAULT '' COMMENT '权限标识',
    type        ENUM('directory','menu','button') NOT NULL DEFAULT 'menu' COMMENT '类型',
    sort        INT          DEFAULT 0 COMMENT '排序',
    status      TINYINT      DEFAULT 1 COMMENT '状态 0=禁用 1=启用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by   VARCHAR(64)  DEFAULT 'system',
    update_by   VARCHAR(64)  DEFAULT 'system',
    deleted     TINYINT      DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

CREATE TABLE IF NOT EXISTS cp_config (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(128) NOT NULL COMMENT '应用名称',
    corp_id     VARCHAR(64)  NOT NULL COMMENT '企业ID',
    corp_secret VARCHAR(512) NOT NULL COMMENT '企业密钥(AES加密)',
    agent_id    INT          NOT NULL COMMENT '应用agentId',
    token       VARCHAR(128) DEFAULT '' COMMENT '回调Token',
    aes_key     VARCHAR(128) DEFAULT '' COMMENT '回调AesKey',
    status      TINYINT      DEFAULT 1 COMMENT '状态 0=停用 1=启用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by   VARCHAR(64)  DEFAULT 'system',
    update_by   VARCHAR(64)  DEFAULT 'system',
    deleted     TINYINT      DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企微应用配置表';

CREATE TABLE IF NOT EXISTS sys_operate_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    module      VARCHAR(64)  DEFAULT '' COMMENT '操作模块',
    type        ENUM('INFO','WARN','ERROR') DEFAULT 'INFO' COMMENT '类型',
    operator    VARCHAR(64)  DEFAULT '' COMMENT '操作人',
    ip          VARCHAR(64)  DEFAULT '' COMMENT 'IP地址',
    content     TEXT COMMENT '操作内容',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

CREATE TABLE IF NOT EXISTS sys_config (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_name  VARCHAR(128) NOT NULL COMMENT '配置名称',
    config_key   VARCHAR(128) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_group VARCHAR(64)  DEFAULT 'default' COMMENT '配置分组',
    remark       VARCHAR(256) DEFAULT '' COMMENT '备注',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by    VARCHAR(64)  DEFAULT 'system',
    update_by    VARCHAR(64)  DEFAULT 'system',
    deleted      TINYINT      DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数表';
