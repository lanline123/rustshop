package com.tigshop.bean.vo.authority;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "API Token信息")
public class AdminApiTokenVO {

    @Schema(description = "Token ID")
    private Integer id;

    @Schema(description = "Token前缀（前8位）")
    private String tokenPrefix;

    @Schema(description = "Token名称/备注")
    private String name;

    @Schema(description = "创建时间")
    private Long createdAt;

    @Schema(description = "最后使用时间")
    private Long lastUsedAt;

    @Schema(description = "过期时间，null表示永不过期")
    private Long expiresAt;

    @Schema(description = "是否有效")
    private Integer isActive;
}
