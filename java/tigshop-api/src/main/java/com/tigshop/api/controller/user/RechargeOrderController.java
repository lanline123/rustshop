package com.tigshop.api.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.ClientUserRechargeOrderPayDTO;
import com.tigshop.bean.dto.finance.ClientUserRechargeOrderUpdateDTO;
import com.tigshop.bean.dto.finance.UserRechargeOrderListDTO;
import com.tigshop.bean.dto.finance.UserRechargeOrderPayDTO;
import com.tigshop.bean.query.promotion.recharge.RechargeSettingPageQuery;
import com.tigshop.bean.vo.finance.ClientUserRechargeOrderVO;
import com.tigshop.bean.vo.finance.RechargeUpdateOrderVO;
import com.tigshop.bean.vo.finance.UserRechargeOrderPayVO;
import com.tigshop.bean.vo.order.pay.PayCreateVO;
import com.tigshop.bean.vo.promotion.RechargeSettingVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.finance.UserRechargeOrderService;
import com.tigshop.service.pay.PaymentService;
import com.tigshop.service.promotion.RechargeSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 充值
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RestController
@RequestMapping("/api/user/rechargeOrder")
@Tag(name = "前台用户账户充值信息")
public class RechargeOrderController {
    @Resource
    private UserRechargeOrderService userRechargeOrderService;
    @Resource
    private RechargeSettingService rechargeSettingService;
    @Resource
    private PaymentService paymentService;

    @GetMapping("/list")
    @Operation(summary = "充值记录列表")
    public Page<ClientUserRechargeOrderVO> list(UserRechargeOrderListDTO listDTO) {
        Integer userId = getCurrentUserId();
        listDTO.setUserId(userId);
        return userRechargeOrderService.clientList(listDTO);
    }

    @GetMapping("/setting")
    @Operation(summary = "充值配置")
    public List<RechargeSettingVO> settingPage(RechargeSettingPageQuery pageQuery) {
        return rechargeSettingService.settingPage(pageQuery).getRecords();
    }

    @GetMapping("/paymentList")
    @Operation(summary = "充值支付方式")
    public List<String> paymentList() {
        return paymentService.getAvailablePayment("recharge");
    }


    @PostMapping("/update")
    @Operation(summary = "充值申请")
    public RechargeUpdateOrderVO update(@RequestBody ClientUserRechargeOrderUpdateDTO updateDTO) {
        Integer userId = getCurrentUserId();
        updateDTO.setUserId(userId);
        return RechargeUpdateOrderVO.builder().orderId(userRechargeOrderService.rechargeOperation(updateDTO)).build();
    }

    @PostMapping("/pay")
    @Operation(summary = "充值支付")
    public Map<String, Object> pay(@RequestBody ClientUserRechargeOrderPayDTO payDTO) {
        UserRechargeOrderPayVO userRechargeOrderPayVO = userRechargeOrderService.pay(payDTO, getCurrentUserId());
        List<String> paymentList = paymentService.getAvailablePayment("order");
        //过滤线下支付
        paymentList = paymentList.stream().filter(method -> !"offline".equals(method)).toList();
        return Map.of("order", userRechargeOrderPayVO, "paymentList", paymentList);
    }

    @GetMapping("/checkStatus")
    @Operation(summary = "获取充值支付状态")
    public Integer checkStatus(@RequestParam(value = "id") Integer id) {
        return userRechargeOrderService.checkStatus(id, getCurrentUserId());
    }

    @PostMapping("/create")
    @Operation(summary = "充值支付")
    public PayCreateVO create(@RequestBody UserRechargeOrderPayDTO payDTO) {
        String type = payDTO.getType();
        if ("cash".equals(type)) {
            throw new GlobalException("请重新选择充值支付方式");
        }
        return userRechargeOrderService.createPay(payDTO, getCurrentUserId());
    }
}
