package com.tigshop.api.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.OrderIdDTO;
import com.tigshop.bean.feign.shipping.ShippingResult;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.query.order.OrderListPageQuery;
import com.tigshop.bean.vo.order.OrderNumVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.enums.DataType;
import com.tigshop.service.cart.CartService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.setting.AfterSalesReasonConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 用户订单
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RestController
@RequestMapping(("/api/user/order"))
@Tag(name = "前台用户订单中心")
@Validated
public class UserOrderController {
    @Resource
    private OrderService orderService;
    @Resource
    private CartService cartService;
    @Resource
    private AfterSalesReasonConfigService afterSalesReasonConfigService;
    @Resource
    private TranslatePackage translatePackage;

    @GetMapping("/list")
    @Operation(summary = "订单列表")
    public Page<OrderVO> list(OrderListPageQuery listDTO) {
        Integer userId = getCurrentUserId();
        listDTO.setUserId(userId);
        return orderService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "订单详情")
    public OrderVO detail(@RequestParam(value = "id") Integer id) {
        Integer userId = getCurrentUserId();
        return orderService.detail(id, userId);
    }

    @GetMapping("/orderNum")
    @Operation(summary = "订单数量")
    public OrderNumVO orderNum() {
        return orderService.orderNum();
    }

    @PostMapping("/cancelOrder")
    @Operation(summary = "取消订单")
    public void cancelOrder(@RequestBody OrderIdDTO orderIddto) {
        Integer userId = getCurrentUserId();
        orderService.cancelOrder(orderIddto.getId(),userId ,orderIddto.getDescription());
    }

    @GetMapping("/reasonConfig")
    @Operation(summary = "获取买家售后与取消订单共用原因配置")
    public Map<String, Object> reasonConfig() {
        AfterSalesReasonConfigService.ReasonConfig config = afterSalesReasonConfigService.getConfig();
        return Map.of(
                "afterSalesReasonEnabled", config.enabled(),
                // Map 中的 List<String> 不会经过 @JsonTranslate，需逐项翻译接口文案。
                "afterSalesReasonList", config.reasons().stream()
                        .map(reason -> translatePackage.translate(reason, DataType.API.getCode()))
                        .toList());
    }

    @PostMapping("/delOrder")
    @Operation(summary = "删除订单")
    public void delOrder(@RequestBody OrderIdDTO orderIddto) {
        Integer userId = getCurrentUserId();
        orderService.delOrder(orderIddto.getId(), userId);
    }

    @PostMapping("/confirmReceipt")
    @Operation(summary = "确认收货")
    public void confirmReceipt(@RequestBody OrderIdDTO orderIddto) {
        Integer userId = getCurrentUserId();
        long count = orderService.count(new LambdaQueryWrapper<Order>().eq(Order::getOrderId, orderIddto.getId())
                .eq(Order::getUserId, userId));
        if(count<=0){
            return;
        }
        orderService.confirmReceipt(orderIddto.getId(), userId);
    }

    @GetMapping("/shippingInfo")
    @Operation(summary = "物流信息")
    public ShippingResult shippingInfo(@RequestParam Integer id) {
        Integer userId = getCurrentUserId();
        return orderService.getShippingInfo(id, userId);
    }

    @PostMapping("/buyAgain")
    @Operation(summary = "再次购买")
    public void buyAgain(@RequestBody OrderIdDTO orderIddto) {
        Integer userId = getCurrentUserId();
        cartService.buyAgain(orderIddto.getId(), userId);
    }
}
