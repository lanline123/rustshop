// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.msg;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 语音播报内容列表VO
 *
 * <p>在管理员消息基础字段之上，额外一次性带出订单/售后的商品摘要与金额，
 * 供后台「语音播报内容」抽屉卡片直接渲染，避免前端逐条拉取订单/售后详情。
 * 仅供语音播报专用列表端点使用，不影响消息中心 {@link AdminMsgVO} 原有口径。</p>
 *
 * @author Tigshop团队
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "语音播报内容列表项（含商品摘要）")
public class AdminMsgBroadcastVO extends AdminMsgVO {

    @Schema(description = "商品摘要列表（订单取订单商品；售后取售后商品）")
    private List<BroadcastProductVO> products;

    @Schema(description = "订单总金额（订单类消息）")
    private BigDecimal totalAmount;

    @Schema(description = "订单供货价合计（订单类消息）")
    private BigDecimal supplyTotalAmount;

    @Schema(description = "配送费/运费（订单类消息）")
    private BigDecimal shippingFee;

    @Schema(description = "配送类型ID（前端据此判断运费展示名称）")
    private Integer shippingTypeId;

    @Schema(description = "退款金额（售后类消息）")
    private BigDecimal refundAmount;

    /**
     * 商品摘要，字段口径与卡片渲染一致：缩略图 + 名称 + 单价 × 数量。
     */
    @Data
    @Schema(description = "商品摘要")
    public static class BroadcastProductVO {
        @Schema(description = "商品缩略图")
        private String picThumb;

        @Schema(description = "商品名称")
        private String productName;

        @Schema(description = "商品单价")
        private BigDecimal price;

        @Schema(description = "商品供货价快照")
        private BigDecimal supplyPrice;

        @Schema(description = "商品供货价小计")
        private BigDecimal supplySubtotal;

        @Schema(description = "购买/售后数量")
        private Integer quantity;
    }
}
