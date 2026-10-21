package com.tigshop.bean.vo.order;

import cn.hutool.json.JSONArray;
import com.tigshop.bean.model.order.AftersalesItem;
import com.tigshop.bean.vo.product.ECardItemVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 售后管理客户端结果
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
@Data
@Schema(description = "售后管理客户端结果")
public class AfterSalesClientVO {
    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "订单编号")
    private String orderSn;

    @Schema(description = "发货时间")
    private String shippingTime;

    @Schema(description = "订单商品列表")
    private List<Item> items;

    @Data
    @Schema(description = "订单商品信息")
    public static class Item {
        @Schema(description = "商品ID")
        private Integer itemId;

        @Schema(description = "订单ID")
        private Integer orderId;

        @Schema(description = "产品ID")
        private Integer productId;

        @Schema(description = "商品价格")
        private BigDecimal price;

        @Schema(description = "商品数量")
        private Integer quantity;

        @Schema(description = "商品名称")
        private String productName;

        @Schema(description = "商品编号")
        private String productSn;

        @Schema(description = "商品缩略图")
        private String picThumb;

        @Schema(description = "商品图片缩略图")
        private String productPicThumb;

        @Schema(description = "商品库存")
        private Integer productStock;

        @Schema(description = "商品重量")
        private BigDecimal productWeight;

        @Schema(description = "虚拟样品")
        private String virtualSample;

        @Schema(description = "付费内容")
        private JSONArray paidContent;

        @Schema(description = "卡片组ID")
        private Integer cardGroupId;

        @Schema(description = "售后服务商品列表")
        private AftersalesItem aftersalesItem;

        @Schema(description = "售后服务标志")
        private Integer aftersaleFlag;

        @Schema(description = "电子卡")
        private List<ECardItemVO> eCard;

        @Schema(description = "是否可以售后；0-否，1-是")
        private Boolean toAftersalses;
    }
}

