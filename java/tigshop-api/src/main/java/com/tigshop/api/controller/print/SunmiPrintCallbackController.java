// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.api.controller.print;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 商米云打印回调控制器（DR-01=α 占位实现）
 * <p>
 * MVP 阶段商米回调仅做"接收 + 返回 200"占位，<b>不触发</b> {@code PrintLog} 状态机从
 * PUSHED(1) 到 PRINTED(2) 的迁移。理由：
 * <ul>
 *     <li>REQ-007 的四态已由同步响应覆盖（PUSHED/PUSH_FAILED/OFFLINE）</li>
 *     <li>对齐飞鹅云"推送成功=终态"的行为基线，不引入平台间差异</li>
 * </ul>
 * 未来若升级到 DR-01=β/γ，只需把 TODO 替换为 {@code PrintLogService.markCallback(...)} 调用 +
 * 签名校验即可（{@code PrintLogService} 已预留 {@code markCallback} 契约）。
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/print/sunmi")
@Tag(name = "商米云打印回调", description = "商米云打印结果回调（MVP 占位实现）")
public class SunmiPrintCallbackController {

    /**
     * 商米云打印结果回调入口
     * <p>返回 {@code {"code":1,"msg":"ok"}} 避免商米重试。
     */
    @PostMapping("/callback")
    @Operation(summary = "接收商米云打印回调（占位）")
    public Map<String, Object> callback(@RequestBody(required = false) String body) {
        log.info("[Sunmi Callback] 收到商米云打印回调（MVP 占位）：{}", body);
        // TODO DR-01=β/γ：在此处：
        //   1. 用 SunmiCloudPrintApiUtils.verifyResponse(...) 校验 Sunmi-Sign 头，失败返回 401
        //   2. 解析 body 得到 order_id 与 status 映射（如 1=已打印/2=已删除/...）
        //   3. 调用 PrintLogService.markCallback(orderId, PrintLogStatusEnum.PRINTED/OFFLINE, body)
        return Map.of("code", 1, "msg", "ok");
    }
}
