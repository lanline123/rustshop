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

import com.tigshop.bean.model.authority.Suppliers;
import com.tigshop.bean.model.setting.GalleryPic;
import com.tigshop.bean.model.setting.ShippingTpl;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import static com.tigshop.common.constant.product.ProductConstants.PRODUCT_NAME_IS_NULL;
import static com.tigshop.common.constant.product.ProductConstants.PRODUCT_NAME_OVER_LENGTH;

/**
 * 商品新增数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月04日 15:28
 */
@Data
@Schema(description = "商品新增参数")
public class ProductCreateDTO {
    @Schema(description = "商品类型")
    private Integer productType;

    @Schema(description = "商品库存")
    private Integer productStock;

    @Schema(description = "商品是否包邮")
    private Integer freeShipping;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

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

    @Schema(description = "商品详细描述")
    private String productDesc;

    @Schema(description = "相关产品")
    private List<Integer> productRelated;

    @Schema(description = "服务列表")
    private List<ServicesDTO> serviceList;

    @Schema(description = "供应商列表")
    private List<Suppliers> suppliersList;

    @Schema(description = "运费模板列表")
    private List<ShippingTpl> shippingTplList;

    @Schema(description = "产品列表")
    private List<ProductListAttributeDTO> productList;

    @Schema(description = "属性模板数据")
    private ProductAttrsDTO attrList;

    @Schema(description = "产品名称")
    @NotNull(message = PRODUCT_NAME_IS_NULL)
    @Size(max = 100, message = PRODUCT_NAME_OVER_LENGTH)
    private String productName;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "是否支持货到付款")
    private Boolean isSupportCod;

    @Schema(description = "产品服务ID列表")
    private List<Integer> productServiceIds;

    @Schema(description = "产品文章列表")
    private List<Integer> productArticleList;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "产品信息")
    private String productInfo;

    @Schema(description = "SEO标题")
    private String seoProductTitle;

    @Schema(description = "虚拟销量")
    private Integer virtualSales;

    @Schema(description = "限购数量")
    private Integer limitNumber;

    @Schema(description = "供应商ID")
    private Integer suppliersId;

    @Schema(description = "属性变更")
    private Boolean attrChanged;

    @Schema(description = "运费模板ID")
    private Long shippingTplId;

    @Schema(description = "品牌ID")
    private Integer brandId;

    @Schema(description = "产品条形码")
    private String productTsn;

    @Schema(description = "产品编号")
    private String productSn;

    @Schema(description = "产品简介")
    private String productBrief;

    @Schema(description = "市场价（划线价）")
    private BigDecimal marketPrice;

    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "关键词")
    private String keywords;

    @Schema(description = "是否删除：0否；1是")
    private Integer isDelete;

    @Schema(description = "是否精品：0否；1是")
    private Integer isBest;

    @Schema(description = "是否新品：0否；1是")
    private Integer isNew;

    @Schema(description = "是否热销：0否；1是")
    private Integer isHot;

    @Schema(description = "图片列表")
    private List<GalleryPic> imgList;
}
