package com.tigshop.bean.dto.content;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 微信直播商品查询参数
 */
@Getter
@Setter
@Schema(description = "微信直播商品查询参数")
public class WechatLiveGoodsQueryDTO extends BasePage {

    @Schema(description = "直播间ID", required = true)
    @NotNull(message = "直播间ID不能为空")
    private Integer roomId;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "商品名称或编号")
    private String keyword;
}
