package com.tigshop.service.seo.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tigshop.bean.model.content.Article;
import com.tigshop.bean.model.product.Brand;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.vo.seo.SeoArticleVO;
import com.tigshop.bean.vo.seo.SeoBrandVO;
import com.tigshop.bean.vo.seo.SeoCategoryVO;
import com.tigshop.bean.vo.seo.SeoProductVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.common.utils.SeoIdentificationUtil;
import com.tigshop.mapper.content.ArticleMapper;
import com.tigshop.mapper.product.BrandMapper;
import com.tigshop.mapper.product.CategoryMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.service.lang.TranslationsService;
import com.tigshop.service.seo.SeoService;
import com.volcengine.model.response.translate.TranslateTextResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.Constants.SEO_LINK_CATEGORY;

/**
 * SEO服务实现类
 *
 * @author Tigshop团队
 */

/**
 * SEO服务实现类
 *
 * @author Tigshop团队
 */
@Service
@Slf4j
public class SeoServiceImpl implements SeoService {
    
    /**
     * 批量翻译最大数量限制
     */
    private static final int MAX_BATCH_TRANSLATE_SIZE = 50;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private TranslationsService translationsService;

    @Resource
    private RedisCache redisCache;

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void generateAndUpdateCategorySeoIdentification() {
        // 查询seoIdentification为空的分类
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .isNull(Category::getSeoIdentification)
                .or()
                .eq(Category::getSeoIdentification, ""));
        
        List<Category> categories = categoryMapper.selectList(queryWrapper);
        if (categories.isEmpty()) {
            return;
        }

        // 过滤掉名称为空的分类
        categories = categories.stream()
                .filter(category -> category.getCategoryName() != null && !category.getCategoryName().isEmpty())
                .collect(Collectors.toList());
        
        if (categories.isEmpty()) {
            return;
        }

        try {
            int totalUpdatedCount = 0;
            // 分批处理，每批最多MAX_BATCH_TRANSLATE_SIZE个
            for (int i = 0; i < categories.size(); i += MAX_BATCH_TRANSLATE_SIZE) {
                int endIndex = Math.min(i + MAX_BATCH_TRANSLATE_SIZE, categories.size());
                List<Category> batchCategories = categories.subList(i, endIndex);
                
                // 提取当前批次分类名称用于批量翻译
                List<String> categoryNames = batchCategories.stream()
                        .map(Category::getCategoryName)
                        .collect(Collectors.toList());
                
                // 批量翻译
                TranslateTextResponse translationsResponse = translationsService.getTranslationsResponse("en", categoryNames);
                if (translationsResponse == null || CollUtil.isEmpty(translationsResponse.getTranslationList())) {
                    continue;
                }
                
                List<TranslateTextResponse.Translation> translationList = translationsResponse.getTranslationList();
                
                // 创建分类ID到翻译结果的映射
                Map<Integer, String> categoryIdToSeoMap = new HashMap<>();
                for (int j = 0; j < batchCategories.size() && j < translationList.size(); j++) {
                    Category category = batchCategories.get(j);
                    String englishText = translationList.get(j).getTranslation();
                    
                    // 格式化英文文本：转小写、空格替换为中划线、去除非字母数字和中划线的字符
                    String seoIdentification = SeoIdentificationUtil.formatEnglishText(englishText);
                    categoryIdToSeoMap.put(category.getCategoryId(), seoIdentification);
                }
                
                // 批量更新分类的SEO标识
                for (Map.Entry<Integer, String> entry : categoryIdToSeoMap.entrySet()) {
                    LambdaUpdateWrapper<Category> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(Category::getCategoryId, entry.getKey())
                            .set(Category::getSeoIdentification, entry.getValue());
                    
                    int result = categoryMapper.update(null, updateWrapper);
                    if (result > 0) {
                        totalUpdatedCount++;
                    }
                }
            }
            if (totalUpdatedCount > 0) {
                // 删除所有语言的 getCatFloor 缓存
                Collection<String> catFloorKeys = redisCache.keys(SEO_LINK_CATEGORY + "getCatFloor*");
                if (catFloorKeys != null && !catFloorKeys.isEmpty()) {
                    redisCache.deleteObject(catFloorKeys);
                }
            }
            log.info("批量生成分类SEO标识完成，更新数量: {}", totalUpdatedCount);
        } catch (Exception e) {
            log.error("批量生成分类SEO标识失败，错误: {}", e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void generateAndUpdateProductSeoIdentification() {
        // 查询seoIdentification为空的商品
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .isNull(Product::getSeoIdentification)
                .or()
                .eq(Product::getSeoIdentification, ""));
        
        List<Product> products = productMapper.selectList(queryWrapper);
        if (products.isEmpty()) {
            return;
        }

        // 过滤掉名称为空的商品
        products = products.stream()
                .filter(product -> product.getProductName() != null && !product.getProductName().isEmpty())
                .collect(Collectors.toList());
        
        if (products.isEmpty()) {
            return;
        }

        try {
            int totalUpdatedCount = 0;
            // 分批处理，每批最多MAX_BATCH_TRANSLATE_SIZE个
            for (int i = 0; i < products.size(); i += MAX_BATCH_TRANSLATE_SIZE) {
                int endIndex = Math.min(i + MAX_BATCH_TRANSLATE_SIZE, products.size());
                List<Product> batchProducts = products.subList(i, endIndex);
                
                // 提取当前批次商品名称用于批量翻译
                List<String> productNames = batchProducts.stream()
                        .map(Product::getProductName)
                        .collect(Collectors.toList());
                
                // 批量翻译
                TranslateTextResponse translationsResponse = translationsService.getTranslationsResponse("en", productNames);
                if (translationsResponse == null || CollUtil.isEmpty(translationsResponse.getTranslationList())) {
                    continue;
                }
                
                List<TranslateTextResponse.Translation> translationList = translationsResponse.getTranslationList();
                
                // 创建商品ID到翻译结果的映射
                Map<Integer, String> productIdToSeoMap = new HashMap<>();
                for (int j = 0; j < batchProducts.size() && j < translationList.size(); j++) {
                    Product product = batchProducts.get(j);
                    String englishText = translationList.get(j).getTranslation();
                    
                    // 格式化英文文本：转小写、空格替换为中划线、去除非字母数字和中划线的字符
                    String seoIdentification = SeoIdentificationUtil.formatEnglishText(englishText);
                    productIdToSeoMap.put(product.getProductId(), seoIdentification);
                }
                
                // 批量更新商品的SEO标识
                for (Map.Entry<Integer, String> entry : productIdToSeoMap.entrySet()) {
                    LambdaUpdateWrapper<Product> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(Product::getProductId, entry.getKey())
                            .set(Product::getSeoIdentification, entry.getValue());
                    
                    int result = productMapper.update(null, updateWrapper);
                    if (result > 0) {
                        totalUpdatedCount++;
                    }
                }
            }
            
            log.info("批量生成商品SEO标识完成，更新数量: {}", totalUpdatedCount);
        } catch (Exception e) {
            log.error("批量生成商品SEO标识失败，错误: {}", e.getMessage());
        }
    }

    @Override
    public Integer getCategoryIdBySeoLink(String seoLink) {
        // 从seoLink中提取seoIdentification
        String seoIdentification = SeoIdentificationUtil.extractSeoIdentificationFromLink(seoLink);

        // 根据seoIdentification查询分类
        Category category = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                .eq(Category::getSeoIdentification, seoIdentification)
                .last("LIMIT 1")
        );

        if (category == null) {
            // 在翻译内容里在查一次
            Object cacheObject = redisCache.getCacheObject(SEO_LINK_CATEGORY + seoIdentification);
            if (cacheObject != null) {
                category = categoryMapper.selectById(cacheObject.toString());
            }
        }

        if (category == null) {
            throw new GlobalException("未找到对应的商品分类");
        }

        return category.getCategoryId();
    }

    @Override
    public List<SeoProductVO> getSitemapProducts() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getProductStatus, 1)
                .eq(Product::getIsDelete, 0)
                .select(Product::getProductSn, Product::getProductName,
                        Product::getSeoIdentification, Product::getLastUpdate, Product::getPicUrl);

        return productMapper.selectList(wrapper).stream()
                .map(p -> SeoProductVO.builder()
                        .productSn(p.getProductSn())
                        .productName(p.getProductName())
                        .seoLink(StrUtil.isNotBlank(p.getSeoIdentification()) ? p.getSeoIdentification() : null)
                        .lastUpdate(p.getLastUpdate())
                        .picUrl(p.getPicUrl())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<SeoCategoryVO> getSitemapCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getIsShow, 1)
                .select(Category::getCategoryId, Category::getCategoryName, Category::getSeoIdentification);

        return categoryMapper.selectList(wrapper).stream()
                .map(c -> SeoCategoryVO.builder()
                        .id(String.valueOf(c.getCategoryId()))
                        .categoryName(c.getCategoryName())
                        .seoLink(StrUtil.isNotBlank(c.getSeoIdentification()) ? c.getSeoIdentification() : null)
                        .lastUpdate(null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<SeoArticleVO> getSitemapArticles() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsShow, 1)
                .select(Article::getArticleId, Article::getArticleTitle, Article::getAddTime);

        return articleMapper.selectList(wrapper).stream()
                .map(a -> {
                    Long lastUpdate = null;
                    if (a.getAddTime() != null && a.getAddTime() > 0) {
                        // addTime 为秒级时间戳时转换为毫秒；已经是毫秒级则直接使用
                        lastUpdate = a.getAddTime() < 10000000000L ? a.getAddTime() * 1000 : a.getAddTime();
                    }
                    return SeoArticleVO.builder()
                            .id(String.valueOf(a.getArticleId()))
                            .articleTitle(a.getArticleTitle())
                            .lastUpdate(lastUpdate)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SeoBrandVO> getSitemapBrands() {
        // 查询有上架且未删除商品关联的品牌ID
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(Product::getProductStatus, 1)
                .eq(Product::getIsDelete, 0)
                .isNotNull(Product::getBrandId)
                .ne(Product::getBrandId, 0)
                .select(Product::getBrandId)
                .groupBy(Product::getBrandId);
        List<Integer> activeBrandIds = productMapper.selectList(productWrapper).stream()
                .map(Product::getBrandId)
                .distinct()
                .collect(Collectors.toList());

        if (activeBrandIds.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Brand> brandWrapper = new LambdaQueryWrapper<>();
        brandWrapper.in(Brand::getBrandId, activeBrandIds)
                .eq(Brand::getIsShow, 1)
                .select(Brand::getBrandId, Brand::getBrandName);

        return brandMapper.selectList(brandWrapper).stream()
                .map(b -> SeoBrandVO.builder()
                        .id(String.valueOf(b.getBrandId()))
                        .brandName(b.getBrandName())
                        .seoLink(null)
                        .lastUpdate(null)
                        .build())
                .collect(Collectors.toList());
    }
}