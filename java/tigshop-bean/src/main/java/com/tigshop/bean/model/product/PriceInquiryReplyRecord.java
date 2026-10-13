package com.tigshop.bean.model.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 询价回复记录
 *
 * @author Tigshop团队
 */
@TableName(value = "price_inquiry_reply_record")
@Data
public class PriceInquiryReplyRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 询价id
     */
    private Integer inquiryId;

    /**
     * 回复类型：1 商家回复 2 用户回复
     */
    private Integer replyType;

    /**
     * 回复人名称
     */
    private String replyName;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 后台管理员id
     */
    private Integer adminId;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 回复时间
     */
    private Long createTime;
}
