package com.tigshop.bean.param.promotion.timediscount;

import com.tigshop.bean.model.promotion.TimeDiscount;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.tigshop.common.constant.promotion.TimeDiscountConstants.TIME_DISCOUNT_ID_NOT_NULL;

/**
  * 限时折扣更新参数
 *
 * @author kidd
 * @create 2025/7/1
 */
@Data
@Schema(description = "限时折扣参数")
public class TimeDiscountEditParam extends TimeDiscountSaveParam {

    @NotNull(message = TIME_DISCOUNT_ID_NOT_NULL)
    @Schema(description = "限时折扣ID")
    private Integer discountId;


    public TimeDiscount createTimeDiscount() {
        TimeDiscount timeDiscount = super.createTimeDiscount();
        timeDiscount.setDiscountId(this.discountId);
        return timeDiscount;
    }
}
