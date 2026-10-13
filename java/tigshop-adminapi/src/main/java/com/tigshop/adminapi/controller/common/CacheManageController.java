package com.tigshop.adminapi.controller.common;

import com.tigshop.service.common.CacheManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 缓存管理 控制器
 *
 * @author kidd
 * @since 2025/4/8 16:03
 */
@RequiredArgsConstructor
@Tag(name = "配置管理")
@RestController
@RequestMapping("/adminapi/common/cacheManage")
public class CacheManageController {

    private final CacheManageService cacheManageService;

    @Operation(summary = "清除缓存")
    @PostMapping("/cleanup")
    public void cleanup(@RequestParam(value = "tag", required = false) String tag) {
        cacheManageService.cleanup(tag);
    }

}
