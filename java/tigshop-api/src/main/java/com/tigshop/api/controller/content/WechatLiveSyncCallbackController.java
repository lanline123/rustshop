package com.tigshop.api.controller.content;

import com.tigshop.bean.dto.content.WechatLiveSyncCallbackDTO;
import com.tigshop.common.annotation.Idempotent;
import com.tigshop.service.content.WechatLiveOrderSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 微信直播同步回调控制器
 *
 * @author Tigshop团队
 * @create 2026年03月08日 16:56
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/content/wechatLiveSync")
@Tag(name = "微信直播订单同步回调", description = "微信直播订单同步回调接口")
public class WechatLiveSyncCallbackController {

    private final WechatLiveOrderSyncService wechatLiveOrderSyncService;

    /**
     * 接收微信直播同步回调
     * <p>请求体格式：JSON</p>
     * @param callbackDTO 回调数据
     */
    @PostMapping("/callback")
    @Operation(summary = "接收微信直播同步回调", description = "请求体格式：JSON")
    @Idempotent(key = "#callbackDTO.idempotentKey", prefix = "wechatLiveSyncCallback")
    public void callback(@Valid @RequestBody WechatLiveSyncCallbackDTO callbackDTO) {
        wechatLiveOrderSyncService.handleInboundCallback(callbackDTO);
    }
}
