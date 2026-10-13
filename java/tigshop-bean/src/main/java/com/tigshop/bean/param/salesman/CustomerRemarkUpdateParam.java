package com.tigshop.bean.param.salesman;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户端分销客户备注更新参数。
 */
@Data
@Schema(description = "分销客户备注更新参数")
public class CustomerRemarkUpdateParam {

    @NotNull(message = "客户用户ID不能为空")
    @Schema(description = "客户用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer userId;

    @NotNull(message = "备注不能为空")
    @Size(max = 255, message = "备注长度不能超过255个字符")
    @Schema(description = "备注，传空字符串可清空", requiredMode = Schema.RequiredMode.REQUIRED)
    private String remark;
}
