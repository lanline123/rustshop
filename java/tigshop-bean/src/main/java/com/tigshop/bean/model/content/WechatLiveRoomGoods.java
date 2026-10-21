package com.tigshop.bean.model.content;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 微信直播间商品关联表
 */
@Data
@TableName("wechat_live_room_goods")
@Schema(description = "微信直播间商品关联表")
public class WechatLiveRoomGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "直播间ID")
    @TableField(value = "room_id")
    private Integer roomId;

    @Schema(description = "直播商品主表ID")
    @TableField(value = "wechat_live_goods_id")
    private Long wechatLiveGoodsId;

    @Schema(description = "微信商品ID")
    @TableField(value = "wechat_goods_id")
    private Integer wechatGoodsId;

    @Schema(description = "直播间商品排序")
    @TableField(value = "sort")
    private Integer sort;

    @Schema(description = "是否显示：0-显示，1-隐藏")
    @TableField(value = "is_show")
    private Integer isShow;

    @Schema(description = "创建时间")
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    @Schema(description = "店铺ID")
    @TableField(value = "shop_id")
    private Integer shopId;
}
