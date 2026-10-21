package com.tigshop.adminapi.controller.proxyshopping;

import com.tigshop.bean.dto.user.UserCouponListDTO;
import com.tigshop.bean.param.user.UserCouponClaimParam;
import com.tigshop.service.user.UserCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Tigshop项目组
 * @create 2025年10月24日 17:05
 */
@RestController
@RequestMapping("/adminapi/proxyShopping/coupon")
@Validated
@Tag(name = "代客下单优惠卷")
@PreAuthorize("@pms.hasPermission('proxyShoppingManage')")
@RequiredArgsConstructor
public class ProxyShoppingUserCouponController {
    private final UserCouponService userCouponService;

    @PostMapping("/claim")
    @Operation(summary = "领取优惠券")
    public void claim(@RequestBody @Validated UserCouponClaimParam param) {
        userCouponService.claim(param);
    }

}