package com.tigshop.bean.dto.im;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * im转接数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "im转接参数")
public class TransferDTO {
    @Schema(description = "待转接客服id")
    private Integer servantId;

    @Schema(description = "会话id")
    private Integer conversationId;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "旧客服id")
    private Integer oldServantId;
}
