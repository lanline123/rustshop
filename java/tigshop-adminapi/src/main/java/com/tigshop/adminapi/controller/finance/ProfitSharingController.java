package com.tigshop.adminapi.controller.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.param.finance.ProfitSharingSubmitParam;
import com.tigshop.bean.query.finance.ProfitSharingPageQuery;
import com.tigshop.bean.vo.finance.ProfitSharingOrderVO;
import com.tigshop.service.finance.ProfitSharingOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 分账结算管理（财务 &gt; 微信线上分账 &gt; 分账结算）
 *
 * @author Tigshop团队
 */
@Tag(name = "分账结算", description = "分账单列表 / 批量发起 / 手动完结")
@RequiredArgsConstructor
@RestController
@RequestMapping("/adminapi/finance/profitSharing")
public class ProfitSharingController {

    private final ProfitSharingOrderService profitSharingOrderService;

    @Operation(summary = "分账单分页列表")
    @GetMapping("/list")
    public Page<ProfitSharingOrderVO> list(ProfitSharingPageQuery query) {
        return profitSharingOrderService.listPage(query);
    }

    @Operation(summary = "批量发起分账")
    @PreAuthorize("@pms.hasPermission('profitSharingSettlementSubmit')")
    @PostMapping("/submitBatch")
    public void submitBatch(@RequestBody @Validated ProfitSharingSubmitParam param) {
        Integer operatorId = getCurrentUserId();
        profitSharingOrderService.submitBatchProfitSharing(
                param.getSettlementIds(), null, operatorId == null ? null : operatorId.longValue());
    }

    @Operation(summary = "手动完结分账单")
    @PreAuthorize("@pms.hasPermission('profitSharingSettlementFinish')")
    @PostMapping("/manualFinish")
    public void manualFinish(@RequestParam Long id) {
        Integer operatorId = getCurrentUserId();
        profitSharingOrderService.manualFinishProfitSharing(id, operatorId == null ? null : operatorId.longValue());
    }
}
