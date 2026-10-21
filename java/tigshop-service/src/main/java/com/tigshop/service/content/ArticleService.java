package com.tigshop.service.content;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.param.content.ArticleBatchParam;
import com.tigshop.bean.model.content.Article;
import com.tigshop.bean.param.content.ArticleEditParam;
import com.tigshop.bean.param.content.ArticleSaveParam;
import com.tigshop.bean.query.content.ArticlePageQuery;
import com.tigshop.bean.vo.content.ArticleDetailVO;
import com.tigshop.bean.vo.content.ArticleVO;
import com.tigshop.bean.vo.content.ChildrenArticleWithCategoryVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 文章
 *
 * @author kidd
 * @create 2025/7/4
 */
public interface ArticleService extends BaseService<Article> {

    /**
     * 文章列表
     */
    Page<ArticleVO> list(ArticlePageQuery pageQuery);

    /**
     * 文章详情
     */
    ArticleDetailVO detail(Integer id);

    /**
     * 创建文章
     */
    void create(ArticleSaveParam param);

    /**
     * 更新文章
     */
    void update(ArticleEditParam param);

    /**
     * 批量操作
     */
    void batch(ArticleBatchParam param);

    /**
     * 根据文章编号获取文章详情
     *
     * @param articleSn 文章编号
     * @return ItemVO
     */
    ArticleDetailVO getDetailBySn(String articleSn);

    /**
     * 上一篇和下一篇
     *
     * @param id 文章ID
     * @return ItemVO
     */
    ArticleVO getNextArticle(Integer id);

    ArticleVO getPrevArticle(Integer id);

    List<ChildrenArticleWithCategoryVO> getChildrenArticleListWithCategory(Integer categorySize, Integer articleSize);
}
