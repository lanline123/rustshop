// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.product;

import cn.hutool.json.JSONArray;
import com.tigshop.bean.dto.product.ProductAttrsDTO;
import com.tigshop.bean.dto.product.ProductDescDTO;
import com.tigshop.bean.dto.product.ProductListAttributeDTO;
import com.tigshop.bean.dto.product.ProductPresaleDTO;
import com.tigshop.bean.dto.product.ServicesDTO;
import com.tigshop.bean.model.authority.Suppliers;
import com.tigshop.bean.model.product.ProductVideo;
import com.tigshop.bean.model.setting.GalleryPic;
import com.tigshop.bean.model.setting.ShippingTpl;
import com.tigshop.bean.vo.promotion.PromotionVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import static com.tigshop.common.constant.product.ProductConstants.PRODUCT_NAME_IS_NULL;
import static com.tigshop.common.constant.product.ProductConstants.PRODUCT_NAME_OVER_LENGTH;
import static com.tigshop.common.constant.product.ProductConstants.PRODUCT_STOCK_MAX_LIMIT;

/**
 * 商品更新数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月04日 16:37
 */
@Data
@Schema(description = "商品更新参数")
public class ProductSaveParam {

    @Schema(description = "商品类型")
    private Integer productType;

    @Schema(description = "销售模式：0-现货销售，1-全款预售，2-定金预售")
    private Integer saleMode;

    @Schema(description = "预售配置信息（当saleMode>0时必填）")
    private ProductPresaleDTO presaleConfig;

    @Schema(description = "交付模式：1-单次交付，2-多次交付（周期购）")
    private Integer deliveryMode;


    @Max(value = PRODUCT_STOCK_MAX_LIMIT, message = "商品库存超出限制")
    @Schema(description = "商品库存")
    private Integer productStock;

    @Schema(description = "商品是否包邮")
    private Integer freeShipping;

    @Schema(description = "商品店铺分类ID")
    private Integer shopCategoryId;

    @Schema(description = "产品状态")
    private Integer productStatus;

    @Schema(description = "赠送积分")
    private Integer giveIntegral;

    @Schema(description = "排名积分")
    private Integer rankIntegral;

    @Schema(description = "积分")
    private Integer integral;

    @Schema(description = "产品重量")
    private BigDecimal productWeight;

    @Schema(description = "产品描述数组")
    private List<ProductDescDTO> productDescArr;

    @Schema(description = "相关产品")
    private List<Integer> productRelated;

    @Schema(description = "服务列表")
    private List<ServicesDTO> serviceList;

    @Schema(description = "产品ID")
    private Integer productId;

    @Schema(description = "产品名称")
    @NotBlank(message = PRODUCT_NAME_IS_NULL)
    @Size(max = 200, message = PRODUCT_NAME_OVER_LENGTH)
    private String productName;

    @Schema(description = "SEO标识")
    private String seoIdentification;

    @Schema(description = "产品编号")
    private String productSn;

    @Schema(description = "产品条形码")
    private String productTsn;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "市场价（划线价）")
    private BigDecimal marketPrice;

    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "运费模板ID")
    private Long shippingTplId;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "品牌ID")
    private Integer brandId;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "关键词")
    private String keywords;

    @Schema(description = "审核状态")
    private Integer checkStatus;

    @Schema(description = "审核原因")
    private String checkReason;

    @Schema(description = "点击次数")
    private Integer clickCount;

    @Schema(description = "是否促销")
    private Boolean isPromote;

    @Schema(description = "是否参与促销活动")
    private Boolean isPromoteActivity;

    @Schema(description = "促销价格")
    private BigDecimal promotePrice;

    @Schema(description = "促销开始日期")
    private String promoteStartDate;

    @Schema(description = "促销结束日期")
    private String promoteEndDate;

    @Schema(description = "秒杀最大数量")
    private Integer seckillMaxNum;

    @Schema(description = "产品简介")
    private String productBrief;

    @Schema(description = "产品描述")
    private String productDesc;

    @Schema(description = "图片URL")
    private String picUrl;

    @Schema(description = "缩略图URL")
    private String picThumb;

    @Schema(description = "原图URL")
    private String picOriginal;


    @Schema(description = "评论标签")
    private String commentTag;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "排序顺序")
    private Integer sortOrder;

    @Schema(description = "店铺排序顺序")
    private Integer storeSortOrder;

    @Schema(description = "是否删除")
    private Integer isDelete;

    @Schema(description = "是否精品")
    private Integer isBest;

    @Schema(description = "是否新品")
    private Integer isNew;

    @Schema(description = "是否热销")
    private Integer isHot;

    @Schema(description = "运费类型")
    private Integer fixedShippingType;

    @Schema(description = "固定运费金额")
    private BigDecimal fixedShippingFee;

    @Schema(description = "最后更新时间")
    private String lastUpdate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "供应商ID")
    private Integer suppliersId;

    @Schema(description = "虚拟销量")
    private Integer virtualSales;

    @Schema(description = "限购数量")
    private Integer limitNumber;

    @Schema(description = "关注产品")
    private String productCare;

    @Schema(description = "产品服务ID列表")
    private List<Integer> productServiceIds;

    @Schema(description = "是否支持退货")
    private Integer isSupportReturn;

    @Schema(description = "是否支持货到付款")
    private Integer isSupportCod;

    @Schema(description = "产品视频")
    private String productVideo;

    @Schema(description = "预付价格")
    private BigDecimal prepayPrice;

    @Schema(description = "卡组ID")
    private Integer cardGroupId;

    @Schema(description = "虚拟样品")
    private String virtualSample;

    @Schema(description = "付费内容")
    private JSONArray paidContent;

    @Schema(description = "是否无需配送；0-否，1-是")
    private Integer noShipping;

    @NotEmpty(message = "商品图片不能为空")
    @Schema(description = "图片列表")
    private List<GalleryPic> imgList;

    @Schema(description = "产品视频")
    private List<ProductVideo> productVideoInfo;

    @Schema(description = "产品重量（单位）")
    private BigDecimal productWeightByUnit;

    @Schema(description = "产品文章列表")
    private List<Integer> productArticleList;

    @Schema(description = "属性模板数据")
    private ProductAttrsDTO attrList;

    @Schema(description = "产品列表")
    private List<ProductListAttributeDTO> productList;

    @Schema(description = "规格类型：1-统一规格，2-多规格")
    private Integer skuType;

    @Schema(description = "[O2O 5.8.32] 配送员服务费类型（仅单规格商品 skuType=1 使用，多规格请用 productList[].deliveryServiceFeeType）：0=固定金额 1=按比例")
    @Min(value = 0, message = "配送员服务费类型必须为 0(固定金额) 或 1(按比例)")
    @Max(value = 1, message = "配送员服务费类型必须为 0(固定金额) 或 1(按比例)")
    private Integer deliveryServiceFeeType;

    @Schema(description = "[O2O 5.8.32] 配送员服务费值（仅单规格商品 skuType=1 使用）：固定金额(元)或按比例(%)")
    @DecimalMin(value = "0.00", message = "配送员服务费值不能小于 0")
    private BigDecimal deliveryServiceFeeValue;

    @Schema(description = "[O2O 5.8.32] 服务员服务费类型（仅单规格商品 skuType=1 使用，多规格请用 productList[].serverServiceFeeType）：0=固定金额 1=按比例")
    @Min(value = 0, message = "服务员服务费类型必须为 0(固定金额) 或 1(按比例)")
    @Max(value = 1, message = "服务员服务费类型必须为 0(固定金额) 或 1(按比例)")
    private Integer serverServiceFeeType;

    @Schema(description = "[O2O 5.8.32] 服务员服务费值（仅单规格商品 skuType=1 使用）：固定金额(元)或按比例(%)")
    @DecimalMin(value = "0.00", message = "服务员服务费值不能小于 0")
    private BigDecimal serverServiceFeeValue;

    @Schema(description = "错误码")
    private Integer errcode;

    @Schema(description = "消息")
    private String message;

    @Schema(description = "供应商列表")
    private List<Suppliers> suppliersList;

    @Schema(description = "运费模板列表")
    private List<ShippingTpl> shippingTplList;

    @Schema(description = "产品信息")
    private String productInfo;

    @Schema(description = "SEO标题")
    private String seoProductTitle;

    @Schema(description = "属性变更")
    private Boolean attrChanged;

    @Schema(description = "购物车id")
    private Integer cartId;

    @Schema(description = "skuId")
    private Integer skuId;

    @Schema(description = "活动详情,优惠计算专用")
    private List<PromotionVO> activityInfo;

    @Schema(description = "是否选中，优惠计算专用，非商品数据")
    private Boolean isChecked;

//    @NotNull(message = "是否到店自提不能为空")
    @Schema(description = "是否到店自提；0-否，1-是")
    private Integer isShopPickup;

    @Schema(description = "是否物流配送0-否，1-是")
    private Integer isLogistics;

    @Schema(description = "是否门店配送0-否，1-是")
    private Integer isShopDelivery;

    @Schema(description = "到店自提模板ID")
    private Long shopPickupTplId;

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

    @Data
    @Schema(description = "预约时间段")
    public static class ReservationTime{
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
