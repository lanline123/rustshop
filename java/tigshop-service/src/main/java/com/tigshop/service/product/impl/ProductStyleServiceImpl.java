package com.tigshop.service.product.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.enums.product.ProductStyleActivityStatusEnum;
import com.tigshop.bean.enums.product.ProductStyleItemTypeEnum;
import com.tigshop.bean.enums.product.ProductStyleTypeEnum;
import com.tigshop.bean.model.product.Brand;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductGroup;
import com.tigshop.bean.model.product.ProductStyle;
import com.tigshop.bean.param.product.ProductStyleEditParam;
import com.tigshop.bean.param.product.ProductStyleItemBatchParam;
import com.tigshop.bean.param.product.ProductStyleSaveParam;
import com.tigshop.bean.query.product.ProductStylePageQuery;
import com.tigshop.bean.vo.product.BrandProductCountVO;
import com.tigshop.bean.vo.product.ProductActiveStyleVO;
import com.tigshop.bean.vo.product.ProductStyleDetailVO;
import com.tigshop.bean.vo.product.ProductStyleVO;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.product.ProductStyleMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.*;
import com.tigshop.service.setting.GalleryPicService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductStyleServiceImpl extends BaseServiceImpl<ProductStyleMapper, ProductStyle> implements ProductStyleService {

    @Resource
    private ProductStyleItemService productStyleItemService;

    @Resource
    private ProductService productService;

    @Resource
    private ProductGroupService productGroupService;

    @Resource
    private BrandService brandService;

    @Resource
    private TranslatePackage translatePackage;

    @Resource
    private GalleryPicService galleryPicService;

    @Override
    public Page<ProductStyleVO> list(ProductStylePageQuery pageQuery) {
        Page<ProductStyleVO> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        page.setOrders(buildStyleOrders(pageQuery.getSortField(), pageQuery.getSortOrder()));

        long now = System.currentTimeMillis() / 1000;
        Long startTime = pageQuery.getStartTime() != null ? TigUtils.toTimestampYmd(pageQuery.getStartTime()) : null;
        Long endTime = pageQuery.getEndTime() != null ? TigUtils.toTimestampYmd(pageQuery.getEndTime()) : null;

        Page<ProductStyleVO> stylePage = baseMapper.pageList(page, pageQuery, HeaderUtils.getShopId(), now, startTime, endTime);

        List<ProductStyleVO> records = stylePage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        records.forEach(vo -> {
            vo.setStyleTypeName(ProductStyleTypeEnum.getTypeName(vo.getStyleType()));
            vo.setActivityStatus(ProductStyleActivityStatusEnum.calcStatus(vo.getStartTime(), vo.getEndTime()));
            vo.setActivityStatusName(ProductStyleActivityStatusEnum.getStatusName(vo.getActivityStatus()));
        });

        return stylePage;
    }

    /**
     * 构建分页排序。商品数（productCount）是 SQL 子查询统计出的别名列 product_count，
     * 单独处理并附加 style_id 作为稳定次序；其余字段按实体列解析并以 ps. 限定，避免与 JOIN 列歧义。
     */
    private List<OrderItem> buildStyleOrders(String sortField, String sortOrder) {
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        if ("productCount".equals(sortField) || "product_count".equals(sortField)) {
            List<OrderItem> orders = new ArrayList<>();
            orders.add(asc ? OrderItem.asc("product_count") : OrderItem.desc("product_count"));
            orders.add(OrderItem.desc("ps.style_id"));
            return orders;
        }
        String field = sortField;
        if (field == null || field.isEmpty() || Objects.equals(field, getKeyProperty())) {
            field = getKeyColumn();
        }
        String column = "ps." + getColumnByPropertyName(field);
        return List.of(asc ? OrderItem.asc(column) : OrderItem.desc(column));
    }

    @Override
    public ProductStyleDetailVO detail(Integer styleId) {
        ProductStyle productStyle = this.getById(styleId);
        Assert.notNull(productStyle, () -> new GlobalException("商品样式不存在"));

        Integer shopId = productStyle.getShopId();
        if(!Objects.equals(shopId, HeaderUtils.getShopId())) {
            //当做不存在
            throw new GlobalException("商品样式不存在");
        }

        ProductStyleDetailVO vo = BeanUtil.copyProperties(productStyle, ProductStyleDetailVO.class);
        vo.setStyleTypeName(ProductStyleTypeEnum.getTypeName(productStyle.getStyleType()));
        Integer detailActivityStatus = ProductStyleActivityStatusEnum.calcStatus(productStyle.getStartTime(), productStyle.getEndTime());
        vo.setActivityStatus(detailActivityStatus);
        vo.setActivityStatusName(ProductStyleActivityStatusEnum.getStatusName(detailActivityStatus));

        List<Integer> productIds = productStyleItemService.getRefIdsByStyleIdAndType(
                styleId, ProductStyleItemTypeEnum.PRODUCT.getCode());
        List<Integer> groupIds = productStyleItemService.getRefIdsByStyleIdAndType(
                styleId, ProductStyleItemTypeEnum.GROUP.getCode());
        List<Integer> brandIds = productStyleItemService.getRefIdsByStyleIdAndType(
                styleId, ProductStyleItemTypeEnum.BRAND.getCode());

        vo.setProductIds(productIds);
        vo.setGroupIds(groupIds);
        vo.setBrandIds(brandIds);

        Set<Integer> allProductIds = new LinkedHashSet<>(productIds);

        if (CollUtil.isNotEmpty(groupIds)) {
            List<ProductGroup> groups = productGroupService.listByIds(groupIds);
            vo.setGroupList(groups.stream().map(g -> {
                ProductStyleDetailVO.GroupItemVO itemVO = new ProductStyleDetailVO.GroupItemVO();
                itemVO.setProductGroupId(g.getProductGroupId());
                itemVO.setProductGroupName(g.getProductGroupName());
                return itemVO;
            }).collect(Collectors.toList()));
            for (ProductGroup group : groups) {
                if (group.getProductIds() != null) {
                    List<Integer> groupProductIds = JSONUtil.toList(group.getProductIds(), Integer.class);
                    allProductIds.addAll(groupProductIds);
                }
            }
        } else {
            vo.setGroupList(Collections.emptyList());
        }

        if (CollUtil.isNotEmpty(brandIds)) {
            List<Brand> brands = brandService.listByIds(brandIds);
            vo.setBrandList(brands.stream().map(b -> {
                ProductStyleDetailVO.BrandItemVO itemVO = new ProductStyleDetailVO.BrandItemVO();
                itemVO.setBrandId(b.getBrandId());
                itemVO.setBrandName(b.getBrandName());
                return itemVO;
            }).collect(Collectors.toList()));
            List<Product> brandProducts = productService.lambdaQuery()
                    .in(Product::getBrandId, brandIds)
                    .eq(Product::getIsDelete, 0)
                    .select(Product::getProductId)
                    .list();
            for (Product p : brandProducts) {
                allProductIds.add(p.getProductId());
            }
        } else {
            vo.setBrandList(Collections.emptyList());
        }

        if (CollUtil.isNotEmpty(allProductIds)) {
            List<Product> products = productService.listByIds(allProductIds);
            vo.setProductList(products.stream().map(p -> {
                ProductStyleDetailVO.ProductItemVO itemVO = new ProductStyleDetailVO.ProductItemVO();
                itemVO.setProductId(p.getProductId());
                itemVO.setProductName(p.getProductName());
                itemVO.setPicThumb(p.getPicThumb());
                itemVO.setProductPrice(p.getProductPrice());
                return itemVO;
            }).collect(Collectors.toList()));
        } else {
            vo.setProductList(Collections.emptyList());
        }

        vo.setProductIds(allProductIds.stream().toList());

        Map<Integer, Integer> countMap = getRealProductCountMap(List.of(styleId));
        vo.setProductCount(countMap.getOrDefault(styleId, 0));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ProductStyleSaveParam param) {
        validateParam(param);

        ProductStyle productStyle = param.createProductStyle();
        productStyle.setShopId(getShopId() != null ? getShopId() : 0);
        this.save(productStyle);

        saveStyleItems(productStyle.getStyleId(), param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ProductStyleEditParam param) {
        ProductStyle existing = this.getById(param.getStyleId());
        Assert.notNull(existing, () -> new GlobalException("商品样式不存在"));

        Integer shopId = existing.getShopId();
        if(!Objects.equals(shopId, HeaderUtils.getShopId())) {
            //当做不存在
            throw new GlobalException("商品样式不存在");
        }

        validateParam(param);

        ProductStyle productStyle = param.createProductStyle();
        this.updateById(productStyle);

        productStyleItemService.removeByStyleId(param.getStyleId());
        saveStyleItems(param.getStyleId(), param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProducts(ProductStyleItemBatchParam param) {
        ProductStyle existing = this.getById(param.getStyleId());
        Assert.notNull(existing, () -> new GlobalException("商品样式不存在"));

        batchAddFromParam(param.getStyleId(), param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeProducts(ProductStyleItemBatchParam param) {
        ProductStyle existing = this.getById(param.getStyleId());
        Assert.notNull(existing, () -> new GlobalException("商品样式不存在"));

        Integer shopId = existing.getShopId();
        if(!Objects.equals(shopId, HeaderUtils.getShopId())) {
            //当做不存在
            throw new GlobalException("商品样式不存在");
        }

        if (CollUtil.isNotEmpty(param.getProductIds())) {
            productStyleItemService.batchRemove(param.getStyleId(),
                    ProductStyleItemTypeEnum.PRODUCT.getCode(), param.getProductIds());
        }
        if (CollUtil.isNotEmpty(param.getGroupIds())) {
            productStyleItemService.batchRemove(param.getStyleId(),
                    ProductStyleItemTypeEnum.GROUP.getCode(), param.getGroupIds());
        }
        if (CollUtil.isNotEmpty(param.getBrandIds())) {
            productStyleItemService.batchRemove(param.getStyleId(),
                    ProductStyleItemTypeEnum.BRAND.getCode(), param.getBrandIds());
        }
    }

    @Override
    public ProductActiveStyleVO getActiveStyle(Integer productId) {
        Map<Integer, ProductActiveStyleVO> result = batchGetActiveStyles(List.of(productId));
        return result.getOrDefault(productId, new ProductActiveStyleVO());
    }

    @Override
    public Map<Integer, ProductActiveStyleVO> batchGetActiveStyles(List<Integer> productIds) {
        if (CollUtil.isEmpty(productIds)) {
            return Collections.emptyMap();
        }

        long now = System.currentTimeMillis() / 1000;
        List<Map<String, Object>> rows = baseMapper.selectActiveStylesByProductIds(productIds, now);

        Map<Integer, ProductActiveStyleVO> resultMap = new HashMap<>();

        for (Map<String, Object> row : rows) {
            Integer productId = ((Number) row.get("productId")).intValue();
            Integer styleType = ((Number) row.get("styleType")).intValue();
            String styleImage = (String) row.get("styleImage");
            String pcImage = (String) row.get("pcImage");
            Integer styleId = ((Number) row.get("styleId")).intValue();
            String styleName = (String) row.get("styleName");

            ProductActiveStyleVO vo = resultMap.computeIfAbsent(productId, k -> new ProductActiveStyleVO());

            if (styleType == ProductStyleTypeEnum.BORDER.getCode() && vo.getBorderStyleId() == null) {
                vo.setBorderStyleId(styleId);
                vo.setBorderImage(translatePackage.translate(styleImage,19));
                vo.setBorderPcImage(translatePackage.translate(pcImage,19));
            } else if (styleType == ProductStyleTypeEnum.ACTIVITY_BACKGROUND.getCode() && vo.getActivityStyleId() == null) {
                vo.setActivityStyleId(styleId);
                vo.setActivityStyleName(translatePackage.translate(styleName,18));
                vo.setActivityBackgroundImage(translatePackage.translate(styleImage,19));
                vo.setActivityBackgroundPcImage(translatePackage.translate(pcImage,19));
            }
        }

        return resultMap;
    }

    @Override
    public Map<String, ProductActiveStyleVO> batchGetActiveStylesBySns(List<String> productSns) {
        if (CollUtil.isEmpty(productSns)) {
            return Collections.emptyMap();
        }

        List<Product> products = productService.lambdaQuery()
                .in(Product::getProductSn, productSns)
                .eq(Product::getIsDelete, 0)
                .select(Product::getProductId, Product::getProductSn)
                .list();
        if (CollUtil.isEmpty(products)) {
            return Collections.emptyMap();
        }

        Map<Integer, String> idToSnMap = products.stream()
                .collect(Collectors.toMap(Product::getProductId, Product::getProductSn, (a, b) -> a));
        List<Integer> productIds = new ArrayList<>(idToSnMap.keySet());

        Map<Integer, ProductActiveStyleVO> styleByIdMap = batchGetActiveStyles(productIds);

        Map<String, ProductActiveStyleVO> resultMap = new HashMap<>();
        for (Map.Entry<Integer, ProductActiveStyleVO> entry : styleByIdMap.entrySet()) {
            String sn = idToSnMap.get(entry.getKey());
            if (sn != null) {
                resultMap.put(sn, entry.getValue());
            }
        }
        return resultMap;
    }

    private void saveStyleItems(Integer styleId, ProductStyleSaveParam param) {
        if (CollUtil.isNotEmpty(param.getProductIds())) {
            productStyleItemService.batchAdd(styleId,
                    ProductStyleItemTypeEnum.PRODUCT.getCode(), param.getProductIds());
        }
        if (CollUtil.isNotEmpty(param.getGroupIds())) {
            productStyleItemService.batchAdd(styleId,
                    ProductStyleItemTypeEnum.GROUP.getCode(), param.getGroupIds());
        }
        if (CollUtil.isNotEmpty(param.getBrandIds())) {
            productStyleItemService.batchAdd(styleId,
                    ProductStyleItemTypeEnum.BRAND.getCode(), param.getBrandIds());
        }
    }

    private void batchAddFromParam(Integer styleId, ProductStyleItemBatchParam param) {
        if (CollUtil.isNotEmpty(param.getProductIds())) {
            productStyleItemService.batchAdd(styleId,
                    ProductStyleItemTypeEnum.PRODUCT.getCode(), param.getProductIds());
        }
        if (CollUtil.isNotEmpty(param.getGroupIds())) {
            productStyleItemService.batchAdd(styleId,
                    ProductStyleItemTypeEnum.GROUP.getCode(), param.getGroupIds());
        }
        if (CollUtil.isNotEmpty(param.getBrandIds())) {
            productStyleItemService.batchAdd(styleId,
                    ProductStyleItemTypeEnum.BRAND.getCode(), param.getBrandIds());
        }
    }

    /**
     * 批量查询样式关联的实际商品数量（通过product表/product_group表实时统计）
     */
    private Map<Integer, Integer> getRealProductCountMap(List<Integer> styleIds) {
        if (CollUtil.isEmpty(styleIds)) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> rows = baseMapper.countRealProductsByStyleIds(styleIds);
        Map<Integer, Integer> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer sid = ((Number) row.get("styleId")).intValue();
            Integer count = ((Number) row.get("productCount")).intValue();
            result.put(sid, count);
        }
        return result;
    }

    @Override
    public List<BrandProductCountVO> brandListWithProductCount(String keyword) {
        return baseMapper.selectBrandListWithProductCount(keyword);
    }

    private void validateParam(ProductStyleSaveParam param) {
        if (param.getStyleType() == ProductStyleTypeEnum.ACTIVITY_BACKGROUND.getCode()) {
            Assert.notBlank(param.getStartTime(), () -> new GlobalException("活动背景的生效开始时间不能为空"));
            Assert.notBlank(param.getEndTime(), () -> new GlobalException("活动背景的生效结束时间不能为空"));
            long start = TigUtils.toTimestampYmdHms(param.getStartTime());
            long end = TigUtils.toTimestampYmdHms(param.getEndTime());
            Assert.isTrue(end > start, () -> new GlobalException("结束时间必须大于开始时间"));
        }
    }
}
