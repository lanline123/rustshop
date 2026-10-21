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

import cn.hutool.json.JSONArray;
import com.tigshop.bean.model.o2o.ShopPickupTpl;
import com.tigshop.bean.model.product.ProductGallery;
import com.tigshop.bean.model.product.ProductVideo;
import com.tigshop.bean.vo.product.ProductPresaleVO;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情参数
 *
 * @author Tigshop团队
 * @create 2024年11月26日 16:38
 */
@Data
@Schema(description = "商品详情参数")
public class ProductDetailDTO {
    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品名称")
    @JsonTranslate(dataType = 2)
    private String productName;

    @Schema(description = "SEO标识")
    private String seoIdentification;

    @Schema(description = "商品货号")
    private String productSn;

    @Schema(description = "商品条形码")
    private String productTsn;

    @Schema(description = "商品库存")
    private Integer productStock;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "商品市场价")
    private BigDecimal marketPrice;

    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "运费模板id")
    private Long shippingTplId;

    @Schema(description = "商品状态")
    private Integer productStatus;

    @Schema(description = "商品类型")
    private Integer productType;

    @Schema(description = "销售模式：0-现货销售，1-全款预售，2-定金预售")
    private Integer saleMode;

    @Schema(description = "销售模式：0-现货销售，1 预售")
    private Integer productSaleMode;

    @Schema(description = "预售信息（仅预售商品有）")
    private ProductPresaleVO presaleInfo;

    @Schema(description = "商品分类ID")
    private Integer categoryId;

    @Schema(description = "商品品牌ID")
    private Integer brandId;

    @Schema(description = "商品店铺ID")
    private Integer shopId;

    @Schema(description = "商品关键词")
    private String keywords;

    @Schema(description = "商品店铺分类ID")
    private Integer shopCategoryId;

    @Schema(description = "商品审核状态")
    private Integer checkStatus;

    @Schema(description = "商品审核原因")
    private String checkReason;

    @Schema(description = "商品点击量")
    private Integer clickCount;

    @Schema(description = "商品重量")
    private BigDecimal productWeight;

    @Schema(description = "商品是否促销")
    private Integer isPromote;

    @Schema(description = "商品是否活动")
    private Integer isPromoteActivity;

    @Schema(description = "商品促销价格")
    private BigDecimal promotePrice;

    @Schema(description = "商品促销开始时间")
    private String promoteStartDate;

    @Schema(description = "商品促销结束时间")
    private String promoteEndDate;

    @Schema(description = "商品秒杀商品数量")
    private Integer seckillMaxNum;

    @Schema(description = "商品秒杀库存")
    private String productBrief;

    @Schema(description = "商品描述")
    private String productDesc;

    @Schema(description = "商品图片")
    private String picUrl;

    @Schema(description = "商品缩略图")
    private String picThumb;

    @Schema(description = "商品原图")
    private String picOriginal;

    @Schema(description = "大图")
    private String picLarge;

    @Schema(description = "商品服务")
    private List<Integer> productServiceIds;

    @Schema(description = "商品是否包邮")
    private Integer freeShipping;

    @Schema(description = "商品积分")
    private Integer integral;

    @Schema(description = "商品添加时间")
    private long addTime;

    @Schema(description = "商品排序")
    private Integer sortOrder;

    @Schema(description = "商品店铺排序")
    private Integer storeSortOrder;

    @Schema(description = "商品是否删除")
    private Integer isDelete;

    @Schema(description = "商品是否精品")
    private Integer isBest;

    @Schema(description = "商品是否新品")
    private Integer isNew;

    @Schema(description = "商品是否热销")
    private Integer isHot;

    @Schema(description = "商品最后更新时间")
    private Integer lastUpdate;

    @Schema(description = "商品备注")
    private String remark;

    @Schema(description = "商品赠送积分")
    private Integer giveIntegral;

    @Schema(description = "商品评价积分")
    private Integer rankIntegral;

    @Schema(description = "商品供应商ID")
    private Integer suppliersId;

    @Schema(description = "商品虚拟销量")
    private Integer virtualSales;

    @Schema(description = "商品限购数量")
    private Integer limitNumber;

    @Schema(description = "商品收藏数")
    private String productCare;

    @Schema(description = "商品关联商品")
    private List<Integer> productRelated;

    @Schema(description = "商品是否支持退货")
    private Integer isSupportReturn;

    @Schema(description = "商品是否支持货到付款")
    private Integer isSupportCod;

    @Schema(description = "商品视频")
    private String productVideo;

    @Schema(description = "商品预存款支付价格")
    private BigDecimal prepayPrice;

    @Schema(description = "商品描述")
    private List<ProductDescDTO> productDescArr;

    @Schema(description = "商品相册")
    private List<ProductGallery> imgList;

    @Schema(description = "视频信息")
    private List<ProductVideo> productVideoInfo;

    @Schema(description = "商品文章")
    private List<Integer> productArticleList;

    @Schema(description = "属性模板数据")
    private ProductAttrsDTO attrList;

    @Schema(description = "商品SKU")
    private List<ProductSkuDTO> productList;

    @Schema(description = "商品SKU总数（前端据此决定是否切换分页编辑模式；单规格通常为0）")
    private Integer skuCount;

    @Schema(description = "规格类型：1-统一规格，2-多规格")
    private Integer skuType;

    @Schema(description = "卡密组")
    private Integer cardGroupId;

    @Schema(description = "虚拟商品")
    private String virtualSample;

    @Schema(description = "付费内容")
    private JSONArray paidContent;

    @Schema(description = "是否无需配送；0-否，1-是")
    private Integer noShipping;

    @Schema(description = "运费类型")
    private Integer fixedShippingType;

    @Schema(description = "固定运费金额")
    private BigDecimal fixedShippingFee;

    @Schema(description = "是否到店自提；0-否，1-是")
    private Integer isShopPickup;

    @Schema(description = "到店自提模板")
    private ShopPickupTpl shopPickupTpl;

    @Schema(description = "供应商产品ID")
    private Integer vendorProductId;

    @Schema(description = "供应商ID")
    private Integer vendorId;

    @Schema(description = "是否物流配送0-否，1-是")
    private Integer isLogistics;

    @Schema(description = "是否门店配送0-否，1-是")
    private Integer isShopDelivery;

    @Schema(description = "预约模板Id")
    private Integer reservationTplId;

    @Schema(description = "预约模板Id")
    private List<ReservationTime> reservationTimes;

    @Schema(description = "礼品卡组id")
    private Integer giftCardGroupsId;

    @Schema(description = "商品单位")
    private String productUnit;

    @Schema(description = "属性模板ID")
    private Integer attributesTplId;

    @Schema(description = "自动上架时间（格式：yyyy-MM-dd HH:mm:ss）")
    private String autoOnlineTime;

    @Schema(description = "自动下架时间（格式：yyyy-MM-dd HH:mm:ss）")
    private String autoOfflineTime;

    @Schema(description = "自动上架任务ID")
    private Integer autoOnlineTaskId;

    @Schema(description = "自动下架任务ID")
    private Integer autoOfflineTaskId;

    @Schema(description = "自动上架任务状态：0-待执行，1-执行成功，2-执行失败")
    private Integer autoOnlineTaskStatus;

    @Schema(description = "自动下架任务状态：0-待执行，1-执行成功，2-执行失败")
    private Integer autoOfflineTaskStatus;

    @Schema(description = "自动上下架错误信息")
    private String autoTaskErrorMsg;

    @Schema(description = "是否开启定时上下架：0-否，1-是（有自动上架或下架时间即为1）")
    private Integer timedOffShelf;

    // ========== 周期购相关字段 ==========
    @Schema(description = "交付模式：1-单次交付（默认），2-多次交付（周期购）")
    private Integer deliveryMode;


    // ========== O2O 服务费上下文（CR-20260513-160000）：仅 detail 接口返回，前端编辑表单使用 ==========

    @Schema(description = "[O2O] 配送员服务费总开关：0=关 1=开（来自 ConfigService.getSettings）")
    private Integer deliveryServiceFeeEnabled;

    @Schema(description = "[O2O] 服务员服务费总开关：0=关 1=开")
    private Integer serverServiceFeeEnabled;

    @Schema(description = "[O2O] 门店独立配送员服务费模式：0=总部统一 1=门店独立")
    private Integer storeAssignDeliveryServiceFee;

    @Schema(description = "[O2O] 门店独立服务员服务费模式：0=总部统一 1=门店独立")
    private Integer storeAssignServerServiceFee;

    @Schema(description = "[O2O] 当前角色 + 配置下，配送员服务费是否可编辑（综合开关后的结论）")
    private Boolean deliveryFeeEditable;

    @Schema(description = "[O2O] 当前角色 + 配置下，服务员服务费是否可编辑（综合开关后的结论）")
    private Boolean serverFeeEditable;

    // ========== 单规格商品服务费（CR-20260514-110000，仅 skuType=1 商品填充；多规格商品请从 productList[] 取） ==========

    @Schema(description = "[O2O 5.8.32] 平台层配送员服务费类型（仅单规格商品填充，来自 product 表）")
    private Integer platformDeliveryServiceFeeType;

    @Schema(description = "[O2O 5.8.32] 平台层配送员服务费值（仅单规格商品填充，来自 product 表）")
    private BigDecimal platformDeliveryServiceFeeValue;

    @Schema(description = "[O2O 5.8.32] 平台层服务员服务费类型（仅单规格商品填充，来自 product 表）")
    private Integer platformServerServiceFeeType;

    @Schema(description = "[O2O 5.8.32] 平台层服务员服务费值（仅单规格商品填充，来自 product 表）")
    private BigDecimal platformServerServiceFeeValue;

    @Schema(description = "[O2O 5.8.32] 门店覆盖配送员服务费类型（仅单规格 + 门店角色看分配商品时填充，来自 store_product 表）")
    private Integer storeDeliveryServiceFeeType;

    @Schema(description = "[O2O 5.8.32] 门店覆盖配送员服务费值（仅单规格 + 门店角色看分配商品时填充，来自 store_product 表）")
    private BigDecimal storeDeliveryServiceFeeValue;

    @Schema(description = "[O2O 5.8.32] 门店覆盖服务员服务费类型（仅单规格 + 门店角色看分配商品时填充，来自 store_product 表）")
    private Integer storeServerServiceFeeType;

    @Schema(description = "[O2O 5.8.32] 门店覆盖服务员服务费值（仅单规格 + 门店角色看分配商品时填充，来自 store_product 表）")
    private BigDecimal storeServerServiceFeeValue;

    @Data
    @Schema(description = "预约时间段")
    public static class ReservationTime {
        /**
         * 主键ID
         */
        private Integer productReservationTimeId;
        /**
         * 开始
         */
        @Schema(description = "开始，格式：HH:mm")
        private String startTime;

        /**
         * 结束
         */
        @Schema(description = "结束，格式：HH:mm")
        private String endTime;
    }
}
