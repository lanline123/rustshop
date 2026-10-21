// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.promotion;

import com.tigshop.bean.model.promotion.ProductGift;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 满减活动VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "满减活动参数")
public class ProductPromotionVO {
    @Schema(description = "活动ID")
    private Integer promotionId;

    @Schema(description = "活动名称")
    private String promotionName;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "[JSON]仅限会员等级")
    private List<Integer> limitUserRank;

    @Schema(description = "活动范围:0,全部商品;3,指定商品.4，不含以下商品")
    private Integer range;

    @Schema(description = "[json]活动范围相关信息")
    private List<Integer> rangeData;

    @Schema(description = "订单金额下限")
    private BigDecimal minOrderAmount;

    @Schema(description = "订单金额上限，可用于阶梯设置")
    private BigDecimal maxOrderAmount;

    @Schema(description = "优惠类型，1：满减，2：折扣，3：满赠")
    private Integer promotionType;

    @Schema(description = "[data]类型相关数据，如满减金额、折扣金额、赠品等")
    private List<PromotionTypeData> promotionTypeData;

    @Schema(description = "是否启用")
    private Integer isAvailable;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "优惠规则：1 阶梯优惠 0 循环优惠")
    private Integer rulesType;

    @Schema(description = "优惠单位：1 元 2 件")
    private Integer unit;

    @Schema(description = "活动名称")
    private String promotionTypeName;

    @Schema(description = "状态")
    private String productStatus;

    @Schema(description = "产品时间")
    private List<String> productTime;

    @Schema(description = "赠品")
    private List<ProductGift> productGift;

    @Schema(description = "适用会员范围：0-全部会员，1-指定标记的会员")
    private Integer memberRange;

    @Schema(description = "选择的会员标记列表")
    private List<Integer> memberMarks;

    @Schema(description = "会员人群圈选（历史结构，无法无损表达 sendUserTarget 时为空）")
    private com.tigshop.bean.dto.user.MemberAudienceDTO memberAudience;

    @Schema(description = "发送会员/参与人群（指定会员+等级+标签三组并集）")
    private com.tigshop.bean.dto.user.SendUserTargetDTO sendUserTarget;

    @Data
    public static class PromotionTypeData {
        @Schema(description = "最小金额")
        private BigDecimal minAmount;

        @Schema(description = "减免金额")
        private BigDecimal reduce;

        @Schema(description = "赠品ID")
        private Integer giftId;

        @Schema(description = "赠品库存")
        private Integer giftStock;

        @Schema(description = "赠品名称")
        @JsonTranslate(dataType = 2)
        private String giftName;

        @Schema(description = "数量")
        private Integer num;
    }
}