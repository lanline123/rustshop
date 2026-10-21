package com.tigshop.api.controller.finance;

import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.dto.finance.profitsharing.NotifyPayload;
import com.tigshop.bean.dto.finance.profitsharing.ProfitSharingNotifyResult;
import com.tigshop.bean.model.finance.ProfitSharingOrder;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.finance.ProfitSharingOrderService;
import com.tigshop.service.finance.profitsharing.ProfitSharingClientRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 线上分账回调
 *
 * @author Tigshop团队
 */
@Slf4j
@Tag(name = "分账回调", description = "接收支付渠道分账动账通知")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/order/pay/profitSharing/notify")
public class ProfitSharingNotifyController {

    private static final String PAY_CHANNEL_WECHAT = "WECHAT";
    private static final String PAY_CHANNEL_HUIFU = "HUIFU";
    private static final String PAY_CHANNEL_ALIPAY = "ALIPAY";

    private final ProfitSharingClientRegistry profitSharingClientRegistry;
    private final ProfitSharingOrderService profitSharingOrderService;




    private Map<String, String> handleNotify(String payChannel, NotifyPayload payload) {
        try {
            ProfitSharingNotifyResult result = profitSharingClientRegistry.get(payChannel).parseNotification(payload);
            validateNotifyChannel(payChannel, result.getOutOrderNo());
            profitSharingOrderService.handleNotify(result.getOutOrderNo(), result.getStatus(), result.getOrderId());
            return Map.of("code", "SUCCESS", "message", "成功");
        } catch (Exception e) {
            log.error("处理分账回调异常", e);
            return Map.of("code", "FAIL", "message", e.getMessage());
        }
    }

    private void validateNotifyChannel(String payChannel, String outOrderNo) {
        if (StrUtil.isBlank(outOrderNo)) {
            throw new GlobalException("分账回调缺少分账单号");
        }
        ProfitSharingOrder order = profitSharingOrderService.lambdaQuery()
                .eq(ProfitSharingOrder::getOutOrderNo, outOrderNo)
                .one();
        if (order != null && !payChannel.equalsIgnoreCase(order.getPayChannel())) {
            throw new GlobalException("分账回调渠道与分账单渠道不匹配");
        }
    }
}
