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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.model.lang.Locales;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductAttribute;
import com.tigshop.common.config.ElasticsearchConfig;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.ApiResponse;
import com.tigshop.common.utils.ElasticsearchUtil;
import com.tigshop.mapper.lang.LocalesMapper;
import com.tigshop.mapper.product.ProductAttributesMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.service.product.ProductEsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 产品ES服务实现
 *
 * @author Tigshop团队
 * @create 2025年02月12日 15:00
 */
@Slf4j
@Service
public class ProductEsServiceImpl implements ProductEsService {

    private volatile boolean indexMappingReady;

    @Resource
    private ElasticsearchUtil elasticsearchUtil;

    @Resource
    private ElasticsearchConfig elasticsearchConfig;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private ProductAttributesMapper productAttributesMapper;

    @Resource
    private  TranslatePackage translatePackage;

    @Resource
    private LocalesMapper localesMapper;

    @Autowired
    private TigshopProperties tigshopProperties;

    @Override
    public Map<String, Object> initIndex() {
        try {
            String indexName = elasticsearchConfig.getProductIndex();
            
            // 检查索引是否已存在
            if (elasticsearchUtil.indexExists(indexName)) {
                ApiResponse<Object> response = elasticsearchUtil.updateIndexMapping(indexName, buildProductMapping());
                Map<String, Object> result = new HashMap<>();
                result.put("success", response.isSuccess());
                result.put("message", response.isSuccess() ? "索引映射更新成功" : response.getMessage());
                indexMappingReady = response.isSuccess();
                return result;
            }

            // 索引设置
            Map<String, Object> settings = new HashMap<>();
            settings.put("number_of_shards", "1");
            settings.put("number_of_replicas", "0");

            // 索引映射
            Map<String, Object> mapping = buildProductMapping();

            // 创建索引
            ApiResponse<Object> response = elasticsearchUtil.createIndex(indexName, settings, mapping);
            
            Map<String, Object> result = new HashMap<>();
            if (response.isSuccess()) {
                log.info("产品索引 {} 创建成功", indexName);
                result.put("success", true);
                result.put("message", "索引创建成功");
                indexMappingReady = true;
            } else {
                log.error("产品索引 {} 创建失败: {}", indexName, response.getMessage());
                result.put("success", false);
                result.put("message", "索引创建失败: " + response.getMessage());
            }
            
            return result;
        } catch (Exception e) {
            log.error("初始化产品索引失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "初始化产品索引失败: " + e.getMessage());
            return result;
        }
    }



    @Override
    public Map<String, Object> syncProduct(Long productId) {
        try {
            Map<String, Object> initResult = ensureIndexMappingReady();
            if (!Boolean.TRUE.equals(initResult.get("success"))) {
                return initResult;
            }

            //查看激活的语言
            List<Locales> locales = getEnableLocalList();

            // 查询产品信息
            Product product = productMapper.selectById(productId);
            if (product == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "产品不存在");
                return result;
            }

            // 构建产品文档
            Map<String, Object> document = buildProductDocument(product,locales);
            
            // 索引到ES
            String indexName = elasticsearchConfig.getProductIndex();
            ApiResponse<Object> response = elasticsearchUtil.indexDocument(indexName, productId.toString(), document);
            
            Map<String, Object> result = new HashMap<>();
            if (response.isSuccess()) {
                log.info("产品 {} 同步到ES成功", productId);
                result.put("success", true);
                result.put("message", "产品同步成功");
            } else {
                log.error("产品 {} 同步到ES失败: {}", productId, response.getMessage());
                result.put("success", false);
                result.put("message", "产品同步失败: " + response.getMessage());
            }
            
            return result;
        } catch (Exception e) {
            log.error("同步产品 {} 到ES失败", productId, e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "同步产品失败: " + e.getMessage());
            return result;
        }
    }

    private List<Locales> getEnableLocalList() {
        List<Locales> locales;
        if(tigshopProperties.getIsOverseas() == 1){
            locales = localesMapper.selectList(new LambdaQueryWrapper<Locales>().eq(Locales::getIsEnabled, 1));
        }else{
            locales = Collections.emptyList();
        }
        return locales;
    }

    @Override
    public Map<String, Object> batchSyncProducts(List<Long> productIds, Integer batchSize) {
        try {
            if (CollUtil.isEmpty(productIds)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "产品ID列表不能为空");
                return result;
            }

            Map<String, Object> initResult = ensureIndexMappingReady();
            if (!Boolean.TRUE.equals(initResult.get("success"))) {
                return initResult;
            }

            String indexName = elasticsearchConfig.getProductIndex();
            int total = productIds.size();
            int successCount = 0;
            int errorCount = 0;
            List<String> errors = new ArrayList<>();

            List<Locales> locales = getEnableLocalList();

            // 分批处理
            for (int i = 0; i < total; i += batchSize) {
                int endIndex = Math.min(i + batchSize, total);
                List<Long> batchIds = productIds.subList(i, endIndex);
                
                try {
                    // 查询产品列表
                    List<Product> products = productMapper.selectBatchIds(batchIds);
                    if (CollUtil.isEmpty(products)) {
                        continue;
                    }

                    // 构建文档列表
                    List<Map<String, Object>> documents = new ArrayList<>();
                    for (Product product : products) {
                        Map<String, Object> document = buildProductDocument(product,locales);
                        Map<String, Object> doc = new HashMap<>();
                        doc.put("id", product.getProductId().toString());
                        doc.put("data", document);
                        documents.add(doc);
                    }

                    // 批量索引
                    ApiResponse<Object> result = elasticsearchUtil.bulkIndex(indexName, documents);
                    if (result.isSuccess()) {
                        successCount += products.size();
                        log.info("批量同步产品成功，批次: {}-{}, 数量: {}", i, endIndex, products.size());
                    } else {
                        errorCount += products.size();
                        errors.add("批次 " + i + "-" + endIndex + ": " + result.getMessage());
                        log.error("批量同步产品失败，批次: {}-{}, 错误: {}", i, endIndex, result.getMessage());
                    }
                } catch (Exception e) {
                    errorCount += batchIds.size();
                    errors.add("批次 " + i + "-" + endIndex + ": " + e.getMessage());
                    log.error("批量同步产品异常，批次: {}-{}", i, endIndex, e);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "批量同步完成");
            result.put("total", total);
            result.put("success_count", successCount);
            result.put("error_count", errorCount);
            result.put("errors", errors);

            return result;
        } catch (Exception e) {
            log.error("批量同步产品失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "批量同步产品失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> syncAllProducts(Integer batchSize) {
        try {
            // 查询所有产品ID
            LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(Product::getProductId);
            queryWrapper.eq(Product::getIsDelete, 0);
            List<Product> products = productMapper.selectList(queryWrapper);
            
            if (CollUtil.isEmpty(products)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "没有需要同步的产品");
                return result;
            }

            List<Long> productIds = products.stream()
                    .map(product -> Long.valueOf(product.getProductId()))
                    .collect(Collectors.toList());

            return batchSyncProducts(productIds, batchSize);
        } catch (Exception e) {
            log.error("全量同步产品失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "全量同步产品失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> syncAllProductsStream(Integer batchSize) {
        try {
            String indexName = elasticsearchConfig.getProductIndex();
            int totalCount = 0;
            int successCount = 0;
            int errorCount = 0;
            List<String> errors = new ArrayList<>();
            // 复用现有索引：不存在则创建，存在则增量补充兼容 mapping。
            Map<String, Object> initResult = ensureIndexMappingReady();
            if (!(Boolean) initResult.get("success")) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "索引 " + indexName + " 初始化失败: " + initResult.get("message"));
                return result;
            }
            // 流式查询，避免内存溢出
            int offset = 0;
            while (true) {
                LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.select(Product::getProductId);
                queryWrapper.eq(Product::getIsDelete, 0);
                queryWrapper.eq(Product::getProductStatus, 1);
                queryWrapper.last("LIMIT " + offset + ", " + batchSize);
                
                List<Product> products = productMapper.selectList(queryWrapper);
                if (CollUtil.isEmpty(products)) {
                    break;
                }

                List<Long> productIds = products.stream()
                        .map(product -> Long.valueOf(product.getProductId()))
                        .collect(Collectors.toList());

                Map<String, Object> result = batchSyncProducts(productIds, batchSize);
                if ((Boolean) result.get("success")) {
                    successCount += ((Number) result.getOrDefault("success_count", 0)).intValue();
                    errorCount += ((Number) result.getOrDefault("error_count", 0)).intValue();
                    @SuppressWarnings("unchecked")
                    List<String> errorList = (List<String>) result.get("errors");
                    if (errorList != null) {
                        errors.addAll(errorList);
                    }
                } else {
                    errorCount += products.size();
                    errors.add("批次 " + offset + ": " + result.get("message"));
                }

                totalCount += products.size();
                offset += batchSize;
                
                log.info("流式同步进度: {}/{}", totalCount, "未知");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "流式全量同步完成");
            result.put("total", totalCount);
            result.put("success_count", successCount);
            result.put("error_count", errorCount);
            result.put("errors", errors);

            return result;
        } catch (Exception e) {
            log.error("流式全量同步产品失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "流式全量同步产品失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> deleteProduct(Long productId) {
        try {
            String indexName = elasticsearchConfig.getProductIndex();
            ApiResponse<Object> response = elasticsearchUtil.deleteDocument(indexName, productId.toString());
            
            Map<String, Object> result = new HashMap<>();
            if (response.isSuccess()) {
                log.info("从ES删除产品 {} 成功", productId);
                result.put("success", true);
                result.put("message", "删除产品成功");
            } else {
                log.error("从ES删除产品 {} 失败: {}", productId, response.getMessage());
                result.put("success", false);
                result.put("message", "删除产品失败: " + response.getMessage());
            }
            
            return result;
        } catch (Exception e) {
            log.error("从ES删除产品 {} 失败", productId, e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "删除产品失败: " + e.getMessage());
            return result;
        }
    }

    private Map<String, Object> ensureIndexMappingReady() {
        if (indexMappingReady) {
            return Map.of("success", true, "message", "索引映射已就绪");
        }
        synchronized (this) {
            if (indexMappingReady) {
                return Map.of("success", true, "message", "索引映射已就绪");
            }
            Map<String, Object> result = initIndex();
            if (!Boolean.TRUE.equals(result.get("success"))) {
                Map<String, Object> failure = new HashMap<>(result);
                failure.put("message", "索引映射未就绪: " + result.get("message"));
                return failure;
            }
            return result;
        }
    }

    @Override
    public Map<String, Object> checkConnection() {
        try {
            // 尝试获取集群信息
            String indexName = elasticsearchConfig.getProductIndex();
            if (elasticsearchUtil.indexExists(indexName)) {
                Map<String, Object> result = new HashMap<>();
                result.put("connected", true);
                result.put("message", "ES连接正常");
                return result;
            } else {
                // 如果索引不存在，尝试创建一个临时索引来测试连接
                Map<String, Object> settings = new HashMap<>();
                settings.put("number_of_shards", "1");
                settings.put("number_of_replicas", "0");
                
                ApiResponse<Object> result = elasticsearchUtil.createIndex("test_connection", settings, new HashMap<>());
                if (result.isSuccess()) {
                    // 删除测试索引
                    elasticsearchUtil.deleteIndex("test_connection");
                    Map<String, Object> response = new HashMap<>();
                    response.put("connected", true);
                    response.put("message", "ES连接正常");
                    return response;
                } else {
                    Map<String, Object> response = new HashMap<>();
                    response.put("connected", false);
                    response.put("message", "ES连接失败: " + result.getMessage());
                    return response;
                }
            }
        } catch (Exception e) {
            log.error("检查ES连接失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("connected", false);
            result.put("message", "ES连接失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 构建产品映射
     */
    private Map<String, Object> buildProductMapping() {
        Map<String, Object> mapping = new HashMap<>();
        
        // 产品ID
        Map<String, Object> productId = new HashMap<>();
        productId.put("type", "long");
        mapping.put("product_id", productId);
        
        // 产品名称
        Map<String, Object> productName = new HashMap<>();
        productName.put("type", "text");
        productName.put("analyzer", "ik_smart");
        mapping.put("product_name", productName);
        
        // 产品编号
        Map<String, Object> productSn = new HashMap<>();
        productSn.put("type", "keyword");
        mapping.put("product_sn", productSn);

        Map<String, Object> productTsn = new HashMap<>();
        productTsn.put("type", "keyword");
        mapping.put("product_tsn", productTsn);
        
        // 关键词
        Map<String, Object> keywords = new HashMap<>();
        keywords.put("type", "text");
        keywords.put("analyzer", "ik_smart");
        mapping.put("keywords", keywords);
        
        // 分类ID
        Map<String, Object> categoryId = new HashMap<>();
        categoryId.put("type", "long");
        mapping.put("category_id", categoryId);
        
        // 品牌ID
        Map<String, Object> brandId = new HashMap<>();
        brandId.put("type", "long");
        mapping.put("brand_id", brandId);
        
        // 店铺ID
        Map<String, Object> shopId = new HashMap<>();
        shopId.put("type", "long");
        mapping.put("shop_id", shopId);
        
        // 店铺分类ID
        Map<String, Object> shopCategoryId = new HashMap<>();
        shopCategoryId.put("type", "long");
        mapping.put("shop_category_id", shopCategoryId);
        
        // 产品价格
        Map<String, Object> productPrice = new HashMap<>();
        productPrice.put("type", "float");
        mapping.put("product_price", productPrice);
        
        // 市场价格
        Map<String, Object> marketPrice = new HashMap<>();
        marketPrice.put("type", "float");
        mapping.put("market_price", marketPrice);
        
        // 库存
        Map<String, Object> productStock = new HashMap<>();
        productStock.put("type", "integer");
        mapping.put("product_stock", productStock);
        
        // 虚拟销量
        Map<String, Object> virtualSales = new HashMap<>();
        virtualSales.put("type", "integer");
        mapping.put("virtual_sales", virtualSales);
        
        // 产品状态
        Map<String, Object> productStatus = new HashMap<>();
        productStatus.put("type", "integer");
        mapping.put("product_status", productStatus);

        Map<String, Object> productType = new HashMap<>();
        productType.put("type", "integer");
        mapping.put("product_type", productType);

        Map<String, Object> deliveryMode = new HashMap<>();
        deliveryMode.put("type", "integer");
        mapping.put("delivery_mode", deliveryMode);

        Map<String, Object> saleMode = new HashMap<>();
        saleMode.put("type", "integer");
        mapping.put("sale_mode", saleMode);

        Map<String, Object> sortOrder = new HashMap<>();
        sortOrder.put("type", "integer");
        mapping.put("sort_order", sortOrder);
        
        // 是否精品
        Map<String, Object> isBest = new HashMap<>();
        isBest.put("type", "integer");
        mapping.put("is_best", isBest);
        
        // 是否新品
        Map<String, Object> isNew = new HashMap<>();
        isNew.put("type", "integer");
        mapping.put("is_new", isNew);
        
        // 是否热销
        Map<String, Object> isHot = new HashMap<>();
        isHot.put("type", "integer");
        mapping.put("is_hot", isHot);
        
        // 是否删除
        Map<String, Object> isDelete = new HashMap<>();
        isDelete.put("type", "integer");
        mapping.put("is_delete", isDelete);
        
        // 创建时间
        Map<String, Object> createdAt = new HashMap<>();
        createdAt.put("type", "date");
        mapping.put("created_at", createdAt);
        
        // 更新时间
        Map<String, Object> updatedAt = new HashMap<>();
        updatedAt.put("type", "date");
        mapping.put("updated_at", updatedAt);
        
        // 属性（嵌套类型）
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("type", "nested");
        Map<String, Object> attrProperties = new HashMap<>();
        
        Map<String, Object> attrName = new HashMap<>();
        attrName.put("type", "text");
        attrName.put("analyzer", "ik_smart");
        Map<String, Object> attrNameFields = new HashMap<>();
        Map<String, Object> attrNameKeyword = new HashMap<>();
        attrNameKeyword.put("type", "keyword");
        attrNameFields.put("keyword", attrNameKeyword);
        attrName.put("fields", attrNameFields);
        attrProperties.put("attr_name", attrName);
        
        Map<String, Object> attrValue = new HashMap<>();
        attrValue.put("type", "text");
        attrValue.put("analyzer", "ik_smart");
        Map<String, Object> attrValueFields = new HashMap<>();
        Map<String, Object> attrValueKeyword = new HashMap<>();
        attrValueKeyword.put("type", "keyword");
        attrValueFields.put("keyword", attrValueKeyword);
        attrValue.put("fields", attrValueFields);
        attrProperties.put("attr_value", attrValue);
        
        Map<String, Object> attrType = new HashMap<>();
        attrType.put("type", "integer");
        attrProperties.put("attr_type", attrType);
        
        attributes.put("properties", attrProperties);
        mapping.put("attributes", attributes);
        
        return mapping;
    }

    /**
     * 构建产品文档
     */
    private Map<String, Object> buildProductDocument(Product product,List<Locales> locales) {
        Map<String, Object> document = new HashMap<>();
        document.put("product_id", product.getProductId());
        //激活的翻译数据
        if(CollUtil.isEmpty(locales)){
            document.put("product_name", product.getProductName());
        }else{
            String productName = product.getProductName();
            StringJoiner joiner = new StringJoiner(" ");
            joiner.add(productName);
            for (Locales locale : locales) {
                String productNameLocal = translatePackage.translate(productName,2,locale.getLocaleCode());
                if(!Objects.equals(productNameLocal,productName) && !StrUtil.isBlank(productNameLocal)){
                    joiner.add(productNameLocal);
                }
            }
            document.put("product_name",joiner.toString());
        }
        document.put("product_sn", product.getProductSn());
        document.put("product_tsn", StrUtil.blankToDefault(product.getProductTsn(), ""));
        document.put("keywords", StrUtil.blankToDefault(product.getKeywords(), ""));
        document.put("category_id", product.getCategoryId());
        document.put("brand_id", product.getBrandId());
        document.put("shop_id", product.getShopId());
        document.put("shop_category_id", product.getShopCategoryId());
        document.put("product_price", product.getProductPrice());
        document.put("market_price", product.getMarketPrice());
        document.put("product_stock", product.getProductStock());
        document.put("virtual_sales", product.getVirtualSales());
        document.put("product_status", product.getProductStatus());
        document.put("product_type", product.getProductType());
        document.put("delivery_mode", product.getDeliveryMode());
        document.put("sale_mode", product.getSaleMode());
        document.put("sort_order", product.getSortOrder());
        document.put("is_best", product.getIsBest());
        document.put("is_new", product.getIsNew());
        document.put("is_hot", product.getIsHot());
        document.put("is_delete", product.getIsDelete());

        // 时间格式化
        if (product.getAddTime() != null) {
            document.put("created_at", product.getAddTime());
        }
        if (product.getAddTime() != null) {
            document.put("updated_at", product.getAddTime());
        }

        // 查询产品属性
        List<Map<String, Object>> attributes = getProductAttributes(Long.valueOf(product.getProductId()));
        document.put("attributes", attributes);

        return document;
    }

    /**
     * 获取产品属性
     */
    private List<Map<String, Object>> getProductAttributes(Long productId) {
        try {
            // 使用ProductAttributesMapper查询产品属性
            LambdaQueryWrapper<ProductAttribute> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ProductAttribute::getProductId, productId.intValue());
            queryWrapper.eq(ProductAttribute::getAttrType, 0);
            List<ProductAttribute> attributes = productAttributesMapper.selectList(queryWrapper);
            
            if (CollUtil.isEmpty(attributes)) {
                return new ArrayList<>();
            }
            
            // 将ProductAttribute对象转换为ES文档格式的Map
            return attributes.stream()
                    .map(this::convertAttributeToMap)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取产品 {} 属性失败", productId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 将ProductAttribute对象转换为ES文档格式的Map
     */
    private Map<String, Object> convertAttributeToMap(ProductAttribute attribute) {
        Map<String, Object> attrMap = new HashMap<>();
        attrMap.put("attributes_id", attribute.getAttributesId());
        attrMap.put("product_id", attribute.getProductId());
        attrMap.put("attr_type", attribute.getAttrType());
        attrMap.put("attr_name", StrUtil.blankToDefault(attribute.getAttrName(), ""));
        attrMap.put("attr_value", StrUtil.blankToDefault(attribute.getAttrValue(), ""));
        attrMap.put("attr_price", attribute.getAttrPrice());
        attrMap.put("attr_color", StrUtil.blankToDefault(attribute.getAttrColor(), ""));
        attrMap.put("attr_pic", StrUtil.blankToDefault(attribute.getAttrPic(), ""));
        attrMap.put("attr_pic_thumb", StrUtil.blankToDefault(attribute.getAttrPicThumb(), ""));
        return attrMap;
    }
}
