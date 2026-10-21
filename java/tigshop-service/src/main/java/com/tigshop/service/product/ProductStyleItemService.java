package com.tigshop.service.product;

import com.tigshop.bean.model.product.ProductStyleItem;
import com.tigshop.service.common.BaseService;

import java.util.List;

public interface ProductStyleItemService extends BaseService<ProductStyleItem> {

    /**
     * 批量添加关联（按类型）
     */
    void batchAdd(Integer styleId, Integer itemType, List<Integer> refIds);

    /**
     * 批量移除关联（按类型）
     */
    void batchRemove(Integer styleId, Integer itemType, List<Integer> refIds);

    /**
     * 根据样式ID删除所有关联
     */
    void removeByStyleId(Integer styleId);

    /**
     * 根据样式ID和类型查询关联的目标ID列表
     */
    List<Integer> getRefIdsByStyleIdAndType(Integer styleId, Integer itemType);

    /**
     * 根据样式ID统计关联数量
     */
    long countByStyleId(Integer styleId);
}
