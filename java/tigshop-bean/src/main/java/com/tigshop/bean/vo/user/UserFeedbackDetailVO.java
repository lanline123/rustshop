package com.tigshop.bean.vo.user;

import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户留言VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "用户留言参数")
public class UserFeedbackDetailVO {
    @Schema(description = "问题类型名称")
    private String typeName;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "留言ID")
    private Integer id;

    @Schema(description = "留言标题")
    private String title;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "留言内容")
    private String content;

    @Schema(description = "问题类型")
    private Integer type;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "产品ID")
    private Integer productId;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "订单号")
    private String orderSn;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "产品信息")
    private Product product;

    @Schema(description = "订单信息")
    private OrderInfo orderInfo;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "店铺信息")
    private Shop shop;

    @Schema(description = "回复信息列表")
    private List<Reply> replyList;

    @Schema(description = "图片")
    private List<String> feedbackPics;

    @Data
    public static class Product{
        @Schema(description = "产品ID")
        private Integer productId;

        @Schema(description = "产品名称")
        private String productName;
    }

    @Data
    public static class OrderInfo{
        @Schema(description = "订单ID")
        private Integer orderId;

        @Schema(description = "订单号")
        private String orderSn;
    }

    @Data
    public static class Shop{
        @Schema(description = "店铺ID")
        private Integer shopId;

        @Schema(description = "店铺名称")
        private String shopTitle;
    }

    @Data
    public static class Reply{
        @Schema(description = "id")
        private Integer id;

        @Schema(description = "父id")
        private Integer parentId;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "email")
        private String email;

        @Schema(description = "mobile")
        @Sensitive(type = SensitiveType.PHONE)
        private String mobile;

        @Schema(description = "回复内容")
        private String content;

        @Schema(description = "状态")
        private Integer status;

        @Schema(description = "类型")
        private Integer type;
    }
}

