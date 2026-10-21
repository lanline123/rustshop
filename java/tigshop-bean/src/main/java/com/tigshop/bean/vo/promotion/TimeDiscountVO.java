package com.tigshop.bean.vo.promotion;

import com.tigshop.bean.model.promotion.TimeDiscount;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 限时折扣VO
 *
 * @author kidd
 * @create 2025/7/1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "限时折扣参数")
public class TimeDiscountVO {

    @Schema(description = "活动ID")
    private Integer discountId;

    @Schema(description = "活动名称")
    private String promotionName;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "创建时间")
    private String addTime;

    @Schema(description = "活动状态")
    private String statusName;

    @Schema(description = "适用会员范围：0-全部会员，1-指定标记的会员")
    private Integer memberRange;

    @Schema(description = "选择的会员标记列表")
    private List<Integer> memberMarks;

    public TimeDiscountVO(TimeDiscount timeDiscount) {
        this.discountId = timeDiscount.getDiscountId();
        this.promotionName = timeDiscount.getPromotionName();
        this.startTime = TigUtils.handelTime(timeDiscount.getStartTime());
        this.endTime = TigUtils.handelTime(timeDiscount.getEndTime());
        this.shopId = timeDiscount.getShopId();
        this.addTime = TigUtils.handelTime(timeDiscount.getAddTime());
    }
}