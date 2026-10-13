package com.tigshop.bean.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 微信直播订单上下文
 *
 * @author Tigshop团队
 * @create 2026年03月08日 16:56
 */
@Data
@Schema(description = "微信直播订单上下文")
public class WechatLiveContextDTO {

    @Schema(description = "直播主键ID")
    private Integer wechatLiveId;

    @Schema(description = "微信直播房间ID")
    private Integer roomId;

    @Schema(description = "微信直播商品ID")
    private Integer wechatGoodsId;

    @Schema(description = "系统商品ID")
    private Integer productId;

    @Schema(description = "系统SKU ID")
    private Integer skuId;

    @Schema(description = "分享人OpenId")
    private String shareOpenId;

    @Schema(description = "观看人OpenId")
    private String viewerOpenId;

    @Schema(description = "直播场景值")
    private String scene;

    @Schema(description = "直播透传参数")
    private String customParams;
}
