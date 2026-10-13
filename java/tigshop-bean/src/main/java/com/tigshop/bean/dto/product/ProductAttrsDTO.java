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

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品属性
 *
 * @author Tigshop团队
 * @create 2024年12月11日 14:50
 */
@Data
@Schema(description = "商品属性参数")
public class ProductAttrsDTO {
    private List<Attribute> normal;
    private List<Attribute> spe;
    private List<Attribute> extra;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Attribute {
        @Schema(description = "属性对象")
        private List<ProductAttributeDTO> attrList;

        @Schema(description = "属性名称")
        @JsonTranslate
        private String attrName;
    }
}
