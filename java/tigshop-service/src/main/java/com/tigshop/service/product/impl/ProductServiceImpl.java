// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.product.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base62;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.EscapeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.bo.vendort.VendorProductBO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.common.UpdateFieldProductDTO;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.dto.o2o.stockcalendar.StockCalendarQueryDTO;
import com.tigshop.bean.dto.order.CategoryParentTreeDTO;
import com.tigshop.bean.dto.product.*;
import com.tigshop.bean.dto.promotion.PromotionTypeDTO;
import com.tigshop.bean.dto.promotion.SeckillItemInfoDTO;
import com.tigshop.bean.dto.promotion.TimeDiscountDTO;
import com.tigshop.bean.dto.user.UserCouponListDTO;
import com.tigshop.bean.model.user.UserCoupon;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.common.shareTypeEnum;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.enums.o2o.ShopDeliveryTypeEnum;
import com.tigshop.bean.enums.o2o.ShopTypeEnum;
import com.tigshop.bean.enums.order.OrderDeliveryTypeEnum;
import com.tigshop.bean.enums.product.*;
import com.tigshop.bean.enums.promotion.PromotionType;
import com.tigshop.bean.enums.promotion.SendRangeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.feign.wechat.GetTokenResult;
import com.tigshop.bean.feign.wechat.ShortLinkParam;
import com.tigshop.bean.feign.wechat.ShortLinkResult;
import com.tigshop.bean.model.authority.Suppliers;
import com.tigshop.bean.model.cart.Cart;
import com.tigshop.bean.model.msg.AdminMsg;
import com.tigshop.bean.model.o2o.*;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.product.*;
import com.tigshop.bean.model.promotion.*;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.setting.GalleryPic;
import com.tigshop.bean.model.setting.ShippingTpl;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.*;
import com.tigshop.bean.model.vendor.VendorShopBind;
import com.tigshop.bean.model.vendor.product.VendorProduct;
import com.tigshop.bean.model.vendor.product.VendorProductSku;
import com.tigshop.bean.param.o2o.AllocationParam;
import com.tigshop.bean.param.product.ProductEditParam;
import com.tigshop.bean.param.product.ProductSaveParam;
import com.tigshop.bean.param.product.VendorProductImportParam;
import com.tigshop.bean.param.product.VendorProductReImportParam;
import com.tigshop.bean.query.content.ArticlePageQuery;
import com.tigshop.bean.query.product.ProductSkuPageQuery;
import com.tigshop.bean.query.promotion.groupon.GrouponProductPageQuery;
import com.tigshop.bean.query.salesman.SalesmanProductPageQuery;
import com.tigshop.bean.query.shop.PickupPageQuery;
import com.tigshop.bean.vo.config.GetStoreSelfOperatedVO;
import com.tigshop.bean.vo.config.GetStoreVO;
import com.tigshop.bean.vo.content.ArticleVO;
import com.tigshop.bean.vo.o2o.DeliveryOptionVO;
import com.tigshop.bean.vo.o2o.StockCalendarVO;
import com.tigshop.bean.vo.product.*;
import com.tigshop.bean.vo.promotion.*;
import com.tigshop.bean.model.salesman.SalesmanProduct;
import com.tigshop.bean.vo.salesman.SalesmanProductListVO;
import com.tigshop.bean.vo.user.UserAddressVO;
import com.tigshop.bean.vo.user.UserCouponVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.common.enums.DataType;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.*;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.feign.WechatApiClient;
import com.tigshop.mapper.cart.CartMapper;
import com.tigshop.mapper.o2o.AreaStoreManagerShopMapper;
import com.tigshop.mapper.o2o.ShopLocalDeliveryConfigMapper;
import com.tigshop.mapper.o2o.ShopPickupConfigMapper;
import com.tigshop.mapper.o2o.StoreProductMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.product.PriceInquiryMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.promotion.*;
import com.tigshop.mapper.salesman.SalesmanProductMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.user.CollectProductMapper;
import com.tigshop.mapper.user.UserAddressMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.mapper.vendor.VendorShopBindMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.content.ArticleService;
import com.tigshop.service.lang.ImageI18nService;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.oauth.WechatOAuthService;
import com.tigshop.service.product.*;
import com.tigshop.service.promotion.*;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.GalleryPicService;
import com.tigshop.service.setting.ShippingTplService;
import com.tigshop.service.shop.ShopProductCategoryService;
import com.tigshop.service.user.UserAddressService;
import com.tigshop.service.user.UserCouponService;
import com.tigshop.service.user.FeedbackShopResolver;
import com.tigshop.service.user.UserRankService;
import com.tigshop.service.user.MemberBenefitService;
import com.tigshop.service.wholesale.WholesalePriceService;
import com.tigshop.service.wholesale.WholesalerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import com.tigshop.utils.WrapperConditionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.tigshop.bean.dto.product.ProductDescDTO.getProductDescArr;
import static com.tigshop.bean.enums.order.PaymentStatus.PAID;
import static com.tigshop.bean.enums.product.DeleteType.NOT_DELETE;
import static com.tigshop.bean.enums.product.IntroType.*;
import static com.tigshop.bean.enums.product.ProductStatus.ON_SALE;
import static com.tigshop.bean.enums.product.ProductType.PRODUCT_TYPE_CARD;
import static com.tigshop.bean.enums.promotion.PromotionRangeEnum.PROMOTION_RANGE_EXCLUDE_PRODUCT;
import static com.tigshop.bean.enums.promotion.PromotionRangeEnum.PROMOTION_RANGE_PRODUCT;
import static com.tigshop.bean.enums.promotion.PromotionType.TYPE_GIVE;
import static com.tigshop.bean.enums.promotion.PromotionType.TYPE_LIMIT_DISCOUNT;
import static com.tigshop.bean.enums.setting.SettingsEnum.STORE_INDEPENDENT_GOODS;
import static com.tigshop.bean.enums.setting.SettingsEnum.STORE_POST_ALLOCATION_STATUS;
import static com.tigshop.common.constant.Constants.NO;
import static com.tigshop.common.constant.Constants.YES;
import static com.tigshop.common.constant.ExceptionConstants.SERVICE_ERROR;
import static com.tigshop.common.constant.product.ProductConstants.*;
import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 商品服务接口实现
 *
 * @author Tigshop团队
 * @create 2024年11月20日 11:11
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class ProductServiceImpl extends BaseServiceImpl<ProductMapper, Product> implements ProductService {
    public static final String PRESALE_SOURCE_PRODUCT_DETAIL = "product_detail";
    public static final String PRESALE_SOURCE_ORDER_SUBMIT = "order_submit";
    public static final String PRESALE_SOURCE_ORDER_DETAIL = "order_detail";

    private final ProductSkuService productSkuService;
    private final CategoryService categoryService;
    private final ProductGalleryService productGalleryService;
    private final ProductAttributesService productAttributesService;
    private final AttributesTplService attributesTplService;
    private final ShippingTplService shippingTplService;
    private final ProductPromotionService productPromotionService;
    private final ServicesService servicesService;
    private final ProductVideoService productVideoService;
    private final UserRankService userRankService;
    private final MemberBenefitService memberBenefitService;
    private final ProductArticleService productArticleService;
    private final ConfigService configService;
    private final ProductGroupService productGroupService;
    private final GalleryPicService galleryPicService;
    private final BrandService brandService;
    private final ArticleService articleService;
    private final OrderMapper orderMapper;
    private final SeckillService seckillService;
    private final SeckillItemMapper seckillItemMapper;
    private final UserMapper userMapper;
    private final CommentService commentService;
    private final FeedbackShopResolver feedbackShopResolver;
    private final CollectProductMapper collectProductMapper;
    private final CouponService couponService;
    private final UserCouponService userCouponService;
    private final PromotionService promotionService;
    private final ProductPromotionMapper productPromotionMapper;
    private final TimeDiscountMapper timeDiscountMapper;
    private final TimeDiscountItemMapper timeoutDiscountItemMapper;
    private final ProductGiftService productGiftService;
    private final TimeDiscountService timeDiscountService;
    private final PriceInquiryMapper priceInquiryMapper;
    private final AdminMsgService adminMsgService;
    private final ShopMapper shopMapper;
    private final ECardGroupService ecardGroupService;
    private final AdminLogService adminLogService;
    private final TranslatePackageImpl translatePackage;
    private final ImageI18nService imageI18nService;
    private final MessageHelper messageHelper;
    private final ShopProductCategoryService shopProductCategoryService;
    private final ECardService eCardService;
    private final ProductInventoryLogService productInventoryLogService;
    private final PointsExchangeMapper pointsExchangeMapper;
    private final VendorShopBindMapper vendorShopBindMapper;
    private final GrouponPromotionService grouponPromotionService;
    private final ProductEsSyncService productEsSyncService;

    // 保证金生命周期服务：上架收取节点触发；@Lazy 注入避免与本类的循环依赖
    @org.springframework.context.annotation.Lazy
    @org.springframework.beans.factory.annotation.Autowired
    private com.tigshop.service.deposit.DepositLifecycleService depositLifecycleService;

    private final TigshopProperties tigshopProperties;
    private final ApplicationContext applicationContext;
    private final CartMapper cartMapper;
    private final AreaStoreManagerShopMapper areaStoreManagerShopMapper;
    private final WechatApiClient wechatApiClient;
    private final ShortUrlMapper shortUrlMapper;
    private final GrouponPromotionShopMapper grouponPromotionShopMapper;
    private final ProductPresaleService productPresaleService;
    private final SalesmanProductMapper salesmanProductMapper;
    private final GrouponPromotionMapper grouponPromotionMapper;


    private final UserAddressMapper userAddressMapper;
    private final ShopLocalDeliveryConfigMapper shopLocalDeliveryConfigMapper;
    private final ShopPickupConfigMapper shopPickupConfigMapper;
    private final UserAddressService userAddressService;
    private final ProductAutoStatusService productAutoStatusService;
    private final GiftCardGroupsMapper giftCardGroupsMapper;
    private final GrouponPromotionItemMapper grouponPromotionItemMapper;
    private final ShortUrlService shortUrlService;
    private final WechatOAuthService wechatOAuthService;
    private final StoreProductMapper storeProductMapper;

    // 不用 final + Lombok 构造器注入：会与 WholesalerServiceImpl(final UserService) + UserServiceImpl(final ProductService)
    // 形成三段构造器循环 (user→product→wholesaler→user)。改用 setter + @Lazy 参数注入：
    // Spring 注入 lazy proxy 解循环；ProductServiceImpl 无 @InjectMocks 测试，setter 不影响既有测试。
    private WholesalerService wholesalerService;
    private WholesalePriceService wholesalePriceService;
    private com.tigshop.service.wholesale.WholesaleProductConfigService wholesaleProductConfigService;

    @Autowired
    public void setWholesalerService(@Lazy WholesalerService wholesalerService) {
        this.wholesalerService = wholesalerService;
    }

    @Autowired
    public void setWholesalePriceService(@Lazy WholesalePriceService wholesalePriceService) {
        this.wholesalePriceService = wholesalePriceService;
    }

    @Autowired
    public void setWholesaleProductConfigService(@Lazy com.tigshop.service.wholesale.WholesaleProductConfigService wholesaleProductConfigService) {
        this.wholesaleProductConfigService = wholesaleProductConfigService;
    }

    @Override
    public Page<ProductListResDTO> list(ProductListDTO listDTO) {
        if (listDTO.getOnlyShowMe() == null) {
            listDTO.setOnlyShowMe(false);
        }
        // 分页查询
        Page<Product> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 创建查询构造器
        MPJLambdaWrapper<Product> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.selectAll(Product.class);
        queryWrapper.in(ObjectUtil.equals(1, listDTO.getReservationProduct()), Product::getProductType,
                List.of(ProductType.PRODUCT_TYPE_RESERVATION_HOME.getCode(), ProductType.PRODUCT_TYPE_RESERVATION_STORE.getCode()));
        if (tigshopProperties.getIsO2o() == 1 && ((listDTO.getSearchShop() == 1 || listDTO.getShopId() != null && listDTO.getShopId() > 0))) {
            queryWrapper
                    .select("sp2.product_price as storeProductPrice")
                    .select("sp2.product_name as storeProductName")
                    .select("sp2.pic_url as storePicUrl")
                    .select("CASE WHEN sp2.product_status = 1 AND t.product_status = 1 THEN 1 ELSE 0 END AS storeProductStatus")
                    .select("sp2.product_stock as storeProductStock")
                    .select("sp2.card_group_id as storeCardGroupId")
            ;

        }
        // 排序
        if (ObjectUtil.isNotEmpty(listDTO.getIds())) {
            // ids 不为空按ids排序
            // 防止 SQL 注入：校验 ids 格式，只允许数字和逗号
            String ids = listDTO.getIds();
            if (!ids.matches("^[0-9,]+$")) {
                throw new GlobalException("非法的 ids 参数");
            }
            // 解析并过滤有效的整数ID
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .toList();
            if (idList.isEmpty()) {
                throw new GlobalException("ids 参数为空");
            }
            // 使用安全的方式构建 FIELD 函数
            String safeIds = idList.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            String orderSql = "FIELD(t." + getKeyColumn() + ", " + safeIds + ")";
            queryWrapper.last("ORDER BY " + orderSql);
        } else {
            if (Boolean.TRUE.equals(listDTO.getSortByTotalSales()) || Boolean.TRUE.equals(listDTO.getSortByThirtyDaySales())) {
                // 累计销量 / 30日销量 排序：口径均复用虚拟销量 virtual_sales，方向取 sortOrder（默认降序）
                buildSortOrder(page, "virtualSales", listDTO.getSortOrder());
            } else if (listDTO.getIsClient() != null && listDTO.getIsClient() == 1 && listDTO.getSortField() == null) {
                queryWrapper.orderByAsc(Product::getSortOrder);
                queryWrapper.orderByDesc(Product::getProductId);
            } else {
                buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());
            }
        }

        // 后台译名搜索沿用列表展示的语言/原文匹配规则；状态与店铺范围仍由外层条件控制。
        ProductListDTO queryDTO = listDTO;
        String localeCode = ThreadLocalUtil.getValue();
        if (Objects.equals(tigshopProperties.getIsOverseas(), 1)
                && StrUtil.isNotBlank(listDTO.getKeyword()) && StrUtil.isNotBlank(localeCode)) {
            List<Integer> translatedIds = baseMapper.searchProductIdsByDisplayedName(
                    listDTO.getKeyword(), localeCode.trim(), DataType.API.getCode());
            if (CollUtil.isNotEmpty(translatedIds)) {
                queryDTO = new ProductListDTO();
                BeanUtils.copyProperties(listDTO, queryDTO);
                Set<Integer> searchIds = new LinkedHashSet<>();
                if (CollUtil.isNotEmpty(listDTO.getIdList())) {
                    searchIds.addAll(listDTO.getIdList());
                }
                searchIds.addAll(translatedIds);
                queryDTO.setIdList(new ArrayList<>(searchIds));
            }
        }
        // 复用 clientList 的原文关键词 OR idList 分组，后续筛选条件保持 AND 关系。
        buildQueryWrapper(queryWrapper, queryDTO);

        //buildQueryWrapper额外扩充
        if (listDTO.getShopId() != null && listDTO.getShopId() == 0) {
            queryWrapper.eq(Product::getShopId, listDTO.getShopId());
        }

        if (tigshopProperties.getIsO2o() == 1) {
            List<Integer> posSource = listDTO.getPosProductSource();
            boolean isPosProductQuery = CollUtil.isNotEmpty(posSource);
            if (listDTO.getSearchShop() == 1 || (listDTO.getShopId() != null && listDTO.getShopId() != 0) || isPosProductQuery) {
                Integer currentShopId = getShopId();
                // 仅收银台商品链路：门店(shopId>0)与平台自营(shopId=0)统一走门店分支；
                // 平台自营收银台当作 currentShopId=0 的普通门店处理，其"本店自建"即平台自营商品(shop_id=0)。
                // D1123 起请求头保留真实 shop/admin 身份，POS 场景以控制器注入的来源上下文识别。
                boolean isPosStore = isPosProductQuery
                        && currentShopId != null && currentShopId >= 0;
                if (isPosStore) {
                    // 门店收银台：只展示「分配给本店的平台商品(shop_id=0 且本店 store_product 有分配) + 本店自建商品」。
                    // 平台自营收银台(currentShopId=0)：本店自建=平台自营商品(shop_id=0)，等同展示全部平台自营商品。
                    // 整体以 AND 收敛门店归属，避免 copyConditionsWithOr 引入的顶层 OR 让 base 分支放行未分配/他店商品。
                    // 门店自建(1)和供应商商品(3)都归属于当前门店；具体来源继续由下游 vendor_id 条件区分。
                    boolean showStoreOwned = CollUtil.isEmpty(posSource) || posSource.contains(1) || posSource.contains(3);
                    boolean showPlatform = CollUtil.isEmpty(posSource) || posSource.contains(2);   // 平台分配(2)
                    queryWrapper.leftJoin(StoreProduct.class, "sp2", StoreProduct::getProductId, Product::getProductId);
                    queryWrapper.and(w -> {
                        if (showPlatform) {
                            w.nested(n -> n.eq(Product::getShopId, 0)
                                    .eq(StoreProduct::getShopId, currentShopId)
                                    .eq(StoreProduct::getIsDelete, 0));
                        }
                        if (showStoreOwned) {
                            if (showPlatform) {
                                w.or();
                            }
                            w.eq(Product::getShopId, currentShopId);
                        }
                    });
                } else {
                    // searchShop / C端 shopId>0 / 其它场景：保持原逻辑不变
                    WrapperConditionUtils.copyConditionsWithOr(queryWrapper, null, listDTO.getKeyword());
                    queryWrapper.ne(StoreProduct::getShopId, 0);
                }
                queryWrapper.groupBy(Product::getProductId);
            }
        }

        Integer saleMode = listDTO.getSaleMode();
        if (saleMode != null) {
            queryWrapper.eq(Product::getSaleMode, saleMode);
        }

        // 查询后的分页数据
        Page<Product> queryPage = this.page(page, queryWrapper);

        if (CollUtil.isEmpty(queryPage.getRecords())) {
            return PageUtil.convertPage(page, List.of());
        }

        // 将配置查询移到循环外部，避免N次重复查询
        boolean needStoreConfig = tigshopProperties.getIsO2o() == 1 && listDTO.getShopId() != null && listDTO.getShopId() != 0;
        ConfigPO storeAssignProductName = null;
        ConfigPO storeAssignProductPrice = null;
        ConfigPO storeAssignProductImage = null;
        ConfigPO storeUseSoloProductStock = null;
        if (needStoreConfig) {
            storeAssignProductName = configService.getConfigByCode(SettingsEnum.STORE_ASSIGN_PRODUCT_NAME.getBizCode());
            storeAssignProductPrice = configService.getConfigByCode(SettingsEnum.STORE_ASSIGN_PRODUCT_PRICE.getBizCode());
            storeAssignProductImage = configService.getConfigByCode(SettingsEnum.STORE_ASSIGN_PRODUCT_IMAGE.getBizCode());
            storeUseSoloProductStock = configService.getConfigByCode(SettingsEnum.STORE_USE_SOLO_PRODUCT_STOCK.getBizCode());
        }

        // 使用 final 变量用于 lambda 表达式
        final ConfigPO finalStoreAssignProductName = storeAssignProductName;
        final ConfigPO finalStoreAssignProductPrice = storeAssignProductPrice;
        final ConfigPO finalStoreAssignProductImage = storeAssignProductImage;
        final ConfigPO finalStoreUseSoloProductStock = storeUseSoloProductStock;

        // 批量查询门店覆盖主图，避免依赖 MPJ auto-mapping
        Map<Integer, String> storePicUrlMapTmp = Map.of();
        final Map<Integer, String> storePicUrlMap = storePicUrlMapTmp;

        queryPage.getRecords().forEach(product -> {
            if (needStoreConfig && product.getShopId() == 0) {
                if (finalStoreAssignProductName != null && "1".equals(finalStoreAssignProductName.getBizVal())) {
                    if (ObjectUtil.isNotEmpty(product.getStoreProductName())) {
                        product.setProductName(product.getStoreProductName());
                    }
                }
                if (finalStoreAssignProductPrice != null && "1".equals(finalStoreAssignProductPrice.getBizVal())) {
                    product.setProductPrice(product.getStoreProductPrice());
                }
                String storePicUrl = storePicUrlMap.get(product.getProductId());
                if (ObjectUtil.isNotEmpty(storePicUrl)) {
                    product.setPicUrl(storePicUrl);
                    product.setPicThumb(storePicUrl);
                    product.setPicLarge(storePicUrl);
                }
                if (finalStoreUseSoloProductStock != null && "1".equals(finalStoreUseSoloProductStock.getBizVal())) {
                    product.setProductStock(product.getStoreProductStock());
                }
                product.setProductStatus(product.getStoreProductStatus());
                product.setCardGroupId(product.getStoreCardGroupId());
            }

            //在这里增加预约商品的库存设置
        });

        // 批量查询卡片商品的未使用卡券库存，避免循环内N次查询
        List<Integer> cardGroupIds = queryPage.getRecords().stream()
                .filter(product -> product.getProductType() == PRODUCT_TYPE_CARD.getCode())
                .map(Product::getCardGroupId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Integer, Long> cardStockMap = new HashMap<>();
        if (!cardGroupIds.isEmpty()) {
            // 批量查询每个 groupId 的未使用卡券数量
            List<ECard> unusedCards = eCardService.list(new LambdaQueryWrapper<ECard>()
                    .eq(ECard::getIsUse, NO)
                    .in(ECard::getGroupId, cardGroupIds));
            cardStockMap = unusedCards.stream()
                    .collect(Collectors.groupingBy(ECard::getGroupId, Collectors.counting()));
        }
        // 设置卡片商品库存
        final Map<Integer, Long> finalCardStockMap = cardStockMap;
        queryPage.getRecords().forEach(product -> {
            if (product.getProductType() == PRODUCT_TYPE_CARD.getCode()) {
                Long stock = finalCardStockMap.getOrDefault(product.getCardGroupId(), 0L);
                product.setProductStock(stock.intValue());
            }
        });
        // 获取商品 ID 列表
        List<Integer> productIds = queryPage.getRecords().stream()
                .map(Product::getProductId)
                .toList();
        // 查询商品规格
        List<ProductSkuDTO> productSkus = productSkuService.getProductSkusByProductIds(productIds);

        // 将商品规格按商品 ID 分组
        Map<Integer, List<ProductSkuDTO>> productSkuMap = productSkus.stream()
                .collect(Collectors.groupingBy(ProductSkuDTO::getProductId));

        List<UserRank> userRankList = userRankService.getUserRank();
        User user = userMapper.selectById(SecurityUtils.getCurrentUserId());
        Integer rankId = user != null ? user.getRankId() : 0;

        // 批量查询参与积分兑换的商品ID，避免循环内N次查询
        Set<Integer> pointsExchangeProductIds = pointsExchangeMapper.selectList(
                Wrappers.lambdaQuery(PointsExchange.class).in(PointsExchange::getProductId, productIds)
        ).stream().map(PointsExchange::getProductId).collect(Collectors.toSet());

        // 整页优化上下文：user / 促销行 / realPromotion JSON / 赠品跨条目复用，避免同一份促销对每个商品重复查库
        ProductListDetailContext listCtx = new ProductListDetailContext(pointsExchangeProductIds);
        // 供 getProductsPromotion 复用已批量加载的 SKU，避免循环内逐商品 getProductSkusByPid
        listCtx.productSkuMap = productSkuMap;

        // 批量查询所有商品的促销信息，避免循环内N次调用getProductsPromotion
        // 按商品的shopId分组，保持原有逻辑（每个商品使用自己的shopId查询促销）
        Map<Integer, List<ProductSaveParam>> productsByShopId = new HashMap<>();
        for (Product product : queryPage.getRecords()) {
            List<ProductSkuDTO> skus = productSkuMap.get(product.getProductId());
            int skuId = (skus != null && !skus.isEmpty()) ? skus.getFirst().getSkuId() : 0;

            ProductSaveParam param = new ProductSaveParam();
            param.setProductId(product.getProductId());
            param.setSkuId(skuId);
            param.setShopId(product.getShopId());

            // 按商品的shopId分组
            productsByShopId.computeIfAbsent(product.getShopId(), k -> new ArrayList<>()).add(param);
        }

        // 对每个shopId分组批量获取促销信息，然后合并结果
        // 使用复合key: productId + "_" + skuId 避免productId和skuId值重复导致的冲突
        Map<String, ProductSaveParam> batchPromotions = new HashMap<>();
        Integer userId = user != null ? user.getUserId() : null;
        // 整页涉及的店铺一次性取活动：逐店铺调用会让每个店铺各查一遍活动表、券行与会员资料
        Map<Integer, List<PromotionVO>> promotionByShopId = new HashMap<>(
                promotionService.getAllAvailablePromotionByShopIds(productsByShopId.keySet(), userId));
        for (Map.Entry<Integer, List<ProductSaveParam>> entry : productsByShopId.entrySet()) {
            Integer productShopId = entry.getKey();
            List<ProductSaveParam> productsInShop = entry.getValue();

            // 已按店铺预取；shopId 为空的分组语义是「不限店铺」，批量入口不覆盖，此处回退单店查询
            List<PromotionVO> allAvailablePromotion = promotionByShopId.computeIfAbsent(
                    productShopId, sid -> promotionService.getAllAvailablePromotion(sid, userId));

            // 拆分为两类：skuId>0的可以批量处理，skuId<=0的需要单独处理
            List<ProductSaveParam> withSkuProducts = productsInShop.stream()
                    .filter(p -> p.getSkuId() != null && p.getSkuId() > 0)
                    .toList();
            List<ProductSaveParam> noSkuProducts = productsInShop.stream()
                    .filter(p -> p.getSkuId() == null || p.getSkuId() <= 0)
                    .toList();

            // skuId>0的商品批量处理，使用"detail"模式，以skuId作为key
            if (!withSkuProducts.isEmpty()) {
                Map<Integer, ProductSaveParam> skuPromotions = getProductsPromotion(withSkuProducts, allAvailablePromotion, productShopId, "detail", userId, listCtx);
                for (ProductSaveParam param : withSkuProducts) {
                    Integer pId = param.getProductId();
                    Integer sId = param.getSkuId();
                    String compositeKey = pId + "_" + sId;
                    ProductSaveParam promotionParam = skuPromotions.get(sId);
                    if (promotionParam != null) {
                        batchPromotions.put(compositeKey, promotionParam);
                    }
                }
            }

            // skuId<=0的商品每个单独调用，使用"list"模式，以productId作为key
            for (ProductSaveParam param : noSkuProducts) {
                List<ProductSaveParam> singleProduct = List.of(param);
                Map<Integer, ProductSaveParam> singlePromotion = getProductsPromotion(singleProduct, allAvailablePromotion, productShopId, "deteail", userId, listCtx);
                Integer pId = param.getProductId();
                String compositeKey = pId + "_0";
                ProductSaveParam promotionParam = singlePromotion.get(0);
                if (promotionParam != null) {
                    batchPromotions.put(compositeKey, promotionParam);
                }
            }
        }

        // 批量查询所有商品的限时折扣状态
        long currentTime = StringUtils.getCurrentTime();
        Set<Integer> timeDiscountProductIds = timeoutDiscountItemMapper.selectList(
                new LambdaQueryWrapper<TimeDiscountItem>()
                        .in(TimeDiscountItem::getProductId, productIds)
                        .le(TimeDiscountItem::getStartTime, currentTime)
                        .ge(TimeDiscountItem::getEndTime, currentTime)
        ).stream().map(TimeDiscountItem::getProductId).collect(Collectors.toSet());

        // 整页批量预取秒杀/限时折扣/拼团明细，替代 getProductPrice 内的逐商品查询（消除 N+1）
        Map<String, SeckillItem> seckillItemMap = new HashMap<>();
        Map<String, TimeDiscountItem> timeDiscountItemMap = new HashMap<>();
        Map<Integer, BigDecimal> grouponMinPriceMap = new HashMap<>();
        {
            Set<Integer> seckillIds = new HashSet<>();
            Set<Integer> seckillSkuIds = new HashSet<>();
            Set<Object> discountIds = new HashSet<>();
            Set<Integer> discountProductIds = new HashSet<>();
            Set<Integer> grouponProductIds = new HashSet<>();
            for (Map.Entry<String, ProductSaveParam> promotionEntry : batchPromotions.entrySet()) {
                ProductSaveParam promotionParam = promotionEntry.getValue();
                if (promotionParam == null || promotionParam.getActivityInfo() == null) {
                    continue;
                }
                String[] keyParts = promotionEntry.getKey().split("_");
                Integer keyProductId = Integer.valueOf(keyParts[0]);
                Integer keySkuId = keyParts.length > 1 ? Integer.valueOf(keyParts[1]) : 0;
                for (PromotionVO promotionVO : promotionParam.getActivityInfo()) {
                    if (promotionVO.getType() == 1 && promotionVO.getRelationId() != null) {
                        seckillIds.add(promotionVO.getRelationId());
                        seckillSkuIds.add(keySkuId);
                    } else if (promotionVO.getType() == 6 && promotionVO.getData() != null
                            && promotionVO.getData().get("discountId") != null) {
                        discountIds.add(promotionVO.getData().get("discountId"));
                        discountProductIds.add(keyProductId);
                    } else if (promotionVO.getType() == PromotionType.TYPE_GROUPON.getCode()) {
                        grouponProductIds.add(keyProductId);
                    }
                }
            }
            if (!seckillIds.isEmpty() && !seckillSkuIds.isEmpty()) {
                seckillItemMapper.selectList(new LambdaQueryWrapper<SeckillItem>()
                        .in(SeckillItem::getSeckillId, seckillIds)
                        .in(SeckillItem::getSkuId, seckillSkuIds))
                        .forEach(item -> seckillItemMap.putIfAbsent(item.getSeckillId() + "_" + item.getSkuId(), item));
            }
            if (!discountIds.isEmpty() && !discountProductIds.isEmpty()) {
                timeoutDiscountItemMapper.selectList(new LambdaQueryWrapper<TimeDiscountItem>()
                        .in(TimeDiscountItem::getDiscountId, discountIds)
                        .in(TimeDiscountItem::getProductId, discountProductIds))
                        .forEach(item -> timeDiscountItemMap.putIfAbsent(item.getDiscountId() + "_" + item.getProductId(), item));
            }
            if (!grouponProductIds.isEmpty()) {
                grouponMinPriceMap.putAll(grouponPromotionService.getGrouponMinPriceBatch(grouponProductIds));
            }
        }

        // 转换为DTO
        List<ProductListResDTO> productListRes = queryPage.getRecords().stream()
                .map(product -> {
                    ProductListResDTO dto = convertToDTO(product);
                    dto.setIsSeckill(false);
                    dto.setIsGroupon(false);
                    List<ProductSkuDTO> skus = productSkuMap.get(product.getProductId());
                    dto.setProductSku(skus);
                    dto.setSkuCount(skus == null ? 0 : skus.size());
                    if (CollUtil.isNotEmpty(skus) && !ProductType.isReservationProduct(product.getProductType())) {
                        Integer productStock = skus.stream().map(ProductSkuDTO::getSkuStock).reduce(0, Integer::sum);
                        if (tigshopProperties.getIsO2o() == 1 && listDTO.getShopId() != null && listDTO.getShopId() != 0 && product.getShopId() == 0) {
                            // 不处理
                        } else {
                            dto.setProductStock(productStock);
                        }
                    }
                    int skuId = 0;
                    ProductSkuDTO firstSku = null;
                    if (skus != null && !skus.isEmpty()) {
                        firstSku = skus.getFirst();
                        skuId = firstSku.getSkuId();
                        dto.setProductPrice(new BigDecimal(firstSku.getSkuPrice()));
                    } else {
                        dto.setProductPrice(product.getProductPrice());
                    }

                    // 设置限时折扣标识（单独查询TimeDiscountItem表）
                    dto.setIsTimeDiscount(timeDiscountProductIds.contains(product.getProductId()));
                    dto.setProductSaleMode(Optional.ofNullable(product.getSaleMode()).orElse(0) >= 1 ? 1 : 0);
                    dto.setPrice(this.getProductPrice(dto, product.getProductId(), skuId, userRankList, rankId, listDTO.getShopId(), true, pointsExchangeProductIds, firstSku, batchPromotions, seckillItemMap, timeDiscountItemMap, grouponMinPriceMap));
                    return dto;
                })
                .toList();
        translateProductListNames(productListRes);


        fillGiftCardGroupType(queryPage.getRecords(), productListRes);

        //循环获得店铺IDS
        List<Integer> shopIds = queryPage.getRecords().stream()
                .map(Product::getShopId)
                .toList();
        //如果不为空则查询shop获得shopMap
        if (!shopIds.isEmpty()) {
            List<Shop> shops = shopMapper.selectBatchIds(shopIds);
            //组装成shopId对应shop的shopMap
            if (shops != null && !shops.isEmpty()) {
                Map<Integer, Shop> shopMap = shops.stream()
                        .collect(Collectors.toMap(Shop::getShopId, Function.identity()));
                productListRes.forEach(product -> {
                    Integer productShopId = product.getShopId();
                    if (productShopId != null && productShopId > 0 && shopMap.containsKey(productShopId)) {
                        Shop shop = shopMap.get(productShopId);
                        product.setShop(shop);
                    }
                });
            }
        }

        // 预约商品库存处理：根据预约日历计算未来时间的可用库存总和
        fillReservationProductStock(listDTO, productListRes);


        return PageUtil.convertPage(page, productListRes);
    }

    @Override
    public Page<ProductListResDTO> clientList(ProductListDTO listDTO) {
        return clientList(listDTO, new ProductListDetailContext(), true);
    }

    @Override
    public List<Page<ProductListResDTO>> clientListBatch(List<ProductListDTO> listDTOs) {
        if (CollUtil.isEmpty(listDTOs)) {
            return List.of();
        }
        // 多次列表查询共享同一整页上下文：促销行、realPromotion、赠品、user、配置等只查一次
        ProductListDetailContext sharedCtx = new ProductListDetailContext();
        List<Page<ProductListResDTO>> results = new ArrayList<>(listDTOs.size());
        for (ProductListDTO listDTO : listDTOs) {
            // 批量入口的调用方只消费 records，且 ids 已限定结果范围，无需为每个门店额外执行 COUNT。
            results.add(clientList(listDTO, sharedCtx, false));
        }
        return results;
    }

    private Page<ProductListResDTO> clientList(ProductListDTO listDTO,
                                               ProductListDetailContext listCtx,
                                               boolean searchCount) {
        // 分页查询
        Page<Product> page = new Page<>(listDTO.getPage(), listDTO.getSize(), searchCount);
        // 创建查询构造器
        MPJLambdaWrapper<Product> queryWrapper = new MPJLambdaWrapper<>();

        if (tigshopProperties.getIsO2o() == 1 && listDTO.getShopId() != null && listDTO.getShopId() > 0) {
            queryWrapper.selectAll(Product.class);
            queryWrapper
                    .select("sp2.product_price as storeProductPrice")
                    .select("sp2.product_name as storeProductName")
                    .select("sp2.pic_url as storePicUrl")
                    .select("sp2.product_status as storeProductStatus")
                    .select("sp2.product_stock as storeProductStock")
                    .select("sp2.card_group_id as storeCardGroupId");
        }

        // 排序
        if (ObjectUtil.isNotEmpty(listDTO.getIds())) {
            // ids 不为空按ids排序
            // 防止 SQL 注入：校验 ids 格式，只允许数字和逗号
            String ids = listDTO.getIds();
            if (!ids.matches("^[0-9,]+$")) {
                throw new GlobalException("非法的 ids 参数");
            }
            // 解析并过滤有效的整数ID
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .toList();
            if (idList.isEmpty()) {
                throw new GlobalException("ids 参数为空");
            }
            // 使用安全的方式构建 FIELD 函数
            String safeIds = idList.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            String orderSql = "FIELD(t." + getKeyColumn() + ", " + safeIds + ")";
            queryWrapper.last("ORDER BY " + orderSql);
        } else {
            if (listDTO.getIsClient() != null && listDTO.getIsClient() == 1 && listDTO.getSortField() == null) {
                queryWrapper.orderByAsc(Product::getSortOrder);
                queryWrapper.orderByDesc(Product::getProductId);
            } else {
                buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());
            }
        }
        // 构造查询条件
        buildQueryWrapper(queryWrapper, listDTO);
        // 平台自营(shopId=0)只按 product.shop_id 判定，不能走 copyConditionsWithOr：
        // 该方法会把 shop_id 条件改写到 store_product 上挂进顶层 OR，导致 OR 右支不再约束
        // product.shop_id，从而放行挂在 0 号门店下的他店自建商品。与后台 list() 的处理保持一致。
        boolean platformOnly = listDTO.getShopId() != null && listDTO.getShopId() == 0;
        if (tigshopProperties.getIsO2o() == 1 && !platformOnly) {
            WrapperConditionUtils.copyConditionsWithOr(queryWrapper, null, listDTO.getKeyword());
            queryWrapper.groupBy(Product::getProductId);
        }
        // 查询后的分页数据
        Page<Product> queryPage = this.page(page, queryWrapper);

        if (CollUtil.isEmpty(queryPage.getRecords())) {
            return PageUtil.convertPage(page, List.of());
        }

        // 批量查询门店覆盖主图
        boolean clientNeedStoreConfig = tigshopProperties.getIsO2o() == 1 && listDTO.getShopId() != null && listDTO.getShopId() != 0;
        Map<Integer, String> clientStorePicUrlMapTmp = Map.of();
        final Map<Integer, String> clientStorePicUrlMap = clientStorePicUrlMapTmp;

        queryPage.getRecords().forEach(product -> {
            if (clientNeedStoreConfig && product.getShopId() == 0) {
                ConfigPO storeAssignProductName = cachedConfig(listCtx, SettingsEnum.STORE_ASSIGN_PRODUCT_NAME.getBizCode());
                if ("1".equals(storeAssignProductName.getBizVal())) {
                    product.setProductName(product.getStoreProductName());
                }
                ConfigPO storeAssignProductPrice = cachedConfig(listCtx, SettingsEnum.STORE_ASSIGN_PRODUCT_PRICE.getBizCode());
                if ("1".equals(storeAssignProductPrice.getBizVal())) {
                    product.setProductPrice(product.getStoreProductPrice());
                }
                String clientStorePicUrl = clientStorePicUrlMap.get(product.getProductId());
                if (ObjectUtil.isNotEmpty(clientStorePicUrl)) {
                    product.setPicUrl(clientStorePicUrl);
                    product.setPicThumb(clientStorePicUrl);
                    product.setPicLarge(clientStorePicUrl);
                }
                ConfigPO storeUseSoloProductStock = cachedConfig(listCtx, SettingsEnum.STORE_USE_SOLO_PRODUCT_STOCK.getBizCode());
                ConfigPO storeUseTotalProductStock = cachedConfig(listCtx, SettingsEnum.STORE_USE_TOTAL_PRODUCT_STOCK.getBizCode());
                if ("1".equals(storeUseSoloProductStock.getBizVal()) && "1".equals(storeUseTotalProductStock.getBizVal())) {
                    product.setProductStock(Math.min(product.getProductStock(), product.getStoreProductStock()));
                } else if ("1".equals(storeUseTotalProductStock.getBizVal())) {
                    product.setProductStock(product.getProductStock());
                } else if ("1".equals(storeUseSoloProductStock.getBizVal())) {
                    product.setProductStock(product.getStoreProductStock());
                }
                product.setProductStatus(product.getStoreProductStatus());
                product.setCardGroupId(product.getStoreCardGroupId());
            }
            if (product.getProductType() == PRODUCT_TYPE_CARD.getCode()) {
                product.setProductStock((int) eCardService.count(new LambdaQueryWrapper<ECard>()
                        .eq(ECard::getGroupId, product.getCardGroupId())));
            }
        });
        // 获取商品 ID 列表
        List<Integer> productIds = queryPage.getRecords().stream()
                .map(Product::getProductId)
                .toList();
        // 查询商品规格
        List<ProductSkuDTO> productSkus = productSkuService.getProductSkusByProductIds(productIds);

        // 将商品规格按商品 ID 分组
        Map<Integer, List<ProductSkuDTO>> productSkuMap = productSkus.stream()
                .collect(Collectors.groupingBy(ProductSkuDTO::getProductId));

        listCtx.productSkuMap = productSkuMap;

        // 整页优化上下文：促销缓存 + 积分兑换商品集合（整页一次性查好），避免每条记录重复查询
        ensurePointsExchangeLoaded(listCtx, productIds);

        // 转换为DTO
        List<ProductListResDTO> productListRes = queryPage.getRecords().stream()
                .map(product -> {
                    ProductListResDTO dto = convertToDTO(product);
                    dto.setProductSaleMode(Optional.ofNullable(product.getSaleMode()).orElse(0) >= 1 ? 1 : 0);
                    dto.setIsSeckill(false);
                    dto.setIsTimeDiscount(false);
                    dto.setIsGroupon(false);
                    List<ProductSkuDTO> skus = productSkuMap.get(product.getProductId());
                    dto.setProductSku(skus);
                    dto.setSkuCount(skus == null ? 0 : skus.size());
                    int skuId = 0;
                    if (skus != null && !skus.isEmpty()) {
                        skuId = skus.getFirst().getSkuId();
                    }
                    ProductAvailabilityVO productSkuDetail = this.getProductSkuDetail(product.getProductId(), skuId, 0, "", listDTO.getShopId(), null, listCtx);
                    List<PromotionVO> promotions = productSkuDetail.getPromotion();

                    PromotionVO grouponPromotionVO = null;
                    if (Objects.nonNull(promotions) && !promotions.isEmpty()) {
                        for (PromotionVO promotion : promotions) {
                            Integer type = promotion.getType();
                            if (type == PromotionType.TYPE_SECKILL.getCode()) {
                                //设置秒杀折扣
                                dto.setIsSeckill(true);
                            } else if (type == PromotionType.TYPE_LIMIT_DISCOUNT.getCode()) {
                                //设置显示折扣标识
                                dto.setIsTimeDiscount(true);
                            } else if (type == PromotionType.TYPE_GROUPON.getCode()) {
                                //拼团
                                if (tigshopProperties.getIsO2o() == 1) {
                                    //(现在只能自己门店的拼团才能显示出来)
                                    Integer groupShopId = promotion.getShopId();
                                    Integer shopId = listDTO.getShopId();
                                    if (Objects.equals(groupShopId, shopId) || shopId == null || shopId <= 0) {
                                        dto.setIsGroupon(true);
                                        grouponPromotionVO = promotion;
                                    }
                                } else {
                                    dto.setIsGroupon(true);
                                    grouponPromotionVO = promotion;
                                }
                            }
                        }
                    }
                    if (Boolean.TRUE.equals(dto.getIsGroupon()) && Objects.nonNull(grouponPromotionVO)) {
                        //最低拼团价
                        Map<String, Object> data = grouponPromotionVO.getData();
                        if (Objects.nonNull(data)) {
                            Object o = data.get("item");
                            if (o instanceof GrouponItemInfoVO grouponItemInfoVO) {
                                dto.setPrice(grouponItemInfoVO.getMinGrouponPrice());
                            } else if (o instanceof JSONObject jsonObject && jsonObject.containsKey("minGrouponPrice")) {
                                dto.setPrice(jsonObject.getBigDecimal("minGrouponPrice"));
                            }
                        }
                    } else {
                        dto.setPrice(productSkuDetail.getPrice());
                    }
                    return dto;
                })
                .toList();
        translateProductListNames(productListRes);


        fillGiftCardGroupType(queryPage.getRecords(), productListRes);

        if (CollUtil.isNotEmpty(productIds)) {
            Long now = StringUtils.getCurrentTime();
            //秒杀
            List<SeckillItem> seckillItems = seckillItemMapper.selectList(
                    buildCurrentSeckillItemsQuery(productIds, now, listDTO.getShopId()));

            Map<Integer, SeckillItem> seckillItemMap = Optional.ofNullable(seckillItems)
                    .orElse(new ArrayList<>())
                    .stream()
                    .collect(Collectors.toMap(SeckillItem::getProductId
                            , Function.identity()
                            , (replacement, existing) -> replacement));
            productListRes.forEach(product -> {
                SeckillItem seckillItem = seckillItemMap.get(product.getProductId());
                if (seckillItem != null) {
                    product.setProductPrice(seckillItem.getSeckillPrice());
                }
            });
        }
        //循环获得店铺IDS
        List<Integer> shopIds = queryPage.getRecords().stream()
                .map(Product::getShopId)
                .toList();
        //如果不为空则查询shop获得shopMap
        if (!shopIds.isEmpty()) {
            List<Shop> shops = shopMapper.selectBatchIds(shopIds);
            //组装成shopId对应shop的shopMap
            if (shops != null && !shops.isEmpty()) {
                Map<Integer, Shop> shopMap = shops.stream()
                        .collect(Collectors.toMap(Shop::getShopId, Function.identity()));
                productListRes.forEach(product -> {
                    Integer shopId = product.getShopId();
                    if (shopId != null && shopId > 0 && shopMap.containsKey(shopId)) {
                        Shop shop = shopMap.get(shopId);
                        product.setShop(shop);
                    }
                });
            }
        }

        //填充预约商品库存
        fillReservationProductStock(listDTO, productListRes);

        if (CollUtil.isNotEmpty(productListRes)) {
            backfillFirstWholesalePrice(productListRes, listDTO.getShopId());
        }

        return PageUtil.convertPage(page, productListRes);
    }

    LambdaQueryWrapper<SeckillItem> buildCurrentSeckillItemsQuery(List<Integer> productIds,
                                                                  Long now,
                                                                  Integer shopId) {
        LambdaQueryWrapper<SeckillItem> query = Wrappers.lambdaQuery(SeckillItem.class)
                .in(SeckillItem::getProductId, productIds)
                .le(SeckillItem::getSeckillStartTime, now)
                .ge(SeckillItem::getSeckillEndTime, now);
        if (tigshopProperties.getIsO2o() == 1) {
            int currentShopId = Objects.requireNonNullElse(shopId, 0);
            query.apply(
                    "EXISTS (SELECT 1 FROM seckill s WHERE s.seckill_id = seckill_item.seckill_id AND s.shop_id = {0})",
                    currentShopId);
        }
        return query;
    }


    /**
     * 列表统一补齐礼品卡类型；普通商品明确返回 0，避免不同装配路径出现 null。
     */
    private void fillGiftCardGroupType(List<Product> products, List<ProductListResDTO> productListRes) {
        if (CollUtil.isEmpty(productListRes)) {
            return;
        }
        List<Integer> giftCardGroupsIds = products.stream()
                .filter(product -> product.getProductType() == ProductType.PRODUCT_TYPE_GIFT_CARD.getCode()
                        || product.getProductType() == ProductType.PRODUCT_TYPE_GIFT_CARD_VIRTUAL.getCode())
                .map(Product::getGiftCardGroupsId)
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .distinct()
                .toList();
        Map<Integer, Integer> giftCardGroupTypeMap = giftCardGroupsIds.isEmpty()
                ? Map.of()
                : giftCardGroupsMapper.selectBatchIds(giftCardGroupsIds).stream()
                .collect(Collectors.toMap(GiftCardGroups::getCardGroupsId,
                        GiftCardGroups::getGroupType, (first, ignored) -> first));
        Map<Integer, Integer> groupIdByProductId = products.stream()
                .filter(product -> product.getProductId() != null
                        && product.getGiftCardGroupsId() != null
                        && product.getGiftCardGroupsId() > 0)
                .collect(Collectors.toMap(Product::getProductId,
                        Product::getGiftCardGroupsId, (first, ignored) -> first));
        productListRes.forEach(dto -> {
            boolean giftCard = dto.getProductType() != null
                    && (dto.getProductType() == ProductType.PRODUCT_TYPE_GIFT_CARD.getCode()
                    || dto.getProductType() == ProductType.PRODUCT_TYPE_GIFT_CARD_VIRTUAL.getCode());
            Integer groupId = groupIdByProductId.get(dto.getProductId());
            dto.setGroupType(giftCard && groupId != null
                    ? giftCardGroupTypeMap.getOrDefault(groupId, 0)
                    : 0);
        });
    }

    private void fillReservationProductStock(ProductListDTO listDTO, List<ProductListResDTO> productListRes) {
        if (tigshopProperties.getIsO2o() == 1) {
            // 筛选预约商品
            List<ProductListResDTO> reservationProducts = productListRes.stream()
                    .filter(p -> p.getProductType() != null && ProductType.isReservationProduct(p.getProductType()))
                    .toList();

            if (!reservationProducts.isEmpty()) {
                for (ProductListResDTO product : reservationProducts) {
                    try {
                        List<Integer> calendarShopIds = resolveReservationCalendarShopIds(listDTO, product);
                        List<ProductSkuDTO> productSkuList = product.getProductSku();
                        if (CollUtil.isNotEmpty(productSkuList)) {
                            int totalStock = 0;
                            for (ProductSkuDTO productSku : productSkuList) {
                                int skuStock = calculateReservationSkuStockForListDisplay(product.getProductId(), calendarShopIds, productSku.getSkuId());
                                productSku.setSkuStock(skuStock);
                                totalStock += skuStock;
                            }
                            product.setProductStock(totalStock);
                        } else {
                            product.setProductStock(calculateReservationStockForListDisplay(product.getProductId(), calendarShopIds, null));
                        }
                    } catch (GlobalException e) {
                        // 获取库存日历失败，设置库存为0
                        log.error("获取库存日历失败", e);
                        product.setProductStock(0);
                        if (CollUtil.isNotEmpty(product.getProductSku())) {
                            product.getProductSku().forEach(sku -> sku.setSkuStock(0));
                        }
                    }
                }
            }
        }
    }

    private List<Integer> resolveReservationCalendarShopIds(ProductListDTO listDTO, ProductListResDTO product) {
        LinkedHashSet<Integer> calendarShopIds = new LinkedHashSet<>();
        if (product.getShopId() != null && product.getShopId() > 0) {
            calendarShopIds.add(product.getShopId());
        }
        if (listDTO.getShopId() != null && listDTO.getShopId() > 0) {
            calendarShopIds.add(listDTO.getShopId());
        }
        // 平台商品（shopId=0）且无门店上下文时，查询平台级预约日历
        if (calendarShopIds.isEmpty() && Objects.equals(product.getShopId(), 0)
                && (listDTO.getShopId() == null || listDTO.getShopId() == 0)) {
            calendarShopIds.add(0);
            return new ArrayList<>(calendarShopIds);
        }
        if (!calendarShopIds.isEmpty() || !Objects.equals(product.getShopId(), 0)) {
            return new ArrayList<>(calendarShopIds);
        }


        if (!calendarShopIds.isEmpty()) {
            return new ArrayList<>(calendarShopIds);
        }


        return new ArrayList<>(calendarShopIds);
    }

    private int calculateReservationSkuStockForListDisplay(Integer productId, List<Integer> shopIds, Integer skuId) {
        return calculateReservationStockForListDisplay(productId, shopIds, skuId);
    }

    private int calculateReservationStockForListDisplay(Integer productId, List<Integer> shopIds, Integer skuId) {
        int result = 0;
        return result;
    }

    @Override
    public List<ProductDescImageVO> getDescImages(Integer productId) {
        Product product = this.getById(productId);
        if (product == null || StrUtil.isBlank(product.getProductDesc())) {
            return Collections.emptyList();
        }
        List<ProductDescImageVO> result = new ArrayList<>();
        for (ProductDescDTO item : getProductDescArr(product.getProductDesc())) {
            if ("pic".equals(item.getType()) && StrUtil.isNotBlank(item.getPic())) {
                result.add(new ProductDescImageVO("pic", item.getPic()));
            } else if ("video".equals(item.getType()) && StrUtil.isNotBlank(item.getCover())) {
                result.add(new ProductDescImageVO("video", item.getCover()));
            }
        }
        return result;
    }

    @Override
    public ProductDetailDTO detail(Integer productId) {
        ProductDetailDTO productDetailDTO = new ProductDetailDTO();
        Product product = this.getById(productId);
        if (product == null) {
            return null;
        }
        product.setClickCount(product.getClickCount() + 1);
        this.updateById(product);
        BeanUtils.copyProperties(product, productDetailDTO);
        if (StrUtil.isNotBlank(product.getPaidContent())) {
            productDetailDTO.setPaidContent(JsonUtil.checkJsonType(product.getPaidContent()));
        }

        // 商品详情，需要拆分并获取里面的图片
        String productDesc = product.getProductDesc();
        productDetailDTO.setProductDescArr(getProductDescArr(productDesc));

        // 商品关联商品，需要转成集合
        String productRelated = product.getProductRelated();
        productDetailDTO.setProductRelated(StringUtils.str2IntList(productRelated));

        // 商品服务，需要转成集合
        String productServiceIds = product.getProductServiceIds();
        productDetailDTO.setProductServiceIds(StringUtils.str2IntList(productServiceIds));

        // 商品相册
        List<ProductGallery> productGalleryList = productGalleryService.getProductGalleryList(productId);
        productDetailDTO.setImgList(productGalleryList);

        List<ProductVideo> productVideo = productVideoService.lambdaQuery().eq(ProductVideo::getProductId, productId).list();
        productDetailDTO.setProductVideoInfo(productVideo);
        // 商品规格
        ProductAttrsDTO productAttributes = productAttributesService.getProductAttributes(productId);
        productDetailDTO.setAttrList(productAttributes);

        // 商品文章
        List<ProductArticle> productArticleByProductId = productArticleService.getProductArticleByProductId(productId);
        productDetailDTO.setProductArticleList(productArticleByProductId.stream()
                .map(ProductArticle::getArticleId)
                .toList());

        // 构建商品列表属性
        productDetailDTO.setProductList(productSkuService.getProductSkusByPid(productId, true));
        productDetailDTO.setSkuType(resolveSkuType(productDetailDTO.getAttrList(), productDetailDTO.getProductList()));
        // SKU 总数：供前端按阈值判断是否切换分页编辑模式（避免为拿数量而拉全量）
        productDetailDTO.setSkuCount(productDetailDTO.getProductList() == null ? 0 : productDetailDTO.getProductList().size());



        // 查询预售信息
        if (product.getSaleMode() != null && product.getSaleMode() > 0) {
            ProductPresaleVO presaleInfo = productPresaleService.getPresaleVOByProductId(productId);
            productDetailDTO.setPresaleInfo(presaleInfo);
        }
        Integer shopId = getShopId();

        //拿到最后一个任务进行回显
        // (平台看店铺和门店商品的自动上下架数据,商品和当前环境的shopId是不同的),然后自己看自己的商品的话
        Integer productShopId = product.getShopId();
        ProductAutoStatus productAutoStatus = productAutoStatusService.lambdaQuery()
                .eq(ProductAutoStatus::getProductId, productId)
                .eq(ProductAutoStatus::getShopId, getShopId() != productShopId ? productShopId : getShopId())
                .last("limit 1")
                .one();
        if (productAutoStatus != null) {
            if (ProductOperationTypeEnum.ONLINE.getCode().equals(productAutoStatus.getOperationType())) {
                // 自动上架任务，将时间戳转换为字符串
                productDetailDTO.setAutoOnlineTime(DateUtil.format(
                        DateUtil.date(productAutoStatus.getScheduledTime() * 1000), "yyyy-MM-dd HH:mm:ss"));
                productDetailDTO.setAutoOnlineTaskId(productAutoStatus.getId());
                productDetailDTO.setAutoOnlineTaskStatus(productAutoStatus.getStatus());
            } else if (ProductOperationTypeEnum.OFFLINE.getCode().equals(productAutoStatus.getOperationType())) {
                // 自动下架任务，将时间戳转换为字符串
                productDetailDTO.setAutoOfflineTime(DateUtil.format(
                        DateUtil.date(productAutoStatus.getScheduledTime() * 1000), "yyyy-MM-dd HH:mm:ss"));
                productDetailDTO.setAutoOfflineTaskId(productAutoStatus.getId());
                productDetailDTO.setAutoOfflineTaskStatus(productAutoStatus.getStatus());
            }
            productDetailDTO.setAutoTaskErrorMsg(productAutoStatus.getErrorMessage());
        }
        // 只要存在自动上架或下架时间，即认为开启了定时上下架
        productDetailDTO.setTimedOffShelf(
                (StrUtil.isNotBlank(productDetailDTO.getAutoOnlineTime())
                        || StrUtil.isNotBlank(productDetailDTO.getAutoOfflineTime())) ? 1 : 0);


        productDetailDTO.setProductSaleMode(product.getSaleMode() > 0 ? 1 : 0);
        productDetailDTO.setProductSaleMode(Optional.ofNullable(product.getSaleMode()).orElse(0) > 0 ? 1 : 0);


        // O2O 服务费上下文 + 角色感知 platform/store fee 字段（CR-20260513-160000）
        // 替代已删除的 /storeSkuServiceFee/detail 和 /storeSkuServerServiceFee/detail，
        // 让前端编辑表单从 product/detail 一次性拿齐：总开关 + 独立模式开关 + 可编辑判定 + 平台默认值 + 门店覆盖值
        if (tigshopProperties.getIsO2o() == 1) {
            GetStoreVO feeSettings = null;
            try {
                feeSettings = configService.getSettings(GetStoreVO.class);
            } catch (Exception e) {
                log.warn("读取服务费配置失败，跳过 detail fee 上下文填充: err={}", e.getMessage());
            }
            if (feeSettings != null) {
                productDetailDTO.setDeliveryServiceFeeEnabled(feeSettings.getDeliveryServiceFeeEnabled());
                productDetailDTO.setServerServiceFeeEnabled(feeSettings.getServerServiceFeeEnabled());
                productDetailDTO.setStoreAssignDeliveryServiceFee(feeSettings.getStoreAssignDeliveryServiceFee());
                productDetailDTO.setStoreAssignServerServiceFee(feeSettings.getStoreAssignServerServiceFee());

                String adminTypeStr = HeaderUtils.getAdminType();
                AdminTypeEnum adminType = adminTypeStr != null ? AdminTypeEnum.fromCode(adminTypeStr) : null;
                boolean deliveryEnabled = feeSettings.getDeliveryServiceFeeEnabled() != null
                        && feeSettings.getDeliveryServiceFeeEnabled() == 1;
                boolean serverEnabled = feeSettings.getServerServiceFeeEnabled() != null
                        && feeSettings.getServerServiceFeeEnabled() == 1;
                if (adminType == AdminTypeEnum.ADMIN) {
                    productDetailDTO.setDeliveryFeeEditable(deliveryEnabled);
                    productDetailDTO.setServerFeeEditable(serverEnabled);
                } else if (adminType != null) {
                    // storeAssign* 门禁（分配商品配送/服务费规则）语义上只对"平台分配商品"(shopId==0)生效；
                    // 门店自建商品(shopId==本店)不受该门禁约束，仅受服务费总开关约束，否则会被误锁为不可编辑。
                    boolean isAssignedProduct = product.getShopId() != null && product.getShopId() == 0;
                    boolean deliveryStoreIndependent = feeSettings.getStoreAssignDeliveryServiceFee() != null
                            && feeSettings.getStoreAssignDeliveryServiceFee() == 1;
                    boolean serverStoreIndependent = feeSettings.getStoreAssignServerServiceFee() != null
                            && feeSettings.getStoreAssignServerServiceFee() == 1;
                    productDetailDTO.setDeliveryFeeEditable(deliveryEnabled && (!isAssignedProduct || deliveryStoreIndependent));
                    productDetailDTO.setServerFeeEditable(serverEnabled && (!isAssignedProduct || serverStoreIndependent));
                }

                if (ObjectUtil.isNotEmpty(productDetailDTO.getProductList())) {
                    // 多规格：platform*Fee*：所有角色都填充（语义=平台层默认值，与 productList[i].delivery|server*ServiceFee* 等价镜像）
                    productDetailDTO.getProductList().forEach(skuDTO -> {
                        skuDTO.setPlatformDeliveryServiceFeeType(skuDTO.getDeliveryServiceFeeType());
                        skuDTO.setPlatformDeliveryServiceFeeValue(skuDTO.getDeliveryServiceFeeValue());
                        skuDTO.setPlatformServerServiceFeeType(skuDTO.getServerServiceFeeType());
                        skuDTO.setPlatformServerServiceFeeValue(skuDTO.getServerServiceFeeValue());
                    });

                } else {
                    // 5.8.32 单规格：填充 ProductDetailDTO 顶层 platform/store 各 4 字段
                    productDetailDTO.setPlatformDeliveryServiceFeeType(product.getDeliveryServiceFeeType());
                    productDetailDTO.setPlatformDeliveryServiceFeeValue(product.getDeliveryServiceFeeValue());
                    productDetailDTO.setPlatformServerServiceFeeType(product.getServerServiceFeeType());
                    productDetailDTO.setPlatformServerServiceFeeValue(product.getServerServiceFeeValue());

                }
            }
        }

        return productDetailDTO;
    }


    @Override
    public ProductConfigVO config(Integer shopId) {
        List<ShippingTpl> shippingTplList;
        if (tigshopProperties.getIsO2o() == 1 && shopId != null && shopId > 0) {
            shippingTplList = shippingTplService.lambdaQuery()
                    .in(ShippingTpl::getShopId, List.of(0, shopId))
                    .list();
        } else {
            shippingTplList = shippingTplService.lambdaQuery()
                    .eq(ShippingTpl::getShopId, shopId)
                    .list();
        }
        List<Suppliers> suppliersList = new java.util.ArrayList<>();
        List<Services> servicesList = servicesService.list();
        List<Integer> servicesIdList = servicesList.stream()
                .map(Services::getProductServiceId)
                .toList();
        List<UserRank> userRankList = userRankService.list();
        List<ECardGroup> eCardGroups = ecardGroupService.lambdaQuery()
                .eq(ECardGroup::getShopId, shopId)
                .eq(ECardGroup::getIsUse, Constants.YES).list();

        ProductConfigVO configVO = new ProductConfigVO(shippingTplList, suppliersList, servicesList, userRankList, servicesIdList, eCardGroups);
        // 商品编辑库存是否可修改开关（默认 1-可修改），供编辑页做库存输入只读联动
        String editStockModify = configService.getConfigVal(SettingsEnum.PRODUCT_EDIT_STOCK_MODIFY);
        configVO.setProductEditStockModify("0".equals(editStockModify) ? 0 : 1);
        return configVO;
    }

    /**
     * 将 ProductSkuDTO 转换为 ProductSku
     */
    public List<ProductSku> convertToProductSku(List<ProductSkuDTO> productSkusDTO) {
        return productSkusDTO.stream()
                .map(this::mapToProductSku)
                .collect(Collectors.toList());
    }

    /**
     * 将 ProductListAttributeDTO 转换为 ProductSkuDTO
     */
    private List<ProductSkuDTO> convertToProductSkuDTO(List<ProductListAttributeDTO> productList, Integer productId) {
        return productList.stream()
                .map(productListAttributeDTO -> {
                    ProductSkuDTO productSkuDTO = new ProductSkuDTO();
                    BeanUtils.copyProperties(productListAttributeDTO, productSkuDTO);

                    List<ProductListAttributeDTO.Attribute> attrs = productListAttributeDTO.getAttrs();
                    if (attrs != null && !attrs.isEmpty()) {
                        // 转换 attrs -> skuData
                        List<ProductSkuDTO.SkuData> skuDataList = attrs.stream()
                                .map(attr -> new ProductSkuDTO.SkuData(attr.getAttrName(), attr.getAttrValue()))
                                .collect(Collectors.toList());

                        productSkuDTO.setSkuData(skuDataList);
                    }
                    productSkuDTO.setProductId(productId);
                    return productSkuDTO;
                })
                .collect(Collectors.toList());
    }

    /**
     * 将单个 ProductSkuDTO 转换为 ProductSku
     */
    private ProductSku mapToProductSku(ProductSkuDTO dto) {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductSku productSku = new ProductSku();
        BeanUtils.copyProperties(dto, productSku);
        String skuPrice = dto.getSkuPrice();
        if (StrUtil.isNotEmpty(skuPrice)) {
            productSku.setSkuPrice(new BigDecimal(skuPrice));
        }

        // 将 SkuData 转换为 JSON 字符串
        try {
            if (dto.getSkuData() != null) {
                productSku.setSkuData(objectMapper.writeValueAsString(dto.getSkuData()));
            }
        } catch (JsonProcessingException e) {
            throw new GlobalException(SERVICE_ERROR);
        }

        return productSku;
    }

    /**
     * 商品编辑库存不可改时的库存守卫（仅作用于编辑路径，新增商品不受限）。
     * <p>
     * 语义：不报错，其余字段照常保存，仅“已有规格的库存”不允许经编辑修改。
     * - 单规格：商品总库存 productStock 还原为库中原值；
     * - 多规格：已存在 sku（skuId 命中库中）skuStock 还原为库中原值；
     *   新增规格（skuId 为空/不命中库中）保留入参库存（即新增时用户填写的库存）；
     *   商品总库存按“调整后各启用 sku 库存之和”重算，保持与 sku 一致。
     */
    private void restoreStockForEdit(ProductSaveParam param, Product dbProduct) {
        List<ProductListAttributeDTO> productList = param.getProductList();
        if (CollUtil.isEmpty(productList)) {
            // 单规格：库存字段还原库中原值
            param.setProductStock(dbProduct.getProductStock());
            return;
        }
        Map<Integer, Integer> dbSkuStockMap = productSkuService.lambdaQuery()
                .eq(ProductSku::getProductId, dbProduct.getProductId())
                .list()
                .stream()
                .collect(Collectors.toMap(ProductSku::getSkuId, ProductSku::getSkuStock));
        for (ProductListAttributeDTO item : productList) {
            if (item.getSkuId() != null && dbSkuStockMap.containsKey(item.getSkuId())) {
                // 已有规格：库存还原为库中原值（其余字段照常保存）
                item.setSkuStock(dbSkuStockMap.get(item.getSkuId()));
            }
            // 新增规格（skuId 为空/不命中）：保留入参库存，不做处理
        }
        // 总库存按调整后各 sku 库存之和重算，保持一致
        int sum = productList.stream()
                .filter(item -> !Objects.equals(item.getIsEnabled(), Constants.NO))
                .mapToInt(item -> item.getSkuStock() == null ? 0 : item.getSkuStock())
                .sum();
        param.setProductStock(sum);
    }

    static void normalizeAndValidateSkuStatus(List<ProductListAttributeDTO> productList) {
        if (CollUtil.isEmpty(productList)) {
            return;
        }
        productList.forEach(item -> item.setIsEnabled(normalizeSkuStatus(item.getIsEnabled())));
        if (productList.stream().noneMatch(item -> Objects.equals(item.getIsEnabled(), Constants.YES))) {
            throw new GlobalException("请至少启用一个SKU");
        }
    }

    private static void applyEnabledSkuSummary(ProductSaveParam param) {
        List<ProductListAttributeDTO> productList = param.getProductList();
        if (CollUtil.isEmpty(productList)) {
            return;
        }
        List<ProductListAttributeDTO> enabledSkus = productList.stream()
                .filter(item -> Objects.equals(item.getIsEnabled(), Constants.YES))
                .toList();
        param.setProductStock(enabledSkus.stream()
                .map(ProductListAttributeDTO::getSkuStock)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum());
        param.setProductPrice(enabledSkus.stream()
                .map(ProductListAttributeDTO::getSkuPrice)
                .filter(StrUtil::isNotBlank)
                .map(BigDecimal::new)
                .min(BigDecimal::compareTo)
                .orElseThrow(() -> new GlobalException("商品价格不能为空")));
    }

    private static Integer normalizeSkuStatus(Integer isEnabled) {
        if (isEnabled == null) {
            return Constants.YES;
        }
        if (!Objects.equals(isEnabled, Constants.NO) && !Objects.equals(isEnabled, Constants.YES)) {
            throw new GlobalException("规格状态不正确");
        }
        return isEnabled;
    }

    static ProductSku prepareSkuForUpdate(ProductSku submittedSku, ProductSku dbSku) {
        Integer targetStatus = normalizeSkuStatus(submittedSku.getIsEnabled());
        Integer dbStatus = normalizeSkuStatus(dbSku.getIsEnabled());
        if (Objects.equals(dbStatus, Constants.NO) || !Objects.equals(targetStatus, dbStatus)) {
            ProductSku preservedSku = BeanUtil.copyProperties(dbSku, ProductSku.class);
            preservedSku.setIsEnabled(targetStatus);
            return preservedSku;
        }
        submittedSku.setIsEnabled(targetStatus);
        return submittedSku;
    }

    static void applyCopiedSkuStatus(List<ProductSku> productSkus,
                                     List<ProductListAttributeDTO> submittedSkus) {
        Map<Integer, Integer> submittedStatusMap = CollUtil.isEmpty(submittedSkus)
                ? Collections.emptyMap()
                : submittedSkus.stream()
                        .filter(item -> item.getSkuId() != null)
                        .collect(Collectors.toMap(ProductSkuDTO::getSkuId,
                                item -> normalizeSkuStatus(item.getIsEnabled()), (first, second) -> second));
        productSkus.forEach(item -> item.setIsEnabled(submittedStatusMap.containsKey(item.getSkuId())
                ? submittedStatusMap.get(item.getSkuId())
                : normalizeSkuStatus(item.getIsEnabled())));
        if (CollUtil.isNotEmpty(productSkus)
                && productSkus.stream().noneMatch(item -> Objects.equals(item.getIsEnabled(), Constants.YES))) {
            throw new GlobalException("请至少启用一个SKU");
        }
    }

    /**
     * 保存商品规格信息
     */
    @Transactional(rollbackFor = Exception.class)
    protected void saveProductSku(List<ProductListAttributeDTO> productList, Product product) {
        Integer productId = product.getProductId();

        if (CollUtil.isEmpty(productList)) {
            productSkuService.lambdaUpdate()
                    .eq(ProductSku::getProductId, productId)
                    .eq(ProductSku::getIsEnabled, Constants.YES)
                    .remove();
            return;
        }

        // 转换为 ProductSkuDTO 列表
        List<ProductSkuDTO> productSkusDTO = convertToProductSkuDTO(productList, productId);

        // 转换为 ProductSku 列表
        List<ProductSku> productSkus = convertToProductSku(productSkusDTO);

        // G2: 平台 SKU 写入策略校验（*ServiceFeeEnabled = 0 时禁止携带服务费值）

        List<Integer> currentSkuIds = productSkus.stream()
                .map(ProductSku::getSkuId)
                .filter(Objects::nonNull)
                .toList();

        List<ProductSku> dbProductSkus = productSkuService.lambdaQuery().eq(ProductSku::getProductId, productId).list();
        Map<Integer, ProductSku> dbSkuMap = dbProductSkus.stream().collect(Collectors.toMap(ProductSku::getSkuId, Function.identity()));
        Set<String> disabledSkuValues = dbProductSkus.stream()
                .filter(item -> Objects.equals(item.getIsEnabled(), Constants.NO))
                .map(ProductSku::getSkuValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<ProductInventoryLog> operationLogs = new ArrayList<>();

        // 新增 sku
        List<ProductSku> addSkus = productSkus.stream()
                .filter(item -> item.getSkuId() == null)
                .map(item -> BeanUtil.copyProperties(item, ProductSku.class))
                .toList();
        if (CollUtil.isNotEmpty(addSkus)) {
            boolean disabledSkuExists = addSkus.stream()
                    .map(ProductSku::getSkuValue)
                    .anyMatch(disabledSkuValues::contains);
            if (disabledSkuExists) {
                throw new GlobalException("该规格已存在，请重新启用");
            }
            productSkuService.saveBatch(addSkus);
            // 为空的 skuSn 自动生成编码：productSn + "-" + skuId
            List<ProductSku> emptySnSkus = addSkus.stream()
                    .filter(item -> StrUtil.isEmpty(item.getSkuSn()))
                    .toList();
            if (CollUtil.isNotEmpty(emptySnSkus)) {
                String currentProductSn = this.lambdaQuery()
                        .select(Product::getProductSn)
                        .eq(Product::getProductId, productId)
                        .one().getProductSn();
                if (StrUtil.isNotEmpty(currentProductSn)) {
                    emptySnSkus.forEach(item -> item.setSkuSn(currentProductSn + "-" + item.getSkuId()));
                    productSkuService.updateBatchById(emptySnSkus);
                }
            }
            // 创建 sku 库存日志
            List<ProductInventoryLog> addLogs = addSkus.stream()
                    .map(item -> ProductInventoryLog.builder()
                            .productId(item.getProductId())
                            .specId(item.getSkuId())
                            .number(item.getSkuStock())
                            .addTime(StringUtils.getCurrentTime())
                            .oldNumber(0)
                            .type(1)
                            .changeNumber(item.getSkuStock())
                            .desc("新增产品规格")
                            .shopId(product.getShopId())
                            .build()
                    ).toList();
            operationLogs.addAll(addLogs);
        }

        // 删除 sku
        List<ProductSku> delSkus = dbProductSkus.stream()
                .filter(item -> !Objects.equals(item.getIsEnabled(), Constants.NO))
                .filter(item -> !currentSkuIds.contains(item.getSkuId()))
                .toList();
        if (CollUtil.isNotEmpty(delSkus)) {
            productSkuService.removeByIds(delSkus);
            // 创建 sku 库存日志
            List<ProductInventoryLog> delLogs = delSkus.stream()
                    .map(item -> ProductInventoryLog.builder()
                            .productId(item.getProductId())
                            .specId(item.getSkuId())
                            .number(0)
                            .addTime(StringUtils.getCurrentTime())
                            .oldNumber(item.getSkuStock())
                            .type(1)
                            .changeNumber(Math.negateExact(item.getSkuStock()))
                            .desc("删除产品规格")
                            .shopId(product.getShopId())
                            .build()
                    ).toList();
            operationLogs.addAll(delLogs);
        }

        // 编辑 sku
        List<ProductSku> editSkus = productSkus.stream()
                .filter(item -> item.getSkuId() != null)
                .map(item -> {
                    ProductSku dbSku = dbSkuMap.get(item.getSkuId());
                    if (dbSku == null) {
                        throw new GlobalException("商品规格不存在");
                    }
                    return prepareSkuForUpdate(item, dbSku);
                })
                .toList();
        if (CollUtil.isNotEmpty(editSkus)) {
            productSkuService.updateBatchById(editSkus);
            // 创建 sku 库存日志
            List<ProductInventoryLog> editLogs = editSkus.stream()
                    .map(item -> {
                                ProductSku dbSku = dbSkuMap.get(item.getSkuId());
                                int changeNumber = item.getSkuStock() - dbSku.getSkuStock();

                                return ProductInventoryLog.builder()
                                        .productId(item.getProductId())
                                        .specId(item.getSkuId())
                                        .number(item.getSkuStock())
                                        .addTime(StringUtils.getCurrentTime())
                                        .oldNumber(dbSku.getSkuStock())
                                        .type(1)
                                        .changeNumber(changeNumber)
                                        .desc("删除产品规格")
                                        .shopId(product.getShopId())
                                        .build();
                            }
                    ).toList();
            operationLogs.addAll(editLogs);
        }

        // 新增库存日志
        if (CollUtil.isNotEmpty(operationLogs)) {
            productInventoryLogService.saveBatch(operationLogs);
        }

        List<ProductSku> savedSkus = productSkuService.lambdaQuery()
                .eq(ProductSku::getProductId, productId)
                .orderByAsc(ProductSku::getSkuId)
                .list();
        updateProductSkuSummary(product, savedSkus);
        // 平台商品从单规格变为多规格后，补齐已有 O2O 门店的 SKU 分配。
        // 仅平台管理员编辑平台商品时执行；门店编辑已有分配商品仍走上面的门店覆盖逻辑。

        // 历史供应商商品绑定单规格时 sourceSkuId=0；来源商品首次变为多规格后，
        // 将该历史绑定稳定迁移到第一条平台 SKU，避免采购单继续按无规格入库。
    }

    private void updateProductSkuSummary(Product product, List<ProductSku> productSkus) {
        List<ProductSku> enabledSkus = productSkus.stream()
                .filter(item -> Objects.equals(normalizeSkuStatus(item.getIsEnabled()), Constants.YES))
                .toList();
        if (enabledSkus.isEmpty()) {
            throw new GlobalException("请至少启用一个SKU");
        }
        int productStock = ProductType.isReservationProduct(product.getProductType())
                ? PRODUCT_STOCK_MAX_LIMIT
                : enabledSkus.stream()
                        .map(ProductSku::getSkuStock)
                        .filter(Objects::nonNull)
                        .mapToInt(Integer::intValue)
                        .sum();
        BigDecimal productPrice = enabledSkus.stream()
                .map(ProductSku::getSkuPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElseThrow(() -> new GlobalException("商品价格不能为空"));

        product.setProductStock(productStock);
        product.setProductPrice(productPrice);
        Product summary = new Product();
        summary.setProductId(product.getProductId());
        summary.setProductStock(productStock);
        summary.setProductPrice(productPrice);
        this.updateById(summary);
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(ProductSaveParam param) {
        normalizeAndValidateSkuStatus(param.getProductList());
        applyEnabledSkuSummary(param);
        if (ProductType.isReservationProduct(param.getProductType())) {
            param.setProductStock(PRODUCT_STOCK_MAX_LIMIT);
        }
        Product product = new Product();
        BeanUtils.copyProperties(param, product);

        if (CollUtil.isNotEmpty(param.getPaidContent())) {
            product.setPaidContent(param.getPaidContent().toJSONString(0));
        }

        // 校验商品名称长度
        checkProductNameLength(param.getProductName());

        // 校验商品价格
        if (!ProductType.isReservationProduct(product.getProductType())) {
            checkProductPrice(param.getProductPrice());
        }

        // 校验商品货号唯一
        checkProductSn(param.getProductSn());

        // 构建商品对象
        buildProduct(param, product);

        // 保存商品
        if (ProductType.isReservationProduct(param.getProductType())) {
            product.setProductStock(PRODUCT_STOCK_MAX_LIMIT);
        }

        ProductPresaleDTO presaleConfig = param.getPresaleConfig();
        if (Objects.nonNull(presaleConfig) && presaleConfig.getSaleMode() != null) {
            param.setSaleMode(param.getPresaleConfig().getSaleMode());
        } else {
            param.setSaleMode(0);
        }
        product.setSaleMode(param.getSaleMode());

        // 5.8.32 单规格商品服务费校验 + 多规格清空 product 顶层 fee（避免数据污染）
        applyProductLevelServiceFee(param, product);

        // 保证金准入：欠缴被限制（限上新/全店下架）时禁止店铺创建即上架商品（目标为在售时阻断，不吞异常）
        if (product.getProductStatus() != null && product.getProductStatus() == 1
                && product.getShopId() != null && product.getShopId() > 0) {
            depositLifecycleService.assertOnShelfAllowed(
                    com.tigshop.bean.enums.deposit.DepositOwnerType.SHOP.getCode(),
                    product.getShopId());
        }

        boolean isSaved = this.save(product);

        if (isSaved) {
            if (Objects.equals(CheckStatus.PENDING.getCode(), product.getCheckStatus())) {
                notifyProductPendingAudit(product);
            }
            // 处理预售信息
            if (param.getSaleMode() != null && param.getSaleMode() > 0) {
                // 校验预售配置
                productPresaleService.validatePresaleConfig(param.getSaleMode(), param.getPresaleConfig(), param.getProductPrice());
                // 创建额外校验,定金预售的尾款支付时间和发货时间截止日期建议保存时需判断大于当前时间
                if (param.getSaleMode() != null && param.getSaleMode() == 2 && presaleConfig != null) {
                    Long currentTime = StringUtils.getCurrentTime();
                    presaleConfig = param.getPresaleConfig();
                    if (presaleConfig.getFinalPaymentStartTimeStamp() != null && presaleConfig.getFinalPaymentEndTimeStamp() < currentTime) {
                        throw new GlobalException("尾款支付时间截止日期不能小于当前时间");
                    }
                    if (presaleConfig.getDeliveryDateTimestamp() != null && presaleConfig.getDeliveryDateTimestamp() < currentTime) {
                        throw new GlobalException("发货时间不能小于当前时间");
                    }
                }
                // 保存预售信息
                productPresaleService.saveOrUpdate(product.getProductId(), param.getPresaleConfig(), param.getProductPrice());
            } else {
                productPresaleService.deleteByProductId(product.getProductId());
            }

            // 其他相关更新（包含SKU保存）
            applicationContext.getBean(ProductService.class).otherUpdates(param, product.getProductSn(), product, param.getImgList());


            // 增加库存编辑日志
            if (CollUtil.isEmpty(param.getProductList())) {
                ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                        .productId(product.getProductId())
                        .specId(0)
                        .number(product.getProductStock())
                        .addTime(StringUtils.getCurrentTime())
                        .oldNumber(0)
                        .type(1)
                        .changeNumber(product.getProductStock())
                        .desc("库存新增")
                        .shopId(product.getShopId())
                        .build();
                productInventoryLogService.save(productInventoryLog);
            }

            // 处理自动上下架任务
            if (StrUtil.isNotBlank(param.getAutoOnlineTime()) || StrUtil.isNotBlank(param.getAutoOfflineTime())) {
                this.handleAutoStatusTasks(product, param.getAutoOnlineTime(), param.getAutoOfflineTime());
            }
        }
        if (!isSaved) {
            return "商品更新失败";
        }

        // 异步同步到ES
        try {
            productEsSyncService.syncProductAsync(Long.valueOf(product.getProductId()));
        } catch (Exception e) {
            log.error("商品创建后ES同步失败: {}", product.getProductId(), e);
        }

        Integer shopId = HeaderUtils.getShopId();
        if (shopId != null && shopId > 0) {
            String shopProductNeedCheck = configService.getConfigByCode("shopProductNeedCheck").getBizVal();
            if ("1".equals(shopProductNeedCheck)) {
                saveProductAuditPendingMessage(product);
                return "商品已添加，等待管理员审核";
            }
        }

        // 保证金收取节点：创建即上架（免审）触发收取缓冲（仅店铺商品，非阻断）
        if (product.getProductStatus() != null && product.getProductStatus() == 1
                && product.getShopId() != null && product.getShopId() > 0) {
            try {
                depositLifecycleService.onCollectTrigger(
                        com.tigshop.bean.enums.deposit.DepositOwnerType.SHOP.getCode(),
                        product.getShopId(),
                        com.tigshop.bean.enums.deposit.DepositCollectNode.ON_SHELF.getCode());
            } catch (Exception ignore) {
            }
        }
        // 保证金：创建即上架导致经营类目变化，记录应缴调整（非阻断）
        if (product.getProductStatus() != null && product.getProductStatus() == 1
                && product.getShopId() != null && product.getShopId() > 0) {
            try {
                depositLifecycleService.onCategoryChange(
                        com.tigshop.bean.enums.deposit.DepositOwnerType.SHOP.getCode(),
                        product.getShopId());
            } catch (Exception ignore) {
            }
        }

        return "商品已添加";
    }

    /**
     * 构建商品对象
     */
    private void buildProduct(ProductSaveParam param, Product product) {
        if (!SeoIdentificationUtil.isValidSeoIdentification(param.getSeoIdentification())) {
            throw new GlobalException("请输入正确的SEO标识");
        }
        List<ProductListAttributeDTO> productList = param.getProductList();
        if (CollUtil.isNotEmpty(productList)) {
            applyEnabledSkuSummary(param);
            Assert.isTrue(ProductType.isReservationProduct(param.getProductType())
                    || param.getProductStock() > 0, "商品总库存必须大于0");
            product.setProductStock(param.getProductStock());
            product.setProductPrice(param.getProductPrice());
        }

        // 商品关键字
        String keywords = param.getKeywords();
        if (StrUtil.isNotEmpty(keywords)) {
            // 分词
            keywords = StringUtils.cutForSearch(keywords);
            product.setKeywords(keywords);
        }

        // 关联产品
        List<Integer> productRelated = param.getProductRelated();
        if (productRelated != null && !productRelated.isEmpty()) {
            product.setProductRelated(productRelated.toString());
        }

        // 商品服务
        List<Integer> productServiceIds = param.getProductServiceIds();
        if (productServiceIds != null && !productServiceIds.isEmpty()) {
            product.setProductServiceIds(productServiceIds.toString());
        }

        // 商品描述
        String productDesc = ProductDescDTO.getProductDesc(param.getProductDescArr());
        if (StrUtil.isNotEmpty(productDesc)) {
            product.setProductDesc(productDesc);
        }

        // 商品图片
        List<GalleryPic> imgList = param.getImgList();
        if (imgList != null && !imgList.isEmpty()) {
            setImgList(imgList, product);
        }

        // 商家后台情况下,新增情况下才需要审核
        if (product.getProductId() == null) {
            checkShopId(product);
        }

        // 商品序号
        product.setProductSn(param.getProductSn());
        // 添加时间
        product.setAddTime(StringUtils.getCurrentTime());

        //属性模板id
        product.setAttributesTplId(param.getAttributesTplId());

        // 交付模式（周期购）
        if (param.getDeliveryMode() != null) {
            product.setDeliveryMode(param.getDeliveryMode());
        }

        syncFreeShippingWithShippingType(product);
    }

    /**
     * free_shipping 由运费方式推导，避免前端回传的历史 free_shipping=1 与运费模板并存：
     * 结算时 free_shipping=1 会直接跳过运费计算，导致运费模板/固定运费不生效。
     * 与批量导入 {@code ProductBatchServiceImpl#applyShippingTpl} 的语义保持一致。
     */
    private void syncFreeShippingWithShippingType(Product product) {
        Integer fixedShippingType = product.getFixedShippingType();
        if (fixedShippingType == null) {
            return;
        }
        if (fixedShippingType == 2) {
            product.setFreeShipping(0);
        } else if (fixedShippingType == 1) {
            BigDecimal fee = product.getFixedShippingFee();
            boolean isFree = fee == null || fee.compareTo(BigDecimal.ZERO) == 0;
            product.setFreeShipping(isFree ? 1 : 0);
        }
    }

    /**
     * 设置商品图片
     */
    private void setImgList(List<GalleryPic> imgList, Product product) {
        if (imgList != null && !imgList.isEmpty()) {
            String picUrl = imgList.getFirst().getPicUrl();
            product.setPicOriginal(picUrl);
            product.setPicThumb(imgList.getFirst().getPicThumb());
            if (product.getProductId() != null) {
                product.setPicUrl(imgList.getFirst().getPicUrl());
                product.setPicLarge(StrUtil.format("{}{}", picUrl, galleryPicService.getImageSize().get("size_800")));
            } else {
                product.setPicUrl(StrUtil.format("{}{}", picUrl, galleryPicService.getImageSize().get("size_400")));
                product.setPicLarge(StrUtil.format("{}{}", picUrl, galleryPicService.getImageSize().get("size_800")));
            }

        }
    }

    /**
     * 商家后台情况下，设置商品状态和审核状态
     */
    private void checkShopId(Product product) {
        if (getShopId() != null && getShopId() > 0) {
            String shopProductNeedCheckVal = configService.getConfigByCode(SettingsEnum.SHOP_PRODUCT_NEED_CHECK.getBizCode()).getBizVal();
            int shopProductNeedCheck = Integer.parseInt(shopProductNeedCheckVal);
            // 商家后台需要审核
            if (shopProductNeedCheck == 1) {
                product.setProductStatus(ProductStatus.OFF_SALE.getCode());
                product.setCheckStatus(CheckStatus.PENDING.getCode());
            } else {
                // 免审核：审核通过，尊重前端传入的上/下架状态（未传时默认上架）
                product.setCheckStatus(CheckStatus.APPROVED.getCode());
                if (product.getProductStatus() == null) {
                    product.setProductStatus(ON_SALE.getCode());
                }
            }
        }
    }

    /**
     * 店铺商品提交审核时通知平台管理员。
     *
     * <p>审核结果通知仍由 {@link #audit(ProductAuditDTO)} 负责，这里只覆盖进入待审核状态的提交事件。</p>
     */
    private void notifyProductPendingAudit(Product product) {
        if (product == null || product.getProductId() == null || product.getShopId() == null || product.getShopId() <= 0) {
            return;
        }
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("route", "product");
        relatedData.put("productId", product.getProductId());
        AdminMsgCreateDTO message = AdminMsgCreateDTO.builder()
                .msgType(AdminMsgTypeEnum.PRODUCT_AUDIT_NOTIFICATION.getCatId())
                .shopId(0)
                .adminId(0)
                .vendorId(0)
                .productId(product.getProductId())
                .title("商品审核通知：" + product.getProductName())
                .content("商品" + product.getProductName() + "审核结果为：待审核")
                .relatedData(relatedData)
                .build();
        adminMsgService.createMessage(message);
    }

    /**
     * 保存商品属性
     *
     * @param attrList  属性参数
     * @param productId 商品id
     */
    @Transactional(rollbackFor = Exception.class)
    protected void saveProductAttr(ProductAttrsDTO attrList, Integer productId) {
        if (attrList == null) {
            return;
        }
        // 规格图片上传范围控制：按 specImageMode 拦截“新增”的非第一组规格图片；历史已有图片不回溯清理
        applySpecImageModePolicy(attrList, productId);
        List<ProductAttributeDTO> attributesDTO = Stream.of(attrList.getExtra(), attrList.getSpe(), attrList.getNormal())
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .flatMap(attribute -> attribute.getAttrList().stream())
                .peek(attr -> attr.setProductId(productId))
                .toList();

        List<ProductAttribute> attrs = IntStream.range(0, attributesDTO.size())
                .mapToObj(i -> {
                    ProductAttributeDTO attr = attributesDTO.get(i);
                    ProductAttribute productAttribute = new ProductAttribute();
                    BeanUtils.copyProperties(attr, productAttribute);
                    productAttribute.setAttrPrice(attr.getAttrPrice());
                    productAttribute.setSortOrder(i);
                    return productAttribute;
                }).toList();

        // 批量保存（如果当前商品没有任何的规格参数传过来，就删除绑定的所有规格）
        if (CollUtil.isEmpty(attrs)) {
            productAttributesService.remove(new LambdaQueryWrapper<ProductAttribute>()
                    .eq(ProductAttribute::getProductId, productId)
            );
            return;
        }

        List<ProductAttribute> saveAttrs = attrs.stream().filter(attr -> attr.getAttributesId() == null).toList();
        List<ProductAttribute> editAttrs = attrs.stream().filter(attr -> attr.getAttributesId() != null).toList();
        productAttributesService.remove(new LambdaQueryWrapper<ProductAttribute>()
                .eq(ProductAttribute::getProductId, productId)
                .notIn(ObjectUtil.isNotEmpty(editAttrs.stream().map(ProductAttribute::getAttributesId).toList()),
                        ProductAttribute::getAttributesId, editAttrs.stream().map(ProductAttribute::getAttributesId).toList())
        );
        if (CollUtil.isNotEmpty(saveAttrs)) {
            productAttributesService.saveBatch(saveAttrs);
        }
        if (CollUtil.isNotEmpty(editAttrs)) {
            productAttributesService.updateBatchById(editAttrs);
        }
    }

    /**
     * 规格图片上传范围控制（配置项 specImageMode）：
     * <ul>
     *   <li>2-全部规格可上传图片：不做任何处理；</li>
     *   <li>1-仅第一组规格可上传图片（默认）：清空“新增”的非第一组规格图片；
     *       但更新时若该规格项在库中原本就有图片，则保持不变（不回溯清理历史数据）。</li>
     * </ul>
     * 读取/返回侧不受影响，始终原样返回库中已存的图片。
     *
     * @param attrList  商品属性参数（会被就地修改）
     * @param productId 商品id
     */
    private void applySpecImageModePolicy(ProductAttrsDTO attrList, Integer productId) {
        List<ProductAttrsDTO.Attribute> speList = attrList.getSpe();
        if (CollUtil.isEmpty(speList) || speList.size() <= 1) {
            return;
        }
        String specImageModeVal = configService.getConfigVal(SettingsEnum.SPEC_IMAGE_MODE);
        int specImageMode = StrUtil.isBlank(specImageModeVal)
                ? Integer.parseInt(SettingsEnum.SPEC_IMAGE_MODE.getDefaultValue())
                : Integer.parseInt(specImageModeVal.trim());
        if (specImageMode == 2) {
            return;
        }
        // specImageMode=1：非第一组规格，仅拦截“新增”的图片；库中原本已有图片的规格项保持不变
        Map<Integer, String> existingPicById = productAttributesService.lambdaQuery()
                .eq(ProductAttribute::getProductId, productId)
                .list().stream()
                .filter(attr -> attr.getAttributesId() != null)
                .collect(Collectors.toMap(ProductAttribute::getAttributesId,
                        attr -> StrUtil.nullToEmpty(attr.getAttrPic()), (a, b) -> a));
        for (int groupIndex = 1; groupIndex < speList.size(); groupIndex++) {
            ProductAttrsDTO.Attribute group = speList.get(groupIndex);
            if (group == null || CollUtil.isEmpty(group.getAttrList())) {
                continue;
            }
            for (ProductAttributeDTO attr : group.getAttrList()) {
                boolean existedWithPic = attr.getAttributesId() != null
                        && StrUtil.isNotBlank(existingPicById.get(attr.getAttributesId()));
                if (existedWithPic) {
                    // 之前就有图片，不管
                    continue;
                }
                attr.setAttrPic(null);
                attr.setAttrPicThumb(null);
            }
        }
    }

    /**
     * 保存商品图片
     *
     * @param imgList   图片列表
     * @param productId 商品ID
     */
    @Transactional(rollbackFor = Exception.class)
    protected void saveProductGallery(List<GalleryPic> imgList, Integer productId) {
        if (CollUtil.isEmpty(imgList)) {
            return;
        }

        // 1. 删除图片
        productGalleryService.lambdaUpdate()
                .eq(ProductGallery::getProductId, productId)
                .eq(ProductGallery::getShopId, 0L)
                .remove();

        // 2. 新增图片
        List<ProductGallery> productGalleryList = IntStream.range(0, imgList.size())
                .mapToObj(i -> {
                    GalleryPic galleryPic = imgList.get(i);
                    ProductGallery productGallery = new ProductGallery();
                    productGallery.setProductId(productId);
                    productGallery.setShopId(0L);
                    String picUrl = galleryPic.getPicUrl();
                    productGallery.setPicOriginal(picUrl);
                    productGallery.setPicThumb(galleryPic.getPicThumb());
                    if (productId == null) {
                        productGallery.setPicUrl(StrUtil.format("{}{}", picUrl, galleryPicService.getImageSize().get("size_400")));
                        productGallery.setPicLarge(StrUtil.format("{}{}", picUrl, galleryPicService.getImageSize().get("size_800")));
                    } else {
                        productGallery.setPicUrl(picUrl);
                        productGallery.setPicLarge(picUrl);
                    }

                    productGallery.setSortOrder(i);
                    return productGallery;
                }).toList();
        productGalleryService.saveBatch(productGalleryList);
    }

    @Transactional(rollbackFor = Exception.class)
    protected void saveStoreProductGallery(List<GalleryPic> imgList, Integer productId, Long shopId) {
        if (imgList == null || productId == null || shopId == null) {
            return;
        }

        productGalleryService.lambdaUpdate()
                .eq(ProductGallery::getProductId, productId)
                .eq(ProductGallery::getShopId, shopId)
                .remove();

        if (imgList.isEmpty()) {
            return;
        }

        List<ProductGallery> productGalleryList = IntStream.range(0, imgList.size())
                .mapToObj(i -> {
                    GalleryPic galleryPic = imgList.get(i);
                    ProductGallery productGallery = new ProductGallery();
                    productGallery.setProductId(productId);
                    productGallery.setShopId(shopId);
                    String picUrl = galleryPic.getPicUrl();
                    productGallery.setPicOriginal(picUrl);
                    productGallery.setPicThumb(galleryPic.getPicThumb());
                    productGallery.setPicUrl(picUrl);
                    productGallery.setPicLarge(picUrl);
                    productGallery.setSortOrder(i);
                    return productGallery;
                }).toList();
        productGalleryService.saveBatch(productGalleryList);
    }

    /**
     * 新增商品关联文章
     *
     * @param productArticleList 文章IDs
     * @param productId          商品ID
     */
    @Transactional(rollbackFor = Exception.class)
    protected void saveProductArticle(List<Integer> productArticleList, Integer productId) {
        if (productArticleList.isEmpty()) {
            return;
        }
        // 需要保存的实体对象
        List<ProductArticle> productArticles = productArticleList.stream().map(articleId -> {
            ProductArticle productArticle = new ProductArticle();
            productArticle.setArticleId(articleId);
            productArticle.setGoodsId(productId);
            return productArticle;
        }).collect(Collectors.toList());

        boolean isSuccess;
        // 保存关联文章
        long count = productArticleService.count(new LambdaQueryWrapper<ProductArticle>()
                .eq(ProductArticle::getGoodsId, productId));
        if (count > 0) {
            productArticleService.remove(new LambdaQueryWrapper<ProductArticle>()
                    .in(ProductArticle::getArticleId, productArticleList));
        }
        isSuccess = productArticleService.saveBatch(productArticles);
        if (!isSuccess) {
            throw new GlobalException(SERVICE_ERROR);
        }
    }

    /**
     * 更新商品货号
     *
     * @param productId 商品ID
     */
    @Transactional(rollbackFor = Exception.class)
    protected void updateSn(Integer productId) {
        String snName = configService.getConfigByCode(SettingsEnum.SN_PREFIX.getBizCode()).getBizVal();
        String productSn = StrUtil.format("{}{}", snName, String.format("%07d", productId));
        Product product = new Product();
        product.setProductId(productId);
        product.setProductSn(productSn);
        boolean isUpdated = this.updateById(product);
        if (!isUpdated) {
            throw new GlobalException(SERVICE_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(ProductEditParam param) {
        // 如果为门店商品，非平台不能操作
        Product dbProduct = this.getById(param.getProductId());
        Assert.isTrue(dbProduct != null, () -> new GlobalException("商品不存在"));
        boolean shouldNotifyPendingAudit = false;

        normalizeAndValidateSkuStatus(param.getProductList());
        applyEnabledSkuSummary(param);

        // 商品编辑库存不可改（新增不受限）：为否时用库存原值覆盖入参，库存仅可在“库存调整”修改
        if ("0".equals(configService.getConfigVal(SettingsEnum.PRODUCT_EDIT_STOCK_MODIFY))) {
            restoreStockForEdit(param, dbProduct);
        }

        Integer shopId = HeaderUtils.getShopId();
        if (shopId != null && shopId > 0) {
            String shopProductNeedCheck = configService.getConfigByCode("shopProductNeedCheck").getBizVal();
            if ("1".equals(shopProductNeedCheck)) {
                shouldNotifyPendingAudit = !Objects.equals(CheckStatus.PENDING.getCode(), dbProduct.getCheckStatus());
                // 待审核，未上架
                param.setCheckStatus(0);
                param.setProductStatus(0);
            }
        }

        Product product = new Product();
        BeanUtils.copyProperties(param, product);
        if (product.getBrandId() == null) {
            product.setBrandId(0);
        }
        // 判断是否是卡密商品，并且状态改为上架。需要判断卡密是否可以使用
        if (product.getProductType() == PRODUCT_TYPE_CARD.getCode()
                && param.getProductStatus() == 1) {
            ECardGroup byId = ecardGroupService.getById(product.getCardGroupId());
            if (byId.getIsUse() == 0) {
                throw new GlobalException("电子卡卷组【" + byId.getGroupName() + "】未启用，请先启用电子卡卷组！");
            }
        }
        if (CollUtil.isNotEmpty(param.getPaidContent())) {
            product.setPaidContent(param.getPaidContent().toJSONString(0));
        }

        // 校验商品名称长度
        checkProductNameLength(param.getProductName());

        // 校验商品价格
        if (!ProductType.isReservationProduct(product.getProductType())) {
            checkProductPrice(param.getProductPrice());
        }
        // 校验商品货号唯一
        long count = this.count(new LambdaQueryWrapper<Product>()
                .eq(Product::getProductSn, param.getProductSn())
                .eq(Product::getProductId, param.getProductId()));
        if (count < 1) {
            checkProductSn(param.getProductSn());
        }

        // 构建商品对象
        buildProduct(param, product);

        ProductPresaleDTO presaleConfig = param.getPresaleConfig();
        if (Objects.nonNull(presaleConfig) && presaleConfig.getSaleMode() != null) {
            param.setSaleMode(param.getPresaleConfig().getSaleMode());
        } else {
            param.setSaleMode(0);
        }
        product.setSaleMode(param.getSaleMode());

        // 5.8.32 单规格商品服务费校验 + 多规格清空 product 顶层 fee（避免数据污染）
        applyProductLevelServiceFee(param, product);

        // 执行更新操作
        boolean isUpdated = this.updateById(product);

        // 处理预售信息
        if (param.getSaleMode() != null && param.getSaleMode() > 0) {
            // 校验预售配置
            productPresaleService.validatePresaleConfig(param.getSaleMode(), param.getPresaleConfig(), param.getProductPrice());
            // 更新预售信息
            productPresaleService.saveOrUpdate(product.getProductId(), param.getPresaleConfig(), param.getProductPrice());
        } else {
            // 取消预售，删除预售记录
            productPresaleService.deleteByProductId(product.getProductId());
        }

        if (isUpdated) {
            if (shouldNotifyPendingAudit && Objects.equals(CheckStatus.PENDING.getCode(), product.getCheckStatus())) {
                notifyProductPendingAudit(product);
            }
            // 其他相关更新（包含SKU保存）
            applicationContext.getBean(ProductService.class).otherUpdates(param, product.getProductSn(), product, param.getImgList());


            // 增加库存编辑日志
            int diff = product.getProductStock() - dbProduct.getProductStock();
            if (CollUtil.isEmpty(param.getProductList()) && diff != 0) {
                ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                        .productId(product.getProductId())
                        .specId(0)
                        .number(product.getProductStock())
                        .addTime(StringUtils.getCurrentTime())
                        .oldNumber(dbProduct.getProductStock())
                        .type(diff > 0 ? 1 : 2)
                        .changeNumber(diff)
                        .desc("库存编辑")
                        .shopId(product.getShopId())
                        .build();
                productInventoryLogService.save(productInventoryLog);
            }

            // 异步同步到ES
            try {
                productEsSyncService.syncProductAsync(Long.valueOf(product.getProductId()));
            } catch (Exception e) {
                log.error("商品编辑后ES同步失败: {}", product.getProductId(), e);
            }
        }


        // 处理自动上下架任务（两个时间都为空时也会进入，用于取消已有定时任务）
        this.handleAutoStatusTasks(product, param.getAutoOnlineTime(), param.getAutoOfflineTime());
        if (isUpdated && shopId != null && shopId > 0
                && Objects.equals(CheckStatus.PENDING.getCode(), product.getCheckStatus())
                && !Objects.equals(CheckStatus.PENDING.getCode(), dbProduct.getCheckStatus())) {
            String shopProductNeedCheck = configService.getConfigByCode("shopProductNeedCheck").getBizVal();
            if ("1".equals(shopProductNeedCheck)) {
                saveProductAuditPendingMessage(product);
            }
        }
        return isUpdated;
    }

    private void saveProductAuditPendingMessage(Product product) {
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("productId", product.getProductId());
        AdminMsg adminMsg = AdminMsg.builder()
                .msgType(AdminMsgTypeEnum.PRODUCT_AUDIT_NOTIFICATION.getCatId())
                .title("商品审核通知：" + product.getProductName())
                .shopId(0)
                .productId(product.getProductId())
                .content("商品" + product.getProductName() + "审核结果为：待审核")
                .relatedData(JSON.toJSONString(relatedData))
                .sendTime(StringUtils.getCurrentTime())
                .build();
        adminMsgService.save(adminMsg);
    }

    /**
     * 其他相关更新
     *
     * @param param     商品DTO
     * @param productSn 商品货号
     * @param product   商品
     * @param imgList   图片列表
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void otherUpdates(ProductSaveParam param, String productSn, Product product, List<GalleryPic> imgList) {
        Integer productId = product.getProductId();
        if (StrUtil.isEmpty(productSn)) {
            // 更新商品序号
            updateSn(productId);
        }
        String adminType = getAdminType();

        if (CollUtil.isNotEmpty(param.getProductVideoInfo())) {
            // 更新 商品视频
            productVideoService.lambdaUpdate().eq(ProductVideo::getProductId, productId).remove();
            for (ProductVideo productVideo : param.getProductVideoInfo()) {
                productVideo.setProductId(productId);
                // 前端将视频id放在 id 字段传入（图库视频ID），转存到 videoId 后再清空自增主键
                Integer videoId = productVideo.getVideoId() != null ? productVideo.getVideoId() : productVideo.getId();
                productVideo.setVideoId(videoId != null ? videoId : 0);
                productVideo.setId(null);
                productVideoService.save(productVideo);
            }
        } else {
            productVideoService.lambdaUpdate().eq(ProductVideo::getProductId, productId).remove();
        }

        // 更新 商品图片
        saveProductGallery(imgList, productId);

        // 更新 关联文章
        List<Integer> productArticleList = param.getProductArticleList();
        if (productArticleList != null && !productArticleList.isEmpty()) {
            // 保存商品关联文章
            saveProductArticle(productArticleList, productId);
        }

        // 更新 商品属性
        ProductAttrsDTO attrList = param.getAttrList();
        if (attrList != null) {
            // 保存商品属性
            saveProductAttr(attrList, productId);
        }
        // 更新 商品分组
        Integer isHot = param.getIsHot();
        if (isHot != null && isHot == 1) {
            productGroupService.updateGroupProductByName(productId, IS_HOT.getDesc());
        }
        Integer isNew = param.getIsNew();
        if (isNew != null && isNew == 1) {
            productGroupService.updateGroupProductByName(productId, IS_NEW.getDesc());
        }

        // 更新sku
        List<ProductListAttributeDTO> productList = normalizeReservationProductList(param, product);
        param.setProductList(productList);


        // 保存商品sku
        saveProductSku(productList, product);


        if (param.getProductStatus() != null && param.getProductStatus() == 0) {
            AdminMsgCreateDTO createDTO = new AdminMsgCreateDTO();
            createDTO.setMsgType(AdminMsgTypeEnum.PRODUCT_OUT_OF_STOCK.getCatId());
            createDTO.setShopId(param.getShopId());
            createDTO.setProductId(productId);
            createDTO.setTitle("商品下架:" + param.getProductName());
            createDTO.setContent("您的商品【" + param.getProductName() + "】已经下架，请及时处理！");
            Map<String, Object> relatedData = new HashMap<>();
            relatedData.put("productId", productId);
            createDTO.setRelatedData(relatedData);
            adminMsgService.createMessage(createDTO);
        }
    }


    @Override
    public List<Product> getProductByIds(List<Integer> productIds) {
        if (productIds.isEmpty()) {
            return List.of();
        }
        return this.list(new LambdaQueryWrapper<Product>().in(Product::getProductId, productIds));
    }



    /**
     * 转换为DTO
     *
     * @param product 商品实体
     * @return DTO
     */
    public ProductListResDTO convertToDTO(Product product) {
        ProductListResDTO dto = new ProductListResDTO();
        BeanUtils.copyProperties(product, dto);
        if (StringUtils.isNotBlank(product.getProductServiceIds())) {
            dto.setProductServiceIds(JSON.parseArray(product.getProductServiceIds(), Integer.class));
        }
        return dto;
    }

    /**
     * 构建商品列表查询条件
     *
     * @param queryWrapper 查询构造器
     * @param listDTO      查询参数
     */
    private void buildQueryWrapper(MPJLambdaWrapper<Product> queryWrapper, ProductListDTO listDTO) {

        // 关键词查询条件（与 idList 合并为同一个 nested 块，避免 OR 破坏后续 AND 条件）
        String keyword = listDTO.getKeyword();
        List<Integer> idList = listDTO.getIdList();
        boolean hasKeyword = StrUtil.isNotEmpty(keyword);
        boolean hasIdList = CollUtil.isNotEmpty(idList);
        if (hasKeyword && hasIdList) {
            queryWrapper.nested(wrapper -> wrapper
                    .nested(w -> w.like(Product::getProductName, keyword)
                            .or().like(Product::getKeywords, keyword)
                            .or().like(Product::getProductSn, keyword)
                            .or().like(Product::getProductTsn, keyword))
                    .or()
                    .in(Product::getProductId, idList));
        } else if (hasKeyword) {
            queryWrapper.nested(wrapper -> wrapper.like(Product::getProductName, keyword)
                    .or()
                    .like(Product::getKeywords, keyword)
                    .or()
                    .like(Product::getProductSn, keyword)
                    .or()
                    .like(Product::getProductTsn, keyword));
        } else if (hasIdList) {
            queryWrapper.in(Product::getProductId, idList);
        }
        // 注意：这里的 shopId 条件需要考虑 posProductSource 的情况
        // 当 posProductSource 包含平台分配商品(2)时，需要跳过此条件
        List<Integer> posProductSourceForShopId = listDTO.getPosProductSource();
        boolean hasPlatformSourceForShopId = CollUtil.isNotEmpty(posProductSourceForShopId) && posProductSourceForShopId.contains(2);
        if (listDTO.getShopId() != null && listDTO.getShopId() > 0 && !hasPlatformSourceForShopId) {
            queryWrapper.eq(Product::getShopId, listDTO.getShopId());
        }
        // 商品类型查询条件
        String introType = listDTO.getIntroType();
        introType = StrUtil.toUnderlineCase(introType);
        if (StrUtil.isNotEmpty(introType)) {
            IntroType type = fromString(introType);
            if (type != null) {
                // 这里introType是is_best/is_hot/is_new中的一个，1代表是，0代表否
                if (IntroType.IS_BEST.equals(type)) {
                    queryWrapper.eq(Product::getIsBest, 1);
                } else if (IntroType.IS_HOT.equals(type)) {
                    queryWrapper.eq(Product::getIsHot, 1);
                } else {
                    queryWrapper.eq(Product::getIsNew, 1);
                }

            }
        }
        // 优惠券
        Integer couponId = listDTO.getCouponId();
        if (couponId != null && couponId > 0) {
            //获取优惠券详情
            CouponVO coupon = couponService.detail(couponId);
            if (coupon != null) {
                if (coupon.getSendRange() == SendRangeEnum.CATEGORY.getValue()) {
                    queryWrapper.nested(wrapper -> {
                        for (Integer itemCategoryId : coupon.getSendRangeData()) {
                            List<Integer> categoryIds = categoryService.getCategoryAllChildIds(itemCategoryId);
                            if (CollUtil.isNotEmpty(categoryIds)) {
                                wrapper.in(Product::getCategoryId, categoryIds, "OR");
                            }
                        }
                    });
                } else if (coupon.getSendRange() == SendRangeEnum.BRAND.getValue()) {
                    if (CollUtil.isNotEmpty(coupon.getSendRangeData())) {
                        queryWrapper.in(Product::getBrandId, coupon.getSendRangeData());
                    }
                } else if (coupon.getSendRange() == SendRangeEnum.PRODUCT.getValue()) {
                    if (CollUtil.isNotEmpty(coupon.getSendRangeData())) {
                        queryWrapper.in(Product::getProductId, coupon.getSendRangeData());
                    }
                } else if (coupon.getSendRange() == SendRangeEnum.NOT_PRODUCT.getValue()) {
                    if (CollUtil.isNotEmpty(coupon.getSendRangeData())) {
                        queryWrapper.notIn(Product::getProductId, coupon.getSendRangeData());
                    }
                }
                queryWrapper.eq(Product::getShopId, coupon.getShopId());

            }
        }

        // 品牌ID
        Integer brandId = listDTO.getBrandId();
        if (brandId != null && brandId >= 0) {
            queryWrapper.eq(Product::getBrandId, brandId);
        }

        //商品类型
        Integer productType = listDTO.getProductType();
        if (Objects.nonNull(productType) && productType > 0) {
            queryWrapper.eq(Product::getProductType, productType);
        }

        // 商品类型列表（支持多个类型过滤）
        List<Integer> productTypes = listDTO.getProductTypes();
        if (CollUtil.isNotEmpty(productTypes)) {
            queryWrapper.in(Product::getProductType, productTypes);
        }

        // 排除的商品类型（多选）
        List<Integer> excludeProductTypes = listDTO.getExcludeProductTypes();
        if (CollUtil.isNotEmpty(excludeProductTypes)) {
            queryWrapper.notIn(Product::getProductType, excludeProductTypes);
        }

        // 品牌IDs
        if (StrUtil.isNotBlank(listDTO.getBrandIds())) {
            List<String> brandIds = StrUtil.split(listDTO.getBrandIds(), ",");
            if (!brandIds.isEmpty()) {
                queryWrapper.in(Product::getBrandId, brandIds);
            }
        }

        // idList 已在上方关键词查询条件中合并处理，此处不再单独 OR

        // 不包含的id
        if (listDTO.getExtraProductIds() != null && !listDTO.getExtraProductIds().isEmpty()) {
            queryWrapper.notIn(Product::getProductId, listDTO.getExtraProductIds());
        }

        // 商品分类ID查询条件
        Integer categoryId = listDTO.getCategoryId();
        if (categoryId != null && categoryId > 0) {
            List<Integer> ids = categoryService.getCategoryAllChildIds(listDTO.getCategoryId());
            if (CollUtil.isNotEmpty(ids)) {
                queryWrapper.in(Product::getCategoryId, ids);
            }
        }

        // 商品分类ID查询条件
        Integer shopCategoryId = listDTO.getShopCategoryId();
        if (shopCategoryId != null && shopCategoryId > 0 && listDTO.getShopId() > 0) {
            List<Integer> ids = shopProductCategoryService.getCategoryAllChildIds(shopCategoryId, listDTO.getShopId());
            if (CollUtil.isNotEmpty(ids)) {
                queryWrapper.in(Product::getShopCategoryId, ids);
            }
        }

        //排除周期购商品
        if(Objects.equals(listDTO.getExcludeCycleProduct(),true)){
            queryWrapper.and(wrapper -> wrapper.ne(Product::getDeliveryMode, 2)
                    .or()
                    .isNull(Product::getDeliveryMode));
        }

        // 仅排除按计划配送的周期购，保留次卡（按需兑换）商品
        if (Objects.equals(listDTO.getExcludePlanDeliveryCycleOnly(), true)) {
            queryWrapper.and(wrapper -> wrapper
                    .ne(Product::getDeliveryMode, 2)
                    .or()
                    .isNull(Product::getDeliveryMode)
                    .or()
                    .inSql(Product::getProductId,
                            "SELECT product_id FROM product_cycle WHERE delivery_type = 2"));
        }

        // 排除礼品卡相关商品（实体礼品卡、电子礼品卡）
        if (Objects.equals(listDTO.getExcludeGiftCardProduct(), true)) {
            queryWrapper.notIn(Product::getProductType, List.of(
                    ProductType.PRODUCT_TYPE_GIFT_CARD.getCode(),
                    ProductType.PRODUCT_TYPE_GIFT_CARD_VIRTUAL.getCode()
            ));
        }

        // 配送类型过滤（多选，OR 关系）：1-到店自提 2-上门服务 3-预约到店 4-同城配送 5-快递配送
        List<Integer> deliveryTypes = listDTO.getDeliveryTypes();
        if (CollUtil.isNotEmpty(deliveryTypes)) {
            boolean pickup = deliveryTypes.contains(1);        // 到店自提
            boolean cityDelivery = deliveryTypes.contains(4);  // 同城配送（门店配送）
            boolean express = deliveryTypes.contains(5);       // 快递配送（物流配送）
            List<Integer> deliveryProductTypes = new java.util.ArrayList<>();
            if (deliveryTypes.contains(2)) {                   // 上门服务 -> 预约上门
                deliveryProductTypes.add(ProductType.PRODUCT_TYPE_RESERVATION_STORE.getCode());
            }
            if (deliveryTypes.contains(3)) {                   // 预约到店
                deliveryProductTypes.add(ProductType.PRODUCT_TYPE_RESERVATION_HOME.getCode());
            }
            if (pickup || cityDelivery || express || CollUtil.isNotEmpty(deliveryProductTypes)) {
                queryWrapper.and(wrapper -> {
                    boolean[] hasPrev = {false};
                    if (pickup) {
                        wrapper.eq(Product::getIsShopPickup, 1);
                        hasPrev[0] = true;
                    }
                    if (cityDelivery) {
                        if (hasPrev[0]) wrapper.or();
                        wrapper.eq(Product::getIsShopDelivery, 1);
                        hasPrev[0] = true;
                    }
                    if (express) {
                        if (hasPrev[0]) wrapper.or();
                        wrapper.eq(Product::getIsLogistics, 1);
                        hasPrev[0] = true;
                    }
                    if (CollUtil.isNotEmpty(deliveryProductTypes)) {
                        if (hasPrev[0]) {
                            wrapper.or(w -> w.in(Product::getProductType, deliveryProductTypes));
                        } else {
                            wrapper.in(Product::getProductType, deliveryProductTypes);
                        }
                    }
                });
            }
            // 选择了实体配送（同城配送/快递配送）时，排除本不该走物流的商品类型：
            // 虚拟(2)、卡密(3)、付费内容(4)、预约上门(5)、预约到店(6)；礼品卡(7/8)与实物(1)不排除。
            // 若用户在 deliveryTypes 中显式请求了对应预约类型（2->预约上门5, 3->预约到店6），则不排除该类型。
            if (cityDelivery || express) {
                List<Integer> nonLogisticsExcludeTypes = new java.util.ArrayList<>();
                nonLogisticsExcludeTypes.add(ProductType.PRODUCT_TYPE_VIRTUAL.getCode());
                nonLogisticsExcludeTypes.add(ProductType.PRODUCT_TYPE_CARD.getCode());
                nonLogisticsExcludeTypes.add(ProductType.PRODUCT_TYPE_PAID.getCode());
                nonLogisticsExcludeTypes.add(ProductType.PRODUCT_TYPE_RESERVATION_STORE.getCode());
                nonLogisticsExcludeTypes.add(ProductType.PRODUCT_TYPE_RESERVATION_HOME.getCode());
                nonLogisticsExcludeTypes.removeAll(deliveryProductTypes);
                if (CollUtil.isNotEmpty(nonLogisticsExcludeTypes)) {
                    queryWrapper.notIn(Product::getProductType, nonLogisticsExcludeTypes);
                }
            }
        }


        //处理字符串的ids
        if (StrUtil.isNotBlank(listDTO.getIds())) {
            List<String> ids = StrUtil.split(listDTO.getIds(), ",");
            if (!ids.isEmpty()) {
                queryWrapper.in(Product::getProductId, ids);
            }
        }
        //maxPrice
        if (listDTO.getMaxPrice() != null) {
            queryWrapper.le(Product::getProductPrice, listDTO.getMaxPrice().doubleValue());
        }
        // minPrice
        if (listDTO.getMinPrice() != null) {
            queryWrapper.ge(Product::getProductPrice, listDTO.getMinPrice().doubleValue());
        }
        // 销量区间（累计销量/30日销量口径均复用虚拟销量 virtual_sales）
        if (listDTO.getMinSales() != null) {
            queryWrapper.ge(Product::getVirtualSales, listDTO.getMinSales());
        }
        if (listDTO.getMaxSales() != null) {
            queryWrapper.le(Product::getVirtualSales, listDTO.getMaxSales());
        }
        if (listDTO.getAddedStartTime() != null) {
            queryWrapper.ge(Product::getAddTime, listDTO.getAddedStartTime());
        }
        if (listDTO.getAddedEndTime() != null) {
            queryWrapper.le(Product::getAddTime, listDTO.getAddedEndTime());
        }
        // 库存区间（按商品总库存 product_stock，非 SKU 维度）
        if (listDTO.getMinStock() != null) {
            queryWrapper.ge(Product::getProductStock, listDTO.getMinStock());
        }
        if (listDTO.getMaxStock() != null) {
            queryWrapper.le(Product::getProductStock, listDTO.getMaxStock());
        }
        // 店铺ID查询条件
        // 注意：当 posProductSource 包含平台分配商品(2)时，需要跳过此条件，让 posProductSource 逻辑自己处理 shopId 筛选
        Integer shopId = listDTO.getShopId();
        List<Integer> posProductSource = listDTO.getPosProductSource();
        boolean hasPlatformSource = CollUtil.isNotEmpty(posProductSource) && posProductSource.contains(2);
        if (shopId != null && shopId != -1 && !hasPlatformSource) {
            queryWrapper.eq(Product::getShopId, shopId);
        }
        // 搜索店铺
        if (listDTO.getSearchShop() != null && listDTO.getSearchShop() != 0) {
            //则查shop_id >0 的
            queryWrapper.gt(Product::getShopId, 0);
        }


        //分组id
        if (listDTO.getProductGroupId() != null && listDTO.getProductGroupId() > 0) {
            // 通过product_group表先获取product_group_id对应的product_ids
            List<Integer> groupProductIds = productGroupService.detail(listDTO.getProductGroupId()).getProductIds();
            if (groupProductIds.isEmpty()) {
                queryWrapper.eq(Product::getProductId, -1);
            } else {
                queryWrapper.in(Product::getProductId, groupProductIds);
            }
        }
        // 商品状态查询条件
        Integer productStatus = listDTO.getProductStatus();
        if (productStatus != null && ProductStatus.isValidCode(productStatus)) {
            queryWrapper.eq(Product::getProductStatus, productStatus);
        }
        // 是否删除
        Integer isDelete = listDTO.getIsDelete();
        if (isDelete != null && DeleteType.isValidCode(isDelete)) {
            queryWrapper.eq(Product::getIsDelete, isDelete);
        } else {
            queryWrapper.eq(Product::getIsDelete, DeleteType.NOT_DELETE.getCode());
        }
        // 是否最新
        if (listDTO.getIsNew() != null && listDTO.getIsNew() > -1) {
            queryWrapper.eq(Product::getIsNew, listDTO.getIsNew());
        }

        // 审核状态查询条件
        Integer checkStatus = listDTO.getCheckStatus();
        if (checkStatus != null && CheckStatus.isValidCode(checkStatus)) {
            queryWrapper.eq(Product::getCheckStatus, checkStatus);
        }

        //供应商
        Integer suppliersId = listDTO.getSuppliersId();
        if (suppliersId != null && suppliersId > 0) {
            queryWrapper.eq(Product::getSuppliersId, suppliersId);
        }

        // 供应商资料来源选择器传 showVendorProduct=0 时，排除自营店导入的供应商商品。
        if (Objects.equals(listDTO.getShowVendorProduct(), 0)) {
            queryWrapper.and(wrapper -> wrapper.le(Product::getVendorId, 0)
                    .or().isNull(Product::getVendorId));
        }

        //排除不运营的店铺
        String adminType = HeaderUtils.getAdminType();
        if (StringUtils.isBlank(adminType) && !"pos".equals(adminType)) {
            //search,pos(商品列表)=>排除不在运营的商品列表(为什么不增加删除门店的状态，因为要进行删除必须(门店无商品,并且是停业状态))
            List<Shop> shops = shopMapper.selectList(new LambdaQueryWrapper<Shop>().eq(Shop::getStatus, 4));
            queryWrapper.notIn(ObjectUtil.isNotEmpty(shops), Product::getShopId, shops.stream().map(Shop::getShopId).toList());
        } else {
            //后台,店铺后台(门店)等等,展示没有删除的门店
            List<Shop> shops = shopMapper.selectList(new LambdaQueryWrapper<Shop>().eq(Shop::getStatus, 10));
            queryWrapper.notIn(ObjectUtil.isNotEmpty(shops), Product::getShopId, shops.stream().map(Shop::getShopId).toList());
        }

        if (listDTO.getIsLottery() != null && listDTO.getIsLottery() == 1) {
            queryWrapper.notIn(Product::getProductType, ProductType.PRODUCT_TYPE_RESERVATION_STORE.getCode(), ProductType.PRODUCT_TYPE_RESERVATION_HOME.getCode());
            queryWrapper.eq(Product::getShopId, 0);
            queryWrapper.eq(Product::getSaleMode, 0);
        }

        //是否排除预售商品
        Integer isFilterPresaleProduct = listDTO.getIsFilterPresaleProduct();
        if (Integer.valueOf(1).equals(isFilterPresaleProduct)) {
            //是排除预售商品(其他模式先不排除),排除销售模式为1和2的
            queryWrapper.notIn(Product::getSaleMode, Arrays.asList(1, 2));
        }

        // 属性筛选（MySQL）: 与 ES 逻辑对齐
        List<ProductSearchClientDTO.AttrObj> attrs = listDTO.getAttrs();
        if (CollUtil.isNotEmpty(attrs)) {
            for (ProductSearchClientDTO.AttrObj attr : attrs) {
                String name = attr.getAttrName();
                if (StrUtil.isBlank(name)) continue;

                Object rawValues = attr.getAttrValue();
                List<String> values = new ArrayList<>();

                if (rawValues instanceof Collection<?> coll) {
                    values.addAll(coll.stream()
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .filter(StrUtil::isNotBlank)
                            .distinct()
                            .toList());
                } else if (rawValues instanceof String str) {
                    values.addAll(Arrays.stream(str.split("\\s*[,，]\\s*"))
                            .filter(StrUtil::isNotBlank)
                            .distinct()
                            .toList());
                }

                if (CollUtil.isEmpty(values)) continue;

                StringBuilder sql = new StringBuilder();
                List<Object> params = new ArrayList<>();

                sql.append("SELECT 1 FROM product_attributes pa ")
                        .append("WHERE pa.product_id = t.product_id ")
                        .append("AND pa.attr_type = 0 ")
                        .append("AND pa.attr_name LIKE {0} ");
                params.add("%" + name.replace("%", "\\%").replace("_", "\\_") + "%");

                // OR 条件
                sql.append("AND (");
                for (int i = 0; i < values.size(); i++) {
                    if (i > 0) sql.append(" OR ");
                    sql.append("pa.attr_value LIKE {").append(i + 1).append("}");
                    params.add("%" + values.get(i)
                            .replace("%", "\\%")
                            .replace("_", "\\_") + "%");
                }
                sql.append(")");

                queryWrapper.exists(sql.toString(), params.toArray());
            }
        }

        // 收银台商品来源过滤
        if (CollUtil.isNotEmpty(posProductSource) && !posProductSource.contains(0)) {
            // 不包含全部商品(0)，需要根据来源类型过滤
            Integer currentShopId = HeaderUtils.getShopId();

            // 如果包含平台分配商品(source=2)，先查询符合条件的商品ID
            Set<Integer> platformProductIds = new HashSet<>();
            if (posProductSource.contains(2)) {
                List<StoreProduct> storeProducts = storeProductMapper.selectList(
                        Wrappers.<StoreProduct>lambdaQuery()
                                .eq(StoreProduct::getShopId, currentShopId)
                                .eq(StoreProduct::getIsDelete, 0)
                                .eq(StoreProduct::getProductStatus, 1));
                platformProductIds = storeProducts.stream()
                        .map(StoreProduct::getProductId)
                        .collect(Collectors.toSet());
            }

            Set<Integer> finalPlatformProductIds = platformProductIds;
            queryWrapper.and(wrapper -> {
                boolean firstCondition = true;
                for (Integer source : posProductSource) {
                    if (source == 1) {
                        // 所属门店（排除供应商商品：vendorId > 0）
                        if (firstCondition) {
                            wrapper.nested(w -> w.eq(Product::getShopId, currentShopId)
                                    .and(v -> v.eq(Product::getVendorId, 0).or().isNull(Product::getVendorId)));
                            firstCondition = false;
                        } else {
                            wrapper.or(w -> w.eq(Product::getShopId, currentShopId)
                                    .and(v -> v.eq(Product::getVendorId, 0).or().isNull(Product::getVendorId)));
                        }
                    } else if (source == 2) {
                        // 平台分配商品：shopId = 0 且在 store_product 表中有对应门店的关联记录
                        if (CollUtil.isNotEmpty(finalPlatformProductIds)) {
                            if (firstCondition) {
                                wrapper.nested(w -> w.eq(Product::getShopId, 0)
                                        .in(Product::getProductId, finalPlatformProductIds));
                                firstCondition = false;
                            } else {
                                wrapper.or(w -> w.eq(Product::getShopId, 0)
                                        .in(Product::getProductId, finalPlatformProductIds));
                            }
                        }
                    } else if (source == 3) {
                        // 供应商商品：vendorId > 0
                        if (firstCondition) {
                            wrapper.gt(Product::getVendorId, 0);
                            firstCondition = false;
                        } else {
                            wrapper.or().gt(Product::getVendorId, 0);
                        }
                    }
                }
            });
        }
    }

    /**
     * 获取待审核商品数量
     *
     * @return 待审核商品数量
     */
    @Override
    public Long getWaitingCheckedCount() {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getCheckStatus, CheckStatus.PENDING.getCode());
        queryWrapper.eq(Product::getIsDelete, NOT_DELETE.getCode());

        Integer shopId = HeaderUtils.getShopId();
        //如果是admin的话,则查询所有
        if (shopId != null && shopId > 0) {
            //只查询自己门店或者店铺的
            queryWrapper.eq(Product::getShopId, shopId);
        } else {
            //排除不运营的店铺
            List<Shop> shops = shopMapper.selectList(new LambdaQueryWrapper<Shop>().eq(Shop::getStatus, 4));
            queryWrapper.notIn(ObjectUtil.isNotEmpty(shops), Product::getShopId, shops.stream().map(Shop::getShopId).toList());
        }

        return this.count(queryWrapper);
    }

    /**
     * 校验商品货号是否重复
     *
     * @param productSn 商品货号
     */
    public void checkProductSn(String productSn) {
        if (StrUtil.isNotEmpty(productSn)) {
            LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Product::getProductSn, productSn);
            if (this.count(queryWrapper) > 0) {
                throw new GlobalException(PRODUCT_SN_EXIST);
            }
        }
    }

    /**
     * 校验商品价格是否大于0
     *
     * @param productPrice 商品价格
     */
    public void checkProductPrice(BigDecimal productPrice) {
        if (productPrice == null) {
            throw new GlobalException(PRODUCT_PRICE_LE_0);
        }
        if (productPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new GlobalException(PRODUCT_PRICE_LE_0);
        }
    }

    /**
     * 校验商品名称是否超过自定义长度限制
     */
    private void checkProductNameLength(String productName) {
        if (StrUtil.isEmpty(productName)) {
            return;
        }
        String customLengthStr = configService.getConfigVal(SettingsEnum.CUSTOM_PRODUCT_NAME_LENGTH);
        if (StrUtil.isNotEmpty(customLengthStr)) {
            int customLength = Integer.parseInt(customLengthStr);
            if (productName.length() > customLength) {
                throw new GlobalException(PRODUCT_NAME_OVER_CUSTOM_LENGTH + "（最大" + customLength + "个字符）");
            }
        }
    }

    /**
     * 获取商品详情描述
     *
     * @param productDescList 商品详情描述列表
     * @return 商品详情描述字符串
     */
    public String getProductDesc(List<ProductDescDTO> productDescList) {
        if (productDescList == null || productDescList.isEmpty()) {
            return "";
        }

        // 生成结果列表
        List<String> result = productDescList.stream()
                .map(item -> {
                    String type = item.getType();
                    if ("pic".equals(type)) {
                        String pic = item.getPic();
                        return "<div class=\"desc-pic-item\"><img src=\"" + pic + "\"></div>";
                    } else if ("video".equals(type)) {
                        String cover = item.getCover() != null ? item.getCover() : "";
                        String videoUrl = item.getVideoUrl() != null ? item.getVideoUrl() : "";
                        return "<div class=\"desc-video-item\" data-cover=\"" + cover + "\" data-video=\"" + videoUrl + "\"></div>";
                    } else if ("text".equals(type)) {
                        return item.getHtml();
                    }
                    return "";
                })
                // 过滤空字符串
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        // 使用分隔符拼接结果
        return String.join("<div data-division=1></div>", result);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void initProductByShopId(Integer shopId) {
        for (int i = 0; i < 4; i++) {
            //获取运费模板内容
            ProductSaveParam productSaveParam = new ProductSaveParam();
            productSaveParam.setProductType(ProductType.PRODUCT_TYPE_NORMAL.getCode());
            productSaveParam.setProductName("实例商品" + i);
            productSaveParam.setProductStock(1000);
            productSaveParam.setProductPrice(new BigDecimal("0.01"));
            productSaveParam.setProductDescArr(List.of());
            productSaveParam.setProductTsn(String.valueOf(i));
            productSaveParam.setShopId(getShopId());
            productSaveParam.setProductStatus(0);

            try {
                create(productSaveParam);
            } catch (Exception e) {
                throw new GlobalException("初始化商品失败");
            }
        }
    }

    @Override
    public List<ProductShippingTplListVO> getShippingTplListByShopId() {
        return shippingTplService.getShippingTplListByShopId();
    }

    @Override
    public Integer getProductStock(Integer productId, Integer skuId, boolean includeActivity, Integer shopId) {
        Long now = StringUtils.getCurrentTime();
        if (skuId != null && skuId > 0) {
            ProductSku sku = productSkuService.getById(skuId);
            if (sku == null || !Objects.equals(sku.getProductId(), productId)
                    || Objects.equals(sku.getIsEnabled(), Constants.NO)) {
                return 0;
            }
        }
        if (includeActivity) {
            SeckillItem seckillItem = seckillItemMapper.selectOne(new QueryWrapper<SeckillItem>().eq("product_id", productId).eq("sku_id", skuId).le("seckill_start_time", now).ge("seckill_end_time", now));
            if (seckillItem != null) {
                return seckillItem.getSeckillStock();
            }
        }
        Product product = this.getById(productId);
        if (skuId != null && skuId > 0) {
            if (tigshopProperties.getIsO2o() == 1 && product != null && ProductType.isReservationProduct(product.getProductType())) {
                Integer effectiveShopId = resolveEffectiveReservationShopId(productId, shopId, product.getShopId());
                return calculateReservationSkuStockForDetailDisplay(productId, effectiveShopId, skuId);
            }
            return productSkuService.getProductStock(productId, skuId);
        } else {
            if (product.getProductType() == PRODUCT_TYPE_CARD.getCode()) {
                product.setProductStock((int) eCardService.count(new LambdaQueryWrapper<ECard>()
                        .eq(ECard::getIsUse, NO)
                        .eq(ECard::getGroupId, product.getCardGroupId())));
            }
            return product.getProductStock();
        }
    }

    @Override
    public BigDecimal getProductFinalPrice(Integer productId, BigDecimal originalPrice, Integer skuId) {
        BigDecimal price = originalPrice;
        Long now = StringUtils.getCurrentTime();
        SeckillItem seckillItem = seckillItemMapper.selectOne(new QueryWrapper<SeckillItem>().eq("product_id", productId).eq("sku_id", skuId).ge("seckill_start_time", now).le("seckill_end_time", now));
        if (seckillItem != null) {
            return seckillItem.getSeckillPrice();
        }
        if (skuId > 0) {
            ProductSku productSku = productSkuService.getById(skuId);
            if (productSku != null) {
                price = productSku.getSkuPrice();
            }
        }
//        // 会员等级优惠价
//        User user = userMapper.selectById(SecurityUtils.getCurrentUserId());
//        //会员等级列表
//        List<UserRank> userRankList = userRankService.list();
//        for (UserRank userRank : userRankList) {
//            assert user != null;
//            if (Objects.equals(user.getRankId(), userRank.getRankId()) && userRank.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
//                price = originalPrice.multiply(userRank.getDiscount());
//                price = price.divide(BigDecimal.valueOf(10), 2, RoundingMode.HALF_UP);
//            }
//        }
        return price;
    }

    @Override
    public List<ProductListResDTO> getProductList(ProductListDTO dto) {
        // dto.setIds(getProductIds());
        return list(dto).getRecords();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void decStock(Integer productId, Integer quantity, String stockDesc) {
        decStock(productId, quantity, null, true);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void decStock(Integer productId, Integer quantity, String stockDesc, boolean addLog) {
        // 1. 查询商品属性
        Product product = this.getById(productId);

        if (quantity <= 0) {
            return;
        }
        // 2. 扣减商品库存（原子操作，WHERE product_stock >= quantity 防超卖）
        boolean success = this.lambdaUpdate()
                .eq(Product::getProductId, productId)
                .ge(Product::getProductStock, quantity)
                .setDecrBy(Product::getProductStock, quantity)
                .update();
        if (!success) {
            throw new GlobalException(product.getProductName() + "库存不足");
        }
        int number = product.getProductStock() - quantity;
        String desc = "下单扣减库存";
        if (StringUtils.isNotBlank(stockDesc)) {
            desc = stockDesc;
        }

        if (addLog) {
            // 3. 增加库存扣减日志
            ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                    .productId(product.getProductId())
                    .specId(0)
                    .number(number)
                    .addTime(StringUtils.getCurrentTime())
                    .oldNumber(product.getProductStock())
                    .type(2)
                    .changeNumber(quantity)
                    .desc(desc)
                    .shopId(product.getShopId())
                    .build();
            productInventoryLogService.save(productInventoryLog);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incStock(Integer productId, Integer quantity, String stockDesc) {
        incStock(productId, quantity, stockDesc, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incStock(Integer productId, Integer quantity, String stockDesc, boolean addLog) {
        // 实际变化量为0时，无需增加库存和记录日志
        if (quantity == null || quantity <= 0) {
            return;
        }
        // 1. 查询商品属性
        Product product = this.getById(productId);

        // 2. 增加商品库存
        this.lambdaUpdate().eq(Product::getProductId, productId).setIncrBy(Product::getProductStock, quantity).update();

        String desc = "取消订单返库存";
        if (StringUtils.isNotBlank(stockDesc)) {
            desc = stockDesc;
        }

        if (addLog) {
            // 3. 增加库存增加日志
            int number = product.getProductStock() + quantity;
            ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                    .productId(product.getProductId())
                    .specId(0)
                    .number(number)
                    .addTime(StringUtils.getCurrentTime())
                    .oldNumber(product.getProductStock())
                    .type(1)
                    .changeNumber(quantity)
                    .desc(desc)
                    .shopId(product.getShopId())
                    .build();
            productInventoryLogService.save(productInventoryLog);
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void incSales(Integer productId, Integer quantity) {
        LambdaUpdateWrapper<Product> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Product::getProductId, productId).setIncrBy(Product::getVirtualSales, quantity);
        this.update(updateWrapper);
    }

    /**
     * 获取随机商品
     *
     * @return String
     */
    @Override
    public String getProductIds() {
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        List<Integer> recommendedProductIds = getRecommendedProductIds(currentUserId);
        return recommendedProductIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(StrUtil.COMMA));
    }

    public List<Integer> getRecommendedProductIds(int userId) {
        Set<Integer> resultSet = new LinkedHashSet<>();

        // 1. 取 is_best = 1 的商品
        List<Integer> bestIds = this.baseMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getIsBest, 1)
                        .eq(Product::getProductStatus, ON_SALE.getCode())
                        .eq(Product::getIsDelete, 0)
                        .select(Product::getProductId)
                        .last("LIMIT 10")
        ).stream().map(Product::getProductId).toList();
        resultSet.addAll(bestIds);

        // 2. 取 is_hot = 1 的商品
        List<Integer> hotIds = this.baseMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getIsHot, 1)
                        .eq(Product::getProductStatus, ON_SALE.getCode())
                        .eq(Product::getIsDelete, 0)
                        .select(Product::getProductId)
                        .last("LIMIT 10")
        ).stream().map(Product::getProductId).toList();
        resultSet.addAll(hotIds);

        // 3. 获取用户浏览记录
        if (userId > 0) {
            User user = userMapper.selectById(userId);
            if (user != null && StrUtil.isNotBlank(user.getHistoryProductIds())) {
                List<Integer> historyIds = JSONUtil.toList(user.getHistoryProductIds(), Integer.class);
                resultSet.addAll(historyIds.stream().limit(20).toList());
            }
        }

        // 4. 如果不足 40 个，随机补全
        int need = 40 - resultSet.size();
        if (need > 0) {
            List<Integer> excludeIds = new ArrayList<>(resultSet);
            /*String idListStr = excludeIds.isEmpty() ? "" :
                    excludeIds.stream().map(String::valueOf).collect(Collectors.joining(","));*/

            // 查询随机补全
            List<Integer> randomIds = this.list(Wrappers.lambdaQuery(Product.class).eq(Product::getIsHot, 1)
                    .eq(Product::getProductStatus, ON_SALE.getCode())
                    .eq(Product::getIsDelete, 0)
                    .select(Product::getProductId)
                    .notIn(CollUtil.isNotEmpty(excludeIds), Product::getProductId, excludeIds)
                    .last("ORDER BY RAND() LIMIT " + need)).stream().map(Product::getProductId).toList();
            resultSet.addAll(randomIds);
        }

        return new ArrayList<>(resultSet).stream().limit(40).collect(Collectors.toList());
    }

    @Override
    public List<ProductLookAlsoVO> getLookAlso(ProductRelateDTO dto) {
        // 获取商品详情
        Product product = this.getById(dto.getProductId());
        // 获取商品分类下所有子分类
        // List<Integer> categoryAllChildIds = categoryService.getCategoryAllChildIds(product.getCategoryId());
        List<Integer> categoryAllChildIds = categoryService.getRelatedCategoryIds(product.getCategoryId());
        if (categoryAllChildIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 查询构造器
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Product::getProductId, Product::getProductName, Product::getProductSn, Product::getMarketPrice, Product::getPicThumb, Product::getPicUrl, Product::getShopId, Product::getProductPrice)
                .in(Product::getCategoryId, categoryAllChildIds)
                .eq(Product::getIsDelete, 0)
                .eq(Product::getProductStatus, 1);
        // 判断是什么类型
        String introType = dto.getIntro();
        if (IS_NEW.getDesc().equals(introType)) {
            queryWrapper.eq(Product::getIsNew, 1);
        } else if (IS_BEST.getDesc().equals(introType)) {
            queryWrapper.eq(Product::getIsBest, 1);
        } else {
            // 默认使用热销
            queryWrapper.eq(Product::getIsHot, 1);
        }
        queryWrapper.orderByAsc(Product::getSortOrder)
                .last("limit " + dto.getSize());

        // 获取商品信息
        List<Product> productList = this.list(queryWrapper);

        List<ProductLookAlsoVO> lookAlsoList = this.getProductRelate(productList, dto.getShopId());
        // W6 Wave 3.5：批发商身份命中平台商品时回填首条批发价（CR-20260603-141000 / CD-032）
        // CR-20260603-1720 / CD-035：同步回填 wholesaleCardMoq（卡片可直接加购起批数）
        // CR-20260613-0943：同步回填 wholesaleMoqDimension（起批维度）+ wholesaleMultiple（SPU 兜底倍数）
        // CR-20260615-1017：VO 无零售字段（price 为会员折后价），从 productList 的 Product.productPrice
        // 建零售价 map，折扣模式批发价据此计算（修复折扣模式全 null）
        Map<Integer, BigDecimal> lookAlsoRetailPriceMap = productList.stream()
                .filter(p -> p.getProductId() != null && p.getProductPrice() != null)
                .collect(Collectors.toMap(Product::getProductId, Product::getProductPrice, (a, b) -> a));
        backfillFirstWholesalePriceForListGeneric(
                lookAlsoList,
                ProductLookAlsoVO::getShopId,
                ProductLookAlsoVO::getProductId,
                ProductLookAlsoVO::setFirstWholesalePrice,
                ProductLookAlsoVO::setWholesaleCardMoq,
                ProductLookAlsoVO::setWholesaleMoqDimension,
                ProductLookAlsoVO::setWholesaleMultiple,
                lookAlsoRetailPriceMap);
        return lookAlsoList;
    }

    /**
     * 获取商品相似信息（最终价）
     *
     * @param productList 商品列表
     * @return List<ProductLookAlsoVO>
     */
    @Override
    public List<ProductLookAlsoVO> getProductRelate(List<Product> productList, Integer shopId) {
        if (CollUtil.isEmpty(productList)) {
            return Collections.emptyList();
        }
        // 获取 productId 列表
        List<Integer> list = productList.stream().map(Product::getProductId).toList();

        // 获取对应的 SKU 列表
        List<ProductSku> productSkuList = productSkuService.lambdaQuery()
                .in(ProductSku::getProductId, list)
                .eq(ProductSku::getIsEnabled, Constants.YES)
                .list();

        // 将 SKU 按 productId 分组（方便快速查找第一个 SKU）
        Map<Integer, List<ProductSku>> skuMap = productSkuList.stream()
                .collect(Collectors.groupingBy(ProductSku::getProductId));

        List<ProductLookAlsoVO> result = new ArrayList<>();
        // 遍历 productList，替换 price
        for (Product p : productList) {
            int productId = p.getProductId();
            List<ProductSku> skus = skuMap.get(productId);

            int skuId = 0;
            if (skus != null && !skus.isEmpty()) {
                skuId = skus.getFirst().getSkuId();
            }

            ProductAvailabilityVO detail = this.getProductSkuDetail(productId, skuId, 0, "", shopId, null);
            if (detail != null) {
                ProductLookAlsoVO vo = ProductLookAlsoVO.builder()
                        .marketPrice(p.getMarketPrice().toString())
                        .productName(p.getProductName())
                        .picThumb(p.getPicThumb())
                        .price(detail.getPrice().toString())
                        .productId(p.getProductId())
                        .productSn(p.getProductSn())
                        .picUrl(p.getPicUrl())
                        .shopId(p.getShopId())
                        .build();
                result.add(vo);
            }
        }

        return result;
    }

    @Override
    public RelateRankVO getRelateRank(ProductRelateDTO dto) {
        // 获取商品详情
        Product product = this.getById(dto.getProductId());
        // 判断是否为空
        if (product == null) {
            throw new GlobalException(PRODUCT_NOT_EXIST);
        }
        // 获取商品分类下所有子分类
        // List<Integer> categoryAllChildIds = categoryService.getCategoryAllChildIds(product.getCategoryId());
        List<Integer> categoryAllChildIds = categoryService.getRelatedCategoryIds(product.getCategoryId());
        // 相同价位
        List<Product> priceProductRankData = getProductRankData(dto, product, 1, categoryAllChildIds);
        // 相同品牌
        List<Product> brandProductRankData = getProductRankData(dto, product, 2, categoryAllChildIds);
        // 相同类别
        List<Product> cateProductRankData = getProductRankData(dto, product, 3, categoryAllChildIds);

        return RelateRankVO.builder()
                .price(this.getProductRelate(priceProductRankData, dto.getShopId()))
                .brand(this.getProductRelate(brandProductRankData, dto.getShopId()))
                .cate(this.getProductRelate(cateProductRankData, dto.getShopId()))
                .build();
    }

    /**
     * 获取相关数据
     *
     * @param dto                 查询构造器
     * @param product             商品详情
     * @param type                相关类型
     * @param categoryAllChildIds 分类下所有子分类
     * @return List<Product>
     */
    public List<Product> getProductRankData(ProductRelateDTO dto, Product product, int type, List<Integer> categoryAllChildIds) {
        // 查询构造器
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("product_id", dto.getProductId())
                .eq("is_delete", 0)
                .eq("product_status", 1)
                .last("limit " + dto.getRankNum());

        switch (type) {
            case 1 -> {
                if (categoryAllChildIds.isEmpty()) {
                    return Collections.emptyList();
                }
                queryWrapper.in("category_id", categoryAllChildIds)
                        .select("product_id", "product_name", "product_sn", "market_price", "pic_thumb", "pic_url", StrUtil.format("ABS(market_price - {}) ", product.getMarketPrice()));
                return this.list(queryWrapper);
            }
            case 2 -> {
                queryWrapper.select("product_id", "product_name", "product_sn", "market_price", "pic_thumb", "pic_url")
                        .eq("brand_id", product.getBrandId())
                        .orderByAsc("sort_order");
                return this.list(queryWrapper);
            }
            case 3 -> {
                queryWrapper.select("product_id", "product_name", "product_sn", "market_price", "pic_thumb", "pic_url")
                        .eq("category_id", product.getCategoryId())
                        .orderByAsc("sort_order");
                return this.list(queryWrapper);
            }
        }
        return Collections.emptyList();
    }

    /**
     * 获取相关品牌
     *
     * @param dto 商品相关参数
     * @return List<Brand>
     */
    @Override
    public List<Brand> getRelateBrand(ProductRelateDTO dto) {
        // 获取商品详情
        Product product = this.getById(dto.getProductId());
        // 获取商品分类下所有子分类
        // List<Integer> categoryAllChildIds = categoryService.getCategoryAllChildIds(product.getCategoryId());
        List<Integer> categoryAllChildIds = categoryService.getRelatedCategoryIds(product.getCategoryId());
        if (categoryAllChildIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询构造器
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(
                        "MAX(product_id) as product_id",
                        "MAX(product_name) as product_name",
                        "MAX(product_sn) as product_sn",
                        "MAX(market_price) as market_price",
                        "MAX(pic_thumb) as pic_thumb",
                        "brand_id"
                )
                .in("category_id", categoryAllChildIds)
                .eq("is_delete", 0)
                .eq("product_status", 1)
                .groupBy("brand_id")
                .last("limit " + dto.getSize());
        // 获取相关的品牌ID
        List<Integer> brandIds = this.list(queryWrapper).stream().map(Product::getBrandId).toList();
        if (brandIds.isEmpty()) {
            return List.of();
        }
        return brandService.list(new LambdaQueryWrapper<Brand>()
                .select(Brand::getBrandId, Brand::getBrandName, Brand::getBrandLogo, Brand::getSiteUrl, Brand::getFirstWord)
                .in(Brand::getBrandId, brandIds)
                .eq(Brand::getIsShow, 1)
                .last("limit " + dto.getSize())
                .orderByAsc(Brand::getSortOrder));
    }

    @Override
    public List<ArticleVO> getArticleList(ProductRelateDTO dto) {
        List<ProductArticle> articleList = productArticleService.getProductArticleByProductId(dto.getProductId());
        if (articleList.isEmpty()) {
            return List.of();
        }
        String articleIds = articleList.stream().map(ProductArticle::getArticleId).map(String::valueOf).collect(Collectors.joining(","));

        ArticlePageQuery articlePageQuery = new ArticlePageQuery();
        articlePageQuery.setArticleIds(articleIds);
        articlePageQuery.setPage(1);
        articlePageQuery.setSize(dto.getSize());
        articlePageQuery.setIsShow(1);
        return articleService.list(articlePageQuery).getRecords();
    }

    @Override
    public List<Category> getRelateCategory(ProductRelateDTO dto) {
        // 获取商品详情
        Product product = this.getById(dto.getProductId());
        // 查询构造器
        /*LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getIsShow, 1)
                .eq(Category::getCategoryId, product.getCategoryId())
                .select(Category::getCategoryId, Category::getCategoryName, Category::getParentId);
        List<Category> categoryList = categoryService.list(queryWrapper);
        if (categoryList.isEmpty()) {
            return List.of();
        }
        List<Category> categoryByParentId = categoryService.getCategoryByParentId(categoryList.getFirst().getParentId(), dto.getSize());*/
        List<Integer> categoryAllChildIds = categoryService.getRelatedCategoryIds(product.getCategoryId());
        if (categoryAllChildIds.isEmpty()) {
            return List.of();
        }
        List<Category> categoryList = categoryService.lambdaQuery()
                .in(Category::getCategoryId, categoryAllChildIds)
                .eq(Category::getIsShow, 1)
                .last("limit " + dto.getSize())
                .orderByAsc(Category::getSortOrder)
                .list();
        // 包一层多语言
        categoryList.forEach(category -> category.setCategoryName(translatePackage.translate(category.getCategoryName(), DataType.CATEGORY.getCode())));
        return categoryList;
    }

    @Override
    public ProductDetailVO clientDetail(String sn, Integer shopId, Integer nearestShopId) {
        int productId = 0, skuId = 0;
        if (StrUtil.isNotBlank(sn)) {
            Map<String, Integer> productBySn = getProductBySn(sn);
            productId = productBySn.get("productId");
            skuId = productBySn.get("skuId");
        }
        ProductDetailVO productDetailVO = new ProductDetailVO();
        // 获取商品详情(各种商品类型信息)
        ProductECardGroupDTO productDetail = getProductInfo(productId, true, shopId, nearestShopId);
        productDetailVO.setShopId(productDetail.getShopId());
        ProductInfoDTO productInfoDTO = new ProductInfoDTO();
        BeanUtils.copyProperties(productDetail, productInfoDTO);
        productInfoDTO.setPaidContent(JsonUtil.checkJsonType(productDetail.getPaidContent()));

        // 【预售功能】查询并填充预售信息
        if (productDetail.getSaleMode() != null && productDetail.getSaleMode() != 0) {
            try {
                ProductPresaleVO presaleVO = productPresaleService.getPresaleVOByProductId(productId);
                if (presaleVO != null) {
                    productInfoDTO.setPresaleInfo(presaleVO);
                    productInfoDTO.setSaleMode(productDetail.getSaleMode());
                    productInfoDTO.setProductSaleMode(productDetail.getSaleMode() > 0 ? 1 : 0);
                    // 设置销售模式文本
                    productInfoDTO.setSaleModeText(presaleVO.getSaleModeText());


                    ProductPresale presale = presaleVO.getPresale();
                    // 日期格式按 locale 取整段 pattern（zh 基线 = 原默认；en = yyyy-MM-dd HH:mm）
                    String dateTimeFormat = messageHelper.get("date.format.datetime");
                    String startTimeStr = TigUtils.handelTime(presale.getFinalPaymentStartTime(), dateTimeFormat);
                    String endTimeStr = TigUtils.handelTime(presale.getFinalPaymentEndTime(), dateTimeFormat);
                    // 如果时间是0分0秒，去掉时间部分
                    if (startTimeStr != null && startTimeStr.endsWith(" 00:00")) {
                        startTimeStr = startTimeStr.substring(0, startTimeStr.lastIndexOf(" "));
                    }
                    if (endTimeStr != null && endTimeStr.endsWith(" 00:00")) {
                        endTimeStr = endTimeStr.substring(0, endTimeStr.lastIndexOf(" "));
                    }
                    presaleVO.setFinalPaymentStartTime(startTimeStr);
                    presaleVO.setFinalPaymentEndTime(endTimeStr);

                    String dateFormat = messageHelper.get("date.format.date");
                    presaleVO.setDeliveryDate(TigUtils.handelTime(presale.getDeliveryDate(), dateFormat));
                    String clientType = HeaderUtils.getClientType();
                    presaleVO.setFinalPaymentStartTimeText(buildPresaleFinalPaymentTimeText(presaleVO, clientType, PRESALE_SOURCE_PRODUCT_DETAIL));
                    presaleVO.setDeliveryTimeText(buildPresaleDeliveryTimeText(presaleVO, clientType, PRESALE_SOURCE_PRODUCT_DETAIL));
                }
            } catch (Exception e) {
                log.error("查询商品[{}]预售信息失败", productId, e);
                // 不影响商品详情的正常返回
            }
        }


        // 是否为参与分销的分销商品（salesman_product 已配置且 isJoin=1），用于分享时携带分销员ID
        try {
            SalesmanProduct salesmanProduct = salesmanProductMapper.selectOne(
                    Wrappers.lambdaQuery(SalesmanProduct.class)
                            .eq(SalesmanProduct::getProductId, productInfoDTO.getProductId())
                            .last("LIMIT 1"));
            productInfoDTO.setIsDistribution(salesmanProduct != null
                    && salesmanProduct.getIsJoin() != null
                    && salesmanProduct.getIsJoin() == 1);
        } catch (Exception e) {
            log.warn("查询商品分销状态失败, productId: {}, error: {}", productInfoDTO.getProductId(), e.getMessage());
            productInfoDTO.setIsDistribution(false);
        }

        productDetailVO.setItem(productInfoDTO);

        // 判断商品是否存在
        if (productDetail.getIsDelete() == 1 || productDetail.getProductStatus() != 1) {
            throw new GlobalException(PRODUCT_NOT_EXIST);
        }
        // 获取商品图文详情
        List<ProductDescDTO> productDescArr = getProductDescArr(productDetail.getProductDesc());
        productDetailVO.setDescArr(productDescArr);
        // 获取sku列表
        List<ProductSkuDTO> productSkusByPid = productSkuService.getProductSkusByPid(productId);
        productDetailVO.setSkuList(productSkusByPid);
        // 获取相册列表
        List<ProductGallery> productGalleryList = productDetail.getShopId() != null
                && productDetail.getShopId() > 0
                && isStoreAssignProductImageEnabled()
                ? productGalleryService.getProductGalleryList(productId, Long.valueOf(productDetail.getShopId()))
                : productGalleryService.getProductGalleryList(productId);
        productDetailVO.setPicList(productGalleryList);
        List<ProductVideo> productVideo = productVideoService.lambdaQuery().eq(ProductVideo::getProductId, productId).list();
        productDetailVO.setVideoList(productVideo);

        // 获取属性列表
        ProductAttrsDTO productAttributes = productAttributesService.getProductAttributes(productId);
        filterDisabledSkuAttributes(productAttributes, productSkusByPid);
        productDetailVO.setAttrList(productAttributes);

        // 获取商品评论评分详情
        // D1146：评价功能关闭时整个 rankDetail 不下发（保持 null），前端已有空值兜底；
        // 相比下发零值对象，可避免漏改某个触点时露出「0 条评价 / 0% 好评」。
        if (commentService.isProductCommentEnabled()) {
            ProductCommentStatisticVO productCommentDetail = commentService.getProductCommentDetail(productId, shopId);
            productDetailVO.setRankDetail(productCommentDetail);
        }
        // 获取商品秒杀信息
        List<SeckillItemInfoDTO> seckillInfo = seckillService.getSeckillInfo(productId, shopId);
        if (tigshopProperties.getIsO2o() == 1) {
            // 移除非 shopId 的秒杀活动
            seckillInfo = new ArrayList<>(seckillInfo);
            seckillInfo.removeIf(seckillItemInfoDTO ->
                    !seckillItemInfoDTO.getShopId().equals(shopId == null ? 0 : shopId)
            );
        }
        // CR-20260714-1335：批发商买「有批发价」的商品，详情页隐藏 C 端活动（秒杀/拼团），只看批发价
        // 同 backfillWholesalePriceList：门店上下文用解析后的实际门店，入参 shopId 可能为空
        boolean wholesaleHideActivity = isWholesalePricedForUser(
                productId, SecurityUtils.getCurrentUserId(), productDetailVO.getShopId());
        if (wholesaleHideActivity) {
            seckillInfo = Collections.emptyList();
        }
        productDetailVO.setSeckillDetail(seckillInfo);
        // 获取商品拼团信息
        GrouponItemInfoVO grouponInfo = grouponPromotionService.getGrouponInfo(productId);
        if (wholesaleHideActivity) {
            grouponInfo = null;
        }
        if (Objects.nonNull(grouponInfo)) {
            Integer grouponShopId = grouponInfo.getShopId();
            if (tigshopProperties.getIsO2o() == 1) {
                if (Objects.equals(grouponShopId, shopId) || shopId == null) {
                    //是同一个所属店铺才显示
                    productDetailVO.setGrouponDetail(grouponInfo);
                }
            } else {
                productDetailVO.setGrouponDetail(grouponInfo);
            }
        }


        // 获取商品服务信息
        List<Services> serviceList = getServiceList(productId);
        productDetailVO.setServiceList(serviceList);
        // 获取默认选择的属性
        List<String> selectValue = getSelectValue(skuId);
        productDetailVO.setCheckedValue(selectValue);
        // 获取商品咨询量
        int productFeedbackCount = getProductFeedbackCount(productId, productDetail.getShopId());
        productDetailVO.setConsultationTotal(productFeedbackCount);

        // 【周期购功能】填充周期购配置信息（在外层返回）

        //预约商品
        // 预约商品库存处理：根据预约日历计算未来时间的可用库存总和
        if (tigshopProperties.getIsO2o() == 1 && ProductType.isReservationProduct(productDetail.getProductType())) {
            try {
                List<Integer> calendarShopIds = resolveReservationCalendarShopIdsForDetail(
                        productId,
                        productDetailVO.getShopId(),
                        shopId,
                        nearestShopId
                );
                Integer calendarShopId;
                if (shopId != null && shopId > 0 && calendarShopIds.contains(shopId)) {
                    calendarShopId = shopId;
                } else {
                    calendarShopId = selectReservationCalendarShopId(productId, calendarShopIds);
                }
                log.warn("预约详情库存选店, productId={}, requestShopId={}, nearestShopId={}, candidateShopIds={}, selectedShopId={}",
                        productId, shopId, nearestShopId, calendarShopIds, calendarShopId);
                if (calendarShopId != null) {
                    productDetailVO.setShopId(calendarShopId);
                    if (productDetailVO.getItem() != null) {
                        productDetailVO.getItem().setShopId(calendarShopId);
                    }
                }
                List<ProductSkuDTO> productSkuList = productDetailVO.getSkuList();
                int totalAvailableStock;
                if (CollUtil.isNotEmpty(productSkuList)) {
                    totalAvailableStock = 0;
                    for (ProductSkuDTO productSku : productSkuList) {
                        int skuStock = calculateReservationSkuStockForDetailDisplay(productId, calendarShopId, productSku.getSkuId());
                        productSku.setSkuStock(skuStock);
                        totalAvailableStock += skuStock;
                    }
                    if (totalAvailableStock <= 0) {
                        int sharedStock = calculateReservationSharedStockForDetailDisplay(productId, calendarShopId);
                        if (sharedStock > 0) {
                            productSkuList.forEach(sku -> sku.setSkuStock(sharedStock));
                            totalAvailableStock = sharedStock;
                            log.warn("预约SKU库存命中统一规格兼容库存, productId={}, shopId={}, sharedStock={}, skuCount={}",
                                    productId, calendarShopId, sharedStock, productSkuList.size());
                        }
                    }
                } else {
                    totalAvailableStock = calculateReservationTotalStockForDetailDisplay(productId, calendarShopId);
                }
                log.warn("预约详情库存计算完成, productId={}, selectedShopId={}, totalAvailableStock={}, skuCount={}",
                        productId, calendarShopId, totalAvailableStock, CollUtil.isEmpty(productSkuList) ? 0 : productSkuList.size());
                if (totalAvailableStock > 0) {
                    productDetailVO.getItem().setProductStock(totalAvailableStock);
                } else {
                    productDetailVO.getItem().setProductStock(0);
                }
            } catch (GlobalException e) {
                // 获取库存日历失败，设置库存为0
                log.error("获取库存日历失败", e);
                productDetailVO.getItem().setProductStock(0);
                if (CollUtil.isNotEmpty(productDetailVO.getSkuList())) {
                    productDetailVO.getSkuList().forEach(sku -> sku.setSkuStock(0));
                }
            }
        }

        // 门店上下文取解析后的实际门店（getProductInfo 在 O2O 下已按 shopId / nearestShopId /
        // store_product 首个可用门店解析并覆写），入参 shopId 可能为空（前端只传 nearestShopId）
        backfillWholesalePriceList(productDetailVO, productId, productDetailVO.getShopId());

        return productDetailVO;
    }

    private void filterDisabledSkuAttributes(ProductAttrsDTO productAttributes, List<ProductSkuDTO> enabledSkus) {
        if (productAttributes == null || CollUtil.isEmpty(productAttributes.getSpe())) {
            return;
        }
        Map<String, Set<String>> enabledValues = new HashMap<>();
        if (CollUtil.isNotEmpty(enabledSkus)) {
            enabledSkus.stream()
                    .map(ProductSkuDTO::getSkuData)
                    .filter(CollUtil::isNotEmpty)
                    .flatMap(List::stream)
                    .filter(data -> StrUtil.isNotBlank(data.getName()) && StrUtil.isNotBlank(data.getValue()))
                    .forEach(data -> enabledValues
                            .computeIfAbsent(data.getName(), key -> new HashSet<>())
                            .add(data.getValue()));
        }
        productAttributes.getSpe().forEach(spec -> {
            Set<String> values = enabledValues.getOrDefault(spec.getAttrName(), Collections.emptySet());
            if (CollUtil.isNotEmpty(spec.getAttrList())) {
                spec.setAttrList(spec.getAttrList().stream()
                        .filter(attr -> values.contains(attr.getAttrValue()))
                        .toList());
            }
        });
        productAttributes.setSpe(productAttributes.getSpe().stream()
                .filter(spec -> CollUtil.isNotEmpty(spec.getAttrList()))
                .toList());
    }

    public String buildPresaleFinalPaymentTimeText(ProductPresaleVO presaleVO, String clientType, String source) {
        if (presaleVO == null) {
            return "";
        }
        String normalizedSource = normalizePresaleSource(source);
        if (Integer.valueOf(1).equals(presaleVO.getFinalPaymentType())) {
            if (PRESALE_SOURCE_PRODUCT_DETAIL.equals(normalizedSource)) {
                return translatePresaleTemplate(
                        "付定金后{daysText}开始支付尾款，并需要在{durationText}内完成支付",
                        Map.of(
                                "daysText", buildPresaleDurationText(presaleVO.getFinalPaymentRelativeDays(), null),
                                "durationText", buildPresaleDurationText(presaleVO.getFinalPaymentDurationDays(), presaleVO.getFinalPaymentDurationHours())
                        )
                );
            }
            boolean hasRelativeDays = presaleVO.getFinalPaymentRelativeDays() != null && presaleVO.getFinalPaymentRelativeDays() > 0;
            String durationText = buildPresaleDurationText(presaleVO.getFinalPaymentDurationDays(), presaleVO.getFinalPaymentDurationHours());
            boolean hasDuration = StrUtil.isNotBlank(durationText);
            if (isPcClient(clientType)) {
                if (hasRelativeDays && hasDuration) {
                    return translatePresaleTemplate(
                            "付定金{daysText}后开始, {durationText}结束",
                            Map.of(
                                    "daysText", buildPresaleDurationText(presaleVO.getFinalPaymentRelativeDays(), null),
                                    "durationText", durationText
                            )
                    );
                }
                if (hasRelativeDays) {
                    return translatePresaleTemplate(
                            "付定金{daysText}后开始",
                            Map.of("daysText", buildPresaleDurationText(presaleVO.getFinalPaymentRelativeDays(), null))
                    );
                }
                if (hasDuration) {
                    return translatePresaleTemplate(
                            "{durationText}结束",
                            Map.of("durationText", durationText)
                    );
                }
                return "";
            }
            if (PRESALE_SOURCE_ORDER_DETAIL.equals(normalizedSource) && hasDuration) {
                if (hasRelativeDays) {
                    return translatePresaleTemplate(
                            "付定金{days}天后开始, {durationText}结束",
                            Map.of("days", String.valueOf(presaleVO.getFinalPaymentRelativeDays()), "durationText", durationText)
                    );
                }
                return translatePresaleTemplate(
                        "付定金后开始, {durationText}结束",
                        Map.of("durationText", durationText)
                );
            }
            if (hasRelativeDays) {
                return translatePresaleTemplate(
                        "付定金{days}天后开始",
                        Map.of("days", String.valueOf(presaleVO.getFinalPaymentRelativeDays()))
                );
            }
            return translatePresaleTemplate("付定金后开始", Collections.emptyMap());
        }

        if (PRESALE_SOURCE_PRODUCT_DETAIL.equals(normalizedSource)
                && StrUtil.isNotBlank(presaleVO.getFinalPaymentStartTime())
                && StrUtil.isNotBlank(presaleVO.getFinalPaymentEndTime())) {
            return translatePresaleTemplate(
                    "{startTime}-{endTime}",
                    Map.of(
                            "startTime", StrUtil.nullToDefault(presaleVO.getFinalPaymentStartTime(), ""),
                            "endTime", StrUtil.nullToDefault(presaleVO.getFinalPaymentEndTime(), "")
                    )
            );
        }
        if (StrUtil.isNotBlank(presaleVO.getFinalPaymentStartTime())) {
            return translatePresaleTemplate(
                    "{startTime}开始",
                    Map.of("startTime", StrUtil.nullToDefault(presaleVO.getFinalPaymentStartTime(), ""))
            );
        }
        return "";
    }

    public String buildPresaleDeliveryTimeText(ProductPresaleVO presaleVO, String clientType, String source) {
        if (presaleVO == null) {
            return "";
        }
        String normalizedSource = normalizePresaleSource(source);
        if (Integer.valueOf(1).equals(presaleVO.getDeliveryType())) {
            boolean isSaleMode2 = Integer.valueOf(2).equals(presaleVO.getSaleMode());
            String deliveryText = buildPresaleDurationText(presaleVO.getDeliveryRelativeDays(), presaleVO.getDeliveryRelativeHours());
            if (isSaleMode2) {
                return translatePresaleTemplate(
                        "付尾款{deliveryText}后发货",
                        Map.of("deliveryText", deliveryText)
                );
            }
            boolean useFullTemplate = !isPcClient(clientType)
                    && (PRESALE_SOURCE_ORDER_SUBMIT.equals(normalizedSource) || PRESALE_SOURCE_ORDER_DETAIL.equals(normalizedSource));
            if (useFullTemplate) {
                return translatePresaleTemplate(
                        "付全款{deliveryText}后发货",
                        Map.of("deliveryText", deliveryText)
                );
            }
            return translatePresaleTemplate(
                    "付全款{deliveryText}后",
                    Map.of("deliveryText", deliveryText)
            );
        }
        return translatePresaleTemplate(
                "{deliveryDate}开始",
                Map.of("deliveryDate", StrUtil.nullToDefault(presaleVO.getDeliveryDate(), ""))
        );
    }

    private  String formatPresaleDuration(Integer value, String unitKey) {
        if (value == null || value <= 0) {
            return "";
        }
        return messageHelper.get(unitKey, value);
    }

    private String buildPresaleDurationText(Integer days, Integer hours) {
        return formatPresaleDuration(days, "date.duration.day") + formatPresaleDuration(hours, "date.duration.hour");
    }

    private String normalizePresaleSource(String source) {
        if (PRESALE_SOURCE_ORDER_SUBMIT.equals(source) || PRESALE_SOURCE_ORDER_DETAIL.equals(source)) {
            return source;
        }
        return PRESALE_SOURCE_PRODUCT_DETAIL;
    }

    private boolean isPcClient(String clientType) {
        return "pc".equalsIgnoreCase(clientType);
    }

    private String translatePresaleTemplate(String template, Map<String, String> placeholders) {
        String translated = translatePackage.translate(template);
        if (placeholders == null || placeholders.isEmpty()) {
            return translated;
        }
        String result = translated;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", StrUtil.nullToDefault(entry.getValue(), ""));
        }
        return result;
    }

    private void translateProductListNames(List<ProductListResDTO> productListRes) {
        if (CollUtil.isEmpty(productListRes) || tigshopProperties.getIsOverseas() == 0) {
            return;
        }
        List<String> names = productListRes.stream()
                .map(ProductListResDTO::getProductName)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();
        if (names.isEmpty()) {
            return;
        }
        Map<String, String> translated = translatePackage.translateBatch(
                names, DataType.API.getCode(), ThreadLocalUtil.getValue());
        productListRes.forEach(product -> {
            String name = product.getProductName();
            if (StrUtil.isNotBlank(name)) {
                product.setProductName(translated.getOrDefault(name, name));
            }
        });
    }




    @Override
    public List<ProductListResDTO> historyProduct(List<Integer> historyProductIds) {
        if (CollUtil.isEmpty(historyProductIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Product::getProductId, historyProductIds);
        //根据historyProductIds顺序排序
        queryWrapper.last("ORDER BY field(product_id, " + historyProductIds.stream().map(String::valueOf).
                collect(Collectors.joining(",")) + ") limit 20");
        //根据 historyProductIds 排序
        List<Product> productList = this.list(queryWrapper);
        // 查询商品规格
        List<ProductSkuDTO> productSkus = productSkuService.getProductSkusByProductIds(historyProductIds);
        // 将商品规格按商品 ID 分组
        Map<Integer, List<ProductSkuDTO>> productSkuMap = productSkus.stream()
                .collect(Collectors.groupingBy(ProductSkuDTO::getProductId));
        List<ProductListResDTO> voList = productList.stream()
                .map(product -> {
                    ProductListResDTO dto = convertToDTO(product);
                    List<ProductSkuDTO> skus = productSkuMap.get(product.getProductId());
                    dto.setProductSku(skus);
                    dto.setSkuCount(skus == null ? 0 : skus.size());

                    // 获取最终价格
                    int skuId = 0;
                    if (skus != null && !skus.isEmpty()) {
                        skuId = skus.getFirst().getSkuId();
                    }
                    ProductAvailabilityVO detail = this.getProductSkuDetail(product.getProductId(), skuId, 0, "", null, null);
                    dto.setPrice(detail.getPrice());
                    return dto;
                })
                .collect(Collectors.toList());
        // CR-20260615-0930：补齐 CR-0943 漏掉的端点——与 getHistoryProduct(6749) 同口径回填批发字段
        // （firstWholesalePrice / wholesaleCardMoq / wholesaleMoqDimension / wholesaleMultiple）。
        backfillFirstWholesalePriceForList(voList);
        return voList;
    }


    public ProductECardGroupDTO getProductInfo(int id, boolean isDetail, Integer shopId, Integer nearestShopId) {
        // 连表查询
        MPJLambdaWrapper<Product> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.eq(Product::getProductId, id)
                .selectAll(Product.class)
                .selectAll(ECardGroup.class)
                .leftJoin(ECardGroup.class, ECardGroup::getGroupId, Product::getCardGroupId);
        ProductECardGroupDTO dto = this.selectJoinOne(ProductECardGroupDTO.class, queryWrapper);

        if (dto == null) {
            return new ProductECardGroupDTO(); // 确保返回非 null
        }

        // 补充业务逻辑
        isSeckill(id, dto);        // 判断是否秒杀
        isBuy(id, dto);            // 判断付费内容
        updateStockByECard(dto);   // 更新卡密库存

        // 浏览记录
        int currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId > 0 && isDetail) {
            historyProductRecord(id, currentUserId);
        }

        return dto;
    }

    /**
     * 构建连表查询条件
     */
    private MPJLambdaWrapper<StoreProduct> buildStoreProductQuery(int productId, Integer shopId) {
        MPJLambdaWrapper<StoreProduct> wrapper = new MPJLambdaWrapper<>();
        wrapper.eq(StoreProduct::getProductId, productId)
                .selectAll(StoreProduct.class)
                .selectAll(ECardGroup.class)
                .leftJoin(ECardGroup.class, ECardGroup::getGroupId, StoreProduct::getCardGroupId);

        if (shopId != null) {
            wrapper.eq(StoreProduct::getShopId, shopId);
        }
        wrapper.eq(StoreProduct::getProductStatus, Constants.YES);
        wrapper.last("limit 1"); // 没有 shopId 时只取一个
        return wrapper;
    }


    private boolean isStoreAssignProductImageEnabled() {
        ConfigPO config = configService.getConfigByCode(SettingsEnum.STORE_ASSIGN_PRODUCT_IMAGE.getBizCode());
        return config != null && "1".equals(config.getBizVal());
    }

    private boolean isStoreAssignProductImageEnabled(GetStoreVO settings) {
        return settings != null && Objects.equals(settings.getStoreAssignProductImage(), 1);
    }

    private String resolveStorePicUrl(ProductSaveParam param) {
        if (param == null) {
            return null;
        }
        if (CollUtil.isNotEmpty(param.getImgList())) {
            return param.getImgList().getFirst().getPicUrl();
        }
        if (ObjectUtil.isNotEmpty(param.getPicUrl())) {
            return param.getPicUrl();
        }
        return null;
    }

    private String resolveStoreProductDesc(ProductSaveParam param) {
        if (param == null) {
            return null;
        }
        String productDesc = ProductDescDTO.getProductDesc(param.getProductDescArr());
        return StrUtil.isNotEmpty(productDesc) ? productDesc : param.getProductDesc();
    }


    /**
     * 获取商品详情（前台详情用法）
     *
     * @param id 商品ID
     * @return ProductECardGroupDTO
     */
    public ProductECardGroupDTO getProductDetail(int id, boolean isDetail, Integer shopId) {
        return getProductDetail(id, isDetail, shopId, false);
    }

    /**
     * @param skipRuntimeChecks 列表路径为 true：跳过 isSeckill/isBuy 等列表输出不使用的逐条运行时查询
     */
    public ProductECardGroupDTO getProductDetail(int id, boolean isDetail, Integer shopId, boolean skipRuntimeChecks) {
        return getProductDetail(id, isDetail, shopId, skipRuntimeChecks, null);
    }

    /**
     * @param listCtx 列表路径传入整页上下文以复用配置读取；为 null 时行为与原详情路径完全一致
     */
    private ProductECardGroupDTO getProductDetail(int id, boolean isDetail, Integer shopId, boolean skipRuntimeChecks, ProductListDetailContext listCtx) {

        // 连表查询
        MPJLambdaWrapper<Product> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.eq(Product::getProductId, id)
                .selectAll(Product.class)
                .selectAll(ECardGroup.class)
                .leftJoin(ECardGroup.class, ECardGroup::getGroupId, Product::getCardGroupId);
        ProductECardGroupDTO dto = this.selectJoinOne(ProductECardGroupDTO.class, queryWrapper);
        if (dto == null) {
            return new ProductECardGroupDTO();
        }

        if (!skipRuntimeChecks) {
            // 判断商品是否参与秒杀
            isSeckill(id, dto);
            // 付费内容商品在用户购买过之后才可以显示
            isBuy(id, dto);
        }
        // 卡密商品根据卡券修改库存
        updateStockByECard(dto);
        // 添加浏览记录
        int currentUserId = SecurityUtils.getCurrentUserId();
        if (!skipRuntimeChecks && SecurityUtils.getCurrentUserId() > 0 && isDetail) {
            historyProductRecord(id, currentUserId);
        }
        return dto;
    }

    /**
     * 添加浏览记录
     *
     * @param productId 商品id
     * @param userId    用户id
     */
    public void historyProductRecord(int productId, int userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 使用 LinkedList 提升头部操作性能
        LinkedList<Integer> historyProductIds = new LinkedList<>();

        // 如果数据库字段不为空则填充数据
        if (StrUtil.isNotBlank(user.getHistoryProductIds())) {
            List<Integer> temp = StringUtils.str2IntList(user.getHistoryProductIds());
            if (temp != null) {
                historyProductIds.addAll(temp);
            }
        }

        Integer target = productId;
        historyProductIds.remove(target);
        historyProductIds.addFirst(target);

        // 优化去重方式（保持顺序）
        LinkedHashSet<Integer> uniqueSet = new LinkedHashSet<>(historyProductIds);
        historyProductIds.clear();
        historyProductIds.addAll(uniqueSet);

        // 限制历史记录数量（可选）
        while (historyProductIds.size() > 20) {
            historyProductIds.removeLast();
        }

        user.setHistoryProductIds(JsonUtil.toJson(historyProductIds));
        userMapper.updateById(user);
    }

    /**
     * 卡密商品根据卡券修改库存
     *
     * @param dto 商品详情
     */
    public void updateStockByECard(ProductECardGroupDTO dto) {
        Integer productType = dto.getProductType();
        if (productType != null && productType == PRODUCT_TYPE_CARD.getCode()) {
            LambdaQueryWrapper<ECard> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ECard::getGroupId, dto.getCardGroupId())
                    .eq(ECard::getIsUse, 0);
            long count = eCardService.count(queryWrapper);
            dto.setProductStock((int) count);
        }
    }

    /**
     * 判断商品是否付费
     *
     * @param id  商品ID
     * @param dto 商品详情
     */
    public void isBuy(int id, ProductECardGroupDTO dto) {
        // 付费内容商品在用户购买过之后才可以显示
        int isBuy = 0;
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId > 0) {
            MPJLambdaWrapper<Order> queryWrapper = new MPJLambdaWrapper<>();
            queryWrapper.leftJoin(OrderItem.class, OrderItem::getOrderId, Order::getOrderId)
                    .eq(Order::getUserId, currentUserId)
                    .eq(OrderItem::getProductId, id)
                    .eq(Order::getPayStatus, PAID.getCode())
                    .eq(Order::getIsDel, 0);

            Long count = orderMapper.selectJoinCount(queryWrapper);
            if (count > 0) {
                isBuy = 1;
            }
        }

        if (isBuy == 0) {
            dto.setPaidContent("");
        }
        dto.setIsBuy(isBuy);
    }

    /**
     * 判断商品是否付费
     */
    @Override
    public Integer isBuy(Integer userId, int id) {
        // 付费内容商品在用户购买过之后才可以显示
        int isBuy = 0;
        Integer currentUserId = userId == null ? SecurityUtils.getCurrentUserId() : userId;
        if (currentUserId > 0) {
            MPJLambdaWrapper<Order> queryWrapper = new MPJLambdaWrapper<>();
            queryWrapper.leftJoin(OrderItem.class, OrderItem::getOrderId, Order::getOrderId)
                    .eq(Order::getUserId, currentUserId)
                    .eq(OrderItem::getProductId, id)
                    .eq(Order::getPayStatus, PAID.getCode())
                    .eq(Order::getIsDel, 0);

            Long count = orderMapper.selectJoinCount(queryWrapper);
            if (count > 0) {
                isBuy = 1;
            }
        }
        return isBuy;
    }

    /**
     * 判断商品是否参与秒杀
     *
     * @param id  商品ID
     * @param dto 商品信息
     */
    public void isSeckill(int id, ProductECardGroupDTO dto) {
        LambdaQueryWrapper<Seckill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Seckill::getProductId, id)
                .eq(tigshopProperties.getIsO2o() == 1, Seckill::getShopId, dto.getShopId())
                .le(Seckill::getSeckillStartTime, StringUtils.getCurrentTime())
                .ge(Seckill::getSeckillEndTime, StringUtils.getCurrentTime());
        long count = seckillService.count(queryWrapper);
        if (count > 0) {
            dto.setIsSeckill(1);
        } else {
            dto.setIsSeckill(0);
        }
    }

    /**
     * 根据商品编号获取商品信息
     *
     * @param sn 商品编号
     * @return Map<String, Integer>
     */
    public Map<String, Integer> getProductBySn(String sn) {
        // 定义变量
        int productId = 0, skuId = 0;
        // SKU 条码优先，避免与商品条码重复时无法精确定位规格
        ProductSku productSkusBySn = productSkuService.getProductSkusBySn(sn);
        if (productSkusBySn != null) {
            productId = productSkusBySn.getProductId();
            skuId = productSkusBySn.getSkuId();
            return Map.of("productId", productId, "skuId", skuId);
        }
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper.eq(Product::getProductSn, sn).or().eq(Product::getProductTsn, sn));
        Product product = this.getOne(queryWrapper);
        if (product != null) {
            productId = product.getProductId();
            return Map.of("productId", productId, "skuId", skuId);
        }
        return Map.of("productId", productId, "skuId", skuId);
    }

    /**
     * 获取商品服务
     *
     * @param id 商品ID
     * @return List<Services>
     */
    public List<Services> getServiceList(int id) {
        Product product = this.getById(id);
        if (product == null) {
            throw new GlobalException(PRODUCT_NOT_EXIST);
        }
        // 商品服务，需要转成集合
        String productServiceIds = product.getProductServiceIds();
        if (StrUtil.isEmpty(productServiceIds)) {
            return List.of();
        }
        LambdaQueryWrapper<Services> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(!StringUtils.str2IntList(productServiceIds).isEmpty(),
                Services::getProductServiceId, StringUtils.str2IntList(productServiceIds));
        queryWrapper
                .eq(Services::getDefaultOn, 1)
                .orderByAsc(Services::getSortOrder);
        return servicesService.list(queryWrapper);
    }

    /**
     * 获取默认选择的属性
     *
     * @param skuId 商品规格ID
     * @return List<String>
     */
    public List<String> getSelectValue(int skuId) {
        if (skuId == 0) {
            return List.of();
        }
        ProductSku productSku = productSkuService.lambdaQuery()
                .eq(ProductSku::getSkuId, skuId)
                .eq(ProductSku::getIsEnabled, Constants.YES)
                .one();
        ProductSkuDTO productSkuDTO = productSkuService.convertToDTO(productSku);
        if (productSkuDTO == null) {
            return List.of();
        }
        List<ProductSkuDTO.SkuData> skuDataList = productSkuDTO.getSkuData();
        if (skuDataList.isEmpty()) {
            return List.of();
        }
        // 循环拼接字符串
        List<String> list = new ArrayList<>();
        for (ProductSkuDTO.SkuData skuData : skuDataList) {
            list.add(StrUtil.format("{}:{}", skuData.getName(), skuData.getValue()));
        }
        return list;
    }

    /**
     * 获取商品反馈总数
     *
     * @param productId 商品ID
     * @return int
     */
    public int getProductFeedbackCount(int productId, Integer shopId) {
        return feedbackShopResolver.countProductConsultations(productId, shopId);
    }

    @Override
    public Boolean isCollect(int productId, Integer shopId) {
        Product product = this.getById(productId);
        if (product == null) {
            return false;
        }
        LambdaQueryWrapper<CollectProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CollectProduct::getProductId, productId)
                .eq(shopId != null, CollectProduct::getShopId, shopId)
                .eq(CollectProduct::getUserId, SecurityUtils.getCurrentUserId());
        long count = collectProductMapper.selectCount(queryWrapper);
        return count > 0;
    }

    @Override
    public List<CategoryParentTreeDTO> parentTree(Integer categoryId) {
        if (categoryId == null) {
            return List.of();
        }
        List<CategoryParentTreeDTO> categoryTrees = new ArrayList<>();

        // 防循环引用
        Set<Integer> visitedIds = new HashSet<>();
        // 最大循环次数限制为4次
        final int MAX_LOOPS = 4;
        // 当前循环计数
        int loopCount = 0;

        while (categoryId != 0 && loopCount < MAX_LOOPS) {
            // 检测循环引用（即使没到4次，出现循环也立即终止）
            if (visitedIds.contains(categoryId)) {
                log.error("检测到分类循环引用，提前终止。当前ID: {}", categoryId);
                break;
            }
            visitedIds.add(categoryId);

            Category category = categoryService.getById(categoryId);
            if (category == null) {
                break;
            }
            Integer parentId = category.getParentId();
            List<Category> alikeParentList = categoryService.list(
                    new LambdaQueryWrapper<Category>()
                            .select(Category::getCategoryId, Category::getCategoryName, Category::getParentId)
                            .eq(Category::getParentId, parentId)
                            .eq(Category::getIsShow, 1)
            );
            categoryTrees.add(new CategoryParentTreeDTO(category, alikeParentList));
            categoryId = parentId;
            // 每次循环计数+1
            loopCount++;
        }
        // 记录超过最大次数的情况（便于排查是否有过深层级）
        if (loopCount >= MAX_LOOPS) {
            log.error("已达到最大循环次数({}次)，终止遍历。最后处理的分类ID: {}", MAX_LOOPS, categoryId);
        }
        categoryTrees.sort(Comparator.comparingInt(CategoryParentTreeDTO::getParentId));
        return categoryTrees;
    }

    @Override
    public List<CouponDetailVO> getCouponList(Integer productId, Integer shopId) {
        ProductDetailDTO productDetail = detail(productId);

        List<CouponDetailVO> productCouponList = couponService.getProductCouponList(productId, tigshopProperties.getIsO2o() == 1 ? shopId : productDetail.getShopId());

        if (CollUtil.isEmpty(productCouponList)) {
            return productCouponList;
        }

        Integer userId = SecurityUtils.getCurrentUserId();
        return filterDisplayableProductCoupons(productCouponList, userId);
    }

    /**
     * 统一商品优惠券弹窗与商详营销区域的最终展示资格。
     */
    private List<CouponDetailVO> filterDisplayableProductCoupons(List<CouponDetailVO> productCouponList, Integer userId) {
        if (CollUtil.isEmpty(productCouponList)) {
            return productCouponList;
        }
        boolean loggedIn = userId != null && userId > 0;

        // 用户维度数据仅登录时查询一次，避免 N+1；会员标记规则已在 getProductCouponList 查询层过滤
        Integer currentUserRankId = null;
        long paidOrderCount = 0L;
        Map<Integer, Long> totalCountMap = Collections.emptyMap();
        Map<Integer, Long> unusedCountMap = Collections.emptyMap();
        if (loggedIn) {
            currentUserRankId = userMapper.selectById(userId).getRankId();
            // 已支付订单数（新人专享判定，与优惠券领取校验 claim() 对齐：payStatus > 0）
            paidOrderCount = orderMapper.selectCount(Wrappers.lambdaQuery(Order.class)
                    .eq(Order::getUserId, userId)
                    .gt(Order::getPayStatus, 0));

            // 当前用户所有领取记录（含已使用）
            List<UserCoupon> userCoupons = userCouponService.list(
                    new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getUserId, userId));
            if (CollUtil.isNotEmpty(userCoupons)) {
                totalCountMap = userCoupons.stream()
                        .collect(Collectors.groupingBy(UserCoupon::getCouponId, Collectors.counting()));
                unusedCountMap = userCoupons.stream()
                        .filter(uc -> uc.getUsedTime() == null || uc.getUsedTime() == 0L)
                        .collect(Collectors.groupingBy(UserCoupon::getCouponId, Collectors.counting()));
            }
        }

        List<CouponDetailVO> result = new ArrayList<>();
        List<CouponDetailVO> receivedList = new ArrayList<>();

        for (CouponDetailVO productCoupon : productCouponList) {
            Integer couponId = productCoupon.getCouponId();
            int limitNum = productCoupon.getLimitNum();
            int sendNum = productCoupon.getSendNum();

            // 规则①·发放总量已满（非用户维度，登录/未登录均过滤）→ 不显示
            if (sendNum > 0 && productCoupon.getReceiveNum() >= sendNum) {
                continue;
            }

            long totalClaimed = totalCountMap.getOrDefault(couponId, 0L);
            long unusedClaimed = unusedCountMap.getOrDefault(couponId, 0L);

            // 用户维度的领取规则与领取次数限制，仅登录用户可判定
            if (loggedIn) {
                // 规则①·新人专享：非新人（已有支付订单）不显示
                if (productCoupon.getIsNewUser() == 1 && paidOrderCount > 0) {
                    continue;
                }
                // 规则①·会员等级专享：用户等级不在限制范围内不显示
                if (productCoupon.getIsNewUser() == 2) {
                    Integer[] limitUserRank = productCoupon.getLimitUserRank();
                    if (limitUserRank != null && limitUserRank.length > 0
                            && !Arrays.asList(limitUserRank).contains(currentUserRankId)) {
                        continue;
                    }
                }
                // 达到个人领取上限后仍展示，由 isReceive 标记为已领取并在前端置灰
                if (limitNum > 0 && totalClaimed >= limitNum) {
                    productCoupon.setIsReceive(1);
                    receivedList.add(productCoupon);
                    continue;
                }
            }

            // 规则②④·正常显示并能领取；保留“已领取”标记，已领取的排在后面
            if (unusedClaimed > 0 && limitNum > 0) {
                productCoupon.setIsReceive(1);
                receivedList.add(productCoupon);
            } else {
                productCoupon.setIsReceive(0);
                result.add(productCoupon);
            }
        }

        // 已领取的排在后面
        result.addAll(receivedList);

        return result;
    }

    private BigDecimal getProductPrice(ProductListResDTO dto, Integer productId, Integer skuId, List<UserRank> userRankList, Integer rankId, Integer shopId, boolean minGrouponPrice, Set<Integer> pointsExchangeProductIds, ProductSkuDTO firstSku, Map<String, ProductSaveParam> batchPromotions, Map<String, SeckillItem> seckillItemMap, Map<String, TimeDiscountItem> timeDiscountItemMap, Map<Integer, BigDecimal> grouponMinPriceMap) {
        // 优化：直接使用已有的商品价格，避免重复查询getProductDetail
        BigDecimal productPrice = dto.getProductPrice();
        // 商品的店铺ID（用于促销查询）
        Integer productShopId = dto.getShopId();

        // 判断是否需要查询门店SKU价格：O2O模式 + 有shopId + 商品是总部商品(shopId=0)
        boolean needStoreSkuPrice = tigshopProperties.getIsO2o() == 1
                && shopId != null && shopId != 0
                && (productShopId == null || productShopId == 0);

        if (needStoreSkuPrice && skuId != null && skuId > 0) {
            // O2O模式下需要门店SKU价格，保留原查询逻辑
            ProductECardGroupDTO productDetail = this.getProductDetail(productId, false, shopId);
            ProductSkuDTO productSkuDetail = productSkuService.getDetail(skuId, productDetail.getStoreProductId());
            if (productSkuDetail != null) {
                productPrice = new BigDecimal(productSkuDetail.getSkuPrice());
            }
        } else if (firstSku != null && firstSku.getSkuPrice() != null) {
            // 非O2O模式或无需门店价格，使用已批量查询的SKU价格
            productPrice = new BigDecimal(firstSku.getSkuPrice());
        }

        // 使用预查询的积分兑换商品ID集合，避免循环内查询
        boolean isPointsExchangeProduct = pointsExchangeProductIds != null && pointsExchangeProductIds.contains(productId);
        if (!isPointsExchangeProduct) {
            if (SecurityUtils.getCurrentUserId() > 0) {
                BigDecimal discount = memberBenefitService.resolveDiscount(SecurityUtils.getCurrentUserId(), rankId);
                if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
                    productPrice = productPrice.multiply(discount).divide(new BigDecimal(10), 2, RoundingMode.HALF_UP);
                }
            }
        }
        BigDecimal newPrice = null;

        // 使用预查询的促销信息，避免循环内N次调用getProductsPromotion
        // 使用复合key: productId + "_" + skuId
        skuId = skuId != null ? skuId : 0;
        String promotionKey = productId + "_" + skuId;
        ProductSaveParam promotionParam = batchPromotions.get(promotionKey);
        List<PromotionVO> promotionListVo = (promotionParam != null && promotionParam.getActivityInfo() != null)
                ? promotionParam.getActivityInfo()
                : new ArrayList<>();
        Iterator<PromotionVO> iterator = promotionListVo.iterator();
        while (iterator.hasNext()) {
            PromotionVO promotionVO = iterator.next();
            if (promotionVO.getType() == 1) {
                // 使用整页预取的秒杀明细，避免逐商品 selectOne（N+1）
                SeckillItem seckillItem = seckillItemMap.get(promotionVO.getRelationId() + "_" + skuId);
                if (canApplyPromotionPrice(promotionVO)) {
                    newPrice = seckillItem.getSeckillPrice();
                }
                promotionVO.getData().put("item", seckillItem);
                // 这里暂时先这样处理，后续应该把所有的驼峰字段改为下划线字段
                promotionVO.getData().put("seckillStartTime", TigUtils.handelTime(seckillItem.getSeckillStartTime()));
                promotionVO.getData().put("seckillEndTime", TigUtils.handelTime(seckillItem.getSeckillEndTime()));
            } else if (promotionVO.getType() == 6) {
                // 使用整页预取的限时折扣明细，避免逐商品 selectOne（N+1）
                TimeDiscountItem discountItem = timeDiscountItemMap.get(promotionVO.getData().get("discountId") + "_" + productId);
                BigDecimal discountPrice = timeDiscountService.getTimeDiscountPrice(productPrice, skuId, discountItem);
                if (canApplyPromotionPrice(promotionVO)) {
                    newPrice = discountPrice;
                }
                TimeDiscountDTO value = new TimeDiscountDTO(discountItem);
                value.setDiscountPrice(productPrice.subtract(discountPrice).setScale(2, RoundingMode.HALF_UP));
                promotionVO.getData().put("item", value);
            } else if (promotionVO.getType() == 2 && promotionVO.getIsDelete() == 1) {
                // 使用 Iterator 的 remove 方法安全删除
                iterator.remove();
            }
            if (promotionVO.getType() == PromotionType.TYPE_GROUPON.getCode()) {
                //在sku里面拿最低价
                if (minGrouponPrice) {
                    // 使用整页预取的拼团最低价，避免逐商品 getGrouponInfo（内部 3~4 条 SQL）
                    BigDecimal grouponMin = grouponMinPriceMap.get(productId);
                    if (grouponMin != null) {
                        dto.setIsGroupon(true);
                        newPrice = grouponMin;
                    }
                }
            }
        }

        productPrice = newPrice == null ? productPrice : newPrice;

        return productPrice != null && productPrice.compareTo(BigDecimal.ZERO) > 0
                ? productPrice.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    /**
     * 商品列表整页优化上下文：仅在列表批量装配路径使用，承载可跨条目复用的预加载数据。
     * 为 null 时表示走原详情路径，行为完全不变。
     */
    private static final class ProductListDetailContext {
        /** 按 shopId 记忆化的整页可用促销 */
        final Map<Integer, List<PromotionVO>> promotionCacheByShop = new HashMap<>();
        /** 整页一次性查好的积分兑换商品 ID 集合；null 表示未预加载、回退逐条查询 */
        Set<Integer> pointsExchangeProductIds;
        /** 已完成积分兑换预加载的商品 ID，供批量入口跨页增量补齐时跳过已查过的商品 */
        final Set<Integer> pointsExchangeLoadedProductIds = new HashSet<>();
        /** 整页级促销缓存：ProductPromotion 行按 relationId 记忆化（供 checkPromotionIsAvailable 复用） */
        final Map<Integer, ProductPromotion> promotionRowCache = new HashMap<>();
        /** 整页级：realPromotion 结果按 type:relationId 记忆化（缓存 JSON 串，用时重新解析成独立对象） */
        final Map<String, String> realPromotionJsonCache = new HashMap<>();
        /** 整页级：赠品按 giftId 记忆化 */
        final Map<Integer, ProductGift> giftCache = new HashMap<>();
        /** 整页级：赠品对应的商品详情按 productId 记忆化（同一赠品商品只查一次） */
        final Map<Integer, Product> giftProductInfoCache = new HashMap<>();
        /** 整页级：促销判定用 user 按 userId 记忆化（整页只查一次） */
        final Map<Integer, User> promotionUserCache = new HashMap<>();
        /** 整页级：门店覆盖开关等配置按 bizCode 记忆化（值在单次请求内不变，取不到时仍由 getConfigByCode 抛出） */
        final Map<String, ConfigPO> configCache = new HashMap<>();
        /** 整页级：会员等级列表（与商品无关，整页只查一次） */
        List<UserRank> userRankCache;
        /** 整页已批量加载的商品 SKU（按 productId 分组）；非 null 时 getProductsPromotion 复用它，避免逐商品 getProductSkusByPid */
        Map<Integer, List<ProductSkuDTO>> productSkuMap;

        ProductListDetailContext() {
        }

        ProductListDetailContext(Set<Integer> pointsExchangeProductIds) {
            this.pointsExchangeProductIds = pointsExchangeProductIds;
        }
    }

    /**
     * 按 bizCode 记忆化配置读取。上下文为 null 时退回逐次查询，行为与直接调用 getConfigByCode 完全一致；
     * 配置缺失时异常照常抛出且不进入缓存。
     */
    private ConfigPO cachedConfig(ProductListDetailContext listCtx, String bizCode) {
        if (listCtx == null) {
            return configService.getConfigByCode(bizCode);
        }
        return listCtx.configCache.computeIfAbsent(bizCode, configService::getConfigByCode);
    }

    /**
     * 会员等级列表整页只查一次。上下文为 null 时退回逐次查询。
     */
    private List<UserRank> cachedUserRank(ProductListDetailContext listCtx) {
        if (listCtx == null) {
            return userRankService.getUserRank();
        }
        if (listCtx.userRankCache == null) {
            listCtx.userRankCache = userRankService.getUserRank();
        }
        return listCtx.userRankCache;
    }

    /**
     * 促销/等级判定用 user 按 userId 记忆化。上下文为 null 时退回逐次查询；
     * 查不到用户时与原逻辑一致地返回 null（不进入缓存）。
     */
    private User cachedUser(ProductListDetailContext listCtx, Integer userId) {
        if (listCtx == null) {
            return userMapper.selectById(userId);
        }
        return listCtx.promotionUserCache.computeIfAbsent(userId, userMapper::selectById);
    }

    /**
     * 增量补齐积分兑换商品 ID 集合：只查本次尚未加载过的商品，供批量入口跨门店共享同一上下文。
     */
    private void ensurePointsExchangeLoaded(ProductListDetailContext listCtx, List<Integer> productIds) {
        if (listCtx.pointsExchangeProductIds == null) {
            listCtx.pointsExchangeProductIds = new HashSet<>();
        }
        List<Integer> pending = productIds.stream()
                .filter(productId -> !listCtx.pointsExchangeLoadedProductIds.contains(productId))
                .distinct()
                .toList();
        if (pending.isEmpty()) {
            return;
        }
        listCtx.pointsExchangeLoadedProductIds.addAll(pending);
        listCtx.pointsExchangeProductIds.addAll(pointsExchangeMapper.selectList(
                        Wrappers.lambdaQuery(PointsExchange.class)
                                .select(PointsExchange::getProductId)
                                .in(PointsExchange::getProductId, pending))
                .stream().map(PointsExchange::getProductId).toList());
    }

    @Override
    public ProductAvailabilityVO getProductSkuDetail(Integer productId, Integer skuId, Integer excludeActivity, String extraAttrIds, Integer shopId, Integer userId) {
        return getProductSkuDetailWithCycle(productId, skuId, excludeActivity, extraAttrIds, shopId, userId, null, null);
    }

    /**
     * 列表专用：携带整页优化上下文（促销缓存 + 积分兑换商品集合），并跳过列表不需要的逐条查询
     */
    public ProductAvailabilityVO getProductSkuDetail(Integer productId, Integer skuId, Integer excludeActivity, String extraAttrIds, Integer shopId, Integer userId, ProductListDetailContext listCtx) {
        return getProductSkuDetailWithCycle(productId, skuId, excludeActivity, extraAttrIds, shopId, userId, null, listCtx);
    }

    private ProductAvailabilityVO getProductSkuDetailWithCycle(Integer productId, Integer skuId, Integer excludeActivity, String extraAttrIds, Integer shopId, Integer userId, Integer cycleSpecId) {
        return getProductSkuDetailWithCycle(productId, skuId, excludeActivity, extraAttrIds, shopId, userId, cycleSpecId, null);
    }

    private ProductAvailabilityVO getProductSkuDetailWithCycle(Integer productId, Integer skuId, Integer excludeActivity, String extraAttrIds, Integer shopId, Integer userId, Integer cycleSpecId, ProductListDetailContext listCtx) {
        if (userId == null) {
            userId = SecurityUtils.getCurrentUserId();
        }

        ProductAvailabilityVO productAvailabilityVO = new ProductAvailabilityVO();
        // 列表路径跳过 getProductDetail 内的 isSeckill/isBuy 运行时查询（列表输出不使用，秒杀在列表侧另有批量处理）
        ProductECardGroupDTO productDetail = this.getProductDetail(productId, false, shopId, listCtx != null, listCtx);
        Integer productStock = productDetail.getProductStock();
        BigDecimal startProductPrice = BigDecimal.ZERO;
        BigDecimal productPrice = startProductPrice = productDetail.getProductPrice();
        if (skuId == null || skuId == 0) {
            List<ProductSkuDTO> productSkuList = listCtx != null && listCtx.productSkuMap != null
                    ? listCtx.productSkuMap.getOrDefault(productId, List.of())
                    : productSkuService.getProductSkusByPid(productId);
            if (CollUtil.isNotEmpty(productSkuList) && productSkuList.size() == 1) {
                skuId = productSkuList.get(0).getSkuId();
            }
        }
        final Integer resolvedSkuId = skuId;
        ProductSkuDTO productSkuDetail = null;
        if (resolvedSkuId != null) {
            if (listCtx != null && listCtx.productSkuMap != null) {
                productSkuDetail = listCtx.productSkuMap.getOrDefault(productId, List.of()).stream()
                        .filter(sku -> Objects.equals(sku.getSkuId(), resolvedSkuId))
                        .findFirst()
                        .orElse(null);
            } else {
                productSkuDetail = productSkuService.getDetail(resolvedSkuId, productDetail.getStoreProductId());
            }
        }
        boolean skuEnabled = productSkuDetail == null
                || !Objects.equals(productSkuDetail.getIsEnabled(), Constants.NO);
        List<ProductSkuDTO.SkuData> skuData = new ArrayList<>();
        if (productSkuDetail != null) {
            productStock = skuEnabled ? productSkuDetail.getSkuStock() : 0;
            productPrice = startProductPrice = new BigDecimal(productSkuDetail.getSkuPrice());
            skuData = productSkuDetail.getSkuData();
//            String attrPicThumb = getProductAttribute(productSkuDetail.getProductId(), skuData.getFirst().getName(), skuData.getFirst().getValue());
        }
        long count;
        if (listCtx != null && listCtx.pointsExchangeProductIds != null) {
            // 列表路径：积分兑换商品集合整页一次性查好，避免逐条 selectCount
            count = listCtx.pointsExchangeProductIds.contains(productId) ? 1 : 0;
        } else {
            count = pointsExchangeMapper.selectCount(Wrappers.lambdaQuery(PointsExchange.class).eq(PointsExchange::getProductId, productId));
        }
        if (count < 1) {
            if (userId > 0) {
                List<UserRank> userRankList = cachedUserRank(listCtx);
                Integer rankId = cachedUser(listCtx, userId).getRankId();
                BigDecimal discount = memberBenefitService.resolveDiscount(userId, rankId);
                if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
                    productPrice = productPrice.multiply(discount).divide(new BigDecimal(10), 2, RoundingMode.HALF_UP);
                }
            }
        }

        BigDecimal attrPrice = BigDecimal.ZERO;
        BigDecimal newPrice = null;
        if (StrUtil.isNotBlank(extraAttrIds)) {
            List<ProductAttribute> productAttributeList = getProductAttributeList(StringUtils.str2IntList(extraAttrIds));
            attrPrice = productAttributeList.stream().map(ProductAttribute::getAttrPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        List<PromotionVO> promotionListVo = new ArrayList<>();
        if (excludeActivity == 0 && skuEnabled) {
            List<ProductSaveParam> products = new ArrayList<>();

            ProductSaveParam productSaveParam = new ProductSaveParam();
            productSaveParam.setProductId(productId);
            skuId = skuId != null ? skuId : 0;
            productSaveParam.setSkuId(skuId);
            products.add(productSaveParam);

            Integer promoShopId = tigshopProperties.getIsO2o() == 1 ? shopId : productDetail.getShopId();
            Map<Integer, ProductSaveParam> promotions;
            if (listCtx != null) {
                final Integer promoUserId = userId;
                List<PromotionVO> allAvailablePromotion =
                        listCtx.promotionCacheByShop.computeIfAbsent(promoShopId, sid -> promotionService.getAllAvailablePromotion(sid, promoUserId));
                promotions = getProductsPromotion(products, allAvailablePromotion, promoShopId, "detail", userId, listCtx);
            } else {
                promotions = getProductsPromotion(products, promoShopId, "detail", userId);
            }

            promotionListVo = promotions.get(skuId).getActivityInfo();
            Iterator<PromotionVO> iterator = promotionListVo.iterator();
            while (iterator.hasNext()) {
                PromotionVO promotionVO = iterator.next();
                if (promotionVO.getType() == 1) {
                    SeckillItem seckillItem = seckillItemMapper.selectOne(new LambdaQueryWrapper<SeckillItem>()
                            .eq(SeckillItem::getSeckillId, promotionVO.getRelationId())
                            .eq(SeckillItem::getSkuId, skuId)
                            .last("limit 1"));
                    if (seckillItem != null) {
                        if (canApplyPromotionPrice(promotionVO)) {
                            newPrice = seckillItem.getSeckillPrice();
                            productStock = seckillItem.getSeckillStock();
                        }
                        promotionVO.getData().put("item", seckillItem);
                        // 这里暂时先这样处理，后续应该把所有的驼峰字段改为下划线字段
                        promotionVO.getData().put("seckillStartTime", TigUtils.handelTime(seckillItem.getSeckillStartTime()));
                        promotionVO.getData().put("seckillEndTime", TigUtils.handelTime(seckillItem.getSeckillEndTime()));
                    }
                } else if (promotionVO.getType() == 6) {
                    TimeDiscountItem discountItem = timeoutDiscountItemMapper.selectOne(new LambdaQueryWrapper<TimeDiscountItem>()
                            .eq(TimeDiscountItem::getDiscountId, promotionVO.getData().get("discountId"))
                            .eq(TimeDiscountItem::getProductId, productId)
                            .last("limit 1"));
                    BigDecimal discountPrice = timeDiscountService.getTimeDiscountPrice(productPrice, skuId, discountItem);
                    if (canApplyPromotionPrice(promotionVO)) {
                        newPrice = discountPrice;
                    }
                    TimeDiscountDTO value = new TimeDiscountDTO(discountItem);
                    value.setDiscountPrice(productPrice.subtract(discountPrice).setScale(2, RoundingMode.HALF_UP));
                    promotionVO.getData().put("item", value);
                } else if (promotionVO.getType() == PromotionType.TYPE_GROUPON.getCode()) {
                    GrouponItemInfoVO grouponInfo = grouponPromotionService.getGrouponInfo(productId);
                    if (promotionVO.getData() != null) {
                        promotionVO.getData().put("item", grouponInfo);
                    }
                } else if (promotionVO.getType() == 2 && promotionVO.getIsDelete() == 1) {
                    // 使用 Iterator 的 remove 方法安全删除
                    iterator.remove();
                }
            }

        }
        //如果该商品是礼品卡商品，则徐判断该商品在礼品卡组里面的库存
        Integer detailProductType = productDetail.getProductType();
        if (detailProductType != null && (detailProductType == ProductType.PRODUCT_TYPE_GIFT_CARD.getCode() || detailProductType == ProductType.PRODUCT_TYPE_GIFT_CARD_VIRTUAL.getCode())) {
            Product product = getById(productId);
            GiftCardGroups giftCardGroups = giftCardGroupsMapper.selectById(product.getGiftCardGroupsId());
            if (giftCardGroups != null && giftCardGroups.getStatus() == 1) {
                productStock = giftCardGroups.getAvailabilityStock();
                if (product.getProductStock() != null && product.getProductStock() > 0) {
                    productStock = Math.min(productStock, product.getProductStock());
                } else {
                    productStock = 0;
                }
            } else {
                productStock = 0;
            }
        }
        productPrice = productPrice.add(attrPrice);
        BigDecimal originPrice = newPrice == null ? productPrice : null;
        productPrice = newPrice == null ? productPrice : newPrice;

        productAvailabilityVO.setId(productId);
        productAvailabilityVO.setPromotion(promotionListVo);
        productAvailabilityVO.setSku(productSkuDetail);
        productAvailabilityVO.setData(skuData);
        // 列表路径不使用 attrPicThumb，跳过逐规格项查询
        if (skuData != null && listCtx == null) {
            skuData.forEach(data -> data.setAttrPicThumb(getProductAttribute(productId, data.getName(), data.getValue())));
        }
        productAvailabilityVO.setPrice(productPrice.setScale(2, RoundingMode.HALF_UP));
        productAvailabilityVO.setStock(productStock);
        productAvailabilityVO.setProductId(productId);
        productAvailabilityVO.setOriginPrice(originPrice);
        productAvailabilityVO.setMarketPrice(productDetail.getMarketPrice());
        productAvailabilityVO.setStartProductPrice(startProductPrice);
        if (skuEnabled && tigshopProperties.getIsO2o() == 1 && ProductType.isReservationProduct(productDetail.getProductType())) {
            // 预约类型库存不受 product 影响，设置最大库存值以跳过前端判断
            productAvailabilityVO.setStock(PRODUCT_STOCK_MAX_LIMIT);
            try {
                List<Integer> calendarShopIds = resolveReservationCalendarShopIdsForDetail(
                        productId,
                        productDetail.getShopId(),
                        shopId,
                        null
                );
                Integer calendarShopId = selectReservationCalendarShopId(productId, calendarShopIds);
                StockCalendarQueryDTO queryDTO = new StockCalendarQueryDTO();
                queryDTO.setProductId(productId);
                queryDTO.setShopId(calendarShopId != null ? calendarShopId : 0);
                queryDTO.setSkuId(skuId);
                StockCalendarVO calendar = null;
                if (calendar == null || calendar.getDateStockList() == null || calendar.getDateStockList().isEmpty()) {
                    productAvailabilityVO.setStock(calculateReservationStockForListDisplay(
                            productId,
                            calendarShopId != null ? List.of(calendarShopId) : List.of(),
                            skuId
                    ));
                }
            } catch (GlobalException e) {
                Integer fallbackShopId = selectReservationCalendarShopId(productId, resolveReservationCalendarShopIdsForDetail(
                        productId,
                        productDetail.getShopId(),
                        shopId,
                        null
                ));
                productAvailabilityVO.setStock(calculateReservationStockForListDisplay(
                        productId,
                        fallbackShopId != null ? List.of(fallbackShopId) : List.of(),
                        skuId
                ));
            }
        }

        //预售商品
        Integer saleMode = Optional.ofNullable(productDetail.getSaleMode()).orElse(0);
        if (saleMode == 2) {
            ProductPresale productPresale = productPresaleService.getByProductId(productId);
            if (Objects.nonNull(productPresale)) {
                BigDecimal depositAmount = productPresaleService.calculateDepositAmount(productId, skuId, productPresale);
                productAvailabilityVO.setDepositAmount(depositAmount);
                productAvailabilityVO.setFinalPaymentAmount(productAvailabilityVO.getPrice().subtract(
                        depositAmount
                ).max(BigDecimal.ZERO));
            }
        }

        return productAvailabilityVO;
    }

    private Integer resolveSkuType(ProductAttrsDTO attrList, List<ProductSkuDTO> productList) {
        boolean hasSpecAttr = attrList != null && CollUtil.isNotEmpty(attrList.getSpe());
        boolean hasMultiSku = CollUtil.isNotEmpty(productList) && productList.size() > 1;
        return (hasSpecAttr || hasMultiSku) ? 2 : 1;
    }

    /**
     * 判断单规格商品的 store_product 顶层 fee 是否有实际变化。
     * 只要 4 个字段均一致，视为"表单回传"而非"修改"。
     */
    private boolean hasStoreProductFeeChanged(StoreProduct existing, StoreProduct patch) {
        if (existing == null || patch == null) {
            return true;
        }
        return !Objects.equals(existing.getDeliveryServiceFeeType(), patch.getDeliveryServiceFeeType())
                || !feeValueEquals(existing.getDeliveryServiceFeeValue(), patch.getDeliveryServiceFeeValue())
                || !Objects.equals(existing.getServerServiceFeeType(), patch.getServerServiceFeeType())
                || !feeValueEquals(existing.getServerServiceFeeValue(), patch.getServerServiceFeeValue());
    }

    /**
     * 从提交的 SKU 列表中筛选出 fee 实际有变化的子集（相对于现有 store_product_sku 或平台默认值）。
     * 仅返回需要走 StoreSkuFeeWritePolicy 校验的 SKU。
     */
    private List<ProductSkuDTO> filterFeeChangedSkus(
            List<? extends ProductSkuDTO> paramSkus,
            Map<Long, StoreProductSku> existingByProductSkuId,
            List<ProductSku> platformSkus) {
        if (paramSkus == null || paramSkus.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, ProductSku> platformBySkuValue = platformSkus.stream()
                .collect(Collectors.toMap(ProductSku::getSkuValue, s -> s, (a, b) -> a));

        List<ProductSkuDTO> changed = new ArrayList<>();
        for (ProductSkuDTO paramSku : paramSkus) {
            ProductSku platformSku = platformBySkuValue.get(paramSku.getSkuValue());
            if (platformSku == null) {
                continue;
            }
            Long productSkuId = Long.valueOf(platformSku.getSkuId());
            StoreProductSku existing = existingByProductSkuId.get(productSkuId);

            if (hasSkuFeeChanged(paramSku, existing, platformSku)) {
                changed.add(paramSku);
            }
        }
        return changed;
    }

    private boolean hasSkuFeeChanged(ProductSkuDTO param, StoreProductSku existing, ProductSku platform) {
        Integer refDeliveryType;
        BigDecimal refDeliveryValue;
        Integer refServerType;
        BigDecimal refServerValue;

        if (existing != null) {
            refDeliveryType = existing.getDeliveryServiceFeeType();
            refDeliveryValue = existing.getDeliveryServiceFeeValue();
            refServerType = existing.getServerServiceFeeType();
            refServerValue = existing.getServerServiceFeeValue();
        } else {
            refDeliveryType = platform.getDeliveryServiceFeeType();
            refDeliveryValue = platform.getDeliveryServiceFeeValue();
            refServerType = platform.getServerServiceFeeType();
            refServerValue = platform.getServerServiceFeeValue();
        }

        if (param.getDeliveryServiceFeeType() != null
                && !Objects.equals(param.getDeliveryServiceFeeType(), refDeliveryType)) {
            return true;
        }
        if (param.getDeliveryServiceFeeValue() != null
                && !feeValueEquals(param.getDeliveryServiceFeeValue(), refDeliveryValue)) {
            return true;
        }
        if (param.getServerServiceFeeType() != null
                && !Objects.equals(param.getServerServiceFeeType(), refServerType)) {
            return true;
        }
        if (param.getServerServiceFeeValue() != null
                && !feeValueEquals(param.getServerServiceFeeValue(), refServerValue)) {
            return true;
        }
        return false;
    }

    private static boolean feeValueEquals(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.compareTo(b) == 0;
    }

    /**
     * 是否单规格商品（5.8.32 新增）。
     *
     * 判定语义：显式 skuType=1 或者 skuType 未设置且 productList 为空。
     * 多规格时返回 false，product 顶层 fee 字段会被显式清空（避免污染）。
     */
    private boolean isSingleSpecProduct(ProductSaveParam param) {
        if (param == null) {
            return false;
        }
        if (param.getSkuType() != null) {
            return param.getSkuType() == 1;
        }
        return CollUtil.isEmpty(param.getProductList());
    }

    /**
     * 处理 product 顶层服务费字段（5.8.32 新增）。
     *
     * 单规格商品：BeanUtils.copyProperties 已自动复制 4 字段，本方法调用平台层校验。
     * 多规格商品：显式置 0，避免历史数据污染（resolver 多规格走 product_sku 路径，product 顶层不读）。
     *
     * 注意：必须在 this.save(product) / this.updateById(product) 之前调用。
     */
    private void applyProductLevelServiceFee(ProductSaveParam param, Product product) {
        product.setDeliveryServiceFeeType(0);
        product.setDeliveryServiceFeeValue(BigDecimal.ZERO);
        product.setServerServiceFeeType(0);
        product.setServerServiceFeeValue(BigDecimal.ZERO);
    }

    private List<ProductListAttributeDTO> normalizeReservationProductList(ProductSaveParam param, Product product) {
        List<ProductListAttributeDTO> productList = param.getProductList();
        if (!ProductType.isReservationProduct(product.getProductType())) {
            return productList;
        }
        // skuType == 1 表示统一规格，不需要创建SKU明细，和普通商品统一规格一致
        if (param.getSkuType() != null && param.getSkuType() == 1) {
            return null;
        }
        if (CollUtil.isNotEmpty(productList)) {
            return productList;
        }
        List<ProductSkuDTO> existingSkuList = productSkuService.getProductSkusByPid(product.getProductId());
        if (CollUtil.isNotEmpty(existingSkuList)) {
            return existingSkuList.stream().map(item -> {
                ProductListAttributeDTO dto = new ProductListAttributeDTO();
                BeanUtils.copyProperties(item, dto);
                if (CollUtil.isNotEmpty(item.getSkuData())) {
                    dto.setAttrs(item.getSkuData().stream().map(skuData -> {
                        ProductListAttributeDTO.Attribute attribute = new ProductListAttributeDTO.Attribute();
                        attribute.setAttrName(skuData.getName());
                        attribute.setAttrValue(skuData.getValue());
                        return attribute;
                    }).toList());
                }
                return dto;
            }).toList();
        }
        ProductListAttributeDTO defaultSku = new ProductListAttributeDTO();
        defaultSku.setSkuId(param.getSkuId());
        defaultSku.setProductId(product.getProductId());
        defaultSku.setSkuSn(StrUtil.isNotBlank(param.getProductSn()) ? param.getProductSn() : product.getProductSn());
        defaultSku.setSkuTsn(StrUtil.isNotBlank(param.getProductTsn()) ? param.getProductTsn() : product.getProductTsn());
        defaultSku.setSkuStock(Optional.ofNullable(param.getProductStock()).orElse(product.getProductStock()));
        BigDecimal skuPrice = Optional.ofNullable(param.getProductPrice()).orElse(product.getProductPrice());
        defaultSku.setSkuPrice(skuPrice == null ? "0" : skuPrice.toString());
        defaultSku.setMarketPrice(Optional.ofNullable(param.getMarketPrice()).orElse(product.getMarketPrice()));
        defaultSku.setCostPrice(Optional.ofNullable(param.getCostPrice()).orElse(product.getCostPrice()));
        defaultSku.setSkuValue("");
        defaultSku.setSkuData(new ArrayList<>());
        defaultSku.setAttrs(new ArrayList<>());
        return List.of(defaultSku);
    }

    private int calculateReservationTotalStock(Integer productId, Integer shopId) {
        if (shopId == null) {
            return 0;
        }
        List<ProductSkuDTO> skuList = productSkuService.getProductSkusByPid(productId);
        if (CollUtil.isEmpty(skuList)) {
            return calculateReservationSkuStock(productId, shopId, null);
        }
        return skuList.stream()
                .map(ProductSkuDTO::getSkuId)
                .filter(Objects::nonNull)
                .mapToInt(skuId -> calculateReservationSkuStock(productId, shopId, skuId))
                .sum();
    }

    private int calculateReservationSkuStock(Integer productId, Integer shopId, Integer skuId) {
        StockCalendarQueryDTO queryDTO = new StockCalendarQueryDTO();
        queryDTO.setProductId(productId);
        queryDTO.setShopId(shopId);
        queryDTO.setSkuId(skuId);
        StockCalendarVO calendar = null;
        try {
        } catch (GlobalException e) {
            if ("没有可预约的日期".equals(e.getMessage())) {
                return 0;
            }
            throw e;
        }
        if (calendar == null || CollUtil.isEmpty(calendar.getDateStockList())) {
            return 0;
        }
        return calendar.getDateStockList().stream()
                .filter(date -> date.getTimeSlotStockList() != null)
                .flatMap(date -> date.getTimeSlotStockList().stream())
                .filter(slot -> slot.getAvailableStock() != null)
                .mapToInt(StockCalendarVO.TimeSlotStockInfo::getAvailableStock)
                .sum();
    }

    private List<Integer> resolveReservationCalendarShopIdsForDetail(Integer productId, Integer productShopId, Integer requestShopId, Integer nearestShopId) {
        LinkedHashSet<Integer> calendarShopIds = new LinkedHashSet<>();
        if (productShopId != null) {
            calendarShopIds.add(productShopId);
        }
        if (requestShopId != null && requestShopId > 0) {
            calendarShopIds.add(requestShopId);
        }
        if (nearestShopId != null && nearestShopId > 0) {
            calendarShopIds.add(nearestShopId);
        }


        return new ArrayList<>(calendarShopIds);
    }

    private Integer selectReservationCalendarShopId(Integer productId, List<Integer> calendarShopIds) {
        if (CollUtil.isEmpty(calendarShopIds)) {
            log.warn("预约库存选店无候选门店, productId={}", productId);
            return null;
        }
        Integer fallbackShopId = null;
        int fallbackStock = 0;
        for (Integer calendarShopId : calendarShopIds) {
            if (calendarShopId == null) {
                continue;
            }
            try {
                if (calculateReservationTotalStock(productId, calendarShopId) > 0) {
                    return calendarShopId;
                }
            } catch (GlobalException e) {
                log.warn("预约库存门店校验失败, productId={}, shopId={}", productId, calendarShopId, e);
            }
            int stockByReservationTable = calculateReservationStockForListDisplay(productId, List.of(calendarShopId), null);
            if (stockByReservationTable > fallbackStock) {
                fallbackStock = stockByReservationTable;
                fallbackShopId = calendarShopId;
            }
        }
        if (fallbackShopId != null) {
            log.warn("预约库存选店使用库存表兜底门店, productId={}, shopId={}, stock={}",
                    productId, fallbackShopId, fallbackStock);
            return fallbackShopId;
        }
        log.warn("预约库存选店回退首个候选门店, productId={}, shopId={}", productId, calendarShopIds.getFirst());
        return calendarShopIds.getFirst();
    }

    private int calculateReservationTotalStockForDetailDisplay(Integer productId, Integer shopId) {
        if (shopId == null) {
            int stockByReservationTable = calculateReservationStockForListDisplay(productId, List.of(), null);
            log.warn("预约总库存门店无效, 使用跨门店库存表兜底, productId={}, invalidShopId={}, fallbackStock={}",
                    productId, shopId, stockByReservationTable);
            return stockByReservationTable;
        }
        int stockByCalendar = 0;
        try {
            stockByCalendar = calculateReservationTotalStock(productId, shopId);
        } catch (GlobalException e) {
            log.warn("预约总库存日历查询失败, productId={}, shopId={}", productId, shopId, e);
        }
        if (stockByCalendar > 0) {
            log.warn("预约总库存来源=日历, productId={}, shopId={}, stock={}", productId, shopId, stockByCalendar);
            return stockByCalendar;
        }
        int stockByReservationTable = calculateReservationStockForListDisplay(productId, List.of(shopId), null);
        log.warn("预约总库存来源=库存表兜底, productId={}, shopId={}, calendarStock={}, fallbackStock={}",
                productId, shopId, stockByCalendar, stockByReservationTable);
        return stockByReservationTable;
    }

    private int calculateReservationSkuStockForDetailDisplay(Integer productId, Integer shopId, Integer skuId) {
        if (shopId == null) {
            int stockByReservationTable = calculateReservationStockForListDisplay(productId, List.of(), skuId);
            log.warn("预约SKU库存门店无效, 使用跨门店库存表兜底, productId={}, skuId={}, invalidShopId={}, fallbackStock={}",
                    productId, skuId, shopId, stockByReservationTable);
            return stockByReservationTable;
        }
        int stockByCalendar = 0;
        try {
            stockByCalendar = calculateReservationSkuStock(productId, shopId, skuId);
        } catch (GlobalException e) {
            log.warn("预约SKU库存日历查询失败, productId={}, shopId={}, skuId={}", productId, shopId, skuId, e);
        }
        if (stockByCalendar > 0) {
            log.warn("预约SKU库存来源=日历, productId={}, shopId={}, skuId={}, stock={}",
                    productId, shopId, skuId, stockByCalendar);
            return stockByCalendar;
        }
        int stockByReservationTable = calculateReservationStockForListDisplay(productId, List.of(shopId), skuId);
        log.warn("预约SKU库存来源=库存表兜底, productId={}, shopId={}, skuId={}, calendarStock={}, fallbackStock={}",
                productId, shopId, skuId, stockByCalendar, stockByReservationTable);
        if (stockByReservationTable <= 0 && skuId != null && skuId > 0) {
            int sharedStock = calculateReservationSharedStockForDetailDisplay(productId, shopId);
            if (sharedStock > 0) {
                log.warn("预约SKU库存来源=统一规格兼容库存, productId={}, shopId={}, skuId={}, sharedStock={}",
                        productId, shopId, skuId, sharedStock);
                return sharedStock;
            }
        }
        return stockByReservationTable;
    }

    private int calculateReservationSharedStockForDetailDisplay(Integer productId, Integer shopId) {
        if (shopId == null) {
            return 0;
        }
        int sharedStock = calculateReservationStockForListDisplay(productId, List.of(shopId), 0);
        if (sharedStock > 0) {
            log.warn("预约库存命中统一规格共享库存, productId={}, shopId={}, sharedStock={}",
                    productId, shopId, sharedStock);
        }
        return sharedStock;
    }


    @Override
    public ProductAvailabilityVO getProductSkuDetail(Integer productId, Integer skuId, Integer excludeActivity, String extraAttrIds, Integer shopId, Integer addressId, Integer userId) {
        ProductAvailabilityVO productAvailabilityVO = getProductSkuDetailWithCycle(productId, skuId, excludeActivity, extraAttrIds, shopId, userId, null);
        if (Objects.nonNull(addressId)) {
            UserAddress userAddress = userAddressMapper.selectById(addressId);
            if (userAddress != null) {
                if (shopId > 0) {
                    Shop shop = shopMapper.selectById(shopId);
                    productAvailabilityVO.setDistance(GpsDistanceUtil.getDistance(userAddress.getLongitude(), userAddress.getLatitude(), shop.getShopLongitude(), shop.getShopLatitude()));
                } else {
                    GetStoreSelfOperatedVO config = configService.getStoreSelfOperatedSettings();
                    productAvailabilityVO.setDistance(GpsDistanceUtil.getDistance(userAddress.getLongitude(), userAddress.getLatitude(), new BigDecimal(config.getStoreSelfOperatedLongitude()), new BigDecimal(config.getStoreSelfOperatedLatitude())));
                }
            }
            return productAvailabilityVO;
        }
        return productAvailabilityVO;
    }



    private Integer resolveEffectiveReservationShopId(Integer productId, Integer requestShopId, Integer productShopId) {
        List<Integer> candidates = resolveReservationCalendarShopIdsForDetail(productId, productShopId, requestShopId, null);
        Integer selected = selectReservationCalendarShopId(productId, candidates);
        return selected != null ? selected : (requestShopId != null ? requestShopId : productShopId);
    }

    @Override
    public ProductAvailabilityVO getProductFinalPrice(Integer productId, Integer skuId, Integer excludeActivity, String extraAttrIds, Integer shopId, Integer userId) {
        return getProductFinalPrice(productId, skuId, excludeActivity, extraAttrIds, shopId, userId, false);
    }

    @Override
    public ProductAvailabilityVO getProductFinalPrice(Integer productId, Integer skuId, Integer excludeActivity, String extraAttrIds, Integer shopId, Integer userId, Boolean excludeTimeDiscountPrice) {
        userId = Objects.isNull(userId) ? SecurityUtils.getCurrentUserId() : userId;
        ProductAvailabilityVO productAvailabilityVO = new ProductAvailabilityVO();
        ProductECardGroupDTO productDetail = this.getProductDetail(productId, false, shopId);
        BigDecimal startProductPrice = BigDecimal.ZERO;
        Integer productStock = productDetail.getProductStock();
        BigDecimal productPrice = startProductPrice = productDetail.getProductPrice();
        ProductSkuDTO productSkuDetail = skuId != null ? productSkuService.getDetail(skuId, productDetail.getStoreProductId()) : null;
        boolean skuEnabled = productSkuDetail == null
                || !Objects.equals(productSkuDetail.getIsEnabled(), Constants.NO);
        List<ProductSkuDTO.SkuData> skuData = new ArrayList<>();
        if (productSkuDetail != null) {
            productStock = skuEnabled ? productSkuDetail.getSkuStock() : 0;
            startProductPrice = productPrice = new BigDecimal(productSkuDetail.getSkuPrice());
            skuData = productSkuDetail.getSkuData();
//            String attrPicThumb = getProductAttribute(productSkuDetail.getProductId(), skuData.getFirst().getName(), skuData.getFirst().getValue());
        }
        Long count = pointsExchangeMapper.selectCount(Wrappers.lambdaQuery(PointsExchange.class).eq(PointsExchange::getProductId, productId));
        if (count < 1) {
            if (userId > 0) {
                List<UserRank> userRankList = userRankService.getUserRank();
                Integer rankId = userMapper.selectById(userId).getRankId();
                BigDecimal discount = memberBenefitService.resolveDiscount(userId, rankId);
                if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
                    productPrice = productPrice.multiply(discount).divide(new BigDecimal(10), 2, RoundingMode.HALF_UP);
                }
            }
        }

        BigDecimal attrPrice = BigDecimal.ZERO;
        BigDecimal newPrice = null;
        if (StrUtil.isNotBlank(extraAttrIds)) {
            List<ProductAttribute> productAttributeList = getProductAttributeList(StringUtils.str2IntList(extraAttrIds));
            attrPrice = productAttributeList.stream().map(ProductAttribute::getAttrPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        List<PromotionVO> promotionListVo = new ArrayList<>();
        // CR-20260714-1335：批发商买「有批发价」的商品，选 SKU 价预览同样隐藏活动（不拉活动、不套活动价）
        // 门店上下文取 getProductDetail 解析后的归属（传入 shopId 时已覆写为该门店）
        boolean wholesaleHideActivity = isWholesalePricedForUser(productId, userId, productDetail.getShopId());
        if (excludeActivity == 0 && skuEnabled
                && !wholesaleHideActivity
        ) {
            List<ProductSaveParam> products = new ArrayList<>();

            ProductSaveParam productSaveParam = new ProductSaveParam();
            productSaveParam.setProductId(productId);
            skuId = skuId != null ? skuId : 0;
            productSaveParam.setSkuId(skuId);
            products.add(productSaveParam);

            Map<Integer, ProductSaveParam> promotions = getProductsPromotion(products, tigshopProperties.getIsO2o() == 1 ? shopId : productDetail.getShopId(), "detail");

            promotionListVo = promotions.get(skuId).getActivityInfo();
            Iterator<PromotionVO> iterator = promotionListVo.iterator();
            while (iterator.hasNext()) {
                PromotionVO promotionVO = iterator.next();
                if (promotionVO.getType() == 1) {
                    SeckillItem seckillItem = seckillItemMapper.selectOne(new LambdaQueryWrapper<SeckillItem>()
                            .eq(SeckillItem::getSeckillId, promotionVO.getRelationId())
                            .eq(SeckillItem::getSkuId, skuId)
                            .last("limit 1"));
                    if (seckillItem != null) {
                        if (canApplyPromotionPrice(promotionVO)) {
                            newPrice = seckillItem.getSeckillPrice();
                            productStock = seckillItem.getSeckillStock();
                        }
                        promotionVO.getData().put("item", seckillItem);
                        // 这里暂时先这样处理，后续应该把所有的驼峰字段改为下划线字段
                        promotionVO.getData().put("seckillStartTime", TigUtils.handelTime(seckillItem.getSeckillStartTime()));
                        promotionVO.getData().put("seckillEndTime", TigUtils.handelTime(seckillItem.getSeckillEndTime()));
                    }
                } else if (promotionVO.getType() == TYPE_LIMIT_DISCOUNT.getCode()) {
                    // CR-20260730-1745：与 getProductDetail 同口径下发折扣价与活动明细，
                    // 否则选规格后前端拿不到 data.item.discountPrice、price 也不是折扣价
                    TimeDiscountItem discountItem = timeoutDiscountItemMapper.selectOne(new LambdaQueryWrapper<TimeDiscountItem>()
                            .eq(TimeDiscountItem::getDiscountId, promotionVO.getData().get("discountId"))
                            .eq(TimeDiscountItem::getProductId, productId)
                            .last("limit 1"));
                    if (discountItem != null) {
                        BigDecimal discountPrice = timeDiscountService.getTimeDiscountPrice(productPrice, skuId, discountItem);
                        if (!Boolean.TRUE.equals(excludeTimeDiscountPrice) && canApplyPromotionPrice(promotionVO)) {
                            newPrice = discountPrice;
                        }
                        if (!Boolean.TRUE.equals(excludeTimeDiscountPrice)) {
                            TimeDiscountDTO value = new TimeDiscountDTO(discountItem);
                            value.setDiscountPrice(productPrice.subtract(discountPrice).setScale(2, RoundingMode.HALF_UP));
                            promotionVO.getData().put("item", value);
                        }
                    }
                } else if (promotionVO.getType() == 2 && promotionVO.getIsDelete() == 1) {
                    // 使用 Iterator 的 remove 方法安全删除
                    iterator.remove();
                } else if (promotionVO.getType() == PromotionType.TYPE_GROUPON.getCode()) {
                    GrouponItemInfoVO grouponInfo = grouponPromotionService.getGrouponInfo(productId);
                    /*GrouponPromotionDetail gpd = grouponInfo.getGrouponPromotionDetails().stream()
                            .filter(d -> d.getProductId().equals(productId.longValue()))
                            .findFirst()
                            .orElse(null);
                    newPrice = grouponInfo.getMinGrouponPrice();
                    if (gpd != null) {
                        productStock = gpd.getLimitNum();
                    }*/
                    if (promotionVO.getData() != null) {
                        promotionVO.getData().put("item", grouponInfo);
                    }

                }
            }
        }
        productPrice = productPrice.add(attrPrice);
        BigDecimal originPrice = newPrice == null ? productPrice : null;
        productPrice = newPrice == null ? productPrice : newPrice;

        productAvailabilityVO.setId(productId);
        productAvailabilityVO.setPromotion(promotionListVo);
        productAvailabilityVO.setSku(productSkuDetail);
        productAvailabilityVO.setData(skuData);
        if (skuData != null) {
            skuData.forEach(data -> data.setAttrPicThumb(getProductAttribute(productId, data.getName(), data.getValue())));
        }
        productAvailabilityVO.setPrice(productPrice.setScale(2, RoundingMode.HALF_UP));
        productAvailabilityVO.setStock(productStock);
        productAvailabilityVO.setProductId(productId);
        productAvailabilityVO.setOriginPrice(originPrice);
        productAvailabilityVO.setStartProductPrice(startProductPrice);
        return productAvailabilityVO;
    }

    /**
     * 商品详情/列表仍需返回受限活动供前端展示，但受限活动不能覆盖可购买商品价。
     */
    static boolean canApplyPromotionPrice(PromotionVO promotion) {
        return !Boolean.FALSE.equals(promotion.getCanJoin());
    }


    @Override
    public ProductAvailabilityVO getProductFinalPrice(Integer productId, Integer skuId, Integer excludeActivity, String extraAttrIds, Integer shopId) {
        return getProductFinalPrice(productId, skuId, excludeActivity, extraAttrIds, shopId, null);
    }

    /**
     * 获取商品属性的图片
     *
     * @param productId 商品ID
     * @param attrName  属性名
     * @param attrValue 属性值
     * @return String
     */
    public String getProductAttribute(int productId, String attrName, String attrValue) {
        LambdaQueryWrapper<ProductAttribute> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductAttribute::getProductId, productId)
                .eq(ProductAttribute::getAttrName, attrName)
                .eq(ProductAttribute::getAttrValue, attrValue);
        List<ProductAttribute> list = productAttributesService.list(queryWrapper);
        if (list != null && !list.isEmpty()) {
            String pic = list.getFirst().getAttrPicThumb();
            if (StrUtil.isNotBlank(pic)) {
                return pic;
            }
        }
        Product product = getById(productId);
        return product != null ? product.getPicThumb() : "";
    }

    /**
     * 获取商品附加规格
     *
     * @param productIds 商品ID集合
     * @return List<ProductAttribute>
     */
    public List<ProductAttribute> getProductAttributeList(List<Integer> productIds) {
        LambdaQueryWrapper<ProductAttribute> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProductAttribute::getAttributesId, productIds)
                .select(ProductAttribute::getAttributesId, ProductAttribute::getAttrName, ProductAttribute::getAttrValue, ProductAttribute::getAttrPrice);
        return productAttributesService.list(queryWrapper);
    }

    /**
     * 获得商品的优惠信息,列表使用简化优惠版本
     *
     * @param products      商品信息
     * @param shopId        店铺ID
     * @param promotionFrom 获取促销信息的来源
     * @return List<ProductDTO>
     */
    @Override
    public Map<Integer, ProductSaveParam> getProductsPromotion(List<ProductSaveParam> products, Integer shopId, String promotionFrom) {
        return getProductsPromotion(products, shopId, promotionFrom, null);
    }


    /**
     * 获得商品的优惠信息,列表使用简化优惠版本
     *
     * @param products      商品信息
     * @param shopId        店铺ID
     * @param promotionFrom 获取促销信息的来源
     * @return List<ProductDTO>
     */
    @Override
    public Map<Integer, ProductSaveParam> getProductsPromotion(List<ProductSaveParam> products, Integer shopId, String promotionFrom, Integer userId) {
        promotionFrom = (promotionFrom == null) ? "list" : promotionFrom;
        userId = Optional.ofNullable(userId).orElse(SecurityUtils.getCurrentUserId());
        if (shopId == null) {
            return getProductsPromotionByProductShop(products, promotionFrom, userId);
        }
        // 获取所有可用的促销活动
        List<PromotionVO> allAvailablePromotion = promotionService.getAllAvailablePromotion(shopId, userId);
        return getProductsPromotion(products, allAvailablePromotion, shopId, promotionFrom, userId);
    }

    /**
     * 调用方未指定店铺时（如购物车页只传 productId），活动最终仍按「活动店铺 == 商品店铺」匹配，
     * 因此先用商品 id 批量取回各自的 shopId，只加载这些店铺的活动。
     * 原实现在 shopId 为空时加载全平台活动，再对每个活动逐条查库，请求中只要有一个商品匹配不到活动
     * 就要遍历完全部活动，线上单次耗时 6~7 秒。
     */
    private Map<Integer, ProductSaveParam> getProductsPromotionByProductShop(List<ProductSaveParam> products, String promotionFrom, Integer userId) {
        if (products == null || products.isEmpty()) {
            return new HashMap<>();
        }
        List<Integer> productIds = products.stream()
                .map(ProductSaveParam::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Integer, Integer> shopIdByProductId = resolveShopIdByProductIds(productIds);

        // 按商品所属店铺分组；查不到商品行的分组 key 为 null，沿用原行为：返回条目但不挂任何活动
        Map<Integer, List<ProductSaveParam>> productsByShopId = new LinkedHashMap<>();
        for (ProductSaveParam product : products) {
            Integer productShopId = product.getProductId() == null ? null : shopIdByProductId.get(product.getProductId());
            productsByShopId.computeIfAbsent(productShopId, k -> new ArrayList<>()).add(product);
        }

        Set<Integer> shopIds = productsByShopId.keySet().stream().filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Integer, List<PromotionVO>> promotionByShopId = shopIds.isEmpty()
                ? Map.of()
                : promotionService.getAllAvailablePromotionByShopIds(shopIds, userId);

        Map<Integer, ProductSaveParam> result = new HashMap<>();
        for (Map.Entry<Integer, List<ProductSaveParam>> entry : productsByShopId.entrySet()) {
            Integer productShopId = entry.getKey();
            List<PromotionVO> allAvailablePromotion = productShopId == null
                    ? List.of()
                    : promotionByShopId.getOrDefault(productShopId, List.of());
            result.putAll(getProductsPromotion(entry.getValue(), allAvailablePromotion, productShopId, promotionFrom, userId));
        }
        return result;
    }

    /**
     * 只取 product_id / shop_id 两列；查不到或 shop_id 为空的商品不进入结果。
     */
    Map<Integer, Integer> resolveShopIdByProductIds(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        return this.lambdaQuery()
                .select(Product::getProductId, Product::getShopId)
                .in(Product::getProductId, productIds)
                .list()
                .stream()
                .filter(p -> p.getShopId() != null)
                .collect(Collectors.toMap(Product::getProductId, Product::getShopId, (a, b) -> a));
    }


    @Override
    public Map<Integer, ProductSaveParam> getProductsPromotion(List<ProductSaveParam> products, List<PromotionVO> allAvailablePromotion, Integer shopId, String promotionFrom, Integer userId) {
        return getProductsPromotion(products, allAvailablePromotion, shopId, promotionFrom, userId, null);
    }

    /**
     * 列表整页优化重载：listCtx 非 null 时，productPromotion 行 / realPromotion JSON / 赠品 / 促销用 user
     * 均使用整页级缓存，避免同一份 allAvailablePromotion 对每个商品重复查库；listCtx 为 null 时行为与原方法一致。
     */
    private Map<Integer, ProductSaveParam> getProductsPromotion(List<ProductSaveParam> products, List<PromotionVO> allAvailablePromotion, Integer shopId, String promotionFrom, Integer userId, ProductListDetailContext listCtx) {
        promotionFrom = (promotionFrom == null) ? "list" : promotionFrom;
        userId = Optional.ofNullable(userId).orElse(SecurityUtils.getCurrentUserId());
        final Integer resolvedUserId = userId;
        Map<Integer, ProductSaveParam> productPromotion = new HashMap<>();

        // 赠品缓存：listCtx 非空走页级，否则本次调用内
        Map<Integer, ProductGift> giftCache = listCtx != null ? listCtx.giftCache : new HashMap<>();
        // 赠品商品详情缓存：同一赠品商品只查一次（listCtx 非空走页级，否则本次调用内）
        Map<Integer, Product> giftProductInfoCache = listCtx != null ? listCtx.giftProductInfoCache : new HashMap<>();
        // sku 列表按 productId 记忆化（每商品 productId 不同，保持调用内即可）
        Map<Integer, List<Integer>> productSkuIdsCache = new HashMap<>();
        // user / productPromotion 行 / realPromotion JSON：listCtx 非空走页级（整页只查一次），否则本次调用内
        User promotionUser = listCtx != null
                ? listCtx.promotionUserCache.computeIfAbsent(resolvedUserId, userMapper::selectById)
                : userMapper.selectById(userId);
        Map<Integer, ProductPromotion> productPromotionCache = listCtx != null ? listCtx.promotionRowCache : new HashMap<>();
        Map<String, String> realPromotionJsonCache = listCtx != null ? listCtx.realPromotionJsonCache : new HashMap<>();
        Map<Integer, Set<Integer>> displayableCouponIdsCache = new HashMap<>();

        for (ProductSaveParam product : products) {
            int filedId = switch (promotionFrom) {
                case "list" -> product.getProductId();
                case "cart" -> product.getCartId();
                case "detail" -> product.getSkuId();
                default -> 0;
            };

            product.setShopId(shopId);
            productPromotion.put(filedId, product);

            List<PromotionVO> promotionList = new ArrayList<>();

            for (PromotionVO promotion : allAvailablePromotion) {
                // 二期（D1096）：购物车/列表计价排除用户不可参与的活动（等价「购物车不展示该优惠」）；商详(detail)保留以便前台展示+置灰
                if (!"detail".equals(promotionFrom) && Boolean.FALSE.equals(promotion.getCanJoin())) {
                    continue;
                }
                // 商详优惠券与优惠券弹窗共用最终展示资格，避免仅凭 promotion 静态状态展示当前不可领取的券。
                if ("detail".equals(promotionFrom)
                        && promotion.getType() == PromotionType.TYPE_COUPON.getCode()
                        && product.getProductId() != null) {
                    int couponShopId = Optional.ofNullable(shopId).orElse(Optional.ofNullable(promotion.getShopId()).orElse(0));
                    Set<Integer> displayableCouponIds = displayableCouponIdsCache.computeIfAbsent(product.getProductId(), productId ->
                            filterDisplayableProductCoupons(couponService.getProductCouponList(productId, couponShopId), resolvedUserId)
                                    .stream()
                                    .map(CouponDetailVO::getCouponId)
                                    .collect(Collectors.toSet()));
                    if (!displayableCouponIds.contains(promotion.getRelationId())) {
                        continue;
                    }
                }
                if (checkPromotionIsAvailable(promotion, product, userId, promotionUser, productPromotionCache)) {
                    String realKey = promotion.getType() + ":" + promotion.getRelationId();
                    final var promoType = promotion.getType();
                    final var promoRelationId = promotion.getRelationId();
                    String realJson = realPromotionJsonCache.computeIfAbsent(realKey,
                            k -> JsonUtil.toJson(realPromotion(promoType, promoRelationId)));
                    promotion.setData(JsonUtil.fromJson(realJson, JSONObject.class));
                    // 处理空值异常
                    if (promotion.getData() == null) {
                        continue;
                    }
                    // 处理时间
                    if (promotion.getData() != null) {
                        promotion.getData().put("startTime", promotion.getStartTime());
                        promotion.getData().put("endTime", promotion.getEndTime());
                    }
                    if ("list".equals(promotionFrom)) {
                        if (YES.equals(promotion.getIsDelete())) {
                            continue;
                        }
                    }

                    PromotionVO promotionVO = new PromotionVO();
                    BeanUtils.copyProperties(promotion, promotionVO);
                    promotion = promotionVO;

                    List<Integer> skuIds = promotion.getSkuIds();
                    Integer productId = product.getProductId();
                    if (skuIds != null && !skuIds.isEmpty() && productId != null) {
                        List<Integer> list = productSkuIdsCache.computeIfAbsent(productId,
                                pid -> {
                                    // 列表路径复用整页已批量加载的 SKU；其他调用方回退逐商品查询
                                    if (listCtx != null && listCtx.productSkuMap != null) {
                                        return listCtx.productSkuMap.getOrDefault(pid, List.of()).stream()
                                                .map(ProductSkuDTO::getSkuId).toList();
                                    }
                                    return productSkuService.getProductSkusByPid(pid).stream()
                                            .map(ProductSkuDTO::getSkuId).toList();
                                });
                        if (!list.isEmpty()) {
                            List<Integer> allowSkuIds = new ArrayList<>(promotion.getSkuIds());
                            // 商品的skuIds和优惠活动做一个交集
                            allowSkuIds.retainAll(list);
                            promotion.setSkuIds(allowSkuIds);
                        } else {
                            promotion.setSkuIds(list);
                        }
                    }

                    promotionList.add(promotion);

                    if ("list".equals(promotionFrom) && promotion.getType() != TYPE_LIMIT_DISCOUNT.getCode()) {
                        break;
                    }

                    if (promotion.getType() == TYPE_GIVE.getCode()) {
                        Map<String, Object> data = promotion.getData();
                        String string = data.get("promotionTypeData").toString();
                        List<PromotionTypeDTO> gitDataList = JsonUtil.fromJson(string, new TypeReference<>() {
                        });

                        // 处理赠品
                        gitDataList.forEach(promotionTypeDTO -> {
                            int giftId = promotionTypeDTO.getGiftId();
                            // 同一赠品只查一次；null 也缓存，避免重复查缺失赠品（computeIfAbsent 不缓存 null，N10）
                            ProductGift productGift;
                            if (giftCache.containsKey(giftId)) {
                                productGift = giftCache.get(giftId);
                            } else {
                                productGift = productGiftService.getById(giftId);
                                giftCache.put(giftId, productGift);
                            }
                            if (productGift != null) {
                                ProductGiftVO productGiftVO = new ProductGiftVO();
                                BeanUtils.copyProperties(productGift, productGiftVO);
                                Integer giftProductId = productGift.getProductId();
                                // 同一赠品商品只查一次；null 也缓存，避免重复查缺失商品（computeIfAbsent 不缓存 null）
                                Product giftProductInfo;
                                if (giftProductInfoCache.containsKey(giftProductId)) {
                                    giftProductInfo = giftProductInfoCache.get(giftProductId);
                                } else {
                                    giftProductInfo = getById(giftProductId);
                                    giftProductInfoCache.put(giftProductId, giftProductInfo);
                                }
                                productGiftVO.setProductInfo(giftProductInfo);
                                promotionTypeDTO.setGift(productGiftVO);
                            }
                        });

                        // 构建返回数据，保留满赠类型相关字段
                        Map<String, Object> giveData = new HashMap<>();
                        giveData.put("promotionTypeData", gitDataList);
                        giveData.put("promotionDesc", gitDataList);
                        if (data.get("promotionDescText") != null) {
                            giveData.put("promotionDescText", data.get("promotionDescText"));
                        }
                        // 填充满赠的类型字段
                        if (data.get("promotionType") != null) {
                            giveData.put("promotionType", data.get("promotionType"));
                        }
                        if (data.get("rulesType") != null) {
                            giveData.put("rulesType", data.get("rulesType"));
                        }
                        if (data.get("unit") != null) {
                            giveData.put("unit", data.get("unit"));
                        }
                        promotion.setData(giveData);
                    }
                }
            }

            product.setActivityInfo(promotionList);

        }
        return productPromotion;
    }


    @Override
    public List<Product> getRelatedList(Integer productId) {
        Product product = getById(productId);
        if (product == null) {
            return List.of();
        }
        List<Integer> productIds = JsonUtil.jsonToList(product.getProductRelated(), Integer.class);
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<Product>()
                .in(Product::getProductId, productIds)
                .select(Product::getProductId, Product::getProductName, Product::getProductSn, Product::getProductTsn, Product::getProductPrice, Product::getProductStock, Product::getProductWeight, Product::getProductDesc, Product::getPicUrl, Product::getPicThumb, Product::getPicOriginal)
                .last("limit 10"));
    }

    @Override
    public void priceInquiry(ProductPriceInquiryDTO productPriceInquiryDTO) {
        Product product = getById(productPriceInquiryDTO.getProductId());
        if (product == null) {
            throw new GlobalException(messageHelper.get("error.product.not.exist"));
        }
        PriceInquiry priceInquiry = new PriceInquiry();
        priceInquiry.setProductId(productPriceInquiryDTO.getProductId());
        priceInquiry.setMobile(productPriceInquiryDTO.getMobile());
        priceInquiry.setEmail(productPriceInquiryDTO.getEmail());
        priceInquiry.setContent(productPriceInquiryDTO.getContent());
        priceInquiry.setShopId(product.getShopId());
        priceInquiry.setUserId(getCurrentUserId());
        priceInquiry.setUserIsDel(0);
        priceInquiry.setCreateTime(StringUtils.getCurrentTime());
        priceInquiryMapper.insert(priceInquiry);

        // B2B 商品询价独立于会员留言，使用商品消息-商品询价类型通知对应店铺。
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("route", "productInquiry");
        relatedData.put("inquiryId", priceInquiry.getId());
        relatedData.put("productId", product.getProductId());
        AdminMsgCreateDTO message = new AdminMsgCreateDTO();
        message.setMsgType(AdminMsgTypeEnum.PRODUCT_INQUIRY.getCatId());
        message.setShopId(product.getShopId());
        message.setVendorId(product.getVendorId() == null ? 0 : product.getVendorId());
        message.setAdminId(0);
        message.setProductId(product.getProductId());
        message.setTitle("您有一个新的商品询价");
        message.setContent("商品【" + product.getProductName() + "】收到新的询价，请及时处理");
        message.setRelatedData(relatedData);
        adminMsgService.createMessage(message);
    }

    /**
     * 检查活动是否可用
     *
     * @param promotion 活动
     * @param dto       商品
     * @return Boolean
     */
    public Boolean checkPromotionIsAvailable(PromotionVO promotion, ProductSaveParam dto, Integer userId) {
        Integer resolvedUserId = Optional.ofNullable(userId).orElse(SecurityUtils.getCurrentUserId());
        return checkPromotionIsAvailable(promotion, dto, resolvedUserId, userMapper.selectById(resolvedUserId), new HashMap<>());
    }

    /**
     * 带缓存的可用性判断：user 只查一次、productPromotion 按 relationId 记忆化，避免每个促销×每个商品重复查库
     */
    private Boolean checkPromotionIsAvailable(PromotionVO promotion, ProductSaveParam dto, Integer userId, User user, Map<Integer, ProductPromotion> productPromotionCache) {
        // 注意：computeIfAbsent 不缓存 null，而多数促销类型(1秒杀/2券/6限时/7拼团)的 relationId 不在 product_promotion 表中，
        // 会返回 null 导致每个商品都重复查库；此处显式缓存 null 结果，避免整页 N×M 次无效查询。
        Integer relId = promotion.getRelationId();
        ProductPromotion productPromotion;
        if (productPromotionCache.containsKey(relId)) {
            productPromotion = productPromotionCache.get(relId);
        } else {
            productPromotion = productPromotionService.getById(relId);
            productPromotionCache.put(relId, productPromotion);
        }
        if (user != null && productPromotion != null) {
            String limitUserRank = productPromotion.getLimitUserRank();
            List<Integer> rankIds = StrUtil.isNotBlank(limitUserRank)
                    ? JsonUtil.jsonToList(limitUserRank, Integer.class)
                    : List.of();

            if (CollUtil.isNotEmpty(rankIds) && !rankIds.contains(user.getRankId())) {
                return false;
            }
        }

        Integer productId = dto.getProductId();

        if (productId != null) {
            if (promotion.getRange().equals(PROMOTION_RANGE_PRODUCT.getCode()) && !promotion.getRangeData().contains(productId)) {
                return false;
            } else if (promotion.getRange().equals(PROMOTION_RANGE_EXCLUDE_PRODUCT.getCode()) && promotion.getRangeData().contains(productId)) {
                return false;
            }
            if (dto.getShopId() == null) {
                Product byId = this.getById(dto.getProductId());
                if (byId != null) {
                    dto.setShopId(byId.getShopId());
                }
            }
        }

        Integer skuId = dto.getSkuId();
        if (skuId != null && skuId > 0) {
            List<Integer> skuIds = promotion.getSkuIds();
            if (CollUtil.isNotEmpty(skuIds) && !skuIds.contains(skuId)) {
                return false;
            }
        }

        return Objects.equals(dto.getShopId(), promotion.getShopId());
    }

    /**
     * 获取真实活动详情
     *
     * @param type       活动类型
     * @param relationId 关联ID
     * @return Map<Integer, Object>
     */
    public Map<String, Object> realPromotion(Integer type, int relationId) {
        switch (type.toString()) {
            case "1": {
                Map<String, Object> data = JsonUtil.fromJson(JsonUtil.toJson(seckillService.getById(relationId)), JSONObject.class);
                if (data != null) {
                    translatePromotionTitle(data, "seckillName");
                    data.put("promotionDesc", formatLocalizedPromotionTemplate("promotion.seckill"));
                }
                return data;
            }
            case "2":
                Coupon coupon = couponService.getById(relationId);
                JSONObject couponJson = JsonUtil.fromJson(JsonUtil.toJson(coupon), JSONObject.class);
                translatePromotionTitle(couponJson, "couponName");
                couponJson.set("promotionDesc", couponService.getCouponPromotionDesc(coupon));
                return couponJson;
            case "3":
            case "4":
            case "5": {
                ProductPromotion productPromotion = productPromotionMapper.selectById(relationId);
                Map<String, Object> data = JsonUtil.fromJson(JsonUtil.toJson(productPromotion), JSONObject.class);
                if (data != null) {
                    translatePromotionTitle(data, "promotionName");
                    String promotionDesc = getProductPromotionDesc(productPromotion);
                    data.put("promotionDesc", promotionDesc);
                    // type=5 historically uses promotionDesc for the structured gift rules;
                    // expose the localized text separately to preserve that response shape.
                    if (productPromotion != null && productPromotion.getPromotionType() == 3) {
                        data.put("promotionDescText", promotionDesc);
                    }
                }
                return data;
            }
            case "6": {
                Map<String, Object> data = JsonUtil.fromJson(JsonUtil.toJson(timeDiscountMapper.selectById(relationId)), JSONObject.class);
                if (data != null) {
                    translatePromotionTitle(data, "promotionName");
                    data.put("promotionDesc", formatLocalizedPromotionTemplate("promotion.time.discount"));
                }
                return data;
            }
            case "7": {
                Map<String, Object> data = JsonUtil.fromJson(JsonUtil.toJson(grouponPromotionMapper.selectById(relationId)), JSONObject.class);
                if (data != null) {
                    translatePromotionTitle(data, "name");
                    data.put("promotionDesc", formatLocalizedPromotionTemplate("promotion.groupon"));
                }
                return data;
            }
            default:
                return new HashMap<>();
        }
    }

    /**
     * 生成满减/满折/满赠活动的展示描述。文案由资源模板决定语序和空格，
     * 这里仅负责提取首条规则并传入格式化后的数值。
     */
    private String getProductPromotionDesc(ProductPromotion promotion) {
        String template;
        Object[] args;
        if (promotion == null || promotion.getPromotionType() == null
                || StrUtil.isBlank(promotion.getPromotionTypeData())) {
            return localizePromotionTitle(promotion != null ? StrUtil.blankToDefault(promotion.getPromotionName(), "") : "");
        }
        try {
            List<ProductPromotionVO.PromotionTypeData> rules = JsonUtil.fromJson(
                    promotion.getPromotionTypeData(), new TypeReference<>() {
                    });
            if (CollUtil.isEmpty(rules) || rules.getFirst().getMinAmount() == null) {
                return localizePromotionTitle(StrUtil.blankToDefault(promotion.getPromotionName(), ""));
            }
            ProductPromotionVO.PromotionTypeData rule = rules.getFirst();
            String threshold = rule.getMinAmount().stripTrailingZeros().toPlainString();
            boolean itemUnit = Objects.equals(promotion.getUnit(), 2);
            String unitSuffix = itemUnit ? ".item" : ".amount";
            String key = switch (promotion.getPromotionType()) {
                case 1 -> (Objects.equals(promotion.getRulesType(), 0)
                        ? "promotion.each.reduce" : "promotion.full.reduce") + unitSuffix;
                case 2 -> (Objects.equals(promotion.getRulesType(), 0)
                        ? "promotion.each.discount" : "promotion.full.discount") + unitSuffix;
                case 3 -> (Objects.equals(promotion.getRulesType(), 0)
                        ? "promotion.each.gift" : "promotion.full.gift") + unitSuffix;
                default -> null;
            };
            if (key == null) {
                return localizePromotionTitle(StrUtil.blankToDefault(promotion.getPromotionName(), ""));
            }
            String value = rule.getReduce() == null ? "0" : rule.getReduce().stripTrailingZeros().toPlainString();
            args = promotion.getPromotionType() == 3
                    ? new Object[]{threshold}
                    : new Object[]{threshold, value};
            return formatLocalizedPromotionTemplate(key, args);
        } catch (Exception ignored) {
            return localizePromotionTitle(StrUtil.blankToDefault(promotion.getPromotionName(), ""));
        }
    }

    /**
     * 活动标题兜底翻译。非简体中文交由既有翻译数据/缓存按 locale 解析。
     */
    private String localizePromotionTitle(String title) {
        if (messageHelper.isSimplifiedChineseLocale() || StrUtil.isBlank(title)) {
            return title;
        }
        return translatePackage.translate(title, DataType.COUPON_TITLE.getCode());
    }

    private String formatLocalizedPromotionTemplate(String key, Object... args) {
        String template = messageHelper.getChineseTemplate(key);
        if (!messageHelper.isSimplifiedChineseLocale()) {
            template = translatePackage.translate(template);
        }
        return new MessageFormat(template).format(args);
    }

    /**
     * realPromotion 返回的是 Map，无法触发 VO/model 上的 @JsonTranslate，需按活动标题类型显式翻译。
     */
    private void translatePromotionTitle(Map<String, Object> data, String field) {
        if (data == null) {
            return;
        }
        Object value = data.get(field);
        if (value instanceof String title && StrUtil.isNotBlank(title)) {
            data.put(field, translatePackage.translate(title, DataType.COUPON_TITLE.getCode()));
        }
    }

    @Override
    public List<Category> getProductCategoryList(ProductListDTO listDTO) {
        // 创建查询构造器
        MPJLambdaWrapper<Product> queryWrapper = new MPJLambdaWrapper<>();
        // 构造查询条件
        buildQueryWrapper(queryWrapper, listDTO);
        // 查询所有结果
        List<Product> list = this.list(queryWrapper);
        // 获取商品 categoryId 列表，去掉重复的
        List<Integer> categoryIdList = list.stream()
                .map(Product::getCategoryId)
                .distinct()
                .toList();
        if (ObjectUtil.isEmpty(categoryIdList)) {
            return Collections.emptyList();
        }
        return categoryService.listByIds(categoryIdList);
    }

    @Override
    public List<Brand> getProductBrandList(ProductListDTO listDTO) {
        // 创建查询构造器
        MPJLambdaWrapper<Product> queryWrapper = new MPJLambdaWrapper<>();
        // 构造查询条件
        buildQueryWrapper(queryWrapper, listDTO);
        // 查询所有结果
        List<Product> list = this.list(queryWrapper);
        // 获取商品 categoryId 列表，去掉重复的
        List<Integer> brandIdList = list.stream()
                .map(Product::getBrandId)
                .distinct()
                .toList();
        if (ObjectUtil.isEmpty(brandIdList)) {
            return Collections.emptyList();
        }

        return brandService.lambdaQuery()
                .in(Brand::getBrandId, brandIdList)
                .eq(Brand::getStatus, BrandStatusEnum.APPROVED.getCode())
                .eq(Brand::getIsShow, 1)
                .list();
    }

    @Override
    public BigDecimal getMaxPrice(ProductListDTO listDTO) {
        // 创建查询构造器
        MPJLambdaWrapper<Product> queryWrapper = new MPJLambdaWrapper<>();
        listDTO.setMaxPrice(null);
        listDTO.setMinPrice(null);
        // 构造查询条件，例如根据关键词、分类、品牌等筛选商品
        buildQueryWrapper(queryWrapper, listDTO);

        // 查询并获取符合条件的商品中价格最高的一个
        // 使用 orderByDesc 按商品价格降序排序，并通过 last("limit 1") 限制只取一条记录
        Product product = this.getOne(queryWrapper.orderByDesc(Product::getProductPrice).last("limit 1"));

        // 如果查询结果不为空，返回其商品价格；否则返回 BigDecimal.ZERO
        return product != null ? product.getProductPrice() : BigDecimal.ZERO;

    }

    @Override
    public Page<ProductListResDTO> getRelatedProductList(Integer productId) {
        // 获取商品信息
        Product product = this.getById(productId);

        List<Integer> productIdList = StringUtils.str2IntList(product.getProductRelated());
        if (productIdList.isEmpty()) {
            return null;
        }
        ProductListDTO productListDTO = new ProductListDTO();
        productListDTO.setIdList(productIdList);
        return list(productListDTO);
    }

    @Override
    public String getProductSnById(Integer productId) {
        if (productId == null) {
            return null;
        }
        Product product = this.getById(productId);
        if (product == null) {
            return null;
        }
        return product.getProductSn();

    }

    @Override
    public String getSharePageHtml(Integer productId, Integer salesmanId) {
        String name = "";
        String pic = "";
        try {
            Product product = this.getById(productId);
            if (product != null) {
                name = StrUtil.isNotBlank(product.getProductName()) ? product.getProductName() : "";
                pic = StrUtil.isNotBlank(product.getPicUrl()) ? product.getPicUrl() : "";
            }
        } catch (Exception e) {
            log.warn("[getSharePageHtml] 查询商品失败: id={}, {}", productId, e.getMessage());
        }

        String h5Domain = "";
        try {
            String configVal = configService.getConfigVal(SettingsEnum.H5_DOMAIN);
            if (StrUtil.isNotBlank(configVal)) {
                h5Domain = configVal;
            }
        } catch (Exception e) {
            log.warn("[getSharePageHtml] 获取H5域名失败: {}", e.getMessage());
        }

        String targetUrl = h5Domain + "/pages/product/index?id=" + productId;
        if (salesmanId != null && salesmanId > 0) {
            targetUrl += "&salesmanId=" + salesmanId;
        }
        String ogImage = ShareHtmlUtil.toAbsoluteUrl(pic, h5Domain);
        return ShareHtmlUtil.buildOgHtml(name, name, ogImage, targetUrl, "product");
    }

    @Override
    public String getParticiple(ProductKeyDTO dto) {
        // 去除首尾空格
        String trimmed = StrUtil.trim(dto.getProductName());
        // 移除所有空格
        String withoutSpaces = StrUtil.removeAll(trimmed, " ");
        // 对 HTML 特殊字符进行转义
        String keyword = EscapeUtil.escapeHtml4(withoutSpaces);
        return StringUtils.cutForSearch(keyword);
    }

    @Override
    @Transactional
    public String updateFieldProduct(UpdateFieldProductDTO updateField, String[] allowFields) {
        Product product = this.getById(updateField.getId());
        Assert.notNull(product, () -> new GlobalException("商品不存在"));

        if ("productStatus".equals(updateField.getField())) {
            Object rawStatus = updateField.getVal();
            String status = String.valueOf(rawStatus);
            // 禁止依赖数据库把01、空白或小数隐式转为1，保留标准值原类型兼容既有上下架分支。
            if (!(rawStatus instanceof Number || rawStatus instanceof String)
                    || !("0".equals(status) || "1".equals(status))) {
                throw new GlobalException("商品上架状态只能为0或1");
            }
        }

        // 保证金准入：欠缴被限制（限上新/全店下架）时禁止店铺上架商品（目标状态为在售时阻断，不吞异常）
        if (Objects.equals(updateField.getField(), "productStatus")
                && "1".equals(String.valueOf(updateField.getVal()))
                && product.getShopId() != null && product.getShopId() > 0) {
            depositLifecycleService.assertOnShelfAllowed(
                    com.tigshop.bean.enums.deposit.DepositOwnerType.SHOP.getCode(),
                    product.getShopId());
        }

        //当商品下架的时候，拼团活动中的拼团商品也移除掉
        if (tigshopProperties.getIsO2o() == 0 && updateField.getField().equals("productStatus") && ObjectUtil.equals(updateField.getVal(), 0)) {
            grouponPromotionItemMapper.delete(Wrappers.lambdaQuery(GrouponPromotionItem.class).eq(GrouponPromotionItem::getProductId, product.getProductId()));
        }

        if (product.getVendorProductId() == null) {
            return this.updateProduct(updateField, allowFields);
        } else {
            //判断是否是门店商品，并修改上下架状态（总后台管理员例外，需能操作任意门店商品）
            if (tigshopProperties.getIsO2o() == 1
                    && AdminTypeEnum.fromCode(getAdminType()) != AdminTypeEnum.ADMIN
                    && List.of("productStatus", "isDelete").contains(updateField.getField())
                    && !ObjectUtil.equals(getShopId(), product.getShopId())) {
                return "操作成功";
            }
            return this.updateVendorProduct(updateField, allowFields);
        }
    }

    /**
     * 物理删除前置校验：商品必须已在回收站。
     *
     * <p>该规则此前只存在于后台前端——非回收站页签不渲染删除按钮而已。走 API 的调用方绕得过 UI，
     * 而物理删除不可逆，故下沉到服务端。后台既有删除入口本就只在回收站出现，实际行为不受影响。
     */
    private void assertInRecycleBin(Product product) {
        if (product == null) {
            throw new GlobalException("商品不存在");
        }
        if (!ObjectUtil.equals(product.getIsDelete(), YES)) {
            throw new GlobalException("商品「" + product.getProductName() + "」还没有移入回收站，请先移入回收站再删除");
        }
    }

    /** 批量删除同理；整批拒绝而非跳过，并点名是哪些商品，否则调用方不知道该先处理谁。 */
    private void assertAllInRecycleBin(List<Product> products) {
        List<String> live = products.stream()
                .filter(product -> !ObjectUtil.equals(product.getIsDelete(), YES))
                .map(Product::getProductName)
                .toList();
        if (!live.isEmpty()) {
            throw new GlobalException("这些商品还没有移入回收站，请先移入回收站再删除：" + String.join("、", live));
        }
    }

    private String updateProduct(UpdateFieldProductDTO updateField, String[] allowFields) {
        if ("del".equals(updateField.getField())) {
            assertInRecycleBin(this.getById(updateField.getId()));
            this.removeById(updateField.getId());
            productEsSyncService.deleteProductAsync(Long.valueOf(updateField.getId()));
            return "操作成功";
        }

        Product product = this.getById(updateField.getId());
        if (product == null) {
            throw new GlobalException("商品不存在");
        }
        // 商品编辑库存不可改：列表快捷改库存（单品）时拒绝，引导去“库存调整”。
        // 多规格快捷更新可能只改价格，库存开关应仅约束实际携带库存的请求。
        if ("productStock".equals(updateField.getField())
                && "0".equals(configService.getConfigVal(SettingsEnum.PRODUCT_EDIT_STOCK_MODIFY))) {
            throw new GlobalException("商品编辑库存已关闭，如需修改库存请前往 库存管理->库存调整 功能中修改");
        }
        if (ObjectUtil.equals(updateField.getField(), "productSku")) {
            String json = JSONUtil.toJsonStr(updateField.getVal());
            List<ProductSkuInfoDTO> productSkuList = JSONUtil.toList(json, ProductSkuInfoDTO.class);

            boolean updatesStock = productSkuList.stream().anyMatch(dto -> dto.getSkuStock() != null);
            boolean updatesPrice = productSkuList.stream().anyMatch(dto -> dto.getSkuPrice() != null);
            if (updatesStock && "0".equals(configService.getConfigVal(SettingsEnum.PRODUCT_EDIT_STOCK_MODIFY))) {
                throw new GlobalException("商品编辑库存已关闭，如需修改库存请前往 库存管理->库存调整 功能中修改");
            }

            //自营商品不是admin修改
            boolean notAdminUpdSkuStock = product.getShopId() == 0 && AdminTypeEnum.fromCode(HeaderUtils.getAdminType()) != AdminTypeEnum.ADMIN;
            //是否属于自己product
            boolean isSelfProduct = Objects.equals(product.getShopId(), getShopId());
            // 修改根据配置项进行修改
            GetStoreVO settings = configService.getSettings(GetStoreVO.class);
            //使用门店独立库存
            boolean storeUseSoloProductStock = settings != null && Objects.equals(1, settings.getStoreUseSoloProductStock());

            if (CollUtil.isNotEmpty(productSkuList)) {
                for (ProductSkuInfoDTO dto : productSkuList) {
                    if (dto.getSkuId() == null) {
                        throw new GlobalException("商品规格ID不能为空");
                    }

                    if (isSelfProduct) {
                        ProductSku productSku = productSkuService.getById(dto.getSkuId());
                        if (productSku == null || !Objects.equals(productSku.getProductId(), product.getProductId())) {
                            throw new GlobalException("商品规格不存在");
                        }
                        if (dto.getSkuPrice() != null) {
                            checkProductPrice(dto.getSkuPrice());
                        }

                        productSkuService.lambdaUpdate()
                                .eq(ProductSku::getSkuId, dto.getSkuId())
                                .set(dto.getSkuStock() != null, ProductSku::getSkuStock, dto.getSkuStock())
                                .set(dto.getSkuPrice() != null, ProductSku::getSkuPrice, dto.getSkuPrice())
                                .update();

                        if (dto.getSkuStock() != null) {
                            int number = dto.getSkuStock() - productSku.getSkuStock();
                            ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                                    .productId(product.getProductId())
                                    .specId(productSku.getSkuId())
                                    .number(dto.getSkuStock())
                                    .addTime(StringUtils.getCurrentTime())
                                    .oldNumber(productSku.getSkuStock())
                                    .type(number > 0 ? 1 : 2)
                                    .changeNumber(Math.abs(number))
                                    .desc(number > 0 ? "增加库存" : "扣减库存")
                                    .shopId(product.getShopId())
                                    .build();
                            productInventoryLogService.save(productInventoryLog);
                        }
                    }

                }
                int sum = productSkuList.stream().filter(dto -> dto.getSkuStock() != null)
                        .mapToInt(ProductSkuInfoDTO::getSkuStock).sum();

                if (isSelfProduct && updatesStock) {
                    sum = productSkuService.lambdaQuery().eq(ProductSku::getProductId, product.getProductId())
                            .list().stream().map(ProductSku::getSkuStock).filter(Objects::nonNull)
                            .mapToInt(Integer::intValue).sum();
                    int oldProductStock = product.getProductStock() == null ? 0 : product.getProductStock();
                    product.setProductStock(sum);
                    this.updateById(product);
                    int diff = sum - oldProductStock;
                    ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                            .productId(product.getProductId())
                            .specId(0)
                            .number(sum)
                            .addTime(StringUtils.getCurrentTime())
                            .oldNumber(oldProductStock)
                            .type(diff > 0 ? 1 : 2)
                            .changeNumber(diff)
                            .desc(diff > 0 ? "增加库存" : "扣减库存")
                            .shopId(product.getShopId())
                            .build();
                    productInventoryLogService.save(productInventoryLog);
                }


                if (isSelfProduct && updatesPrice) {
                    BigDecimal lowestSkuPrice = productSkuService.lambdaQuery()
                            .eq(ProductSku::getProductId, product.getProductId())
                            .list().stream()
                            .map(ProductSku::getSkuPrice)
                            .filter(Objects::nonNull)
                            .min(BigDecimal::compareTo)
                            .orElse(null);
                    if (lowestSkuPrice != null) {
                        product.setProductPrice(lowestSkuPrice);
                        this.updateById(product);
                        try {
                            productEsSyncService.syncProductAsync(Long.valueOf(product.getProductId()));
                        } catch (Exception e) {
                            log.error("商品快捷改价后ES同步失败: {}", product.getProductId(), e);
                        }
                    }
                }
            }

            return "操作成功";
        }
        // 判断是否是卡密商品，并且状态改为上架。需要判断卡密是否可以使用
        if (product.getProductType() == PRODUCT_TYPE_CARD.getCode()
                && ObjectUtil.equals(updateField.getField(), "productStatus")
                && ObjectUtil.equals(updateField.getVal(), "1")) {
            ECardGroup byId = ecardGroupService.getById(product.getCardGroupId());
            if (byId.getIsUse() == 0) {
                throw new GlobalException("电子卡卷组【" + byId.getGroupName() + "】未启用，请先启用电子卡卷组！");
            }
        }


        boolean isAdminLogin = AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.ADMIN;
        if (tigshopProperties.getIsO2o() == 0
                || (tigshopProperties.getIsO2o() == 1
                && (product.getShopId().equals(getShopId()) || isAdminLogin))) {
            UpdateFieldDTO updateFieldDTO = new UpdateFieldDTO();
            updateFieldDTO.setId(updateField.getId());
            updateFieldDTO.setField(updateField.getField());
            updateFieldDTO.setVal(updateField.getVal().toString());
            // 修改状态
            updateField(updateFieldDTO, allowFields);
        }
        if ("productStock".equals(updateField.getField())) {
            int newStock = Integer.parseInt(updateField.getVal().toString());
            int number = newStock - product.getProductStock();
            ProductInventoryLog productInventoryLog = ProductInventoryLog.builder()
                    .productId(product.getProductId())
                    .specId(0)
                    .number(newStock)
                    .addTime(StringUtils.getCurrentTime())
                    .oldNumber(product.getProductStock())
                    .type(number > 0 ? 1 : 2)
                    .changeNumber(Math.abs(number))
                    .desc(number > 0 ? "增加库存" : "扣减库存")
                    .shopId(product.getShopId())
                    .build();
            productInventoryLogService.save(productInventoryLog);
        }

        if ("productSn".equals(updateField.getField())) {
            Long count = this.lambdaQuery()
                    .eq(Product::getProductSn, updateField.getVal())
                    .ne(Product::getProductId, updateField.getId())
                    .count();
            if (count > 0) {
                throw new GlobalException("商品编号重复，请重新输入");
            }
        }

        //发送后台消息
        if (Objects.equals(updateField.getField(), "productStatus") && Objects.equals(updateField.getVal(), 0)) {
            AdminMsgCreateDTO createDTO = new AdminMsgCreateDTO();
            createDTO.setMsgType(AdminMsgTypeEnum.PRODUCT_OUT_OF_STOCK.getCatId());
            createDTO.setShopId(product.getShopId());
            createDTO.setProductId(product.getProductId());
            createDTO.setTitle("商品下架:" + product.getProductName());
            createDTO.setContent("您的商品【" + product.getProductName() + "】已经下架，请及时处理！");
            Map<String, Object> relatedData = new HashMap<>();
            relatedData.put("productId", product.getProductId());
            createDTO.setRelatedData(relatedData);
            adminMsgService.createMessage(createDTO);
        }

        // 异步同步到ES
        try {
            productEsSyncService.syncProductAsync(Long.valueOf(product.getProductId()));
        } catch (Exception e) {
            log.error("商品状态变更后ES同步失败: {}", product.getProductId(), e);
        }

        // 保证金收取节点：店铺商品上架触发收取缓冲（非阻断，不影响上架主流程）
        if (Objects.equals(updateField.getField(), "productStatus")
                && "1".equals(String.valueOf(updateField.getVal()))
                && product.getShopId() != null && product.getShopId() > 0) {
            try {
                depositLifecycleService.onCollectTrigger(
                        com.tigshop.bean.enums.deposit.DepositOwnerType.SHOP.getCode(),
                        product.getShopId(),
                        com.tigshop.bean.enums.deposit.DepositCollectNode.ON_SHELF.getCode());
            } catch (Exception ignore) {
            }
        }
        // 保证金：商品上下架/删除导致经营类目变化，记录应缴调整（非阻断）
        if (product.getShopId() != null && product.getShopId() > 0
                && (Objects.equals(updateField.getField(), "productStatus")
                || Objects.equals(updateField.getField(), "isDelete"))) {
            try {
                depositLifecycleService.onCategoryChange(
                        com.tigshop.bean.enums.deposit.DepositOwnerType.SHOP.getCode(),
                        product.getShopId());
            } catch (Exception ignore) {
            }
        }

        return "操作成功";
    }

    private String updateVendorProduct(UpdateFieldProductDTO updateField, String[] allowFields) {
        if ("del".equals(updateField.getField())) {
            assertInRecycleBin(this.getById(updateField.getId()));
            this.removeById(updateField.getId());
            productEsSyncService.deleteProductAsync(Long.valueOf(updateField.getId()));
            return "操作成功";
        }

        Product product = this.getById(updateField.getId());


        UpdateFieldDTO updateFieldDTO = new UpdateFieldDTO();
        updateFieldDTO.setId(updateField.getId());
        updateFieldDTO.setField(updateField.getField());
        updateFieldDTO.setVal(updateField.getVal().toString());
        // 修改状态
        updateField(updateFieldDTO, allowFields);


        //发送后台消息
        if (Objects.equals(updateField.getField(), "productStatus") && Objects.equals(updateField.getVal(), "0")) {
            AdminMsgCreateDTO createDTO = new AdminMsgCreateDTO();
            createDTO.setMsgType(AdminMsgTypeEnum.PRODUCT_OUT_OF_STOCK.getCatId());
            createDTO.setShopId(product.getShopId());
            createDTO.setProductId(product.getProductId());
            createDTO.setTitle("商品下架:" + product.getProductName());
            createDTO.setContent("您的商品【" + product.getProductName() + "】已经下架，请及时处理！");
            Map<String, Object> relatedData = new HashMap<>();
            relatedData.put("productId", product.getProductId());
            createDTO.setRelatedData(relatedData);
            adminMsgService.createMessage(createDTO);
        }

        // 保证金收取节点：商品上架触发收取缓冲（仅店铺商品，非阻断，不影响上架主流程）
        if (Objects.equals(updateField.getField(), "productStatus")
                && "1".equals(String.valueOf(updateField.getVal()))
                && product != null && product.getShopId() != null && product.getShopId() > 0) {
            try {
                depositLifecycleService.onCollectTrigger(
                        com.tigshop.bean.enums.deposit.DepositOwnerType.SHOP.getCode(),
                        product.getShopId(),
                        com.tigshop.bean.enums.deposit.DepositCollectNode.ON_SHELF.getCode());
            } catch (Exception ignore) {
            }
        }
        // 保证金：商品上下架/删除导致经营类目变化，记录应缴调整（非阻断）
        if (product != null && product.getShopId() != null && product.getShopId() > 0
                && (Objects.equals(updateField.getField(), "productStatus")
                || Objects.equals(updateField.getField(), "isDelete"))) {
            try {
                depositLifecycleService.onCategoryChange(
                        com.tigshop.bean.enums.deposit.DepositOwnerType.SHOP.getCode(),
                        product.getShopId());
            } catch (Exception ignore) {
            }
        }

        return "操作成功";
    }

    @Transactional
    @Override
    public void batchOperation(ProductBatchOperationDTO dto) {
        if (ProductBatchOperationEnum.DEL.getCode().equals(dto.getType())) {
            List<Product> toDelete = this.lambdaQuery().in(Product::getProductId, dto.getIds()).list();
            assertAllInRecycleBin(toDelete);
            // 保证金：删除前取受影响店铺，物理删除后经营类目变化记录应缴调整（非阻断）
            java.util.List<Integer> delShopIds = toDelete.stream().map(Product::getShopId)
                    .filter(sid -> sid != null && sid > 0).distinct().toList();
            this.removeByIds(dto.getIds());
            dto.getIds().forEach(productId ->
                    productEsSyncService.deleteProductAsync(Long.valueOf(productId)));
            try {
                delShopIds.forEach(shopId -> depositLifecycleService.onCategoryChange(
                        com.tigshop.bean.enums.deposit.DepositOwnerType.SHOP.getCode(),
                        shopId));
            } catch (Exception ignore) {
            }
            return;
        }

        // 查询产品信息
        List<Product> products = this.lambdaQuery().in(Product::getProductId, dto.getIds()).list();
        if (CollUtil.isEmpty(products)) {
            return;
        }

        if (ProductBatchOperationEnum.UP.getCode().equals(dto.getType())) {
            if (tigshopProperties.getIsO2o() != 1) {
                products.forEach(product -> product.setProductStatus(1));
            } else {
                Integer shopId = HeaderUtils.getShopId();
                products.forEach(product -> {
                    boolean equals = Objects.equals(shopId, product.getShopId());
                    if (equals || shopId == 0) {
                        //admin和自己的商品都是可以操作的
                        product.setProductStatus(1);
                    } else {
                    }
                });
            }
        }

        if (ProductBatchOperationEnum.DOWN.getCode().equals(dto.getType())) {
            if (tigshopProperties.getIsO2o() != 1) {
                products.forEach(product -> product.setProductStatus(0));
            } else {
                // 020分配门店的情况
                Integer shopId = HeaderUtils.getShopId();
                products.forEach(product -> {
                    boolean equals = Objects.equals(shopId, product.getShopId());
                    if (equals || shopId == 0) {
                        //admin和自己的商品都是可以操作的
                        product.setProductStatus(0);
                    } else {
                    }
                });
            }
        }

        if (ProductBatchOperationEnum.RECYCLE.getCode().equals(dto.getType())) {
            // O2O：在售商品不能直接进回收站，需先下架（整批拦截）
            if (tigshopProperties.getIsO2o() == 1
                    && products.stream().anyMatch(p -> p.getProductStatus() != null
                    && p.getProductStatus() == ON_SALE.getCode())) {
                throw new GlobalException("存在在售商品，请先下架后再移入回收站");
            }
            products.forEach(product -> {
                product.setProductStatus(0);
                product.setIsDelete(1);
            });
        }

        if (ProductBatchOperationEnum.RESTORE.getCode().equals(dto.getType())) {
            products.forEach(product -> product.setIsDelete(0));
        }

        if (ProductBatchOperationEnum.AUDIT.getCode().equals(dto.getType())) {
            products.forEach(product -> product.setCheckStatus(1));
        }

        // 保证金准入：批量上架时被限制店铺禁止上新品（按店铺去重，任一命中即整批阻断，不吞异常）
        if (ProductBatchOperationEnum.UP.getCode().equals(dto.getType())) {
            products.stream()
                    .filter(p -> Objects.equals(p.getProductStatus(), 1)
                            && p.getShopId() != null && p.getShopId() > 0)
                    .map(Product::getShopId)
                    .distinct()
                    .forEach(shopId -> depositLifecycleService.assertOnShelfAllowed(
                            com.tigshop.bean.enums.deposit.DepositOwnerType.SHOP.getCode(),
                            shopId));
        }

        this.updateBatchById(products);

        // 异步同步到ES
        try {
            List<Long> productIds = products.stream()
                    .map(product -> Long.valueOf(product.getProductId()))
                    .collect(Collectors.toList());
            productEsSyncService.batchSyncProductsAsync(productIds);
        } catch (Exception e) {
            log.error("批量操作后ES同步失败: {}", dto.getType(), e);
        }

        // 保证金收取节点：批量上架触发收取缓冲（按店铺去重，非阻断，不影响上架主流程）
        if (ProductBatchOperationEnum.UP.getCode().equals(dto.getType())) {
            try {
                products.stream()
                        .filter(p -> Objects.equals(p.getProductStatus(), 1)
                                && p.getShopId() != null && p.getShopId() > 0)
                        .map(Product::getShopId)
                        .distinct()
                        .forEach(shopId -> depositLifecycleService.onCollectTrigger(
                                com.tigshop.bean.enums.deposit.DepositOwnerType.SHOP.getCode(),
                                shopId,
                                com.tigshop.bean.enums.deposit.DepositCollectNode.ON_SHELF.getCode()));
            } catch (Exception ignore) {
            }
        }
        // 保证金：批量上架/下架/回收/恢复导致经营类目变化，按店铺去重记录应缴调整（非阻断）
        try {
            products.stream()
                    .map(Product::getShopId)
                    .filter(sid -> sid != null && sid > 0)
                    .distinct()
                    .forEach(shopId -> depositLifecycleService.onCategoryChange(
                            com.tigshop.bean.enums.deposit.DepositOwnerType.SHOP.getCode(),
                            shopId));
        } catch (Exception ignore) {
        }
    }

    @Override
    public boolean recycle(ProductRecycleDTO dto) {
        if (tigshopProperties.getIsO2o() == 0) {
            Product product = this.getById(dto.getId());
            Assert.notNull(product, () -> new GlobalException("未知的产品"));

            product.setProductStatus(0);
            product.setIsDelete(1);
            this.updateById(product);

            // 保证金：单个商品回收下架导致经营类目变化，记录应缴调整（非阻断）
            if (product.getShopId() != null && product.getShopId() > 0) {
                try {
                    depositLifecycleService.onCategoryChange(
                            com.tigshop.bean.enums.deposit.DepositOwnerType.SHOP.getCode(),
                            product.getShopId());
                } catch (Exception ignore) {
                }
            }

            // 异步从ES删除
            try {
                productEsSyncService.deleteProductAsync(Long.valueOf(product.getProductId()));
            } catch (Exception e) {
                log.error("商品移入回收站后ES删除失败: {}", product.getProductId(), e);
            }
            return true;
        } else {
            Product product = this.getById(dto.getId());
            Assert.notNull(product, () -> new GlobalException("未知的产品"));

            // 在售商品不能直接进回收站，需先下架
            if (product.getProductStatus() != null && product.getProductStatus() == ON_SALE.getCode()) {
                throw new GlobalException("请先下架该商品后再移入回收站");
            }

            // 管理员登录可回收任意商品（含其他门店自建商品 shop_id>0），自建商品无 store_product 记录，需直接软删除 product 行
            if (ObjectUtil.equals(product.getShopId(), getShopId())
                    || AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.ADMIN) {
                product.setProductStatus(0);
                product.setIsDelete(1);
                this.updateById(product);
                // 保证金：店铺自营商品回收下架导致经营类目变化，记录应缴调整（非阻断）
                if (product.getShopId() != null && product.getShopId() > 0) {
                    try {
                        depositLifecycleService.onCategoryChange(
                                com.tigshop.bean.enums.deposit.DepositOwnerType.SHOP.getCode(),
                                product.getShopId());
                    } catch (Exception ignore) {
                    }
                }
            }

            return true;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String copy(ProductCopyDTO dto) {
        Product product = this.lambdaQuery().eq(Product::getProductId, dto.getProductId()).one();
        Assert.notNull(product, () -> new GlobalException("商品不存在"));

        // 当前时间（周期购配置等表会用到）
        Long currentTime = StringUtils.getCurrentTime();

        // 1. 新增产品
        product.setProductId(null);
        String newProductSn = this.createNewProductSn();
        product.setProductSn(newProductSn);
        this.save(product);
        Integer newProductId = product.getProductId();

        // 2. 关联产品文章
        List<ProductArticle> productArticles = productArticleService.lambdaQuery().eq(ProductArticle::getGoodsId, dto.getProductId()).list();
        if (CollUtil.isNotEmpty(productArticles)) {
            productArticles.forEach(item -> item.setGoodsId(newProductId));
            productArticleService.saveBatch(productArticles);
        }

        // 3. 保存产品属性
        List<ProductAttribute> productAttributes = productAttributesService.lambdaQuery().eq(ProductAttribute::getProductId, dto.getProductId()).list();
        if (CollUtil.isNotEmpty(productAttributes)) {
            productAttributes.forEach(item -> {
                item.setAttributesId(null);
                item.setProductId(newProductId);
            });
            productAttributesService.saveBatch(productAttributes);
        }

        // 4. 保存产品规格
        List<ProductSku> productSkus = productSkuService.lambdaQuery().eq(ProductSku::getProductId, dto.getProductId()).list();
        applyCopiedSkuStatus(productSkus, dto.getProductList());
        List<ProductSku> newProductSkus = new ArrayList<>();
        // SKU ID 映射：旧skuId -> 新skuId（用于周期购 SKU 单独设置时复制 product_cycle_spec.sku_id）
        Map<Integer, Integer> skuIdMap = new HashMap<>();
        if (CollUtil.isNotEmpty(productSkus)) {
            List<Integer> oldSkuIds = productSkus.stream().map(ProductSku::getSkuId).toList();
            for (int i = 0; i < productSkus.size(); i++) {
                ProductSku productSku = productSkus.get(i);
                productSku.setSkuId(null);
                productSku.setProductId(newProductId);
                String skuSn = newProductSn + "-" + (i + 1);
                productSku.setSkuSn(skuSn);
                newProductSkus.add(productSku);
            }
            productSkuService.saveBatch(newProductSkus);
            updateProductSkuSummary(product, newProductSkus);
            for (int i = 0; i < oldSkuIds.size(); i++) {
                Integer oldSkuId = oldSkuIds.get(i);
                Integer newSkuId = newProductSkus.get(i).getSkuId();
                if (oldSkuId != null && newSkuId != null) {
                    skuIdMap.put(oldSkuId, newSkuId);
                }
            }
        }


        // 5. 保存产品相册
        List<ProductGallery> productGalleries = productGalleryService.lambdaQuery()
                .eq(ProductGallery::getProductId, dto.getProductId())
                .orderByAsc(ProductGallery::getSortOrder)
                .list();
        if (CollUtil.isNotEmpty(productGalleries)) {
            productGalleries.forEach(item -> {
                item.setProductId(newProductId);
                item.setPicId(null);
            });
            productGalleryService.saveBatch(productGalleries);
        }

        //6. 保存产品视频
        List<ProductVideo> productVideos = productVideoService.lambdaQuery()
                .eq(ProductVideo::getProductId, dto.getProductId())
                .list();
        if (CollUtil.isNotEmpty(productVideos)) {
            productVideos.forEach(item -> {
                item.setProductId(newProductId);
                item.setId(null);
            });
            productVideoService.saveBatch(productVideos);
        }
        // 产品新增日志
        adminLogService.createByLogInfo(StrUtil.format("创建商品:{}", product.getProductName()));
        return "复制成功";
    }

    @Override
    public boolean audit(ProductAuditDTO dto) {
        dto.validParam();

        Product product = this.getById(dto.getId());
        Assert.notNull(product, () -> new GlobalException("商品不存在"));
        Assert.isTrue(CheckStatus.PENDING.getCode() == product.getCheckStatus(), () -> new GlobalException("商品审核状态错误"));

        product.setCheckStatus(dto.getCheckStatus());
        product.setCheckReason(dto.getCheckReason());
        // 审核通过直接上架
        product.setProductStatus(CheckStatus.APPROVED.getCode() == dto.getCheckStatus() ? 1 : 0);


        // 保证金准入：审核通过会自动上架，店铺欠缴被限制时禁止（阻断，不吞异常）
        if (CheckStatus.APPROVED.getCode() == dto.getCheckStatus()
                && product.getShopId() != null && product.getShopId() > 0) {
            depositLifecycleService.assertOnShelfAllowed(
                    com.tigshop.bean.enums.deposit.DepositOwnerType.SHOP.getCode(),
                    product.getShopId());
        }

        this.updateById(product);

        // 发送审核通知消息
        String isPass = CheckStatus.APPROVED.getCode() == dto.getCheckStatus() ? "通过" : "不通过";
        String checkReason = CheckStatus.APPROVED.getCode() == dto.getCheckStatus() ? "" : ",未通过的原因为：" + dto.getCheckReason();
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("productId", product.getProductId());
        AdminMsg adminMsg = AdminMsg.builder()
                .msgType(AdminMsgTypeEnum.PRODUCT_AUDIT_NOTIFICATION.getCatId())
                .title("商品审核通知：" + product.getProductName())
                .shopId(product.getShopId())
                .productId(product.getProductId())
                .content("商品" + product.getProductName() + "审核结果为：" + isPass + checkReason)
                .relatedData(JSON.toJSONString(relatedData))
                .sendTime(StringUtils.getCurrentTime())
                .build();
        adminMsgService.save(adminMsg);

        // 审核通过 -- 更新商品分组
        if (CheckStatus.APPROVED.getCode() == dto.getCheckStatus()) {
            List<ProductGroup> productGroups = productGroupService.lambdaQuery()
                    .eq(ProductGroup::getShopId, product.getShopId())
                    .in(ProductGroup::getProductGroupName, "新品", "热卖")
                    .list();

            if (CollUtil.isNotEmpty(productGroups)) {
                productGroups.forEach(group -> {
                    List<Integer> productIds = StringUtils.str2IntList(group.getProductIds());
                    if ("新品".equals(group.getProductGroupName()) && product.getIsNew() == 1) {
                        productIds.add(product.getProductId());
                    }
                    if ("热卖".equals(group.getProductGroupName()) && product.getIsHot() == 1) {
                        productIds.add(product.getProductId());
                    }
                    group.setProductIds(productIds.toString());
                });
                productGroupService.updateBatchById(productGroups);
            }
        }

        // 保证金收取节点：审核通过自动上架触发收取缓冲（仅店铺商品，非阻断）
        if (CheckStatus.APPROVED.getCode() == dto.getCheckStatus()
                && product.getShopId() != null && product.getShopId() > 0) {
            try {
                depositLifecycleService.onCollectTrigger(
                        com.tigshop.bean.enums.deposit.DepositOwnerType.SHOP.getCode(),
                        product.getShopId(),
                        com.tigshop.bean.enums.deposit.DepositCollectNode.ON_SHELF.getCode());
            } catch (Exception ignore) {
            }
            // 保证金：审核通过自动上架导致经营类目变化，记录应缴调整（非阻断）
            try {
                depositLifecycleService.onCategoryChange(
                        com.tigshop.bean.enums.deposit.DepositOwnerType.SHOP.getCode(),
                        product.getShopId());
            } catch (Exception ignore) {
            }
        }

        // 审核会改变商品上下架状态，成功完成后同步最新商品文档到 ES。
        productEsSyncService.syncProductAsync(Long.valueOf(product.getProductId()));

        return true;
    }

    @Override
    public String createNewProductSn() {
        Product product = this.lambdaQuery().orderByDesc(Product::getProductId).last("LIMIT 1").one();

        int num = product != null ? product.getProductId() + 1 : 1;
        String snPrefix = configService.getConfigByCode(SettingsEnum.SN_PREFIX.getBizCode()).getBizVal();

        String leadingZeros = String.format("%07d", num).substring(0, 7 - java.lang.String.valueOf(num).length());

        return snPrefix + leadingZeros + num;
    }

    @Override
    public boolean auditAgain(Integer id) {
        Product product = this.getById(id);
        Assert.notNull(product, () -> new GlobalException("商品不存在"));
//        Assert.isTrue(CheckStatus.REJECTED.getCode() == product.getCheckStatus(), () -> new GlobalException("商品审核状态错误"));
        product.setCheckStatus(CheckStatus.PENDING.getCode());
        product.setCheckReason("");
        boolean isUpdated = this.updateById(product);
        if (isUpdated && getShopId() != null && getShopId() > 0) {
            notifyProductPendingAudit(product);
        }
        return isUpdated;
    }

    // 批量获取最终价库存信息
    @Override
    public Map<Integer, ProductAvailabilityVO> getBatchProductAvailability(String skuIds, Integer shopId) {
        if (StrUtil.isBlank(skuIds)) {
            return Collections.emptyMap();
        }
        String[] skuIdArray = skuIds.split(",");
        if (skuIdArray.length == 0) {
            return Collections.emptyMap();
        }
        List<String> skuIdList = Arrays.stream(skuIdArray).toList();
        Map<Integer, ProductAvailabilityVO> map = new HashMap<>();
        for (String skuId : skuIdList) {
            // 判断是否为数字
            if (StrUtil.isBlank(skuId) || !skuId.chars().allMatch(Character::isDigit)) {
                continue;
            }
            int parseIntSkuId = Integer.parseInt(skuId);
            // 获取商品sku详情
            ProductSkuDTO productSkuServiceDetail = productSkuService.getDetail(parseIntSkuId, null);
            if (productSkuServiceDetail == null) {
                continue;
            }
            ProductAvailabilityVO productSkuDetail = this.getProductSkuDetail(productSkuServiceDetail.getProductId(), parseIntSkuId, 0, null, shopId, null);
            map.put(parseIntSkuId, productSkuDetail);
        }
        return map;
    }

    @Override
    public Page<ProductListResDTO> getHistoryProduct(BasePage basePage) {
        List<Integer> productIds = orderMapper.getHistoryProduct(getCurrentUserId());
        if (ObjectUtil.isEmpty(productIds)) {
            return new Page<>();
        }
        // 分页
        Page<Product> page = new Page<>(basePage.getPage(), basePage.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Product::getProductId, productIds);

        // W6 Wave 3.5：VO 化（避免污染 Product 实体）。ProductListResDTO 是 C 端商品列表标准响应，
        // 经 BeanUtils.copyProperties 由 Product 复制同名列表展示字段（CR-20260603-141000 / CD-032）。
        Page<Product> productPage = this.page(page, queryWrapper);
        List<ProductListResDTO> voList = productPage.getRecords().stream()
                .map(p -> {
                    ProductListResDTO vo = new ProductListResDTO();
                    BeanUtils.copyProperties(p, vo);
                    return vo;
                })
                .collect(Collectors.toList());
        backfillFirstWholesalePriceForList(voList);
        return PageUtil.convertPage(productPage, voList);
    }

    @Override
    public Page<SalesmanProductListVO> salesmanProducts(SalesmanProductPageQuery pageQuery) {
        Page<SalesmanProductListVO> page = buildSortOrder(pageQuery);

        pageQuery.setShopId(HeaderUtils.getShopId());

        page.orders().forEach(orderItem -> orderItem.setColumn("p." + orderItem.getColumn()));

        return this.baseMapper.salesmanProducts(page, pageQuery);
    }


    @Override
    public DeliveryOptionVO getDeliveryOption(List<Cart> carts, BigDecimal latitude, BigDecimal longitude, Integer deliveryType) {
        return getDeliveryOption(carts, latitude, longitude, deliveryType, null);
    }

    @Override
    public DeliveryOptionVO getDeliveryOption(List<Cart> carts, BigDecimal latitude, BigDecimal longitude, Integer deliveryType, UserAddressVO selectedAddress) {
        if (CollectionUtils.isEmpty(carts)) {
            return new DeliveryOptionVO();
        }
        List<Integer> productIds = carts.stream().map(Cart::getProductId).toList();

        Assert.notEmpty(productIds, () -> new GlobalException("商品不能为空"));
        List<Product> deliveryProductList = new ArrayList<>();
        List<Product> shopDeliveryProductList = new ArrayList<>();
        Map<Integer, DeliveryOptionVO.PickupShopInfo> pickupMap = new HashMap<>();
        List<DeliveryOptionVO.ShopDeliveryData> shopDeliveryDataList = new ArrayList<>();
        List<Product> noShippingProductList = new ArrayList<>();
        for (Cart cartProduct : carts) {
            Product product = this.getById(cartProduct.getProductId());
            if (product == null) {
                continue;
            }

            int productShopId = cartProduct.getShopId();

            boolean canPickup = product.getIsShopPickup() != null && product.getIsShopPickup() == 1;
            boolean canDelivery = product.getIsLogistics() != null && product.getIsLogistics() == 1;
            boolean isNoShipping = product.getNoShipping() != null && product.getNoShipping() == 1;
            boolean isShopDelivery = product.getIsShopDelivery() != null && product.getIsShopDelivery() == 1;

            //用mapper查询表shop_local_delivery_config的该店铺的enable是否开启，未开启则不支持门店配送
            //门店运力二选一：切到第三方运力时自建 enabled 会被置 0，此时同城配送仍然可用
            ShopLocalDeliveryConfig shopLocalDeliveryConfig = shopLocalDeliveryConfigMapper.selectOne(Wrappers.lambdaQuery(ShopLocalDeliveryConfig.class).eq(ShopLocalDeliveryConfig::getShopId, productShopId));
            boolean thirdPartyCarrierEnabled = false;
            if (shopLocalDeliveryConfig == null
                    || (shopLocalDeliveryConfig.getEnabled() != 1 && !thirdPartyCarrierEnabled)) {
                isShopDelivery = false;
            } else {
                if (shopLocalDeliveryConfig.getDistancePricingEnabled() == 0) {
                    UserAddressVO addressForRange = selectedAddress != null ? selectedAddress : userAddressService.getUserSelectedAddress();
                    //计算收货地址与门店或平台门店的距离
                    BigDecimal distance = BigDecimal.ZERO;
                    if (addressForRange != null && addressForRange.getLongitude() != null && addressForRange.getLatitude() != null && productShopId > 0) {
                        // 获取店铺信息
                        Shop shop = shopMapper.selectById(productShopId);
                        if (shop != null && shop.getShopLongitude() != null && shop.getShopLatitude() != null) {
                            distance = GpsDistanceUtil.getDistance(
                                    addressForRange.getLongitude(),
                                    addressForRange.getLatitude(),
                                    shop.getShopLongitude(),
                                    shop.getShopLatitude()
                            );
                        }
                    } else if (addressForRange != null && addressForRange.getLongitude() != null && addressForRange.getLatitude() != null) {
                        // 获取平台自营门店信息
                        GetStoreSelfOperatedVO storeSelfOperated = configService.getStoreSelfOperatedSettings();
                        if (storeSelfOperated == null || Objects.equals(storeSelfOperated.getStoreSelfOperatedLatitude(), "") || storeSelfOperated.getStoreSelfOperatedLatitude() == null || storeSelfOperated.getStoreSelfOperatedLongitude() == null || Objects.equals(storeSelfOperated.getStoreSelfOperatedLongitude(), "")) {

                        } else {
                            distance = GpsDistanceUtil.getDistance(
                                    addressForRange.getLongitude(),
                                    addressForRange.getLatitude(),
                                    new BigDecimal(storeSelfOperated.getStoreSelfOperatedLongitude()),
                                    new BigDecimal(storeSelfOperated.getStoreSelfOperatedLatitude())
                            );
                        }

                    }

                    boolean outOfRange = distance.compareTo(shopLocalDeliveryConfig.getDeliveryRadius()) > 0;
                    DeliveryOptionVO.ShopDeliveryData shopDeliveryData = new DeliveryOptionVO.ShopDeliveryData();
                    shopDeliveryData.setDistance(distance);
                    shopDeliveryData.setShopId(productShopId);
                    shopDeliveryData.setIsOutOfRange(outOfRange); // 一定要将最新的 outOfRange 更新到 map 里的对象
                    shopDeliveryDataList.add(shopDeliveryData); // 确保更新后的对象写回 map
                }

            }

            //用mapper查询自提是否开启
            ShopPickupConfig shopPickupConfig = shopPickupConfigMapper.selectOne(Wrappers.lambdaQuery(ShopPickupConfig.class).eq(ShopPickupConfig::getShopId, productShopId));
            if (shopPickupConfig == null || shopPickupConfig.getStatus() != 1) {
                canPickup = false;
            }

            if (isNoShipping && product.getProductType() != 1) {
                noShippingProductList.add(product);
                break;
            }

            if (isShopDelivery) {
                shopDeliveryProductList.add(product);
            }

            if (canDelivery) {
                deliveryProductList.add(product);
            }

            if (canPickup) {
                // 获取自提点，并根据经纬度查询
                List<Shop> shops = new ArrayList<>();
                if (ObjectUtil.isNull(longitude) || ObjectUtil.isNull(latitude)) {
                    shops = shopMapper.selectList(Wrappers.lambdaQuery(Shop.class).eq(Shop::getStoreParentId, productShopId).eq(Shop::getStatus, 1));
                } else {
                    PickupPageQuery query = new PickupPageQuery();
                    query.setShopId(productShopId);
                    query.setLongitude(longitude);
                    query.setLatitude(latitude);
                    shops = shopMapper.pickupByLocation(query);
                }
                if (ObjectUtil.isNotEmpty(shops)) {
                    // 自提点不为空时添加
                    DeliveryOptionVO.PickupShopInfo info = pickupMap.computeIfAbsent(
                            productShopId, k -> new DeliveryOptionVO.PickupShopInfo(productShopId, new HashSet<>(), new ArrayList<>()));
                    info.getPickups().add(shops.getFirst());
                    info.getProducts().add(product);
                }
            }


        }


        DeliveryOptionVO result = new DeliveryOptionVO();

        if (CollUtil.isNotEmpty(noShippingProductList)) {
            result.setDeliveryType(ShopDeliveryTypeEnum.DELIVERY.getCode());
            result.setDeliveryProducts(noShippingProductList);
            return result;
        }

        // 总商品数量
        int total = carts.size();
        // 配送的商品数量
        int deliveryCount = deliveryProductList.size();
        //门店配送商品数量
        int shopDeliveryCount = shopDeliveryProductList.size();
        // 自提的商品数量
        int pickupCount = pickupMap.values().stream().mapToInt(i -> i.getProducts().size()).sum();

        if (pickupCount == total && deliveryCount == total && shopDeliveryCount == total) {
            result.setDeliveryType(ShopDeliveryTypeEnum.ALL_CHOOSE.getCode());
        } else if (pickupCount == 0 && deliveryCount == total && shopDeliveryCount == total) {
            result.setDeliveryType(ShopDeliveryTypeEnum.ALL_CHOOSE.getCode());
        } else if (pickupCount == total && deliveryCount == 0 && shopDeliveryCount == total) {
            result.setDeliveryType(ShopDeliveryTypeEnum.ALL_CHOOSE.getCode());
        } else if (pickupCount == total && deliveryCount == total && shopDeliveryCount == 0) {
            result.setDeliveryType(ShopDeliveryTypeEnum.ALL_CHOOSE.getCode());
        } else if (pickupCount == total && deliveryCount == 0 && shopDeliveryCount == 0) {
            result.setDeliveryType(ShopDeliveryTypeEnum.SELF_PICK_UP.getCode());
        } else if (shopDeliveryCount == total && pickupCount == 0 && deliveryCount == 0) {
            result.setDeliveryType(ShopDeliveryTypeEnum.CITY.getCode());
        } else if (deliveryCount == total && pickupCount == 0 && shopDeliveryCount == 0) {
            result.setDeliveryType(ShopDeliveryTypeEnum.DELIVERY.getCode());
        } else {
            result.setDeliveryType(ShopDeliveryTypeEnum.PART_CHOOSE.getCode());
        }
        result.setShopDeliveryData(shopDeliveryDataList);
        if (ObjectUtil.isNotNull(deliveryType)) {
            result.setDeliveryProducts(List.of());
            result.setShopDeliveryProducts(List.of());
            result.setPickupProducts(List.of());
            if (ObjectUtil.equals(deliveryType, OrderDeliveryTypeEnum.LOGISTICS.getCode())) {
                result.setDeliveryProducts(deliveryProductList);
            } else if (ObjectUtil.equals(deliveryType, OrderDeliveryTypeEnum.PICKUP.getCode())) {
                result.setPickupProducts(pickupCount > 0 ? new ArrayList<>(pickupMap.values()) : List.of());
            } else if (ObjectUtil.equals(deliveryType, OrderDeliveryTypeEnum.SAME_CITY.getCode())) {
                result.setShopDeliveryProducts(shopDeliveryProductList);
            }
            return result;
        }

        result.setDeliveryProducts(deliveryProductList);
        result.setShopDeliveryProducts(shopDeliveryProductList);

        result.setPickupProducts(pickupCount > 0 ? new ArrayList<>(pickupMap.values()) : List.of());
        return result;
    }

    @Override
    public DeliveryOptionVO getDeliveryOption(Integer shopId, BigDecimal latitude, BigDecimal longitude, Integer deliveryType) {
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        List<Cart> carts = cartMapper.selectList(Wrappers.lambdaQuery(Cart.class)
                .eq(Cart::getUserId, currentUserId)
                .eq(Cart::getIsChecked, 1)
                .eq(shopId != null, Cart::getShopId, shopId));

        return getDeliveryOption(carts, latitude, longitude, deliveryType);
    }


    @Override
    public Page<Product> productList(GrouponProductPageQuery pageQuery) {
        Page<Product> page = buildSortOrder(pageQuery);

        Integer currentShopId = Optional.ofNullable(getShopId()).orElse(0);

        MPJLambdaWrapper<Product> wrapper = null;
        if (tigshopProperties.getIsO2o() == 1 && currentShopId > 0) {
            // 平台分配(product表shop_id=0,并且store_product表的shop_id是currentShopId)
            // 或者 (product表的shop_id是currentShopId)
            wrapper = new MPJLambdaWrapper<Product>()
                    .selectAll(Product.class)
                    .leftJoin(StoreProduct.class, StoreProduct::getProductId, Product::getProductId)
                    // 核心条件：(平台商品且已分配给当前店铺) OR (店铺自己的商品)
                    .and(w -> w
                            .nested(n -> n.eq(Product::getShopId, 0).eq(StoreProduct::getShopId, currentShopId).eq(StoreProduct::getProductStatus, YES))
                            .or()
                            .eq(Product::getShopId, currentShopId).eq(Product::getProductStatus, YES)
                    )
                    // 筛选普通商品：deliveryMode=1 或 deliveryMode=0（排除周期购商品 deliveryMode=2）
                    .and(w -> w.eq(Product::getDeliveryMode, 1).or().eq(Product::getDeliveryMode, 0))
                    .like(StrUtil.isNotBlank(pageQuery.getKeyword()), Product::getProductName, pageQuery.getKeyword())
                    .like(pageQuery.getBrandId() != null, Product::getBrandId, pageQuery.getBrandId())
                    .orderByAsc(Product::getSortOrder);
        } else {
            wrapper = new MPJLambdaWrapper<Product>()
                    .selectAll(Product.class)
                    .eq(Product::getShopId, currentShopId)
                    // 筛选普通商品：deliveryMode=1 或 deliveryMode为null（排除周期购商品 deliveryMode=2）
                    .and(w -> w.eq(Product::getDeliveryMode, 1).or().eq(Product::getDeliveryMode, 0))
                    .eq(Product::getProductStatus, YES)
                    .like(StrUtil.isNotBlank(pageQuery.getKeyword()), Product::getProductName, pageQuery.getKeyword())
                    .like(pageQuery.getBrandId() != null, Product::getBrandId, pageQuery.getBrandId())
                    .orderByAsc(Product::getSortOrder);
        }

        return baseMapper.selectJoinPage(page, Product.class, wrapper);
    }

    @Override
    public ProductPromoteVO getProductPromote(Integer productId) {
        ProductDetailDTO detail = this.detail(productId);
        Assert.notNull(detail, () -> new GlobalException("商品不存在"));

        String productName = detail.getProductName();
        BigDecimal productPrice = detail.getProductPrice();
        String picUrl = detail.getPicUrl();
        String productBrief = detail.getProductBrief();
        String productSn = detail.getProductSn();
        Integer shopId = detail.getShopId();

        String shopName = "商城自营";
        if (shopId != null && shopId > 0) {
            Shop shop = shopMapper.selectById(shopId);
            shopName = shop == null ? shopName : shop.getShopTitle();
        }

        String wechatShortLink = null;
        try {
            wechatShortLink = getWechatShortLinkWithError(productId);
        } catch (Exception e) {
            log.warn("[getProductPromote] 微信短链生成失败：{}", e.getMessage());
        }

        return ProductPromoteVO.builder()
                .productName(productName)
                .productPrice(productPrice)
                .picUrl(picUrl)
                .productBrief(productBrief)
                .productSn(productSn)
                .shopName(shopName)
                .shortLink(getWebShortLink(productId))
                .wechatShortLink(wechatShortLink)
                .build();
    }

    @Override
    public ProductPostVO post(Integer productId) {
        ProductDetailDTO detail = this.detail(productId);
        Assert.notNull(detail, () -> new GlobalException("商品不存在"));

        String productName = detail.getProductName();
        BigDecimal productPrice = detail.getProductPrice();
        String picLarge = detail.getPicLarge();
        String productBrief = detail.getProductBrief();
        String productSn = detail.getProductSn();
        Integer shopId = detail.getShopId();

        String shopName = "商城自营";
        if (shopId != null && shopId > 0) {
            Shop shop = shopMapper.selectById(shopId);
            shopName = shop == null ? shopName : shop.getShopTitle();
        }

        String h5Domain = configService.getConfigVal(SettingsEnum.H5_DOMAIN);
        String pcDomain = configService.getConfigVal(SettingsEnum.PC_DOMAIN);

        String h5Path = "/pages/product/index?id=";
        String pcOriginalLink = pcDomain + "/product/" + productSn;

        Integer promoteShopId = shopId == null ? 0 : shopId;

        String wechatShortLink = null;
        String wechatShortLinkError = null;
        try {
            wechatShortLink = getWechatShortLinkWithError(productId, promoteShopId);
        } catch (Exception e) {
            wechatShortLinkError = e.getMessage();
        }

        String miniCode = "";
        try {
            miniCode = wechatOAuthService.getMiniCode("/pages/product/index?id=", productId, null, promoteShopId);
        } catch (Exception e) {
            log.warn("[post] 小程序码生成失败：{}", e.getMessage());
        }

        return ProductPostVO.builder()
                .productName(productName)
                .productPrice(productPrice)
                .picUrl(picLarge)
                .productBrief(productBrief)
                .productSn(productSn)
                .shopName(shopName)
                .h5ShortLink(h5Domain + shortUrlService.getWebShortLink(h5Path, productId, shareTypeEnum.PRODUCT.getKey()))
                .h5OriginalLink(h5Domain + h5Path + productId)
                .pcShortLink(pcDomain + shortUrlService.getPcWebShortLink("/product/", productSn, shareTypeEnum.PRODUCT.getKey()))
                .pcOriginalLink(pcOriginalLink)
                .miniCode(miniCode)
                .wechatShortLink(wechatShortLink)
                .wechatShortLinkError(wechatShortLinkError)
                .build();
    }


    private String getWechatShortLinkWithError(Integer productId) {
        return getWechatShortLinkWithError(productId, null);
    }

    private String getWechatShortLinkWithError(Integer productId, Integer shopId) {
        String appId = configService.getConfigVal(SettingsEnum.WECHAT_MINI_PROGRAM_APP_ID);
        String appSecret = configService.getConfigVal(SettingsEnum.WECHAT_MINI_PROGRAM_SECRET);
        if (StrUtil.isBlank(appId) || StrUtil.isBlank(appSecret)) {
            throw new GlobalException("请先配置微信小程序AppID和AppSecret");
        }
        GetTokenResult tokenResult = wechatApiClient.getToken("client_credential", appId, appSecret);
        if (tokenResult == null || StrUtil.isBlank(tokenResult.getAccessToken())) {
            throw new GlobalException("获取微信access_token失败");
        }
        String pageUrl = StrUtil.format("pages/product/index?id={}", productId);
        if (shopId != null && shopId > 0) {
            pageUrl = pageUrl + StrUtil.format("&shopId={}", shopId);
        }
        ShortLinkParam param = ShortLinkParam.builder()
                .pageTitle("商品详情")
                .pageUrl(pageUrl)
                .build();
        ShortLinkResult result = wechatApiClient.genwxashortlink(tokenResult.getAccessToken(), param);
        if (result != null && result.getErrcode() == 0) {
            return result.getLink();
        }
        String errMsg = result != null ? StrUtil.format("微信短链生成失败[{}]：{}", result.getErrcode(), result.getErrmsg()) : "微信短链生成失败：响应为空";
        throw new GlobalException(errMsg);
    }

    // 获取网页短链
    private String getWebShortLink(Integer productId) {
        String baseUrl = StrUtil.format("/pages/product/index?id={}", productId);
        String encode = StrUtil.format("/s/{}", Base62.encode(productId.toString()));
        Long count = shortUrlMapper.selectCount(Wrappers.lambdaQuery(ShortUrl.class).eq(ShortUrl::getShortCode, encode));
        if (count > 0) {
            return encode;
        }
        ShortUrl shortUrl = ShortUrl.builder()
                .longUrl(baseUrl)
                .shortCode(encode)
                .addTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"))
                .build();
        shortUrlMapper.insert(shortUrl);
        return encode;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAutoStatusTasks(Product product, String autoOnlineTime, String autoOfflineTime) {
        if (product == null) {
            return;
        }

        Integer productId = product.getProductId();

        //根据商品状态来确定采用上架时间还是下架时间
        Product finalyProduct = this.lambdaQuery().eq(Product::getProductId, productId)
                .select(Product::getProductStatus, Product::getShopId, Product::getProductId).one();

        Integer targetShopId;
        if (product.getShopId() != getShopId()) {
            //admin查看门店的商品/店铺的所属的商品
            targetShopId = finalyProduct.getShopId();
        } else {
            targetShopId = getShopId();
        }

        //两个时间都为空：视为取消定时上下架，清除已有任务后直接返回
        if (StrUtil.isBlank(autoOnlineTime) && StrUtil.isBlank(autoOfflineTime)) {
            productAutoStatusService.cancelProductTasks(finalyProduct.getProductId(), targetShopId);
            return;
        }

        Integer productStatus = finalyProduct.getProductStatus();
        if (productStatus == 0) {
            //下架,说明要自动上架
            if (StrUtil.isBlank(autoOnlineTime)) {
                throw new GlobalException(messageHelper.get("error.product.on.sale.time.required"));
            }
            autoOfflineTime = null;
        } else {
            //上架,说明要自动下架
            if (StrUtil.isBlank(autoOfflineTime)) {
                throw new GlobalException(messageHelper.get("error.product.off.sale.time.required"));
            }
            autoOnlineTime = null;
        }

        long currentTime = DateUtil.currentSeconds();

        //清除掉之前的任务
        productAutoStatusService.cancelProductTasks(finalyProduct.getProductId(), targetShopId);

        // 创建自动上架任务
        if (StrUtil.isNotBlank(autoOnlineTime)) {
            long onlineTimestamp = TigUtils.toTimestampYmdHms(autoOnlineTime);
            if (onlineTimestamp > currentTime) {
                ProductAutoStatusCreateDTO onlineDTO = ProductAutoStatusCreateDTO.builder()
                        .productId(product.getProductId())
                        .shopId(targetShopId)
                        .operationType(ProductOperationTypeEnum.ONLINE.getCode())
                        .scheduledTime(onlineTimestamp)
                        .build();
                productAutoStatusService.create(onlineDTO);
            }
        }

        // 创建自动下架任务
        if (StrUtil.isNotBlank(autoOfflineTime)) {
            long offlineTimestamp = TigUtils.toTimestampYmdHms(autoOfflineTime);
            if (offlineTimestamp > currentTime) {
                ProductAutoStatusCreateDTO offlineDTO = ProductAutoStatusCreateDTO.builder()
                        .productId(product.getProductId())
                        .shopId(targetShopId)
                        .operationType(ProductOperationTypeEnum.OFFLINE.getCode())
                        .scheduledTime(offlineTimestamp)
                        .build();
                productAutoStatusService.create(offlineDTO);
            }
        }
    }

    @Override
    public Page<ProductSkuVO> selectProductList(ProductSkuPageQuery query) {
        Page<ProductSkuVO> page = buildSortOrder(query);
        // 如果传入了分类ID，查询其所有子分类ID（包括自身），用于IN条件查询
        List<Integer> categoryIds = null;
        if (query.getCategoryId() != null) {
            categoryIds = categoryService.getCategoryAllChildIds(query.getCategoryId());
        }
        Page<ProductSkuVO> result = this.baseMapper.selectProductList(page, query.getKeyword(), categoryIds, query.getShopId(),
                query.getProductType(), query.getIsSaleMode(), query.getIsIncludeCycleProduct(), query.getExcludeProductTypes());
        if (result != null && CollUtil.isNotEmpty(result.getRecords())) {
            for (ProductSkuVO vo : result.getRecords()) {
                if (vo == null) {
                    continue;
                }
                if (StrUtil.isNotBlank(vo.getProductName())) {
                    vo.setProductName(translatePackage.translate(vo.getProductName(), DataType.PRODUCT.getCode()));
                }
                if (StrUtil.isNotBlank(vo.getSkuData())) {
                    vo.setSkuData(SkuValueTranslateUtils.translateSkuDataJson(vo.getSkuData(), translatePackage));
                }
                if (StrUtil.isNotBlank(vo.getPicUrl())) {
                    vo.setPicUrl(translatePackage.translate(vo.getPicUrl(), DataType.IMAGE_TITLE.getCode()));
                }
            }
        }
        return result;
    }

    @Override
    public boolean saveShareInfo(com.tigshop.bean.dto.product.ProductShareInfoDTO dto) {
        Product product = this.getById(dto.getProductId());
        Assert.notNull(product, () -> new GlobalException("商品不存在"));
        cn.hutool.json.JSONObject json = new cn.hutool.json.JSONObject();
        json.set("productName", dto.getProductName());
        json.set("productPrice", dto.getProductPrice());
        json.set("picUrl", dto.getPicUrl());
        product.setShareInfo(json.toString());
        return this.updateById(product);
    }

    @Override
    public com.tigshop.bean.vo.product.ProductShareInfoVO getShareInfo(Integer productId) {
        Product product = this.getById(productId);
        Assert.notNull(product, () -> new GlobalException("商品不存在"));
        com.tigshop.bean.vo.product.ProductShareInfoVO vo = com.tigshop.bean.vo.product.ProductShareInfoVO.builder()
                .productId(productId)
                .productName(product.getProductName())
                .productPrice(product.getProductPrice())
                .picUrl(product.getPicUrl())
                .build();
        if (StrUtil.isNotBlank(product.getShareInfo())) {
            cn.hutool.json.JSONObject json = cn.hutool.json.JSONUtil.parseObj(product.getShareInfo());
            vo.setProductName(json.getStr("productName", product.getProductName()));
            vo.setProductPrice(json.getBigDecimal("productPrice") != null ? json.getBigDecimal("productPrice") : product.getProductPrice());
            vo.setPicUrl(json.getStr("picUrl", product.getPicUrl()));
        }
        return vo;
    }

    /**
     * 公共 hook：供其他 C 端商品列表 service（search / 等）复用「平台商品首条批发价」回填能力
     * （CR-20260603-141000 / CD-032 列表覆盖范围扩展）。
     *
     * <p>仅做空值守卫后委托给 {@link #backfillFirstWholesalePrice(List, Integer)}，批发商判定/
     * 单次批量 IN 查询逻辑完全复用，不新增独立分支（CD-017 性能基线不变）。
     *
     * @param voList 商品列表（null 或空为安全空操作，不抛异常）
     */
    @Override
    public void backfillFirstWholesalePriceForList(List<ProductListResDTO> voList) {
        if (CollUtil.isEmpty(voList)) {
            return;
        }
        // 历史购买等无门店上下文的列表：传 null 不做归属过滤（行为不变）
        backfillFirstWholesalePrice(voList, null);
    }

    /**
     * C 端商品列表 backfill：批发商身份命中商品时填充 {@code firstWholesalePrice}（TASK-WS-209 / CD-021）。
     *
     * <p>非批发商身份直接返回（不发 DB 查询）；批发商身份发 1 个批量 SQL 预取每个商品的「首条」批发价
     * （{@code id ASC = admin 录入顺序首条}），命中商品 set 字段，未命中商品保留 null。
     *
     * @param voList 商品列表（调用方保证非空；本方法不会修改 list 大小）
     */
    private void backfillFirstWholesalePrice(List<ProductListResDTO> voList, Integer contextShopId) {
        if (CollUtil.isEmpty(voList)) {
            return;
        }
        // CR-20260610-162600 / CD-039：店铺商品批发已放开（原 CD-031 平台过滤移除），
        // 平台 + 店铺商品统一按 productId 解析批发配置；仅保留判空守卫。
        List<ProductListResDTO> candidateVoList = voList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (candidateVoList.isEmpty()) {
            return;
        }
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return;
        }
        if (!wholesalerService.isWholesaleBuyer(userId)) {
            return;
        }
        Integer gradeId = wholesalerService.getGradeIdByUserId(userId);
        if (gradeId == null || gradeId <= 0) {
            return;
        }
        // 收集 productIds + retailPriceMap（折扣模式所需）
        List<Integer> productIds = new ArrayList<>(candidateVoList.size());
        Map<Integer, BigDecimal> retailPriceMap = new HashMap<>(candidateVoList.size() * 2);
        for (ProductListResDTO vo : candidateVoList) {
            if (vo.getProductId() == null) {
                continue;
            }
            productIds.add(vo.getProductId());
            if (vo.getProductPrice() != null) {
                retailPriceMap.put(vo.getProductId(), vo.getProductPrice());
            }
        }
        if (productIds.isEmpty()) {
            return;
        }
        // 单 SQL 批量预取（禁 N+1，CD-021）
        Map<Integer, BigDecimal> firstPriceMap =
                wholesalePriceService.resolveFirstWholesalePriceMap(productIds, gradeId, retailPriceMap);
        if (firstPriceMap == null || firstPriceMap.isEmpty()) {
            return;
        }
        // CR-20260603-1720 / CD-035：仅对 firstPrice 命中商品派生卡片起批数 + 维度（1 IN config）
        // CR-20260613-0943：同一份 config 查询额外取 moqDimension；再 +1 IN 取 SPU 兜底倍数
        List<Integer> hitProductIds = new ArrayList<>(firstPriceMap.keySet());
        Map<Integer, com.tigshop.bean.model.wholesale.ProductWholesaleConfig> cardConfigMap =
                wholesalePriceService.resolveCardConfigMap(hitProductIds);
        if (cardConfigMap == null) {
            cardConfigMap = Collections.emptyMap();
        }
        Map<Integer, Integer> multipleMap =
                wholesalePriceService.resolveSpuMultipleMap(hitProductIds);
        if (multipleMap == null) {
            multipleMap = Collections.emptyMap();
        }
        for (ProductListResDTO vo : candidateVoList) {
            if (vo.getProductId() == null) {
                continue;
            }
            com.tigshop.bean.model.wholesale.ProductWholesaleConfig cfg = cardConfigMap.get(vo.getProductId());
            // O2O 渠道：商品归属与当前门店上下文不一致（平台分配商品）→ 卡片不回填批发字段
            if (cfg != null && !com.tigshop.service.wholesale.WholesaleBuyRuleValidator
                    .isClientEligible(cfg, isO2oWholesaleChannel(), contextShopId)) {
                continue;
            }
            BigDecimal first = firstPriceMap.get(vo.getProductId());
            if (first != null) {
                vo.setFirstWholesalePrice(first);
            }
            if (cfg != null) {
                vo.setWholesaleCardMoq(cfg.getMoq());
                vo.setWholesaleMoqDimension(cfg.getMoqDimension());
            }
            Integer multiple = multipleMap.get(vo.getProductId());
            if (multiple != null) {
                vo.setWholesaleMultiple(multiple);
            }
        }
    }

    /**
     * 通用列表 backfill：与 {@link #backfillFirstWholesalePrice(List, Integer)} 同构（批发商判定 +
     * 单次批量 IN 查询），但通过 getter/setter 适配任意 VO 类型（W6 Wave 3.5 / CD-032）。
     *
     * <p>不携带零售价，{@code retailPriceMap} 传空：折扣模式行不计算（返回 null），仅固定价模式回填。
     * CD-017 性能基线不变：每次调用至多 1 次批量 IN 查询，非批发商/无平台商品直接短路返回。
     */
    @Override
    public <T> void backfillFirstWholesalePriceForListGeneric(
            List<T> voList,
            java.util.function.Function<T, Integer> shopIdGetter,
            java.util.function.Function<T, Integer> productIdGetter,
            java.util.function.BiConsumer<T, BigDecimal> setter) {
        // 向后兼容：委托新 5 参数版，moqSetter 传 null（不回填 wholesaleCardMoq）
        backfillFirstWholesalePriceForListGeneric(voList, shopIdGetter, productIdGetter, setter, null);
    }

    /**
     * 通用列表 backfill（5 参数版，CR-20260603-1720 / CD-035）：在 firstWholesalePrice 之外
     * 同步回填 wholesaleCardMoq（仅 priceSetter 命中商品集合上派生规则后批量回填）。
     *
     * <p>CR-20260613-0943 起委托 7 参数版，dimensionSetter / multipleSetter 传 null
     * （不回填维度/倍数），行为与本版历史语义完全一致。
     */
    @Override
    public <T> void backfillFirstWholesalePriceForListGeneric(
            List<T> voList,
            java.util.function.Function<T, Integer> shopIdGetter,
            java.util.function.Function<T, Integer> productIdGetter,
            java.util.function.BiConsumer<T, BigDecimal> priceSetter,
            java.util.function.BiConsumer<T, Integer> moqSetter) {
        backfillFirstWholesalePriceForListGeneric(
                voList, shopIdGetter, productIdGetter, priceSetter, moqSetter, null, null);
    }

    /**
     * 通用列表 backfill（7 参数版，CR-20260613-0943）：在 firstWholesalePrice / wholesaleCardMoq
     * 之外，同步回填 wholesaleMoqDimension（起批维度）与 wholesaleMultiple（SPU 兜底 sku_id=0 倍数）。
     *
     * <p>性能基线：维度复用 moq 的同一份 config 查询（0 额外 SQL）；multipleSetter 非 null 时
     * +1 次 sku_config IN（仅 firstPriceMap 非空时触发）。各 setter 传 null 时对应字段不回填。
     */
    @Override
    public <T> void backfillFirstWholesalePriceForListGeneric(
            List<T> voList,
            java.util.function.Function<T, Integer> shopIdGetter,
            java.util.function.Function<T, Integer> productIdGetter,
            java.util.function.BiConsumer<T, BigDecimal> priceSetter,
            java.util.function.BiConsumer<T, Integer> moqSetter,
            java.util.function.BiConsumer<T, Integer> dimensionSetter,
            java.util.function.BiConsumer<T, Integer> multipleSetter) {
        // 向后兼容：委托 8 参版，retailPriceMap 传空（折扣模式行不计算，仅固定价模式回填）
        backfillFirstWholesalePriceForListGeneric(voList, shopIdGetter, productIdGetter, priceSetter,
                moqSetter, dimensionSetter, multipleSetter, Collections.emptyMap());
    }

    @Override
    public <T> void backfillFirstWholesalePriceForListGeneric(
            List<T> voList,
            java.util.function.Function<T, Integer> shopIdGetter,
            java.util.function.Function<T, Integer> productIdGetter,
            java.util.function.BiConsumer<T, BigDecimal> priceSetter,
            java.util.function.BiConsumer<T, Integer> moqSetter,
            java.util.function.BiConsumer<T, Integer> dimensionSetter,
            java.util.function.BiConsumer<T, Integer> multipleSetter,
            Map<Integer, BigDecimal> retailPriceMap) {
        if (CollUtil.isEmpty(voList) || shopIdGetter == null || productIdGetter == null || priceSetter == null) {
            return;
        }
        // CR-20260610-162600 / CD-039：店铺商品批发已放开（原 CD-031 平台过滤移除）。
        // shopIdGetter 参数保留签名兼容（6 个调用方零改动），不再用于过滤。
        List<T> candidateVoList = voList.stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        if (candidateVoList.isEmpty()) {
            return;
        }
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return;
        }
        if (!wholesalerService.isWholesaleBuyer(userId)) {
            return;
        }
        Integer gradeId = wholesalerService.getGradeIdByUserId(userId);
        if (gradeId == null || gradeId <= 0) {
            return;
        }
        List<Integer> productIds = candidateVoList.stream()
                .map(productIdGetter)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (productIds.isEmpty()) {
            return;
        }
        // 单 SQL 批量预取（禁 N+1，CD-021）。CR-20260615-1017：传入 retailPriceMap（productId→零售价），
        // 折扣模式（price_mode=1）据此算价；为空时退化为原行为（折扣模式不计算）。
        Map<Integer, BigDecimal> safeRetailPriceMap = retailPriceMap != null ? retailPriceMap : Collections.emptyMap();
        Map<Integer, BigDecimal> firstPriceMap =
                wholesalePriceService.resolveFirstWholesalePriceMap(productIds, gradeId, safeRetailPriceMap);
        if (firstPriceMap == null || firstPriceMap.isEmpty()) {
            return;
        }
        // CR-20260603-1720 / CD-035 + CR-20260613-0943：moqSetter/dimensionSetter 任一非 null 时
        // 取命中集合的 config（1 IN，含 moq + moqDimension）。
        Map<Integer, com.tigshop.bean.model.wholesale.ProductWholesaleConfig> cardConfigMap;
        if (moqSetter != null || dimensionSetter != null) {
            Map<Integer, com.tigshop.bean.model.wholesale.ProductWholesaleConfig> resolved =
                    wholesalePriceService.resolveCardConfigMap(new ArrayList<>(firstPriceMap.keySet()));
            cardConfigMap = (resolved != null) ? resolved : Collections.emptyMap();
        } else {
            cardConfigMap = Collections.emptyMap();
        }
        // CR-20260613-0943：multipleSetter 非 null 时 +1 IN 取 SPU 兜底倍数。
        Map<Integer, Integer> multipleMap;
        if (multipleSetter != null) {
            Map<Integer, Integer> resolved =
                    wholesalePriceService.resolveSpuMultipleMap(new ArrayList<>(firstPriceMap.keySet()));
            multipleMap = (resolved != null) ? resolved : Collections.emptyMap();
        } else {
            multipleMap = Collections.emptyMap();
        }
        for (T vo : candidateVoList) {
            Integer pid = productIdGetter.apply(vo);
            if (pid == null) {
                continue;
            }
            BigDecimal first = firstPriceMap.get(pid);
            if (first != null) {
                priceSetter.accept(vo, first);
            }
            com.tigshop.bean.model.wholesale.ProductWholesaleConfig cfg = cardConfigMap.get(pid);
            if (cfg != null) {
                if (moqSetter != null && cfg.getMoq() != null) {
                    moqSetter.accept(vo, cfg.getMoq());
                }
                if (dimensionSetter != null && cfg.getMoqDimension() != null) {
                    dimensionSetter.accept(vo, cfg.getMoqDimension());
                }
            }
            if (multipleSetter != null) {
                Integer multiple = multipleMap.get(pid);
                if (multiple != null) {
                    multipleSetter.accept(vo, multiple);
                }
            }
        }
    }

    /**
     * C 端商品详情 backfill：批发商身份命中商品时填充 {@code wholesalePriceList}（TASK-WS-210 / CD-022）。
     *
     * <p>非批发商身份直接返回（字段保持 null）；批发商身份按当前等级过滤后返回阶梯
     * （CR-20260612-1659 修订 CD-022：仅 {@code grade_id ∈ {0(通用), 当前等级}}，与成交价 resolver 同口径），
     * 按 {@code price_id ASC} 排序，UI 高亮当前等级行。
     *
     * @param vo        商品详情 VO（调用方保证非空；本方法不修改 vo.item 等其他字段）
     * @param productId 商品 ID
     */
    private void backfillWholesalePriceList(ProductDetailVO vo, Integer productId, Integer contextShopId) {
        if (vo == null || productId == null || productId <= 0) {
            return;
        }
        // CR-20260610-162600 / CD-039：店铺商品批发已放开（原 CD-031 非平台 early return 移除），
        // 平台 + 店铺商品详情统一按 productId 返回批发阶梯价。
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return;
        }
        if (!wholesalerService.isWholesaleBuyer(userId)) {
            return;
        }
        // CR-20260612-1659：按当前批发商等级过滤阶梯；无有效等级（≤0）与成交价 resolver 同口径，返回空（字段保持 null）
        Integer gradeId = wholesalerService.getGradeIdByUserId(userId);
        if (gradeId == null || gradeId <= 0) {
            return;
        }
        // O2O 渠道：平台商品分配到门店后不参与批发，详情不展示阶梯价
        com.tigshop.bean.model.wholesale.ProductWholesaleConfig eligibleCfg =
                wholesaleProductConfigService.getActiveByProductId(productId);
        if (!com.tigshop.service.wholesale.WholesaleBuyRuleValidator.isClientEligible(
                eligibleCfg, isO2oWholesaleChannel(), contextShopId)) {
            return;
        }
        BigDecimal retailPrice = vo.getItem() != null ? vo.getItem().getProductPrice() : null;
        // 折扣模式阶梯按行所属 SKU 取价：避免多 SKU 商品把所有阶梯折扣价塌成商品级最低 SKU 价
        // （与下单算价 WholesalePriceResolver 基数=SKU 零售价对齐）。
        java.util.Map<Integer, BigDecimal> skuRetailPriceMap = new java.util.HashMap<>();
        List<ProductSkuDTO> wholesaleSkuList = productSkuService.getProductSkusByPid(productId);
        if (wholesaleSkuList != null) {
            for (ProductSkuDTO sku : wholesaleSkuList) {
                if (sku == null || sku.getSkuId() == null || sku.getSkuId() <= 0
                        || StrUtil.isBlank(sku.getSkuPrice())) {
                    continue;
                }
                try {
                    skuRetailPriceMap.put(sku.getSkuId(), new BigDecimal(sku.getSkuPrice().trim()));
                } catch (NumberFormatException ignore) {
                    // 脏价格数据跳过，对应行回退商品级 retailPrice
                }
            }
        }
        List<com.tigshop.bean.vo.wholesale.WholesalePriceTierVO> tierList =
                wholesalePriceService.buildWholesalePriceTierList(productId, retailPrice, skuRetailPriceMap, gradeId);
        if (CollUtil.isNotEmpty(tierList)) {
            vo.setWholesalePriceList(tierList);
            // CR-20260604-1430：返回多规格定价模式，前端据此区分混批(1)/SKU 独立(2)展示与提示文案
            if (eligibleCfg.getMultiSpecPriceMode() != null) {
                vo.setWholesalePriceMode(eligibleCfg.getMultiSpecPriceMode());
            }
        }
    }

    /**
     * CR-20260714-1335：判定「当前用户是否为批发商，且该商品对其有生效批发价」——
     * 命中则 C 端商品详情/选 SKU 价预览隐藏活动（批发商只看批发价，与 CR-1620 列表口径同源）。
     *
     * <p>非批发商 / 未登录 / 无有效等级 / 该商品无批发价 → false（零影响）。
     * userId 显式传入以兼容 {@link #getProductFinalPrice} 的 userId 参数语义
     * （可能与 {@code SecurityUtils.getCurrentUserId()} 不同）。
     *
     * @param productId 商品 ID
     * @param userId    当前用户 ID（调用方解析后传入）
     * @return true=应隐藏该商品在详情页的 C 端活动
     */
    private boolean isWholesalePricedForUser(Integer productId, Integer userId, Integer contextShopId) {
        if (productId == null || productId <= 0 || userId == null || userId <= 0) {
            return false;
        }
        if (!wholesalerService.isWholesaleBuyer(userId)) {
            return false;
        }
        Integer gradeId = wholesalerService.getGradeIdByUserId(userId);
        if (gradeId == null || gradeId <= 0) {
            return false;
        }
        // O2O 渠道：商品归属与当前门店上下文不一致（平台分配商品）→ 不参与批发，不隐藏其 C 端活动
        if (!com.tigshop.service.wholesale.WholesaleBuyRuleValidator.isClientEligible(
                wholesaleProductConfigService.getActiveByProductId(productId),
                isO2oWholesaleChannel(), contextShopId)) {
            return false;
        }
        return wholesalePriceService.hasPricedProduct(productId, gradeId);
    }

    /** 当前是否 O2O 形态（门店渠道）：决定批发是否按商品归属过滤（平台商品不参与）。 */
    private boolean isO2oWholesaleChannel() {
        return tigshopProperties != null && tigshopProperties.getIsO2o() != null
                && tigshopProperties.getIsO2o() == 1;
    }
}
