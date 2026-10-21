package com.tigshop.bean.param.promotion.buyershow;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author wzh
 */
@Data
@Schema(description = "更新买家秀分组参数")
public class BuyerShowGroupUpdateParam {
    @NotNull(message = "买家秀分组ID不能为空")
    @Schema(description = "买家秀分组ID")
    private Integer buyerShowGroupId;

    @NotBlank(message = "分组名称不能为空")
    @Schema(description = "分组名称")
    private String groupName;

    @NotEmpty(message = "商品分组Ids不能为空")
    @Schema(description = "商品分组Ids")
    private List<Integer> groupIds;
}
