package com.tigshop.bean.vo.workbuddy;

import lombok.Data;

/** MCP 请求密钥鉴权结果；不包含密钥原文。 */
@Data
public class WorkBuddyKeyStatusVO {
    private boolean valid;
    private Long keyId;
    private String keyName;
    private Integer shopId;
    private Long expiresAt;
    private String message;
}
