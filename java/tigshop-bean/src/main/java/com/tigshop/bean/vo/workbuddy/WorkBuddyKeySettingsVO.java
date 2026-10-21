package com.tigshop.bean.vo.workbuddy;

import lombok.Data;

/** WorkBuddy 管理端密钥设置（仅返回掩码，不返回哈希或明文）。 */
@Data
public class WorkBuddyKeySettingsVO {
    private String maskedKey;
    private Long expiresAt;
    private boolean configured;
}
