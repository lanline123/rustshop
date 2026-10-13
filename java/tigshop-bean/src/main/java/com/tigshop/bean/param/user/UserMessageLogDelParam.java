package com.tigshop.bean.param.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 会员消息删除参数
 *
 * @author kidd
 * @since 2025/4/10 10:49
 */
@Data
public class UserMessageLogDelParam {

    @NotNull(message = "会员消息ID不能为空")
    @Schema(description = "会员消息ID")
    private Integer id;
}
