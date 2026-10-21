package com.tigshop.service.content.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.content.Article;
import com.tigshop.bean.model.content.ArticleCategory;
import com.tigshop.bean.model.product.ProductArticle;
import com.tigshop.bean.param.content.ArticleBatchParam;
import com.tigshop.bean.param.content.ArticleEditParam;
import com.tigshop.bean.param.content.ArticleSaveParam;
import com.tigshop.bean.query.content.ArticleCategoryPageQuery;
import com.tigshop.bean.query.content.ArticlePageQuery;
import com.tigshop.bean.vo.content.ArticleCategoryVO;
import com.tigshop.bean.vo.content.ArticleDetailVO;
import com.tigshop.bean.vo.content.ArticleVO;
import com.tigshop.bean.vo.content.ChildrenArticleWithCategoryVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.content.ArticleMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.content.ArticleCategoryService;
import com.tigshop.service.content.ArticleService;
import com.tigshop.service.product.ProductArticleService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;
import static com.tigshop.common.constant.content.ArticleConstants.*;

/**
 * 文章
 *
 * @author kidd
 * @create 2025/7/4
 */
@RequiredArgsConstructor
@Service
public class ArticleServiceImpl extends BaseServiceImpl<ArticleMapper, Article> implements
    ArticleService {

  private final ArticleCategoryService articleCategoryService;
  private final ProductArticleService productArticleService;

  @Override
  public Page<ArticleVO> list(ArticlePageQuery pageQuery) {
    pageQuery.init();

    Page<ArticleVO> page = buildSortOrder(pageQuery);

    Page<ArticleVO> articlePage = this.baseMapper.page(page, pageQuery);

    List<ArticleVO> records = articlePage.getRecords();
    if (CollUtil.isEmpty(records)) {
      return new Page<>();
    }

    records.forEach(ArticleVO::assembleData);

    return PageUtil.convertPage(articlePage, records);
  }

  @Override
  public ArticleDetailVO detail(Integer id) {
    Article article = this.getById(id);
    Assert.notNull(article, () -> new GlobalException(ARTICLE_NOT_FOUND));

    ArticleCategory articleCategory = articleCategoryService.getById(
        article.getArticleCategoryId());

    List<ProductArticle> productArticles = productArticleService.lambdaQuery()
        .eq(ProductArticle::getArticleId, id).list();

    ArticleDetailVO articleDetailVO = new ArticleDetailVO(article, articleCategory,
        productArticles);

    List<Integer> articleCategoryId = new ArrayList<>();
    if (article.getArticleCategoryId() != null) {
      Integer categoryId = article.getArticleCategoryId();
      while (categoryId != 0) {
        articleCategoryId.add(categoryId);
        ArticleCategory byId = articleCategoryService.getById(categoryId);
        categoryId = byId.getParentId();
      }
    }
    Collections.reverse(articleCategoryId);
    articleDetailVO.setArticleCategoryId(articleCategoryId);

    return articleDetailVO;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void create(ArticleSaveParam param) {
    // 1. 检查文章名称是否重复
    long titleCount = this.lambdaQuery().eq(Article::getArticleTitle, param.getArticleTitle())
        .count();
    Assert.isTrue(titleCount == 0,
        () -> new GlobalException(ARTICLE_TITLE_EXISTS, SERVICE_DATA_ERROR));

    Article article = param.createArticle();
    this.save(article);

    if (CollUtil.isNotEmpty(param.getProductIds())) {
      productArticleService.saveBatchProductArticle(param.getProductIds(), article.getArticleId());
    }
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(ArticleEditParam param) {
    Article article = this.getById(param.getArticleId());
    Assert.notNull(article, () -> new GlobalException(ARTICLE_NOT_FOUND));

    Article updateArticle = param.createArticle();
    this.updateById(updateArticle);

    List<Integer> productIds = Optional.ofNullable(param.getProductIds())
        .orElse(Collections.emptyList());
    productArticleService.updateBatchProductArticle(productIds, article.getArticleId());
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void batch(ArticleBatchParam param) {
    String type = param.getType();

    if ("del".equals(type)) {
      this.removeByIds(param.getIds());
    }

    if ("show".equals(type) || "hide".equals(type)) {
      int isShow = "show".equals(type) ? 1 : 0;
      this.lambdaUpdate().in(Article::getArticleId, param.getIds()).set(Article::getIsShow, isShow)
          .update();
    }

    if ("moveCat".equals(type)) {
      Assert.notEmpty(param.getTargetCat(), () -> new GlobalException(ARTICLE_TARGET_CAT_IS_NULL));

      Integer lastCat = param.getTargetCat()[param.getTargetCat().length - 1];
      this.lambdaUpdate().in(Article::getArticleId, param.getIds())
          .set(Article::getArticleCategoryId, lastCat).update();
    }
  }

  @Override
  public ArticleDetailVO getDetailBySn(String articleSn) {
    LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Article::getArticleSn, articleSn);
    Article article = this.getOne(queryWrapper);
    if (article == null) {
      throw new GlobalException(ARTICLE_NOT_FOUND);
    }
    return this.detail(article.getArticleId());
  }

  @Override
  public ArticleVO getNextArticle(Integer id) {
    // 获取当前文章
    Article currentArticle = this.getById(id);
    if (currentArticle == null) {
      throw new GlobalException(ARTICLE_NOT_FOUND);
    }
    // 查询下一篇文章
    Article nextArticle = this.getArticleInSameCategory(currentArticle.getArticleCategoryId(), id,
        true);
    if (nextArticle == null) {
      return new ArticleVO();
    }
    ArticleVO articleVO = new ArticleVO();
    BeanUtils.copyProperties(nextArticle, articleVO);

    return articleVO;
  }

  @Override
  public ArticleVO getPrevArticle(Integer id) {
    // 获取当前文章
    Article currentArticle = this.getById(id);
    if (currentArticle == null) {
      throw new GlobalException(ARTICLE_NOT_FOUND);
    }
    // 查询上一篇文章
    Article prevArticle = this.getArticleInSameCategory(currentArticle.getArticleCategoryId(), id,
        false);
    if (prevArticle == null) {
      return null;
    }
    ArticleVO articleVO = new ArticleVO();
    BeanUtils.copyProperties(prevArticle, articleVO);

    return articleVO;
  }

  /**
   * 根据分类ID和当前文章ID获取上一篇或下一篇文章
   *
   * @param categoryId 分类ID
   * @param currentId  当前文章ID
   * @param isNext     是否获取下一篇文章
   * @return 文章对象
   */
  private Article getArticleInSameCategory(Integer categoryId, Integer currentId, boolean isNext) {
    LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Article::getArticleCategoryId, categoryId)
        // 只获取显示的文章
        .eq(Article::getIsShow, 1);

    if (isNext) {
      // 获取下一篇
      queryWrapper.gt(Article::getArticleId, currentId).orderByAsc(Article::getArticleId)
          // 只获取一篇
          .last("limit 1");
    } else {
      // 获取上一篇
      queryWrapper.lt(Article::getArticleId, currentId).orderByDesc(Article::getArticleId)
          // 只获取一篇
          .last("limit 1");
    }

    return this.getOne(queryWrapper);
  }

  @Override
  public List<ChildrenArticleWithCategoryVO> getChildrenArticleListWithCategory(
      Integer categorySize, Integer articleSize) {
    //获取sn为帮助中心的分类id
    ArticleCategoryVO category = articleCategoryService.getDetailBySn("bzzx");

    ArticleCategoryPageQuery articleCategoryPageQuery = new ArticleCategoryPageQuery();
    articleCategoryPageQuery.setParentId(category.getArticleCategoryId());
    articleCategoryPageQuery.setSize(categorySize);
    articleCategoryPageQuery.setSortField("sort_order");
    articleCategoryPageQuery.setSortOrder("asc");

    List<ArticleCategoryVO> articleCategoryList = articleCategoryService.list(
        articleCategoryPageQuery).getRecords();

    List<ChildrenArticleWithCategoryVO> childrenList = new ArrayList<>();
    for (ArticleCategoryVO articleCategory : articleCategoryList) {
      ChildrenArticleWithCategoryVO childrenArticleWithCategoryVO = new ChildrenArticleWithCategoryVO();
      childrenArticleWithCategoryVO.setArticleCategoryId(articleCategory.getArticleCategoryId());
      childrenArticleWithCategoryVO.setArticleCategoryName(
          articleCategory.getArticleCategoryName());
      childrenArticleWithCategoryVO.setCategorySn(articleCategory.getCategorySn());
      //获取文章列表
      List<Article> articleList = this.list(
          new LambdaQueryWrapper<Article>().eq(Article::getArticleCategoryId,
                  articleCategory.getArticleCategoryId()).eq(Article::getIsShow, 1)
              .orderByAsc(Article::getArticleId).last("limit " + articleSize));

      List<ArticleVO> articleVOList = articleList.stream().map(article -> {
        ArticleVO articleVO = new ArticleVO();
        BeanUtils.copyProperties(article, articleVO);
        articleVO.setAddTime(article.getAddTime().toString());
        return articleVO;
      }).toList();

      childrenArticleWithCategoryVO.setArticles(articleVOList);
      childrenList.add(childrenArticleWithCategoryVO);
    }
    return childrenList;
  }

}