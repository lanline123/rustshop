package com.tigshop.bean.param.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量给用户打标签/取消标签参数")
public class UserMemberTagBatchParam {

    @Schema(description = "用户ID列表")
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;

    @Schema(description = "标签ID列表")
    @NotEmpty(message = "标签ID列表不能为空")
    private List<Long> tagIds;
}
