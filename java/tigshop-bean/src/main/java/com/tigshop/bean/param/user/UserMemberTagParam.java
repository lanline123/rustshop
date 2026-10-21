package com.tigshop.bean.param.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "用户打标签/取消标签参数")
public class UserMemberTagParam {

    @Schema(description = "用户ID")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "标签ID列表")
    @NotEmpty(message = "标签ID列表不能为空")
    private List<Long> tagIds;
}
