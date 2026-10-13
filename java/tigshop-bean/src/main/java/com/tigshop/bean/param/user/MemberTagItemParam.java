package com.tigshop.bean.param.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "标签条目参数")
public class MemberTagItemParam {

    @Schema(description = "标签ID（已有标签传ID，新标签不传）")
    private Long id;

    @Schema(description = "标签名称")
    @NotBlank(message = "标签名称不能为空")
    private String tagName;
}
