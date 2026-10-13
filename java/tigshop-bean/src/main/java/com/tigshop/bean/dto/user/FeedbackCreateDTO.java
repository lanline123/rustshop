package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 前台提交的反馈信息
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "反馈信息表参数")
public class FeedbackCreateDTO {
    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "产品id")
    private Integer productId;

    @Schema(description = "订单id")
    private Integer orderId;

    @Schema(description = "当前店铺/门店ID（商品咨询场景由服务端校验后确定最终归属）")
    private Integer shopId;

    @Schema(description = "投诉项")
    private String complaintInfo;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "图片上传")
    private String[] feedbackPics;
}
