package com.tigshop.service.workbuddy.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.dto.product.ProductAttributeDTO;
import com.tigshop.bean.dto.product.ProductAttrsDTO;
import com.tigshop.bean.dto.product.ProductDescDTO;
import com.tigshop.bean.dto.product.ProductDetailDTO;
import com.tigshop.bean.dto.product.ProductListAttributeDTO;
import com.tigshop.bean.dto.workbuddy.ProductDraftDTO;
import com.tigshop.bean.dto.workbuddy.ProductSkuDraftDTO;
import com.tigshop.bean.enums.product.ProductStatus;
import com.tigshop.bean.enums.product.ProductType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.setting.GalleryPic;
import com.tigshop.bean.model.video.GalleryVideoInfo;
import com.tigshop.bean.model.product.ProductVideo;
import com.tigshop.bean.param.product.ProductSaveParam;
import com.tigshop.bean.vo.workbuddy.ProductCategoryVO;
import com.tigshop.bean.vo.workbuddy.ProductImportResultVO;
import com.tigshop.bean.vo.workbuddy.ProductRequirementsVO;
import com.tigshop.bean.vo.workbuddy.ProductValidationVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.SpecImportUtils;
import com.tigshop.service.product.CategoryService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.GalleryPicService;
import com.tigshop.service.video.GalleryVideoInfoService;
import com.tigshop.service.workbuddy.ProductImportService;
import com.tigshop.service.workbuddy.WorkBuddyApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.HttpStatus.BAD_REQUEST;
import static com.tigshop.common.constant.HttpStatus.CONFLICT;

@Service
public class ProductImportServiceImpl implements ProductImportService {

    /**
     * 预览与幂等记录仍为单实例内存态（GAP-01 未处理，多实例部署下不一致）。
     * 此处设置存活期并在每次导入前清理，避免无界增长。
     */
    private static final long PREVIEW_TTL_MILLIS = 30 * 60 * 1000L;

    private static final long IDEMPOTENCY_TTL_MILLIS = 24 * 60 * 60 * 1000L;

    /** 与 SettingsEnum.CUSTOM_PRODUCT_NAME_LENGTH 的默认值一致，仅在配置缺失时兜底。 */
    private static final int DEFAULT_PRODUCT_NAME_LENGTH = 200;

    /** 与 ProductBatchServiceImpl.MARKET_PRICE_RATE_DEFAULT 保持一致。 */
    private static final BigDecimal DEFAULT_MARKET_PRICE_RATE = new BigDecimal("1.2");

    /** 与 ProductBatchServiceImpl.MAX_SKU_SN_LEN 保持一致。 */
    private static final int MAX_SKU_SN_LENGTH = 60;

    private static final int SKU_TYPE_SINGLE = 1;

    private static final int SKU_TYPE_MULTI = 2;

    /** ProductAttribute.attrType：1 为规格属性。 */
    private static final int SPEC_ATTR_TYPE = 1;

    /** 分类结果条数上限：结果直接进入 AI 上下文，过多既无用也挤占 token。 */
    private static final int CATEGORY_PAGE_SIZE = 50;

    private static final int MAX_CATEGORY_DEPTH = 10;

    private static final int MAX_PRODUCTS = 50;

    private static final Logger log = LoggerFactory.getLogger(ProductImportServiceImpl.class);

    private final ProductService ps;

    private final CategoryService categoryService;

    private final ConfigService configService;

    private final ProductSkuService productSkuService;

    private final GalleryPicService galleryPicService;

    private final GalleryVideoInfoService galleryVideoInfoService;

    private final Map<String, Preview> previews = new ConcurrentHashMap<>();

    private final Map<String, IdempotentRecord> idem = new ConcurrentHashMap<>();

    public ProductImportServiceImpl(ProductService ps, CategoryService categoryService, ConfigService configService,
                                    ProductSkuService productSkuService) {
        this(ps, categoryService, configService, productSkuService, null, null);
    }

    @Autowired
    public ProductImportServiceImpl(ProductService ps, CategoryService categoryService, ConfigService configService,
                                    ProductSkuService productSkuService, GalleryPicService galleryPicService,
                                    GalleryVideoInfoService galleryVideoInfoService) {
        this.ps = ps;
        this.categoryService = categoryService;
        this.configService = configService;
        this.productSkuService = productSkuService;
        this.galleryPicService = galleryPicService;
        this.galleryVideoInfoService = galleryVideoInfoService;
    }

    @Override
    public ProductRequirementsVO requirements() {
        ProductRequirementsVO v = new ProductRequirementsVO();
        v.getFields().addAll(List.of(
                ProductRequirementsVO.Field.builder()
                        .name("productName").label("商品名称").type("string").required(true)
                        .description("商品名称")
                        .constraint("最长 " + maxProductNameLength() + " 个字符，该上限由本站配置决定")
                        .example("纯棉圆领短袖T恤")
                        .build(),
                ProductRequirementsVO.Field.builder()
                        .name("productSn").label("商品编码").type("string").required(true)
                        .description("商品编码，用于唯一标识商品；新建前应先调用商品上下文查询，按当前客户历史格式生成候选值")
                        .constraint("不可与已有商品重复；重复会在校验阶段报 PRODUCT_SN_EXIST。历史格式不统一时必须询问用户")
                        .example("调用 product_query(operation=context) 获取候选编码")
                        .templateHint("优先沿用历史编码格式；无法可靠推断时请向用户询问")
                        .build(),
                ProductRequirementsVO.Field.builder()
                        .name("productPrice").label("商品售价").type("decimal").required(true)
                        .description("销售价，单位元")
                        .constraint("不得为负数")
                        .example("99.00")
                        .build(),
                ProductRequirementsVO.Field.builder()
                        .name("categoryId").label("商品分类ID").type("integer").required(true)
                        .description("商品分类ID，与 categoryName 二选一")
                        .constraint("须为已存在的分类；调用分类查询工具按名称检索获取，不要臆造")
                        .example("8")
                        // 表单上只应出现一行「商品分类」，由 categoryName 承担；ID 是给程序调用方的
                        .templateSection(ProductRequirementsVO.TemplateSection.OMITTED)
                        .build(),
                ProductRequirementsVO.Field.builder()
                        .name("categoryName").label("商品分类").type("string").required(false)
                        .description("分类名称路径，可代替 categoryId")
                        .constraint("用 / 或 | 分隔，须从顶级写到末级；分类不存在时校验失败，不会自动创建。"
                                + "与 categoryId 同时给出时以 categoryId 为准")
                        .example("女鞋/运动系列")
                        .templateSection(ProductRequirementsVO.TemplateSection.REQUIRED)
                        .templateHint("从顶级写到末级，用 / 分隔；必须是后台已有的分类")
                        .build(),
                ProductRequirementsVO.Field.builder()
                        .name("productStock").label("库存").type("integer").required(false)
                        .description("库存数量")
                        .defaultValue("0")
                        .constraint("默认上架，故为 0 时前台显示售罄，需在后台补库存")
                        .example("100")
                        .build(),
                ProductRequirementsVO.Field.builder()
                        .name("images").label("商品图片").type("string[]").required(true)
                        .description("商品图片地址，按顺序排列，首张作为主图")
                        .constraint("至少一张；须为 http/https 直链，服务端按地址直接引用，不做下载转存")
                        .example("[\"https://example.com/main.jpg\"]")
                        .templateExample("https://example.com/main.jpg，https://example.com/2.jpg")
                        .templateHint("图片直链，多张用逗号分隔，第一张是主图")
                        .build(),
                ProductRequirementsVO.Field.builder()
                        .name("productBrief").label("商品描述").type("string").required(false)
                        .description("商品简介")
                        .templateExample("100%纯棉，四季可穿")
                        .build(),
                ProductRequirementsVO.Field.builder()
                        .name("productDesc").label("商品详情").type("string").required(false)
                        .description("商品详情，支持HTML")
                        // HTML 不是人在表单里手写的东西；用户要放详情图请填「商品详情图」
                        .templateSection(ProductRequirementsVO.TemplateSection.OMITTED)
                        .build(),
                ProductRequirementsVO.Field.builder()
                        .name("detailImages").label("商品详情图").type("string[]").required(false)
                        .description("商品详情图，按顺序拼进商品详情")
                        .constraint("须为 http/https 直链；与 productDesc 同时给出时本字段被忽略")
                        .example("[\"https://example.com/detail-1.jpg\"]")
                        .templateExample("https://example.com/detail-1.jpg，https://example.com/detail-2.jpg")
                        .templateHint("直链，多张用逗号分隔")
                        .build(),
                ProductRequirementsVO.Field.builder()
                        .name("marketPrice").label("市场价").type("number").required(false)
                        .description("市场价（划线价）")
                        .defaultValue("售价 × 站点配置的换算倍率，默认 1.2")
                        .constraint("不填或填 0 时自动推算，与后台批量导入口径一致")
                        .example("129.00")
                        .templateHint("不填会按售价自动推算")
                        .build(),
                ProductRequirementsVO.Field.builder()
                        .name("costPrice").label("成本价").type("number").required(false)
                        .description("成本价，仅后台可见")
                        .constraint("不得为负数")
                        .example("60.00")
                        .templateHint("仅后台可见，前台不展示")
                        .build(),
                ProductRequirementsVO.Field.builder()
                        .name("productTsn").label("商品条码").type("string").required(false)
                        .description("商品条码")
                        .example("6901234567890")
                        .build(),
                ProductRequirementsVO.Field.builder()
                        .name("keywords").label("搜索关键词").type("string").required(false)
                        .description("搜索关键词")
                        .constraint("多个词用空格分隔")
                        .example("运动鞋 跑步鞋 透气")
                        .templateHint("多个词用空格分隔")
                        .build(),
                ProductRequirementsVO.Field.builder()
                        .name("skus").label("商品规格").type("object[]").required(false)
                        .description("多规格组合；每项含 specs 名值对、price、stock，可选 skuSn/skuTsn/marketPrice/costPrice")
                        .constraint("留空即单规格商品。各项的规格名必须一致；规格组合不得重复；"
                                + "两个及以上规格维度时会自动补齐缺失组合（库存 0）；"
                                + "规格编码留空由系统按 商品编码-规格ID 生成，填写则不得超过 60 字符，"
                                + "且不得与本批次或已有商品的规格编码重复")
                        .example("[{\"specs\":{\"颜色\":\"红色\",\"尺码\":\"37\"},\"price\":149.9,\"stock\":100}]")
                        .templateExample(String.join("\n",
                                "",
                                "  颜色=红色，尺码=37，售价=149，库存=10",
                                "  颜色=红色，尺码=38，售价=159，库存=8",
                                "  颜色=蓝色，尺码=37，售价=149，库存=6"))
                        // 提示里带上写法，使空白件单独拿出来也够用，不必对照样例件
                        .templateHint("单规格商品删掉这段；多规格每行一条，写成 颜色=红色，尺码=37，售价=149，库存=10")
                        .build(),
                ProductRequirementsVO.Field.builder()
                        .name("productStatus").label("是否上架").type("integer").required(false)
                        .description("上架状态：1 上架，0 下架")
                        .defaultValue("1")
                        .constraint("默认上架，与后台批量导入一致；上架后前台立即可见，如需先核对请显式传 0")
                        .example("1")
                        .templateExample("是")
                        .templateHint("填「是」或「否」，不填默认上架")
                        .build()));

        v.getWorkflow().addAll(List.of(
                "1. 分类可直接传 categoryName 名称路径；不确定层级时先用分类查询工具检索",
                "2. 调用校验工具提交商品，得到 previewId 与逐项校验结果",
                "3. 把待创建的商品清单展示给用户，取得明确确认",
                "4. 调用导入工具，原样回传 previewId，并自拟一个 requestId 作为幂等键",
                "5. 导入失败需重试时复用同一个 requestId，避免重复建单"));

        v.getUnsupported().addAll(List.of(
                "品牌、商品参数与规格图",
                "配送方式与运费模板",
                "预售、周期购等销售模式",
                "修改或删除已有商品：本接口只做新建"));

        // 模板字段仅保留兼容模型，不再返回客户可复制的空白/示例模板；
        // AI 客户端应根据字段规范和当前对话动态询问缺失信息。
        v.setTemplate(null);
        return v;
    }

    /** 商品名长度由站点配置决定，回真实值而非写死，避免调用方按错误上限裁剪。 */
    private int maxProductNameLength() {
        try {
            String configured = configService.getConfigVal(SettingsEnum.CUSTOM_PRODUCT_NAME_LENGTH);
            if (StrUtil.isNotBlank(configured)) {
                return Integer.parseInt(configured.trim());
            }
        } catch (Exception e) {
            log.warn("读取商品名称长度配置失败，回落到默认值", e);
        }
        return DEFAULT_PRODUCT_NAME_LENGTH;
    }

    @Override
    public List<ProductCategoryVO> categories(String keyword, Integer parentId) {
        LambdaQueryWrapper<Category> query = new LambdaQueryWrapper<Category>()
                .eq(Category::getIsShow, 1)
                .orderByAsc(Category::getSortOrder)
                .last("limit " + CATEGORY_PAGE_SIZE);
        if (StrUtil.isNotBlank(keyword)) {
            query.like(Category::getCategoryName, StrUtil.trim(keyword));
        } else {
            // 无关键词时按层级浏览：未指定父级即从顶级开始
            query.eq(Category::getParentId, parentId == null ? 0 : parentId);
        }

        List<Category> matched = categoryService.list(query);
        if (matched.isEmpty()) {
            return List.of();
        }

        Set<Integer> withChildren = childBearingIds(matched);
        List<ProductCategoryVO> results = new ArrayList<>();
        for (Category category : matched) {
            ProductCategoryVO vo = new ProductCategoryVO();
            vo.setCategoryId(category.getCategoryId());
            vo.setCategoryName(category.getCategoryName());
            vo.setParentId(category.getParentId());
            vo.setPath(pathOf(category));
            vo.setHasChildren(withChildren.contains(category.getCategoryId()));
            results.add(vo);
        }
        return results;
    }

    /** 一次查出哪些分类还有下级，避免逐条查询。 */
    private Set<Integer> childBearingIds(List<Category> categories) {
        List<Integer> ids = categories.stream().map(Category::getCategoryId).toList();
        return categoryService.list(new LambdaQueryWrapper<Category>().in(Category::getParentId, ids))
                .stream()
                .map(Category::getParentId)
                .collect(Collectors.toSet());
    }

    /**
     * 自下而上拼出分类全路径。
     *
     * <p>同名分类可能挂在不同父级下，只回名称无法区分，故一并返回路径。
     * 设上限以防数据成环时死循环。
     */
    private String pathOf(Category category) {
        Deque<String> segments = new ArrayDeque<>();
        segments.addFirst(category.getCategoryName());
        Integer parentId = category.getParentId();
        for (int depth = 0; depth < MAX_CATEGORY_DEPTH && parentId != null && parentId > 0; depth++) {
            Category parent = categoryService.getById(parentId);
            if (parent == null) {
                break;
            }
            segments.addFirst(parent.getCategoryName());
            parentId = parent.getParentId();
        }
        return String.join("/", segments);
    }

    @Override
    public ProductValidationVO validate(List<ProductDraftDTO> p, Integer shop) {
        if (p == null || p.isEmpty()) {
            throw new WorkBuddyApiException("PRODUCTS_REQUIRED", "商品列表不能为空", BAD_REQUEST, false);
        }
        if (p.size() > MAX_PRODUCTS) {
            throw new WorkBuddyApiException("PRODUCTS_TOO_MANY", "单次最多导入 50 个商品", BAD_REQUEST, false);
        }
        ProductValidationVO v = new ProductValidationVO();
        v.setTotal(p.size());
        Set<String> sns = new HashSet<>();
        boolean all = true;
        for (int i = 0; i < p.size(); i++) {
            ProductDraftDTO d = p.get(i);
            ProductValidationVO.Item it = new ProductValidationVO.Item();
            it.setIndex(i + 1);
            if (d != null) {
                it.setProductName(d.getProductName());
                it.setProductSn(d.getProductSn());
                applySourceMetadata(d, it);
                if (StrUtil.isBlank(d.getProductName())) {
                    err(it, "productName", "REQUIRED");
                    it.getMissingFields().add("productName");
                }
                if (StrUtil.isBlank(d.getProductSn())) {
                    err(it, "productSn", "REQUIRED");
                    it.getMissingFields().add("productSn");
                } else if (!sns.add(d.getProductSn()) || exists(d.getProductSn(), shop)) {
                    err(it, "productSn", "PRODUCT_SN_EXIST");
                }
                if (d.getProductPrice() == null) {
                    err(it, "productPrice", "REQUIRED");
                    it.getMissingFields().add("productPrice");
                }
                if (d.getCategoryId() == null && StrUtil.isBlank(d.getCategoryName())) {
                    err(it, "categoryId", "REQUIRED");
                    it.getMissingFields().add("categoryId/categoryName");
                } else if (resolveCategoryId(d) == null) {
                    err(it, "categoryName", "CATEGORY_NOT_FOUND");
                    it.getMissingFields().add("categoryId/categoryName");
                } else if (isNonLeafCategory(resolveCategoryId(d))) {
                    err(it, "categoryId", "CATEGORY_NOT_LEAF");
                    it.getWarnings().add("当前分类还有下级，请确认并选择末级分类后再创建");
                    it.getMissingFields().add("categoryId/categoryName");
                }
                // 图片必填是默认上架的前提：无图商品一旦自动上架即为前台可见的残次品
                List<String> mediaImages = new ArrayList<>(mediaImageUrls(d));
                mediaImages.addAll(d.getImages() == null ? List.of() : d.getImages());
                if (mediaImages.isEmpty() || mediaImages.stream().allMatch(StrUtil::isBlank)) {
                    err(it, "images", "REQUIRED");
                    it.getMissingFields().add("images");
                } else if (hasInvalidImage(mediaImages)) {
                    err(it, "images", "INVALID_URL");
                    it.getMissingFields().add("images");
                }
                if (hasInvalidImage(d.getDetailImages())) {
                    err(it, "detailImages", "INVALID_URL");
                    it.getMissingFields().add("detailImages");
                }
                int mediaErrorsBefore = it.getErrors().size();
                validateMediaAssets(d, it);
                if (it.getErrors().size() > mediaErrorsBefore) {
                    it.getMissingFields().add("media");
                }
                validateSkus(d, it);
            } else {
                err(it, "product", "REQUIRED");
                it.getMissingFields().add("product");
            }
            it.setValid(it.getErrors().isEmpty());
            it.setProduct(d == null ? null : normalizedDraft(d));
            for (String field : it.getMissingFields()) {
                if (!v.getMissingFields().contains(field)) {
                    v.getMissingFields().add(field);
                }
            }
            all &= it.isValid();
            v.getItems().add(it);
        }
        v.setValid(all);
        v.setPreviewId(UUID.randomUUID().toString());

        evictExpired();
        previews.put(v.getPreviewId(), new Preview(shop, Collections.unmodifiableList(new ArrayList<>(p)),
                fingerprint(p), all, System.currentTimeMillis() + PREVIEW_TTL_MILLIS));
        return v;
    }

    /**
     * 为校验结果补齐字段来源。旧调用方未传来源时保留兼容，但明确标为
     * {@code user_provided}（显式提交值）或 {@code site_default}（服务端补值），
     * AI 推荐值必须由调用方显式传 {@code fieldSources[field]=ai_recommended}。
     */
    private static void applySourceMetadata(ProductDraftDTO draft, ProductValidationVO.Item item) {
        Map<String, String> supplied = draft.getFieldSources() == null ? Map.of() : draft.getFieldSources();
        markSource(item, supplied, "productName", StrUtil.isNotBlank(draft.getProductName()), "user_provided");
        markSource(item, supplied, "productSn", StrUtil.isNotBlank(draft.getProductSn()), "user_provided");
        markSource(item, supplied, "productPrice", draft.getProductPrice() != null, "user_provided");
        markSource(item, supplied, "categoryId", draft.getCategoryId() != null || StrUtil.isNotBlank(draft.getCategoryName()), "user_provided");
        markSource(item, supplied, "categoryName", StrUtil.isNotBlank(draft.getCategoryName()), "user_provided");
        markSource(item, supplied, "images", (draft.getImages() != null && draft.getImages().stream().anyMatch(StrUtil::isNotBlank))
                || (draft.getMedia() != null && !draft.getMedia().isEmpty()), "user_provided");
        markSource(item, supplied, "productStock", draft.getProductStock() != null, "user_provided");
        markSource(item, supplied, "productStatus", draft.getProductStatus() != null, "user_provided");
        markSource(item, supplied, "marketPrice", draft.getMarketPrice() != null, "user_provided");
        if (draft.getProductStock() == null) {
            item.getSource().put("productStock", "site_default");
            item.getWarnings().add("库存未提供，将使用系统默认值 0，请在创建前确认");
        }
        if (draft.getProductStatus() == null) {
            item.getSource().put("productStatus", "site_default");
            item.getWarnings().add("上下架状态未提供，将使用系统默认值上架，请在创建前确认");
        }
        if (draft.getMarketPrice() == null) {
            item.getSource().put("marketPrice", "site_default");
            item.getWarnings().add("市场价未提供，将按站点规则推算，请在创建前确认");
        }
        for (Map.Entry<String, String> entry : supplied.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) continue;
            String source = entry.getValue();
            if (!Set.of("user_provided", "ai_recommended", "site_default").contains(source)) continue;
            item.getSource().put(entry.getKey(), source);
            if ("ai_recommended".equals(source) && !item.getWarnings().contains("字段「" + entry.getKey() + "」为 AI 建议，请在创建前确认")) {
                item.getWarnings().add("字段「" + entry.getKey() + "」为 AI 建议，请在创建前确认");
            }
        }
    }

    private static void markSource(ProductValidationVO.Item item, Map<String, String> supplied,
                                   String field, boolean present, String defaultSource) {
        if (!present) return;
        String source = supplied.get(field);
        if (source == null || !Set.of("user_provided", "ai_recommended", "site_default").contains(source)) {
            source = defaultSource;
        }
        item.getSource().put(field, source);
        if ("ai_recommended".equals(source)) {
            item.getWarnings().add("字段「" + field + "」为 AI 建议，请在创建前确认");
        }
    }

    private ProductDraftDTO normalizedDraft(ProductDraftDTO draft) {
        ProductDraftDTO normalized = new ProductDraftDTO();
        BeanUtils.copyProperties(draft, normalized);
        normalized.setCategoryId(resolveCategoryId(draft));
        if (normalized.getProductStock() == null) normalized.setProductStock(0);
        if (normalized.getProductStatus() == null) normalized.setProductStatus(ProductStatus.ON_SALE.getCode());
        normalized.setMarketPrice(resolveMarketPrice(draft));
        return normalized;
    }

    /** 有实体时检查是否还有子分类；测试替身未提供实体则保留旧兼容行为。 */
    private boolean isNonLeafCategory(Integer categoryId) {
        if (categoryId == null || categoryService == null) return false;
        Category category;
        try {
            category = categoryService.getById(categoryId);
            if (category == null) return false;
            List<Category> children = categoryService.list(new LambdaQueryWrapper<Category>()
                    .eq(Category::getParentId, categoryId).last("limit 1"));
            return children != null && !children.isEmpty();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    @Override
    public ProductImportResultVO importProducts(List<ProductDraftDTO> submitted, Integer shop, String previewId,
                                                String idempotencyKey, boolean confirmation) {
        if (!confirmation) {
            throw new WorkBuddyApiException("CONFIRMATION_REQUIRED", "导入前必须确认预览结果", BAD_REQUEST, false);
        }
        if (StrUtil.isBlank(idempotencyKey)) {
            throw new WorkBuddyApiException("IDEMPOTENCY_KEY_REQUIRED", "缺少 Idempotency-Key", BAD_REQUEST, false);
        }
        if (StrUtil.isBlank(previewId)) {
            throw new WorkBuddyApiException("PREVIEW_REQUIRED", "缺少 previewId，请先调用校验接口", BAD_REQUEST, false);
        }

        evictExpired();

        String submittedFingerprint = (submitted == null || submitted.isEmpty()) ? null : fingerprint(submitted);

        // 幂等先于预览判定：预览存活期短于幂等存活期，重试仍需拿到原结果。
        String idemKey = shop + ":" + idempotencyKey;
        IdempotentRecord prior = idem.get(idemKey);
        if (prior != null) {
            if (submittedFingerprint != null && !submittedFingerprint.equals(prior.fingerprint())) {
                throw new WorkBuddyApiException("IDEMPOTENCY_KEY_CONFLICT",
                        "相同 Idempotency-Key 已用于不同的导入内容", CONFLICT, false);
            }
            return prior.result();
        }

        Preview preview = previews.get(previewId);
        if (preview == null || !Objects.equals(preview.shopId(), shop)) {
            throw new WorkBuddyApiException("PREVIEW_NOT_FOUND",
                    "previewId 不存在或已过期，请重新调用校验接口", CONFLICT, false);
        }
        if (!preview.importable()) {
            throw new WorkBuddyApiException("PREVIEW_NOT_IMPORTABLE",
                    "预览结果存在字段错误，修正后请重新校验", CONFLICT, false);
        }
        if (submittedFingerprint != null && !submittedFingerprint.equals(preview.fingerprint())) {
            throw new WorkBuddyApiException("PREVIEW_MISMATCH",
                    "提交的商品与预览结果不一致，请重新校验", CONFLICT, false);
        }

        List<ProductDraftDTO> products = preview.products();
        ProductImportResultVO r = new ProductImportResultVO();
        r.setRequestId(UUID.randomUUID().toString());
        r.setPreviewId(previewId);
        r.setTotal(products.size());
        for (int i = 0; i < products.size(); i++) {
            ProductDraftDTO d = products.get(i);
            ProductImportResultVO.Item it = new ProductImportResultVO.Item();
            it.setIndex(i + 1);
            if (d != null) {
                it.setProductName(d.getProductName());
                it.setProductSn(d.getProductSn());
            }
            try {
                if (d == null) {
                    throw new GlobalException("商品数据不能为空", BAD_REQUEST);
                }
                if (exists(d.getProductSn(), shop)) {
                    throw new GlobalException("商品编码已存在", CONFLICT);
                }
                List<String> filled = new ArrayList<>();
                ProductSaveParam q = toSaveParam(d, shop, filled);
                ps.create(q);
                it.setSuccess(true);
                // create 返回的是提示文案而非主键，且被大量既有调用方共用，故按编码回查
                it.setProductId(findProductId(d.getProductSn(), shop));
                it.setProductStatus(q.getProductStatus());
                it.setSuggestion(buildSuggestion(q, filled));
                it.setVerification(verifyCreatedProduct(it.getProductId(), d, q));
                r.setSuccessCount(r.getSuccessCount() + 1);
            } catch (Exception e) {
                boolean snExists = e instanceof GlobalException ge && ge.getCode() == CONFLICT;
                boolean assetError = e instanceof IllegalArgumentException && "ASSET_NOT_ACCESSIBLE".equals(e.getMessage());
                it.setErrorCode(snExists ? "PRODUCT_SN_EXIST" : assetError ? "ASSET_NOT_ACCESSIBLE" : "PRODUCT_CREATE_FAILED");
                it.setMessage(StrUtil.blankToDefault(e.getMessage(), "商品创建失败"));
                it.setSuggestion(snExists ? "商品编码已被占用，请更换编码后重新校验并导入" : "请修正商品数据后重新校验并导入");
                r.setFailureCount(r.getFailureCount() + 1);
            }
            r.getItems().add(it);
        }

        idem.put(idemKey, new IdempotentRecord(preview.fingerprint(), r,
                System.currentTimeMillis() + IDEMPOTENCY_TTL_MILLIS));
        return r;
    }

    /**
     * 创建后回读商品详情，避免只回显 create 入参而把串位/默认值问题说成成功。
     * 回查失败不会回滚已创建商品，调用方必须看到独立的 failed 状态。
     */
    private ProductImportResultVO.Verification verifyCreatedProduct(Integer productId,
                                                                      ProductDraftDTO draft,
                                                                      ProductSaveParam expected) {
        ProductImportResultVO.Verification result = new ProductImportResultVO.Verification();
        if (productId == null) {
            result.setStatus("failed");
            result.setMessage("商品已创建，但未返回商品ID，无法完成写后核对");
            return result;
        }
        try {
            ProductDetailDTO actual = ps.detail(productId);
            if (actual == null) {
                result.setStatus("failed");
                result.setMessage("商品已创建，但详情查询未返回数据");
                return result;
            }
            List<String> mismatches = new ArrayList<>();
            check(result, mismatches, "商品名称", expected.getProductName(), actual.getProductName());
            check(result, mismatches, "商品编码", expected.getProductSn(), actual.getProductSn());
            checkDecimal(result, mismatches, "售价", expected.getProductPrice(), actual.getProductPrice());
            check(result, mismatches, "库存", expected.getProductStock() == null ? 0 : expected.getProductStock(), actual.getProductStock());
            check(result, mismatches, "分类", expected.getCategoryId(), actual.getCategoryId());
            check(result, mismatches, "上下架状态", expected.getProductStatus(), actual.getProductStatus());

            String expectedPic = firstImage(expected.getImgList());
            if (expectedPic != null) {
                result.getCheckedFields().add("主图");
                if (!Objects.equals(expectedPic, actual.getPicUrl())) mismatches.add("主图");
            }
            if (expected.getProductDesc() != null) {
                result.getCheckedFields().add("详情图");
                if (actual.getProductDesc() == null || !containsAllDetailUrls(expected.getProductDesc(), actual.getProductDesc())) {
                    mismatches.add("详情图");
                }
            }
            if (expected.getProductVideoInfo() != null && !expected.getProductVideoInfo().isEmpty()) {
                result.getCheckedFields().add("视频");
                if (actual.getProductVideoInfo() == null || actual.getProductVideoInfo().isEmpty()) {
                    mismatches.add("视频");
                }
            }
            result.setMismatches(mismatches);
            if (mismatches.isEmpty()) {
                result.setStatus("verified");
                result.setMessage("商品已创建，关键字段已核对一致");
            } else {
                result.setStatus("mismatch");
                result.setMessage("商品已创建，但部分字段与草稿不一致");
            }
            return result;
        } catch (RuntimeException e) {
            result.setStatus("failed");
            result.setMessage("商品已创建，但详情查询失败");
            return result;
        }
    }

    private static String firstImage(List<GalleryPic> images) {
        if (images == null || images.isEmpty()) return null;
        return images.getFirst() == null ? null : images.getFirst().getPicUrl();
    }

    private static boolean containsAllDetailUrls(String expected, String actual) {
        for (ProductDescDTO segment : ProductDescDTO.getProductDescArr(expected)) {
            if ("pic".equals(segment.getType()) && StrUtil.isNotBlank(segment.getPic()) && !actual.contains(segment.getPic())) {
                return false;
            }
        }
        return true;
    }

    private static void check(ProductImportResultVO.Verification result, List<String> mismatches,
                              String label, Object expected, Object actual) {
        result.getCheckedFields().add(label);
        if (!Objects.equals(expected, actual)) mismatches.add(label);
    }

    private static void checkDecimal(ProductImportResultVO.Verification result, List<String> mismatches,
                                     String label, BigDecimal expected, BigDecimal actual) {
        result.getCheckedFields().add(label);
        if (expected == null ? actual != null : actual == null || expected.compareTo(actual) != 0) mismatches.add(label);
    }

    /**
     * 草稿转创建参数。
     *
     * <p>草稿只覆盖名称、货号、价格、分类等基础字段，缺失项需在此补默认值：
     * {@code ProductService#create} 会直接拆箱 productType，为空即抛 NPE。
     * 上架状态默认为上架，与后台 Excel 批量导入的「是否上架（默认1）」一致；
     * 该默认值以图片必填为前提，二者不可拆分交付（CR-20260828-152000 CH-02/CH-03）。
     */
    private ProductSaveParam toSaveParam(ProductDraftDTO draft, Integer shopId, List<String> filledCombinations) {
        ProductSaveParam param = new ProductSaveParam();
        BeanUtils.copyProperties(draft, param);
        param.setShopId(shopId);
        param.setProductType(ProductType.PRODUCT_TYPE_NORMAL.getCode());
        param.setCategoryId(resolveCategoryId(draft));
        if (param.getProductStatus() == null) {
            param.setProductStatus(ProductStatus.ON_SALE.getCode());
        }
        param.setMarketPrice(resolveMarketPrice(draft));
        MediaMapping mediaMapping = mapMedia(draft);
        param.setImgList(toGallery(mediaMapping.imageUrls()));
        if (StrUtil.isBlank(param.getProductDesc())) {
            param.setProductDesc(toProductDesc(mediaMapping.detailUrls()));
        }
        if (draft.getMedia() != null && !mediaMapping.videos().isEmpty()) {
            param.setProductVideoInfo(mediaMapping.videos());
        }
        applySkus(draft, param, filledCombinations);
        return param;
    }

    /**
     * 把结构化规格映射成标准创建链路的多规格入参。
     *
     * <p>{@code ProductService#create} 按 skuType 与 productList 分叉，但**不会**汇总
     * 主表的价格与库存，需在此按最低价与库存总和算好；主表价格还要过 checkProductPrice 的非零校验。
     *
     * @param filledCombinations 收集自动补全出的规格组合，供导入结果如实告知调用方
     */
    private void applySkus(ProductDraftDTO draft, ProductSaveParam param, List<String> filledCombinations) {
        List<ProductSkuDraftDTO> skus = draft.getSkus();
        if (skus == null || skus.isEmpty()) {
            param.setSkuType(SKU_TYPE_SINGLE);
            return;
        }

        List<List<String[]>> combos = new ArrayList<>();
        List<ProductSkuDraftDTO> ordered = new ArrayList<>();
        for (ProductSkuDraftDTO sku : skus) {
            combos.add(toPairs(sku.getSpecs()));
            ordered.add(sku);
        }
        fillMissingCombinations(combos, ordered, filledCombinations);

        List<ProductListAttributeDTO> productList = new ArrayList<>();
        for (int i = 0; i < ordered.size(); i++) {
            productList.add(toSkuParam(ordered.get(i), combos.get(i), draft));
        }

        param.setSkuType(SKU_TYPE_MULTI);
        param.setProductList(productList);
        param.setAttrList(toAttrList(combos));
        param.setProductPrice(lowestPrice(ordered, draft));
        param.setProductStock(totalStock(ordered));
    }

    private static List<String[]> toPairs(Map<String, String> specs) {
        List<String[]> pairs = new ArrayList<>();
        if (specs == null) {
            return pairs;
        }
        specs.forEach((name, value) -> {
            String n = StrUtil.trim(name);
            String v = StrUtil.trim(value);
            if (StrUtil.isNotBlank(n) && StrUtil.isNotBlank(v)) {
                pairs.add(new String[]{n, v});
            }
        });
        return pairs;
    }

    /**
     * 补齐笛卡尔积中缺失的规格组合，与后台 Excel 批量导入行为一致。
     *
     * <p>只在两个及以上规格维度时补全——单维度不存在“组合缺失”。
     * 补出的组合价格与库存均为 0，属服务端行为，调用方并未要求，故须回传给调用方。
     */
    private static void fillMissingCombinations(List<List<String[]>> combos, List<ProductSkuDraftDTO> ordered,
                                                List<String> filledCombinations) {
        Map<String, Set<String>> groups = new LinkedHashMap<>();
        for (List<String[]> combo : combos) {
            for (String[] pair : combo) {
                groups.computeIfAbsent(pair[0], k -> new LinkedHashSet<>()).add(pair[1]);
            }
        }
        if (groups.size() < 2) {
            return;
        }
        Set<String> present = combos.stream().map(SpecImportUtils::buildSkuValue).collect(Collectors.toSet());
        for (List<String[]> combo : cartesian(groups)) {
            String skuValue = SpecImportUtils.buildSkuValue(combo);
            if (present.contains(skuValue)) {
                continue;
            }
            ProductSkuDraftDTO placeholder = new ProductSkuDraftDTO();
            placeholder.setPrice(BigDecimal.ZERO);
            placeholder.setStock(0);
            combos.add(combo);
            ordered.add(placeholder);
            filledCombinations.add(skuValue);
        }
    }

    private static List<List<String[]>> cartesian(Map<String, Set<String>> groups) {
        List<List<String[]>> result = new ArrayList<>();
        result.add(new ArrayList<>());
        for (Map.Entry<String, Set<String>> group : groups.entrySet()) {
            List<List<String[]>> expanded = new ArrayList<>();
            for (List<String[]> prefix : result) {
                for (String value : group.getValue()) {
                    List<String[]> combo = new ArrayList<>(prefix);
                    combo.add(new String[]{group.getKey(), value});
                    expanded.add(combo);
                }
            }
            result = expanded;
        }
        return result;
    }

    private static ProductListAttributeDTO toSkuParam(ProductSkuDraftDTO sku, List<String[]> combo,
                                                     ProductDraftDTO draft) {
        ProductListAttributeDTO param = new ProductListAttributeDTO();
        param.setSkuValue(SpecImportUtils.buildSkuValue(combo));
        param.setAttrs(combo.stream().map(pair -> {
            ProductListAttributeDTO.Attribute attribute = new ProductListAttributeDTO.Attribute();
            attribute.setAttrName(pair[0]);
            attribute.setAttrValue(pair[1]);
            return attribute;
        }).collect(Collectors.toList()));
        BigDecimal price = sku.getPrice() == null ? draft.getProductPrice() : sku.getPrice();
        // 落库字段为字符串，null 会在转换时 NPE
        param.setSkuPrice(price == null ? "0" : price.toPlainString());
        param.setSkuStock(sku.getStock() == null ? 0 : sku.getStock());
        param.setSkuSn(StrUtil.trimToEmpty(sku.getSkuSn()));
        param.setSkuTsn(StrUtil.trimToEmpty(sku.getSkuTsn()));
        param.setMarketPrice(sku.getMarketPrice());
        param.setCostPrice(sku.getCostPrice());
        return param;
    }

    /** 规格属性用于后台商品编辑页回显规格结构，缺失会导致商品建出后无法编辑规格。 */
    private static ProductAttrsDTO toAttrList(List<List<String[]>> combos) {
        Map<String, Set<String>> groups = new LinkedHashMap<>();
        for (List<String[]> combo : combos) {
            for (String[] pair : combo) {
                groups.computeIfAbsent(pair[0], k -> new LinkedHashSet<>()).add(pair[1]);
            }
        }
        List<ProductAttrsDTO.Attribute> spe = new ArrayList<>();
        groups.forEach((name, values) -> {
            List<ProductAttributeDTO> attrs = new ArrayList<>();
            for (String value : values) {
                ProductAttributeDTO attr = new ProductAttributeDTO();
                attr.setAttrType(SPEC_ATTR_TYPE);
                attr.setAttrName(name);
                attr.setAttrValue(value);
                attrs.add(attr);
            }
            spe.add(new ProductAttrsDTO.Attribute(attrs, name));
        });
        ProductAttrsDTO attrList = new ProductAttrsDTO();
        attrList.setSpe(spe);
        return attrList;
    }

    /** 主表价格须为正数，否则 create 的 checkProductPrice 会拒绝；补全出的 0 价占位不参与取最低。 */
    private static BigDecimal lowestPrice(List<ProductSkuDraftDTO> skus, ProductDraftDTO draft) {
        return skus.stream()
                .map(ProductSkuDraftDTO::getPrice)
                .filter(price -> price != null && price.compareTo(BigDecimal.ZERO) > 0)
                .min(BigDecimal::compareTo)
                .orElse(draft.getProductPrice());
    }

    private static Integer totalStock(List<ProductSkuDraftDTO> skus) {
        return skus.stream().mapToInt(sku -> sku.getStock() == null ? 0 : sku.getStock()).sum();
    }

    /**
     * 市场价缺省时按售价乘以换算倍率推算，与 Excel 批量导入一致。
     *
     * <p>不照做会导致同一批商品经两条通道导入得到不同的划线价。
     */
    private BigDecimal resolveMarketPrice(ProductDraftDTO draft) {
        BigDecimal given = draft.getMarketPrice();
        if (given != null && given.compareTo(BigDecimal.ZERO) > 0) {
            return given;
        }
        if (draft.getProductPrice() == null) {
            return null;
        }
        return draft.getProductPrice().multiply(marketPriceRate()).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal marketPriceRate() {
        try {
            String configured = configService.getConfigVal(SettingsEnum.MARKET_PRICE_RATE);
            if (StrUtil.isNotBlank(configured)) {
                return new BigDecimal(configured.trim());
            }
        } catch (Exception e) {
            log.warn("读取市场价换算倍率配置失败，回落到默认值", e);
        }
        return DEFAULT_MARKET_PRICE_RATE;
    }

    /**
     * 详情图组装为商品详情。
     *
     * <p>商品详情落库的是约定 HTML 而非任意富文本，须经 {@code ProductDescDTO} 生成，
     * 否则后台编辑器与「导出再导入」都无法反解析出图片段。
     */
    private static String toProductDesc(List<String> detailImages) {
        if (detailImages == null || detailImages.isEmpty()) {
            return null;
        }
        List<ProductDescDTO> segments = new ArrayList<>();
        for (String url : dedupe(detailImages)) {
            ProductDescDTO segment = new ProductDescDTO();
            segment.setType("pic");
            segment.setPic(url);
            segments.add(segment);
        }
        return segments.isEmpty() ? null : ProductDescDTO.getProductDesc(segments);
    }

    /**
     * 解析分类：categoryId 优先，其次按名称路径逐级匹配。
     *
     * <p>{@code isAutoCat} 固定传 0。该开关在 Excel 导入侧由运营在页面上显式勾选，
     * 而本接口的调用方是 AI；放开会让模型臆造的分类名直接建成分类并污染分类树。
     *
     * @return 分类 ID；未给出或路径无法匹配时返回 null
     */
    private Integer resolveCategoryId(ProductDraftDTO draft) {
        if (draft.getCategoryId() != null) {
            return draft.getCategoryId();
        }
        if (StrUtil.isBlank(draft.getCategoryName())) {
            return null;
        }
        Integer resolved = categoryService.getCategoryIds(draft.getCategoryName().trim().split("[/|]"), 0);
        return resolved == null || resolved == 0 ? null : resolved;
    }

    /**
     * 图片沿用 productBatchModify 的既有约定：外链原样引用，不下载转存，首张即主图。
     *
     * <p>{@code ProductService#create} 会据此写入商品主图并生成图册行，无需另建图库记录。
     * 与批量导入一致按顺序去重，避免同一张图重复占位。
     */
    private static List<GalleryPic> toGallery(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        List<GalleryPic> pics = new ArrayList<>();
        for (String url : dedupe(images)) {
            GalleryPic pic = new GalleryPic();
            pic.setPicUrl(url);
            pic.setPicThumb(url);
            pics.add(pic);
        }
        return pics.isEmpty() ? null : pics;
    }

    private List<String> mediaImageUrls(ProductDraftDTO draft) {
        if (draft.getMedia() == null) return List.of();
        List<String> urls = new ArrayList<>();
        for (var m : draft.getMedia()) {
            if (m == null) continue;
            if (!"image".equalsIgnoreCase(m.getType())) continue;
            if (!("main_image".equalsIgnoreCase(m.getRole()) || "gallery_image".equalsIgnoreCase(m.getRole()) || m.getRole() == null)) continue;
            String u = trustedImageUrl(m); if (StrUtil.isNotBlank(u)) urls.add(u);
        }
        return urls;
    }

    private MediaMapping mapMedia(ProductDraftDTO draft) {
        List<String> images = new ArrayList<>(mediaImageUrls(draft));
        List<String> details = new ArrayList<>();
        List<ProductVideo> videos = new ArrayList<>();
        if (draft.getMedia() != null) {
            for (var m : draft.getMedia()) {
                if (m == null) continue;
                if ("image".equalsIgnoreCase(m.getType()) && "detail_image".equalsIgnoreCase(m.getRole())) {
                    String u = trustedImageUrl(m); if (StrUtil.isNotBlank(u)) details.add(u);
                } else if ("video".equalsIgnoreCase(m.getType())) {
                    ProductVideo pv = new ProductVideo();
                    if (m.getAssetId() != null) {
                        if (galleryVideoInfoService == null) {
                            throw new IllegalArgumentException("ASSET_NOT_ACCESSIBLE");
                        }
                        try {
                            GalleryVideoInfo v = galleryVideoInfoService.getById(m.getAssetId());
                            if (v == null || !Objects.equals(v.getShopId(), 0) || StrUtil.isBlank(v.getVideoUrl())) {
                                throw new IllegalArgumentException("ASSET_NOT_ACCESSIBLE");
                            }
                            pv.setVideoId(v.getId()); pv.setVideoUrl(v.getVideoUrl()); pv.setVideoCover(v.getVideoCover()); pv.setFormat(v.getFormat());
                        } catch (IllegalArgumentException e) {
                            // Preserve our sentinel while normalizing provider/query argument failures.
                            if ("ASSET_NOT_ACCESSIBLE".equals(e.getMessage())) {
                                throw e;
                            }
                            throw new IllegalArgumentException("ASSET_NOT_ACCESSIBLE", e);
                        } catch (RuntimeException e) {
                            throw new IllegalArgumentException("ASSET_NOT_ACCESSIBLE", e);
                        }
                    } else {
                        pv.setVideoUrl(m.getUrl()); pv.setVideoCover(m.getCoverUrl()); pv.setFormat(m.getFormat());
                    }
                    if (StrUtil.isNotBlank(pv.getVideoUrl())) videos.add(pv);
                }
            }
        }
        if (images.isEmpty() || draft.getMedia() == null) images.addAll(draft.getImages() == null ? List.of() : draft.getImages());
        else images.addAll(draft.getImages() == null ? List.of() : draft.getImages());
        details.addAll(draft.getDetailImages() == null ? List.of() : draft.getDetailImages());
        return new MediaMapping(new ArrayList<>(dedupe(images)), new ArrayList<>(dedupe(details)), videos);
    }

    private String trustedImageUrl(com.tigshop.bean.dto.workbuddy.ProductMediaDraftDTO m) {
        if (m.getAssetId() != null) {
            if (galleryPicService == null) return null;
            GalleryPic p = galleryPicService.getById(m.getAssetId());
            return p == null || !Objects.equals(p.getShopId(), 0) ? null : p.getPicUrl();
        }
        return m.getUrl();
    }

    private void validateMediaAssets(ProductDraftDTO draft, ProductValidationVO.Item item) {
        if (draft.getMedia() == null) return;
        for (var m : draft.getMedia()) {
            if (m == null) { err(item, "media", "INVALID_MEDIA"); continue; }
            if (m.getAssetId() == null) continue;
            boolean ok = false;
            if ("video".equalsIgnoreCase(m.getType())) {
                if (galleryVideoInfoService != null) {
                    try {
                        GalleryVideoInfo v = galleryVideoInfoService.getById(m.getAssetId()); ok = v != null && StrUtil.isNotBlank(v.getVideoUrl()) && Objects.equals(v.getShopId(), 0);
                    } catch (RuntimeException ignored) { ok = false; }
                }
            } else if (galleryPicService != null) {
                try {
                    GalleryPic p = galleryPicService.getById(m.getAssetId()); ok = p != null && Objects.equals(p.getShopId(), 0);
                } catch (RuntimeException ignored) { ok = false; }
            }
            if (!ok) err(item, "media", "ASSET_NOT_ACCESSIBLE");
        }
    }

    private record MediaMapping(List<String> imageUrls, List<String> detailUrls, List<ProductVideo> videos) {}

    /** 按序去重并去空白，避免同一张图重复占位。 */
    private static Set<String> dedupe(List<String> urls) {
        Set<String> unique = new LinkedHashSet<>();
        for (String raw : urls) {
            String url = StrUtil.trim(raw);
            if (StrUtil.isNotBlank(url)) {
                unique.add(url);
            }
        }
        return unique;
    }

    /**
     * 说明商品建出后的可售状态与待办。
     *
     * <p>默认上架后商品即刻对前台可见，故已上架但无库存必须显式提示：
     * 用户看到的是「售罄」而非缺陷，不提示就无从得知需要补库存。
     */
    private static String buildSuggestion(ProductSaveParam saved, List<String> filledCombinations) {
        boolean onSale = Objects.equals(saved.getProductStatus(), ProductStatus.ON_SALE.getCode());
        boolean outOfStock = saved.getProductStock() == null || saved.getProductStock() <= 0;
        String base;
        if (onSale && outOfStock) {
            base = "已上架，但库存为 0，前台显示售罄；请在后台补库存";
        } else if (onSale) {
            base = "已上架，前台可正常购买";
        } else if (outOfStock) {
            base = "当前为下架状态且库存为 0，补库存后可在后台手动上架";
        } else {
            base = "当前为下架状态，可在后台商品列表核对后手动上架";
        }
        if (filledCombinations == null || filledCombinations.isEmpty()) {
            return base;
        }
        // 补全出的规格调用方并未要求，不告知则用户会在后台看到自己没提过的规格
        return base + StrUtil.format("。已自动补齐缺失的规格组合 {}（库存为 0），如不需要请在后台删除",
                String.join("、", filledCombinations));
    }

    private Integer findProductId(String sn, Integer shop) {
        LambdaQueryWrapper<Product> q = new LambdaQueryWrapper<Product>()
                .eq(Product::getProductSn, sn)
                .orderByDesc(Product::getProductId)
                .last("limit 1");
        if (shop != null) {
            q.eq(Product::getShopId, shop);
        }
        Product product = ps.getOne(q);
        return product == null ? null : product.getProductId();
    }

    private boolean exists(String sn, Integer shop) {
        LambdaQueryWrapper<Product> q = new LambdaQueryWrapper<Product>().eq(Product::getProductSn, sn);
        if (shop != null) {
            q.eq(Product::getShopId, shop);
        }
        return ps.count(q) > 0;
    }

    /**
     * 多规格校验。
     *
     * <p>规格名跨 SKU 必须一致，否则笛卡尔补全会按并集展开，建出大量调用方没要求的组合。
     * 规格编码沿用批量导入的长度上限与组内唯一约束。
     */
    private void validateSkus(ProductDraftDTO draft, ProductValidationVO.Item item) {
        List<ProductSkuDraftDTO> skus = draft.getSkus();
        if (skus == null || skus.isEmpty()) {
            return;
        }
        Set<String> names = null;
        Set<String> combos = new HashSet<>();
        Set<String> skuSns = new HashSet<>();
        for (ProductSkuDraftDTO sku : skus) {
            List<String[]> pairs = toPairs(sku.getSpecs());
            if (pairs.isEmpty()) {
                err(item, "skus", "SPEC_REQUIRED");
                continue;
            }
            Set<String> current = pairs.stream().map(pair -> pair[0]).collect(Collectors.toCollection(LinkedHashSet::new));
            if (names == null) {
                names = current;
            } else if (!names.equals(current)) {
                err(item, "skus", "SPEC_NAME_MISMATCH");
            }
            if (!combos.add(SpecImportUtils.buildSkuValue(pairs))) {
                err(item, "skus", "SPEC_DUPLICATED");
            }
            String skuSn = StrUtil.trim(sku.getSkuSn());
            if (StrUtil.isNotBlank(skuSn)) {
                if (skuSn.length() > MAX_SKU_SN_LENGTH) {
                    err(item, "skus", "SKU_SN_TOO_LONG");
                }
                if (!skuSns.add(skuSn)) {
                    err(item, "skus", "SKU_SN_DUPLICATED");
                } else if (isSkuSnOccupied(skuSn)) {
                    err(item, "skus", "SKU_SN_EXIST");
                }
            }
        }
    }

    /**
     * 规格编码全局占用检查，口径同 ProductBatchServiceImpl.isSkuSnOccupied。
     *
     * <p>标准创建链路不校验规格编码唯一性，不在此拦截会静默建出与已有商品重码的 SKU，
     * 让按编码定位货品的场景取到错误结果。已软删的商品不占用编码。
     */
    private boolean isSkuSnOccupied(String skuSn) {
        return productSkuService.count(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getSkuSn, skuSn)
                .inSql(ProductSku::getProductId, "SELECT product_id FROM product WHERE is_delete = 0")) > 0;
    }

    /**
     * 只校验协议，不校验扩展名。
     *
     * <p>图库的外链入口按扩展名白名单拒绝，但 AI 工具与 CDN 给出的地址常无扩展名；
     * 在此照搬会挡掉合法图片。这里只拦截相对路径、data URI 等确定不可用的输入。
     */
    private static boolean hasInvalidImage(List<String> images) {
        if (images == null) {
            return false;
        }
        return images.stream().anyMatch(image -> {
            String url = StrUtil.trim(image);
            return StrUtil.isNotBlank(url) && !url.startsWith("http://") && !url.startsWith("https://");
        });
    }

    private static void err(ProductValidationVO.Item i, String f, String c) {
        ProductValidationVO.FieldError e = new ProductValidationVO.FieldError();
        e.setField(f);
        e.setCode(c);
        e.setMessage(c);
        i.getErrors().add(e);
    }

    /** 载荷指纹，用于预览一致性与幂等冲突判定。 */
    private static String fingerprint(List<ProductDraftDTO> products) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(String.valueOf(products).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    private void evictExpired() {
        long now = System.currentTimeMillis();
        previews.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= now);
        idem.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= now);
    }

    private record Preview(Integer shopId, List<ProductDraftDTO> products, String fingerprint,
                           boolean importable, long expiresAt) {
    }

    private record IdempotentRecord(String fingerprint, ProductImportResultVO result, long expiresAt) {
    }
}
