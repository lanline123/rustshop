package com.tigshop.bean.param.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新会员标签参数")
public class MemberTagUpdateParam {

    @Schema(description = "标签ID")
    @NotNull(message = "标签ID不能为空")
    private Long id;

    @Schema(description = "标签名称")
    @NotBlank(message = "标签名称不能为空")
    private String tagName;
}
