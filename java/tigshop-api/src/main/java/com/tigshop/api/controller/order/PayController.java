package com.tigshop.api.controller.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.enums.order.OrderType;
import com.tigshop.bean.enums.order.PayMethodType;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderPresale;
import com.tigshop.bean.model.order.OrderReservation;
import com.tigshop.bean.vo.order.OrderReservationVO;
import com.tigshop.bean.vo.order.PaylogVO;
import com.tigshop.bean.vo.order.pay.PayCreateVO;
import com.tigshop.bean.vo.order.pay.PayIndexVO;
import com.tigshop.service.finance.PaylogService;
import com.tigshop.service.order.OrderPayService;
import com.tigshop.service.order.OrderPresaleService;
import com.tigshop.service.order.OrderReservationService;
import com.tigshop.service.order.OrderService;
import com.tigshop.bean.model.finance.UserRechargeOrder;
import com.tigshop.service.finance.UserRechargeOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 支付管理控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/api/order/pay")
@Validated
@Tag(name = "订单支付", description = "订单支付")
public class PayController {

    @Resource
    private OrderService orderService;

    @Resource
    OrderPayService orderPayService;

    @Resource
    PaylogService paylogService;

    @Resource
    private OrderReservationService orderReservationService;
    @Autowired
    private OrderPresaleService orderPresaleService;

    @Resource
    private UserRechargeOrderService userRechargeOrderService;

    private Integer requireUserId() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null || userId == 0) {
            throw new GlobalException("用户未登录");
        }
        return userId;
    }

    private void verifyOrderOwnership(Integer orderId, Integer userId) {
        if (orderId == null) {
            return;
        }
        Order order = orderService.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new GlobalException("订单不存在");
        }
    }

    @GetMapping("/index")
    @Operation(summary = "订单支付")
    public PayIndexVO index(@RequestParam(value = "id", required = false) Integer id) {
        Integer userId = requireUserId();
        verifyOrderOwnership(id, userId);
        return orderPayService.getPayOrderInfo(id);
    }

    @GetMapping("/paymentList")
    @Operation(summary = "支付方式列表（下单前收银台，无需订单）")
    public PayIndexVO paymentList() {
        requireUserId();
        return orderPayService.getPaymentSelection();
    }

    @GetMapping("/getPayLog")
    @Operation(summary = "检测订单支付状态")
    public PaylogVO getPayLog(@RequestParam(value = "id", required = false) Integer orderId) {
        Integer userId = requireUserId();
        if (orderId == null || orderId == 0) {
            throw new GlobalException("参数缺失");
        }
        Order order = orderService.getById(orderId);
        // 兼容拆单后父订单被物理删除的场景：develop/旧逻辑在支付后按店铺/供应商拆单会删除父订单，
        // 此时按父订单 id 查不到订单。回退用 parentOrderId 关联的子订单兜底；
        // D1068「提交时拆单」父订单保留不删除，仍走正常分支，二者均不报错。
        List<Order> splitChildren = null;
        if (order == null) {
            splitChildren = orderService.lambdaQuery()
                    .eq(Order::getParentOrderId, orderId)
                    .eq(Order::getUserId, userId)
                    .list();
            if (splitChildren.isEmpty()) {
                throw new GlobalException("订单不存在");
            }
            order = splitChildren.get(0);
        } else if (!order.getUserId().equals(userId)) {
            throw new GlobalException("订单不存在");
        }
        // 拆单后父订单的 paylog 仍保留（未随父单删除），故仍按原始 orderId 查询支付记录；
        // 实付金额优先取所有子订单合计，反映整单实际支付额。
        BigDecimal effectivePaidAmount = splitChildren == null
                ? order.getPaidAmount()
                : splitChildren.stream()
                        .map(o -> Optional.ofNullable(o.getPaidAmount()).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        Paylog one = paylogService.lambdaQuery()
                .eq(Paylog::getOrderId, orderId)
                // 商品订单：orderType=0，避免撞充值单号
                .eq(Paylog::getOrderType, 0)
                .eq(Paylog::getPayStatus, 1)
                .last("limit 1")
                .one();
        PaylogVO target = new PaylogVO();
        if(!BigDecimal.ONE.equals(order.getBalance())){
            if(order.getIsPresale() != null && order.getIsPresale() == 1
                    && Objects.equals(2,order.getPresaleType())){
                //定金预售(进入流程2了,支付的是尾款,采用paylog的形式)
                OrderPresale byOrderId = orderPresaleService.getByOrderId(order.getOrderId());
                if(Objects.nonNull(byOrderId)){
                    Integer presaleStatus = byOrderId.getPresaleStatus();
                    if(Objects.equals(1,presaleStatus)){
                        //待支付尾款,代表定金已经支付了
                        target.setPayAmount(effectivePaidAmount);
                    }else if (Objects.equals(2,presaleStatus)){
                        //代表支付成功了(尾款支付成功了)
                        BeanUtils.copyProperties(one, target);
                    }
                    //非预期状态进入
                }
                //非预期状态进入
            }else{
                //包含了余额支付
                target.setPayAmount(effectivePaidAmount);
            }
        }else{
            //全部都是在线支付或者线下支付
            BeanUtils.copyProperties(one, target);
        }
        if (target.getPayName() == null || target.getPayName().isBlank()) {
            target.setPayName(PayMethodType.getTypeName(target.getPayCode()));
        }
//        if (one != null) {
//            BeanUtils.copyProperties(one, target);
//        }else{
//            //余额支付不产生paylog,现在要特殊处理(直接使用已支付金额)
//            target.setPayAmount(order.getPaidAmount());
//        }
        // 设置预约相关信息，如果有的话
        target.setOrderType(order.getOrderType());
        if (OrderType.isReservationOrder(order.getOrderType())) {
            OrderReservation orderReservation = orderReservationService.getOne(new LambdaQueryWrapper<OrderReservation>().eq(OrderReservation::getOrderId, order.getOrderId()).last("LIMIT 1"));
            if (orderReservation != null) {
                target.setOrderReservation(OrderReservationVO.toOrderReservationVO(orderReservation));
            }
        }
        return target;
    }

    @GetMapping("/checkStatus")
    @Operation(summary = "检测订单支付状态")
    public Integer checkStatus(@RequestParam(value = "id") Integer id,
                               @RequestParam(value = "paylogId", required = false) Integer payLogId) {
        Integer userId = requireUserId();
        verifyOrderOwnership(id, userId);
        return orderPayService.getCheckStatus(id, payLogId);
    }

    @GetMapping("/create")
    @Operation(summary = "支付订单创建（尾款支付/普通订单支付）")
    public PayCreateVO create(@RequestParam(value = "id") Integer id,
                              @RequestParam(value = "type", required = false) String payType,
                              @RequestParam(value = "code", required = false) String code) {
        Integer userId = requireUserId();
        verifyOrderOwnership(id, userId);
        return orderPayService.create(id, payType, code);
    }

    @GetMapping("/createDepositPayment")
    @Operation(summary = "预售定金支付创建")
    public PayCreateVO createDepositPayment(@RequestParam(value = "id") Integer orderId,
                                            @RequestParam(value = "type", required = false) String payType,
                                            @RequestParam(value = "code", required = false) String code) {
        Integer userId = requireUserId();
        verifyOrderOwnership(orderId, userId);
        return orderPayService.createDepositPayment(orderId, payType, code);
    }

    // Apple Pay 原生流程端点（validateMerchant / confirm / confirmByPaymentMethod / notify/ums）
    // 及其支付单归属校验已拆至 ApplePayController（VORT 门控 common.pay.applePay 专属，整文件可删）

    /**
     * 同时具有支付结果通知和退款通知功能
     * @param request
     * @return
     * @throws Exception
     */

    /**
     * 拉卡拉异步通知（支付成功；退款以同步为主，兼容异步退款推送）。
     * 单独走原始 body 通道：拉卡拉 SDK 验签需要字节级请求体 + Authorization 请求头，
     * Spring 用 @RequestBody Map 解析后会丢失原始格式导致验签失败。
     * 成功须响应固定 JSON：{ "code": "SUCCESS", "message": "执行成功" }，否则拉卡拉会重复通知。
     */
    @PostMapping(value = "/notify/lakala", consumes = "application/json")
    @Operation(summary = "拉卡拉支付/退款回调")
    public String lakalaNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        byte[] bytes = request.getInputStream().readAllBytes();
        String characterEncoding = request.getCharacterEncoding();
        String jsonString = new String(bytes, Optional.ofNullable(characterEncoding).orElse("UTF-8"));
        String authorization = request.getHeader("Authorization");
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("requestBody", jsonString);
        resMap.put("authorization", authorization);
        String notifyRes = orderPayService.payNotify(resMap, "lakala");
        response.setContentType("application/json;charset=UTF-8");
        if ("SUCCESS".equals(notifyRes)) {
            response.setStatus(200);
            return "{\"code\":\"SUCCESS\",\"message\":\"执行成功\"}";
        }
        response.setStatus(400);
        return "{\"code\":\"FAIL\",\"message\":\"失败\"}";
    }


    @PostMapping(value = "/notify/{payType}", consumes = "application/json")
    @Operation(summary = "支付回调")
    public String notify(@RequestBody Map<String, Object> resMap,
                         @PathVariable("payType") String payType) {
        return orderPayService.payNotify(resMap, payType);
    }

    @PostMapping(value = "/notify/{payType}", consumes = "application/x-www-form-urlencoded")
    @Operation(summary = "支付回调")
    public String notify(HttpServletRequest request,
                         @PathVariable("payType") String payType) {
        Map<String, Object> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = String.join(",", values);
            params.put(name, valueStr);
        }
        return orderPayService.payNotify(params, payType);
    }



    @PostMapping(value = "/refundNotify/{payType}", consumes = "application/json")
    @Operation(summary = "退款回调地址")
    public String refundNotify(@RequestBody Map<String, Object> resMap,
                               @PathVariable("payType") String payType) {
        return orderPayService.payRefundNotify(resMap, payType);
    }

    @PostMapping(value = "/refundNotify/{payType}", consumes = "application/x-www-form-urlencoded")
    @Operation(summary = "退款回调地址（表单）")
    public String refundNotifyForm(HttpServletRequest request,
                                   @PathVariable("payType") String payType) {
        Map<String, Object> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            params.put(name, String.join(",", requestParams.get(name)));
        }
        return orderPayService.payRefundNotify(params, payType);
    }
}
