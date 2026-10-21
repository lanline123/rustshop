package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 买家秀点赞表
 * @author wzh
 */
@TableName(value ="buyer_show_like")
@Data
@Schema(description = "买家秀点赞表")
public class BuyerShowLike {
    @TableId(type = IdType.AUTO)
    @Schema(description = "自增ID")
    private Integer id;

    @Schema(description = "买家秀ID")
    private Integer buyerShowId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "发布时间")
    private Long addTime;
}