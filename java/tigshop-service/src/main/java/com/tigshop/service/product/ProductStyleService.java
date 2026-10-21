package com.tigshop.service.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.product.ProductStyle;
import com.tigshop.bean.param.product.ProductStyleEditParam;
import com.tigshop.bean.param.product.ProductStyleItemBatchParam;
import com.tigshop.bean.param.product.ProductStyleSaveParam;
import com.tigshop.bean.query.product.ProductStylePageQuery;
import com.tigshop.bean.vo.product.BrandProductCountVO;
import com.tigshop.bean.vo.product.ProductActiveStyleVO;
import com.tigshop.bean.vo.product.ProductStyleDetailVO;
import com.tigshop.bean.vo.product.ProductStyleVO;
import com.tigshop.service.common.BaseService;

import java.util.List;
import java.util.Map;

public interface ProductStyleService extends BaseService<ProductStyle> {

    /**
     * 商品样式分页列表
     */
    Page<ProductStyleVO> list(ProductStylePageQuery pageQuery);

    /**
     * 商品样式详情
     */
    ProductStyleDetailVO detail(Integer styleId);

    /**
     * 创建商品样式
     */
    void create(ProductStyleSaveParam param);

    /**
     * 更新商品样式
     */
    void update(ProductStyleEditParam param);

    /**
     * 批量添加关联商品
     */
    void addProducts(ProductStyleItemBatchParam param);

    /**
     * 批量移除关联商品
     */
    void removeProducts(ProductStyleItemBatchParam param);

    /**
     * 获取单个商品的生效样式
     */
    ProductActiveStyleVO getActiveStyle(Integer productId);

    /**
     * 批量获取商品的生效样式
     */
    Map<Integer, ProductActiveStyleVO> batchGetActiveStyles(List<Integer> productIds);

    /**
     * 批量获取商品的生效样式（通过商品编号SN）
     */
    Map<String, ProductActiveStyleVO> batchGetActiveStylesBySns(List<String> productSns);

    /**
     * 查询品牌列表及每个品牌下的商品数量
     */
    List<BrandProductCountVO> brandListWithProductCount(String keyword);
}
