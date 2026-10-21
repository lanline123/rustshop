package com.tigshop.service.promotion.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.model.promotion.TimeDiscount;
import com.tigshop.bean.model.promotion.TimeDiscountItem;
import com.tigshop.mapper.promotion.TimeDiscountItemMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.promotion.TimeDiscountItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 限时折扣商品服务实现类
 *
 * @author kidd
 * @create 2025/7/1
 */
@Service
public class TimeDiscountItemServiceImpl extends BaseServiceImpl<TimeDiscountItemMapper, TimeDiscountItem> implements TimeDiscountItemService {
    @Override
    public List<TimeDiscountItem> getTimeDiscountItemByDiscountId(Integer discountId) {
        LambdaQueryWrapper<TimeDiscountItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TimeDiscountItem::getDiscountId, discountId)
                .select(TimeDiscountItem::getDiscountId);
        return this.list(queryWrapper);
    }

    @Transactional
    @Override
    public void saveBatchTimeDiscountItem(List<TimeDiscountItem> timeDiscountItems, TimeDiscount timeDiscount) {
        if (CollUtil.isEmpty(timeDiscountItems)) {
            return;
        }

        timeDiscountItems.forEach(item -> {
            item.setDiscountId(timeDiscount.getDiscountId());
            item.setStartTime(timeDiscount.getStartTime());
            item.setEndTime(timeDiscount.getEndTime());
        });

        this.saveBatch(timeDiscountItems);
    }

    @Transactional
    @Override
    public void updateBatchTimeDiscountItem(List<TimeDiscountItem> timeDiscountItems, TimeDiscount timeDiscount) {
        this.lambdaUpdate().eq(TimeDiscountItem::getDiscountId, timeDiscount.getDiscountId()).remove();
        this.saveBatchTimeDiscountItem(timeDiscountItems, timeDiscount);
    }
}
