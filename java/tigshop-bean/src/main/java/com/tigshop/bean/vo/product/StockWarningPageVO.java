package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 库存预警列表项
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "库存预警列表项")
public class StockWarningPageVO {

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "SKU ID；0=单规格商品")
    private Integer skuId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "规格")
    private String productSkuName;

    @Schema(description = "商品图片")
    private String productPicThumb;

    @Schema(description = "商品编码")
    private String productSn;

    @Schema(description = "SKU编码")
    private String skuSn;

    @Schema(description = "条形码")
    private String productTsn;

    @Schema(description = "归属主体ID：0=平台自营")
    private Integer shopId;

    @Schema(description = "归属主体名称（自营门店名 / 店铺名 / 门店名）")
    private String shopName;

    @Schema(description = "归属主体类型：1=自营，2=店铺，3=门店")
    private Integer ownerType;

    @Schema(description = "归属主体类型名称")
    private String ownerTypeName;

    @Schema(description = "当前库存（按主体口径）")
    private Integer currentStock;

    @Schema(description = "在途库存（采购库存）：采购单已下单未入库的数量，无采购单时为 0")
    private Integer transitStock;

    @Schema(description = "库存下限；未配置时为空")
    private Integer lowerLimit;

    @Schema(description = "库存上限；未配置时为空")
    private Integer upperLimit;

    @Schema(description = "库存状态：0=正常库存，1=低库存，2=高库存，3=无货，9=未配置")
    private Integer stockStatus;

    @Schema(description = "库存状态名称")
    private String stockStatusName;

    @Schema(description = "预警开关：0=关闭，1=开启；未配置时为空")
    private Integer warnEnabled;

    @Schema(description = "规则更新时间")
    private Long updateTime;

    @Schema(description = "相关商品ID列表，供跳转商品列表时作为 ids 参数使用")
    private List<Integer> relatedProductIds;

    @Schema(description = "相关商品数量")
    private Integer relatedProductCount;
}
