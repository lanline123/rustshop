package com.tigshop.bean.dto.product;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 库存预警列表查询参数
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "库存预警列表查询参数")
public class StockWarningListDTO extends BasePage {

    /**
     * 排序字段：currentStock（实物库存，兼容 physicalStock/productStock）、transitStock（在途库存）、lowerLimit（库存下限）、upperLimit（库存上限）。
     * 非白名单值回退按商品 ID 倒序。
     */
    @Override
    @Schema(description = "排序字段：currentStock/physicalStock/productStock（实物库存）、transitStock（在途库存）、lowerLimit（库存下限）、upperLimit（库存上限）；非法值回退商品ID倒序")
    public String getSortField() {
        return super.getSortField();
    }

    /**
     * 排序方向：asc 升序，其他值按 desc 降序。
     */
    @Override
    @Schema(description = "排序方向：asc 升序，其他值按 desc 降序")
    public String getSortOrder() {
        return super.getSortOrder();
    }

    @Schema(description = "归属主体：0=平台自营，>0=店铺或门店；平台可指定，店铺/门店强制为自身")
    private Integer shopId;

    @Schema(description = "库存状态筛选：不传=全部，0=正常库存，1=低库存，2=高库存，3=无货，9=未配置")
    private Integer stockStatus;

    @Schema(description = "预警开关筛选：不传=全部，0=已关闭，1=已开启")
    private Integer warnEnabled;

    @Schema(description = "商品类型")
    private Integer productType;

    /** 由服务层按主体与门店独立库存配置计算，不接受外部传入 */
    @Schema(description = "是否按门店独立库存口径取数", hidden = true)
    private Boolean useStoreStock;

    /** 门店独立库存与总库存同时启用时，可售库存取两者较小值 */
    @Schema(description = "是否同时启用总库存", hidden = true)
    private Boolean useTotalStock;

    /** 导出按勾选行取数时由服务层解析填充，为空表示不限定具体行 */
    @Schema(description = "限定的商品/SKU 行", hidden = true)
    private List<Target> targets;

    /**
     * 列表的一行：单规格商品 skuId 为 0
     */
    @Data
    @Schema(description = "商品/SKU 行标识")
    public static class Target {

        @Schema(description = "商品ID")
        private Integer productId;

        @Schema(description = "SKU ID，单规格商品为 0")
        private Integer skuId;

        public Target() {
        }

        public Target(Integer productId, Integer skuId) {
            this.productId = productId;
            this.skuId = skuId;
        }
    }
}
