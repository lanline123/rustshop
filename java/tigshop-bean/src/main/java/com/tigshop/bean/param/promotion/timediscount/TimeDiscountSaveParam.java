package com.tigshop.bean.param.promotion.timediscount;

import com.tigshop.bean.model.promotion.TimeDiscount;
import com.tigshop.bean.model.promotion.TimeDiscountItem;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.promotion.TimeDiscountConstants.*;

/**
 * 限时折扣创建数据对象
 *
 * @author kidd
 * @create 2025/7/1
 */
@Data
@Schema(description = "限时折扣参数")
public class TimeDiscountSaveParam {

    @NotBlank(message = PROMOTION_NAME_NOT_NULL)
    @Size(max = 255, message = PROMOTION_NAME_OVER_LENGTH)
    @Schema(description = "活动名称")
    private String promotionName;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @NotEmpty(message = TIME_DISCOUNT_ITEM_NOT_NULL)
    @Schema(description = "活动商品")
    private List<TimeDiscountItemParam> item;

    @Schema(description = "适用会员范围：0-全部会员，1-指定标记的会员")
    private Integer memberRange;

    @Schema(description = "选择的会员标记列表")
    private List<Integer> memberMarks;

    @Schema(description = "会员人群圈选（历史结构，仅供旧调用方兼容；新调用方请用 sendUserTarget）")
    private com.tigshop.bean.dto.user.MemberAudienceDTO memberAudience;

    @Schema(description = "发送会员/参与人群（指定会员+等级+标签三组并集，优先于 memberAudience）")
    private com.tigshop.bean.dto.user.SendUserTargetDTO sendUserTarget;

    public List<Integer> getProductIds() {
        return item.stream().map(TimeDiscountItemParam::getProductId).collect(Collectors.toList());
    }

    public TimeDiscount createTimeDiscount() {
        Integer shopId = HeaderUtils.getShopId();
        return TimeDiscount.builder()
                .promotionName(this.promotionName)
                .startTime(StringUtils.dateToTimestampExample(this.startTime))
                .endTime(StringUtils.dateToTimestampExample(this.endTime))
                .shopId(shopId)
                .addTime(StringUtils.getCurrentTime())
                .build();
    }

    public List<TimeDiscountItem> createTimeDiscountItems() {
        return item.stream().map(TimeDiscountItemParam::createTimeDiscountItem).toList();
    }

}
