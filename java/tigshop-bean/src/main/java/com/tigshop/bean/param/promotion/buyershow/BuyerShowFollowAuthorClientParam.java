package com.tigshop.bean.param.promotion.buyershow;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author wzh
 */
@Data
@Schema(description = "买家秀关注作者参数-客户端")
public class BuyerShowFollowAuthorClientParam {

    @NotNull(message = "作者信息错误")
    @Schema(description = "作者（会员）id")
    private Integer authorId;

    @NotNull(message = "操作失败")
    @Schema(description = "关注 / 取消关注（0：取消关注，1：关注）")
    private Integer isFollow;
}
