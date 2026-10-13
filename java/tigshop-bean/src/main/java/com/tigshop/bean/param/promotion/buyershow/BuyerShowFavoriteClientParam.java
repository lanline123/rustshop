package com.tigshop.bean.param.promotion.buyershow;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author wzh
 */
@Data
@Schema(description = "买家秀收藏参数-客户端")
public class BuyerShowFavoriteClientParam {

    @NotNull(message = "买家秀信息错误")
    @Schema(description = "买家秀id")
    private Integer buyerShowId;

    @NotNull(message = "操作失败")
    @Schema(description = "收藏 / 取消收藏（0：取消收藏，1：收藏）")
    private Integer isFavorite;
}
