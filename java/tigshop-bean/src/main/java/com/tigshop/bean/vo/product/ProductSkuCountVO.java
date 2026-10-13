// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品 SKU 数量统计投影（COUNT(*) GROUP BY product_id）。
 *
 * <p>用途：CR-20260603-1720 / CD-035 C 端列表卡片 wholesaleCardMoq 派生 — 判定单/多规格
 * （skuCount &le; 1 = 单规格，可卡片加购；&gt; 1 = 多规格，跳详情）。
 *
 * @author Tigshop团队
 * @create 2026-06-03
 */
@Schema(description = "商品 SKU 数量统计投影（COUNT(*) GROUP BY product_id）")
@Data
public class ProductSkuCountVO {

    @Schema(description = "商品 ID")
    private Integer productId;

    @Schema(description = "SKU 数量")
    private Integer skuCount;
}
