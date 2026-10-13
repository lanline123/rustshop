package com.tigshop.bean.dto.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.tigshop.bean.model.promotion.TimeDiscountItem;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author wzh
 */
@Data
public class TimeDiscountDTO {
    @TableId(value = "item_id", type = IdType.AUTO)
    @Schema(description = "活动商品ID")
    private Integer itemId;

    @Schema(description = "关联折扣活动ID")
    private Integer discountId;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "产品ID")
    private Integer productId;

    @Schema(description = "规格ID")
    private String skuIds;

    @Schema(description = "促销价格，减的金额或打的折数")
    private BigDecimal value;

    @Schema(description = "1打折2减钱3促销")
    private Integer discountType;

    @Schema(description = "")
    private BigDecimal discountPrice;

    public TimeDiscountDTO(TimeDiscountItem item){
        this.itemId = item.getItemId();
        this.discountId = item.getDiscountId();
        this.startTime = TigUtils.handelTime(item.getStartTime());
        this.endTime = TigUtils.handelTime(item.getEndTime());
        this.productId = item.getProductId();
        this.skuIds = item.getSkuIds();
        this.value = item.getValue();
        this.discountType = item.getDiscountType();
    }
}
