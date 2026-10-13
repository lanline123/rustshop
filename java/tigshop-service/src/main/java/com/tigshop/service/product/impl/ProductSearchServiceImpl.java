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

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.collection.CollUtil;
import com.tigshop.common.properties.TigshopProperties;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.ProductListDTO;
import com.tigshop.bean.dto.product.ProductListResDTO;
import com.tigshop.bean.dto.product.ProductSearchClientDTO;
import com.tigshop.bean.model.product.ProductAttribute;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.enums.product.ProductType;
import com.tigshop.bean.vo.product.KeywordSearchListVO;
import com.tigshop.bean.vo.product.ProductSearchFilterListVO;
import com.tigshop.bean.vo.product.ProductSearchFilterSelectedVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.product.ProductAttributesMapper;
import com.tigshop.mapper.shop.ShopProductCategoryMapper;
import com.tigshop.service.product.BrandService;
import com.tigshop.service.product.CategoryService;
import com.tigshop.service.product.ProductEsSearchService;
import com.tigshop.service.product.ProductSearchService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.shop.ShopProductCategoryService;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.tigshop.bean.enums.setting.SettingsEnum.SELECT_SEARCH_TYPE;

/**
 * 商品服务接口实现
 *
 * @author Tigshop团队
 * @create 2024年11月20日 11:11
 */
@Slf4j
@Service
public class ProductSearchServiceImpl implements ProductSearchService {

    @Resource
    private CategoryService categoryService;

    @Resource
    private ProductService productService;

    @Resource
    private ShopProductCategoryMapper shopProductCategoryMapper;

    @Resource
    private ShopProductCategoryService shopProductCategoryService;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private BrandService brandService;

    @Resource
    private ProductEsSearchService productEsSearchService;

    @Resource
    private ConfigService configService;

    @Resource
    private ProductAttributesMapper productAttributesMapper;

    @Resource
    private TigshopProperties tigshopProperties;


    @Override
    public Page<ProductListResDTO> list(ProductSearchClientDTO searchDTO) {
        boolean hasLocation = validateLocation(searchDTO);
        Page<ProductListResDTO> page;
        // 判断是否使用ES搜索
        if (useEsSearch(searchDTO)) {
            try {
                page = productEsSearchService.searchProducts(searchDTO);
            } catch (RuntimeException e) {
                log.error("ES 商品搜索失败，自动回退 MySQL", e);
                page = productService.list(buildProductListDTO(searchDTO));
            }
            // 搜索结果统一回填平台商品首条批发价（CR-20260603-141000 / CD-032）：
            // ES 与 MySQL 分支均调用 productService.list()，但该方法不含批发回填
            // （回填仅在 clientList()），故两个分支都需在此显式回填。
            if (page != null) {
                productService.backfillFirstWholesalePriceForList(page.getRecords());
            }
        } else {
            page = productService.list(buildProductListDTO(searchDTO));
            if (page != null) {
                productService.backfillFirstWholesalePriceForList(page.getRecords());
            }
        }
        return page;
    }

    /**
     * 不传定位时保持旧逻辑；定位只传一项或超出合法范围时拒绝请求，避免静默选错门店。
     */
    private boolean validateLocation(ProductSearchClientDTO searchDTO) {
        BigDecimal longitude = searchDTO.getLongitude();
        BigDecimal latitude = searchDTO.getLatitude();
        if (longitude == null && latitude == null) {
            return false;
        }
        if (longitude == null || latitude == null) {
            throw new GlobalException("经度和纬度必须同时传递");
        }
        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0
                || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new GlobalException("经度范围必须在 -180 到 180 之间");
        }
        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0
                || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new GlobalException("纬度范围必须在 -90 到 90 之间");
        }
        return true;
    }

    /**
     * 构建商品列表查询条件
     *
     * @param searchDTO 查询参数
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
        if (StrUtil.isNotBlank(searchDTO.getSort()) && !"default".equals(searchDTO.getSort())) {
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
        listDTO.setIsFilterPresaleProduct(searchDTO.getIsFilterPresaleProduct());
        listDTO.setExcludeGiftCardProduct(searchDTO.getExcludeGiftCardProduct());
        return listDTO;
    }

    @Override
    public ProductSearchFilterListVO getFilterList(ProductSearchClientDTO searchDTO) {
        // 判断是否使用ES搜索
        if (useEsSearch()) {
            return productEsSearchService.getFilterList(searchDTO);
        } else {
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
            searchDTO.setMax(null);
            searchDTO.setMin(null);
            filterListVO.setMaxPrice(productService.getMaxPrice(buildProductListDTO(searchDTO)));
            return filterListVO;
        }
    }

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

    @Override
    public ProductSearchFilterSelectedVO getFilterSelected(ProductSearchClientDTO dto) {
        // 判断是否使用ES搜索
        if (useEsSearch()) {
            return productEsSearchService.getFilterSelected(dto);
        } else {
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
                filterSelectedVO.setShopCategory(shopProductCategoryMapper.selectById(dto.getShopCategoryId()).getCategoryName());
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
    }

    @Override
    public List<KeywordSearchListVO> searchGuess(String keyword) {
        if (keyword == null) {
            return new ArrayList<>();
        }
        //keyword去掉空格
        keyword = keyword.trim();
        // 如果是0-9这种太过简单的则过滤
        if (keyword.matches("^[0-9]$")) {
            keyword = "";
        }
        if (keyword.isEmpty()) {
            return new ArrayList<>();
        }
        String keywords = StringUtils.cutForSearch(keyword);
        //keywords使用' '分割
        String[] keywordsArr = keywords.split(" ");
        //从product表中查询出product_name 中包含keywordsArr元素的
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Product::getProductName, Product::getKeywords, Product::getProductId);
        queryWrapper.eq(Product::getIsDelete, 0);
        queryWrapper.and(
                (wrapper) -> {
                    for (String keywordItem : keywordsArr) {
                        wrapper.like(Product::getProductName, keywordItem);
                    }
                }
        );
        List<Product> products = productMapper.selectList(queryWrapper);

        // 处理结果
        Map<String, List<Integer>> ks = new HashMap<>();

        for (Product product : products) {
            String[] keywordsArray = product.getKeywords().split(" ");
            for (String val : keywordsArray) {
                // 关键词双向匹配
                if (!val.isEmpty()  && (keyword.contains(val) || val.contains(keyword))) {
                    ks.computeIfAbsent(val, k -> new ArrayList<>()).add(product.getProductId());
                }
            }
        }

        // 统计每个关键词的出现次数
        Map<String, Integer> arr = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : ks.entrySet()) {
            // 去重计数
            int count = (int) entry.getValue().stream().distinct().count();
            arr.put(entry.getKey(), count);
        }

        // 排序
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(arr.entrySet());
        // 按值降序排序
        sortedList.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // 获取前10个关键词
        List<KeywordSearchListVO> keywordList = new ArrayList<>();
        for (int i = 0; i < Math.min(10, sortedList.size()); i++) {
            Map.Entry<String, Integer> entry = sortedList.get(i);
            KeywordSearchListVO keywordVO = new KeywordSearchListVO();
            keywordVO.setType("search");
            keywordVO.setKeyword(entry.getKey());
            keywordVO.setCount(entry.getValue());
            keywordList.add(keywordVO);
        }
        return keywordList;

    }

    @Override
    public List<KeywordSearchListVO> searchGuessPosProduct(String keyword) {
        if (keyword == null) {
            return new ArrayList<>();
        }
        //keyword去掉空格
        keyword = keyword.trim();
        // 如果是0-9这种太过简单的则过滤
        if (keyword.matches("^[0-9]$")) {
            keyword = "";
        }
        if (keyword.isEmpty()) {
            return new ArrayList<>();
        }
        String keywords = StringUtils.cutForSearch(keyword);
        //keywords使用' '分割
        String[] keywordsArr = keywords.split(" ");
        //从product表中查询出product_name 中包含keywordsArr元素的
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Product::getProductName, Product::getKeywords, Product::getProductId);
        queryWrapper.eq(Product::getIsDelete, 0);
        // 排除礼品卡商品
        queryWrapper.notIn(Product::getProductType,
                ProductType.PRODUCT_TYPE_GIFT_CARD.getCode(),
                ProductType.PRODUCT_TYPE_GIFT_CARD_VIRTUAL.getCode());
        queryWrapper.and(
                (wrapper) -> {
                    for (String keywordItem : keywordsArr) {
                        wrapper.like(Product::getProductName, keywordItem);
                    }
                }
        );
        queryWrapper.last("LIMIT 10");
        List<Product> products = productMapper.selectList(queryWrapper);

        // 处理结果
        List<KeywordSearchListVO> keywordList = new ArrayList<>();
        for (Product product : products) {
            KeywordSearchListVO keywordVO = new KeywordSearchListVO();
            keywordVO.setType("search");
            keywordVO.setKeyword(product.getProductName());
            keywordVO.setCount(0);
            keywordList.add(keywordVO);
        }

        return keywordList;
    }

    /**
     * 判断是否使用ES搜索
     *
     * @return true使用ES，false使用MySQL
     */
    private boolean useEsSearch() {
        try {
            // 从配置中获取搜索类型
            String searchType = configService.getConfigVal(SELECT_SEARCH_TYPE);
            return "es".equalsIgnoreCase(searchType);
        } catch (Exception e) {
            // 配置获取失败时默认使用MySQL
            return false;
        }
    }

    private boolean useEsSearch(ProductSearchClientDTO searchDTO) {
        if (!useEsSearch()) {
            return false;
        }
        if (searchDTO == null) {
            return true;
        }
        // 优惠券和按计划配送周期购依赖关系表；历史全量索引也只回灌可售商品。
        // ES 若先分页再由 MySQL 过滤，会造成空洞页和错误 total，因此这些条件保留 MySQL 主查询。
        if ((searchDTO.getCouponId() != null && searchDTO.getCouponId() > 0)
                || Boolean.TRUE.equals(searchDTO.getExcludePlanDeliveryCycleOnly())
                || (searchDTO.getProductStatus() != null && searchDTO.getProductStatus() != 1)) {
            return false;
        }
        // POS 来源筛选及 O2O 门店范围包含 store_product 分配语义，本轮明确不进入 ES。
        if (CollUtil.isNotEmpty(searchDTO.getPosProductSource())) {
            return false;
        }
        return tigshopProperties.getIsO2o() != 1
                || searchDTO.getShopId() == null
                || searchDTO.getShopId() <= 0;
    }

    @Override
    public List<Map<String, Object>> getAttributeSuggestions(ProductSearchClientDTO searchDTO) {
        // 判断是否使用ES搜索
//        if (useEsSearch()) {
//            return productEsSearchService.getAttributeSuggestions(searchDTO);
//        } else {
        return getAttributeSuggestionsFromMysql(searchDTO);
        //}
    }

    /**
     * 从MySQL获取属性搜索建议
     */
    private List<Map<String, Object>> getAttributeSuggestionsFromMysql(ProductSearchClientDTO searchDTO) {
        try {
            // 构建基础查询条件
            LambdaQueryWrapper<Product> productQuery = new LambdaQueryWrapper<>();
            productQuery.eq(Product::getProductStatus, 1)
                    .eq(Product::getIsDelete, 0);

            // 分类ID
            if (searchDTO.getCat() != null && searchDTO.getCat() > 0) {
                List<Integer> ids = categoryService.getCategoryAllChildIds(searchDTO.getCat());
                productQuery.in(CollUtil.isNotEmpty(ids),Product::getCategoryId, ids);
            }

            // 品牌ID
            if (StrUtil.isNotBlank(searchDTO.getBrand())) {
                List<String> brandIds = StrUtil.split(searchDTO.getBrand(), ",");
                if (!brandIds.isEmpty()) {
                    productQuery.in(Product::getBrandId, brandIds);
                }
            }

            // 店铺ID
            if (searchDTO.getShopId() != null && searchDTO.getShopId() > 0) {
                productQuery.eq(Product::getShopId, searchDTO.getShopId());
            }

            // 店铺分类ID
            if (searchDTO.getShopCategoryId() != null && searchDTO.getShopCategoryId() > -1) {
                if (searchDTO.getShopCategoryId() > 0) {
                    productQuery.eq(Product::getShopCategoryId, searchDTO.getShopCategoryId());
                }
            }

            // 价格范围
            if (searchDTO.getMin() != null && searchDTO.getMin().compareTo(BigDecimal.ZERO) > 0) {
                productQuery.ge(Product::getProductPrice, searchDTO.getMin());
            }
            if (searchDTO.getMax() != null && searchDTO.getMax().compareTo(BigDecimal.ZERO) > 0) {
                productQuery.le(Product::getProductPrice, searchDTO.getMax());
            }

            // 商品类型筛选
            if (StrUtil.isNotBlank(searchDTO.getIntro())) {
                String introType = searchDTO.getIntro();
                if ("new".equals(introType)) {
                    productQuery.eq(Product::getIsNew, 1);
                } else if ("hot".equals(introType)) {
                    productQuery.eq(Product::getIsHot, 1);
                } else if ("best".equals(introType)) {
                    productQuery.eq(Product::getIsBest, 1);
                }
            }

            // 关键词搜索
            if (StrUtil.isNotBlank(searchDTO.getKeyword())) {
                productQuery.and(wrapper -> wrapper
                        .like(Product::getProductName, searchDTO.getKeyword())
                        .or()
                        .like(Product::getKeywords, searchDTO.getKeyword())
                        .or()
                        .like(Product::getProductSn, searchDTO.getKeyword())
                );
            }

            // 只查询产品ID
            productQuery.select(Product::getProductId);

            // 获取符合条件的商品ID
            List<Product> products = productMapper.selectList(productQuery);
            if (CollUtil.isEmpty(products)) {
                return new ArrayList<>();
            }

            List<Integer> productIds = products.stream()
                    .map(Product::getProductId)
                    .collect(Collectors.toList());

            // 查询属性数据，按属性名和属性值分组统计
            // 与PHP逻辑保持一致：SELECT attr_name, attr_value, COUNT(*) as count GROUP BY attr_name, attr_value ORDER BY count DESC
            LambdaQueryWrapper<ProductAttribute> attrQuery = new LambdaQueryWrapper<>();
            attrQuery.in(ProductAttribute::getProductId, productIds)
                    .eq(ProductAttribute::getAttrType, 0)
                    .select(ProductAttribute::getAttrName, ProductAttribute::getAttrValue)
                    .last("GROUP BY attr_name, attr_value ORDER BY COUNT(*) DESC");

            // 直接获取所有属性记录进行统计
            List<ProductAttribute> allAttributes = productAttributesMapper.selectList(
                    new LambdaQueryWrapper<ProductAttribute>()
                            .in(ProductAttribute::getProductId, productIds)
                            .eq(ProductAttribute::getAttrType, 0)
                            .select(ProductAttribute::getAttrName, ProductAttribute::getAttrValue)
            );

            if (CollUtil.isEmpty(allAttributes)) {
                return new ArrayList<>();
            }

            // 手动统计每个属性名-属性值组合的出现次数
            Map<String, Map<String, Integer>> attrCountMap = new HashMap<>();
            for (ProductAttribute attr : allAttributes) {
                String attrName = attr.getAttrName();
                String attrValue = attr.getAttrValue();

                attrCountMap.computeIfAbsent(attrName, k -> new HashMap<>())
                        .merge(attrValue, 1, Integer::sum);
            }

            // 按属性名分组，与PHP逻辑保持一致
            Map<String, Map<String, Object>> groupedAttributes = new LinkedHashMap<>();

            // 将统计结果转换为带count的格式，并按count降序排列
            for (Map.Entry<String, Map<String, Integer>> attrEntry : attrCountMap.entrySet()) {
                String attrName = attrEntry.getKey();
                Map<String, Integer> valueCountMap = attrEntry.getValue();

                // 按count降序排序
                List<Map.Entry<String, Integer>> sortedValues = valueCountMap.entrySet().stream()
                        .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                        .toList();

                if (!groupedAttributes.containsKey(attrName)) {
                    Map<String, Object> attrData = new HashMap<>();
                    attrData.put("name", attrName);
                    attrData.put("values", new ArrayList<Map<String, Object>>());
                    groupedAttributes.put(attrName, attrData);
                }

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> values = (List<Map<String, Object>>) groupedAttributes.get(attrName).get("values");

                for (Map.Entry<String, Integer> valueEntry : sortedValues) {
                    Map<String, Object> valueData = new HashMap<>();
                    valueData.put("value", valueEntry.getKey());
                    valueData.put("count", valueEntry.getValue());
                    values.add(valueData);
                }
            }

            // 限制每个属性名称最多20个值，总共最多6个属性名称，与PHP逻辑保持一致
            List<Map<String, Object>> result = new ArrayList<>();
            int count = 0;
            for (Map.Entry<String, Map<String, Object>> entry : groupedAttributes.entrySet()) {
                if (count >= 6) break;

                Map<String, Object> attrData = entry.getValue();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> values = (List<Map<String, Object>>) attrData.get("values");

                // 限制每个属性最多20个值
                if (values.size() > 20) {
                    values = values.subList(0, 20);
                }

                Map<String, Object> attribute = new HashMap<>();
                attribute.put("name", attrData.get("name"));
                attribute.put("values", values);
                result.add(attribute);

                count++;
            }
            //去掉result 里 object为空的数据
            result = result.stream()
                    .filter(item -> item.get("values") != null)
                    .collect(Collectors.toList());
            // 直接返回result，与方法签名保持一致
            return result;

        } catch (Exception e) {
            log.error("MySQL属性建议查询失败", e);
            return new ArrayList<>();
        }
    }

}
