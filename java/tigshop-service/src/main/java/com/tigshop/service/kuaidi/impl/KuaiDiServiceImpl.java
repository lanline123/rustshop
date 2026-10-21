// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.service.kuaidi.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.feign.shipping.ShippingResult;
import com.tigshop.bean.model.setting.LogisticsCompany;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.mapper.setting.LogisticsCompanyMapper;
import com.tigshop.service.kuaidi.KuaiDiService;
import com.tigshop.service.logistics.LogisticsProviderRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 物流服务门面：按后台配置的 logisticsProvider 路由到快递鸟 / 快递100。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KuaiDiServiceImpl implements KuaiDiService {

    private final LogisticsCompanyMapper logisticsCompanyMapper;
    private final LogisticsProviderRouter logisticsProviderRouter;

    @Override
    public Map<String, Object> getElectronicWaybill(OrderVO order, String remark) {
        return logisticsProviderRouter.getActive().createElectronicWaybill(order, remark);
    }

    @Override
    public ShippingResult getShippingInfo(String logisticsName, String trackingNo, String mobile) {
        log.info("物流轨迹查询：物流名称：{}，物流编号：{}，手机号：{}", logisticsName, trackingNo, mobile);
        if (StrUtil.isBlank(logisticsName) || StrUtil.isBlank(trackingNo)) {
            return null;
        }

        LogisticsCompany logisticsCompany = logisticsCompanyMapper.selectOne(new LambdaQueryWrapper<LogisticsCompany>()
                .and(wrapper -> wrapper.eq(LogisticsCompany::getLogisticsName, logisticsName.trim())
                        .or()
                        .eq(LogisticsCompany::getLogisticsCode, logisticsName.trim())
                        .or()
                        .eq(LogisticsCompany::getApiLogisticsCode, logisticsName.trim())
                        .or()
                        .eq(LogisticsCompany::getKuaidi100Code, logisticsName.trim()))
                .last("limit 1"));
        if (logisticsCompany == null) {
            return null;
        }
        return logisticsProviderRouter.getActive().queryTrack(logisticsCompany, trackingNo, mobile);
    }
}
