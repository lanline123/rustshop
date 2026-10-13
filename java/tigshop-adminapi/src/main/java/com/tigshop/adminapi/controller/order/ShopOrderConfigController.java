package com.tigshop.adminapi.controller.order;

import cn.hutool.json.JSON;
import com.tigshop.bean.dto.order.OrderConfigDTO;
import com.tigshop.service.shop.OrderConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * @author wzh
 */
@RestController
@RequestMapping("/adminapi/order/config")
@Tag(name = "店铺订单配置", description = "店铺订单配置")
@AllArgsConstructor
public class ShopOrderConfigController {


    // 注入OrderConfigService
    private final OrderConfigService orderConfigService;

    // 根据shopId和code获取配置详情
    @GetMapping("/detail")
    @Operation(summary = "获取配置详情")
    public JSON detail(@RequestParam(value = "shopId") Integer shopId, @RequestParam(value = "code") String code) {
        // 根据shopId和code获取配置详情
        return orderConfigService.shopOrderConfigDetail(shopId, code);
    }

    @PostMapping("/save")
    @Operation(summary = "保存配置")
    @PreAuthorize("@pms.hasPermission('orderConfigModifyManage')")
    public void save(@RequestBody OrderConfigDTO dto) {
        orderConfigService.saveOrderConfig(dto);
    }
}
