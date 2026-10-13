package com.tigshop.bean.param.product;

import com.tigshop.bean.model.product.ProductStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "商品样式编辑参数")
public class ProductStyleEditParam extends ProductStyleSaveParam {

    @NotNull(message = "id 不能为空")
    @Schema(description = "样式ID")
    private Integer styleId;

    @Override
    public ProductStyle createProductStyle() {
        ProductStyle productStyle = super.createProductStyle();
        productStyle.setStyleId(styleId);
        productStyle.setAddTime(null);
        productStyle.setUpdateTime(System.currentTimeMillis() / 1000);
        return productStyle;
    }
}
