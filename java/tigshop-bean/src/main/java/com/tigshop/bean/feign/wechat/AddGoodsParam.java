// **-----------------------------------------------------------------+
// ** 文件 --
// **-----------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **-----------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **-----------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **-----------------------------------------------------------------+

package com.tigshop.bean.feign.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 添加商品到直播间参数
 *
 * @author Tigshop团队
 * @create 2024年12月27日 17:13
 */
@Data
@Schema(description = "添加商品到直播间参数")
public class AddGoodsParam {

    @Schema(description = "房间ID")
    private Integer roomId;

    @Schema(description = "商品ID列表")
    private List<Integer> goodsIds;
}
