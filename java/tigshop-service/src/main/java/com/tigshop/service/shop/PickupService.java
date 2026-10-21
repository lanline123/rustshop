// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.service.shop;


import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.vo.o2o.PickupListVO;
import com.tigshop.service.common.BaseService;

/**
 * @author Tigshop团队
 * @create 2025/9/8 17:20
 */

public interface PickupService extends BaseService<Shop> {

    /**
     * 获取自提点详情
     * @param shopId 店铺id
     * @return PickupListVO
     */
    PickupListVO pickupDetail(Integer shopId);
}
