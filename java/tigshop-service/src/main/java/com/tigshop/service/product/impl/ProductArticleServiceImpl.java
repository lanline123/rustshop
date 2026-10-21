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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.model.product.ProductArticle;
import com.tigshop.mapper.product.ProductArticleMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductArticleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Tigshop团队
 * @create 2024年12月03日 15:17
 */
@Service
public class ProductArticleServiceImpl extends
    BaseServiceImpl<ProductArticleMapper, ProductArticle> implements ProductArticleService {

  @Override
  public List<ProductArticle> getProductArticleByProductId(Integer productId) {
    LambdaQueryWrapper<ProductArticle> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(ProductArticle::getGoodsId, productId).select(ProductArticle::getArticleId);
    return this.list(queryWrapper);
  }

  @Transactional
  @Override
  public void saveBatchProductArticle(List<Integer> productIds, Integer articleId) {
    List<ProductArticle> productArticles = productIds.stream()
        .map(productId -> new ProductArticle(productId, articleId)).toList();
    this.saveBatch(productArticles);
  }

  @Transactional
  @Override
  public void updateBatchProductArticle(List<Integer> productIds, Integer articleId) {
    //先移除之前关联的
    this.lambdaUpdate().eq(ProductArticle::getArticleId, articleId).remove();
    if (!productIds.isEmpty()) {
      //在进行添加
      this.saveBatchProductArticle(productIds, articleId);
    }
  }
}
