package com.tigshop.service.content;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.content.ArticleCategory;
import com.tigshop.bean.param.content.ArticleCategoryEditParam;
import com.tigshop.bean.param.content.ArticleCategorySaveParam;
import com.tigshop.bean.query.content.ArticleCategoryPageQuery;
import com.tigshop.bean.query.content.ArticleCategoryQuery;
import com.tigshop.bean.vo.content.ArticleCategoryVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 文章分类
 *
 * @author kidd
 * @create 2025/7/4
 */
public interface ArticleCategoryService extends BaseService<ArticleCategory> {

    /**
     * 文章分类列表
     */
    Page<ArticleCategoryVO> list(ArticleCategoryPageQuery pageQuery);

    /**
     * 文章分类详情
     */
    ArticleCategoryVO detail(Integer id);

    /**
     * 创建文章分类
     */
    void create(ArticleCategorySaveParam param);

    /**
     * 更新文章分类
     */
    void update(ArticleCategoryEditParam param);

    /**
     * 文章分类树
     */
    List<Tree<Integer>> getArticleCategoryTree();

    /**
     * 文章分类详情
     */
    ArticleCategoryVO getDetailBySn(String sn);

    /**
     * 根据文章分类ID获取文章分类
     *
     * @param ids 文章分类ID
     * @return List<ArticleCategory>
     */
    List<ArticleCategory> getArticleCategoryByIds(List<Integer> ids);

    /**
     * 根据文章分类查询文章分类列表
     *
     * @param query 文章分类查询
     * @return List<ArticleCategory>
     */
    List<ArticleCategory> getArticleCategoryList(ArticleCategoryQuery query);

}
