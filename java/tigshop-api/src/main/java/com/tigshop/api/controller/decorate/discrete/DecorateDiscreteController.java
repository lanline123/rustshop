package com.tigshop.api.controller.decorate.discrete;

import com.tigshop.bean.bo.decorate.discrete.OpenAdvertisingBO;
import com.tigshop.service.decorate.DecorateDiscreteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 装修组件控制器
 *
 * @author kidd
 * @since 2025/6/30 15:22
 */
@RequiredArgsConstructor
@Tag(name = "装修组件控制器")
@RestController
@RequestMapping("/api/decorate/discrete")
public class DecorateDiscreteController {

    private final DecorateDiscreteService decorateDiscreteService;

    @Operation(summary = "获取开屏广告")
    @GetMapping("/getOpenAdvertising")
    public OpenAdvertisingBO getOpenAdvertising() {
        return decorateDiscreteService.getOpenAdvertising();
    }
}
