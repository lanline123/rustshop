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

import com.tigshop.bean.dto.product.BrandSearchDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 品牌查询参数（商品查询的下拉列表）
 *
 * @author Jayce
 * @create 2024年11月19日 14:31
 */
@Getter
@Setter
@Schema(description = "品牌查询参数")
public class BrandSearchVO {
    @Schema(description = "品牌列表")
    private List<BrandSearchDTO> brandList;

    @Schema(description = "品牌名称列表")
    private List<String> firstWordList;

    public BrandSearchVO(List<BrandSearchDTO> brandList, List<String> firstWordList) {
        this.brandList = brandList;
        this.firstWordList = firstWordList;
    }
}
