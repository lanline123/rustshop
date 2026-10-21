package com.tigshop.service.seo;

import com.tigshop.bean.vo.seo.SeoArticleVO;
import com.tigshop.bean.vo.seo.SeoBrandVO;
import com.tigshop.bean.vo.seo.SeoCategoryVO;
import com.tigshop.bean.vo.seo.SeoProductVO;

import java.util.List;

/**
 * SEO服务接口
 *
 * @author Tigshop团队
 */
public interface SeoService {

    /**
     * 批量生成并更新分类的SEO标识
     * 查询seoIdentification为空的分类，生成并更新
     */
    void generateAndUpdateCategorySeoIdentification();

    /**
     * 批量生成并更新商品的SEO标识
     * 查询seoIdentification为空的商品，生成并更新
     */
    void generateAndUpdateProductSeoIdentification();

    /**
     * 根据分类SEO标识获取分类ID
     *
     * @param seoLink 分类SEO标识
     * @return 分类ID
     */
    Integer getCategoryIdBySeoLink(String seoLink);

    /**
     * 获取站点地图商品列表（已上架且未删除）
     */
    List<SeoProductVO> getSitemapProducts();

    /**
     * 获取站点地图分类列表（已启用，扁平化）
     */
    List<SeoCategoryVO> getSitemapCategories();

    /**
     * 获取站点地图文章列表（已发布）
     */
    List<SeoArticleVO> getSitemapArticles();

    /**
     * 获取站点地图品牌列表（有上架商品关联的品牌）
     */
    List<SeoBrandVO> getSitemapBrands();
}