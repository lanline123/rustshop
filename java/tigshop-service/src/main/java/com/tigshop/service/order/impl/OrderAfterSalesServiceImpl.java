// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.order.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.dto.order.OrderConfigDTO;
import com.tigshop.bean.enums.order.AftersalesStatusEnum;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.AftersalesItem;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.shop.OrderConfig;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.mapper.o2o.OrderPickupShipmentMapper;
import com.tigshop.mapper.order.AftersalesItemMapper;
import com.tigshop.mapper.order.AftersalesMapper;
import com.tigshop.mapper.order.OrderItemMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.shop.OrderConfigMapper;
import com.tigshop.service.order.OrderAftersalesService;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单管理服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class OrderAfterSalesServiceImpl implements OrderAftersalesService {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderConfigMapper orderConfigMapper;
    @Resource
    private ConfigService configService;
    @Resource
    private AftersalesMapper aftersalesMapper;
    @Resource
    private AftersalesItemMapper aftersalesItemMapper;
    @Resource
    private OrderItemMapper orderItemMapper;
    @Resource
    private TigshopProperties tigshopProperties;
    @Resource
    private OrderPickupShipmentMapper orderPickupShipmentMapper;


    @Override
    public Boolean getOrderAfterSalesToAction(Integer orderId) {
        boolean flag = true;
        Order order = orderMapper.selectById(orderId);

        // 多门店，且已经核销，则不允许售后

        // 查询店铺结算配置
        OrderConfig orderConfig = orderConfigMapper.selectOne(
                new LambdaQueryWrapper<OrderConfig>()
                        .eq(OrderConfig::getShopId, order.getShopId())
        );
        // 1. 判断店铺结算配置（确认收货后立即不可售后）（自营店不用判断）
        if (order.getShopId() != null && order.getShopId() > 0) {
            if (orderConfig != null) {
                OrderConfigDTO orderConfigData = JsonUtil.fromJson(orderConfig.getData(), OrderConfigDTO.class);
                if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus() && 0 == orderConfigData.getDateType()) {
                    flag = false;
                }
            }
        }
        // 2. 全局设置（买家申请售后限制）
        String afterSalesLimitDays = configService.getConfigVal(SettingsEnum.AFTER_SALES_LIMIT_DAYS);
        if (OrderStatusEnum.COMPLETED.getCode() == order.getOrderStatus()) {
            if (StrUtil.isNotBlank(afterSalesLimitDays)) {
                long receivedTime = order.getReceivedTime() * 1000;
                BigDecimal afterSalesLimitDaysTime = new BigDecimal(afterSalesLimitDays).multiply(new BigDecimal(24 * 60 * 60 * 1000));
                LocalDateTime limitDateTime = Instant.ofEpochMilli(receivedTime + afterSalesLimitDaysTime.longValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                flag = limitDateTime.isAfter(LocalDateTime.now());
            }
        }
        if (flag) {
            // 判断订单中的商品是否全部进行了售后操作
            List<Aftersales> aftersales = aftersalesMapper.selectList(new LambdaQueryWrapper<Aftersales>()
                    .eq(Aftersales::getOrderId, orderId)
                    .in(Aftersales::getStatus, AftersalesStatusEnum.validStatus()));
            if (ObjectUtil.isNotEmpty(aftersales)) {
                List<Integer> list = aftersales.stream().map(Aftersales::getAftersaleId).toList();
                List<AftersalesItem> aftersalesItems = aftersalesItemMapper.selectList(new LambdaQueryWrapper<AftersalesItem>().in(AftersalesItem::getAftersaleId, list));
                List<OrderItem> orderItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
                // aftersalesItems group by orderItemId
                Map<Integer, List<AftersalesItem>> aftersalesItemMap = aftersalesItems.stream()
                        .collect(Collectors.groupingBy(AftersalesItem::getOrderItemId));

                // 5. 遍历订单项，判断是否全部售后完毕
                boolean allAftersaled = true;
                for (OrderItem orderItem : orderItems) {
                    Integer orderItemId = orderItem.getItemId();
                    int orderQuantity = orderItem.getQuantity();

                    List<AftersalesItem> relatedAftersales = aftersalesItemMap.getOrDefault(orderItemId, Collections.emptyList());

                    // 累加售后数量
                    int aftersaleQuantity = relatedAftersales.stream()
                            .mapToInt(AftersalesItem::getNumber)
                            .sum();

                    if (aftersaleQuantity < orderQuantity) {
                        allAftersaled = false;
                        break; // 有一个未售后完就结束
                    }
                }

                if (allAftersaled) {
                    return false;
                }
            }
        }
        return flag;
    }
}
