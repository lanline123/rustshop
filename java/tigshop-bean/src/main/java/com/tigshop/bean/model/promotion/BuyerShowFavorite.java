package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 买家秀收藏
 * &#064;TableName  buyer_show_favorite
 *
 * @author wzh
 */
@TableName(value = "buyer_show_favorite")
@Data
@Schema(description = "买家秀收藏")
public class BuyerShowFavorite implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "买家秀ID")
    private Integer buyerShowId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "收藏时间")
    private Long addTime;
}
