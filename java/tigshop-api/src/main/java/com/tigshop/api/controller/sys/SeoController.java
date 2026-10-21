// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.api.controller.sys;

import com.tigshop.bean.vo.seo.SeoArticleVO;
import com.tigshop.bean.vo.seo.SeoBrandVO;
import com.tigshop.bean.vo.seo.SeoCategoryVO;
import com.tigshop.bean.vo.seo.SeoProductVO;
import com.tigshop.mapper.product.CategoryMapper;
import com.tigshop.service.seo.SeoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * seo控制器
 *
 * @author Tigshop团队
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/sys/seo")
@Tag(name = "seo")
public class SeoController {

    @Resource
    private SeoService seoService;

    @Resource
    private CategoryMapper categoryMapper;

    /**
     * 商品分类seoLink转商品分类ID
     *
     * @param seoLink 商品分类SEO链接
     * @return 商品分类ID
     */
    @GetMapping("getCategoryIdBySeoLink")
    @Operation(summary = "商品分类seoLink转商品分类ID")
    public Integer getCategoryIdBySeoLink(@RequestParam(value = "seoLink") String seoLink) {
        return seoService.getCategoryIdBySeoLink(seoLink);
    }

    @GetMapping("products")
    @Operation(summary = "站点地图-商品列表")
    public List<SeoProductVO> getSitemapProducts() {
        return seoService.getSitemapProducts();
    }

    @GetMapping("categories")
    @Operation(summary = "站点地图-分类列表")
    public List<SeoCategoryVO> getSitemapCategories() {
        return seoService.getSitemapCategories();
    }

    @GetMapping("articles")
    @Operation(summary = "站点地图-文章列表")
    public List<SeoArticleVO> getSitemapArticles() {
        return seoService.getSitemapArticles();
    }

    @GetMapping("brands")
    @Operation(summary = "站点地图-品牌列表")
    public List<SeoBrandVO> getSitemapBrands() {
        return seoService.getSitemapBrands();
    }
}
