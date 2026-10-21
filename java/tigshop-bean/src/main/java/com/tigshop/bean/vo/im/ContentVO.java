package com.tigshop.bean.vo.im;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 会话消息VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "消息内容")
public class ContentVO {
    @Schema(description = "消息类型")
    private String messageType;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "图片")
    private String pic;

    @Schema(description = "视频地址")
    private String videoUrl;

    @Schema(description = "视频封面")
    private String videoCover;

    @Schema(description = "消息分类")
    private String contentCategory;

    @Schema(description = "订单消息")
    private OrderInfoVO order;

    @Schema(description = "商品信息")
    private ProductInfoVO product;

    @Data
    @Schema(description = "商品信息")
    public static class ProductInfoVO {
        @Schema(description = "消息类型")
        private String messageType;

        @Schema(description = "商品ID")
        private Integer productId;

        @Schema(description = "商品图片")
        private String picUrl;

        @Schema(description = "商品名称")
        private String productName;

        @Schema(description = "商品sn")
        private String productSn;

        @Schema(description = "商品价格")
        private BigDecimal productPrice;

    }

    @Data
    @Schema(description = "订单信息")
    public static class OrderInfoVO {
        @Schema(description = "消息类型")
        private String messageType;

        @Schema(description = "订单ID")
        private Integer orderId;

        @Schema(description = "订单sn")
        private String orderSn;

        @Schema(description = "订单图片")
        private String picUrl;

        @Schema(description = "产品名称")
        private String productName;

        @Schema(description = "产品数量")
        private Integer productNum;

        @Schema(description = "产品价格")
        private BigDecimal totalAmount;

        @Schema(description = "订单状态")
        private String orderStatusName;

        @Schema(description = "商品类型")
        private Integer productType;
    }
}
