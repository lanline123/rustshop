package com.tigshop.service.workbuddy.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tigshop.bean.dto.common.UpdateFieldProductDTO;
import com.tigshop.bean.dto.product.ProductCopyDTO;
import com.tigshop.bean.dto.product.ProductDetailDTO;
import com.tigshop.bean.dto.product.ProductListDTO;
import com.tigshop.bean.dto.product.ProductListResDTO;
import com.tigshop.bean.dto.product.ProductRecycleDTO;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.dto.workbuddy.ProductSearchDTO;
import com.tigshop.bean.enums.product.CheckStatus;
import com.tigshop.bean.enums.product.ProductType;
import com.tigshop.bean.enums.workbuddy.ProductScope;
import com.tigshop.bean.enums.workbuddy.ProductSortField;
import com.tigshop.bean.model.product.Brand;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.vo.product.ProductPostVO;
import com.tigshop.bean.vo.workbuddy.ProductDetailVO;
import com.tigshop.bean.vo.workbuddy.ProductSearchVO;
import com.tigshop.bean.vo.workbuddy.ProductSummaryVO;
import com.tigshop.bean.vo.workbuddy.ProductWriteResultVO;
import com.tigshop.bean.vo.workbuddy.PromotionLinksVO;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.service.product.BrandService;
import com.tigshop.service.product.CategoryService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.workbuddy.ProductManageService;
import com.tigshop.service.workbuddy.WorkBuddyApiException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.tigshop.bean.dto.product.ProductBatchOperationDTO;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.CheckFieldConstants.PRODUCT_FIELDS;
import static com.tigshop.common.constant.HttpStatus.BAD_REQUEST;

/**
 * 商品列表管理能力的实现。
 *
 * <p>本类是包装层而非透传层。三件事在此完成，不推给上层：数字码译成中文、
 * 易错的参数组合收敛成一个 scope、后台仅存于 UI 的约束补成服务端校验。
 */
@Service
public class ProductManageServiceImpl implements ProductManageService {

    /** 单页上限与 BasePage 一致；结果直接进 AI 上下文，再多也读不完。 */
    private static final int MAX_PAGE_SIZE = 100;

    private static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 允许改值的字段及其中文名。
     *
     * <p>不含 isDelete 与 del：它们语义是删除而非改值，混进来就挂不上各自的前置校验与确认。
     * 也不含 productSku：多规格库存需逐规格给值，不是单值改动。
     */
    private static final Map<String, String> EDITABLE_FIELDS = new LinkedHashMap<>();

    static {
        EDITABLE_FIELDS.put("productName", "商品名称");
        EDITABLE_FIELDS.put("productSn", "商品货号");
        EDITABLE_FIELDS.put("productPrice", "价格");
        EDITABLE_FIELDS.put("productStock", "库存");
        EDITABLE_FIELDS.put("productSku", "多规格库存");
        EDITABLE_FIELDS.put("sortOrder", "排序值");
    }

    private final ProductService productService;

    private final CategoryService categoryService;

    private final BrandService brandService;

    private final TigshopProperties tigshopProperties;
    private final Map<String, BatchSnapshot> batchSnapshots = new ConcurrentHashMap<>();

    public ProductManageServiceImpl(ProductService productService, CategoryService categoryService,
                                    BrandService brandService) {
        this(productService, categoryService, brandService, null);
    }

    @Autowired
    public ProductManageServiceImpl(ProductService productService, CategoryService categoryService,
                                    BrandService brandService, TigshopProperties tigshopProperties) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.brandService = brandService;
        this.tigshopProperties = tigshopProperties;
    }

    @Override
    public ProductSearchVO search(ProductSearchDTO query, Integer shopId) {
        ProductScope scope = ProductScope.parse(query.getScope());
        if (scope == null) {
            throw badRequest("INVALID_SCOPE",
                    "查询范围「" + query.getScope() + "」无法识别，可用的范围有：" + ProductScope.supportedValues());
        }
        ProductSortField sortField = resolveSortField(query.getSortField());
        boolean ascending = "asc".equalsIgnoreCase(StrUtil.trimToEmpty(query.getSortOrder()));

        IPage<ProductListResDTO> page = productService.list(toListQuery(query, scope, sortField, ascending, shopId));

        ProductSearchVO result = new ProductSearchVO();
        result.setItems(toSummaries(page.getRecords(), scope));
        result.setTotal(page.getTotal());
        result.setPage((int) page.getCurrent());
        result.setSize((int) page.getSize());
        result.setScope(scope.getLabel());
        if (sortField != null) {
            result.setSortedBy("按" + sortField.getLabel() + (ascending ? "从低到高" : "从高到低"));
        }
        return result;
    }

    @Override
    public ProductDetailVO detail(Integer productId, Integer shopId) {
        ProductDetailDTO source = productService.detail(requireProductId(productId));
        if (source == null) {
            throw notFound(productId);
        }
        assertOwnedBy(source.getShopId(), shopId, productId);

        ProductDetailVO detail = new ProductDetailVO();
        detail.setProductId(source.getProductId());
        detail.setProductName(source.getProductName());
        detail.setProductSn(source.getProductSn());
        detail.setPrice(source.getProductPrice());
        detail.setMarketPrice(source.getMarketPrice());
        detail.setStock(source.getProductStock());
        detail.setStatus(statusLabel(source.getProductStatus()));
        detail.setAuditStatus(auditLabel(source.getCheckStatus()));
        // 只在未通过时给原因：其余状态下 checkReason 可能残留上一轮驳回的文案，原样带出会误导
        if (Objects.equals(source.getCheckStatus(), CheckStatus.REJECTED.getCode())) {
            detail.setAuditReason(source.getCheckReason());
        }
        detail.setProductType(typeLabel(source.getProductType()));
        // 用 singletonList 而非 List.of：商品可以没有分类，而 List.of 不接受 null
        detail.setCategoryName(categoryNames(Collections.singletonList(source.getCategoryId()))
                .get(source.getCategoryId()));
        detail.setBrandName(brandName(source.getBrandId()));
        detail.setBrief(source.getProductBrief());
        detail.setPicUrl(source.getPicUrl());
        detail.setSales(source.getVirtualSales());
        detail.setSortOrder(source.getSortOrder());
        detail.setRecycled(isTrue(source.getIsDelete()));
        detail.setSkus(toSkus(source.getProductList()));
        return detail;
    }

    @Override
    public PromotionLinksVO promotionLinks(Integer productId, Integer shopId) {
        // 不走 detail()：那条路径会给商品的点击量加一，取个推广链接不该顺带刷浏览数
        Product product = productService.getById(requireProductId(productId));
        if (product == null) {
            ProductDetailDTO detail = productService.detail(productId);
            if (detail == null) throw notFound(productId);
            product = new Product(); product.setProductId(detail.getProductId()); product.setProductName(detail.getProductName()); product.setShopId(detail.getShopId());
        }
        assertOwnedBy(product.getShopId(), shopId, productId);
        ProductPostVO source = productService.post(productId);

        PromotionLinksVO links = new PromotionLinksVO();
        links.setProductId(productId);
        links.setProductName(product.getProductName());
        links.setH5ShortLink(source.getH5ShortLink());
        links.setH5OriginalLink(source.getH5OriginalLink());
        links.setPcShortLink(source.getPcShortLink());
        links.setPcOriginalLink(source.getPcOriginalLink());
        links.setMiniCode(source.getMiniCode());
        links.setWechatShortLink(source.getWechatShortLink());
        // 失败原因如实带出：微信短链依赖公众号配置，失败是常态，静默吞掉会让调用方以为链接可用
        links.setWechatShortLinkError(source.getWechatShortLinkError());
        // 海报由后台前端 Canvas 现场合成，后端不产出图片
        links.setPosterAvailable(false);
        return links;
    }

    @Override
    public ProductWriteResultVO updateField(Integer productId, String field, Object value, Integer shopId) {
        String label = EDITABLE_FIELDS.get(field);
        if (label == null) {
            throw badRequest("FIELD_NOT_EDITABLE", "这里只能改：" + String.join("、", EDITABLE_FIELDS.values())
                    + "。上下架、移入回收站、还原和删除各有专门的操作。");
        }
        if (value == null || StrUtil.isBlank(String.valueOf(value))) {
            throw badRequest("MISSING_VALUE", label + "不能为空");
        }
        Product before = loadOwned(productId, shopId);
        String beforeValue = "productSku".equals(field) ? skuStocks(productService.detail(productId)) : fieldValue(before, field);

        UpdateFieldProductDTO request = new UpdateFieldProductDTO();
        request.setId(productId);
        request.setField(field);
        request.setVal(value);
        productService.updateFieldProduct(request, PRODUCT_FIELDS);

        // 回读而非回显入参：入参与调用方自述必然一致，只有服务端读到的值才能暴露串位
        Product after = productService.getById(productId);
        ProductWriteResultVO result = writeResult(before, label,
                beforeValue, "productSku".equals(field) ? skuStocks(productService.detail(productId)) : fieldValue(after, field));
        result.getConsequences().addAll(updateConsequences(field, after));
        return result;
    }

    @Override
    public ProductWriteResultVO setStatus(Integer productId, boolean onSale, Integer shopId) {
        Product before = loadOwned(productId, shopId);
        if (onSale) {
            assertApproved(before);
        }

        UpdateFieldProductDTO request = new UpdateFieldProductDTO();
        request.setId(productId);
        request.setField("productStatus");
        request.setVal(onSale ? 1 : 0);
        productService.updateFieldProduct(request, PRODUCT_FIELDS);

        ProductWriteResultVO result = writeResult(before, "上下架状态",
                statusLabel(before.getProductStatus()), statusLabel(onSale ? 1 : 0));
        if (onSale) {
            result.getConsequences().add("前台已经能搜到这个商品，买家可以直接下单");
        } else {
            result.getConsequences().add("前台不再显示这个商品，已下单的订单不受影响");
            // B2C 下架会连带清掉拼团活动项，商家事后发现拼团没了会归咎于操作方，必须先说
            if (isB2c()) {
                result.getConsequences().add("这个商品参加的拼团活动也一并撤下了，重新上架后需要重新加入活动");
            }
        }
        return result;
    }

    @Override
    public ProductWriteResultVO recycle(Integer productId, Integer shopId) {
        Product before = loadOwned(productId, shopId);
        if (isTrue(before.getIsDelete())) {
            throw badRequest("ALREADY_RECYCLED", "商品「" + before.getProductName() + "」已经在回收站里了");
        }
        // O2O 形态下 recycle 不会自动下架，在售商品直接回收会留下前台仍可见的中间态
        if (!isB2c() && Objects.equals(before.getProductStatus(), 1)) {
            throw badRequest("MUST_UNLIST_FIRST",
                    "商品「" + before.getProductName() + "」还在出售中，请先下架再移入回收站");
        }

        ProductRecycleDTO request = new ProductRecycleDTO();
        request.setId(Long.valueOf(productId));
        productService.recycle(request);

        ProductWriteResultVO result = writeResult(before, "所在位置", "商品列表", "回收站");
        result.getConsequences().add("前台已经看不到这个商品了，需要的话可以从回收站还原");
        if (isB2c() && Objects.equals(before.getProductStatus(), 1)) {
            result.getConsequences().add("移入回收站的同时也下架了，还原后需要重新上架");
        }
        return result;
    }

    @Override
    public ProductWriteResultVO restore(Integer productId, Integer shopId) {
        Product before = loadOwned(productId, shopId);
        if (!isTrue(before.getIsDelete())) {
            throw badRequest("NOT_RECYCLED", "商品「" + before.getProductName() + "」不在回收站里，不需要还原");
        }

        UpdateFieldProductDTO request = new UpdateFieldProductDTO();
        request.setId(productId);
        request.setField("isDelete");
        request.setVal(0);
        productService.updateFieldProduct(request, PRODUCT_FIELDS);

        ProductWriteResultVO result = writeResult(before, "所在位置", "回收站", "商品列表");
        result.getConsequences().add("商品回到了列表里，但仍是下架状态，需要上架前台才看得到");
        return result;
    }

    @Override
    public ProductWriteResultVO copy(Integer productId, Integer shopId) {
        Product source = loadOwned(productId, shopId);
        productService.copy(copyRequest(productId));

        ProductWriteResultVO result = writeResult(source, "复制商品", source.getProductName(), null);
        Product created = findCopyOf(source);
        if (created == null) {
            // 找不到不等于没成功：复制接口只回文案不回主键，此处只能如实说明
            result.setAfter("已创建，请在商品列表中查看");
            result.getConsequences().add("新商品排在商品列表最前面，名称与原商品相同");
        } else {
            result.setProductId(created.getProductId());
            result.setAfter("新商品「" + created.getProductName() + "」，货号 " + created.getProductSn());
            result.getConsequences().add("新商品是下架状态，改好内容后需要手动上架");
        }
        return result;
    }

    @Override
    public ProductWriteResultVO deletePermanently(Integer productId, Integer shopId) {
        Product before = loadOwned(productId, shopId);
        if (!isTrue(before.getIsDelete())) {
            throw badRequest("NOT_RECYCLED", "商品「" + before.getProductName() + "」需先移入回收站再彻底删除");
        }
        UpdateFieldProductDTO request = new UpdateFieldProductDTO(); request.setId(productId); request.setField("del"); request.setVal(1);
        productService.updateFieldProduct(request, PRODUCT_FIELDS);
        ProductWriteResultVO result = writeResult(before, "彻底删除", "回收站", "已永久删除");
        result.getConsequences().add("商品及其关联数据已删除，无法恢复"); return result;
    }

    @Override
    public com.tigshop.bean.vo.workbuddy.BatchPreviewVO previewBatch(String operation, List<Integer> productIds, Integer shopId) {
        String op = normalizeOperation(operation);
        List<Integer> ids = normalizeIds(productIds);
        if (ids.size() > 50) {
            throw badRequest("BATCH_LIMIT_EXCEEDED", "一次最多操作 50 个商品，请缩小范围或前往后台商品列表使用批量操作");
        }
        List<Product> products = productService.listByIds(ids);
        Map<Integer, Product> byId = products.stream().collect(Collectors.toMap(Product::getProductId, p -> p));
        com.tigshop.bean.vo.workbuddy.BatchPreviewVO preview = new com.tigshop.bean.vo.workbuddy.BatchPreviewVO();
        preview.setOperation(operationLabel(op));
        preview.setIrreversible("del".equals(op));
        for (Integer id : ids) {
            Product product = byId.get(id);
            if (product == null || (shopId != null && shopId > 0 && !Objects.equals(product.getShopId(), shopId))) {
                com.tigshop.bean.vo.workbuddy.BatchPreviewVO.Blocked blocked = new com.tigshop.bean.vo.workbuddy.BatchPreviewVO.Blocked();
                blocked.setProductId(id); blocked.setReason("找不到商品或商品不属于当前店铺"); preview.getBlocked().add(blocked); continue;
            }
            String reason = batchBlockReason(op, product);
            if (reason != null) {
                com.tigshop.bean.vo.workbuddy.BatchPreviewVO.Blocked blocked = new com.tigshop.bean.vo.workbuddy.BatchPreviewVO.Blocked();
                blocked.setProductId(id); blocked.setProductName(product.getProductName()); blocked.setReason(reason); preview.getBlocked().add(blocked);
            } else {
                preview.getAffected().add(summary(product));
            }
        }
        preview.getConsequences().addAll(batchConsequences(op));
        String id = UUID.randomUUID().toString();
        preview.setPreviewId(id);
        batchSnapshots.put(id, new BatchSnapshot(fingerprint(op, ids), op, ids, shopId));
        return preview;
    }

    @Override
    public com.tigshop.bean.vo.workbuddy.BatchResultVO executeBatch(String operation, List<Integer> productIds,
                                                                      String previewId, boolean confirmation, Integer shopId) {
        if (!confirmation) throw badRequest("CONFIRMATION_REQUIRED", "批量操作前必须明确确认");
        if (StrUtil.isBlank(previewId)) throw badRequest("PREVIEW_REQUIRED", "请先预览批量操作，再执行");
        String op = normalizeOperation(operation); List<Integer> ids = normalizeIds(productIds);
        BatchSnapshot snapshot = batchSnapshots.get(previewId);
        if (snapshot == null || !Objects.equals(snapshot.fingerprint, fingerprint(op, ids)) || !Objects.equals(snapshot.shopId, shopId)) {
            throw badRequest("PREVIEW_MISMATCH", "预览已失效或商品清单发生变化，请重新预览");
        }
        if (ids.size() > 50) throw badRequest("BATCH_LIMIT_EXCEEDED", "一次最多操作 50 个商品，请前往后台商品列表操作");
        for (Integer id : ids) loadOwned(id, shopId);
        for (Integer id : ids) {
            String reason = batchBlockReason(op, loadOwned(id, shopId));
            if (reason != null) throw badRequest("BATCH_ITEM_BLOCKED", reason + "；请重新预览并调整商品清单");
        }
        if ("up".equals(op)) ids.forEach(id -> assertApproved(loadOwned(id, shopId)));
        if ("del".equals(op)) ids.forEach(id -> { Product p = loadOwned(id, shopId); if (!isTrue(p.getIsDelete())) throw badRequest("NOT_RECYCLED", "商品「" + p.getProductName() + "」需先移入回收站再删除"); });
        ProductBatchOperationDTO dto = new ProductBatchOperationDTO(); dto.setIds(ids.stream().map(Long::valueOf).toList()); dto.setType(op);
        productService.batchOperation(dto);
        com.tigshop.bean.vo.workbuddy.BatchResultVO result = new com.tigshop.bean.vo.workbuddy.BatchResultVO(); result.setOperation(operationLabel(op)); result.setSuccessCount(ids.size()); result.getConsequences().addAll(batchConsequences(op));
        ids.forEach(id -> { Product p = productService.getById(id); if (p != null) result.getAffected().add(summary(p)); });
        batchSnapshots.remove(previewId);
        return result;
    }

    @Override
    public boolean isProductSnOccupied(String productSn, Integer shopId) {
        if (StrUtil.isBlank(productSn)) {
            return false;
        }
        var query = productService.lambdaQuery().eq(Product::getProductSn, productSn.trim());
        if (shopId != null && shopId > 0) {
            query.eq(Product::getShopId, shopId);
        }
        return query.exists();
    }

    private ProductSummaryVO summary(Product p) { ProductSummaryVO v = new ProductSummaryVO(); v.setProductId(p.getProductId()); v.setProductName(p.getProductName()); v.setProductSn(p.getProductSn()); v.setPrice(p.getProductPrice()); v.setStock(p.getProductStock()); v.setStatus(statusLabel(p.getProductStatus())); v.setAuditStatus(auditLabel(p.getCheckStatus())); v.setProductType(typeLabel(p.getProductType())); v.setCategoryName(categoryNames(Collections.singletonList(p.getCategoryId())).get(p.getCategoryId())); v.setSales(p.getVirtualSales()); v.setRecycled(isTrue(p.getIsDelete())); return v; }
    private String batchBlockReason(String op, Product p) { if ("up".equals(op) && !Objects.equals(p.getCheckStatus(), CheckStatus.APPROVED.getCode())) return "审核未通过或尚未审核，审核通过后才能上架"; if ("del".equals(op) && !isTrue(p.getIsDelete())) return "需先移入回收站再删除"; if ("restore".equals(op) && !isTrue(p.getIsDelete())) return "商品不在回收站，无需还原"; if ("recycle".equals(op) && isTrue(p.getIsDelete())) return "商品已经在回收站"; return null; }
    private static List<String> batchConsequences(String op) { return switch (op) { case "up" -> List.of("这些商品会立即在前台显示并可下单"); case "down" -> List.of("这些商品会从前台隐藏，已下单订单不受影响"); case "recycle" -> List.of("这些商品会移入回收站，之后仍可还原"); case "restore" -> List.of("这些商品会回到商品列表，但保持下架状态"); case "del" -> List.of("这些商品将被彻底删除，数据不可恢复"); default -> List.of(); }; }
    private static String operationLabel(String op) { return switch (op) { case "up" -> "批量上架"; case "down" -> "批量下架"; case "recycle" -> "批量移入回收站"; case "restore" -> "批量还原"; case "del" -> "批量彻底删除"; default -> op; }; }
    private static String normalizeOperation(String operation) { if (operation == null) throw badRequest("INVALID_OPERATION", "缺少批量操作类型"); String op = operation.trim().toLowerCase(); if (!Set.of("up","down","recycle","restore","del").contains(op)) throw badRequest("INVALID_OPERATION", "仅支持 up、down、recycle、restore、del"); return op; }
    private static List<Integer> normalizeIds(List<Integer> ids) { if (ids == null || ids.isEmpty()) throw badRequest("EMPTY_PRODUCT_IDS", "至少选择一个商品"); return ids.stream().filter(Objects::nonNull).distinct().toList(); }
    private static String fingerprint(String op, List<Integer> ids) { return op + ":" + ids.stream().sorted().map(String::valueOf).collect(Collectors.joining(",")); }
    private record BatchSnapshot(String fingerprint, String operation, List<Integer> ids, Integer shopId) {}

    /**
     * 复制接口只返回提示文案，不返回新商品主键。
     *
     * <p>故按同店同名取最新一条回捞。同名商品可能有多个，取主键最大者即刚建的那条。
     */
    private Product findCopyOf(Product source) {
        return productService.lambdaQuery()
                .eq(Product::getProductName, source.getProductName())
                .eq(Product::getShopId, source.getShopId())
                .gt(Product::getProductId, source.getProductId())
                .orderByDesc(Product::getProductId)
                .last("limit 1")
                .one();
    }

    /** 上架前置条件，对应后台上架开关仅在审核通过时可用。 */
    private static void assertApproved(Product product) {
        Integer checkStatus = product.getCheckStatus();
        if (Objects.equals(checkStatus, CheckStatus.APPROVED.getCode())) {
            return;
        }
        if (Objects.equals(checkStatus, CheckStatus.REJECTED.getCode())) {
            throw badRequest("AUDIT_REJECTED", "商品「" + product.getProductName()
                    + "」审核没有通过，不能上架。请先按审核意见修改后重新提交审核。");
        }
        throw badRequest("AUDIT_PENDING", "商品「" + product.getProductName()
                + "」还在等待审核，审核通过后才能上架。");
    }

    private List<String> updateConsequences(String field, Product after) {
        if (after == null) {
            return List.of();
        }
        if ("productPrice".equals(field)) {
            return List.of("前台已经按新价格显示，购物车里的商品下单时也按新价格算");
        }
        if ("productStock".equals(field)) {
            return Objects.equals(after.getProductStock(), 0)
                    ? List.of("库存为 0，前台会显示售罄，买家无法下单")
                    : List.of("前台可售数量已经更新");
        }
        if ("productName".equals(field) || "productSn".equals(field)) {
            return List.of("前台展示已同步更新");
        }
        return List.of();
    }

    private ProductWriteResultVO writeResult(Product product, String changedField, String before, String after) {
        ProductWriteResultVO result = new ProductWriteResultVO();
        result.setProductId(product.getProductId());
        result.setProductName(product.getProductName());
        result.setChangedField(changedField);
        result.setBefore(before);
        result.setAfter(after);
        return result;
    }

    private static String fieldValue(Product product, String field) {
        if (product == null) {
            return null;
        }
        return switch (field) {
            case "productName" -> product.getProductName();
            case "productSn" -> product.getProductSn();
            case "productPrice" -> text(product.getProductPrice());
            case "sortOrder" -> text(product.getSortOrder());
            case "productStock" -> text(product.getProductStock());
            default -> null;
        };
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String skuStocks(ProductDetailDTO detail) {
        if (detail == null || detail.getProductList() == null) return "无规格数据";
        return detail.getProductList().stream()
                .map(s -> StrUtil.blankToDefault(s.getSkuValue(), "规格" + s.getSkuId()) + "：" + s.getSkuStock())
                .collect(Collectors.joining("，"));
    }

    private static ProductCopyDTO copyRequest(Integer productId) {
        ProductCopyDTO request = new ProductCopyDTO();
        request.setProductId(productId);
        return request;
    }

    /** 拼团清理等若干连带行为只发生在非 O2O 形态，措辞须据此区分。 */
    private boolean isB2c() {
        return tigshopProperties == null || tigshopProperties.getIsO2o() == 0;
    }

    /** 写操作统一的取数与归属校验入口；比 detail() 轻，且不会给点击量加一。 */
    private Product loadOwned(Integer productId, Integer shopId) {
        Product product = productService.getById(requireProductId(productId));
        if (product == null) {
            throw notFound(productId);
        }
        assertOwnedBy(product.getShopId(), shopId, productId);
        return product;
    }

    private ProductListDTO toListQuery(ProductSearchDTO query, ProductScope scope,
                                       ProductSortField sortField, boolean ascending, Integer shopId) {
        ProductListDTO listQuery = new ProductListDTO();
        listQuery.setProductStatus(scope.getProductStatus());
        listQuery.setCheckStatus(scope.getCheckStatus());
        listQuery.setIsDelete(scope.getIsDelete());
        listQuery.setKeyword(StrUtil.trimToNull(query.getKeyword()));
        listQuery.setCategoryId(query.getCategoryId());
        listQuery.setBrandId(query.getBrandId());
        listQuery.setProductType(query.getProductType());
        listQuery.setMinPrice(query.getMinPrice());
        listQuery.setMaxPrice(query.getMaxPrice());
        listQuery.setMinStock(query.getMinStock());
        listQuery.setMaxStock(query.getMaxStock());
        listQuery.setMinSales(query.getMinSales());
        listQuery.setMaxSales(query.getMaxSales());
        if (query.getAddedStartTime() != null) {
            listQuery.setAddedStartTime(query.getAddedStartTime().atZone(ZoneId.systemDefault()).toEpochSecond());
        }
        if (query.getAddedEndTime() != null) {
            listQuery.setAddedEndTime(query.getAddedEndTime().atZone(ZoneId.systemDefault()).toEpochSecond());
        }
        if (sortField != null) {
            listQuery.setSortField(sortField.getProperty());
            listQuery.setSortOrder(ascending ? "asc" : "desc");
        }
        listQuery.setPage(query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage());
        listQuery.setSize(normalizeSize(query.getSize()));
        // 与后台列表接口同一规则：店铺身份只看自己的商品，平台身份不加限制
        if (shopId != null && shopId > 0) {
            listQuery.setShopId(shopId);
        }
        return listQuery;
    }

    private List<ProductSummaryVO> toSummaries(List<ProductListResDTO> records, ProductScope scope) {
        if (records == null || records.isEmpty()) {
            return List.of();
        }
        Map<Integer, String> categories = categoryNames(records.stream()
                .map(ProductListResDTO::getCategoryId).toList());
        List<ProductSummaryVO> summaries = new ArrayList<>(records.size());
        for (ProductListResDTO record : records) {
            ProductSummaryVO summary = new ProductSummaryVO();
            summary.setProductId(record.getProductId());
            summary.setProductName(record.getProductName());
            summary.setProductSn(record.getProductSn());
            summary.setPrice(record.getProductPrice());
            summary.setStock(record.getProductStock());
            summary.setStatus(statusLabel(record.getProductStatus()));
            summary.setAuditStatus(auditLabel(record.getCheckStatus()));
            summary.setProductType(typeLabel(record.getProductType()));
            summary.setCategoryName(categories.get(record.getCategoryId()));
            summary.setSkuCount(record.getSkuCount());
            summary.setSales(record.getVirtualSales());
            // 列表接口不回 isDelete，故据查询范围判定：回收站范围内的必然已删除
            summary.setRecycled(scope == ProductScope.RECYCLED);
            summaries.add(summary);
        }
        return summaries;
    }

    private List<ProductDetailVO.Sku> toSkus(List<ProductSkuDTO> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        List<ProductDetailVO.Sku> skus = new ArrayList<>(source.size());
        for (ProductSkuDTO item : source) {
            ProductDetailVO.Sku sku = new ProductDetailVO.Sku();
            sku.setSkuId(item.getSkuId());
            sku.setSpecs(item.getSkuValue());
            sku.setSkuSn(item.getSkuSn());
            sku.setPrice(toPrice(item.getSkuPrice()));
            sku.setStock(item.getSkuStock());
            skus.add(sku);
        }
        return skus;
    }

    /** 批量取名，避免逐条查库；列表一页最多 100 条，逐条查会打出 100 次查询。 */
    private Map<Integer, String> categoryNames(Collection<Integer> categoryIds) {
        Set<Integer> ids = categoryIds.stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (ids.isEmpty()) {
            // 不能用 Map.of()：商品可以没有分类，而不可变 Map 连以 null 为键查询都会抛
            return Collections.emptyMap();
        }
        return categoryService.listByIds(ids).stream()
                .filter(category -> category.getCategoryName() != null)
                .collect(Collectors.toMap(Category::getCategoryId, Category::getCategoryName,
                        (first, second) -> first));
    }

    private String brandName(Integer brandId) {
        if (brandId == null || brandId <= 0) {
            return null;
        }
        Brand brand = brandService.getById(brandId);
        return brand == null ? null : brand.getBrandName();
    }

    private ProductSortField resolveSortField(String raw) {
        if (StrUtil.isBlank(raw)) {
            return null;
        }
        ProductSortField field = ProductSortField.parse(raw);
        if (field == null) {
            // 底层对无法识别的排序字段静默回退主键，放行等于给出一份错序的结果且无人察觉
            throw badRequest("INVALID_SORT_FIELD",
                    "排序字段「" + raw + "」不支持，可用的有：" + ProductSortField.supportedValues());
        }
        return field;
    }

    private static int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    /** 与后台页签一致的措辞，而非枚举里的「上架/下架」——商家在屏幕上看到的是「出售中」。 */
    private static String statusLabel(Integer code) {
        if (code == null) {
            return null;
        }
        return code == 1 ? "出售中" : "已下架";
    }

    private static String auditLabel(Integer code) {
        if (code == null) {
            return null;
        }
        for (CheckStatus status : CheckStatus.values()) {
            if (status.getCode() == code) {
                return status.getDescription();
            }
        }
        return null;
    }

    private static String typeLabel(Integer code) {
        if (code == null) {
            return null;
        }
        ProductType type = ProductType.fromCode(code);
        return type == null ? null : type.getDescription();
    }

    private static boolean isTrue(Integer flag) {
        return flag != null && flag == 1;
    }

    /** SKU 价格在源 DTO 中是字符串；脏数据不该让整个详情查询失败，故解析不了就留空。 */
    private static BigDecimal toPrice(String raw) {
        if (StrUtil.isBlank(raw)) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer requireProductId(Integer productId) {
        if (productId == null || productId <= 0) {
            throw badRequest("INVALID_PRODUCT_ID", "缺少商品ID");
        }
        return productId;
    }

    /**
     * 店铺身份只能操作自己的商品。
     *
     * <p>列表查询靠 shopId 条件天然隔离，而详情按主键直取，不校验就能跨店读到别人的商品。
     */
    private static void assertOwnedBy(Integer productShopId, Integer shopId, Integer productId) {
        if (shopId != null && shopId > 0 && !Objects.equals(productShopId, shopId)) {
            throw notFound(productId);
        }
    }

    private static WorkBuddyApiException notFound(Integer productId) {
        return badRequest("PRODUCT_NOT_FOUND", "找不到商品（ID " + productId + "），可能已被彻底删除或不属于当前店铺");
    }

    private static WorkBuddyApiException badRequest(String code, String message) {
        return new WorkBuddyApiException(code, message, BAD_REQUEST, false);
    }
}
