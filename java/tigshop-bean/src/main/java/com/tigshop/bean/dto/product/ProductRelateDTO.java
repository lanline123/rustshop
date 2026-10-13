// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品相关信息
 *
 * @author Tigshop团队
 * @create 2025年02月08日 15:21
 */
@Data
public class ProductRelateDTO {
    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "查询数")
    private Integer size;

    @Schema(description = "排行榜显示数量")
    private Integer rankNum;

    @Schema(description = "商品情况，默认hot")
    private String intro;

    @Schema(description = "店铺ID")
    private Integer shopId;

    public ProductRelateDTO() {
        this.size = 10;
        this.rankNum = 5;
        this.intro = "hot";
        this.productId = 0;
    }
}