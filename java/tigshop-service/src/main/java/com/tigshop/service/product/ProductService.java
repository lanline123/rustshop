// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.service.product;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.UpdateFieldProductDTO;
import com.tigshop.bean.dto.order.CategoryParentTreeDTO;
import com.tigshop.bean.dto.product.*;
import com.tigshop.bean.enums.common.shareTypeEnum;
import com.tigshop.bean.enums.promotion.PromotionType;
import com.tigshop.bean.model.cart.Cart;
import com.tigshop.bean.model.product.Brand;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.promotion.GrouponPromotion;
import com.tigshop.bean.model.setting.GalleryPic;
import com.tigshop.bean.param.product.ProductEditParam;
import com.tigshop.bean.param.product.ProductSaveParam;
import com.tigshop.bean.query.product.ProductSkuPageQuery;
import com.tigshop.bean.query.promotion.groupon.GrouponProductPageQuery;
import com.tigshop.bean.query.salesman.SalesmanProductPageQuery;
import com.tigshop.bean.vo.content.ArticleVO;
import com.tigshop.bean.vo.o2o.DeliveryOptionVO;
import com.tigshop.bean.vo.product.*;
import com.tigshop.bean.vo.promotion.CouponDetailVO;
import com.tigshop.bean.vo.promotion.GrouponPostVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.bean.vo.salesman.SalesmanProductListVO;
import com.tigshop.bean.vo.user.UserAddressVO;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.common.BaseService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 商品服务接口
 *
 * @author Tigshop团队
 * @create 2024年11月20日 11:09
 */
public interface ProductService extends BaseService<Product> {
    /**
     * 商品列表
     *
     * @param product 查询条件
     * @return ProductListVO
     */
    Page<ProductListResDTO> list(ProductListDTO product);


    /**
     * 商品列表
     *
     * @param product 查询条件
     * @return ProductListVO
     */
    Page<ProductListResDTO> clientList(ProductListDTO product);

    /**
     * 批量商品列表：多次列表查询共享同一整页优化上下文，避免促销、赠品、用户和配置等
     * 与门店无关的数据被逐次重复查询。调用方只消费 records，因此批量入口不执行分页 COUNT，
     * Page.total 不作为有效返回值。
     *
     * @param products 查询条件列表
     * @return 与入参顺序一一对应的分页结果
     */
    List<Page<ProductListResDTO>> clientListBatch(List<ProductListDTO> products);

    /**
     * 待审核商品数量
     *
     * @return long
     */
    Long getWaitingCheckedCount();

    /**
     * 商品详情
     *
     * @param productId 商品ID
     * @return ItemVO<ProductDetailDTO>
     */
    ProductDetailDTO detail(Integer productId);

    /**
     * 获取商品描述中的图片列表（供后台配置描述图本地化，dataType=7）
     *
     * @param productId 商品ID
     * @return 描述图 / 视频封面 URL 列表
     */
    List<ProductDescImageVO> getDescImages(Integer productId);

    /**
     * 商品配置
     */
    ProductConfigVO config(Integer shopId);

    /**
     * 新增商品
     */
    String create(ProductSaveParam param);

    /**
     * 更新商品
     */
    boolean update(ProductEditParam param);

    void otherUpdates(ProductSaveParam param, String productSn, Product product, List<GalleryPic> imgList);

    /**
     * 根据商品ID查询商品
     *
     * @param productIds 商品ID
     * @return List<Product>
     */
    List<Product> getProductByIds(List<Integer> productIds);

    /**
     * 根据店铺ID初始化商品
     *
     * @param shopId 店铺id
     */
    void initProductByShopId(Integer shopId);

    /**
     * 获取店铺运费模板
     *
     * @return ShippingTplListVO
     */
    List<ProductShippingTplListVO> getShippingTplListByShopId();

    /**
     * 获取商品库存
     *
     * @param productId       商品ID
     * @param skuId           skuID
     * @param includeActivity 是否包含活动
     * @param shopId
     * @return Integer
     */
    Integer getProductStock(Integer productId, Integer skuId, boolean includeActivity, Integer shopId);

    /**
     * 获取商品最终价格
     *
     * @param productId     商品ID
     * @param originalPrice 原价
     * @param skuId         skuID
     * @return BigDecimal
     */
    BigDecimal getProductFinalPrice(Integer productId, BigDecimal originalPrice, Integer skuId);

    /**
     * 获取商品列表
     */
    List<ProductListResDTO> getProductList(ProductListDTO dto);

    void decStock(Integer productId, Integer quantity ,String description);

    void decStock(Integer productId, Integer quantity ,String description,boolean addLog);

    void incStock(Integer productId, Integer quantity,String description);

    void incStock(Integer productId, Integer quantity,String description,boolean addLog);

    void incSales(Integer productId, Integer quantity);

    /**
     * 获取商品ID列表
     *
     * @return String
     */
    String getProductIds();

    /**
     * 看了还看
     *
     * @param dto 相关信息
     * @return List<Category>
     */
    List<ProductLookAlsoVO> getLookAlso(ProductRelateDTO dto);

    /**
     * 获取商品相似信息（最终价）
     *
     * @param productList 商品列表
     * @return List<ProductLookAlsoVO>
     */
    List<ProductLookAlsoVO> getProductRelate(List<Product> productList, Integer shopId);

    /**
     * 相关排行
     *
     * @param dto 相关信息
     * @return Map<String, Object>
     */
    RelateRankVO getRelateRank(ProductRelateDTO dto);

    /**
     * 相关文章
     *
     * @param dto 相关信息
     * @return List<ArticleVO>
     */
    List<ArticleVO> getArticleList(ProductRelateDTO dto);

    /**
     * 相关品牌
     */
    List<Brand> getRelateBrand(ProductRelateDTO dto);

    /**
     * 相关分类
     *
     * @param dto 相关信息
     * @return List<Category>
     */
    List<Category> getRelateCategory(ProductRelateDTO dto);

    /**
     * 获取商品详情页
     *
     * @param sn 商品编号
     * @return ProductDetailVO
     */
    ProductDetailVO clientDetail(String sn, Integer shopId, Integer nearestShopId);

    /**
     * 获取浏览商品
     */
    List<ProductListResDTO> historyProduct(List<Integer> productIds);

    /**
     * 获取当前分类的父级分类
     *
     * @param categoryId 商品编号
     * @return List<CategoryParentTreeDTO>
     */
    List<CategoryParentTreeDTO> parentTree(Integer categoryId);

    Integer isBuy(Integer userId,int id);

    /**
     * 获取商品是否收藏
     *
     * @param productId 商品id
     * @return ProductDetailVO
     */
    Boolean isCollect(int productId, Integer shopId);

    /**
     * 获取商品优惠券列表
     *
     * @param productId 商品ID
     * @param shopId
     * @return List<CouponDetailVO>
     */
    List<CouponDetailVO> getCouponList(Integer productId, Integer shopId);

    /**
     * 根据筛选条件查询所有商品的分类列表
     *
     * @param productListDTO 查询条件
     * @return ProductListVO
     */
    List<Category> getProductCategoryList(ProductListDTO productListDTO);

    /**
     * 根据筛选条件查询所有商品的品牌列表
     *
     * @param productListDTO 查询条件
     * @return ProductListVO
     */
    List<Brand> getProductBrandList(ProductListDTO productListDTO);

    /**
     * 根据筛选条件查询所有商品的最高价格
     *
     * @param productListDTO 查询条件
     * @return BigDecimal
     */
    BigDecimal getMaxPrice(ProductListDTO productListDTO);

    /**
     * 获取商品SKU详情
     */
    ProductAvailabilityVO getProductSkuDetail(Integer productId, Integer skuId, Integer excludeActivity, String extraAttrIds, Integer shopId,Integer userId);

    /**
     * 获取商品SKU详情多距离计算
     */
    ProductAvailabilityVO getProductSkuDetail(Integer productId, Integer skuId, Integer excludeActivity, String extraAttrIds, Integer shopId, Integer addressId,Integer userId);


    /**
     * 获取商品SKU详情
     */
    ProductAvailabilityVO getProductFinalPrice(Integer productId, Integer skuId, Integer excludeActivity, String extraAttrIds, Integer shopId);

    /**
     * 获取商品SKU详情
     */
    ProductAvailabilityVO getProductFinalPrice(Integer productId, Integer skuId, Integer excludeActivity, String extraAttrIds, Integer shopId,Integer userId);

    /**
     * 获取商品 SKU 详情，可控制是否预套限时折扣价。
     */
    ProductAvailabilityVO getProductFinalPrice(Integer productId, Integer skuId, Integer excludeActivity, String extraAttrIds, Integer shopId, Integer userId, Boolean excludeTimeDiscountPrice);



    /**
     * 获取商品活动与数据
     *
     * @param products      products
     * @param shopId        shopId
     * @param promotionFrom 来源
     * @return Map<Integer, ProductDTO>
     */
    Map<Integer, ProductSaveParam> getProductsPromotion(List<ProductSaveParam> products, Integer shopId, String promotionFrom);

    /**
     * 获取商品活动与数据
     *
     * @param products      products
     * @param shopId        shopId
     * @param promotionFrom 来源
     * @return Map<Integer, ProductDTO>
     */
    Map<Integer, ProductSaveParam> getProductsPromotion(List<ProductSaveParam> products, Integer shopId, String promotionFrom,Integer userId);


    Map<Integer, ProductSaveParam> getProductsPromotion(List<ProductSaveParam> products, List<PromotionVO> allAvailablePromotion, Integer shopId, String promotionFrom, Integer userId);

    /**
     * 获取相关商品
     *
     * @param productId 商品ID
     * @return List<Product>
     */
    List<Product> getRelatedList(Integer productId);

    /**
     * 获取商品询价
     *
     * @param productPriceInquiryDTO productPriceInquiryDTO
     */
    void priceInquiry(ProductPriceInquiryDTO productPriceInquiryDTO);

    /**
     * 获取商品SKU详情
     *
     * @param productId 商品ID
     */
    Page<ProductListResDTO> getRelatedProductList(Integer productId);

    /**
     * 获取商品Sn
     *
     * @param productId 商品ID
     */
    String getProductSnById(Integer productId);

    /**
     * 获取商品分享落地页 HTML
     * <p>
     * 返回带 Open Graph 标签的 HTML 供社交平台（Facebook 等）抓取链接预览卡，
     * 真实访客访问后由落地页自动跳转到 H5 商品详情页。分销中心分享商品时携带分销员ID用于归因。
     *
     * @param productId  商品ID
     * @param salesmanId 分销员ID（可选，用于分销归因，为空则不透传）
     * @return 落地页 HTML
     */
    String getSharePageHtml(Integer productId, Integer salesmanId);

    /**
     * 更新关键词
     *
     * @param dto 参数
     * @return boolean
     */
    String getParticiple(ProductKeyDTO dto);

    /**
     * 更新字段
     *
     * @param updateField updateField
     */
    String updateFieldProduct(UpdateFieldProductDTO updateField, String[] allowFields);

    /**
     * 批量操作
     */
    void batchOperation(ProductBatchOperationDTO dto);

    /**
     * 产品回收
     */
    boolean recycle(ProductRecycleDTO dto);

    /**
     * 产品复制
     */
    String copy(ProductCopyDTO dto);

    /**
     * 产品审核
     */
    boolean audit(ProductAuditDTO dto);

    String createNewProductSn();

    boolean auditAgain(Integer id);

    /**
     * 批量获取最终价库存信息
     *
     * @param skuIds 字符串ids，需要拆分
     * @return map
     */
    Map<Integer, ProductAvailabilityVO> getBatchProductAvailability(String skuIds, Integer shopId);


    /**
     * 获取当前用户历史购买过的商品信息
     */
    Page<ProductListResDTO> getHistoryProduct(BasePage basePage);

    /**
     * 分销商品列表
     */
    Page<SalesmanProductListVO> salesmanProducts(SalesmanProductPageQuery pageQuery);


    /**
     * 获取门店商品的配送方式
     */
    DeliveryOptionVO getDeliveryOption(Integer shopId, BigDecimal latitude, BigDecimal longitude, Integer deliveryType);

    Page<Product> productList(GrouponProductPageQuery pageQuery);

    /**
     * 获取商品推广
     */
    ProductPromoteVO getProductPromote(Integer productId);

    Page<ProductSkuVO> selectProductList(ProductSkuPageQuery query);

    /**
     * 获取商品推广信息（包含H5、PC、小程序三个维度）
     *
     * @param productId 商品ID
     * @return ProductPostVO
     */
    ProductPostVO post(Integer productId);


    /**
     * 处理商品的自动上下架任务（支持门店）
     * 在创建或更新商品时调用
     *
     * @param product 商品
     * @param autoOnlineTime 自动上架时间（格式：yyyy-MM-dd HH:mm:ss）
     * @param autoOfflineTime 自动下架时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    void handleAutoStatusTasks(Product product, String autoOnlineTime, String autoOfflineTime);

    DeliveryOptionVO getDeliveryOption(List<Cart> carts, BigDecimal latitude, BigDecimal longitude, Integer deliveryType);

    DeliveryOptionVO getDeliveryOption(List<Cart> carts, BigDecimal latitude, BigDecimal longitude, Integer deliveryType, UserAddressVO selectedAddress);

    String buildPresaleFinalPaymentTimeText(ProductPresaleVO presaleVO, String clientType, String source);

    String buildPresaleDeliveryTimeText(ProductPresaleVO presaleVO, String clientType, String source);

    /**
     * 保存商品分享信息
     */
    boolean saveShareInfo(com.tigshop.bean.dto.product.ProductShareInfoDTO dto);

    /**
     * 获取商品分享信息
     */
    com.tigshop.bean.vo.product.ProductShareInfoVO getShareInfo(Integer productId);

    /**
     * 公共 hook：供其他 C 端商品列表 service 复用「首条批发价」回填能力
     * （CR-20260603-141000 / CD-032 列表覆盖范围扩展；CR-20260610-162600 / CD-039 放开店铺商品）。
     *
     * <p>批发商身份回填，平台 + 店铺商品统一按 productId 解析（原 CD-031 平台过滤已移除）；
     * 非批发商直接跳过；入参 null 或空列表为安全空操作（不抛异常）。
     *
     * @param voList 商品列表（本方法不会修改 list 大小）
     */
    void backfillFirstWholesalePriceForList(List<ProductListResDTO> voList);

    /**
     * 通用 hook：供任意 C 端商品列表 VO（如 {@code ProductLookAlsoVO} / {@code CollectProductVO}）
     * 复用「首条批发价」回填能力（CR-20260603-141000 / CD-032 列表覆盖范围扩展 W6 Wave 3.5）。
     *
     * <p>语义与 {@link #backfillFirstWholesalePriceForList(List)} 一致：批发商身份回填，
     * 平台 + 店铺商品统一处理（CR-20260610-162600 / CD-039）；非批发商直接跳过；入参 null 或空列表
     * 为安全空操作。通过 getter/setter 函数适配任意 VO 类型，避免污染共享 bean 或为每种 VO 写重复回填分支。
     *
     * <p>注意：本通用版本不携带零售价（retailPrice），因此「折扣模式」批发价行（依赖零售价计算）
     * 不会回填，仅「固定价模式」批发价行命中时回填；如需折扣模式回填请使用携带零售价的专用通道。
     *
     * @param voList         商品列表（null 或空为安全空操作；本方法不会修改 list 大小，仅 set 元素字段）
     * @param shopIdGetter   从 VO 取 shopId 的函数（CR-20260610-162600 起仅保留签名兼容，不再用于过滤）
     * @param productIdGetter 从 VO 取 productId 的函数
     * @param setter         向 VO 写入首条批发价的函数
     * @param <T>            VO 类型
     */
    <T> void backfillFirstWholesalePriceForListGeneric(
            List<T> voList,
            java.util.function.Function<T, Integer> shopIdGetter,
            java.util.function.Function<T, Integer> productIdGetter,
            java.util.function.BiConsumer<T, java.math.BigDecimal> setter);

    /**
     * 通用 hook（5 参数重载，CR-20260603-1720 / CD-035）：在 firstWholesalePrice 之外
     * 同步回填 {@code wholesaleCardMoq}（卡片可直接加购的起批数）。
     *
     * <p>语义与 4 参数版完全一致；额外的 {@code moqSetter} 在 firstPriceMap 命中商品集合上
     * 派生 4 条规则后批量回填（CD-035）。{@code moqSetter} 传 null 时退化为 4 参数版行为。
     *
     * @param voList         商品列表
     * @param shopIdGetter   shopId getter
     * @param productIdGetter productId getter
     * @param priceSetter    首条批发价 setter
     * @param moqSetter      卡片起批数 setter（null = 不回填该字段）
     * @param <T>            VO 类型
     */
    <T> void backfillFirstWholesalePriceForListGeneric(
            List<T> voList,
            java.util.function.Function<T, Integer> shopIdGetter,
            java.util.function.Function<T, Integer> productIdGetter,
            java.util.function.BiConsumer<T, java.math.BigDecimal> priceSetter,
            java.util.function.BiConsumer<T, Integer> moqSetter);

    /**
     * 通用 hook（7 参数重载，CR-20260613-0943）：在 firstWholesalePrice / wholesaleCardMoq 之外，
     * 同步回填 {@code wholesaleMoqDimension}（起批维度）与 {@code wholesaleMultiple}（SPU 兜底
     * sku_id=0 倍数）。
     *
     * <p>性能：维度复用 moq 的同一份 config 查询（0 额外 SQL）；{@code multipleSetter} 非 null 时
     * +1 次 sku_config IN。各 setter 传 null 时对应字段不回填；门槛/范围与 5 参数版完全一致。
     *
     * @param voList          商品列表
     * @param shopIdGetter    shopId getter
     * @param productIdGetter productId getter
     * @param priceSetter     首条批发价 setter
     * @param moqSetter       卡片起批数 setter（null = 不回填）
     * @param dimensionSetter 起批维度 setter（null = 不回填）
     * @param multipleSetter  SPU 兜底倍数 setter（null = 不回填）
     * @param <T>             VO 类型
     */
    <T> void backfillFirstWholesalePriceForListGeneric(
            List<T> voList,
            java.util.function.Function<T, Integer> shopIdGetter,
            java.util.function.Function<T, Integer> productIdGetter,
            java.util.function.BiConsumer<T, java.math.BigDecimal> priceSetter,
            java.util.function.BiConsumer<T, Integer> moqSetter,
            java.util.function.BiConsumer<T, Integer> dimensionSetter,
            java.util.function.BiConsumer<T, Integer> multipleSetter);

    /**
     * 通用 hook（8 参数重载，CR-20260615-1017）：在 7 参版基础上显式传入 {@code retailPriceMap}
     * （productId → 零售价），供折扣模式（price_mode=1）批发价计算（{@code retailPrice × discount/10}）。
     *
     * <p>修复泛型通道遗留限制：原 7 参版内部传空零售价 map，折扣模式行算不出价（返回 null）被丢弃，
     * 致折扣模式批发商品 firstWholesalePrice 及 moq/维度/倍数全 null。本版与强类型通道
     * {@code backfillFirstWholesalePrice} 口径一致。{@code retailPriceMap} 传 null/空时退化为原行为。
     *
     * @param retailPriceMap productId → 零售价（折扣模式所需；缺失项折扣模式不计算）
     * @param <T>            VO 类型
     */
    <T> void backfillFirstWholesalePriceForListGeneric(
            List<T> voList,
            java.util.function.Function<T, Integer> shopIdGetter,
            java.util.function.Function<T, Integer> productIdGetter,
            java.util.function.BiConsumer<T, java.math.BigDecimal> priceSetter,
            java.util.function.BiConsumer<T, Integer> moqSetter,
            java.util.function.BiConsumer<T, Integer> dimensionSetter,
            java.util.function.BiConsumer<T, Integer> multipleSetter,
            java.util.Map<Integer, java.math.BigDecimal> retailPriceMap);
}
