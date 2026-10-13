package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 库存预警列表的数据库查询结果行，仅供服务层组装 {@link StockWarningPageVO} 使用
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "库存预警列表查询结果行")
public class StockWarningRowVO {

    private Integer productId;

    private Integer skuId;

    private String productName;

    private String productSkuName;

    private String productPicThumb;

    private String productSn;

    private String skuSn;

    private String productTsn;

    /** 商品归属主体，平台商品为 0 */
    private Integer productShopId;

    /** 门店分配记录所属主体，未分配时为空 */
    private Integer storeShopId;

    private Integer currentStock;

    /** 采购单已下单未入库的数量 */
    private Integer transitStock;

    /** 规则主键，为空表示该商品/SKU 未配置规则 */
    private Integer ruleId;

    private Integer warnEnabled;

    private Integer lowerLimit;

    private Integer upperLimit;

    private Long updateTime;

    /** 商品配置的相关商品原始文本，形如 [1, 2, 3] */
    private String productRelated;

    /** 由 SQL 计算的库存状态，口径与筛选条件保持一致 */
    private Integer stockStatus;
}
