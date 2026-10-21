package com.tigshop.api.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.query.promotion.coupon.CouponPageQuery;
import com.tigshop.bean.dto.user.UserCouponListDTO;
import com.tigshop.bean.param.user.UserCouponClaimParam;
import com.tigshop.bean.param.user.UserCouponIdParam;
import com.tigshop.bean.vo.promotion.CouponClientVO;
import com.tigshop.bean.vo.promotion.CouponDetailVO;
import com.tigshop.bean.vo.user.UserCouponVO;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.promotion.CouponService;
import com.tigshop.service.user.UserCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户优惠券
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RestController
@RequestMapping(("/api/user/coupon"))
@Tag(name = "前台用户优惠券列表")
@Validated
public class UserCouponController {
    @Resource
    UserCouponService userCouponService;
    @Resource
    CouponService couponService;

    @GetMapping("/list")
    @Operation(summary = "会员优惠券列表")
    public Page<UserCouponVO> list(UserCouponListDTO dto) {
        dto.setUserId(SecurityUtils.getCurrentUserId());
        return userCouponService.list(dto);
    }

    @PostMapping("/del")
    @Operation(summary = "删除会员优惠券")
    public void del(@RequestBody UserCouponIdParam param) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new GlobalException("用户未登录");
        }
        userCouponService.deleteByUser(param.getId(), userId);
    }

    @GetMapping("/detail")
    @Operation(summary = "会员优惠券详情")
    public CouponDetailVO detail(@RequestParam("id") Integer id) {
        return userCouponService.detail(id);
    }

    @PostMapping("/claim")
    @Operation(summary = "领取优惠券")
    public void claim(@RequestBody @Validated UserCouponClaimParam param) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new GlobalException("用户未登录");
        }
        param.setUserId(userId);
        userCouponService.claim(param);
    }

    @GetMapping("getList")
    @Operation(summary = "优惠券列表")
    public Page<CouponClientVO> getList(CouponPageQuery listDTO) {
        listDTO.setIsShow(1);
        listDTO.setValidDate(1);
        listDTO.setReceiveDate(1);
        listDTO.setReceiveFlag(1);
        return couponService.getList(listDTO);
    }
}
