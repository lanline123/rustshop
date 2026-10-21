// **---------------------------------------------------------------------+
// ** 文件 -- ${DESCRIPTION}
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.dto.cart;

import cn.hutool.core.collection.CollUtil;
import com.tigshop.bean.enums.cart.CartTypeEnum;
import com.tigshop.bean.param.promotion.giftcard.ExchangeCardParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 添加购物车
 *
 * @author Jayce
 * @create 2024年10月22日 13:50
 */
@Data
@Schema(description = "添加购物车")
public class AddToCartDTO {
    @Schema(description = "商品/活动ID")
    @NotNull(message = "id不能为空")
    private Integer id;

    @Schema(description = "数量")
    @NotNull(message = "数量不能为空")
    private Integer number;

    @Schema(description = "商品规格ID")
    private Integer skuId;

    @Schema(description = "商品规格属性")
    private List<SkuItem> skuItem;

    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "销售员ID")
    private Integer salesmanId;

    @Schema(description = "是否快速购买")
    private Boolean isQuick;

    @Schema(description = "商品多规格属性 多个id 用逗号分割")
    private String extraAttrIds;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "拼团信息")
    private GrouponData grouponData;

    @Schema(description = "卡信息")
    private ExchangeCardParam giftCardData;

    @Schema(description = "抽奖记录id")
    private Integer lotteryRecordId;

    // *** 周期购相关参数 ***
    @Schema(description = "周期购规格ID（周期购商品必填）")
    private Integer cycleSpecId;

    @Schema(description = "兑换次卡ID（次卡兑换时必填，非空即进入兑换模式）")
    private Integer redeemCardId;

    @Schema(description = "代客下单",hidden = true)
    private Integer finalCartType;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrouponData {

        @Schema(description = "拼团活动 ID")
        private Long grouponPromotionId;

        @Schema(description = "拼团活动商品 ID")
        private Long grouponPromotionItemId;

        @Schema(description = "成团信息 ID")
        private Long grouponDetailId;

        @Schema(description = "成团人数 key")
        private Integer groupNumKey;

        @Schema(description = "拼团价")
        private BigDecimal price;

        @Schema(description = "开团记录 ID")
        private Long grouponPromotionRecordId;
    }

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "购物车id")
    private Integer originalCartId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkuItem {

        @Schema(description = "商品ID")
        private Integer productId;

        @Schema(description = "规格ID")
        private Integer skuId;

        @Schema(description = "规格数量")
        @NotNull(message = "数量不能为空")
        private Integer num;

    }

    public void initParam() {
        if (this.type == null) {
            this.type = CartTypeEnum.TYPE_NORMAL.getCode();
        }
        if (this.salesmanId == null) {
            this.salesmanId = 0;
        }

        if (CollUtil.isNotEmpty(this.skuItem)) {
            this.skuItem.forEach(item -> item.setProductId(this.id));
        }
    }
}
