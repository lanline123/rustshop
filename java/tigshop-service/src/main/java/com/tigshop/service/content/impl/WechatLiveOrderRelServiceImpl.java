package com.tigshop.service.content.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tigshop.bean.model.content.WechatLiveOrderRel;
import com.tigshop.mapper.content.WechatLiveOrderRelMapper;
import com.tigshop.service.content.WechatLiveOrderRelService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 微信直播订单归因关系服务实现
 *
 * @author Tigshop团队
 * @create 2026年03月08日 16:56
 */
@Service
public class WechatLiveOrderRelServiceImpl extends ServiceImpl<WechatLiveOrderRelMapper, WechatLiveOrderRel> implements WechatLiveOrderRelService {

    @Override
    public List<WechatLiveOrderRel> listByOrderIds(List<Integer> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyList();
        }
        return this.list(new LambdaQueryWrapper<WechatLiveOrderRel>()
                .in(WechatLiveOrderRel::getOrderId, orderIds)
                .orderByDesc(WechatLiveOrderRel::getId));
    }

    @Override
    public List<WechatLiveOrderRel> listByOrderId(Integer orderId) {
        if (orderId == null || orderId <= 0) {
            return Collections.emptyList();
        }
        return this.list(new LambdaQueryWrapper<WechatLiveOrderRel>()
                .eq(WechatLiveOrderRel::getOrderId, orderId)
                .orderByDesc(WechatLiveOrderRel::getId));
    }

    @Override
    public List<WechatLiveOrderRel> listByRoomId(Integer roomId) {
        if (roomId == null || roomId <= 0) {
            return Collections.emptyList();
        }
        return this.list(new LambdaQueryWrapper<WechatLiveOrderRel>()
                .eq(WechatLiveOrderRel::getRoomId, roomId)
                .orderByDesc(WechatLiveOrderRel::getId));
    }

    @Override
    public void removeByOrderId(Integer orderId) {
        if (orderId == null || orderId <= 0) {
            return;
        }
        this.remove(new LambdaQueryWrapper<WechatLiveOrderRel>()
                .eq(WechatLiveOrderRel::getOrderId, orderId));
    }
}
