package com.tigshop.service.promotion.impl;

import com.tigshop.bean.model.promotion.GiftCardOrder;
import com.tigshop.mapper.promotion.GiftCardOrderMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.promotion.GiftCardOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Tigshop项目组
 */
@RequiredArgsConstructor
@Service
public class GiftCardOrderServiceImpl extends BaseServiceImpl<GiftCardOrderMapper, GiftCardOrder> implements GiftCardOrderService {
}
