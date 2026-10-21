package com.tigshop.bean.dto.order.deliver;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.tigshop.bean.model.order.AftersalesItem;
import com.tigshop.bean.vo.order.OrderItemVO;
import com.tigshop.common.exception.GlobalException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zx
 * @create 2024年12月16日 13:47
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliverOrderParam {

    @NotNull(message = "订单 ID不能为空")
    @Schema(description = "订单id")
    private Integer id;

    @Schema(description = "数据")
    private List<DeliverInfo> deliverData;

    @Schema(description = "物流ID")
    private Integer logisticsId;

    @Schema(description = "发货方法")
    private Integer shippingMethod;

    @Schema(description = "快递单号")
    private String trackingNo;

    @Schema(description = "电子面单备注")
    private String billRemark;

    @Schema(description = "是否自动核销")
    private Boolean isAutoWriteOff = true;

    private Boolean userIdIsNull = false;

    //来源，用于自定义判断
    @Schema(description = "1 配送订单抢单")
    private Integer from;

    private Map<Integer, Integer> getDeliverItemQuantityMap() {
        return this.deliverData.stream().collect(Collectors.toMap(DeliverInfo::getItemId, DeliverInfo::getToDeliveryQuantity));
    }

    /**
     * 同一订单项可能存在多条售后明细（多次申请部分数量），按订单项累加占用数量。
     */
    private static Map<Integer, Integer> getAftersalesQuantityMap(List<AftersalesItem> aftersalesItems) {
        return aftersalesItems.stream()
                .collect(Collectors.groupingBy(AftersalesItem::getOrderItemId, Collectors.summingInt(AftersalesItem::getNumber)));
    }

    public List<OrderItemVO> getRemainItems(List<OrderItemVO> orderItems, List<AftersalesItem> aftersalesItems) {
        Map<Integer, Integer> deliverItemMap = this.getDeliverItemQuantityMap();

        List<OrderItemVO> remainItems = new ArrayList<>();

        for (OrderItemVO item : orderItems) {
            Integer deliverItemQuantity = deliverItemMap.get(item.getItemId());

            // 订单项无发货数量
            if (deliverItemQuantity == null) {
                Map<Integer, Integer> aftersalesItemMap = getAftersalesQuantityMap(aftersalesItems);
                Integer aftersalseItemNum = aftersalesItemMap.get(item.getItemId());

                if (aftersalseItemNum == null) {
                    remainItems.add(item);
                } else {
                    int remainItemQuantity = item.getQuantity() - aftersalseItemNum;
                    if (remainItemQuantity > 0) {
                        OrderItemVO remainItem = BeanUtil.copyProperties(item, OrderItemVO.class);
                        remainItem.setQuantity(remainItemQuantity);
                        remainItems.add(remainItem);
                    }
                }

                continue;
            }

            Assert.isTrue(deliverItemQuantity > 0 && deliverItemQuantity <= item.getQuantity(), () -> new GlobalException("发货数量错误"));

            // 未全部发货
            if (!item.getQuantity().equals(deliverItemQuantity)) {
                Map<Integer, Integer> aftersalesItemMap = getAftersalesQuantityMap(aftersalesItems);
                Integer aftersalseItemNum = 0;
                if (aftersalesItemMap.get(item.getItemId()) != null) {
                    aftersalseItemNum = aftersalesItemMap.get(item.getItemId());
                }
                int remainItemQuantity = item.getQuantity() - deliverItemQuantity - aftersalseItemNum;
                if (remainItemQuantity > 0) {
                    OrderItemVO remainItem = BeanUtil.copyProperties(item, OrderItemVO.class);
                    remainItem.setQuantity(remainItemQuantity);
                    remainItems.add(remainItem);
                }
            }
        }

        return remainItems;
    }

    public List<OrderItemVO> getDeliverItems(List<OrderItemVO> orderItems, List<AftersalesItem> aftersalesItems) {
        Map<Integer, Integer> deliverItemMap = this.getDeliverItemQuantityMap();

        List<OrderItemVO> deliverItems = new ArrayList<>();

        for (OrderItemVO item : orderItems) {
            Integer deliverItemQuantity = deliverItemMap.get(item.getItemId());

            if (deliverItemQuantity == null) {
                Map<Integer, Integer> aftersalesItemMap = getAftersalesQuantityMap(aftersalesItems);
                Integer aftersalseItemNum = aftersalesItemMap.get(item.getItemId());

                if (aftersalseItemNum == null || aftersalseItemNum == 0) {
                    continue;
                } else {
                    OrderItemVO deliverItem = BeanUtil.copyProperties(item, OrderItemVO.class);
                    deliverItem.setQuantity(aftersalseItemNum);
                    deliverItems.add(deliverItem);
                }

                continue;
            }

            Assert.isTrue(deliverItemQuantity > 0 && deliverItemQuantity <= item.getQuantity(), () -> new GlobalException("发货数量错误"));

            // 全部发货
            if (item.getQuantity().equals(deliverItemQuantity)) {
                deliverItems.add(item);
            } else {
                Map<Integer, Integer> aftersalesItemMap = getAftersalesQuantityMap(aftersalesItems);
                int aftersalseItemNum = aftersalesItemMap.getOrDefault(item.getItemId(), 0);
                // 售后占用的数量会并入发货单，发货数量必须为扣除售后后的可发数量
                int totalQuantity = deliverItemQuantity + aftersalseItemNum;
                Assert.isTrue(totalQuantity <= item.getQuantity(), () -> new GlobalException("发货数量超出可发数量，该商品有部分数量正在售后中"));
                OrderItemVO deliverItem = BeanUtil.copyProperties(item, OrderItemVO.class);
                deliverItem.setQuantity(totalQuantity);
                deliverItems.add(deliverItem);
            }
        }

        return deliverItems;
    }

}
