package com.tigshop.bean.model.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商品询价表
 *
 * @author Tigshop
 */
@TableName(value = "price_inquiry")
@Data
public class PriceInquiry {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 联系电话
     */
    private String mobile;

    /**
     * 联系邮箱
     */
    private String email;

    /**
     * 需求信息
     */
    private String content;

    /**
     * 商品id
     */
    private Integer productId;

    /**
     * 回复备注
     */
    private String remark;

    /**
     * 回复状态：0 未回复 1 已回复
     */
    private Integer status;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 用户侧是否删除：0 否 1 是
     */
    private Integer userIsDel;

    /**
     * 创建时间
     */
    private Long createTime;
}
