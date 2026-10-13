package com.tigshop.adminapi.controller.common;

import cn.hutool.crypto.digest.DigestUtil;
import com.tigshop.common.utils.RedisCache;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 表单令牌校验
 *
 * @author Tigshop项目组
 * @create 2025年07月10日 14:35
 */
@RestController
@RequestMapping("/adminapi/common/csrf")
@Tag(name = "表单令牌校验")
@RequiredArgsConstructor
public class CSRFController {
    private final RedisCache redisCache;

    @GetMapping("/create")
    public String create() {
        String md5Hex1 = DigestUtil.md5Hex(String.valueOf(System.currentTimeMillis()));
        redisCache.setCacheObject(md5Hex1, md5Hex1, 10, TimeUnit.MINUTES);
        return md5Hex1;
    }
}