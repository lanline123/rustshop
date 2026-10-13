package com.tigshop.service.decorate;

import com.tigshop.bean.bo.decorate.discrete.OpenAdvertisingBO;
import com.tigshop.bean.dto.decorate.DecorateDiscreteMenuItemDTO;
import com.tigshop.bean.param.decorate.DecorateDiscreteUpdateParam;
import com.tigshop.bean.model.decorate.DecorateDiscrete;
import com.tigshop.bean.vo.decorate.DecorateDiscreteVO;
import com.tigshop.bean.vo.decorate.DecorateMemberDataVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 装修组件服务
 *
 * @author Kidd
 * @since 2025/6/30
 */
public interface DecorateDiscreteService extends BaseService<DecorateDiscrete> {

    /**
     * 详情
     */
    DecorateDiscreteVO detail(String decorateSn);

    /**
     * 更新
     */
    void update(DecorateDiscreteUpdateParam param);

    /**
     * 个人中心基础数据
     */
    DecorateMemberDataVO<List<DecorateDiscreteMenuItemDTO>> getMemberDecorateData();

    /**
     * 店铺头部装修
     */
    DecorateDiscreteVO getShopHead(Integer shopId);

    /**
     * 客户端：获取开屏广告
     */
    OpenAdvertisingBO getOpenAdvertising();

}
