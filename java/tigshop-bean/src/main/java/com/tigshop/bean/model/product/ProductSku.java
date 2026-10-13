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
package com.tigshop.bean.model.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品SKU实体类
 *
 * @author Jayce
 * @create 2024年11月20日 14:50
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品SKU实体类")
@TableName("product_sku")
public class ProductSku implements Serializable {

    @TableId(value = "sku_id", type = IdType.AUTO)
    @Schema(description = "规格ID")
    private Integer skuId;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品规格值")
    private String skuValue;

    @Schema(description = "商品规格值（JSON）")
    private String skuData;

    @Schema(description = "货品编码")
    private String skuSn;

    @Schema(description = "货品库存")
    private Integer skuStock;

    @Schema(description = "条形码")
    private String skuTsn;

    @Schema(description = "货品价格")
    private BigDecimal skuPrice;

    @Schema(description = "是否启用：0-禁用，1-启用")
    private Integer isEnabled;

    @Schema(description = "市场价（划线价）")
    private BigDecimal marketPrice;

    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "配送员服务费类型：0=固定金额 1=按比例（仅 O2O 版本使用）")
    private Integer deliveryServiceFeeType;

    @Schema(description = "配送员服务费值：固定金额(元)或按比例(%)（仅 O2O 版本使用）")
    private BigDecimal deliveryServiceFeeValue;

    @Schema(description = "服务员服务费类型：0=固定金额 1=按比例（仅 O2O 上门服务订单使用）")
    private Integer serverServiceFeeType;

    @Schema(description = "服务员服务费值：固定金额(元)或按比例(%)（仅 O2O 上门服务订单使用）")
    private BigDecimal serverServiceFeeValue;

    @Schema(description = "供应商产品skuID")
    private Integer vendorProductSkuId;
}
