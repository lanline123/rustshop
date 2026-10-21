package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("groupon_promotion_shops") // 指定数据库表名
public class GrouponPromotionShop {

    /**
     * 主键ID，自增
     */
    @TableId(value = "groupon_shop_id", type = IdType.AUTO)
    private Integer grouponShopId;

    /**
     * 活动ID
     */
    @TableField("groupon_promotion_id")
    private Integer grouponPromotionId;

    /**
     * 适用店铺IDs
     */
    @TableField("shop_ids")
    private String shopIds;
}
