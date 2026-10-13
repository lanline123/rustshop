package com.tigshop.service.content.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tigshop.bean.model.content.WechatLiveRoomGoods;
import com.tigshop.mapper.content.WechatLiveRoomGoodsMapper;
import com.tigshop.service.content.WechatLiveRoomGoodsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 微信直播间商品关联服务实现类
 */
@Service
public class WechatLiveRoomGoodsServiceImpl extends ServiceImpl<WechatLiveRoomGoodsMapper, WechatLiveRoomGoods> implements WechatLiveRoomGoodsService {

    @Override
    public List<WechatLiveRoomGoods> getGoodsByRoomId(Integer roomId) {
        return this.list(new LambdaQueryWrapper<WechatLiveRoomGoods>()
                .eq(WechatLiveRoomGoods::getRoomId, roomId)
                .orderByAsc(WechatLiveRoomGoods::getSort)
                .orderByAsc(WechatLiveRoomGoods::getId));
    }

    @Override
    public WechatLiveRoomGoods getGoodsByRoomIdAndGoodsId(Integer roomId, Long goodsId) {
        return this.lambdaQuery()
                .eq(WechatLiveRoomGoods::getRoomId, roomId)
                .eq(WechatLiveRoomGoods::getWechatLiveGoodsId, goodsId)
                .last("limit 1")
                .one();
    }

    @Override
    public WechatLiveRoomGoods getGoodsByRoomIdAndWechatGoodsId(Integer roomId, Integer wechatGoodsId) {
        return this.lambdaQuery()
                .eq(WechatLiveRoomGoods::getRoomId, roomId)
                .eq(WechatLiveRoomGoods::getWechatGoodsId, wechatGoodsId)
                .last("limit 1")
                .one();
    }
}
