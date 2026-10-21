// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.api.controller.product;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.*;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductPresale;
import com.tigshop.bean.model.promotion.PointsExchange;
import com.tigshop.bean.param.product.ProductSaveParam;
import com.tigshop.bean.query.product.CommentListPageQuery;
import com.tigshop.bean.query.user.FeedBackListPageQuery;
import com.tigshop.bean.vo.product.*;
import com.tigshop.bean.vo.promotion.CouponDetailVO;
import com.tigshop.bean.vo.user.UserFeedbackVO;
import com.tigshop.common.annotation.IgnoreResponseAdvice;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.service.product.CommentService;
import com.tigshop.service.product.ProductPresaleService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.promotion.PointsExchangeService;
import com.tigshop.service.user.FeedbackService;
import com.tigshop.service.wholesale.ProductAmountWholesaleEnhancer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 商品
 *
 * @author Tigshop团队
 * @create 2025年02月12日 14:59
 */
@RestController
@RequestMapping("/api/product/product")
@Tag(name = "商品")
public class ProductController {
    @Resource
    private ProductService productService;

    @Resource
    private PointsExchangeService pointsExchangeService;

    @Resource
    private CommentService productCommentService;

    @Resource
    private FeedbackService feedbackService;
    @Autowired
    private ProductPresaleService productPresaleService;

    @Autowired(required = false)
    private ProductAmountWholesaleEnhancer productAmountWholesaleEnhancer;

    @GetMapping("/detail")
    @Operation(summary = "商品详情")
    public ProductDetailVO detail(@RequestParam("sn") String productSn,
                                  @RequestParam(value = "shopId", required = false) Integer shopId,
                                  @RequestParam(value = "nearestShopId", required = false) Integer nearestShopId) {
        return productService.clientDetail(productSn, shopId, nearestShopId);
    }

    /**
     * 商品分享落地页
     * <p>
     * 返回带 Open Graph 标签的 HTML 供社交平台抓取预览，访客自动跳转到 H5 商品详情页。
     * 分销中心分享商品时可携带 salesmanId 用于分销归因。
     */
    @IgnoreResponseAdvice
    @GetMapping("/share")
    @Operation(summary = "商品分享落地页")
    public void sharePage(@RequestParam("id") Integer productId,
                          @RequestParam(value = "salesmanId", required = false) Integer salesmanId,
                          HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(productService.getSharePageHtml(productId, salesmanId));
    }

    @GetMapping(value = "/detail", params = {"id", "!sn"})
    @Operation(summary = "商品详情")
    public ProductDetailVO detailById(@RequestParam("id") Integer productId,
                                      @RequestParam(value = "shopId", required = false) Integer shopId,
                                      @RequestParam(value = "nearestShopId", required = false) Integer nearestShopId) {
        String productSn = productService.getProductSnById(productId);
        return productService.clientDetail(productSn, shopId, nearestShopId);
    }

    @GetMapping("/isCollect")
    @Operation(summary = "商品是否收藏")
    public boolean isCollectMethod(@RequestParam("id") int productId, @RequestParam(value = "shopId", required = false) Integer shopId) {
        //获取用户id
        //获取用户id
        String username = SecurityUtils.getCurrentUsername();
        if (ObjectUtil.isEmpty(username)) {
            return false;
        }
        return productService.isCollect(productId, shopId);
    }

    @GetMapping("/getCoupon")
    @Operation(summary = "获取商品优惠券")
    public List<CouponDetailVO> getCoupon(@RequestParam("id") int productId, @RequestParam(value = "shopId", required = false) Integer shopId) {
        return productService.getCouponList(productId, shopId);
    }

    @GetMapping("/getComment")
    @Operation(summary = "获取商品评论")
    public ProductCommentStatisticVO getComment(@RequestParam("id") Integer productId, @RequestParam(value = "shopId", required = false) Integer shopId) {
        return productCommentService.getProductCommentDetail(productId, shopId);
    }

    @GetMapping("/getCommentList")
    @Operation(summary = "获取商品评论列表")
    public Page<CommentVO> getCommentList(@RequestParam("id") Integer productId,
                                          @RequestParam("page") Integer page,
                                          @RequestParam("type") Integer type,
                                          @RequestParam(value = "shopId", required = false) Integer shopId) {
        CommentListPageQuery pageQuery = new CommentListPageQuery();
        pageQuery.setPage(page);
        pageQuery.setSize(10);
        pageQuery.setProductId(productId);
        pageQuery.setShopId(shopId);
        if (type == 5) {
            pageQuery.setIsShowed(1);
        }
        pageQuery.setType(type);
        pageQuery.setSortField("isTop");
        pageQuery.setSortOrder("desc");
        return productCommentService.listForClient(pageQuery);
    }

    @GetMapping("/getFeedbackList")
    @Operation(summary = "获取商品反馈列表")
    public Page<UserFeedbackVO> getFeedbackList(@RequestParam("productId") Integer productId,
                                                @RequestParam("page") Integer page,
                                                @RequestParam("size") Integer size,
                                                @RequestParam(value = "shopId", required = false) Integer shopId,
                                                @RequestParam(value = "sortField", required = false) String sortField,
                                                @RequestParam(value = "sortOrder", required = false) String sortOrder) {
        FeedBackListPageQuery feedBackListPageQuery = new FeedBackListPageQuery();
        feedBackListPageQuery.setPage(page);
        feedBackListPageQuery.setSize(size);
        feedBackListPageQuery.setProductId(productId);
        feedBackListPageQuery.setShopId(shopId);
        feedBackListPageQuery.setSortField(sortField);
        feedBackListPageQuery.setSortOrder(sortOrder);
        return feedbackService.listProductConsultations(feedBackListPageQuery);
    }

    @GetMapping("/getProductAvailability")
    @Operation(summary = "商品可用活动与数据")
    public ProductAvailabilityVO getProductAvailability(@RequestParam("id") Integer productId,
                                                        @RequestParam(name = "skuId", required = false) Integer skuId,
                                                        @RequestParam(name = "isExchange", required = false) Integer isExchange,
                                                        @RequestParam(name = "extraAttrIds", required = false) String extraAttrIds,
                                                        @RequestParam(value = "shopId", required = false) Integer shopId,
                                                        @RequestParam(value = "addressId", required = false) Integer addressId
    ) {
        if (isExchange == null) {
            isExchange = 0;
        }
        ProductAvailabilityVO productAvailabilityVo = productService.getProductSkuDetail(productId, skuId, isExchange, extraAttrIds, shopId, addressId, null);
        if (isExchange == 1) {
            PointsExchange infoByProductId = pointsExchangeService.getInfoByProductId(productAvailabilityVo.getProductId(), skuId);
            if (infoByProductId != null && infoByProductId.getPointsDeductedAmount().compareTo(BigDecimal.ZERO) > 0) {
                productAvailabilityVo.setPrice(TigUtils.toDecimalConvert(infoByProductId.getPointsDeductedAmount()));
            }
        }
        return productAvailabilityVo;
    }

    @PostMapping("/promotion")
    @Operation(summary = "商品活动")
    public Map<Integer, ProductSaveParam> getProductsPromotion(@RequestBody ProductsPromotionDTO productsPromotion) {
        //todo
        return productService.getProductsPromotion(productsPromotion.getProducts(), productsPromotion.getShopId(), productsPromotion.getPromotionFrom());
    }

    @PostMapping("/getProductAmount")
    @Operation(summary = "获取商品数量")
    public ProductAmountVO getProductAmount(@RequestBody ProductAmountDTO productAmountDTO) {
        Integer count = 0;
        BigDecimal total = BigDecimal.ZERO;
        // CR-20260604-1015：收集每行零售价，供 Enhancer 调 WholesalePriceResolver 用（避免二次查 SKU 详情）
        java.util.Map<Integer, BigDecimal> retailPriceBySkuId = new java.util.HashMap<>();
        // CR-20260613-1720 修复（折上折）：批发折扣基数统一用「实时零售价」startProductPrice
        // （有SKU=skuPrice / 无SKU=product.productPrice），而非含会员等级折扣的 getPrice()。
        // 原写法仅在「有SKU」时放 skuPrice，无SKU商品落空 → getOrDefault 回退到 getPrice()（会员折后价）
        // → 无SKU批发商品折上折。startProductPrice 两种情况都等于零售价，统一对所有 item 放入。
        java.util.Map<Integer, BigDecimal> wholesaleBasePriceBySkuId = new java.util.HashMap<>();
        for (ProductAmountDTO.SkuItem skuItem : productAmountDTO.getSkuItem()) {
            ProductAvailabilityVO productSkuDetail = productService.getProductSkuDetail(productAmountDTO.getId(), skuItem.getSkuId(), 0, null, productAmountDTO.getShopId(),null);
            count += skuItem.getNum();
            total = total.add(productSkuDetail.getPrice().multiply(new BigDecimal(skuItem.getNum())));
            retailPriceBySkuId.put(skuItem.getSkuId(), productSkuDetail.getPrice());
            if (productSkuDetail.getStartProductPrice() != null) {
                wholesaleBasePriceBySkuId.put(skuItem.getSkuId(), productSkuDetail.getStartProductPrice());
            }
        }
        ProductAmountVO vo = ProductAmountVO.builder()
                .count(count)
                .total(total)
                .build();
        // CR-20260604-1015：批发场景旁路覆写 total + 追加 wholesale/wholesaleSubtotal/hint
        // 非批发场景（未登录 / 非批发商 / 非平台商品 / 未配置批发）静默返回，vo 保持零售
        if (productAmountWholesaleEnhancer != null) {
            productAmountWholesaleEnhancer.enhance(vo, productAmountDTO, retailPriceBySkuId, wholesaleBasePriceBySkuId, SecurityUtils.getCurrentUserId());
        }
        return vo;
    }

    @PostMapping("/priceInquiry")
    @Operation(summary = "商品询价")
    public void priceInquiry(@RequestBody ProductPriceInquiryDTO productPriceInquiryDTO) {
        productService.priceInquiry(productPriceInquiryDTO);
    }

    @GetMapping("/list")
    @Operation(summary = "商品列表")
    public Page<ProductListResDTO> list(ProductListDTO productListDTO) {
        productListDTO.setIsDelete(0);
        productListDTO.setIsClient(1);
        productListDTO.setProductStatus(1);
        return productService.clientList(productListDTO);
    }

    @GetMapping("/getPresaleDepositAmount")
    @Operation(summary = "获取预售的定金价格")
    public BigDecimal getPresaleDepositAmount(@RequestParam(value = "productId",required = true) Integer productId,
                                              @RequestParam("skuId") Integer skuId) {
        return productPresaleService.calculateDepositAmount(productId,skuId,
                productPresaleService.getByProductId(productId));
    }


    @GetMapping("/getRelated")
    @Operation(summary = "获取相关商品")
    public List<ProductListResDTO> getRelated(@RequestParam("productId") Integer productId) {
        Page<ProductListResDTO> relatedList = productService.getRelatedProductList(productId);
        return relatedList != null ? relatedList.getRecords() : new ArrayList<>();
    }

    @GetMapping("/getBatchProductAvailability")
    @Operation(summary = "批量获取商品可用活动与数据")
    public Map<Integer, ProductAvailabilityVO> getBatchProductAvailability(@RequestParam("skuIds") String skuIds, @RequestParam(name = "shopId", required = false) Integer shopId) {
        return productService.getBatchProductAvailability(skuIds, shopId);
    }

    @GetMapping("/getHistoryProduct")
    @Operation(summary = "获取当前用户历史购买过的商品信息")
    public Page<ProductListResDTO> getHistoryProduct(BasePage basePage) {
        return productService.getHistoryProduct(basePage);
    }
}
