// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.product;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.model.product.ProductMemberPrice;

/**
 * @author Tigshop团队
 */
public interface ProductMemberPriceService extends IService<ProductMemberPrice> {

    void dealMemberPrice(Integer productId);
}
