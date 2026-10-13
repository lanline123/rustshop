package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 买家秀
 *
 * @author wzh
 * &#064;TableName  buyer_show
 */
@Data
@TableName(value = "buyer_show")
@Schema(description = "买家秀")
public class BuyerShow implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "自增ID，买家秀ID")
    private Integer buyerShowId;

    @Schema(description = "关联商品数据 （json）")
    private String rangeData;

    @Schema(description = "用户id（非真实用户为0）")
    private Integer userId;

    @Schema(description = "买家头像")
    private String buyerPhoto;

    @Schema(description = "买家名称")
    private String buyerName;

    @Schema(description = "文字内容")
    private String content;

    @Schema(description = "所属分组（json）")
    private String groupData;

    @Schema(description = "创建时间")
    private Long addTime;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否公开（0：隐藏，1：公开）")
    private Integer isShow;

    @Schema(description = "状态（0：待审核，1：通过，2：拒绝）")
    private Integer status;

}