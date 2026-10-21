// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

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

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.TableField;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品SKU数据参数
 *
 * @author Jayce
 * @create 2024年11月21日 11:26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSkuDTO {
    @Schema(description = "规格ID")
    private Integer skuId;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品规格值")
    private String skuValue;

    @Schema(description = "商品规格值（JSON）")
    private List<SkuData> skuData;

    @Schema(description = "货品编码")
    private String skuSn;

    @Schema(description = "货品库存")
    private Integer skuStock;

    @Schema(description = "条形码")
    private String skuTsn;

    @Schema(description = "货品价格")
    private String skuPrice;

    @Schema(description = "是否启用：0-禁用，1-启用")
    private Integer isEnabled;

    @TableField("market_price")
    @Schema(description = "市场价（划线价）")
    private BigDecimal marketPrice;

    @TableField("cost_price")
    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "配送员服务费类型：0=固定金额 1=按比例（仅 O2O 版本使用）")
    @Min(value = 0, message = "配送员服务费类型必须为 0(固定金额) 或 1(按比例)")
    @Max(value = 1, message = "配送员服务费类型必须为 0(固定金额) 或 1(按比例)")
    private Integer deliveryServiceFeeType;

    @Schema(description = "配送员服务费值：固定金额(元)或按比例(%)（仅 O2O 版本使用）")
    @DecimalMin(value = "0.00", message = "配送员服务费值不能小于 0")
    private BigDecimal deliveryServiceFeeValue;

    @Schema(description = "服务员服务费类型：0=固定金额 1=按比例（仅 O2O 上门服务订单使用）")
    @Min(value = 0, message = "服务员服务费类型必须为 0(固定金额) 或 1(按比例)")
    @Max(value = 1, message = "服务员服务费类型必须为 0(固定金额) 或 1(按比例)")
    private Integer serverServiceFeeType;

    @Schema(description = "服务员服务费值：固定金额(元)或按比例(%)（仅 O2O 上门服务订单使用）")
    @DecimalMin(value = "0.00", message = "服务员服务费值不能小于 0")
    private BigDecimal serverServiceFeeValue;

    // *** O2O detail 角色感知扩展（CR-20260513-160000）：仅 product/detail 返回，前端编辑表单使用 ***

    @Schema(description = "[O2O detail 扩展] 平台层配送员服务费类型（门店端只读，不可改）")
    private Integer platformDeliveryServiceFeeType;

    @Schema(description = "[O2O detail 扩展] 平台层配送员服务费值（门店端只读，不可改）")
    private BigDecimal platformDeliveryServiceFeeValue;

    @Schema(description = "[O2O detail 扩展] 平台层服务员服务费类型（门店端只读，不可改）")
    private Integer platformServerServiceFeeType;

    @Schema(description = "[O2O detail 扩展] 平台层服务员服务费值（门店端只读，不可改）")
    private BigDecimal platformServerServiceFeeValue;

    @Schema(description = "[O2O detail 扩展] 门店覆盖层配送员服务费类型（仅 storeAssignDeliveryServiceFee=1 时可改）")
    private Integer storeDeliveryServiceFeeType;

    @Schema(description = "[O2O detail 扩展] 门店覆盖层配送员服务费值")
    private BigDecimal storeDeliveryServiceFeeValue;

    @Schema(description = "[O2O detail 扩展] 门店覆盖层服务员服务费类型（仅 storeAssignServerServiceFee=1 时可改）")
    private Integer storeServerServiceFeeType;

    @Schema(description = "[O2O detail 扩展] 门店覆盖层服务员服务费值")
    private BigDecimal storeServerServiceFeeValue;

    @Schema(description = "供应商产品skuID")
    private Integer vendorProductSkuId;

    @Schema(description = "实时计算的定金金额（仅定金预售返回）")
    private BigDecimal depositAmount;

    @Schema(description = "尾款金额（定金预售）")
    private BigDecimal finalPaymentAmount;

    // *** Other ***

    @Schema(description = "供货价")
    private BigDecimal supplyPrice;

    @Data
    @NoArgsConstructor
    public static class SkuData {

        @Schema(description = "规格值名称")
        @JsonTranslate(dataType = 8)
        private String name;

        @Schema(description = "规格值名称")
        @JsonTranslate(dataType = 8)
        private String value;

        @Schema(description = "规格图片")
        @JsonTranslate(dataType = 7)
        private String attrPicThumb;

        public SkuData(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public SkuData(String name, String value, String attrPicThumb) {
            this.name = name;
            this.value = value;
            this.attrPicThumb = attrPicThumb;
        }
    }

    public ProductSkuDTO(ProductSku productSku) {
        this.skuId = productSku.getSkuId();
        this.productId = productSku.getProductId();
        this.skuValue = productSku.getSkuValue();
        if (StrUtil.isNotBlank(productSku.getSkuData())) {
            this.skuData = JSON.parseArray(productSku.getSkuData(), SkuData.class);
        }
        this.skuSn = productSku.getSkuSn();
        this.skuStock = productSku.getSkuStock();
        this.skuTsn = productSku.getSkuTsn();
        this.skuPrice = productSku.getSkuPrice().toString();
        this.isEnabled = productSku.getIsEnabled();
    }
}
