package com.tigshop.adminapi.controller.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.param.promotion.timediscount.TimeDiscountSaveParam;
import com.tigshop.bean.query.promotion.timediscount.TimeDiscountPageQuery;
import com.tigshop.bean.param.promotion.timediscount.TimeDiscountEditParam;
import com.tigshop.bean.vo.promotion.TimeDiscountDetailVO;
import com.tigshop.bean.vo.promotion.TimeDiscountVO;
import com.tigshop.service.promotion.TimeDiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 限时折扣控制器
 *
 * @author kidd
 * @create 2025/7/1
 */
@RequiredArgsConstructor
@Tag(name = "限时折扣", description = "限时折扣功能")
@RestController
@RequestMapping("/adminapi/promotion/timeDiscount")
public class TimeDiscountController {

    private final TimeDiscountService timeDiscountService;

    @Operation(summary = "获取列表")
    @GetMapping("/list")
    public Page<TimeDiscountVO> list(TimeDiscountPageQuery pageQuery) {
        return timeDiscountService.list(pageQuery);
    }

    @Operation(summary = "获取详情")
    @GetMapping("/detail")
    public TimeDiscountDetailVO detail(@RequestParam("discountId") Integer discountId) {
        return timeDiscountService.detail(discountId);
    }

    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('timeDiscountModifyManage')")
    @PostMapping("/create")
    public void create(@RequestBody @Validated TimeDiscountSaveParam param) {
        timeDiscountService.create(param);
    }

    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('timeDiscountModifyManage')")
    @PostMapping("/update")
    public void update(@Valid @RequestBody TimeDiscountEditParam param) {
        timeDiscountService.update(param);
    }

    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('timeDiscountModifyManage')")
    @PostMapping("/del")
    public void del(@Valid @RequestBody OperateDTO operateDTO) {
        timeDiscountService.del(operateDTO.getId());
    }

    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('timeDiscountModifyManage')")
    @PostMapping("/batch")
    public void batch(@Valid @RequestBody BatchDTO batchDTO) {
        timeDiscountService.batch(batchDTO);
    }
}

