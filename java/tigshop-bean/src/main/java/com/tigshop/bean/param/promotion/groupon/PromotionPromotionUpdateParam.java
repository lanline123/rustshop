package com.tigshop.bean.param.promotion.groupon;

import com.tigshop.bean.model.promotion.GrouponPromotion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 拼团活动编辑参数
 *
 * @author kidd
 * @since 2025/8/28 14:27
 */
@Data
public class PromotionPromotionUpdateParam extends PromotionPromotionCreateParam {

    @Schema(description = "拼团 ID")
    private Long grouponPromotionId;

    @Override
    public GrouponPromotion buildGrouponPromotion() {
        GrouponPromotion grouponPromotion = super.buildGrouponPromotion();
        grouponPromotion.setGrouponPromotionId(grouponPromotionId);
        return grouponPromotion;
    }

}
