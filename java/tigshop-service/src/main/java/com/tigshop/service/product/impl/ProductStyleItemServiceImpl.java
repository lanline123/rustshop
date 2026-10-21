package com.tigshop.service.product.impl;

import cn.hutool.core.collection.CollUtil;
import com.tigshop.bean.model.product.ProductStyleItem;
import com.tigshop.mapper.product.ProductStyleItemMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductStyleItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductStyleItemServiceImpl extends BaseServiceImpl<ProductStyleItemMapper, ProductStyleItem> implements ProductStyleItemService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAdd(Integer styleId, Integer itemType, List<Integer> refIds) {
        if (CollUtil.isEmpty(refIds)) {
            return;
        }
        List<Integer> existingRefIds = getRefIdsByStyleIdAndType(styleId, itemType);
        long now = System.currentTimeMillis() / 1000;

        List<ProductStyleItem> items = refIds.stream()
                .filter(refId -> !existingRefIds.contains(refId))
                .map(refId -> ProductStyleItem.builder()
                        .styleId(styleId)
                        .itemType(itemType)
                        .refId(refId)
                        .addTime(now)
                        .build())
                .collect(Collectors.toList());

        if (CollUtil.isNotEmpty(items)) {
            this.saveBatch(items);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchRemove(Integer styleId, Integer itemType, List<Integer> refIds) {
        if (CollUtil.isEmpty(refIds)) {
            return;
        }
        this.lambdaUpdate()
                .eq(ProductStyleItem::getStyleId, styleId)
                .eq(ProductStyleItem::getItemType, itemType)
                .in(ProductStyleItem::getRefId, refIds)
                .remove();
    }

    @Override
    public void removeByStyleId(Integer styleId) {
        this.lambdaUpdate()
                .eq(ProductStyleItem::getStyleId, styleId)
                .remove();
    }

    @Override
    public List<Integer> getRefIdsByStyleIdAndType(Integer styleId, Integer itemType) {
        return this.lambdaQuery()
                .eq(ProductStyleItem::getStyleId, styleId)
                .eq(ProductStyleItem::getItemType, itemType)
                .list()
                .stream()
                .map(ProductStyleItem::getRefId)
                .collect(Collectors.toList());
    }

    @Override
    public long countByStyleId(Integer styleId) {
        return this.lambdaQuery()
                .eq(ProductStyleItem::getStyleId, styleId)
                .count();
    }
}
