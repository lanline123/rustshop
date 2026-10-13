package com.tigshop.service.content.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.content.ArticleCategory;
import com.tigshop.bean.param.content.ArticleCategoryEditParam;
import com.tigshop.bean.param.content.ArticleCategorySaveParam;
import com.tigshop.bean.query.content.ArticleCategoryPageQuery;
import com.tigshop.bean.query.content.ArticleCategoryQuery;
import com.tigshop.bean.vo.content.ArticleCategoryVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.content.ArticleCategoryMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.content.ArticleCategoryService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;
import static com.tigshop.common.constant.content.ArticleCategoryConstants.ARTICLE_CATEGORY_NAME_EXISTS;
import static com.tigshop.common.constant.content.ArticleCategoryConstants.ARTICLE_CATEGORY_NOT_FOUND;

/**
 * 文章分类
 *
 * @author kidd
 * @create 2025/7/4
 */
@Service
public class ArticleCategoryServiceImpl extends BaseServiceImpl<ArticleCategoryMapper, ArticleCategory> implements ArticleCategoryService {

    @Override
    public Page<ArticleCategoryVO> list(ArticleCategoryPageQuery pageQuery) {

        Page<ArticleCategory> page = buildSortOrder(pageQuery);

        Page<ArticleCategory> articleCategoryPage = this.lambdaQuery()
                .eq(ArticleCategory::getParentId, pageQuery.getParentId())
                .page(page);

        List<ArticleCategory> records = articleCategoryPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        // 获取所有父分类ID
        List<Integer> parentIds = records.stream().map(ArticleCategory::getArticleCategoryId).toList();
        Map<Integer, List<ArticleCategory>> childrenMap = new HashMap<>();
        if (CollUtil.isNotEmpty(parentIds)) {
            List<ArticleCategory> childCategories = this.lambdaQuery().in(ArticleCategory::getParentId, parentIds).list();
            childrenMap = childCategories.stream().collect(Collectors.groupingBy(ArticleCategory::getParentId));
        }


        Map<Integer, List<ArticleCategory>> finalChildrenMap = childrenMap;
        List<ArticleCategoryVO> resultRecords = records.stream()
                .map(item -> {
                    ArticleCategoryVO articleCategoryVO = BeanUtil.copyProperties(item, ArticleCategoryVO.class);
                    List<ArticleCategory> articleCategories = finalChildrenMap.get(articleCategoryVO.getArticleCategoryId());
                    articleCategoryVO.setHasChildren(CollUtil.isNotEmpty(articleCategories) ? Constants.YES : Constants.NO);
                    return articleCategoryVO;
                })
                .collect(Collectors.toList());

        return PageUtil.convertPage(articleCategoryPage, resultRecords);
    }

    @Override
    public ArticleCategoryVO detail(Integer id) {
        ArticleCategory articleCategory = this.getById(id);
        Assert.notNull(articleCategory, () -> new GlobalException(ARTICLE_CATEGORY_NOT_FOUND));

        return BeanUtil.copyProperties(articleCategory, ArticleCategoryVO.class);
    }

    @Override
    public void create(ArticleCategorySaveParam param) {
        // 检查文章分类名称是否重复
        Long nameCount = this.lambdaQuery().eq(ArticleCategory::getArticleCategoryName, param.getArticleCategoryName()).count();
        Assert.isTrue(nameCount == 0, () -> new GlobalException(ARTICLE_CATEGORY_NAME_EXISTS, SERVICE_DATA_ERROR));

        ArticleCategory articleCategory = param.createArticleCategory();
        this.save(articleCategory);
    }

    @Override
    public void update(ArticleCategoryEditParam param) {
        ArticleCategory articleCategory = this.getById(param.getArticleCategoryId());
        Assert.notNull(articleCategory, () -> new GlobalException(ARTICLE_CATEGORY_NOT_FOUND));

        // 检查文章分类名称是否重复
        Long nameCount = this.lambdaQuery()
                .eq(ArticleCategory::getArticleCategoryName, param.getArticleCategoryName())
                .ne(ArticleCategory::getArticleCategoryId, param.getArticleCategoryId())
                .count();
        Assert.isTrue(nameCount == 0, () -> new GlobalException(ARTICLE_CATEGORY_NAME_EXISTS, SERVICE_DATA_ERROR));

        ArticleCategory updateArticleCategory = param.createArticleCategory();
        this.updateById(updateArticleCategory);
    }

    @Override
    public List<Tree<Integer>> getArticleCategoryTree() {
        List<ArticleCategory> categories = this.list();

        if (categories.isEmpty()) {
            return Collections.emptyList();
        }

        TreeNodeConfig config = new TreeNodeConfig();
        config.setIdKey("articleCategoryId");
        config.setParentIdKey("parentId");
        config.setNameKey("articleCategoryName");
        config.setWeightKey("sortOrder");
        config.setDeep(5);

        return TreeUtil.build(categories, 0, config, (object, treeNode) -> {
            treeNode.putExtra("articleCategoryId", object.getArticleCategoryId());
            treeNode.putExtra("parentId", object.getParentId());
            treeNode.putExtra("articleCategoryName", object.getArticleCategoryName());
            treeNode.putExtra("sortOrder", object.getSortOrder());
            treeNode.putExtra("categorySn", object.getCategorySn());
            treeNode.putExtra("categoryType", object.getCategoryType());
        });
    }

    @Override
    public ArticleCategoryVO getDetailBySn(String sn) {
        ArticleCategory articleCategory = this.lambdaQuery().eq(ArticleCategory::getCategorySn, sn).one();
        Assert.notNull(articleCategory, () -> new GlobalException(ARTICLE_CATEGORY_NOT_FOUND));

        return BeanUtil.copyProperties(articleCategory, ArticleCategoryVO.class);
    }

    @Override
    public List<ArticleCategory> getArticleCategoryByIds(List<Integer> ids) {
        // 如果为空或null，提前返回
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        // 过滤掉id为0的元素
        List<Integer> filteredIds = new ArrayList<>(ids);
        filteredIds.removeIf(id -> id == 0);

        // 如果过滤后为空，也直接返回
        if (filteredIds.isEmpty()) {
            return List.of();
        }

        // 使用LambdaQueryWrapper查询
        LambdaQueryWrapper<ArticleCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ArticleCategory::getArticleCategoryId, filteredIds);
        return this.list(queryWrapper);
    }

    @Override
    public List<ArticleCategory> getArticleCategoryList(ArticleCategoryQuery query) {
        Integer parentId = query.getParentId() == null ? 0 : query.getParentId();
        return this.lambdaQuery()
                .eq(ArticleCategory::getParentId, parentId)
                .orderByAsc(ArticleCategory::getSortOrder)
                .list();
    }


}