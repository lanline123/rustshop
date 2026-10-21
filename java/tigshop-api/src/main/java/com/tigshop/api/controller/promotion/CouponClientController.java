package com.tigshop.api.controller.promotion;

import com.tigshop.bean.vo.promotion.CouponClientVO;
import com.tigshop.service.promotion.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 前台优惠券公开接口（无需 admin 权限）
 *
 * <p>用于 mall 端装修等场景按 ids 批量回显优惠券信息</p>
 *
 * @author Tigshop团队
 * @create 2026/05/26
 */
@RequiredArgsConstructor
@Tag(name = "前台优惠券")
@RestController
@RequestMapping("/api/promotion/coupon")
public class CouponClientController {

    private final CouponService couponService;

    @GetMapping("/listByIds")
    @Operation(summary = "按 ids 批量获取优惠券（前台展示用）")
    public List<CouponClientVO> listByIds(@RequestParam String ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty() && s.matches("\\d+"))
                .map(Integer::parseInt)
                .toList();
        if (idList.isEmpty()) {
            return Collections.emptyList();
        }
        return couponService.getClientCouponListByIds(idList);
    }
}
