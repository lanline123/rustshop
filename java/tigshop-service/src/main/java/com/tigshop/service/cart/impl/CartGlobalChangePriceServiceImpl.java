package com.tigshop.service.cart.impl;

import com.tigshop.bean.model.cart.CartGlobalChangePrice;
import com.tigshop.mapper.cart.CartGlobalChangePriceMapper;
import com.tigshop.service.cart.CartGlobalChangePriceService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class CartGlobalChangePriceServiceImpl  extends BaseServiceImpl<CartGlobalChangePriceMapper, CartGlobalChangePrice>
        implements
        CartGlobalChangePriceService
{

}
