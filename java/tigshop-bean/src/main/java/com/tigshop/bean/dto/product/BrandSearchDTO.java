// **---------------------------------------------------------------------+
// ** 文件 --
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
 * 品牌搜索数据对象
 *
 * @author Jayce
 * @create 2024年11月19日 14:33
 */
@Data
@Schema(description = "品牌搜索数据对象")
public class BrandSearchDTO {
    @Schema(description = "品牌id")
    private Integer brandId;

    @Schema(description = "品牌名称")
    private String brandName;

    @Schema(description = "首字母")
    private String firstWord;
}
