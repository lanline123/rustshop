package com.tigshop.bean.vo.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.bean.dto.content.WechatLiveContextDTO;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.model.order.AftersalesItem;
import com.tigshop.bean.vo.product.ECardItemVO;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.core.BigDecimalSerializer;
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
public class OrderItemVO {
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

    @Schema(description = "供应商订单供货价快照")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal supplyPrice;

    @Schema(description = "供应商订单供货价小计")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal supplySubtotal;

    /**
     * 订单创建时保存的供应商供货价，仅供后台供应商订单价格组装使用，不对外序列化。
     */
    @JsonIgnore
    private BigDecimal vendorProductSupplyPrice;

    @Schema(description = "商品的购买数量")
    private Integer quantity;

    @Schema(description = "允许发货数量")
    private Integer allowDeliverNum;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品的名称")
    @JsonTranslate(dataType = 2)
    private String productName;

    @Schema(description = "商品的唯一货号")
    private String productSn;

    @Schema(description = "商品重量")
    private BigDecimal productWeight;

    @Schema(description = "虚拟样品")
    private String virtualSample;

    @Schema(description = "商品缩略图")
    private String picThumb;

    @Schema(description = "规格ID")
    private Integer skuId;

    @Schema(description = "【JSON】sku信息[{sku_id,sku_value,sku_name}]")
    private List<ProductSkuDTO.SkuData> skuData;

    @Schema(description = "规格值")
    private String skuValue;

    @Schema(description = "附加规格")
    private List<OrderExtraSkuDataDTO> extraSkuData;

    @Schema(description = "发货数量")
    private Integer deliveryQuantity;

    @Schema(description = "商品类型，1：实体，2：虚拟，3赠品")
    private Integer productType;

    @Schema(description = "是否为赠品")
    private Integer isGift;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "店铺名称")
    private String shopTitle;

    @Schema(description = "库存")
    private Integer productStock;

    @Schema(description = "是否拼团")
    private Integer isPin;

    @Schema(description = "预售价格")
    private BigDecimal prepayPrice;

    @Schema(description = "佣金数据")
    private String commission;

    @Schema(description = "商品初始价格")
    private BigDecimal originPrice;

    @Schema(description = "改价前单位价")
    private BigDecimal adminChangePrePrice;

    @Schema(description = "优惠信息")
    private String promotionData;

    @Schema(description = "售后申请信息（多条售后时取最后一条，仅兼容旧前端展示）")
    private AftersalesItem aftersalesItem;

    @Schema(description = "该行处于锁定状态（进行中/已完成，不含已拒绝/已取消）的售后累计数量；allowDeliverNum 已扣除该数量，前端可直接使用 allowDeliverNum")
    private Integer aftersalesNumber;

    @Schema(description = "电子卡券信息")
    @JsonProperty("eCard")
    private List<ECardItemVO> eCard;

    @Schema(description = "商品小计价格")
    private BigDecimal subtotal;

    @Schema(description = "税率(百分比)，与订单一致，无税时为0")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private BigDecimal taxRate;

    @Schema(description = "该行商品分摊的税费金额，按订单总税费按商品金额比例分摊，保留2位小数")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private BigDecimal taxRateAmount;

    @Schema(description = "评论详情")
    private CommentInfoVO commentInfo;

    @Schema(description = "供应商ID")
    private Integer vendorId;

    @Schema(description = "供应商商品ID")
    private Integer vendorProductId;

    @Schema(description = "供应商商品skuID")
    private Integer vendorProductSkuId;

    @Schema(description = "是否秒杀")
    private Integer isSeckill;

    @Schema(description = "定金金额（定金预售）")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal depositAmount;

    @Schema(description = "尾款金额（定金预售）")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal finalPaymentAmount;

    @Schema(description = "微信直播订单上下文")
    private WechatLiveContextDTO wechatLiveContext;

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
        private List<ShowPic> showPics;

        @Schema(description = "店铺id")
        private Integer shopId;

        @Schema(description = "是否展示")
        private Integer isShowed;
    }
}
