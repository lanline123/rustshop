package com.tigshop.bean.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 反馈信息
 */
@Data
@Schema(description = "反馈信息")
@TableName(value = "feedback")
public class Feedback implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "反馈ID")
    private Integer id;

    @Schema(description = "是否为回复")
    private Integer parentId;

    @Schema(description = "反馈会员id")
    private Integer userId;

    @Schema(description = "反馈会员名")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "反馈标题")
    private String title;

    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "状态，0：待回复，1：已回复，2：无效")
    private Integer status;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "时间")
    private Long addTime;

    @Schema(description = "【JSON】图片上传")
    private String feedbackPics;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "投诉项")
    private String complaintInfo;

    @Schema(description = "投诉店铺")
    private Integer shopId;
}
