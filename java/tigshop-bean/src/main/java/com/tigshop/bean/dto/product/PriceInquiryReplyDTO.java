package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author wzh
 */
@Data
public class PriceInquiryReplyDTO {

    @NotNull(message = "id不能为空")
    @Schema(description = "主键id")
    private Integer id;

    @Size(min = 1, max = 100, message = "回复内容长度在1到255之间")
    @NotBlank(message = "回复内容不能为空")
    @Schema(description = "回复内容")
    private String remark;
}
