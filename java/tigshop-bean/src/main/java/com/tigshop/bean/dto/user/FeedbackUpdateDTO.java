package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台回复
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "后台反馈参数")
public class FeedbackUpdateDTO {
    @Schema(description = "反馈内容")
    private String content;

    @Schema(description = "id")
    private Integer id;

    @Schema(description = "父id")
    private Integer parentId;

    @Schema(description = "产品信息")
    private Product product;

    @Schema(description = "回复标题")
    private String title;

    @Schema(description = "email")
    private String email;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "图片上传")
    private String[] feedbackPics;

    @Schema(description = "product_id")
    private Integer productId;

    @Schema(description = "订单id")
    private Integer orderId;

    @Data
    public static class Product{
        @Schema(description = "产品id")
        private Integer id;

        @Schema(description = "产品名称")
        private String productName;
    }
}
