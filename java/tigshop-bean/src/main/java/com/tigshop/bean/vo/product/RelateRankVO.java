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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Tigshop团队
 * @create 2025/4/2 16:15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "相关排行")
public class RelateRankVO {
    @Schema(description = "相同价位")
    private List<ProductLookAlsoVO> price;
    @Schema(description = "相同品牌")
    private List<ProductLookAlsoVO> brand;
    @Schema(description = "相同类别")
    private List<ProductLookAlsoVO> cate;
}
