package com.tigshop.bean.model.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 店铺表model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("collect_shop")
@Schema(description = "会员收藏店铺的记录列表")
public class CollectShop {
    @Schema(description = "店铺表ID")
    @TableId(value = "collect_id", type = IdType.AUTO)
    private Integer collectId;

    @Schema(description = "会员ID")
    private Integer userId;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "收藏时间")
    private Long addTime;
}
