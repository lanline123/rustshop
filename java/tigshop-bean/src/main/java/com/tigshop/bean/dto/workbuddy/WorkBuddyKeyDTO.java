package com.tigshop.bean.dto.workbuddy;

import lombok.Data;

/** 兼容旧调用方的单 MCP 密钥参数；密钥不绑定工具白名单。 */
@Data
public class WorkBuddyKeyDTO {
    private String key;
    private Integer shopId;
    private Long expiresAt;
}
