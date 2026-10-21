package com.tigshop.bean.param.promotion.buyershow;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author wzh
 */
@Data
@Schema(description = "买家秀点赞参数-客户端")
public class BuyerShowLikeClientParam {

    @NotNull(message = "买家秀信息错误")
    @Schema(description = "买家秀id")
    private Integer buyerShowId;

    @NotNull(message = "操作失败")
    @Schema(description = "点赞 / 取消点赞（0：取消点赞，1：点赞）")
    private Integer isLike;
}
