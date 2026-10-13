package com.tigshop.bean.param.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "新增会员标签参数")
public class MemberTagCreateParam {

    @Schema(description = "所属标签组ID")
    @NotNull(message = "标签组ID不能为空")
    private Long groupId;

    @Schema(description = "标签名称")
    @NotBlank(message = "标签名称不能为空")
    private String tagName;
}
