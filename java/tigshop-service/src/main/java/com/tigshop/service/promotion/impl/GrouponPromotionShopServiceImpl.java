// GrouponPromotionShopServiceImpl.java
package com.tigshop.service.promotion.impl;

import com.tigshop.bean.model.promotion.GrouponPromotionShop;
import com.tigshop.mapper.promotion.GrouponPromotionShopMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.promotion.GrouponPromotionShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author tigshop
 * @description 拼团活动适用店铺服务实现类
 */
@RequiredArgsConstructor
@Service
public class GrouponPromotionShopServiceImpl extends BaseServiceImpl<GrouponPromotionShopMapper, GrouponPromotionShop> implements GrouponPromotionShopService {

    private final GrouponPromotionShopMapper grouponPromotionShopMapper;
}
