package com.tigshop.service.content;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.model.content.WechatLiveRoomGoods;

import java.util.List;

/**
 * 微信直播间商品关联服务
 */
public interface WechatLiveRoomGoodsService extends IService<WechatLiveRoomGoods> {

    List<WechatLiveRoomGoods> getGoodsByRoomId(Integer roomId);

    WechatLiveRoomGoods getGoodsByRoomIdAndGoodsId(Integer roomId, Long goodsId);

    WechatLiveRoomGoods getGoodsByRoomIdAndWechatGoodsId(Integer roomId, Integer wechatGoodsId);
}
