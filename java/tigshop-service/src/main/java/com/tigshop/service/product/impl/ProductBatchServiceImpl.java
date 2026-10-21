package com.tigshop.service.product.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.tigshop.bean.dto.product.ProductBatchDealDTO;
import com.tigshop.bean.dto.product.ProductBatchEditDTO;
import com.tigshop.bean.dto.product.ProductDescDTO;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.enums.product.CheckStatus;
import com.tigshop.bean.enums.product.ProductStatus;
import com.tigshop.bean.enums.product.ProductType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductAttribute;
import com.tigshop.bean.model.product.ProductGallery;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.setting.ShippingTpl;
import com.tigshop.bean.vo.product.ProductBatchModifyVO;
import com.tigshop.bean.vo.product.ProductImportExcelPrueVO;
import com.tigshop.bean.vo.product.ProductImportExcelVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.ExcelUtils;
import com.tigshop.common.utils.SpecImportUtils;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.*;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.ShippingTplService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.product.ProductConstants.PRODUCT_SN_EXIST;

/**
 * 产品批量操作 服务实现
 *
 * @author kidd
 * @since 2025/3/26 16:17
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ProductBatchServiceImpl extends BaseServiceImpl<ProductMapper, Product> implements ProductBatchService {
    private static final String TEMPLATE_MERCHANT = "template/多商户商品导入模板.xlsx";
    private static final String TEMPLATE_O2O = "template/o2o商品导入模板.xlsx";
    private static final String DOWNLOAD_NAME_MERCHANT = "商品导入模板.xlsx";
    private static final String DOWNLOAD_NAME_O2O = "o2o商品导入模板.xlsx";

    private static final int MAX_PRODUCT_SN_LEN = 50;
    private static final int MAX_PRODUCT_NAME_LEN = 200;
    private static final int MAX_SKU_SN_LEN = 60;
    private static final String MARKET_PRICE_RATE_DEFAULT = "1.2";

    private static final Pattern SHIPPING_TPL_NAME_PATTERN = Pattern.compile("【([^【】]*)】");

    private final CategoryService categoryService;
    private final ProductService productService;
    private final ConfigService configService;
    private final ProductGalleryService productGalleryService;
    private final ProductSkuService productSkuService;
    private final ProductAttributesService productAttributesService;
    private final ShippingTplService shippingTplService;


    private final TigshopProperties tigshopProperties;


    @Override
    public void downloadTemplate(HttpServletResponse response) {
        boolean o2o = Integer.valueOf(1).equals(tigshopProperties.getIsO2o());
        String classpath = o2o ? TEMPLATE_O2O : TEMPLATE_MERCHANT;
        String downloadName = o2o ? DOWNLOAD_NAME_O2O : DOWNLOAD_NAME_MERCHANT;
        String encoded = URLEncoder.encode(downloadName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded);
        ClassPathResource resource = new ClassPathResource(classpath);
        try (InputStream in = resource.getInputStream(); OutputStream out = response.getOutputStream()) {
            in.transferTo(out);
            out.flush();
        } catch (IOException e) {
            log.error("下载商品导入模板失败 classpath={}", classpath, e);
            throw new GlobalException("商品导入模板文件不存在或读取失败");
        }
    }

    @Override
    public List<ProductImportExcelVO> productBatchDeal(ProductBatchDealDTO dto) {
        List<Integer> rangeIds = StrUtil.isBlank(dto.getRangeIds()) ? List.of() : Arrays.stream(dto.getRangeIds().split(",")).map(Integer::valueOf).toList();
        Integer shopId = getShopId();
        List<Product> products = new ArrayList<>();

        boolean isStore = shopId != null && shopId > 0;
        List<Integer> platformProductIds = List.of();
        List<Integer> finalPlatformProductIds = platformProductIds;

        if (dto.getDealRange() == 1) {
            List<Integer> allCateGoryIds = new ArrayList<>();
            if (CollUtil.isNotEmpty(rangeIds)) {
                rangeIds.forEach(rangeId -> {
                    List<Integer> categoryAllChildIds = categoryService.getCategoryAllChildIds(rangeId);
                    allCateGoryIds.addAll(categoryAllChildIds);
                });
            }
            if (CollUtil.isNotEmpty(allCateGoryIds)) {
                List<Integer> categoryIds = allCateGoryIds.stream().distinct().toList();
                products = this.lambdaQuery()
                        .in(Product::getCategoryId, categoryIds)
                        .and(isStore, w -> {
                            w.eq(Product::getShopId, shopId);
                            if (CollUtil.isNotEmpty(finalPlatformProductIds)) {
                                w.or(o -> o.eq(Product::getShopId, 0).in(Product::getProductId, finalPlatformProductIds));
                            }
                        })
                        .eq(Product::getIsDelete, Constants.NO)
                        .eq(Product::getCheckStatus, CheckStatus.APPROVED.getCode())
                        .eq(Product::getProductStatus, ProductStatus.ON_SALE.getCode())
                        .list();
            }

        } else if (dto.getDealRange() == 2) {
            products = this.lambdaQuery()
                    .in(CollUtil.isNotEmpty(rangeIds), Product::getBrandId, rangeIds)
                    .and(isStore, w -> {
                        w.eq(Product::getShopId, shopId);
                        if (CollUtil.isNotEmpty(finalPlatformProductIds)) {
                            w.or(o -> o.eq(Product::getShopId, 0).in(Product::getProductId, finalPlatformProductIds));
                        }
                    })
                    .eq(Product::getIsDelete, Constants.NO)
                    .eq(Product::getCheckStatus, CheckStatus.APPROVED.getCode())
                    .eq(Product::getProductStatus, ProductStatus.ON_SALE.getCode())
                    .list();
        } else if (dto.getDealRange() == 3) {
            products = this.lambdaQuery()
                    .in(CollUtil.isNotEmpty(rangeIds), Product::getProductId, rangeIds)
                    .and(isStore, w -> {
                        w.eq(Product::getShopId, shopId);
                        if (CollUtil.isNotEmpty(finalPlatformProductIds)) {
                            w.or(o -> o.eq(Product::getShopId, 0).in(Product::getProductId, finalPlatformProductIds));
                        }
                    })
                    .eq(Product::getIsDelete, Constants.NO)
                    .eq(Product::getCheckStatus, CheckStatus.APPROVED.getCode())
                    .eq(Product::getProductStatus, ProductStatus.ON_SALE.getCode())
                    .list();
        } else {
            products = this.lambdaQuery()
                    .notIn(CollUtil.isNotEmpty(rangeIds), Product::getProductId, rangeIds)
                    .and(isStore, w -> {
                        w.eq(Product::getShopId, shopId);
                        if (CollUtil.isNotEmpty(finalPlatformProductIds)) {
                            w.or(o -> o.eq(Product::getShopId, 0).in(Product::getProductId, finalPlatformProductIds));
                        }
                    })
                    .eq(Product::getIsDelete, Constants.NO)
                    .eq(Product::getCheckStatus, CheckStatus.APPROVED.getCode())
                    .eq(Product::getProductStatus, ProductStatus.ON_SALE.getCode())
                    .list();
        }

        Map<Integer, Category> catById = Map.of();
        if (CollUtil.isNotEmpty(products)) {
            List<Category> allCategories = categoryService.lambdaQuery().list();
            catById = allCategories.stream().collect(Collectors.toMap(Category::getCategoryId, c -> c, (a, b) -> a));
        }

        List<Integer> productIds = products.stream().map(Product::getProductId).toList();

        Map<Integer, List<ProductSkuDTO>> productSkuMap = Map.of();
        if (CollUtil.isNotEmpty(productIds)) {
            List<ProductSkuDTO> skuList = productSkuService.getProductSkusByProductIds(productIds);
            productSkuMap = skuList.stream().collect(Collectors.groupingBy(ProductSkuDTO::getProductId));
        }

        Map<String, String> specPicDict = loadSpecPicDict(productIds);

        boolean isO2o = tigshopProperties.getIsO2o() == 1;
        List<ProductImportExcelVO> result = new ArrayList<>();
        for (Product p : products) {
            String catPath = buildCategoryPath(p.getCategoryId(), catById);
            List<ProductSkuDTO> skuList = productSkuMap.get(p.getProductId());


            if (CollUtil.isNotEmpty(skuList)) {
                for (ProductSkuDTO sku : skuList) {
                    ProductImportExcelVO vo = ProductImportExcelVO.valueOfExport(p, catPath, sku);
                    vo.setSpecPicUrl(resolveExportSpecPic(specPicDict, p.getProductId(), vo.getSpecName()));
                    result.add(vo);
                }
            } else {
                ProductImportExcelVO vo = ProductImportExcelVO.valueOfExport(p, catPath, null);
                result.add(vo);
            }
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ProductBatchModifyVO productBatchModify(Integer isAutoCat, Integer isAutoBrand, Integer isChangePic, Integer isUpload, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new GlobalException("请上传文件");
        }

        List<ProductImportExcelPrueVO> rawList;
        try {
            rawList = new ExcelUtils<ProductImportExcelPrueVO>(file, ProductImportExcelPrueVO.class).importExcel();
        } catch (Exception e) {
            log.warn("读取Excel失败", e);
            throw new GlobalException("读取Excel文件失败");
        }

        if (CollUtil.isEmpty(rawList)) {
            return new ProductBatchModifyVO(0, "请上传有数据的文件");
        }

        List<ProductImportExcelVO> allRows = new ArrayList<>();
        for (ProductImportExcelPrueVO raw : rawList) {
            if (isNonDataRow(raw)) {
                continue;
            }
            ProductImportExcelVO vo = new ProductImportExcelVO();
            BeanUtil.copyProperties(raw, vo);
            allRows.add(vo);
        }

        if (CollUtil.isEmpty(allRows)) {
            return new ProductBatchModifyVO(0, "上传完成");
        }

        Integer shopId = getShopId();
        boolean isStore = shopId != null && shopId > 0;
        if (isStore) {
            isAutoCat = 0;
        }

        boolean shopProductNeedCheck = false;
        if (isStore) {
            var cfg = configService.getConfigByCode(SettingsEnum.SHOP_PRODUCT_NEED_CHECK.getBizCode());
            String val = cfg != null ? cfg.getBizVal() : null;
            shopProductNeedCheck = "1".equals(val);
        }

        String marketPriceRate = MARKET_PRICE_RATE_DEFAULT;
        var rateCfg = configService.getConfigByCode(SettingsEnum.MARKET_PRICE_RATE.getBizCode());
        if (rateCfg != null && StrUtil.isNotBlank(rateCfg.getBizVal())) {
            marketPriceRate = rateCfg.getBizVal().trim();
        }
        if (StrUtil.isBlank(marketPriceRate)) {
            marketPriceRate = MARKET_PRICE_RATE_DEFAULT;
        }

        StringBuilder msg = new StringBuilder();
        LinkedHashMap<String, List<ProductImportExcelVO>> grouped = new LinkedHashMap<>();

        int rowIndex = 0;
        for (ProductImportExcelVO row : allRows) {
            rowIndex++;
            String sn = StrUtil.trimToEmpty(row.getProductSn());
            String name = StrUtil.trimToEmpty(row.getProductName());
            if (sn.length() > MAX_PRODUCT_SN_LEN) {
                throw new GlobalException("第" + rowIndex + "行：商品编码超过" + MAX_PRODUCT_SN_LEN + "个字符");
            }
            String key;
            if (StrUtil.isNotBlank(sn)) {
                key = "SN:" + sn;
            } else if (StrUtil.isNotBlank(name)) {
                key = "NAME:" + name;
            } else {
                throw new GlobalException("第" + rowIndex + "行：商品编码和商品名称均为空");
            }
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }

        for (Map.Entry<String, List<ProductImportExcelVO>> e : grouped.entrySet()) {
            if (e.getKey().startsWith("NAME:")) {
                String newSn = productService.createNewProductSn();
                for (ProductImportExcelVO r : e.getValue()) {
                    r.setProductSn(newSn);
                }
            }
        }

        Map<String, Long> shippingTplDict = null;
        int groupIndex = 0;
        int count = 0;

        for (Map.Entry<String, List<ProductImportExcelVO>> entry : grouped.entrySet()) {
            groupIndex++;
            List<ProductImportExcelVO> rows = entry.getValue();
            if (CollUtil.isEmpty(rows)) {
                continue;
            }
            ProductImportExcelVO first = rows.get(0);
            String productSn = StrUtil.trimToEmpty(first.getProductSn());

            String missing = validateRequiredFields(first, rows);
            if (missing != null) {
                throw new GlobalException("第" + groupIndex + "组(" + productSn + ")：" + missing + "为空");
            }

            if (shippingTplDict == null && rows.stream().anyMatch(r -> StrUtil.contains(r.getShippingTpl(), "【"))) {
                shippingTplDict = loadShippingTplDict(shopId);
            }

            Product exist = this.lambdaQuery().eq(Product::getProductSn, productSn).one();
            if (exist != null) {
                if (!isOwnerMatch(exist, shopId)) {
                    throw new GlobalException("第" + groupIndex + "组(" + productSn + ")：编码已被其他店铺占用");
                }
                int appended = handleExistingProduct(exist, rows, first, isChangePic, shippingTplDict, msg, groupIndex, productSn, marketPriceRate);
                if (appended > 0) {
                    count++;
                }
            } else {
                boolean ok = handleNewProduct(rows, first, isAutoCat, shippingTplDict, shopId, shopProductNeedCheck, msg, groupIndex, productSn, marketPriceRate);
                if (ok) {
                    count++;
                }
            }
        }

        String resultMsg = msg.isEmpty() ? "上传完成" : msg.toString();
        return new ProductBatchModifyVO(count, resultMsg);
    }

    private boolean handleNewProduct(List<ProductImportExcelVO> rows, ProductImportExcelVO first, Integer isAutoCat,
                                     Map<String, Long> shippingTplDict, Integer shopId,
                                     boolean shopProductNeedCheck, StringBuilder msg, int groupIndex, String productSn,
                                     String marketPriceRate) {
        LinkedHashSet<String> galleryUrls = collectGalleryUrls(rows);
        LinkedHashSet<String> descPicUrls = collectDescPicUrls(rows);

        String[] categoryNames = first.getCategoryName().trim().split("[/|]");
        Integer categoryId = categoryService.getCategoryIds(categoryNames, isAutoCat);
        if (categoryId == null || categoryId == 0) {
            msg.append("第").append(groupIndex).append("组(").append(productSn).append(")：分类不存在，已跳过；");
            return false;
        }

        boolean multiSku = rows.size() > 1 || CollUtil.isNotEmpty(SpecImportUtils.parseSpecPairs(first.getSpecName()));

        String firstPic = galleryUrls.isEmpty() ? "" : galleryUrls.iterator().next();
        int itemShopId = resolveShopId(shopId);

        Product product = Product.builder()
                .productSn(productSn)
                .productName(first.getProductName())
                .productTsn(StrUtil.trimToNull(first.getProductTsn()))
                .categoryId(categoryId)
                .productStatus(parseInteger(first.getProductStatus(), 1))
                .checkStatus(CheckStatus.APPROVED.getCode())
                .brandId(0)
                .keywords(first.getKeywords())
                .productBrief(first.getProductBrief())
                .shopId(itemShopId)
                .picUrl(firstPic)
                .picThumb(firstPic)
                .picOriginal(firstPic)
                .productWeight(BigDecimal.ZERO)
                .fixedShippingType(0)
                .build();

        if (shopProductNeedCheck) {
            product.setProductStatus(ProductStatus.OFF_SALE.getCode());
            product.setCheckStatus(CheckStatus.PENDING.getCode());
        }

        ShippingTypeFlags flags = parseShippingType(first.getShippingType());
        if (!flags.hasAny()) {
            flags = ShippingTypeFlags.defaults();
        }
        applyShippingFlags(product, flags);

        if (StrUtil.isNotBlank(first.getShippingTpl())) {
            ShippingTplResolution res = resolveShippingTpl(StrUtil.trim(first.getShippingTpl()), shippingTplDict);
            if (res.warning != null) {
                msg.append("第").append(groupIndex).append("组：").append(res.warning).append("；");
            }
            applyShippingTpl(product, res);
        }

        BigDecimal rateBd = parseBigDecimalSafe(marketPriceRate, new BigDecimal(MARKET_PRICE_RATE_DEFAULT));

        String productDescHtml = buildProductDescHtml(descPicUrls);
        if (StrUtil.isNotBlank(productDescHtml)) {
            product.setProductDesc(productDescHtml);
        }

        if (!multiSku) {
            BigDecimal productPrice = parseBigDecimal(first.getProductPrice());
            int stock = parseInteger(first.getProductStock(), 0);
            BigDecimal marketPrice = computeMarketPrice(first.getMarketPrice(), productPrice, rateBd);
            BigDecimal costPrice = parseBigDecimal(first.getCostPrice());
            product.setProductPrice(productPrice);
            product.setProductStock(stock);
            product.setMarketPrice(marketPrice);
            product.setCostPrice(costPrice);
            this.save(product);
        } else {
            product.setProductPrice(BigDecimal.ZERO);
            product.setMarketPrice(BigDecimal.ZERO);
            product.setProductStock(0);
            this.save(product);

            List<ProductSku> skuList = new ArrayList<>();
            List<List<String[]>> specPairsList = new ArrayList<>();
            // 同规格名下规格值允许重复，组内以规格编码(skuSn)唯一
            LinkedHashSet<String> seenSkuSns = new LinkedHashSet<>();
            for (ProductImportExcelVO row : rows) {
                String skuSn = StrUtil.trimToEmpty(row.getSkuSn());
                if (StrUtil.isNotBlank(skuSn)) {
                    validateSkuSn(skuSn, seenSkuSns, groupIndex, productSn);
                }
                List<String[]> pairs = SpecImportUtils.parseSpecPairs(row.getSpecName());
                String skuValue = SpecImportUtils.buildSkuValue(pairs);
                BigDecimal skuPrice = parseBigDecimal(row.getProductPrice());
                int skuStock = parseInteger(row.getProductStock(), 0);
                BigDecimal skuMarket = computeMarketPrice(row.getMarketPrice(), skuPrice, rateBd);
                BigDecimal skuCost = parseBigDecimal(row.getCostPrice());

                ProductSku sku = ProductSku.builder()
                        .productId(product.getProductId())
                        .skuValue(skuValue)
                        .skuData(buildSkuDataJson(pairs))
                        .skuSn(skuSn)
                        // 规格条码选填，未填按 sku_tsn 的列约定存空字符串
                        .skuTsn(StrUtil.trimToEmpty(row.getSkuTsn()))
                        .skuPrice(skuPrice)
                        .skuStock(skuStock)
                        .marketPrice(skuMarket)
                        .costPrice(skuCost)
                        .build();
                skuList.add(sku);
                specPairsList.add(pairs);
            }
            if (skuList.isEmpty()) {
                this.removeById(product.getProductId());
                return false;
            }
            List<ProductSku> importedSkus = new ArrayList<>(skuList);
            fillMissingSkuCombinations(product.getProductId(), skuList, specPairsList);
            productSkuService.saveBatch(skuList);
            generateMissingSkuSns(productSn, importedSkus);
            saveSpecAttributesNew(product.getProductId(), skuList, specPairsList);
            applySpecPics(product.getProductId(), collectSpecPics(rows), true);

            List<ProductSku> all = productSkuService.lambdaQuery().eq(ProductSku::getProductId, product.getProductId()).list();
            product.setProductPrice(minSkuPrice(all));
            product.setProductStock(sumSkuStock(all));
            product.setMarketPrice(minSkuMarketPrice(all, rateBd));
            this.updateById(product);
        }

        saveProductGalleries(product.getProductId(), galleryUrls);
        return true;
    }

    private int handleExistingProduct(Product exist, List<ProductImportExcelVO> rows, ProductImportExcelVO first,
                                      Integer isChangePic, Map<String, Long> shippingTplDict, StringBuilder msg,
                                      int groupIndex, String productSn, String marketPriceRate) {
        long skuCnt = productSkuService.lambdaQuery().eq(ProductSku::getProductId, exist.getProductId()).count();
        if (skuCnt == 0L) {
            msg.append("第").append(groupIndex).append("组(").append(productSn).append(")：原商品为统一规格，不支持追加 SKU；");
            return 0;
        }

        boolean dirty = false;

        ShippingTypeFlags flags = parseShippingType(first.getShippingType());
        if (flags.hasAny()) {
            applyShippingFlags(exist, flags);
            dirty = true;
        }
        if (StrUtil.isNotBlank(first.getShippingTpl())) {
            ShippingTplResolution res = resolveShippingTpl(StrUtil.trim(first.getShippingTpl()), shippingTplDict);
            if (res.warning != null) {
                msg.append("第").append(groupIndex).append("组：").append(res.warning).append("；");
            }
            applyShippingTpl(exist, res);
            dirty = true;
        }

        BigDecimal rateBd = parseBigDecimalSafe(marketPriceRate, new BigDecimal(MARKET_PRICE_RATE_DEFAULT));

        List<ProductSku> newSkuList = new ArrayList<>();
        List<List<String[]>> specPairsList = new ArrayList<>();
        List<ProductSku> existingSkus = productSkuService.lambdaQuery()
                .eq(ProductSku::getProductId, exist.getProductId()).list();
        // 同规格名下规格值允许重复，追加时以规格编码(skuSn)唯一（含本商品已有 SKU）
        LinkedHashSet<String> seenSkuSns = existingSkus.stream()
                .map(ProductSku::getSkuSn)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        for (ProductImportExcelVO row : rows) {
            String skuSn = StrUtil.trimToEmpty(row.getSkuSn());
            if (StrUtil.isNotBlank(skuSn)) {
                validateSkuSn(skuSn, seenSkuSns, groupIndex, productSn);
            }
            List<String[]> pairs = SpecImportUtils.parseSpecPairs(row.getSpecName());
            String skuValue = SpecImportUtils.buildSkuValue(pairs);
            BigDecimal skuPrice = parseBigDecimal(row.getProductPrice());
            int skuStock = parseInteger(row.getProductStock(), 0);
            BigDecimal skuMarket = computeMarketPrice(row.getMarketPrice(), skuPrice, rateBd);
            BigDecimal skuCost = parseBigDecimal(row.getCostPrice());
            ProductSku sku = ProductSku.builder()
                    .productId(exist.getProductId())
                    .skuValue(skuValue)
                    .skuData(buildSkuDataJson(pairs))
                    .skuSn(skuSn)
                    // 规格条码选填，未填按 sku_tsn 的列约定存空字符串
                    .skuTsn(StrUtil.trimToEmpty(row.getSkuTsn()))
                    .skuPrice(skuPrice)
                    .skuStock(skuStock)
                    .marketPrice(skuMarket)
                    .costPrice(skuCost)
                    .build();
            newSkuList.add(sku);
            specPairsList.add(pairs);
        }

        if (CollUtil.isNotEmpty(newSkuList)) {
            List<ProductSku> importedSkus = new ArrayList<>(newSkuList);
            fillMissingSkuCombinations(exist.getProductId(), newSkuList, specPairsList);
            productSkuService.saveBatch(newSkuList);
            generateMissingSkuSns(productSn, importedSkus);
            appendSpecAttributes(exist.getProductId(), newSkuList, specPairsList);
            List<ProductSku> all = productSkuService.lambdaQuery().eq(ProductSku::getProductId, exist.getProductId()).list();
            exist.setProductPrice(minSkuPrice(all));
            exist.setProductStock(sumSkuStock(all));
            exist.setMarketPrice(minSkuMarketPrice(all, rateBd));
            dirty = true;
        }

        // 规格图独立于商品图册/详情图：列有值则覆盖同规格值已有图，留空则保留（不看 isChangePic）
        applySpecPics(exist.getProductId(), collectSpecPics(rows), true);

        if (Integer.valueOf(1).equals(isChangePic)) {
            LinkedHashSet<String> urls = collectGalleryUrls(rows);
            if (CollUtil.isNotEmpty(urls)) {
                productGalleryService.lambdaUpdate().eq(ProductGallery::getProductId, exist.getProductId()).remove();
                saveProductGalleries(exist.getProductId(), urls);
                String fp = urls.iterator().next();
                exist.setPicUrl(fp);
                exist.setPicThumb(fp);
                exist.setPicOriginal(fp);
                dirty = true;
            }
            LinkedHashSet<String> descPicUrls = collectDescPicUrls(rows);
            if (CollUtil.isNotEmpty(descPicUrls)) {
                String productDescHtml = buildProductDescHtml(descPicUrls);
                if (StrUtil.isNotBlank(productDescHtml)) {
                    exist.setProductDesc(productDescHtml);
                    dirty = true;
                }
            }
        }

        if (dirty) {
            this.updateById(exist);
        }
        return newSkuList.size();
    }

    private void saveProductGalleries(Integer productId, LinkedHashSet<String> galleryUrls) {
        int i = 0;
        for (String imageUrl : galleryUrls) {
            i++;
            ProductGallery g = ProductGallery.builder()
                    .productId(productId)
                    .picUrl(imageUrl)
                    .picThumb(imageUrl)
                    .picLarge(imageUrl)
                    .picOriginal(imageUrl)
                    .sortOrder(i)
                    .build();
            productGalleryService.save(g);
        }
    }

    private void saveSpecAttributesNew(Integer productId, List<ProductSku> skuList, List<List<String[]>> specPairsList) {
        // 与保存/更新一致：不按“规格值”去重；以规格编码(skuSn)区分，编码不同的每个 SKU 各自贡献规格值。
        // 跳过无编码的补全占位行与重复编码，避免笛卡尔占位污染规格池。
        LinkedHashSet<String> seenSkuSns = new LinkedHashSet<>();
        List<ProductAttribute> attrs = new ArrayList<>();
        for (int i = 0; i < specPairsList.size(); i++) {
            String skuSn = i < skuList.size() ? StrUtil.trimToEmpty(skuList.get(i).getSkuSn()) : "";
            if (StrUtil.isBlank(skuSn) || !seenSkuSns.add(skuSn)) {
                continue;
            }
            for (String[] pv : specPairsList.get(i)) {
                if (pv.length < 2) {
                    continue;
                }
                attrs.add(ProductAttribute.builder()
                        .productId(productId)
                        .attrType(1)
                        .attrName(pv[0])
                        .attrValue(pv[1])
                        .build());
            }
        }
        if (CollUtil.isNotEmpty(attrs)) {
            productAttributesService.saveBatch(attrs);
        }
    }

    private void appendSpecAttributes(Integer productId, List<ProductSku> skuList, List<List<String[]>> specPairsList) {
        // 与保存/更新一致：不按“规格值”去重；以规格编码(skuSn)区分，本次追加中编码不同的每个 SKU 各自贡献规格值。
        // 跳过无编码的补全占位行与重复编码，避免笛卡尔占位污染规格池。
        LinkedHashSet<String> seenSkuSns = new LinkedHashSet<>();
        List<ProductAttribute> toAdd = new ArrayList<>();
        for (int i = 0; i < specPairsList.size(); i++) {
            String skuSn = i < skuList.size() ? StrUtil.trimToEmpty(skuList.get(i).getSkuSn()) : "";
            if (StrUtil.isBlank(skuSn) || !seenSkuSns.add(skuSn)) {
                continue;
            }
            for (String[] pv : specPairsList.get(i)) {
                if (pv.length < 2) {
                    continue;
                }
                toAdd.add(ProductAttribute.builder()
                        .productId(productId)
                        .attrType(1)
                        .attrName(pv[0])
                        .attrValue(pv[1])
                        .build());
            }
        }
        if (CollUtil.isNotEmpty(toAdd)) {
            productAttributesService.saveBatch(toAdd);
        }
    }

    private String buildSkuDataJson(List<String[]> pairs) {
        List<Map<String, String>> list = new ArrayList<>();
        for (String[] pv : pairs) {
            if (pv.length < 2) {
                continue;
            }
            Map<String, String> m = new HashMap<>();
            m.put("name", pv[0]);
            m.put("value", pv[1]);
            list.add(m);
        }
        return JSON.toJSONString(list);
    }

    private BigDecimal minSkuPrice(List<ProductSku> all) {
        return all.stream()
                .map(ProductSku::getSkuPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private int sumSkuStock(List<ProductSku> all) {
        return all.stream()
                .mapToInt(s -> s.getSkuStock() != null ? s.getSkuStock() : 0)
                .sum();
    }

    private BigDecimal minSkuMarketPrice(List<ProductSku> all, BigDecimal rateBd) {
        Optional<BigDecimal> min = all.stream()
                .map(ProductSku::getMarketPrice)
                .filter(Objects::nonNull)
                .filter(v -> v.compareTo(BigDecimal.ZERO) > 0)
                .min(BigDecimal::compareTo);
        if (min.isPresent()) {
            return min.get();
        }
        BigDecimal mp = minSkuPrice(all);
        return mp.multiply(rateBd).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeMarketPrice(String marketCell, BigDecimal basePrice, BigDecimal rateBd) {
        if (StrUtil.isBlank(marketCell)) {
            return basePrice.multiply(rateBd).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal m = parseBigDecimal(marketCell);
        if (m.compareTo(BigDecimal.ZERO) <= 0) {
            return basePrice.multiply(rateBd).setScale(2, RoundingMode.HALF_UP);
        }
        return m;
    }

    private BigDecimal parseBigDecimalSafe(String s, BigDecimal def) {
        try {
            if (StrUtil.isBlank(s)) {
                return def;
            }
            return new BigDecimal(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static int resolveShopId(Integer shopId) {
        return shopId != null && shopId > 0 ? shopId : 0;
    }

    private String validateRequiredFields(ProductImportExcelVO first, List<ProductImportExcelVO> rows) {
        LinkedHashSet<String> m = new LinkedHashSet<>();
        if (StrUtil.isBlank(first.getProductName())) {
            m.add("商品名称");
        } else if (first.getProductName().length() > MAX_PRODUCT_NAME_LEN) {
            m.add("商品名称超过" + MAX_PRODUCT_NAME_LEN + "个字符");
        }
        if (StrUtil.isBlank(first.getPicUrl())) {
            m.add("商品图片");
        }
        if (StrUtil.isBlank(first.getCategoryName())) {
            m.add("商品分类");
        }
        if (StrUtil.isBlank(first.getShippingType())) {
            m.add("配送方式");
        } else if (parseShippingType(first.getShippingType()).logistics
                && StrUtil.isBlank(first.getShippingTpl())) {
            m.add("快递运费模板");
        }
        for (ProductImportExcelVO r : rows) {
            if (StrUtil.isBlank(r.getProductPrice())) {
                m.add("商品售价");
                break;
            }
        }
        for (ProductImportExcelVO r : rows) {
            if (StrUtil.isBlank(r.getProductStock())) {
                m.add("库存");
                break;
            }
        }
        return m.isEmpty() ? null : String.join("、", m);
    }

    private boolean isOwnerMatch(Product exist, Integer myShopId) {
        if (myShopId == null || myShopId == 0) {
            return exist.getShopId() == null || exist.getShopId() == 0;
        }
        return myShopId.equals(exist.getShopId());
    }

    private boolean isNonDataRow(ProductImportExcelPrueVO raw) {
        String sn = StrUtil.trimToEmpty(raw.getProductSn());
        if (sn.contains("示例数据") || sn.contains("规则说明")
                || sn.startsWith("1，") || sn.startsWith("2，")
                || sn.startsWith("3，") || sn.startsWith("4，")) {
            return true;
        }
        return StrUtil.isBlank(raw.getProductSn())
                && StrUtil.isBlank(raw.getProductName())
                && StrUtil.isBlank(raw.getPicUrl())
                && StrUtil.isBlank(raw.getCategoryName())
                && StrUtil.isBlank(raw.getShippingType())
                && StrUtil.isBlank(raw.getProductPrice())
                && StrUtil.isBlank(raw.getProductStock())
                && StrUtil.isBlank(raw.getProductStatus())
                && StrUtil.isBlank(raw.getSpecName())
                && StrUtil.isBlank(raw.getSpecPicUrl())
                && StrUtil.isBlank(raw.getSkuSn())
                && StrUtil.isBlank(raw.getMarketPrice())
                && StrUtil.isBlank(raw.getCostPrice())
                && StrUtil.isBlank(raw.getShippingTpl())
                && StrUtil.isBlank(raw.getDescPicUrl())
                && StrUtil.isBlank(raw.getSkuTsn())
                && StrUtil.isBlank(raw.getProductTsn())
                && StrUtil.isBlank(raw.getKeywords())
                && StrUtil.isBlank(raw.getProductBrief());
    }

    private Map<String, Long> loadShippingTplDict(Integer shopId) {
        try {
            int sid = resolveShopId(shopId);
            List<ShippingTpl> list = shippingTplService.lambdaQuery()
                    .eq(ShippingTpl::getShopId, sid)
                    .orderByAsc(ShippingTpl::getShippingTplId)
                    .list();
            Map<String, Long> map = new HashMap<>();
            for (ShippingTpl t : list) {
                if (StrUtil.isNotBlank(t.getShippingTplName()) && t.getShippingTplId() != null) {
                    map.putIfAbsent(t.getShippingTplName().trim(), t.getShippingTplId());
                }
            }
            return map;
        } catch (Exception e) {
            log.warn("加载运费模板字典失败 shopId={}", shopId, e);
            return Collections.emptyMap();
        }
    }

    private ShippingTplResolution resolveShippingTpl(String cell, Map<String, Long> dict) {
        Matcher matcher = SHIPPING_TPL_NAME_PATTERN.matcher(cell);
        if (matcher.find()) {
            String name = matcher.group(1).trim();
            if (StrUtil.isBlank(name)) {
                return ShippingTplResolution.warn("运费模板名称为空，已忽略");
            }
            if (dict == null || !dict.containsKey(name)) {
                return ShippingTplResolution.warn("运费模板【" + name + "】不存在，已忽略");
            }
            return ShippingTplResolution.tpl(dict.get(name));
        }
        String t = cell.trim();
        if (t.matches("^\\d+(\\.\\d+)?$")) {
            return ShippingTplResolution.fixed(new BigDecimal(t));
        }
        return ShippingTplResolution.warn("运费模板格式不识别（" + cell + "），请填数字或【运费模板名】");
    }

    private void applyShippingTpl(Product product, ShippingTplResolution res) {
        if (res.warning != null && !res.fixed && res.shippingTplId == null) {
            return;
        }
        if (res.fixed) {
            BigDecimal fee = res.fixedFee != null ? res.fixedFee : BigDecimal.ZERO;
            product.setFixedShippingType(1);
            product.setFixedShippingFee(fee);
            product.setFreeShipping(fee.compareTo(BigDecimal.ZERO) == 0 ? 1 : 0);
            product.setShippingTplId(null);
        } else if (res.shippingTplId != null) {
            product.setFixedShippingType(2);
            product.setFixedShippingFee(BigDecimal.ZERO);
            product.setFreeShipping(0);
            product.setShippingTplId(res.shippingTplId);
        }
    }

    private void applyShippingFlags(Product product, ShippingTypeFlags f) {
        product.setIsLogistics(f.logistics ? 1 : 0);
        product.setIsShopDelivery(f.shopDelivery ? 1 : 0);
        product.setIsShopPickup(f.shopPickup ? 1 : 0);
        product.setNoShipping(f.hasAny() ? 0 : 1);
    }

    private ShippingTypeFlags parseShippingType(String raw) {
        ShippingTypeFlags f = new ShippingTypeFlags();
        if (StrUtil.isBlank(raw)) {
            return f;
        }
        String[] parts = raw.split("[;；,，]");
        for (String p : parts) {
            String s = StrUtil.trimToEmpty(p);
            if (StrUtil.isBlank(s)) {
                continue;
            }
            if (s.contains("快递") || "logistics".equalsIgnoreCase(s)) {
                f.logistics = true;
            }
            if (s.contains("同城") || "same_city".equalsIgnoreCase(s)) {
                f.shopDelivery = true;
            }
            if (s.contains("到店") || s.contains("自提") || "pickup".equalsIgnoreCase(s)) {
                f.shopPickup = true;
            }
        }
        return f;
    }

    private LinkedHashSet<String> collectGalleryUrls(List<ProductImportExcelVO> rows) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (ProductImportExcelVO row : rows) {
            for (String u : SpecImportUtils.splitPicUrls(row.getPicUrl())) {
                if (StrUtil.isNotBlank(u)) {
                    set.add(u.trim());
                }
            }
        }
        return set;
    }

    /**
     * 收集本组所有行的「商品详情图」URL（去重 + 去前缀 + 按 [；;|] 切分）。
     * 与 {@link #collectGalleryUrls(List)} 同款；不限制数量，与单个创建商品保持一致。
     */
    private LinkedHashSet<String> collectDescPicUrls(List<ProductImportExcelVO> rows) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (ProductImportExcelVO row : rows) {
            if (StrUtil.isBlank(row.getDescPicUrl())) {
                continue;
            }
            for (String pic : SpecImportUtils.splitPicUrls(row.getDescPicUrl())) {
                if (StrUtil.isNotBlank(pic)) {
                    set.add(pic.trim());
                }
            }
        }
        return set;
    }

    /**
     * 收集本组「首组规格值 → 规格图」映射。
     * <p>
     * 规格图存在 {@code product_attributes.attr_pic} 上，粒度是规格值而非 SKU，所以只取每行第一组规格
     * （与后台 {@code specImageMode} 默认「仅第一组规格可上传图片」一致）；同一规格值在多行图片不同时按首次出现取值。
     */
    private Map<String, String> collectSpecPics(List<ProductImportExcelVO> rows) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (ProductImportExcelVO row : rows) {
            String pic = SpecImportUtils.firstPicUrl(row.getSpecPicUrl());
            if (StrUtil.isBlank(pic)) {
                continue;
            }
            List<String[]> pairs = SpecImportUtils.parseSpecPairs(row.getSpecName());
            if (CollUtil.isEmpty(pairs)) {
                continue;
            }
            String[] first = pairs.get(0);
            map.putIfAbsent(specPicKey(first[0], first[1]), pic);
        }
        return map;
    }

    /**
     * 把采集到的规格图写进 {@code product_attributes}。
     *
     * @param overwrite 覆盖已有图。新建与存量商品再导入均传 {@code true}（列有值覆盖、留空不进 map 故保留原图）；
     *                  与商品图册/详情图的 {@code isChangePic} 开关解耦
     */
    private void applySpecPics(Integer productId, Map<String, String> picByValue, boolean overwrite) {
        if (CollUtil.isEmpty(picByValue)) {
            return;
        }
        List<ProductAttribute> attrs = productAttributesService.lambdaQuery()
                .eq(ProductAttribute::getProductId, productId)
                .eq(ProductAttribute::getAttrType, 1)
                .list();
        List<ProductAttribute> changed = new ArrayList<>();
        for (ProductAttribute attr : attrs) {
            String pic = picByValue.get(specPicKey(StrUtil.trimToEmpty(attr.getAttrName()),
                    StrUtil.trimToEmpty(attr.getAttrValue())));
            if (StrUtil.isBlank(pic)) {
                continue;
            }
            if (!overwrite && StrUtil.isNotBlank(attr.getAttrPic())) {
                continue;
            }
            attr.setAttrPic(pic);
            attr.setAttrPicThumb(pic);
            changed.add(attr);
        }
        if (CollUtil.isNotEmpty(changed)) {
            productAttributesService.updateBatchById(changed);
        }
    }

    /**
     * 导出侧规格图字典：{@code productId + 规格名 + 规格值 → attr_pic}。
     * 与 C 端 {@code getProductAttribute} 一样，同名同值的多条记录只认第一条。
     */
    private Map<String, String> loadSpecPicDict(List<Integer> productIds) {
        if (CollUtil.isEmpty(productIds)) {
            return Map.of();
        }
        Map<String, String> dict = new HashMap<>();
        productAttributesService.lambdaQuery()
                .in(ProductAttribute::getProductId, productIds)
                .eq(ProductAttribute::getAttrType, 1)
                .orderByAsc(ProductAttribute::getAttributesId)
                .list()
                .forEach(attr -> {
                    if (StrUtil.isBlank(attr.getAttrPic())) {
                        return;
                    }
                    dict.putIfAbsent(specPicKey(attr.getProductId(),
                            StrUtil.trimToEmpty(attr.getAttrName()),
                            StrUtil.trimToEmpty(attr.getAttrValue())), attr.getAttrPic());
                });
        return dict;
    }

    /**
     * 只按第一组规格取图，与导入侧的挂载口径对称。
     * <p>
     * 这里刻意复用导入的 {@code parseSpecPairs} 解析导出行的规格文本：两侧共用同一个解析器，
     * 导出格式与导入期望格式就不会再各自漂移。
     */
    private String resolveExportSpecPic(Map<String, String> dict, Integer productId, String specText) {
        if (dict.isEmpty() || StrUtil.isBlank(specText)) {
            return "";
        }
        List<String[]> pairs = SpecImportUtils.parseSpecPairs(specText);
        if (CollUtil.isEmpty(pairs)) {
            return "";
        }
        String[] first = pairs.get(0);
        String pic = dict.get(specPicKey(productId, first[0], first[1]));
        return StrUtil.blankToDefault(pic, "");
    }

    private static String specPicKey(String attrName, String attrValue) {
        return attrName + '\u0000' + attrValue;
    }

    private static String specPicKey(Integer productId, String attrName, String attrValue) {
        return productId + "\u0000" + attrName + '\u0000' + attrValue;
    }

    /**
     * 按 {@link ProductDescDTO} 把已采集的详情图 URL 集合拼成 {@code product_desc} HTML。
     * 空集合返回空字符串，调用方自行决定是否写库。
     */
    private String buildProductDescHtml(LinkedHashSet<String> descPicUrls) {
        if (CollUtil.isEmpty(descPicUrls)) {
            return "";
        }
        List<ProductDescDTO> descList = descPicUrls.stream()
                .map(url -> {
                    ProductDescDTO dto = new ProductDescDTO();
                    dto.setType("pic");
                    dto.setPic(url);
                    return dto;
                })
                .collect(Collectors.toList());
        return ProductDescDTO.getProductDesc(descList);
    }

    private void fillMissingSkuCombinations(Integer productId, List<ProductSku> skuList, List<List<String[]>> specPairsList) {
        LinkedHashMap<String, LinkedHashSet<String>> specGroups = new LinkedHashMap<>();
        for (List<String[]> pairs : specPairsList) {
            for (String[] pv : pairs) {
                if (pv.length < 2) {
                    continue;
                }
                specGroups.computeIfAbsent(pv[0], k -> new LinkedHashSet<>()).add(pv[1]);
            }
        }
        if (specGroups.size() < 2) {
            return;
        }
        LinkedHashSet<String> existingSkuValues = skuList.stream()
                .map(ProductSku::getSkuValue)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<List<String[]>> allCombinations = cartesianProduct(specGroups);
        for (List<String[]> combo : allCombinations) {
            String skuValue = SpecImportUtils.buildSkuValue(combo);
            if (!existingSkuValues.contains(skuValue)) {
                ProductSku placeholder = ProductSku.builder()
                        .productId(productId)
                        .skuValue(skuValue)
                        .skuData(buildSkuDataJson(combo))
                        .skuSn("")
                        .skuPrice(BigDecimal.ZERO)
                        .skuStock(0)
                        .marketPrice(BigDecimal.ZERO)
                        .costPrice(BigDecimal.ZERO)
                        .build();
                skuList.add(placeholder);
                specPairsList.add(combo);
            }
        }
    }

    private List<List<String[]>> cartesianProduct(LinkedHashMap<String, LinkedHashSet<String>> specGroups) {
        List<List<String[]>> result = new ArrayList<>();
        result.add(new ArrayList<>());
        for (Map.Entry<String, LinkedHashSet<String>> entry : specGroups.entrySet()) {
            String groupName = entry.getKey();
            List<List<String[]>> expanded = new ArrayList<>();
            for (List<String[]> existing : result) {
                for (String value : entry.getValue()) {
                    List<String[]> combo = new ArrayList<>(existing);
                    combo.add(new String[]{groupName, value});
                    expanded.add(combo);
                }
            }
            result = expanded;
        }
        return result;
    }

    private boolean isSkuSnOccupied(String skuSn) {
        return productSkuService.lambdaQuery()
                .eq(ProductSku::getSkuSn, skuSn)
                .inSql(ProductSku::getProductId, "SELECT product_id FROM product WHERE is_delete = 0")
                .count() > 0;
    }

    private void validateSkuSn(String skuSn, LinkedHashSet<String> seenSkuSns, int groupIndex, String productSn) {
        if (skuSn.length() > MAX_SKU_SN_LEN) {
            throw new GlobalException("第" + groupIndex + "组(" + productSn + ")：规格编码[" + skuSn + "]超过" + MAX_SKU_SN_LEN + "个字符");
        }
        if (!seenSkuSns.add(skuSn)) {
            throw new GlobalException("第" + groupIndex + "组(" + productSn + ")：规格编码[" + skuSn + "]重复");
        }
        if (isSkuSnOccupied(skuSn)) {
            throw new GlobalException("第" + groupIndex + "组(" + productSn + ")：规格编码[" + skuSn + "]已存在");
        }
    }

    private void generateMissingSkuSns(String productSn, List<ProductSku> importedSkus) {
        List<ProductSku> missingSkuSns = importedSkus.stream()
                .filter(sku -> StrUtil.isBlank(sku.getSkuSn()))
                .filter(sku -> sku.getSkuId() != null)
                .toList();
        if (CollUtil.isEmpty(missingSkuSns)) {
            return;
        }
        missingSkuSns.forEach(sku -> sku.setSkuSn(productSn + "-" + sku.getSkuId()));
        productSkuService.updateBatchById(missingSkuSns);
    }

    private int parseInteger(String v, int defaultVal) {
        if (StrUtil.isBlank(v)) {
            return defaultVal;
        }
        try {
            return new BigDecimal(v.trim()).intValue();
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private BigDecimal parseBigDecimal(String v) {
        if (StrUtil.isBlank(v)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(v.trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String buildCategoryPath(Integer leafId, Map<Integer, Category> catById) {
        if (leafId == null || catById.isEmpty()) {
            return "";
        }
        LinkedList<String> names = new LinkedList<>();
        Integer cur = leafId;
        int guard = 0;
        while (cur != null && cur > 0 && guard++ < 24) {
            Category c = catById.get(cur);
            if (c == null) {
                break;
            }
            names.addFirst(c.getCategoryName());
            Integer pid = c.getParentId();
            if (pid == null || pid == 0) {
                break;
            }
            cur = pid;
        }
        return String.join("/", names);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean productBatchEdit(ProductBatchEditDTO dto) {
        if (CollUtil.isNotEmpty(dto.getEditItems())) {
            for (ProductBatchEditDTO.EditItem item : dto.getEditItems()) {
                if (StrUtil.isNotBlank(item.getProductSn())) {
                    Long cnt = this.lambdaQuery()
                            .eq(Product::getProductSn, item.getProductSn())
                            .ne(Product::getProductId, item.getProductId())
                            .count();
                    if (cnt > 0) {
                        throw new GlobalException(PRODUCT_SN_EXIST + "：" + item.getProductSn());
                    }
                }
            }
            List<Product> products = dto.buildProducts();
            this.updateBatchById(products);
        }
        return true;
    }


    static final class ShippingTypeFlags {
        boolean logistics;
        boolean shopDelivery;
        boolean shopPickup;

        boolean hasAny() {
            return logistics || shopDelivery || shopPickup;
        }

        static ShippingTypeFlags defaults() {
            ShippingTypeFlags f = new ShippingTypeFlags();
            f.logistics = true;
            return f;
        }
    }

    static final class ShippingTplResolution {
        Long shippingTplId;
        BigDecimal fixedFee;
        boolean fixed;
        String warning;

        static ShippingTplResolution fixed(BigDecimal fee) {
            ShippingTplResolution r = new ShippingTplResolution();
            r.fixed = true;
            r.fixedFee = fee;
            return r;
        }

        static ShippingTplResolution tpl(Long id) {
            ShippingTplResolution r = new ShippingTplResolution();
            r.shippingTplId = id;
            return r;
        }

        static ShippingTplResolution warn(String msg) {
            ShippingTplResolution r = new ShippingTplResolution();
            r.warning = msg;
            return r;
        }
    }
}
