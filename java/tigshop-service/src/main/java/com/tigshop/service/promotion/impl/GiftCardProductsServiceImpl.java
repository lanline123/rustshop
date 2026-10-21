package com.tigshop.service.promotion.impl;

import com.tigshop.bean.model.promotion.GiftCardProducts;
import com.tigshop.mapper.promotion.GiftCardProductsMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.promotion.GiftCardProductsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Tigshop项目组
 */
@RequiredArgsConstructor
@Service
public class GiftCardProductsServiceImpl extends BaseServiceImpl<GiftCardProductsMapper, GiftCardProducts> implements GiftCardProductsService {
}
