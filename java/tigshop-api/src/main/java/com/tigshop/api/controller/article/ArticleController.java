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
import com.tigshop.bean.query.content.ArticlePageQuery;
import com.tigshop.bean.vo.content.ArticleDetailVO;
import com.tigshop.bean.vo.content.ArticleVO;
import com.tigshop.bean.vo.content.ChildrenArticleWithCategoryVO;
import com.tigshop.bean.vo.content.NewsInfoVO;
import com.tigshop.service.content.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

import static com.tigshop.common.constant.ResultTextConstants.ID_CANNOT_IS_NULL;

/**
 * @author Jayce
 * @create 2024/9/30 15:56
 */
@RestController
@RequestMapping("/api/article")
@Tag(name = "文章")
@Validated
public class ArticleController {
    @Resource
    private ArticleService articleService;

    @GetMapping("/article/list")
    @Operation(summary = "文章列表")
    public Page<ArticleVO> list(ArticlePageQuery article) {
        // 前台只显示已发布的文章
        article.setIsShow(1);
        return articleService.list(article);
    }

    @GetMapping("/article/newsInfo")
    @Operation(summary = "资讯类文章详情")
    public NewsInfoVO newsInfo(@RequestParam @NotNull(message = ID_CANNOT_IS_NULL) Integer id) {  // 获取文章详情
        ArticleDetailVO articleDetail = articleService.detail(id);
        // 构建返回结果
        ArticleVO prevArticle = articleService.getPrevArticle(id);
        ArticleVO nextArticle = articleService.getNextArticle(id);
        return NewsInfoVO.builder()
                .item(articleDetail)
                .prev(Objects.nonNull(prevArticle) && prevArticle.getArticleId()==null ? null : prevArticle)
                .next(Objects.nonNull(nextArticle) && nextArticle.getArticleId()==null ? null : nextArticle)
                .build();
    }

    @GetMapping("/article/issueInfo")
    @Operation(summary = "资讯类文章详情")
    public NewsInfoVO issueInfo(@RequestParam(name = "id", required = false) Integer id, @RequestParam(name = "articleSn", required = false, defaultValue = "") String articleSn) {  // 获取文章详情
        ArticleDetailVO articleDetail = (id == null) ? articleService.getDetailBySn(articleSn) : articleService.detail(id);
        // 构建返回结果
        return NewsInfoVO.builder()
                .item(articleDetail)
                .prev(articleService.getPrevArticle(articleDetail.getArticleId()))
                .next(articleService.getNextArticle(articleDetail.getArticleId()))
                .build();
    }

    @GetMapping("/category/indexBzzxList")
    @Operation(summary = "资讯类文章列表，包含文章")
    public List<ChildrenArticleWithCategoryVO> childrenArticleListWithCategory(
            @RequestParam("categorySize") Integer categorySize,
            @RequestParam("articleSize") Integer articleSize) {  // 获取文章详情
        return articleService.getChildrenArticleListWithCategory(categorySize, articleSize);
    }

}
