package com.tigshop.api.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.product.PriceInquiryDTO;
import com.tigshop.bean.dto.product.PriceInquiryReplyDTO;
import com.tigshop.bean.vo.product.PriceInquiryDetailVO;
import com.tigshop.bean.vo.product.PriceInquiryVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.product.PriceInquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台用户询价
 *
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/api/user/priceInquiry")
@Tag(name = "前台用户询价")
@Validated
public class UserPriceInquiryController {

    @Resource
    private PriceInquiryService priceInquiryService;

    @GetMapping("/list")
    @Operation(summary = "获取当前登录用户询价列表")
    public Page<PriceInquiryVO> list(PriceInquiryDTO dto) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId <= 0) {
            throw new GlobalException("用户未登录");
        }
        dto.setUserId(userId);
        dto.setUserIsDel(0);
        return priceInquiryService.list(dto);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取当前登录用户询价详情")
    public PriceInquiryDetailVO detail(@RequestParam Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId <= 0) {
            throw new GlobalException("用户未登录");
        }
        return priceInquiryService.detail(id, userId);
    }

    @PostMapping("/reply")
    @Operation(summary = "用户回复商家")
    public void reply(@RequestBody @Valid PriceInquiryReplyDTO dto) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId <= 0) {
            throw new GlobalException("用户未登录");
        }
        priceInquiryService.replyByUser(dto, userId);
    }

    @PostMapping("/del")
    @Operation(summary = "删除询价（仅用户侧隐藏）")
    public void del(@RequestBody @Valid OperateDTO dto) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId <= 0) {
            throw new GlobalException("用户未登录");
        }
        priceInquiryService.deleteByUser(dto.getId(), userId);
    }
}
