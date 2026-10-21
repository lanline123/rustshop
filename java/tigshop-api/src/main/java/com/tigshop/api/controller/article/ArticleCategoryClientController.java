// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

/*
 * ---------------------------------------------------------------------+
 *  控制器文件 -- 文章
 * ---------------------------------------------------------------------+
 *  版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
 * ---------------------------------------------------------------------+
 *  作者：Tigshop团队，yq@tigshop.com
 * ---------------------------------------------------------------------+
 *  提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
 * ---------------------------------------------------------------------+
 */
package com.tigshop.api.controller.article;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.content.ArticleCategory;
import com.tigshop.bean.query.content.ArticleCategoryQuery;
import com.tigshop.bean.query.content.ArticlePageQuery;
import com.tigshop.bean.vo.content.ArticleVO;
import com.tigshop.service.content.ArticleCategoryService;
import com.tigshop.service.content.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Jayce
 * @create 2024/9/30 15:56
 */
@RestController
@RequestMapping("/api/article/category")
@Tag(name = "文章分类")
@Validated
public class ArticleCategoryClientController {
    @Resource
    private ArticleService articleService;

    @Resource
    private ArticleCategoryService articleCategoryService;

    @GetMapping("/list")
    @Operation(summary = "文章列表")
    public Page<ArticleVO> list(ArticlePageQuery article) {
        // 前台只显示已发布的文章
        article.setIsShow(1);
        return articleService.list(article);
    }

    @Operation(summary = "文章分类列表")
    @GetMapping("/getArticleCategoryList")
    public List<ArticleCategory> getArticleCategoryList(ArticleCategoryQuery query) {
        return articleCategoryService.getArticleCategoryList(query);
    }
}
