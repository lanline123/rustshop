package com.tigshop.bean.param.promotion.groupon;

import com.tigshop.bean.model.promotion.GrouponPromotionItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拼团活动商品修改参数
 *
 * @author kidd
 * @since 2025/8/28 18:03
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PromotionPromotionItemUpdateParam extends PromotionPromotionItemCreateParam {

    @NotNull(message = "拼团活动商品 ID 不能为空")
    @Schema(description = "拼团活动商品 ID")
    private Long grouponPromotionItemId;

    @Override
    public GrouponPromotionItem buildGrouponPromotionItem() {
        GrouponPromotionItem grouponPromotionItem = super.buildGrouponPromotionItem();
        grouponPromotionItem.setGrouponPromotionItemId(this.grouponPromotionItemId);
        return grouponPromotionItem;
    }

}
