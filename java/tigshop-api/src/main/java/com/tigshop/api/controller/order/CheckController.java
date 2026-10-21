package com.tigshop.api.controller.order;

import com.tigshop.bean.enums.order.SelectFromEnum;
import com.tigshop.bean.model.finance.OrderInvoice;
import com.tigshop.bean.param.order.OrderCheckParam;
import com.tigshop.bean.query.order.StoreShippingTypeQuery;
import com.tigshop.bean.vo.order.OrderCheckVO;
import com.tigshop.bean.vo.order.OrderSubmitVO;
import com.tigshop.service.order.OrderCheckService;
import com.tigshop.service.setting.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.tigshop.bean.enums.setting.SettingsEnum.USE_COUPON;

/**
 * 订单管理控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/api/order/check")
@Validated
@Tag(name = "订单结算", description = "订单结算")
public class CheckController {

    @Resource
    OrderCheckService orderCheckService;
    @Resource
    ConfigService configService;

    @PostMapping("/index")
    @Operation(summary = "获取列表")
    public OrderCheckVO index(@RequestBody OrderCheckParam param) {
        param.indexInitParam();
        // 获取是否默认优惠券
        String useCoupon = configService.getConfigVal(USE_COUPON);
        param.setUseDefaultCouponIds(Integer.parseInt(useCoupon));
        //禁用改价
        disableChangePrice(param);
        return orderCheckService.getOrderCheckData(param);
    }


    @GetMapping("/getAvailablePaymentType")
    @Operation(summary = "获取可用支付方式")
    public List<OrderCheckVO.PaymentType> getAvailablePaymentType(@RequestParam(value = "flowType", required = false) Integer flowType) {
        return orderCheckService.getAvailablePaymentType(flowType);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    public OrderCheckVO update(@RequestBody OrderCheckParam param) {
        param.setSelectFrom(SelectFromEnum.CHECK_INDEX.getValue());
        disableChangePrice(param);
        return orderCheckService.getOrderCheckData(param);
    }

    @PostMapping("/updateCoupon")
    @Operation(summary = "更新优惠券")
    public OrderCheckVO updateCoupon(@RequestBody OrderCheckParam param) {
        disableChangePrice(param);
        param.setSelectFrom(SelectFromEnum.CHECK_UPDATE.getValue());
        return orderCheckService.getOrderCheckData(param);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交订单")
    public OrderSubmitVO submit(@RequestBody OrderCheckParam param) {
        param.setUserId(null);
        //禁用改价
        disableChangePrice(param);
        return orderCheckService.submit(param);
    }

    @GetMapping("/getInvoice")
    @Operation(summary = "获得上次订单发票信息")
    public OrderInvoice getInvoice(@RequestParam(value = "invoiceType", required = false) Integer invoiceType, @RequestParam(value = "titleType", required = false) Integer titleType) {
        return orderCheckService.getLastInvoice(invoiceType, titleType);
    }

    @GetMapping("/getStoreShippingType")
    @Operation(summary = "获得店铺配送方式")
    public Map<Integer, List<OrderCheckVO.ShippingType>> getStoreShippingType(StoreShippingTypeQuery query) {
        query.setUserId(null);
        return orderCheckService.getStoreShippingType(query);
    }


    /**
     * 禁用改价
     *
     * @param param
     */
    private void disableChangePrice(OrderCheckParam param) {
        param.setUseChangePrice(false);
    }

}
