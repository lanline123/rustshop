package com.tigshop.bean.dto.im;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * im删除会话参数
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "im删除会话参数")
public class DelDTO {
    @Schema(description = "conversation_id")
    private Integer conversationId;

    @Schema(description = "shop_id")
    private Integer shopId;
}
