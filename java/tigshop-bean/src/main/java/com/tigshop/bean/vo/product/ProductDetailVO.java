// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.product;

import com.tigshop.bean.dto.product.ProductAttrsDTO;
import com.tigshop.bean.dto.product.ProductDescDTO;
import com.tigshop.bean.dto.product.ProductInfoDTO;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.dto.promotion.SeckillItemInfoDTO;
import com.tigshop.bean.model.product.ProductGallery;
import com.tigshop.bean.model.product.ProductVideo;
import com.tigshop.bean.model.product.Services;
import com.tigshop.bean.vo.promotion.GrouponItemInfoVO;
import com.tigshop.bean.vo.promotion.PointsExchangeVO;
import com.tigshop.bean.vo.wholesale.WholesalePriceTierVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 前台商品详情
 *
 * @author Tigshop团队
 * @create 2025年02月12日 13:43
 */
@Setter
@Getter
public class ProductDetailVO {
    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "商品详情")
    private ProductInfoDTO item;

    @Schema(description = "商品图文详情")
    private List<ProductDescDTO> descArr;

    @Schema(description = "sku列表")
    private List<ProductSkuDTO> skuList;

    @Schema(description = "相册列表")
    private List<ProductGallery> picList;

    @Schema(description = "视频信息")
    private List<ProductVideo> videoList;

    @Schema(description = "商品属性列表")
    private ProductAttrsDTO attrList;

    @Schema(description = "商品评论评分详情")
    private ProductCommentStatisticVO rankDetail;

    @Schema(description = "秒杀活动详情")
    private List<SeckillItemInfoDTO> seckillDetail;

    @Schema(description = "拼团活动商品详情")
    private GrouponItemInfoVO grouponDetail;

    @Schema(description = "服务列表")
    private List<Services> serviceList;

    @Schema(description = "默认选择的属性")
    private List<String> checkedValue;

    @Schema(description = "商品咨询总数")
    private Integer consultationTotal;

    @Schema(description = "积分商品信息")
    private PointsExchangeVO exchangeInfo;

    // *** 周期购相关字段 ***
    @Schema(description = "交付模式：1-单次交付，2-多次交付（周期购）")
    private Integer deliveryMode;

    @Schema(description = "交付模式文本：普通商品 / 周期购")
    private String deliveryModeText;


    @Schema(description = "批发阶梯价表（仅批发商身份返回全等级阶梯，按 price_id ASC 排序；非批发商为 null）")
    private List<WholesalePriceTierVO> wholesalePriceList;

    @Schema(description = "批发多规格定价模式（CR-20260604-1430：1=商品级混批，按全规格总数匹配阶梯；2=SKU 级独立。非批发商/未配置为 null）")
    private Integer wholesalePriceMode;
}
