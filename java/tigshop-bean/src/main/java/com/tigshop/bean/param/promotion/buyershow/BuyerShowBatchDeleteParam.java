package com.tigshop.bean.param.promotion.buyershow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量删除买家秀参数
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "批量删除买家秀参数")
public class BuyerShowBatchDeleteParam {

    @NotEmpty(message = "请选择要删除的买家秀")
    @Schema(description = "买家秀ID列表")
    private List<@NotNull(message = "买家秀ID不能为空") Integer> ids;
}
