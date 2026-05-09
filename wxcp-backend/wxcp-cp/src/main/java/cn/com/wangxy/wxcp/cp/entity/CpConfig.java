package cn.com.wangxy.wxcp.cp.entity;

import cn.com.wangxy.wxcp.common.model.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 企业微信应用配置实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("cp_config")
public class CpConfig extends BaseEntity {

    /**
     * 配置名称
     */
    private String name;

    /**
     * 企业ID
     */
    private String corpId;

    /**
     * 应用密钥（AES加密存储）
     */
    private String corpSecret;

    /**
     * 应用AgentId
     */
    private Integer agentId;

    /**
     * 接收消息的Token
     */
    private String token;

    /**
     * 接收消息的EncodingAESKey
     */
    private String aesKey;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}
