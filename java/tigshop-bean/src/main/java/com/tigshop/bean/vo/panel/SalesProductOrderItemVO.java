package com.tigshop.bean.vo.panel;

import com.tigshop.bean.model.order.AftersalesItem;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.product.ECardItemVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单商品VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "订单商品参数")
public class SalesProductOrderItemVO {

    // *** OrderItem ***

    @Schema(description = "订单商品信息自增id")
    private Integer itemId;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "订单编号")
    private String orderSn;

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "商品最终单价")
    private BigDecimal price;

    @Schema(description = "商品的购买数量")
    private Integer quantity;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品的名称")
    private String productName;

    @Schema(description = "商品的唯一货号")
    private String productSn;

    @Schema(description = "商品缩略图")
    private String picThumb;

    @Schema(description = "规格ID")
    private Integer skuId;

    @Schema(description = "【JSON】sku信息[{sku_id,sku_value,sku_name}]")
    private String skuData;

    @Schema(description = "发货数量")
    private Integer deliveryQuantity;

    @Schema(description = "商品类型，1：实体，2：虚拟，3赠品")
    private Integer productType;

    @Schema(description = "是否为赠品")
    private Integer isGift;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "是否拼团")
    private Integer isPin;

    @Schema(description = "预售价格")
    private BigDecimal prepayPrice;

    @Schema(description = "佣金数据")
    private String commission;

    @Schema(description = "商品初始价格")
    private BigDecimal originPrice;

    @Schema(description = "优惠信息")
    private String promotionData;

    // *** Other ***

    @Schema(description = "订单信息")
    private OrderVO orders;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "商品重量")
    private BigDecimal productWeight;

    @Schema(description = "售后申请信息")
    private AftersalesItem aftersalesItem;

    @Schema(description = "电子卡券信息")
    private List<ECardItemVO> eCard;

    @Schema(description = "商品小计价格")
    private BigDecimal subtotal;

    @Schema(description = "评论详情")
    private CommentInfoVO commentInfo;

    @Data
    public static class CommentInfoVO {
        @Schema(description = "评论id")
        private Integer commentId;

        @Schema(description = "评论星级")
        private Integer commentRank;

        @Schema(description = "评论内容")
        private String content;

        @Schema(description = "评论标签")
        private List<String> commentTag;

        @Schema(description = "评论图片")
        private List<String> showPics;

        @Schema(description = "店铺id")
        private Integer shopId;

        @Schema(description = "是否展示")
        private Integer isShowed;
    }
}
