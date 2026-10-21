package com.tigshop.bean.dto.authority;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "生成API Token请求")
public class AdminApiTokenGenerateDTO {

    @Schema(description = "Token名称/备注")
    private String name;

    @Schema(description = "过期天数，不填则永不过期")
    private Integer expiresInDays;
}
