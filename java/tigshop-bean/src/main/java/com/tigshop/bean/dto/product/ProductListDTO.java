package com.tigshop.bean.dto.product;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品列表入参
 *
 * @author Jayce
 * @create 2024年11月20日 15:10
 */
@Getter
@Setter
public class ProductListDTO extends BasePage {

    @Schema(description = "是否筛选预约商品 1 预约商品，其他值筛选全部")
    private Integer reservationProduct;

    @Schema(description = "介绍类型")
    private String introType;

    @Schema(description = "是否删除，0-未删除，1-已删除，-1-全部")
    private Integer isDelete;

    @Schema(description = "店铺ID，-1表示所有店铺，-2表示其他特定店铺")
    private Integer shopId;

    @Schema(description = "产品状态，1-正常，0-下架")
    private Integer productStatus;

    @Schema(description = "是否仅仅展示自己的门店的商品(平台分配的也是)")
    private Boolean onlyShowMe;

    @Schema(description = "审核状态，1-审核通过，0-未审核 2 审核未通过")
    private Integer checkStatus;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "是否过滤预售商品")
    private Integer isFilterPresaleProduct;

    @Schema(description = "商品类型 1普通商品 2虚拟商品 3卡密商品 4付费内容 5上门服务 6到店服务")
    private Integer productType;

    @Schema(description = "商品类型列表，支持多个类型过滤，为空则查询所有")
    private List<Integer> productTypes;

    @Schema(description = "排除的商品类型（多选），如秒杀选品排除预约上门(5)、预约到店(6)")
    private List<Integer> excludeProductTypes;

    @Schema(description = "是否查所有店铺商品")
    private Integer searchShop = 0;

    @Schema(description = "商品分类ID")
    private Integer categoryId;

    @Schema(description = "品牌ID")
    private Integer brandId;

    @Schema(description = "是否是抽奖活动")
    private Integer isLottery;

    @Schema(description = "商品组ID")
    private Integer productGroupId;

    @Schema(description = "店铺分类ID")
    private Integer shopCategoryId;

    @Schema(description = "商品id组 (默认为 null)")
    private String ids;

    @Schema(description = "商品id list组 (默认为 null)")
    private List<Integer> idList;

    @Schema(description = "不包含的商品ids")
    private List<Integer> extraProductIds;

    @Schema(description = "品牌ids")
    private String brandIds;

    @Schema(description = "优惠券id")
    private Integer couponId;

    @Schema(description = "价格区间")
    private BigDecimal minPrice;

    @Schema(description = "价格区间")
    private BigDecimal maxPrice;

    @Schema(description = "销量区间-最低销量（按累计销量/虚拟销量 virtual_sales 过滤）")
    private Integer minSales;

    @Schema(description = "销量区间-最高销量（按累计销量/虚拟销量 virtual_sales 过滤）")
    private Integer maxSales;

    @Schema(description = "商品创建时间下限，秒级时间戳")
    private Long addedStartTime;

    @Schema(description = "商品创建时间上限，秒级时间戳")
    private Long addedEndTime;

    @Schema(description = "库存区间-最低库存（按商品总库存 product_stock 过滤）")
    private Integer minStock;

    @Schema(description = "库存区间-最高库存（按商品总库存 product_stock 过滤），用于筛选临近缺货的商品")
    private Integer maxStock;

    @Schema(description = "是否最新")
    private Integer isNew;

    @Schema(description = "供应商id")
    private Integer suppliersId;

    @Schema(description = "是否展示供应商商品：0-不展示，1-展示")
    private Integer showVendorProduct;

    @Schema(description = "是否显示，-1-全部，0-不显示，1-显示")
    private Integer isShow;

    @Schema(description = "排序字段")
    private String sortField;

    @Schema(description = "排序顺序，asc-升序，desc-降序")
    private String sortOrder;

    @Schema(description = "是否按累计销量排序，true-启用（按 virtual_sales 排序，方向取 sortOrder，默认降序）")
    private Boolean sortByTotalSales;

    @Schema(description = "是否按30日销量排序，true-启用（口径复用 virtual_sales，方向取 sortOrder，默认降序）")
    private Boolean sortByThirtyDaySales;

    @Schema(description = "是否包含购物车数量，0-不包含，1-包含")
    private Integer withCartSum;

    @Schema(description = "用于前台搜索，sale|price|time")
    private String sort;

    @Schema(description = "用于后台端还是客户端（0：后台，1：客户端）")
    private Integer isClient;

    @Schema(description = "用于前台搜索，1-出售的商品，2-待审核的商品")
    private Integer type;

    @Schema(description = "销售模式：0-现货销售，1-全款预售，2-定金预售")
    private Integer saleMode;

    @Schema(description = "属性查询")
    private List<ProductSearchClientDTO.AttrObj> attrs;

    @Schema(description = "收银台商品来源过滤：0-全部商品，1-门店自建商品，2-平台分配商品，3-供应商商品")
    private List<Integer> posProductSource;

    @Schema(description = "是否排除周期购商品")
    private Boolean excludeCycleProduct;

    @Schema(description = "是否仅排除按计划配送的周期购，保留次卡（按需兑换）商品")
    private Boolean excludePlanDeliveryCycleOnly;

    @Schema(description = "是否排除礼品卡商品")
    private Boolean excludeGiftCardProduct;

    @Schema(description = "配送类型过滤（多选，OR 关系）：1-到店自提 2-上门服务 3-预约到店 4-同城配送 5-快递配送")
    private List<Integer> deliveryTypes;

}
