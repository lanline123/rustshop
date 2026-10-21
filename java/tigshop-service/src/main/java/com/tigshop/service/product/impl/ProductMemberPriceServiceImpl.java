// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.product.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tigshop.bean.model.product.ProductMemberPrice;
import com.tigshop.mapper.product.ProductMemberPriceMapper;
import com.tigshop.service.product.ProductMemberPriceService;
import org.springframework.stereotype.Service;

/**
 * @author Tigshop团队
 */
@Service
public class ProductMemberPriceServiceImpl extends ServiceImpl<ProductMemberPriceMapper, ProductMemberPrice>
        implements ProductMemberPriceService {

    @Override
    public void dealMemberPrice(Integer productId) {

    }
}

