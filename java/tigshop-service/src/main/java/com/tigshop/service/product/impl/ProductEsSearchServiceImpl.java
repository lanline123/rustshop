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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.ProductListDTO;
import com.tigshop.bean.dto.product.ProductListResDTO;
import com.tigshop.bean.dto.product.ProductSearchClientDTO;
import com.tigshop.bean.enums.product.ProductType;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.vo.product.ProductSearchFilterListVO;
import com.tigshop.bean.vo.product.ProductSearchFilterSelectedVO;
import java.util.Map;
import com.tigshop.common.config.ElasticsearchConfig;
// import com.tigshop.common.utils.ApiResponse;
import com.tigshop.common.utils.ElasticsearchUtil;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.service.product.BrandService;
import com.tigshop.service.product.CategoryService;
import com.tigshop.service.product.ProductEsSearchService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.shop.ShopProductCategoryService;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.mapper.shop.ShopMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 产品ES搜索服务实现
 *
 * @author Tigshop团队
 * @create 2025年02月12日 15:00
 */
@Slf4j
@Service
public class ProductEsSearchServiceImpl implements ProductEsSearchService {

    @Resource
    private ElasticsearchUtil elasticsearchUtil;

    @Resource
    private ElasticsearchConfig elasticsearchConfig;

    @Resource
    private ProductService productService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private BrandService brandService;

    @Resource
    private ShopProductCategoryService shopProductCategoryService;

    @Resource
    private ShopMapper shopMapper;


    @Override
    public Page<ProductListResDTO> searchProducts(ProductSearchClientDTO searchDTO) {
        try {
            // 构建ES查询
            Map<String, Object> query = buildEsQuery(searchDTO);
            
            // 构建搜索选项
            Map<String, Object> options = buildSearchOptions(searchDTO);
            
            // 记录完整的ES查询语句到日志
            Map<String, Object> fullQuery = new HashMap<>();
            fullQuery.put("query", query);
            fullQuery.putAll(options);
            log.debug("ES产品搜索查询语句 - 索引: {}, 查询条件: {}", elasticsearchConfig.getProductIndex(), fullQuery);
            
            // 执行ES搜索
            String indexName = elasticsearchConfig.getProductIndex();

            Map<String, Object> result = elasticsearchUtil.search(indexName, query, options);
            
            if (result == null || result.isEmpty()) {
                throw new IllegalStateException("ES搜索未返回有效结果");
            }
            // 解析ES搜索结果
            List<Map<String, Object>> hits = (List<Map<String, Object>>) result.get("hits");
            Long total = ((Number) result.get("total")).longValue();
            
            if (CollUtil.isEmpty(hits)) {
                return emptyPage(searchDTO, total);
            }
            
            // 提取产品ID列表
            List<Long> productIds = hits.stream()
                    .map(hit -> Long.valueOf(hit.get("product_id").toString()))
                    .toList();
            // 只按 ES 命中 ID 回表装配，避免关键词/筛选条件二次执行造成空洞页和 total 失真。
            ProductListDTO listDTO = buildHydrationListDTO(searchDTO, productIds);
            
            // 使用原有的产品服务获取完整信息
            Page<ProductListResDTO> resultPage = productService.list(listDTO);
            resultPage.setTotal(total);
            resultPage.setCurrent(searchDTO.getPage() == null ? 1 : searchDTO.getPage());
            resultPage.setSize(searchDTO.getSize() != null && searchDTO.getSize() <= 50 ? searchDTO.getSize() : 25);
            
            return resultPage;
        } catch (Exception e) {
            log.error("ES搜索产品失败", e);
            throw new IllegalStateException("ES搜索产品失败", e);
        }
    }

    private Page<ProductListResDTO> emptyPage(ProductSearchClientDTO searchDTO, long total) {
        Page<ProductListResDTO> page = new Page<>(
                searchDTO.getPage() == null ? 1 : searchDTO.getPage(),
                searchDTO.getSize() != null && searchDTO.getSize() <= 50 ? searchDTO.getSize() : 25,
                total
        );
        page.setRecords(List.of());
        return page;
    }

    private ProductListDTO buildHydrationListDTO(ProductSearchClientDTO searchDTO, List<Long> productIds) {
        ProductListDTO listDTO = new ProductListDTO();
        listDTO.setPage(1);
        listDTO.setSize(productIds.size());
        listDTO.setIsClient(1);
        listDTO.setIds(productIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        listDTO.setIsDelete(0);
        listDTO.setProductStatus(searchDTO.getProductStatus() == null ? 1 : searchDTO.getProductStatus());
        return listDTO;
    }

    @Override
    public Long getProductCount(ProductSearchClientDTO searchDTO) {
        try {
            // 构建ES查询
            Map<String, Object> query = buildEsQuery(searchDTO);
            
            // 只查询总数，不返回数据
            Map<String, Object> options = new HashMap<>();
            options.put("from", 0);
            options.put("size", 0);
            
            // 记录完整的ES查询语句到日志
            Map<String, Object> fullQuery = new HashMap<>();
            fullQuery.put("query", query);
            fullQuery.putAll(options);
            log.error("ES产品计数查询语句 - 索引: {}, 查询条件: {}", elasticsearchConfig.getProductIndex(), fullQuery);
            
            // 执行ES搜索
            String indexName = elasticsearchConfig.getProductIndex();
            Map<String, Object> result = elasticsearchUtil.search(indexName, query, options);
            
            if (result == null || result.isEmpty()) {
                log.error("ES搜索总数失败");
                return 0L;
            }
            
            // 直接获取总数
            return ((Number) result.get("total")).longValue();
        } catch (Exception e) {
            log.error("ES搜索产品总数失败", e);
            return 0L;
        }
    }

    @Override
    public ProductSearchFilterListVO getFilterList(ProductSearchClientDTO searchDTO) {
        // ES搜索的筛选列表暂时使用MySQL实现
        // 后续可以优化为ES聚合查询
        ProductSearchFilterListVO filterListVO = new ProductSearchFilterListVO();

        filterListVO.setCategory(getFilterListCategoryList(searchDTO));
        filterListVO.setBrand(productService.getProductBrandList(buildProductListDTO(searchDTO)));

        if (searchDTO.getShopId() != null && searchDTO.getShopId() > 0) {
            if (searchDTO.getShopCategoryId() != null && searchDTO.getShopCategoryId() > 0) {
                filterListVO.setShopCategory(shopProductCategoryService.getCategoryByParentId(searchDTO.getShopCategoryId(),
                        searchDTO.getShopId()));
            } else {
                filterListVO.setShopCategory(shopProductCategoryService.getCategoryByParentId(0,
                        searchDTO.getShopId()));
            }
        }
        
        // 获取最高价格
        searchDTO.setMax(null);
        searchDTO.setMin(null);
        filterListVO.setMaxPrice(productService.getMaxPrice(buildProductListDTO(searchDTO)));
        
        return filterListVO;
    }

    @Override
    public ProductSearchFilterSelectedVO getFilterSelected(ProductSearchClientDTO dto) {
        // ES搜索的已选择筛选条件暂时使用MySQL实现
        ProductSearchFilterSelectedVO filterSelectedVO = new ProductSearchFilterSelectedVO();
        if (dto.getCat() != null && dto.getCat() > 0) {
            filterSelectedVO.setCategory(categoryService.getById(dto.getCat()).getCategoryName());
        }
        if (StrUtil.isNotBlank(dto.getBrand())) {
            List<Integer> brandIds = Arrays.stream(dto.getBrand().split(",")).map(Integer::parseInt).collect(Collectors.toList());
            List<String> brandNames = brandService.getBrandNameByIds(brandIds);
            filterSelectedVO.setBrand(String.join(",", brandNames));
        }
        if (dto.getShopCategoryId() != null && dto.getShopCategoryId() > 0) {
            filterSelectedVO.setShopCategory(shopProductCategoryService.getById(dto.getShopCategoryId()).getCategoryName());
        }
        if (dto.getKeyword() != null) {
            filterSelectedVO.setKeyword(dto.getKeyword());
        }else{
            //走分类sse
            if(dto.getCat()!=null){
                Optional.ofNullable(categoryService.getById(dto.getCat())).map(Category::getSeoTitle)
                        .ifPresent(filterSelectedVO::setCategory);
            }
        }
        if (dto.getIntro() != null) {
            filterSelectedVO.setIntro(dto.getIntro());
        }
        filterSelectedVO.setAttrs(dto.getAttrs());
        return filterSelectedVO;
    }

    @Override
    public List<Map<String, Object>> getAttributeSuggestions(ProductSearchClientDTO searchDTO) {
        try {
            // 构建基础查询条件（与商品搜索相同的过滤条件）
            Map<String, Object> query = buildAttributeEsQuery(searchDTO);
            
            // 构建聚合查询 - 仅统计普通属性（attr_type=0）
            Map<String, Object> aggs = buildAttributeAggregation();
            
            // 执行ES搜索
            String indexName = elasticsearchConfig.getProductIndex();
            Map<String, Object> searchQuery = new HashMap<>();
            searchQuery.put("query", query);
            searchQuery.put("aggs", aggs);
            searchQuery.put("size", 0);
            
            log.error("ES属性建议查询 - 索引: {}, 查询条件: {}", indexName, searchQuery);
            
            Map<String, Object> result = elasticsearchUtil.search(indexName, searchQuery, new HashMap<>());
            
            if (result == null || result.isEmpty()) {
                log.error("ES属性建议查询失败 - 结果为空");
                return new ArrayList<>();
            }
            
            log.error("ES属性建议查询结果: {}", result);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> aggregations = (Map<String, Object>) result.get("aggregations");
            if (aggregations == null) {
                log.error("ES属性建议查询 - 聚合结果为空");
                return new ArrayList<>();
            }
            
            log.error("ES属性聚合结果: {}", aggregations);
            
            // 解析属性聚合结果
            List<Map<String, Object>> attributes = parseAttributeAggregations(aggregations);
            log.error("解析后的属性结果: {}", attributes);
            
            return attributes;
        } catch (Exception e) {
            log.error("ES属性建议查询失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 构建属性建议的ES查询
     */
    private Map<String, Object> buildAttributeEsQuery(ProductSearchClientDTO searchDTO) {
        Map<String, Object> boolQuery = new HashMap<>();
        List<Map<String, Object>> must = new ArrayList<>();
        List<Map<String, Object>> filter = new ArrayList<>();
        
        // 基础条件：未删除且状态正常
        filter.add(Map.of("term", Map.of("product_status", 1)));
        filter.add(Map.of("term", Map.of("is_delete", 0)));
        
        // 关键词搜索（与MySQL逻辑一致：product_name|keywords|product_sn）
        if (StrUtil.isNotBlank(searchDTO.getKeyword())) {
            Map<String, Object> boolKeyword = new HashMap<>();
            List<Map<String, Object>> should = new ArrayList<>();
            
            // 产品名称搜索
            should.add(Map.of("match", Map.of("product_name", searchDTO.getKeyword())));
            // 关键词搜索
            should.add(Map.of("match", Map.of("keywords", searchDTO.getKeyword())));
            // 产品编号搜索
            should.add(Map.of("match", Map.of("product_sn", searchDTO.getKeyword())));
            
            boolKeyword.put("should", should);
            boolKeyword.put("minimum_should_match", 1);
            must.add(Map.of("bool", boolKeyword));
        }
        
        // 分类筛选
        if (searchDTO.getCat() != null && searchDTO.getCat() > 0) {
            filter.add(Map.of("term", Map.of("category_id", searchDTO.getCat())));
        }
        
        // 品牌筛选
        if (StrUtil.isNotBlank(searchDTO.getBrand())) {
            List<Integer> brandIds = Arrays.stream(searchDTO.getBrand().split(","))
                    .map(Integer::parseInt)
                    .toList();
            filter.add(Map.of("terms", Map.of("brand_id", brandIds)));
        }
        
        // 店铺筛选
        if (searchDTO.getShopId() != null && searchDTO.getShopId() >= 0) {
            filter.add(Map.of("term", Map.of("shop_id", searchDTO.getShopId())));
        }
        
        // 店铺分类筛选
        if (searchDTO.getShopCategoryId() != null && searchDTO.getShopCategoryId() > -1) {
            if (searchDTO.getShopCategoryId() > 0 && searchDTO.getShopId() != null && searchDTO.getShopId() > 0) {
                filter.add(Map.of("term", Map.of("shop_category_id", searchDTO.getShopCategoryId())));
            }
        }

        
        // 价格区间
        if (searchDTO.getMin() != null || searchDTO.getMax() != null) {
            Map<String, Object> range = new HashMap<>();
            if (searchDTO.getMin() != null && searchDTO.getMin().compareTo(BigDecimal.ZERO) > 0) {
                range.put("gte", searchDTO.getMin());
            }
            if (searchDTO.getMax() != null && searchDTO.getMax().compareTo(BigDecimal.ZERO) > 0) {
                range.put("lte", searchDTO.getMax());
            }
            filter.add(Map.of("range", Map.of("product_price", range)));
        }
        
        // 推荐类型筛选
        if (StrUtil.isNotBlank(searchDTO.getIntro())) {
            Map<String, String> introMap = Map.of(
                "new", "is_new",
                "hot", "is_hot", 
                "best", "is_best"
            );
            String field = introMap.get(searchDTO.getIntro());
            if (field != null) {
                filter.add(Map.of("term", Map.of(field, 1)));
            }
        }
        
        if (!must.isEmpty()) {
            boolQuery.put("must", must);
        }
        boolQuery.put("filter", filter);

        return Map.of("bool", boolQuery);
    }

    /**
     * 构建ES查询
     */
    private Map<String, Object> buildEsQuery(ProductSearchClientDTO searchDTO) {
        Map<String, Object> boolQuery = new HashMap<>();
        List<Map<String, Object>> must = new ArrayList<>();
        List<Map<String, Object>> filter = new ArrayList<>();
        List<Map<String, Object>> should = new ArrayList<>();
        List<Map<String, Object>> mustNot = new ArrayList<>();
        
        // 基础条件：未删除且状态正常（放在filter中）
        Map<String, Object> isDeleteQuery = new HashMap<>();
        isDeleteQuery.put("term", Map.of("is_delete", 0));
        filter.add(isDeleteQuery);
        
        Map<String, Object> statusQuery = new HashMap<>();
        statusQuery.put("term", Map.of("product_status",
                searchDTO.getProductStatus() == null ? 1 : searchDTO.getProductStatus()));
        filter.add(statusQuery);
        
        // 关键词搜索
        if (StrUtil.isNotBlank(searchDTO.getKeyword())) {
            Map<String, Object> multiMatch = new HashMap<>();
            multiMatch.put("query", searchDTO.getKeyword());
            multiMatch.put("fields", Arrays.asList("product_name^3", "keywords^2", "product_sn", "product_tsn"));
            multiMatch.put("type", "best_fields");
            multiMatch.put("fuzziness", "AUTO");
            should.add(Map.of("multi_match", multiMatch));
            // MySQL 对货号/条码使用 LIKE，keyword 字段补 contains wildcard 保持编码片段可检索。
            should.add(wildcardQuery("product_sn", searchDTO.getKeyword()));
            should.add(wildcardQuery("product_tsn", searchDTO.getKeyword()));
        }
        
        // 分类筛选
        if (searchDTO.getCat() != null && searchDTO.getCat() > 0) {
            List<Integer> categoryAllChildIds = categoryService.getCategoryAllChildIds(searchDTO.getCat());
            if(Objects.nonNull(categoryAllChildIds) && !categoryAllChildIds.isEmpty()){
                filter.add(Map.of("terms", Map.of("category_id", categoryAllChildIds)));
            }
        }
        // 品牌筛选
        if (StrUtil.isNotBlank(searchDTO.getBrand())) {
            List<Integer> brandIds = Arrays.stream(searchDTO.getBrand().split(","))
                    .map(Integer::parseInt)
                    .toList();
            Map<String, Object> brandQuery = new HashMap<>();
            brandQuery.put("terms", Map.of("brand_id", brandIds));
            filter.add(brandQuery);
        }
        
        // 价格区间
        if (searchDTO.getMin() != null || searchDTO.getMax() != null) {
            Map<String, Object> rangeQuery = new HashMap<>();
            Map<String, Object> range = new HashMap<>();
            if (searchDTO.getMin() != null) {
                range.put("gte", searchDTO.getMin());
            }
            if (searchDTO.getMax() != null) {
                range.put("lte", searchDTO.getMax());
            }
            rangeQuery.put("range", Map.of("product_price", range));
            filter.add(rangeQuery);
        }
        
        // 推荐类型
        if (StrUtil.isNotBlank(searchDTO.getIntro())) {
            String introType = searchDTO.getIntro().toLowerCase();
            Map<String, Object> introQuery = new HashMap<>();
            switch (introType) {
                case "best" -> introQuery.put("term", Map.of("is_best", 1));
                case "hot" -> introQuery.put("term", Map.of("is_hot", 1));
                case "new" -> introQuery.put("term", Map.of("is_new", 1));
            }
            if (!introQuery.isEmpty()) {
                filter.add(introQuery);
            }
        }
        
        // 店铺筛选
        if (searchDTO.getShopId() != null && searchDTO.getShopId() >= 0) {
            Map<String, Object> shopQuery = new HashMap<>();
            shopQuery.put("term", Map.of("shop_id", searchDTO.getShopId()));
            filter.add(shopQuery);
        }
        
        // 店铺分类筛选
        if (searchDTO.getShopCategoryId() != null && searchDTO.getShopCategoryId() > -1) {
            if (searchDTO.getShopCategoryId() > 0 && searchDTO.getShopId() != null && searchDTO.getShopId() > 0) {
                List<Integer> shopCategoryIds = shopProductCategoryService.getCategoryAllChildIds(
                        searchDTO.getShopCategoryId(), searchDTO.getShopId());
                if (CollUtil.isNotEmpty(shopCategoryIds)) {
                    filter.add(Map.of("terms", Map.of("shop_category_id", shopCategoryIds)));
                }
            }
        }


        // 排除礼品卡相关商品（实体礼品卡、电子礼品卡）
        if (Boolean.TRUE.equals(searchDTO.getExcludeGiftCardProduct())) {
            mustNot.add(Map.of("terms", Map.of("product_type", List.of(
                    ProductType.PRODUCT_TYPE_GIFT_CARD.getCode(),
                    ProductType.PRODUCT_TYPE_GIFT_CARD_VIRTUAL.getCode()
            ))));
        }

        if (Integer.valueOf(1).equals(searchDTO.getIsFilterPresaleProduct())) {
            mustNot.add(Map.of("terms", Map.of("sale_mode", List.of(1, 2))));
        }
        
        // 排除不在运营的店铺（status=4）
        List<Shop> notOperatingShops = shopMapper.selectList(new LambdaQueryWrapper<Shop>().eq(Shop::getStatus, 4));
        if (CollUtil.isNotEmpty(notOperatingShops)) {
            List<Integer> excludeShopIds = notOperatingShops.stream()
                    .map(Shop::getShopId)
                    .toList();
            mustNot.add(Map.of("terms", Map.of("shop_id", excludeShopIds)));
        }
        
        // 属性筛选 - 按照指定结构组装
        List<ProductSearchClientDTO.AttrObj> attrs = searchDTO.getAttrs();
        if (attrs != null && !attrs.isEmpty()) {
            for (ProductSearchClientDTO.AttrObj attr : attrs) {
                if (attr == null || StrUtil.isBlank(attr.getAttrName())) {
                    continue;
                }
                
                // 处理属性值
                List<String> attrValues = new ArrayList<>();
                List<String> attrValueList = attr.getAttrValue();
                if (attrValueList != null && !attrValueList.isEmpty()) {
                    for (String value : attrValueList) {
                        if (StrUtil.isNotBlank(value)) {
                            // 支持逗号分隔的属性值
                            String[] values = value.split("[,，]");
                            for (String v : values) {
                                String trimmedValue = v.trim();
                                if (StrUtil.isNotBlank(trimmedValue)) {
                                    attrValues.add(trimmedValue);
                                }
                            }
                        }
                    }
                }
                
                if (attrValues.isEmpty()) {
                    continue;
                }
                
                List<Map<String, Object>> attrMust = new ArrayList<>();
                attrMust.add(Map.of("term", Map.of("attributes.attr_type", 0)));
                attrMust.add(wildcardQuery("attributes.attr_name.keyword", attr.getAttrName()));

                List<Map<String, Object>> valueShould = attrValues.stream()
                        .map(value -> wildcardQuery("attributes.attr_value.keyword", value))
                        .toList();
                attrMust.add(Map.of("bool", Map.of(
                        "should", valueShould,
                        "minimum_should_match", 1
                )));

                must.add(Map.of("nested", Map.of(
                        "path", "attributes",
                        "query", Map.of("bool", Map.of("must", attrMust))
                )));
            }
        }
        
        if (!must.isEmpty()) {
            boolQuery.put("must", must);
        }
        boolQuery.put("filter", filter);
        if (!should.isEmpty()) {
            boolQuery.put("should", should);
            boolQuery.put("minimum_should_match", 1);
        }
        if (!mustNot.isEmpty()) {
            boolQuery.put("must_not", mustNot);
        }
        
        return Map.of("bool", boolQuery);
    }

    private Map<String, Object> wildcardQuery(String field, String value) {
        return Map.of("wildcard", Map.of(
                "field", field,
                "value", toContainsWildcard(value),
                "case_insensitive", true
        ));
    }

    private String toContainsWildcard(String value) {
        String escaped = value.trim()
                .replace("\\", "\\\\")
                .replace("*", "\\*")
                .replace("?", "\\?");
        return "*" + escaped + "*";
    }

    /**
     * 构建搜索选项
     */
    private Map<String, Object> buildSearchOptions(ProductSearchClientDTO searchDTO) {
        Map<String, Object> options = new HashMap<>();
        
        // 分页
        int page = searchDTO.getPage() != null ? searchDTO.getPage() : 1;
        int size = searchDTO.getSize() != null ? searchDTO.getSize() : 25;
        options.put("from", (page - 1) * size);
        options.put("size", size);
        options.put("track_total_hits", true);
        // 排序
        if (StrUtil.isNotBlank(searchDTO.getSort()) && !"default".equals(searchDTO.getSort())) {
            List<Map<String, Object>> sortList = new ArrayList<>();
            
            switch (searchDTO.getSort()) {
                case "sale" -> {
                    Map<String, Object> sort = new HashMap<>();
                    sort.put("field", "virtual_sales");
                    sort.put("order", "desc");
                    sortList.add(sort);
                }
                case "price" -> {
                    Map<String, Object> sort = new HashMap<>();
                    sort.put("field", "product_price");
                    sort.put("order", StrUtil.isNotBlank(searchDTO.getOrder()) ? searchDTO.getOrder() : "asc");
                    sortList.add(sort);
                }
                case "time" -> {
                    Map<String, Object> sort = new HashMap<>();
                    sort.put("field", "product_id");
                    sort.put("order", "desc");
                    sortList.add(sort);
                }
                default -> {
                    sortList.add(Map.of("field", "sort_order", "order", "asc"));
                    sortList.add(Map.of("field", "product_id", "order", "desc"));
                }
            }
            
            if (!sortList.isEmpty()) {
                if (!"time".equals(searchDTO.getSort()) && sortList.size() == 1) {
                    sortList.add(Map.of("field", "product_id", "order", "desc"));
                }
                options.put("sort", sortList);
            }
        } else if (StrUtil.isBlank(searchDTO.getKeyword())) {
            options.put("sort", List.of(
                    Map.of("field", "sort_order", "order", "asc"),
                    Map.of("field", "product_id", "order", "desc")
            ));
        }
        
        // 高亮
        if (StrUtil.isNotBlank(searchDTO.getKeyword())) {
            Map<String, Object> highlight = new HashMap<>();
            highlight.put("fields", Arrays.asList("product_name", "keywords"));
            options.put("highlight", highlight);
        }
        
        return options;
    }

    /**
     * 构建属性聚合查询
     */
    private Map<String, Object> buildAttributeAggregation() {
        Map<String, Object> aggs = new HashMap<>();
        
        // 嵌套聚合 - 仅统计普通属性（attr_type=0）
        Map<String, Object> attributeNames = new HashMap<>();
        attributeNames.put("nested", Map.of("path", "attributes"));
        
        Map<String, Object> attributeNamesAggs = new HashMap<>();
        
        // 仅统计普通属性（attr_type=0），避免其他类型干扰
        Map<String, Object> onlyNormal = new HashMap<>();
        onlyNormal.put("filter", Map.of("term", Map.of("attributes.attr_type", 0)));
        
        Map<String, Object> onlyNormalAggs = new HashMap<>();
        
        // 属性名称聚合
        Map<String, Object> attrNames = new HashMap<>();
        attrNames.put("terms", Map.of("field", "attributes.attr_name.keyword", "size", 6));
        
        Map<String, Object> attrNamesAggs = new HashMap<>();
        
        // 属性值聚合
        Map<String, Object> attrValues = new HashMap<>();
        attrValues.put("terms", Map.of("field", "attributes.attr_value.keyword", "size", 20));
        attrNamesAggs.put("attr_values", attrValues);
        
        attrNames.put("aggs", attrNamesAggs);
        onlyNormalAggs.put("attr_names", attrNames);
        
        onlyNormal.put("aggs", onlyNormalAggs);
        attributeNamesAggs.put("only_normal", onlyNormal);
        
        attributeNames.put("aggs", attributeNamesAggs);
        aggs.put("attribute_names", attributeNames);
        
        return aggs;
    }

    /**
     * 解析属性聚合结果
     */
    private List<Map<String, Object>> parseAttributeAggregations(Map<String, Object> aggregations) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> attributeNames = (Map<String, Object>) aggregations.get("attribute_names");
            if (attributeNames == null) {
                return result;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> onlyNormal = (Map<String, Object>) attributeNames.get("only_normal");
            if (onlyNormal == null) {
                return result;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> attrNames = (Map<String, Object>) onlyNormal.get("attr_names");
            if (attrNames == null) {
                return result;
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> buckets = (List<Map<String, Object>>) attrNames.get("buckets");
            if (CollUtil.isEmpty(buckets)) {
                return result;
            }
            
            for (Map<String, Object> bucket : buckets) {
                String attrName = (String) bucket.get("key");
                @SuppressWarnings("unused")
                Long docCount = (Long) bucket.get("doc_count");
                
                @SuppressWarnings("unchecked")
                Map<String, Object> attrValues = (Map<String, Object>) bucket.get("attr_values");
                if (attrValues == null) {
                    continue;
                }
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> valueBuckets = (List<Map<String, Object>>) attrValues.get("buckets");
                if (CollUtil.isEmpty(valueBuckets)) {
                    continue;
                }
                
                List<Map<String, Object>> values = valueBuckets.stream()
                        .map(valueBucket -> {
                            Map<String, Object> value = new HashMap<>();
                            value.put("value", valueBucket.get("key"));
                            value.put("count", ((Number) valueBucket.get("doc_count")).intValue());
                            return value;
                        })
                        .collect(Collectors.toList());
                
                Map<String, Object> attribute = new HashMap<>();
                attribute.put("name", attrName);
                attribute.put("values", values);
                result.add(attribute);
            }
        } catch (Exception e) {
            log.error("解析属性聚合结果失败", e);
        }
        
        return result;
    }

    /**
     * 构建商品列表查询条件
     */
    private ProductListDTO buildProductListDTO(ProductSearchClientDTO searchDTO) {
        ProductListDTO listDTO = new ProductListDTO();
        listDTO.setPage(searchDTO.getPage() == null ? 1 : searchDTO.getPage());
        listDTO.setSize(searchDTO.getSize() != null && searchDTO.getSize() <= 50 ? searchDTO.getSize() : 25);
        listDTO.setIsClient(1);
        if (searchDTO.getCat() != null) {
            listDTO.setCategoryId(searchDTO.getCat());
        }
        if (searchDTO.getBrand() != null) {
            listDTO.setBrandIds(searchDTO.getBrand());
        }
        if (searchDTO.getMin() != null) {
            listDTO.setMinPrice(searchDTO.getMin());
        }
        if (searchDTO.getMax() != null) {
            listDTO.setMaxPrice(searchDTO.getMax());
        }
        if (searchDTO.getCouponId() != null) {
            listDTO.setCouponId(searchDTO.getCouponId());
        }
        if (searchDTO.getIntro() != null) {
            listDTO.setIntroType(searchDTO.getIntro());
        }
        if (searchDTO.getShopId() != null) {
            listDTO.setShopId(searchDTO.getShopId());
        }
        if (searchDTO.getShopCategoryId() != null) {
            listDTO.setShopCategoryId(searchDTO.getShopCategoryId());
        }
        if (searchDTO.getKeyword() != null) {
            listDTO.setKeyword(searchDTO.getKeyword());
        }
        if (searchDTO.getProductStatus() != null) {
            listDTO.setProductStatus(searchDTO.getProductStatus());
        } else {
            listDTO.setProductStatus(1);
        }
        if (searchDTO.getSort() != null) {
            switch (searchDTO.getSort()) {
                case "sale" -> {
                    listDTO.setSortField("virtual_sales");
                    listDTO.setSortOrder("desc");
                }
                case "price" -> {
                    listDTO.setSortField("product_price");
                    listDTO.setSortOrder(searchDTO.getOrder());
                }
                case "time" -> {
                    listDTO.setSortField("product_id");
                    listDTO.setSortOrder("desc");
                }
                default -> {
                    listDTO.setSortField("sort_order");
                    listDTO.setSortOrder("asc");
                }
            }
            listDTO.setSort(searchDTO.getSort());
        }
        listDTO.setIsDelete(0);
        listDTO.setAttrs(searchDTO.getAttrs());
        listDTO.setPosProductSource(searchDTO.getPosProductSource());
        listDTO.setExcludePlanDeliveryCycleOnly(searchDTO.getExcludePlanDeliveryCycleOnly());
        listDTO.setExcludeGiftCardProduct(searchDTO.getExcludeGiftCardProduct());
        listDTO.setIsFilterPresaleProduct(searchDTO.getIsFilterPresaleProduct());
        return listDTO;
    }

    /**
     * 获取筛选列表分类
     */
    private List<Category> getFilterListCategoryList(ProductSearchClientDTO searchDTO) {
        // 首先检查 categoryId
        if (searchDTO.getCat() != null && searchDTO.getCat() > 0) {
            return categoryService.getCategoryByParentId(searchDTO.getCat(), 100);
        }
        // 如果 categoryId 不满足条件，检查 introType
        if (searchDTO.getIntro() != null) {
            // 获取筛选条件下的所有分类
            return productService.getProductCategoryList(buildProductListDTO(searchDTO));
        }
        // 如果 introType 也不满足条件，检查 keyword
        if (searchDTO.getKeyword() != null) {
            // 获取筛选条件下的所有分类
            return productService.getProductCategoryList(buildProductListDTO(searchDTO));
        }
        // 如果以上条件都不满足，使用默认分类
        return categoryService.getCategoryByParentId(0, 100);
    }
}
