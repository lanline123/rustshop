// **-----------------------------------------------------------------+
// ** 文件 --
// **-----------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **-----------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **-----------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **-----------------------------------------------------------------+

package com.tigshop.bean.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 微信直播间添加商品DTO
 *
 * @author Tigshop团队
 * @create 2024年12月27日 17:13
 */
@Data
@Schema(description = "微信直播间添加商品DTO")
public class WechatLiveGoodsAddDTO {

    @Schema(description = "直播间ID", required = true)
    @NotNull(message = "直播间ID不能为空")
    private Integer roomId;

    @Schema(description = "直播商品主表ID列表", required = true)
    @NotEmpty(message = "直播商品ID列表不能为空")
    private List<Long> goodsIds;
}
