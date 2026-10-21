// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.service.kuaidi;


import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.feign.shipping.ShippingResult;

import java.util.Map;

/**
 * @author Tigshop团队
 * @create 2025/8/5 14:12
 */
public interface KuaiDiService {


    Map<String, Object> getElectronicWaybill(OrderVO order, String remark);

    /**
     * 查询物流轨迹
     *
     * @param logisticsName 物流公司名称或编码
     * @param trackingNo 物流单号
     * @param mobile 收货手机号
     * @return 物流轨迹结果
     */
    ShippingResult getShippingInfo(String logisticsName, String trackingNo, String mobile);
}
