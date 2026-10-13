package com.tigshop.service.content;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.model.content.WechatLiveOrderRel;

import java.util.List;

/**
 * 微信直播订单归因关系服务
 *
 * @author Tigshop团队
 * @create 2026年03月08日 16:56
 */
public interface WechatLiveOrderRelService extends IService<WechatLiveOrderRel> {

    List<WechatLiveOrderRel> listByOrderIds(List<Integer> orderIds);

    List<WechatLiveOrderRel> listByOrderId(Integer orderId);

    List<WechatLiveOrderRel> listByRoomId(Integer roomId);

    void removeByOrderId(Integer orderId);
}
