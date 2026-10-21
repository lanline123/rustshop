package com.tigshop.bean.vo.authority;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "生成API Token响应")
public class AdminApiTokenGenerateVO {

    @Schema(description = "Token ID")
    private Integer id;

    @Schema(description = "完整Token明文（仅此一次展示，请妥善保存）")
    private String token;

    @Schema(description = "Token名称/备注")
    private String name;

    @Schema(description = "过期时间，null表示永不过期")
    private Long expiresAt;
}
