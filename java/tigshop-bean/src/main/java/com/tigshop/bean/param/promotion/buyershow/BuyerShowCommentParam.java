package com.tigshop.bean.param.promotion.buyershow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author wzh
 */
@Data
@Schema(description = "评论买家秀参数")
public class BuyerShowCommentParam {

    @NotNull(message = "买家秀id不能为空")
    @Schema(description = "自增ID，买家秀ID")
    private Integer buyerShowId;

    @NotBlank(message = "用户名称不能为空")
    @Schema(description = "用户名称")
    private String userName;

    @NotBlank(message = "用户头像不能为空")
    @Schema(description = "用户头像")
    private String userPhoto;

    @NotBlank(message = "评论内容不能为空")
    @Schema(description = "评论内容")
    private String content;

}
